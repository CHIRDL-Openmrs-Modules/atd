package org.openmrs.module.atd.util;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters files in a directory based on the extension.  The file
 *         name must start with the provided text string and the 
 *         last modified time must be greater than the provided 
 *         time.
 *
 * @author Steve McKee
 */
public class BadScansFileFilter implements FilenameFilter {
	private Date date;
	private SimpleDateFormat sdf;
	private List<String> fileExtensionsToIgnore;

	public BadScansFileFilter(Date date, List<String> fileExtensionsToIgnore) {
		this.date = date;
		sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (fileExtensionsToIgnore == null) {
			fileExtensionsToIgnore = new ArrayList<String>();
		} else {
			this.fileExtensionsToIgnore = fileExtensionsToIgnore;
		}
	}

	public boolean accept(File directory, String filename) {
		boolean accept = false;

		// The File.isDirectory() check is extremely slow, so I chose to test the file
		// extension since the only files in the directories are tif files.
		if (!filename.endsWith(".tif")) {
			// We don't want the files in here because they've already been 
			// taken care of.
			if ("rescanned bad scans".equals(filename) || 
					"ignored bad scans".equals(filename) || 
					"archive".equals(filename)) {
				return false;
			}
			
			Iterator<String> i = fileExtensionsToIgnore.listIterator();
			while(i.hasNext()) {
				String extension = i.next();
				if (filename.endsWith(extension)) {
					return false;
				}
			}
			
			return true;
		}
		
		String newFilename = filename.replace("-", "");
		newFilename = newFilename.replace("_", "");
		Pattern pattern = Pattern.compile("^[0-9]+\\.tif");
		Matcher matcher = pattern.matcher(newFilename);
		if (!matcher.find()) {
			accept = true;
		}
		
		if (accept && date != null) {
			String current = sdf.format(date);
			Date lastModDate = new Date(new File(directory, filename).lastModified());
			accept &= (current.compareTo(sdf.format(lastModDate))) == 0;
		}

		return accept;
	}
	
}
