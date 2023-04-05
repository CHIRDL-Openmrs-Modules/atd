/**
 * 
 */
package org.openmrs.module.atd.xmlBeans;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Object representation of <Record> xml
 *
 * @author Tammy Dugan
 */
public class Record implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ArrayList<Field> fields = new ArrayList<>();
	
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
