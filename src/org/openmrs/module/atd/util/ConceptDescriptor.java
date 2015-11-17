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
package org.openmrs.module.atd.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.Concept;

/**
 * Bean containing the information provided from the concept import file.
 * 
 * @author Tammy Dugan
 */
public class ConceptDescriptor {
	
	private String name = null;
	
	private String conceptClass = null;
	
	private String datatype = null;
	
	private String description = null;
	
	private String parentConcept = null;
	
	private String units = null;
		
	private int conceptId;
	
	// DWE CHICA-330 4/30/15 Added parentConceptId for use with data table selected rows, conceptId was not unique enough
	private int parentConceptId;
	private Timestamp dateCreatedStamp;
	
	
	/**
	 * Constructor Method
	 * Create a ConceptDescriptor with name, concept class, data type, description, parent concept, units, concept Id
	 * @param name concept name
	 * @param conceptClass the class of concept
	 * @param datatype datatype
	 * @param description description of this concept
	 * @param parentConcept parent concept
	 * @param units measurement unit
	 * @param conceptId the id of concept
	 */
	public ConceptDescriptor(String name, String conceptClass, String datatype, String description, String parentConcept, String units, int conceptId) {
		super();
		this.name = name;
		this.conceptClass = conceptClass;
		this.datatype = datatype;
		this.description = description;
		this.parentConcept = parentConcept;
		this.units = units;
		this.conceptId = conceptId;
	}
	
	/**
	 * Constructor Method
	 * Create a ConceptDescriptor with name, concept class, data type, description, parent concept, units, concept Id
	 * @param name concept name
	 * @param conceptClass the class of concept
	 * @param datatype datatype
	 * @param description description of this concept
	 * @param parentConcept parent concept
	 * @param units measurement unit
	 * @param conceptId the id of concept
	 * @param dateCreated the date the concept was created
	 */
	public ConceptDescriptor(String name, String conceptClass, String datatype, String description, String parentConcept, String units, int conceptId, int parentConceptId, Timestamp dateCreatedStamp) {
		super();
		this.name = name;
		this.conceptClass = conceptClass;
		this.datatype = datatype;
		this.description = description;
		this.parentConcept = parentConcept;
		this.units = units;
		this.conceptId = conceptId;
		this.parentConceptId = parentConceptId;
		this.dateCreatedStamp = dateCreatedStamp;
	}

	/**
	 * Constructor Method
	 * Create a empty ConceptDescriptor.
	 */
	public ConceptDescriptor() {
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the conceptClass
	 */
	public String getConceptClass() {
		return this.conceptClass;
	}
	
	/**
	 * @param conceptClass the conceptClass to set
	 */
	public void setConceptClass(String conceptClass) {
		this.conceptClass = conceptClass;
	}
	
	/**
	 * @return the datatype
	 */
	public String getDatatype() {
		return this.datatype;
	}
	
	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the parentConcept
	 */
	public String getParentConcept() {
		return this.parentConcept;
	}
	
	/**
	 * @param parentConcept the parentConcept to set
	 */
	public void setParentConcept(String parentConcept) {
		this.parentConcept = parentConcept;
	}
	
	/**
	 * @return the units
	 */
	public String getUnits() {
		return this.units;
	}
	
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	public int getConceptId() {
		return conceptId;
	}

	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}
	
	/**
	 * @return the parent concept Id
	 */
	public int getParentConceptId(){
		return this.parentConceptId;
	}
	
	/**
	 * @param parentConceptId the parent concept Id to set
	 */
	public void setParentConceptId(int parentConceptId){
		this.parentConceptId = parentConceptId;
	}
	
	/**
	 * @return the date created
	 */
	public Timestamp getDateCreatedStamp(){
		return this.dateCreatedStamp;
	}
	
	/**
	 * @param dateCreated
	 */
	public void setDateCreatedStamp(Timestamp dateCreatedStamp){
		this.dateCreatedStamp = dateCreatedStamp;
	}
	
	/**
	 * @return dateCreated in a formatted string for the data table display
	 */
	public String getFormattedDateCreated()
	{
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
		Date date = new Date(dateCreatedStamp.getTime());
		return format.format(date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptClass == null) ? 0 : conceptClass.hashCode());
		result = prime * result + conceptId;
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentConcept == null) ? 0 : parentConcept.hashCode());
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		result = prime * result + parentConceptId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConceptDescriptor other = (ConceptDescriptor) obj;
		if (conceptClass == null) {
			if (other.conceptClass != null)
				return false;
		} else if (!conceptClass.equals(other.conceptClass))
			return false;
		if (conceptId != other.conceptId)
			return false;
		if (datatype == null) {
			if (other.datatype != null)
				return false;
		} else if (!datatype.equals(other.datatype))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentConcept == null) {
			if (other.parentConcept != null)
				return false;
		} else if (!parentConcept.equals(other.parentConcept))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConceptDescriptor [name=" + name + ", conceptClass=" + conceptClass + ", datatype=" + datatype + ", description=" + description + ", parentConcept=" + parentConcept + ", units=" + units + ", conceptId=" + conceptId + ", parentConceptId=" + parentConceptId + ", dateCreatedStamp=" + dateCreatedStamp +"]";
	}
	

	
	
}
