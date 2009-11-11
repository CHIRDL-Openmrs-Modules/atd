/**
 * 
 */
package org.openmrs.module.atd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.atd.datasource.TeleformExportXMLDatasource;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;

import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * @author Vibha Anand
 * 
 */

public class TeleformFileMonitor extends AbstractTask
{
	private static Log log = LogFactory.getLog(TeleformFileMonitor.class);

	private static ConcurrentHashMap<FormInstance, TeleformFileState> pendingStatesWithFilename = null;
	private static ConcurrentHashMap<FormInstance, TeleformFileState> pendingStatesWithoutFilename = null;
	private static ConcurrentHashMap<String, LinkedList<String>> tifFileQueue = null;
	//private static Set<String> unparseableFiles = null;
	private static boolean isInitialized = false;
	private boolean keepLooking = true;
	
	private static TaskDefinition taskConfig;
	private final static Lock aopLock = new ReentrantLock();
	private final static Condition goTFStates = aopLock.newCondition();
	private static Boolean statesBeingProcessedByAOP = false;
	private static Date lastMoveMergeFiles;
	private static Date nextLatestMoveMergeFiles;
	private static Boolean TFAMP_Alert = false;
	
	@Override
	public void initialize(TaskDefinition config)
	{
		taskConfig = config;
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
				if (Context.isAuthenticated() == false)
					authenticate();
				
				ArrayList<TeleformFileState> listTFstatesProcessed = new ArrayList<TeleformFileState>();
				
				processPendingStatesWithoutFilename(listTFstatesProcessed);
				processPendingStatesWithFilename(listTFstatesProcessed);
				processTifFiles();
				kickProcess(listTFstatesProcessed);
				LocationService locationService = Context.getLocationService();
				List<Location> locations = locationService.getAllLocations();
				for(Location location:locations){
					Set<LocationTag> tags = location.getTags();
					if(tags != null){
						for(LocationTag tag:tags){
							moveMergeFiles(tag.getLocationTagId(),location.getLocationId());
						}
					}
				}
			} 
			catch (Exception e)
			{
				log.error(e.getMessage());
				log.error(Util.getStackTrace(e));
			} finally
			{
				Context.closeSession();			
			}
		}
	}
	
	public static void init()
	{
		log.info("Initializing Teleform monitor...");

		try
		{
			//call the initialization method if one has been set in the task property
			String initClassString = taskConfig.getProperty("initClass");
			String initMethodString = taskConfig.getProperty("initMethod");
			if(initClassString == null || initMethodString == null)
			{
				return;
			}
			//class the initialization method is in
			Class clas = null;

			boolean isMyModuleStarted = false;
			List<Module> startedModules = new ArrayList<Module>();
			startedModules.addAll(ModuleFactory.getStartedModules());
			for (Module r:startedModules){
				log.info("Module started: " + r.getName());
				if(initClassString.toLowerCase().contains(r.getName().toLowerCase()))
				{
					isMyModuleStarted = true;
				}
			}
			try
			{
				if(startedModules.size() > 0 && isMyModuleStarted ) {
					log.info("Teleform monitor: Loading class" + initClassString + "...");
					clas = OpenmrsClassLoader.getInstance().loadClass(initClassString);
				}
				else
				{
					log.info("Teleform monitor: MODULE NOT STARTED FOR - " + initClassString + "...");
				}
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				log.error(Util.getStackTrace(e));
			}
			if (clas != null)
			{
				// initialization method
				Method initMethod = clas.getMethod(initMethodString, null);

				// Call the method
				initMethod.invoke(clas.newInstance(), null);
			}
		} catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		isInitialized = true;
		log.info("Finished initializing Teleform monitor.");
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
	
	private void moveMergeFiles(Integer locationTagId,Integer locationId){
		
		ATDService atdService = Context.getService(ATDService.class);
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getForms(null,null,null,false,null,null,null);
		AdministrationService adminService = Context.getAdministrationService();
		
		//look at a global property for the batch size of merge files that
		//Teleform is able to process
		Integer numFilesToMove = null;
		
		try
		{
			numFilesToMove = Integer.parseInt(adminService.getGlobalProperty("atd.mergeDirectoryBatchSize"));
		} catch (NumberFormatException e)
		{
		}
		
		if(numFilesToMove==null){
			return;
		}
		
		
			
		
		for(Form form:forms){

			//get the value of the pending merge directory for the form
			FormAttributeValue pendingMergeValue = 
				atdService.getFormAttributeValue(form.getFormId(), "pendingMergeDirectory",locationTagId,locationId);
			
			String pendingMergeDirectory = null;
			
			if(pendingMergeValue != null){
				pendingMergeDirectory = pendingMergeValue.getValue();
			}else{
				continue;
			}
			
			//get the value of the default merge directory for the form
			FormAttributeValue defaultMergeValue = 
				atdService.getFormAttributeValue(form.getFormId(), "defaultMergeDirectory",locationTagId,locationId);
			
			String defaultMergeDirectory = null;
			
			if(defaultMergeValue != null){
				defaultMergeDirectory = defaultMergeValue.getValue();
			}else{
				continue;
			}
			
			//see how many xml files are in the default merge directory for the form
			String[] fileExtensions = new String[]
			{ ".xml" };
			File[] files = IOUtil.getFilesInDirectory(defaultMergeDirectory,
					fileExtensions);
			
			Integer numFiles = 0;
			
			if(files != null){
				numFiles = files.length;
			}
			
			files = IOUtil.getFilesInDirectory(
					pendingMergeDirectory, fileExtensions);
			
			if(files == null){
				continue;
			}
			
			//move files from the pending merge directory to the default merge directory
			//until the batch size number of files are in the default merge directory
			int i = 0;
			for (i = 0; numFiles <= numFilesToMove && i < files.length; numFiles++,i++)
			{
				File fileToMove = files[i];
				if(fileToMove.length() == 0){
					continue;
				}
				String sourceFilename = fileToMove.getPath();
				String targetFilename = defaultMergeDirectory+"/"+
					IOUtil.getFilenameWithoutExtension(sourceFilename)+".20";
				try
				{
					//copy the file to .20 extension so Teleform doesn't
					//pick up the file before it is done writing
					IOUtil.copyFile(sourceFilename, targetFilename, true);
					//rename the file to .xml so teleform will see it
					IOUtil.renameFile(targetFilename, defaultMergeDirectory+"/"+
							IOUtil.getFilenameWithoutExtension(targetFilename)+".xml");
					
					File srcFile = new File(sourceFilename);
					File tgtFile = new File(targetFilename);
					
					//check if the file exists under the following file extensions
					targetFilename = defaultMergeDirectory
							+ "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
							+ ".20";
					
					tgtFile = new File(targetFilename);
					
					if(!tgtFile.exists()){
					
						targetFilename = defaultMergeDirectory
							+ "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
							+ ".22";
						tgtFile = new File(targetFilename);
						if(!tgtFile.exists()){
							targetFilename = defaultMergeDirectory
							+ "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
								+ ".xml";
							tgtFile = new File(targetFilename);
							if(!tgtFile.exists()){
								targetFilename = defaultMergeDirectory
								+ "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
								+ ".19";
								tgtFile = new File(targetFilename);
							}
						}
					}
					
					//if the source file is bigger than 
					//the target file, the copy was truncated so
					//don't mark it as copied
					if (tgtFile.exists())
					{
						if (srcFile.length() > tgtFile.length())
						{
							// don't rename the pendingMergeDirectory file so
							// that it will get picked up on the next time
							// around
							log.error("merge file: " + tgtFile.getPath()
									+ " is truncated.");
						} else
						{
							IOUtil.renameFile(sourceFilename,
							pendingMergeDirectory+"/"+
							IOUtil.getFilenameWithoutExtension(sourceFilename)+".copy");

						}
					}
				} catch (Exception e)
				{
					log.error("File copy exception in TF monitor task:" + e.toString());
					continue;
				}
				
			}
			if(i == 0 && lastMoveMergeFiles != null && nextLatestMoveMergeFiles != null) // and there are files to move for any form but 
																						//we did not move any for this form
			{
					Calendar now = GregorianCalendar.getInstance();
					if(now.getTime().after(nextLatestMoveMergeFiles))
					{
						// Log error and alert some problem with TF Merger
						if (!TFAMP_Alert)	//already alerted
						{
							TFAMP_Alert = true;
							log.error("TF AutoMerger NOT RUNNING IT SEEMS!!! ");
						}
					}
			}
			else if(lastMoveMergeFiles == null || i > 0) // First time or we moved some files
			{
				Calendar today = GregorianCalendar.getInstance();
				lastMoveMergeFiles = today.getTime();
				Calendar threshold = GregorianCalendar.getInstance();
				threshold.add(GregorianCalendar.MINUTE,5);   
				nextLatestMoveMergeFiles = threshold.getTime();
				if(TFAMP_Alert)
				{
					TFAMP_Alert = false;
					log.error("TF AutoMerger Issue Rectified IT SEEMS!!! ");
				}
			}
			
		}
	}
	
	public synchronized static TeleformFileState addToPendingStatesWithFilename(FormInstance formInstance,
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
	
	public synchronized static TeleformFileState addToPendingStatesWithoutFilename(FormInstance formInstance)
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

	public synchronized static void addToTifFileProcessing(FormInstance formInstance, String directoryName, String fileExt )
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
	
	private static void processPendingStatesWithoutFilename(ArrayList<TeleformFileState> listTFstatesProcessed)
	{
		FormService formService = Context.getFormService();
		ATDService atdService = Context.getService(ATDService.class);
		ArrayList<String> exportDirectories = atdService.getExportDirectories();
		String[] fileExtensions = new String[]
		{ ".xml" };

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
			TeleformExportXMLDatasource xmlDatasource = (TeleformExportXMLDatasource) logicService
					.getLogicDataSource("xml");
			
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
	
					formInstance = xmlDatasource.parse(input,formInstance,null);
					input.close();
					//This is an old form with a single key (formInstanceId) barcode
					//we need to figure out the formId, locationId, and locationTagId
					if (formInstance == null)
					{
						Integer locationTagId = null;
						List<FormAttributeValue> formAttrValues = atdService
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
							formInstance = xmlDatasource.parse(input,
									formInstance, locationTagId);
							input.close();

							if(formInstance == null){
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
								PatientState patientState = atdService
										.getPatientStateByFormInstanceAction(
												lookupFormInstance,
												"PRODUCE FORM INSTANCE");
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
							tfFileState.setFullFilePath(newFilename);
							tfFileState.setFormInstance(formInstance);
							pendingStatesWithoutFilename.remove(formInstance);
							listTFstatesProcessed.add(tfFileState);
						}else{
							//This means we have found a .xml file that is not in pending processing.
							//This is most likely a rescan. We need to see if a scan already exists
							//for the session that has this formInstance
							String action = "PRODUCE FORM INSTANCE";
							
							//get the session that goes with this formInstanceId
							PatientState patientState = atdService
								.getPatientStateByFormInstanceAction(
									formInstance, action);
					
							String formName = formService.getForm(formInstance.getFormId()).getName();
							
							Integer sessionId = patientState.getSessionId();
							//see if consume exists for the session
							//if so, then it is a rescan
							
							String stateName = formName+"_process";
							State state = atdService.getStateByName(stateName);
							List<PatientState> patientStates = atdService.
									getPatientStateBySessionState(sessionId, state.getStateId());
							
							if (patientStates!=null&&patientStates.size()>0)
							{	
									stateName = formName + "_rescan";
									State currState = atdService
											.getStateByName(stateName);
									action = "PRODUCE FORM INSTANCE";
									patientState = atdService
											.getPatientStateByFormInstanceAction(
													formInstance, action);
									try
									{
										patientState = atdService.addPatientState(
												patientState.getPatient(), currState,
												patientState.getSessionId(),
												patientState.getLocationTagId(),
												patientState.getLocationId());
										patientState = atdService.updatePatientState(patientState);
										tfFileState = new TeleformFileState();
										String newFilename = IOUtil.getDirectoryName(filename)+"/"+formInstance.toString()+"_rescan.20";
										
										File newFile = new File(newFilename);
										if(newFile.exists()){
											IOUtil.deleteFile(newFilename);
										}
										IOUtil.renameFile(filename, newFilename);
										tfFileState.setFullFilePath(newFilename);
										tfFileState.setFormInstance(formInstance);
										tfFileState.addParameter("patientState",
												patientState);
										listTFstatesProcessed.add(tfFileState);
									} catch (Exception e)
									{
										log.error("RESCAN for formInstanceId: "
												+ formInstance.getFormInstanceId() + " failed.");
										log.error(Util.getStackTrace(e));
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

	private static void processPendingStatesWithFilename(ArrayList<TeleformFileState> listTFstatesProcessed) throws APIException, Exception
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
				listTFstatesProcessed.add(tfState);
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

	private static void kickProcess(
			ArrayList<TeleformFileState> listTFstatesProcessed)
	{
		aopLock.lock();
		try
		{
			if (listTFstatesProcessed.size() > 0)
			{
				while (statesBeingProcessedByAOP)
				{
					log
							.info("Waiting for states process in AOP to finish.....");
					try
					{
						goTFStates.await();
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				ATDService atdService = Context.getService(ATDService.class);
				statesBeingProcessedByAOP = true;
				atdService.fileProcessed(listTFstatesProcessed);
			}
		} finally
		{
			aopLock.unlock();
		}
	}
	
	
	private static void fileProcessed(TeleformFileState tfstate,ArrayList<TeleformFileState> listTFstatesProcessed)
	{
		String fn = tfstate.getFilename().toString();
		log.info("fileProcessed: ADDING TF STATE to LIST for: " + fn);
		listTFstatesProcessed.add(tfstate);	
	}
	
	
	public static void statesProcessed()
	{
		statesBeingProcessedByAOP = false;
		goTFStates.signalAll();
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

}
