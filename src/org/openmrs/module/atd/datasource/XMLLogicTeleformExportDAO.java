package org.openmrs.module.atd.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.dss.util.Util;
import org.openmrs.module.dss.util.XMLUtil;

/**
 * Implementation of LogicTeleformExportDAO
 * 
 * @author Tammy Dugan
 *
 */
public class XMLLogicTeleformExportDAO implements LogicTeleformExportDAO
{
	protected final Log log = LogFactory.getLog(getClass());

	private HashMap<FormInstance, HashMap<String,Field>> parsedFiles = null;

	/**
	 * Initialize the parsed files cache
	 */
	public XMLLogicTeleformExportDAO()
	{
		this.parsedFiles = new HashMap<FormInstance,HashMap<String, Field>>();
	}
	
	public HashMap<String,Field> getParsedFile(Integer formInstanceId, Integer formId)
	{
		FormInstance formInstance = new FormInstance();
		formInstance.setFormId(formId);
		formInstance.setFormInstanceId(formInstanceId);
		
		return this.parsedFiles.get(formInstance);
	}

	public void deleteParsedFile(Integer formInstanceId, Integer formId)
	{
		FormInstance formInstance = new FormInstance();
		formInstance.setFormId(formId);
		formInstance.setFormInstanceId(formInstanceId);
		
		this.parsedFiles.remove(formInstance);
	}
	
	public FormInstance parse(InputStream input, 
			 Integer formInstanceId, Integer formId)
	{
		// TODO if formId is null, parse out of xml
		String formInstanceIdTag = org.openmrs.module.atd.util.Util
				.getFormAttributeValue(formId, "formInstanceIdTag");

		if (formInstanceIdTag == null)
		{
			// try the form instance id on the back of the form
			formInstanceIdTag = org.openmrs.module.atd.util.Util
					.getFormAttributeValue(formId, "formInstanceIdTag2");
			if (formInstanceIdTag == null&&formInstanceId==null)
			{
				return null;
			}
		}
		
		HashMap<String,Field> fieldMap = getParsedFile(formInstanceId,formId);
		
		if(fieldMap != null)
		{
			FormInstance formInstance = new FormInstance();
			formInstance.setFormId(formId);
			formInstance.setFormInstanceId(formInstanceId);
			return formInstance;
		}
		
		Records records = this.parse(input);
	
		if (records != null)
		{
			fieldMap = new HashMap<String,Field>();
			// pull out formInstanceId
			ArrayList<Field> fields = records.getRecord().getFields();
			for (Field currField : fields)
			{
				fieldMap.put(currField.getId(), currField);
			}
			if(fieldMap.get(formInstanceIdTag)!=null&&fieldMap.get(formInstanceIdTag).getValue() != null){
				try
				{
					formInstanceId = Integer.parseInt(fieldMap.get(formInstanceIdTag)
							.getValue());
				} catch (NumberFormatException e)
				{
				}
			}
			if (formInstanceId != null)
			{
				FormInstance formInstance = new FormInstance();
				formInstance.setFormId(formId);
				formInstance.setFormInstanceId(formInstanceId);
				this.parsedFiles.put(formInstance, fieldMap);
				return formInstance;
			}
		}
		return null;
	}
	
	public Records parse(InputStream input)
	{
		Records records = null;
	
		// only parse if file hasn't been parsed already
		try
		{
			records = (Records) XMLUtil.deserializeXML(Records.class, input);
		} catch (IOException e)
		{
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
		return records;
	}

	private XMLResult evaluateLogic(LogicExpression expression)
	{
		XMLResult xmlResult = checkCriteria(expression,null);

		return xmlResult;
	}

	private XMLResult checkCriteria(LogicExpression expression,
			XMLResult xmlResult)
	{
		Result result = null;
		HashMap<String, Field> parsedFile = null;
		Operator operator = null;
		Object rightOperand = null;
		Object leftOperand = null;

		if (xmlResult == null)
		{
			xmlResult = new XMLResult();
		}

		operator = expression.getOperator();
		rightOperand = expression.getRightOperand();
		if(expression instanceof LogicExpressionBinary){
			leftOperand = ((LogicExpressionBinary) expression).getLeftOperand();
		}

		parsedFile = xmlResult.getParsedFile();
		result = xmlResult.getResult();

		if (operator == null)
		{
			if (parsedFile != null && rightOperand != null)
			{
				Field field = parsedFile.get(rightOperand.toString());

				if (field != null && field.getValue() != null)
				{
					result = new Result(field.getValue());
					xmlResult.setResult(result);
				}
			}
		} else if (operator instanceof org.openmrs.logic.op.Equals)
		{
			if (rightOperand instanceof FormInstance)
			{
				FormInstance formInstance = (FormInstance) rightOperand;
				parsedFile = getParsedFile(formInstance.getFormInstanceId(),
						formInstance.getFormId());
				xmlResult.setParsedFile(parsedFile);
			}
		} else if (operator instanceof org.openmrs.logic.op.And)
		{
			if (leftOperand instanceof LogicExpression)
			{
				xmlResult = checkCriteria((LogicExpression) leftOperand,
						xmlResult);
			}
			if (rightOperand instanceof LogicExpression)
			{
				xmlResult = checkCriteria((LogicExpression) rightOperand,
						xmlResult);
			}
		}

		return xmlResult;
	}

	public List<XMLResult> getXMLResults(Cohort who,
			LogicCriteria logicCriteria)
	{
		XMLResult xmlResult = evaluateLogic(logicCriteria.getExpression());
		Set<Integer> personIds = who.getMemberIds();
		List<XMLResult> xmlResults = new ArrayList<XMLResult>();
		
		for(Integer personId:personIds)
		{
			xmlResult.setPersonId(personId);
			xmlResults.add(xmlResult);
		}

		return xmlResults;
	}

}
