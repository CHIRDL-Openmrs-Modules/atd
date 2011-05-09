/**
 * 
 */
package org.openmrs.module.atd;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * @author Tammy Dugan
 */

public class MoveMergeFiles extends AbstractTask {
	
	private static Log log = LogFactory.getLog(TeleformFileMonitor.class);
	
	private static boolean isInitialized = false;
	
	private boolean keepLooking = true;
	
	private static TaskDefinition taskConfig;
	
	private static Date lastMoveMergeFiles;
	
	private static Date nextLatestMoveMergeFiles;
	
	private static Boolean TFAMP_Alert = false;
	
	@Override
	public void initialize(TaskDefinition config) {
		taskConfig = config;
		isInitialized = false;
	}
	
	@Override
	public void execute() {
		while (this.keepLooking) {
			Context.openSession();
			
			try {
				if (!isInitialized) {
					isInitialized = true;
				}
				if (Context.isAuthenticated() == false)
					authenticate();
				
				//move files from pending to merge directory
				moveMergeFiles();
				
			}
			catch (Exception e) {
				log.error(e.getMessage());
				log.error(Util.getStackTrace(e));
			}
			finally {
				Context.closeSession();
			}
			try {
				Thread.sleep(100);//check every tenth second
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
		}
	}
	
	@Override
	public void shutdown() {
		this.keepLooking = false;
		
		super.shutdown();
	}
	
	private void moveMergeFiles() {
		
		ATDService atdService = Context.getService(ATDService.class);
		AdministrationService adminService = Context.getAdministrationService();
		
		//look at a global property for the batch size of merge files that
		//Teleform is able to process
		Integer numFilesToMove = null;
		
		try {
			numFilesToMove = Integer.parseInt(adminService.getGlobalProperty("atd.mergeDirectoryBatchSize"));
		}
		catch (NumberFormatException e) {}
		
		if (numFilesToMove == null) {
			return;
		}
		
		ArrayList<String> pendingDirectories = atdService.getFormAttributesByNameAsString("pendingMergeDirectory");
		
		for (String pendingDirectory : pendingDirectories) {
			
			//get the value of the pending merge directory for the form			
			if (pendingDirectory == null||pendingDirectory.length()==0) {
				continue;
			}
			
			//looking up all of the default and pending directories individually
			//was very slow so I am doing a substring match to speed this method up
			int index = pendingDirectory.lastIndexOf("Pending");
			
			String defaultMergeDirectory = null;
			
			if(index > -1){
				defaultMergeDirectory = pendingDirectory.substring(0,index);
			}else
			{
				log.error("could not process pending directory: "+pendingDirectory);
			}
			
			if(defaultMergeDirectory == null||defaultMergeDirectory.length()==0){
				continue;
			}
			
			//see how many xml files are in the default merge directory for the form
			String[] fileExtensions = new String[] { ".xml" };
			File[] files = IOUtil.getFilesInDirectory(defaultMergeDirectory, fileExtensions);
			
			Integer numFiles = 0;
			
			if (files != null) {
				numFiles = files.length;
			}
			
			files = IOUtil.getFilesInDirectory(pendingDirectory, fileExtensions);
			
			if (files == null) {
				continue;
			}
			
			//move files from the pending merge directory to the default merge directory
			//until the batch size number of files are in the default merge directory
			int i = 0;
			for (i = 0; numFiles <= numFilesToMove && i < files.length; numFiles++, i++) {
				File fileToMove = files[i];
				if (fileToMove.length() == 0) {
					continue;
				}
				String sourceFilename = fileToMove.getPath();
				String targetFilename = defaultMergeDirectory + "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
				        + ".20";
				try {
					//copy the file to .20 extension so Teleform doesn't
					//pick up the file before it is done writing
					IOUtil.copyFile(sourceFilename, targetFilename, true);
					//rename the file to .xml so teleform will see it
					IOUtil.renameFile(targetFilename, defaultMergeDirectory + "/"
					        + IOUtil.getFilenameWithoutExtension(targetFilename) + ".xml");
					
					File srcFile = new File(sourceFilename);
					File tgtFile = new File(targetFilename);
					
					//check if the file exists under the following file extensions
					targetFilename = defaultMergeDirectory + "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
					        + ".20";
					
					tgtFile = new File(targetFilename);
					
					if (!tgtFile.exists()) {
						
						targetFilename = defaultMergeDirectory + "/" + IOUtil.getFilenameWithoutExtension(sourceFilename)
						        + ".22";
						tgtFile = new File(targetFilename);
						if (!tgtFile.exists()) {
							targetFilename = defaultMergeDirectory + "/"
							        + IOUtil.getFilenameWithoutExtension(sourceFilename) + ".xml";
							tgtFile = new File(targetFilename);
							if (!tgtFile.exists()) {
								targetFilename = defaultMergeDirectory + "/"
								        + IOUtil.getFilenameWithoutExtension(sourceFilename) + ".19";
								tgtFile = new File(targetFilename);
							}
						}
					}
					
					//if the source file is bigger than 
					//the target file, the copy was truncated so
					//don't mark it as copied
					if (tgtFile.exists()) {
						if (srcFile.length() > tgtFile.length()) {
							// don't rename the pendingMergeDirectory file so
							// that it will get picked up on the next time
							// around
							log.error("merge file: " + tgtFile.getPath() + " is truncated. File will be deleted.");
							//delete the truncated xml so it won't break the
							//Teleform merger
							IOUtil.deleteFile(targetFilename);
						} else {
							IOUtil.renameFile(sourceFilename, pendingDirectory + "/"
							        + IOUtil.getFilenameWithoutExtension(sourceFilename) + ".copy");
							
						}
					}
				}
				catch (Exception e) {
					log.error("File copy exception in TF monitor task:" + e.toString());
					continue;
				}
				
			}
			if (i == 0 && lastMoveMergeFiles != null && nextLatestMoveMergeFiles != null) // and there are files to move for any form but 
			//we did not move any for this form
			{
				Calendar now = GregorianCalendar.getInstance();
				if (now.getTime().after(nextLatestMoveMergeFiles)) {
					// Log error and alert some problem with TF Merger
					if (!TFAMP_Alert) //already alerted
					{
						TFAMP_Alert = true;
						log.error("TF AutoMerger NOT RUNNING IT SEEMS!!! ");
					}
				}
			} else if (lastMoveMergeFiles == null || i > 0) // First time or we moved some files
			{
				Calendar today = GregorianCalendar.getInstance();
				lastMoveMergeFiles = today.getTime();
				Calendar threshold = GregorianCalendar.getInstance();
				threshold.add(GregorianCalendar.MINUTE, 5);
				nextLatestMoveMergeFiles = threshold.getTime();
				if (TFAMP_Alert) {
					TFAMP_Alert = false;
					log.error("TF AutoMerger Issue Rectified IT SEEMS!!! ");
				}
			}
			
		}
	}
	
}
