/**
 * 
 */
package org.openmrs.module.atd.xmlBeans;

import java.util.ArrayList;

/**
 * Object representation of <Record> xml
 *
 * @author Tammy Dugan
 */
public class Record
{
	private ArrayList<Field> fields = null;
	
	/**
	 * Empty constructor
	 */
	public Record()
	{
		
	}
	
	/**
	 * Adds a field to the list of fields
	 * @param field Field to add to field list
	 */
	public void addField(Field field)
	{
		if(this.fields == null)
		{
			this.fields = new ArrayList<Field>();
		}
		this.fields.add(field);
	}
	
	/**
	 * @return the fields
	 */
	public ArrayList<Field> getFields()
	{
		return this.fields;
	}
}
