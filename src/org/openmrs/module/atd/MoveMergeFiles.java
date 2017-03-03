/**
 * 
 */
package org.openmrs.module.atd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

/**
 * @author Tammy Dugan
 */

public class MoveMergeFiles extends AbstractTask {
	
	private static Log log = LogFactory.getLog(TeleformFileMonitor.class);
	
	private static boolean isInitialized = false;
	
	private boolean keepLooking = true;
	
	private static Date lastMoveMergeFiles;
	
	@Override
	public void initialize(TaskDefinition config) {
		super.initialize(config);
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
		
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
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
		
		ArrayList<String> mergeDirectories = chirdlUtilBackportsService.getFormAttributesByNameAsString("defaultMergeDirectory");
		
		for (String defaultMergeDirectory : mergeDirectories) {
			
			//get the value of the pending merge directory for the form			
			if (defaultMergeDirectory == null||defaultMergeDirectory.length()==0) {
				continue;
			}
			
			//looking up all of the default and pending directories individually
			//was very slow so I am doing a substring match to speed this method up
			String pendingDirectory = defaultMergeDirectory + File.separator + "Pending" + File.separator;
			
			//see how many xml files are in the default merge directory for the form
			String[] fileExtensions = new String[] { ".xml" };
			File[] files = IOUtil.getFilesInDirectory(defaultMergeDirectory, fileExtensions);
			
			Integer numFiles = 0;
			
			if (files != null) {
				numFiles = files.length;
			}
			
			fileExtensions = new String[] { ".xml", ".pdf" };
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
					numFiles--;
					continue;
				}
				String sourceFilename = fileToMove.getPath();
				String targetFilename = null;
				String extension = ".xml";
				boolean pdfFile = false;
				String pdfFilename = null;
				if (sourceFilename.endsWith(".pdf")) {
					extension = ".pdf";
					pdfFile = true;
					pdfFilename = getTargetPdfFilename(fileToMove);
					if (pdfFilename == null) {
						continue;
					}
					
					File targetDir = new File(defaultMergeDirectory, "pdf");
					targetDir.mkdirs();
					targetFilename = defaultMergeDirectory + File.separator + "pdf" + File.separator + pdfFilename;
					IOUtil.renameFile(sourceFilename, pendingDirectory
				        + pdfFilename);
					sourceFilename = pendingDirectory + pdfFilename;
				} else {
					targetFilename = defaultMergeDirectory + File.separator + 
						IOUtil.getFilenameWithoutExtension(sourceFilename) + ".move";
				}
				try {
					//copy the file to .20 extension so Teleform doesn't
					//pick up the file before it is done writing
					IOUtil.copyFile(sourceFilename, targetFilename, true);
					//rename the file to .xml so teleform will see it
					if (!pdfFile) {
						IOUtil.renameFile(targetFilename, defaultMergeDirectory + File.separator
						        + IOUtil.getFilenameWithoutExtension(targetFilename) + extension);
					}
					
					File srcFile = new File(sourceFilename);
//					File tgtFile = new File(targetFilename);
					
					//check if the file exists under the following file extensions
					if (!pdfFile) {
						targetFilename = defaultMergeDirectory + File.separator + 
							IOUtil.getFilenameWithoutExtension(sourceFilename) + ".20";
					}
					
					File tgtFile = new File(targetFilename);
					
					if (!tgtFile.exists() && !pdfFile) {
						
						targetFilename = defaultMergeDirectory + File.separator + 
							IOUtil.getFilenameWithoutExtension(sourceFilename) + ".22";
						tgtFile = new File(targetFilename);
						if (!tgtFile.exists()) {
							targetFilename = defaultMergeDirectory + File.separator
							        + IOUtil.getFilenameWithoutExtension(sourceFilename) + extension;
							tgtFile = new File(targetFilename);
							if (!tgtFile.exists()) {
								targetFilename = defaultMergeDirectory + File.separator
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
							extension= ".copy";
							if (pdfFile) {
								extension = ".pdfcopy";
							}
							
							IOUtil.renameFile(sourceFilename, pendingDirectory + File.separator
						        + IOUtil.getFilenameWithoutExtension(sourceFilename) + extension);
						}
					}
				}
				catch (Exception e) {
					log.error("File copy exception in TF monitor task:" + e.toString());
					continue;
				}
				
			}
			if (lastMoveMergeFiles == null || i > 0) // First time or we moved some files
			{
				Calendar today = GregorianCalendar.getInstance();
				lastMoveMergeFiles = today.getTime();
				Calendar threshold = GregorianCalendar.getInstance();
				threshold.add(GregorianCalendar.MINUTE, 5);
			}
		}
	}
	
	/**
	 * Pulls the barcode from a PDF file to determine the correct filename.
	 * 
	 * @param pdfFile The PDF document to rename.
	 * @return The target filename for the PDF document or null if one can't be determined.
	 */
	private String getTargetPdfFilename(File pdfFile) {
		String targetFilename = null;
		try {
			Map<DecodeHintType,Object> hints = new HashMap<DecodeHintType,Object>();
			Collection<BarcodeFormat> formats = new HashSet<BarcodeFormat>();
			formats.add(BarcodeFormat.CODE_39);
			formats.add(BarcodeFormat.CODE_128);
			hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
			hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
			String[] results = Util.getPdfFormBarcodes(pdfFile, hints, "[\\d]+-[\\d]+-[\\d]+");
			if (results.length > 0) {
				String idBarcode = results[0];
				idBarcode = idBarcode.replace("-", "_");
				targetFilename = idBarcode + ".pdf";
			}
		}
		catch (IOException e) {
			log.error(" IO error determining PDF filename from " + pdfFile.getAbsolutePath() + ".", e);
		} catch (Exception e) {
			log.error("Error determining PDF filename from " + pdfFile.getAbsolutePath() + ".", e);
		}
		
		return targetFilename;
	}
}
