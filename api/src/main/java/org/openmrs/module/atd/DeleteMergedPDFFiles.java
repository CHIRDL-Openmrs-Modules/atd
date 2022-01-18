/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.atd;

import java.io.File;
import java.io.FileFilter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.FileAgeFilter;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.scheduler.tasks.AbstractTask;


/**
 * Task to cleanup PDF merge files that are older than the specified "daysToKeep" property.
 * 
 * @author Steve McKee
 */
public class DeleteMergedPDFFiles extends AbstractTask {
	
	private static final Logger log = LoggerFactory.getLogger(DeleteMergedPDFFiles.class);
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		Context.openSession();
		
		try {
			log.info("Starting Delete Merged PDF Files at " + new Timestamp(new Date().getTime()));
			deleteMergedPDFFiles();
			log.info("Finished Delete Merged PDF Files at " + new Timestamp(new Date().getTime()));
		}
		catch (Exception e) {
			log.info("Delete Merged PDF Files errored out at " + new Timestamp(new Date().getTime()));
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		finally {
			Context.closeSession();
		}
	}
	
	/**
	 * Delete any files in the pdf directories that are over the "daysToKeep" property set for the this 
	 * scheduled task.
	 */
	private void deleteMergedPDFFiles() {
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		List<String> mergeDirectories = chirdlUtilBackportsService.getFormAttributesByNameAsString("defaultMergeDirectory");
		if (mergeDirectories == null) {
			return;
		}
		
		String days = getTaskDefinition().getProperty("daysToKeep");
		int daysToKeep = 14;
		try {
			daysToKeep = Integer.parseInt(days);
		} catch (NumberFormatException e) {
			log.error("The daysToKeep property has no valid value set.  The default of 14 will be used.");
		}
		
		FileFilter oldFilesFilter = new FileAgeFilter(daysToKeep);
		for (String mergeDirectoryStr : mergeDirectories) {
			File mergeDirectory = new File(mergeDirectoryStr);
			if (!mergeDirectory.exists()) {
				continue;
			}
			
			// Do the pdf folder
            File pdfDirectory = new File(mergeDirectory, ChirdlUtilConstants.FILE_PDF);
			if (pdfDirectory.exists()) {
	            File[] files = pdfDirectory.listFiles(oldFilesFilter);
	            if (files != null && files.length > 0) {
	            	deleteFiles(files, pdfDirectory);
	            }
			}
		}
	}
	
	/**
	 * Deletes the specified list of files.
	 * 
	 * @param files The files to move to the target directory.
	 * @param pdfDirectory The directory containing the files to be deleted.
	 */
	protected void deleteFiles(File[] files, File pdfDirectory) {
		int filesDeleted = 0;
		log.info("Deleting files in the following directory: " + pdfDirectory.getAbsolutePath());
        for (File file : files) {        	
        	try {
        		IOUtil.deleteFile(file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Error deleting file: " + file.getAbsolutePath(), e);
                continue;
            }
            
            filesDeleted++;
        }
        
        log.info("Successfully deleted " + filesDeleted + " files from " + pdfDirectory.getAbsolutePath());
	}
}
