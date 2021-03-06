/**
 * 
 */
package org.openmrs.module.atd.xmlBeans;

import java.util.ArrayList;

import org.openmrs.module.atd.xmlBeans.Field;

/**
 * Object representation of <language> xml
 *
 * @author Tammy Dugan
 */
public class Language
{	
	private ArrayList<Field> fields = null;
	private String name = null;
	
	/**
	 * Empty constructor
	 */
	public Language()
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

	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}
}
