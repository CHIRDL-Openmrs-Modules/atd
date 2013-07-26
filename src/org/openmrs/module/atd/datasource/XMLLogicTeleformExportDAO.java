package org.openmrs.module.atd.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Error;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.dss.logic.op.OperandObject;

/**
 * Implementation of LogicTeleformExportDAO
 * 
 * @author Tammy Dugan
 *
 */
public class XMLLogicTeleformExportDAO implements LogicTeleformExportDAO
{
	protected final Log log = LogFactory.getLog(getClass());

	private HashMap<FormInstance, HashMap<String, Field>> parsedFiles = null;

	/**
	 * Initialize the parsed files cache
	 */
	public XMLLogicTeleformExportDAO()
	{
		this.parsedFiles = new HashMap<FormInstance, HashMap<String, Field>>();
	}

	public HashMap<String, Field> getParsedFile(FormInstance formInstance)
	{
		return this.parsedFiles.get(formInstance);
	}

	public void deleteParsedFile(FormInstance formInstance)
	{
		this.parsedFiles.remove(formInstance);
	}
	
	private FormInstance parseFormInstanceOldForms(HashMap<String, Field> fieldMap,
			Integer formId,Integer locationTagId, Integer locationId)
	{
		String formInstanceIdTag = org.openmrs.module.chirdlutilbackports.util.Util
				.getFormAttributeValue(formId, "formInstanceIdTag", locationTagId,
						locationId);

		if (formInstanceIdTag == null)
		{
			// try the form instance id on the back of the form
			formInstanceIdTag = org.openmrs.module.chirdlutilbackports.util.Util
					.getFormAttributeValue(formId, "formInstanceIdTag2",
							locationTagId, locationId);
			if (formInstanceIdTag == null)
			{
				return null;
			}
		}

		Integer formInstanceId = null;

		if (fieldMap.get(formInstanceIdTag) != null
				&& fieldMap.get(formInstanceIdTag).getValue() != null)
		{
			try
			{
				formInstanceId = Integer.parseInt(fieldMap.get(
						formInstanceIdTag).getValue());
			} catch (NumberFormatException e)
			{
			}
		}
		if (formInstanceId != null)
		{
			FormInstance formInstance = new FormInstance(locationId,formId,formInstanceId);
			this.parsedFiles.put(formInstance, fieldMap);
			return formInstance;
		}
		return null;
	}
	

	public FormInstance parse(InputStream input, 
			FormInstance formInstance,Integer locationTagId)
	{
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		String frontTagValue = null;
		AdministrationService adminService = Context.getAdministrationService();
		HashMap<String, Field> fieldMap = getParsedFile(formInstance);

		// if the file has been previously parsed, return the formInstance
		if (fieldMap != null)
		{
			return formInstance;
		}

		Records records = this.parse(input);
	
		if (records != null)
		{
			fieldMap = new HashMap<String, Field>();

			ArrayList<Field> fields = records.getRecord().getFields();
			for (Field currField : fields)
			{
				fieldMap.put(currField.getId(), currField);
			}

			String xmlIdTagFront = adminService
					.getGlobalProperty("atd.xmlIdTagFront");
			
			// This is possibly an old form with the single key (form_instance_id) barcode
			// The location_id and form_id are passed through in the
			// formInstance parameter
			if (fieldMap.get(xmlIdTagFront) == null)
			{
				if (formInstance != null)
				{
					Integer formId = formInstance.getFormId();
					Integer locationId = formInstance.getLocationId();
					return parseFormInstanceOldForms(fieldMap, formId,
							locationTagId, locationId);
				} else
				{
					return null;
				}
			}

			Integer formIdFront = null;
			Integer formInstanceIdFront = null;
			Integer locationIdFront = null;

			// The unique identifier for the form is in the following form:
			// <locationId>_<formId>_<formInstanceId>
			
			//read the identifier from the front
			if (fieldMap.get(xmlIdTagFront) != null
					&& fieldMap.get(xmlIdTagFront).getValue() != null)
			{
				frontTagValue = fieldMap.get(xmlIdTagFront)
						.getValue();
				
				frontTagValue = frontTagValue.replace("*","");

				StringTokenizer tokenizer = new StringTokenizer(
						frontTagValue, "-");

				// pull out the location id
				if (tokenizer.hasMoreTokens())
				{
					String locationIdString = tokenizer.nextToken();
					if (locationIdString != null)
					{
						try
						{
							locationIdFront = Integer
									.parseInt(locationIdString);
						} catch (NumberFormatException e)
						{
						}
					}
				}

				// pull out the form id
				if (tokenizer.hasMoreTokens())
				{
					String formIdString = tokenizer.nextToken();
					if (formIdString != null)
					{
						try
						{
							formIdFront = Integer.parseInt(formIdString);
						} catch (NumberFormatException e)
						{
						}
					}
				}

				// pull out the form instance id
				if (tokenizer.hasMoreTokens())
				{
					String formInstanceIdString = tokenizer.nextToken();
					if (formInstanceIdString != null)
					{
						try
						{
							formInstanceIdFront = Integer
									.parseInt(formInstanceIdString);
						} catch (NumberFormatException e)
						{
						}
					}
				}
			}
						
			if (locationIdFront != null && 
					formIdFront != null && 
					formInstanceIdFront != null)
			{
				formInstance = new FormInstance(locationIdFront,formIdFront,formInstanceIdFront);

				this.parsedFiles.put(formInstance, fieldMap);
				return formInstance;
			}else{
				String message = "";
				
				if(locationIdFront == null){
					message+=" locationId ";
				}
				if(formIdFront == null){
					message+=" formId ";
				}
				if(formInstanceIdFront == null){
					message+=" formInstanceId ";
				}
				
				Error error = new Error("Fatal", "ID Validity", 
						"The following IDs are missing from the front of the form: "+message+"."
						,"Front tag name: "+xmlIdTagFront
						+ "\r\n " + "front tag value: "+frontTagValue
						, new Date(), null);
				chirdlUtilBackportsService.saveError(error);
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
			if (rightOperand != null && rightOperand instanceof OperandObject && 
					((OperandObject)rightOperand).getObject() instanceof FormInstance)
			{
				FormInstance formInstance = (FormInstance) ((OperandObject)rightOperand).getObject();
				parsedFile = getParsedFile(formInstance);
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

	
	public void clearParsedFiles() {
	    if(parsedFiles != null && !parsedFiles.isEmpty()) {
	        log.info("Before clearing parsedFile, No. of elements" + parsedFiles.size());
	        parsedFiles.clear();
	        log.info("After clearing parsedFile, No. of elements" + parsedFiles.size());
	    }
	}
	
}
