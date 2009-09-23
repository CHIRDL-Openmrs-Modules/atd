/**
 * 
 */
package org.openmrs.module.atd;

import java.io.File;
import java.io.FilenameFilter;


/**
 * @author Vibha Anand
 * 
 */
public class FileExt implements FilenameFilter
{
	private String ext = "";
	
	public  FileExt(String str)
	{
		this.ext = str;
	}
	
	public boolean accept(File dir, String name)
	{
		if(this.ext.length()==0)
			return true;
		else
			return name.endsWith(this.ext);
	}
	
}
