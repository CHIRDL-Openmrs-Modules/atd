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
		
	//private Concept concept = null;
	private int conceptId;
	
	/**
	 * Constructor Method
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
	

	
	
}
