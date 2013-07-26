package org.openmrs.module.atd.ruleLibrary;

import java.util.Map;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;

public class storeObs implements Rule
{

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList()
	{
		return null;
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getDependencies()
	 */
	public String[] getDependencies()
	{
		return new String[]
		{};
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getTTL()
	 */
	public int getTTL()
	{
		return 0; // 60 * 30; // 30 minutes
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getDatatype(String)
	 */
	public Datatype getDefaultDatatype()
	{
		return Datatype.CODED;
	}

	public Result eval(LogicContext context, Integer patientId,
			Map<String, Object> parameters) throws LogicException
	{
		FormInstance formInstance = null;
		String conceptName = null;
		Integer encounterId = null;
		Integer ruleId = null;
		String value = null;
		Integer locationTagId = null;
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(patientId);

		if (parameters != null)
		{
			formInstance = (FormInstance) parameters.get("formInstance");

			conceptName = (String) parameters.get("param1");

			ruleId = (Integer) parameters.get("ruleId");
			locationTagId = (Integer) parameters.get("locationTagId");

			if (conceptName == null)
			{
				return Result.emptyResult();
			}
			encounterId = (Integer) parameters.get("encounterId");
			value = (String) parameters.get("param2");
		}
		ConceptService conceptService = Context.getConceptService();

		Concept currConcept = conceptService.getConceptByName(conceptName);

		Obs obs = org.openmrs.module.atd.util.Util.saveObsWithStatistics(patient, currConcept, encounterId, value, formInstance,
				ruleId,locationTagId,false);

		if (obs == null) {
			return Result.emptyResult();
		}
		
		return new Result(obs);
	}
}