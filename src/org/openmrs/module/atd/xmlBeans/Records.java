/**
 * 
 */
package org.openmrs.module.atd.xmlBeans;



/**
 * Object representation of <Records> xml
 * 
 * @author Tammy Dugan
 */
public class Records
{
	private Record record = null;
	private String title = null;
	
	/**
	 * Assigns a Record for this object
	 * @param record Record for this object
	 */
	public Records(Record record)
	{
		this.record = record;
	}
	
	/**
	 * Empty constructor
	 */
	public Records()
	{
		
	}

	/**
	 * @return the record
	 */
	public Record getRecord()
	{
		return this.record;
	}

	public String getTitle()
	{
		return this.title;
	}

}
