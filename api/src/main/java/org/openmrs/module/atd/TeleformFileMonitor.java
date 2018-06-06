/**
 * 
 */
package org.openmrs.module.atd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.atd.datasource.FormDatasource;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.FileExt;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * @author Vibha Anand
 * 
 */

public class TeleformFileMonitor extends AbstractTask
{
	private static Log log = LogFactory.getLog(TeleformFileMonitor.class);

	private static ConcurrentHashMap<FormInstance, TeleformFileState> pendingStatesWithFilename = new ConcurrentHashMap<FormInstance, TeleformFileState>();
	private static ConcurrentHashMap<FormInstance, TeleformFileState> pendingStatesWithoutFilename = new ConcurrentHashMap<FormInstance, TeleformFileState>();
	private static ConcurrentHashMap<String, LinkedList<String>> tifFileQueue = new ConcurrentHashMap<String, LinkedList<String>>();
	//private static Set<String> unparseableFiles = null;
	private static boolean isInitialized = false;
	private boolean keepLooking = true;

	@Override
	public void initialize(TaskDefinition config)
	{
		super.initialize(config);
		//unparseableFiles = new HashSet<String>();
		isInitialized = false;
		pendingStatesWithFilename = new ConcurrentHashMap<FormInstance, TeleformFileState>();
		pendingStatesWithoutFilename = new ConcurrentHashMap<FormInstance, TeleformFileState>();
		tifFileQueue = new ConcurrentHashMap<String, LinkedList<String>>();
	}

	@Override
	public void execute()
	{
		while (this.keepLooking)
		{
			Context.openSession();

			try
			{
				if (!isInitialized)
				{
					init();
				}
				
				ATDService atdService = Context.getService(ATDService.class);
				processPendingStatesWithoutFilename(atdService);
				processPendingStatesWithFilename(atdService);
				processTifFiles();
			} 
			catch (Exception e)
			{
				log.error(e.getMessage());
				log.error(Util.getStackTrace(e));
			} finally
			{
				Context.closeSession();			
			}
			try {
	            Thread.sleep(100);//check every tenth second
            }
            catch (InterruptedException e) {
	            log.error("Error generated", e);
	            Thread.currentThread().interrupt();
            }
		}
	}
	
	public static void init()
	{
		log.info("Initializing Teleform monitor...");
		fillUnfinishedStates();
		
		isInitialized = true;
		log.info("Finished initializing Teleform monitor.");
	}
	
	public static void fillUnfinishedStates()
	{
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Calendar todaysDate = Calendar.getInstance();
		todaysDate.set(Calendar.HOUR_OF_DAY, 0);
		todaysDate.set(Calendar.MINUTE, 0);
		todaysDate.set(Calendar.SECOND, 0);
		LocationService locationService = Context.getLocationService();

		List<Location> locations = locationService.getAllLocations();
		
		for(Location location:locations){
		
		Set<LocationTag> tags = location.getTags();
		
		if(tags != null){
		
			for(LocationTag tag:tags){
				Integer locationId = location.getLocationId();
				Integer locationTagId = tag.getLocationTagId();
		List<PatientState> unfinishedStatesToday = chirdlutilbackportsService.
			getUnfinishedPatientStatesAllPatients(todaysDate.getTime(),locationTagId,locationId);
				
		int numUnfinishedStates = unfinishedStatesToday.size();
		double processedStates = 0;
		
		log.info("fillUnfinishedStates(): Starting Today's state initialization....");
		for(PatientState currPatientState:unfinishedStatesToday)
		{	
			State state = currPatientState.getState();
			if (state != null)
			{
				StateAction stateAction = state.getAction();

				try
				{
					if (stateAction!=null&&stateAction.getActionName().equalsIgnoreCase(
							"CONSUME FORM INSTANCE"))
					{
						TeleformFileState teleformFileState = TeleformFileMonitor
							.addToPendingStatesWithoutFilename(
								currPatientState.getFormInstance());
						teleformFileState.addParameter("patientState",
								currPatientState);
					}
					HashMap<String,Object> parameters = new HashMap<String,Object>();
					parameters.put("formInstance", currPatientState.getFormInstance());
					BaseStateActionHandler handler = BaseStateActionHandler.getInstance();
					handler.processAction(stateAction, currPatientState.getPatient(),
							currPatientState,parameters);
				} catch (Exception e)
				{
					log.error(e.getMessage());
					log
							.error(org.openmrs.module.chirdlutil.util.Util
									.getStackTrace(e));
				}
			}
			if(processedStates%100==0){
				log.info("State initialization is: "+(int)((processedStates/numUnfinishedStates)*100)+"% complete. "+
						processedStates+" out of "+numUnfinishedStates+" processed.");
			}
			processedStates++;
		}
		
		log.info("Today's state initialization is: "+(int)((processedStates/numUnfinishedStates)*100)+"% complete.");
		}}}
		
		//TODO: Make the following two lines dependent on a new global property to control them.
		Thread thread = new Thread(new InitializeOldStates());
		thread.start();
//		ThreadManager threadManager = ThreadManager.getInstance();
//		threadManager.execute(new InitializeOldStates(), ThreadManager.NO_LOCATION);
	}
	
	@Override
	public void shutdown()
	{
		this.keepLooking = false;
		
		//unparseableFiles = new HashSet<String>();
		pendingStatesWithFilename = new ConcurrentHashMap<FormInstance, TeleformFileState>();
		pendingStatesWithoutFilename = new ConcurrentHashMap<FormInstance, TeleformFileState>();
		tifFileQueue = new ConcurrentHashMap<String, LinkedList<String>>();
		
		super.shutdown();
	}
	
	public static TeleformFileState addToPendingStatesWithFilename(FormInstance formInstance,
			String filename)
	{
		TeleformFileState tfFileState = new TeleformFileState();
		try{
				
			tfFileState.setFullFilePath(filename);
			tfFileState.setFormInstance(formInstance);
			pendingStatesWithFilename.put(formInstance, tfFileState);
			
			
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		
		return tfFileState;
	}
	
	public static TeleformFileState addToPendingStatesWithoutFilename(FormInstance formInstance)
	{
		TeleformFileState tfFileState = new TeleformFileState();
		try{				
			pendingStatesWithoutFilename.put(formInstance, tfFileState);
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		
		return tfFileState;
	}

	public static void addToTifFileProcessing(FormInstance formInstance, String directoryName, String fileExt )
	{
		if(formInstance == null){
			log.error("FormInstance cannot be null when adding to TIF processing");
			return;
		}
		try{
			String filename = directoryName + "\\tif\\" + Integer.toString(formInstance.getFormInstanceId()) + fileExt;
			
			if(!tifFileQueue.containsKey(directoryName))
			{
				LinkedList<String> queue = new LinkedList<String> ();
				tifFileQueue.put(directoryName, queue);
			}
	
			tifFileQueue.get(directoryName).add(filename);
		}catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		
		//log.info("Added: " + directoryName + ": " + filename);
	}
	
	private static void processPendingStatesWithoutFilename(ATDService atdService)
	{
		FormService formService = Context.getFormService();
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		ArrayList<String> exportDirectories = chirdlUtilBackportsService.getFormAttributesByNameAsString("defaultExportDirectory");
		String[] fileExtensions = new String[]
		{ ".xml", ".xmle" };

		if (exportDirectories == null || exportDirectories.size() == 0)
		{
			return;
		}

		//look at all .xml files in the export directories
		for (String currExportDirectory : exportDirectories)
		{
			File[] files = IOUtil.getFilesInDirectory(currExportDirectory,
					fileExtensions);
			if (files == null||files.length==0)
			{
				continue;
			}
			LogicService logicService = Context.getLogicService();
			FormDatasource formDatasource = (FormDatasource) logicService
					.getLogicDataSource("form");
			
			for (File currFile : files)
			{
				String filename = currFile.getPath();
				FormInstance formInstance = null;

				// don't keep trying to parse a file that can't be
				// parsed
				//if (unparseableFiles.contains(filename))
				//{
				//	continue;
				//}
				
				try
				{
					// parse out the formInstance
				
					InputStream input;
					try
					{
						input = new FileInputStream(filename);
					} catch (Exception e)
					{
						if (!e.getMessage().contains(
										"The process cannot access the file because it is being used by another process"))
						{
							log.error(Util.getStackTrace(e));
						}
						continue;
					}
	
					formInstance = formDatasource.parseTeleformXmlFormat(input,formInstance,null);
					input.close();
					//This is an old form with a single key (formInstanceId) barcode
					//we need to figure out the formId, locationId, and locationTagId
					if (formInstance == null)
					{
						Integer locationTagId = null;
						List<FormAttributeValue> formAttrValues = chirdlUtilBackportsService
							.getFormAttributeValuesByValue(
								org.openmrs.module.chirdlutil.util.IOUtil.getDirectoryName(filename));

						if (formAttrValues != null&&formAttrValues.size()>0)
						{
							FormAttributeValue value = formAttrValues.get(0);
							formInstance = new FormInstance();
							formInstance.setLocationId(value.getLocationId());
							formInstance.setFormId(value.getFormId());
							locationTagId = value.getLocationTagId();

							try
							{
								input = new FileInputStream(filename);
							} catch (Exception e)
							{
								if (!e.getMessage().contains(
												"The process cannot access the file because it is being used by another process"))
								{
									log.error(Util.getStackTrace(e));
								}
								continue;
							}
							formInstance = formDatasource.parseTeleformXmlFormat(input,
									formInstance, locationTagId);
							input.close();

							if(formInstance == null){
							    try {
	                                // Strictly Stick to what was asked in Ticket #216 (the request in the ticket was just updated today),
	                                // with the addition of the extra backward slashes around 'bad scans' to make it compilable.  
							    	File badScansDir = new File(currExportDirectory,"bad scans");
							    	if (!badScansDir.exists()) {
							    		badScansDir.mkdirs();
							    	}
	                                IOUtil.copyFile(filename,currExportDirectory+"\\bad scans\\"
	                                        + IOUtil.getFilenameWithoutExtension(filename) + ".xml");
	                                IOUtil.deleteFile(filename);
                                }
                                catch (Exception e) {
									log.error("Could not copy " + filename + " to " + currExportDirectory + "\\bad scans\\"
									        + IOUtil.getFilenameWithoutExtension(filename) + ".xml");
                                }
								continue;
							}
							
							// we need to figure out the correct formId now
							// that we have
							// the formInstanceId
							for (FormAttributeValue formAttrValue : formAttrValues)
							{
								FormInstance lookupFormInstance = new FormInstance(
										formAttrValue.getLocationId(),
										formAttrValue.getFormId(), formInstance
												.getFormInstanceId());
								PatientState patientState = 
									org.openmrs.module.atd.util.Util.getProducePatientStateByFormInstanceAction(
										lookupFormInstance);
								if (patientState != null)
								{
									formInstance = new FormInstance(
											patientState.getLocationId(),
											patientState.getFormId(),
											patientState.getFormInstanceId());
									break;
								}
							}
						}
					}
					

					if (formInstance != null){
									
						TeleformFileState tfFileState = pendingStatesWithoutFilename.get(formInstance);

						if(tfFileState != null){
							// rename the consumed file by changing extension to .20
							// rename here so TeleformFileMonitor doesn't process it
							// more than once
							String newFilename = IOUtil.getDirectoryName(filename)+"/"+formInstance.toString()+".20";
							
							IOUtil.renameFile(filename, newFilename);
							
							//if file rename fails, don't continue processing otherwise
							//the file will be processed more than once
							File newFile = new File(newFilename);
							if(!newFile.exists()){
								continue;
							}
							
							// check to make sure the original file is gone.  If not, don't continue processing.  Otherwise 
							// the file will be processed more than once.
							File oldFile = new File(filename);
							if (oldFile.exists()) {
								IOUtil.deleteFile(newFilename);
								continue;
							}
							    
							tfFileState.setFullFilePath(newFilename);
							tfFileState.setFormInstance(formInstance);
							pendingStatesWithoutFilename.remove(formInstance);
							recordMedium(formInstance, filename);
							atdService.fileProcessed(tfFileState);
							
						}else{
							
							//see if it is retired 
							List<PatientState> states = chirdlUtilBackportsService.getPatientStatesByFormInstance(formInstance,true);
							
							if (states != null && states.size() > 0) {
								PatientState firstState = states.get(0);
								chirdlUtilBackportsService.unretireStatesBySessionId(firstState.getSessionId());
								states = 
									chirdlUtilBackportsService.getPatientStatesBySession(firstState.getSessionId(), false);
								
								for (PatientState formInstState : states) {
									
									//only process unfinished states for this sessionId
									if(formInstState.getEndTime() != null){
										continue;
									}
									
									StateAction stateAction = formInstState.getState().getAction();
									Patient patient = formInstState.getPatient();
									
									try {
										if (stateAction != null
										        && (stateAction.getActionName().equalsIgnoreCase("CONSUME FORM INSTANCE"))) {
											TeleformFileState teleformFileState = TeleformFileMonitor
											        .addToPendingStatesWithoutFilename(formInstState.getFormInstance());
											teleformFileState.addParameter("patientState", formInstState);
										}
										HashMap<String, Object> parameters = new HashMap<String, Object>();
										parameters.put("formInstance", formInstState.getFormInstance());
										BaseStateActionHandler handler = BaseStateActionHandler.getInstance();
										handler.processAction(stateAction, patient, formInstState, parameters);
									}
									catch (Exception e) {
										log.error(e.getMessage());
										log.error(org.openmrs.module.chirdlutil.util.Util.getStackTrace(e));
									}
								}
							} else {
								
								//This means we have found a .xml file that is not in pending processing.
								//This is most likely a rescan. We need to see if a scan already exists
								//for the session that has this formInstance
								
								//get the session that goes with this formInstanceId
								PatientState patientState = 
									org.openmrs.module.atd.util.Util.getProducePatientStateByFormInstanceAction(formInstance);
//								PatientState patientState = chirdlUtilBackportsService.getPatientStateByFormInstanceAction(formInstance,
//								    action);
								
								if (patientState != null) {
									String formName = formService.getForm(formInstance.getFormId()).getName();
									
									Integer sessionId = patientState.getSessionId();
									//see if consume exists for the session
									//if so, then it is a rescan
									
									String stateName = null;
									
									//if a form name is not configured assume JIT
									if (patientState.getState().getFormName() != null) {
										stateName = formName + "_process";
									} else {
										stateName = "JIT_process";
									}
									
									State state = chirdlUtilBackportsService.getStateByName(stateName);
									List<PatientState> patientStates = chirdlUtilBackportsService.getPatientStateBySessionState(sessionId,
									    state.getStateId());
									
									if (patientStates != null && patientStates.size() > 0) {
										//assume JIT if no form name is configured
										if (patientState.getState().getFormName() != null) {
											stateName = formName + "_rescan";
										} else {
											stateName = "JIT_rescan";
										}
										State currState = chirdlUtilBackportsService.getStateByName(stateName);
										patientState = 
											org.openmrs.module.atd.util.Util.getProducePatientStateByFormInstanceAction(
												formInstance);
//										patientState = chirdlUtilBackportsService.getPatientStateByFormInstanceAction(formInstance, action);
										try {
											patientState = chirdlUtilBackportsService.addPatientState(patientState.getPatient(), currState,
											    patientState.getSessionId(), patientState.getLocationTagId(), patientState
											            .getLocationId(), patientState.getFormInstance());
											tfFileState = new TeleformFileState();
											String newFilename = IOUtil.getDirectoryName(filename) + "/"
											        + formInstance.toString() + "_rescan.20";
											
											File newFile = new File(newFilename);
											if (newFile.exists()) {
												IOUtil.deleteFile(newFilename);
												//if file delete fails, don't continue processing otherwise
												//the file will be processed more than once
												newFile = new File(newFilename);
												if (newFile.exists()) {
													continue;
												}
											}										
											IOUtil.renameFile(filename, newFilename);
											//if file rename fails, don't continue processing otherwise
											//the file will be processed more than once
											newFile = new File(newFilename);
											if (!newFile.exists()) {
												continue;
											}
											
											// check to make sure the original file is gone.  If not, don't continue 
											// processing.  Otherwise the file will be processed more than once.
											File oldFile = new File(filename);
											if (oldFile.exists()) {
												IOUtil.deleteFile(newFilename);
												continue;
											}
											
											tfFileState.setFullFilePath(newFilename);
											tfFileState.setFormInstance(formInstance);
											tfFileState.addParameter("patientState", patientState);
											recordMedium(formInstance, filename);
											atdService.fileProcessed(tfFileState);
										}
										catch (Exception e) {
											log.error("RESCAN for formInstanceId: " + formInstance.getFormInstanceId()
											        + " failed.");
											log.error(Util.getStackTrace(e));
										}
									}
								}
							}
						}
					}
				} catch (Exception e)
				{
					log.error("Error processing filename: " + filename);
					log.error(e.getMessage());
					log.error(Util.getStackTrace(e));
					//unparseableFiles.add(filename);
				}
			}
		}
	}

	private static void processPendingStatesWithFilename(ATDService atdService) throws APIException, Exception
	{
		String filename = "";
		
		Iterator <FormInstance> iterator = pendingStatesWithFilename.keySet().iterator();
		while (iterator.hasNext())
		{
			FormInstance instance = iterator.next();
			TeleformFileState tfState = pendingStatesWithFilename.get(instance);
			if(tfState.getCriteria().equalsIgnoreCase("GT_SENTINEL_TIME"))
			{
				filename = tfState.getDirectoryName() + "\\" + getLastModifiedFilename(tfState.getDirectoryName(), tfState.getFileExtension(), tfState.getSentinelDate(), tfState.getCriteria());
			}
			if(tfState.getCriteria().equalsIgnoreCase("EXISTS")){
				filename = tfState.getFullFilePath();
			}
			//filename will be null for scanned files
			if(filename == null){
				log.error("Filename name for formInstanceId: "+instance.getFormInstanceId()+" and formId: "+instance.getFormId()+" is null.");
				continue;
			}
			
			// Check to see if a PDF file was created
			if (filename.toLowerCase().endsWith(ChirdlUtilConstants.FILE_EXTENSION_PDF)) {
				File pdfFilename = new File(filename);
				if (pdfFilename.exists()) {
					iterator.remove();
					atdService.fileProcessed(tfState);
					continue;
				}
			}
			
			int index = filename.lastIndexOf(".");
			
			String filenameWithoutExtension = null;
			
			if(index > -1){
				filenameWithoutExtension = filename.substring(0, index);
			}
			
			File twentyFilename = new File(filenameWithoutExtension+".20");
			File twentyTwoFilename = new File(filenameWithoutExtension+".22");

			if(twentyFilename.exists()||twentyTwoFilename.exists()){
				// event file processed
				iterator.remove();
				atdService.fileProcessed(tfState);
			}
		}
	}
	
	private static void processTifFiles() throws APIException, Exception
	{
			Iterator <String> iterator = tifFileQueue.keySet().iterator();
			while (iterator.hasNext())
			{
				String directory = iterator.next();
				log.info("----------" + directory + "-----------------");
				LinkedList<String> filenames = tifFileQueue.get(directory);
				
				modifyFilenames(directory, ".tif", filenames);
			}
		}
	
	/*
	 * Find the last modified file in a folder that is modified relative to the sentinelDateTime given the criteria
	 */
	private static String getLastModifiedFilename(String directoryName, String fileExt, long sentinelDateTime, String criteria)
	{
		
		File directory;
		File[] files;
		String lastModifiedFilename = "";
 
		directory = new File(directoryName);
		FilenameFilter filter = new FileExt(fileExt);
		
		if (directory.isDirectory())
		{
			files = directory.listFiles(filter);
			
			File lastModifiedFile = null;
			for (int i = 0; i < files.length; i++)
			{
				if(files[i].isFile())
				{
					if(lastModifiedFile == null)
						lastModifiedFile = files[i];
					
					if(lastModifiedFile != null)
					{
						if(lastModifiedFile.lastModified() < files[i].lastModified())
							lastModifiedFile = files[i];
					}
				}
			}
			if(lastModifiedFile != null)
			{
				if(criteria.equals("GT_SENTINEL_TIME") )
				{
					if(lastModifiedFile.lastModified() >= sentinelDateTime)
					{
						lastModifiedFilename = lastModifiedFile.getName();
					}
						
				}
				else if(criteria.length()==0)
				{
					lastModifiedFilename = lastModifiedFile.getName();
				}
					
			}
		}
		return lastModifiedFilename;
	}

	/*
	 * Find the last modified file in a folder that is modified relative to the sentinelDateTime given the criteria
	 */
	private static int modifyFilenames(String directoryName, String fileExt, LinkedList<String> filenames)
	{
		
		File directory;
		File[] files;
		int i = 0;
		
		directory = new File(directoryName);
		FilenameFilter filter = new FileExt(fileExt);
		
		if (directory.isDirectory())
		{
			files = directory.listFiles(filter);
			
			Arrays.sort(files, new Comparator() {
			    public int compare(Object o1, Object o2) {
			      File f1 = (File) o1; File f2 = (File) o2;
			      return (int) (f1.lastModified() - f2.lastModified());
			    }
			});
			
			File thisFile = null;
			
			ListIterator<String> iterator = filenames.listIterator(0);
			while (iterator.hasNext() && i < files.length)
			{
				String tifFilename = iterator.next();
				if(files[i].isFile())
				{
					thisFile = files[i];
					IOUtil.renameFile(thisFile.getPath(), tifFilename);
					iterator.remove();
					log.info("Renamed file:" + thisFile.getName() + "---->" + tifFilename);
					i++;
				}
			}
			
			
		}
		return i;
	}

	/**
	 * Records the medium for which the form was populated.  This is based on the file extension.  If the extension is 
	 * 'xmle', the medium will be set to 'electronic'.  Otherwise, it will be set to 'paper'.
	 * 
	 * @param formInstance The form instance for which to record the medium.
	 * @param filename The name of the file (including extension) being processed.
	 */
	private static void recordMedium(FormInstance formInstance, String filename) {
		// Record population method as 'paper' or 'electronic'
		if (filename == null) {
			log.error("The filename parameter must contain a non-null value");
			return;
		}
		
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		FormInstanceAttribute fia = chirdlutilbackportsService.getFormInstanceAttributeByName("medium");
		if (fia == null) {
			log.error("Form Instance Attribute 'medium' does not exist.  The form instance medium will not be " +
					"recorded");
			return;
		}
		
		FormInstanceAttributeValue fiav = new FormInstanceAttributeValue();
		fiav.setFormId(formInstance.getFormId());
		fiav.setFormInstanceAttributeId(fia.getFormInstanceAttributeId());
		fiav.setFormInstanceId(formInstance.getFormInstanceId());
		fiav.setLocationId(formInstance.getLocationId());
		if (filename.endsWith(".xmle")) {
			fiav.setValue("electronic");
		} else {
			fiav.setValue("paper");
		}
		
		chirdlutilbackportsService.saveFormInstanceAttributeValue(fiav);
	}
}
