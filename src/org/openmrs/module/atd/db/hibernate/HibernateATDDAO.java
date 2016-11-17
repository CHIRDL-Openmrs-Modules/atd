package org.openmrs.module.atd.db.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Obs;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.LocationTagPrinterConfig;
import org.openmrs.module.atd.db.ATDDAO;
import org.openmrs.module.atd.hibernateBeans.PSFQuestionAnswer;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;

/**
 * Hibernate implementations of ATD database methods.
 * 
 * @author Tammy Dugan
 * 
 */
public class HibernateATDDAO implements ATDDAO
{

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	/**
	 * Empty constructor
	 */
	public HibernateATDDAO()
	{
	}

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}
	
	public PatientATD addPatientATD(PatientATD patientATD) {
		this.sessionFactory.getCurrentSession().saveOrUpdate(patientATD);
		return patientATD;
	}

	public PatientATD getPatientATD(FormInstance formInstance, int fieldId)
	{
		try
		{
			DssService dssService = Context.getService(DssService.class);
			String sql = "select * from atd_patient_atd_element "
					+ "where form_id=? and field_id=? and form_instance_id=? and location_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, formInstance.getFormId());
			qry.setInteger(1, fieldId);
			qry.setInteger(2,formInstance.getFormInstanceId());
			qry.setInteger(3, formInstance.getLocationId());
			qry.addEntity(PatientATD.class);

			List<PatientATD> list = qry.list();

			if (list != null && list.size() > 0)
			{
				PatientATD patientATD = list.get(0);
				Rule rule = patientATD.getRule();

				// This is a hack to get around a hibernate error
				// that I can't figure out how to fix
				if (rule != null && rule.getTokenName() == null)
				{
					rule = dssService.getRule(rule.getRuleId());
					patientATD.setRule(rule);
				}
				return patientATD;
			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.atd.db.ATDDAO#getPatientATDs(org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance, java.util.List)
	 */
	public List<PatientATD> getPatientATDs(FormInstance formInstance, List<Integer> fieldIds) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientATD.class);
		if (formInstance != null) {
			criteria.add(Restrictions.eq("formInstanceId", formInstance.getFormInstanceId()));
			criteria.add(Restrictions.eq("formId", formInstance.getFormId()));
			criteria.add(Restrictions.eq("locationId", formInstance.getLocationId()));
		}
		
		if (fieldIds != null && fieldIds.size() > 0) {
			criteria.add(Restrictions.in("fieldId", fieldIds));
		}
		
		return criteria.list();
	}

	public void updatePatientStates(Date thresholdDate){
	try
	{
		//retire all unretired states before threshold date
		String sql = "update chirdlutilbackports_patient_state " +
		"set retired=?,date_retired=NOW() " + 
		"where start_time < ? and retired=?";		// To speed process of atd initialization we should retire any state (complete or incomplete)
		SQLQuery qry = this.sessionFactory.getCurrentSession()
		.createSQLQuery(sql);

		qry.setBoolean(0,true);
		qry.setDate(1, thresholdDate);
		qry.setBoolean(2,false);
		qry.executeUpdate();
		
		//retire all the other states for the encounters of the retired states
		sql = "UPDATE chirdlutilbackports_patient_state a,"+
				"       (SELECT session_id"+
				"          FROM chirdlutilbackports_patient_state"+
				"         WHERE retired = ?) b"+
				"   SET a.retired = ?, date_retired = NOW()"+
				" WHERE a.session_id = b.session_id AND retired = ?";
		
		qry = this.sessionFactory.getCurrentSession()
		.createSQLQuery(sql);

		qry.setBoolean(0,true);
		qry.setBoolean(1,true);
		qry.setBoolean(2,false);
		qry.executeUpdate();

	} catch (Exception e)
	{
		log.error(Util.getStackTrace(e));
	}
	}
	
	

    public void prePopulateNewFormFields(Integer formId) throws DAOException {
		Connection con = this.sessionFactory.getCurrentSession().connection();
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		// copy concept, default_value, and field_type to the new form
		String step1 = "update field f1, "
			+ "(select b.name,b.default_value,b.concept_id,b.field_type from "
			+ "(select b.name,max(form_id) as form_id from "
			+ "(select name, max(count) as count from "
			+ "(select a.name,a.default_value,max(form_id) as form_id,count(*) as count from "
			+ "(select b.name,default_value,c.form_id from form_field a inner join field b on a.field_id = b.field_id "
			+ "inner join form c on a.form_id = c.form_id "
			+ "where c.form_id not in (?) and c.retired = 0)a "
			+ "group by a.name,a.default_value)a "
			+ "group by name) a "
			+ "inner join "
			+ "(select a.name,a.default_value,max(form_id) as form_id,count(*) as count from "
			+ "(select b.name,default_value,c.form_id from form_field a inner join field b on a.field_id = b.field_id "
			+ "inner join form c on a.form_id = c.form_id "
			+ "where c.form_id not in (?) and c.retired = 0)a "
			+ "group by a.name,a.default_value)b "
			+ "on a.name=b.name and a.count=b.count "
			+ "group by b.name) a "
			+ "inner join "
			+ "(select a.name,default_value,concept_id,field_type,c.form_id from field a inner join form_field b on a.field_id=b.field_id "
			+ "inner join form c on b.form_id = c.form_id where c.retired = 0)b "
			+ "on a.name=b.name and a.form_id=b.form_id) f2, "
			+ "form_field f3 "
			+ "set f1.concept_id=f2.concept_id, f1.default_value=f2.default_value, f1.field_type=f2.field_type "
			+ "where f1.name=f2.name and "
			+ "f1.field_id=f3.field_id and f3.form_id=?";

		// copy parent_field mapping to the new form
		String step2 = "update form_field a, ("
			+ "select b.form_field_id as parent_form_field,b.name as parent_form_field_name,c.form_field_id,c.name as field_name from "
			+ "(select distinct b.name as field_name,c.name as parent_field_name from form_field a "
			+ "inner join field b on a.field_id=b.field_id "
			+ "inner join field c on a.parent_form_field=c.field_id "
			+ "inner join form d on a.form_id=d.form_id "
			+ "where parent_form_field is not null and d.retired = 0) a "
			+ "inner join (select a.*,form_field_id from field a inner join form_field b on a.field_id=b.field_id where form_id=?) b "
			+ "on a.parent_field_name=b.name "
			+ "inner join (select a.*,form_field_id from field a inner join form_field b on a.field_id=b.field_id where form_id=?) c "
			+ "on a.field_name=c.name order by b.name,c.name) b "
			+ "set a.parent_form_field=b.parent_form_field "
			+ "where a.form_field_id=b.form_field_id";

		try {
			ps1 = con.prepareStatement(step1);
			ps1.setInt(1, formId);
			ps1.setInt(2, formId);
			ps1.setInt(3, formId);
			ps1.executeUpdate();
			
			ps2 = con.prepareStatement(step2);
			ps2.setInt(1, formId);
			ps2.setInt(2, formId);
			ps2.executeUpdate();
			con.commit();
		} catch (Exception e) {
			try {
	            con.rollback();
            }
            catch (SQLException e1) {
	            log.error("Error rolling back connection", e1);
            }
            throw new DAOException(e);
		} finally {
			if (ps1 != null) {
				try {
	                ps1.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps2 != null) {
				try {
					ps2.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
		}
    }
    
    public void populateEmtptyFormFields(Integer formId) throws DAOException {
    	Connection con = this.sessionFactory.getCurrentSession().connection();
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		// copy parent_field mapping to the new form
		String step1 = "update form_field a, ("
			+ "select b.form_field_id as parent_form_field,b.name as parent_form_field_name,c.form_field_id,c.name as field_name from "
			+ "(select distinct b.name as field_name,c.name as parent_field_name from form_field a "
			+ "inner join field b on a.field_id=b.field_id "
			+ "inner join field c on a.parent_form_field=c.field_id "
			+ "inner join form d on a.form_id=d.form_id "
			+ "where parent_form_field is not null and d.retired = 0) a "
			+ "inner join (select a.*,form_field_id from field a inner join form_field b on a.field_id=b.field_id where form_id=?) b "
			+ "on a.parent_field_name=b.name "
			+ "inner join (select a.*,form_field_id from field a inner join form_field b on a.field_id=b.field_id where form_id=? and a.field_type is null and concept_id is null and default_value is null) c "
			+ "on a.field_name=c.name order by b.name,c.name) b "
			+ "set a.parent_form_field=b.parent_form_field "
			+ "where a.form_field_id=b.form_field_id";

		// copy concept, default_value, and field_type to the new form		
		String step2 = "update field f1, "
			+ "(select b.name,b.default_value,b.concept_id,b.field_type from "
			+ "(select b.name,max(form_id) as form_id from "
			+ "(select name, max(count) as count from "
			+ "(select a.name,a.default_value,max(form_id) as form_id,count(*) as count from "
			+ "(select b.name,default_value,c.form_id from form_field a inner join field b on a.field_id = b.field_id "
			+ "inner join form c on a.form_id = c.form_id "
			+ "where c.form_id not in (?) and c.retired = 0)a "
			+ "group by a.name,a.default_value)a "
			+ "group by name) a "
			+ "inner join "
			+ "(select a.name,a.default_value,max(form_id) as form_id,count(*) as count from "
			+ "(select b.name,default_value,c.form_id from form_field a inner join field b on a.field_id = b.field_id "
			+ "inner join form c on a.form_id = c.form_id "
			+ "where c.form_id not in (?) and c.retired = 0)a "
			+ "group by a.name,a.default_value)b "
			+ "on a.name=b.name and a.count=b.count "
			+ "group by b.name) a "
			+ "inner join "
			+ "(select a.name,default_value,concept_id,field_type,c.form_id from field a inner join form_field b on a.field_id=b.field_id "
			+ "inner join form c on b.form_id = c.form_id where c.retired = 0)b "
			+ "on a.name=b.name and a.form_id=b.form_id) f2, "
			+ "form_field f3 "
			+ "set f1.concept_id=f2.concept_id, f1.default_value=f2.default_value, f1.field_type=f2.field_type "
			+ "where f1.name=f2.name "
			+ "and f1.concept_id is null "
			+ "and f1.default_value is null "
			+ "and f1.field_type is null "
		    + "and f1.field_id=f3.field_id and f3.form_id=?";

		try {
			ps1 = con.prepareStatement(step1);
			ps1.setInt(1, formId);
			ps1.setInt(2, formId);
			ps1.executeUpdate();
			
			ps2 = con.prepareStatement(step2);
			ps2.setInt(1, formId);
			ps2.setInt(2, formId);
			ps2.setInt(3, formId);
			ps2.executeUpdate();
			
			con.commit();
		} catch (Exception e) {
			try {
	            con.rollback();
            }
            catch (SQLException e1) {
	            log.error("Error rolling back connection", e1);
            }
            throw new DAOException(e);
		} finally {
			if (ps1 != null) {
				try {
	                ps1.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps2 != null) {
				try {
					ps2.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
		}
    }
	
	public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
	                                   String installationDirectory, String serverName, boolean faxableForm, 
	                                   boolean scannableForm, boolean scorableForm, String scoreConfigLoc, 
	                                   Integer numPrioritizedFields, Integer copyPrinterConfigFormId) throws DAOException {
		Connection con = this.sessionFactory.getCurrentSession().connection();
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		
		String merge = installationDirectory + "\\merge\\";
		String formNameDrive = "\\" + formName;
		String step1 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat(?,c.name,?),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='defaultMergeDirectory' and a.retired=0 and d.location_id=c.location_id and (";

		int i = 0;
		String locationStr = "";
	    while (i < locationNames.size()) {
	    	if (i == 0) {
	    		locationStr += "c.name = ?";
	    	} else {
	    		locationStr += " or c.name = ?";
	    	}
	    	i++;
	    }
					
		step1 += locationStr + ") group by d.location_tag_id,d.location_id";
		
		
		try {
			i = 1;
			ps1 = con.prepareStatement(step1);
			ps1.setString(i++, merge);
			ps1.setString(i++, formNameDrive);
			ps1.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps1.setString(i++, locationName);
			}
			ps1.executeUpdate();
			
			if (scannableForm) {
				setupScannableFormValues(con, formId, formName, locationNames, installationDirectory, serverName);
			} else if (faxableForm) {
				setupFaxableFormValues(con, formId, formName, locationNames, installationDirectory, serverName);
			}
			
			if (scorableForm) {
				setupScorableFormValues(con, formId, locationNames, scoreConfigLoc);
			}
			
			if (numPrioritizedFields > 0) {
				setupPrioritizedFormValues(con, formId, numPrioritizedFields, locationNames);
			}
			
			if (copyPrinterConfigFormId != null) {
				copyPrinterConfiguration(con, copyPrinterConfigFormId, formId, locationNames);
			}
			
			con.commit();
		} catch (Exception e) {
			try {
	            con.rollback();
	            log.error("Error setting up form attribute values", e);
            }
            catch (SQLException e1) {
	            log.error("Error rolling back connection", e1);
            }
            throw new DAOException(e);
		} finally {
			if (ps1 != null) {
				try {
	                ps1.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps2 != null) {
				try {
	                ps2.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			
		}
	}

	public void purgeFormAttributeValues(Integer formId) throws DAOException {
		Connection con = this.sessionFactory.getCurrentSession().connection();
		PreparedStatement ps = null;
		String sql = "delete from chirdlutilbackports_form_attribute_value where form_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, formId);
			ps.executeUpdate();
			con.commit();
		} catch (Exception e) {
			log.error("Error deleting form attribute values", e);
			throw new DAOException(e);
		} finally {
			if (ps != null) {
				try {
	                ps.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
		}
	}
	
	public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId) throws DAOException {
		FormPrinterConfig printerConfig = new FormPrinterConfig(formId);
		try {
			LocationService locService = Context.getLocationService();
			Location location = locService.getLocation(locationId);
			List<LocationTagPrinterConfig> tagConfigs = new ArrayList<LocationTagPrinterConfig>();
			ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
			FormAttribute defaultPrinterAttr = chirdlUtilBackportsService.getFormAttributeByName("defaultPrinter");
			FormAttribute alternatePrinterAttr = chirdlUtilBackportsService.getFormAttributeByName("alternatePrinter");
			FormAttribute useAlternatePrinterAttr = chirdlUtilBackportsService.getFormAttributeByName("useAlternatePrinter");
			
			for (LocationTag locTag : location.getTags()) {
				LocationTagPrinterConfig locTagPrinterConfig = new LocationTagPrinterConfig(locTag.getTag(), 
					locTag.getLocationTagId());
				FormAttributeValue defaultPrinterVal = chirdlUtilBackportsService.getFormAttributeValue(formId, "defaultPrinter", 
					locTag.getLocationTagId(), locationId);
				FormAttributeValue altPrinterVal = chirdlUtilBackportsService.getFormAttributeValue(formId, "alternatePrinter", 
					locTag.getLocationTagId(), locationId);
				FormAttributeValue useAltPrinterVal = chirdlUtilBackportsService.getFormAttributeValue(formId, "useAlternatePrinter", 
					locTag.getLocationTagId(), locationId);
				
				if (defaultPrinterVal != null) {
					locTagPrinterConfig.setDefaultPrinter(defaultPrinterVal);
				} else {
					FormAttributeValue newVal = new FormAttributeValue();
					newVal.setFormAttributeId(defaultPrinterAttr.getFormAttributeId());
					newVal.setFormId(formId);
					newVal.setLocationId(locationId);
					newVal.setLocationTagId(locTag.getLocationTagId());
					newVal.setValue("");
					locTagPrinterConfig.setDefaultPrinter(newVal);
				}
				
				if (altPrinterVal != null) {
					locTagPrinterConfig.setAlternatePrinter(altPrinterVal);
				} else {
					FormAttributeValue newVal = new FormAttributeValue();
					newVal.setFormAttributeId(alternatePrinterAttr.getFormAttributeId());
					newVal.setFormId(formId);
					newVal.setLocationId(locationId);
					newVal.setLocationTagId(locTag.getLocationTagId());
					newVal.setValue("");
					locTagPrinterConfig.setAlternatePrinter(newVal);
				}
				
				if (useAltPrinterVal != null) {
					locTagPrinterConfig.setUseAlternatePrinter(useAltPrinterVal);
				} else {
					FormAttributeValue newVal = new FormAttributeValue();
					newVal.setFormAttributeId(useAlternatePrinterAttr.getFormAttributeId());
					newVal.setFormId(formId);
					newVal.setLocationId(locationId);
					newVal.setLocationTagId(locTag.getLocationTagId());
					newVal.setValue("");
					locTagPrinterConfig.setUseAlternatePrinter(newVal);
				}
				
				tagConfigs.add(locTagPrinterConfig);
			}
			
			printerConfig.setLocationTagPrinterConfigs(tagConfigs);
		}
		catch (Exception e) {
			log.error("Error retrieving printer configurations", e);
			throw new DAOException(e);
		}
		
		return printerConfig;
	}
	
	public void savePrinterConfigurations(FormPrinterConfig printerConfig) throws DAOException {
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);

		try {
			List<LocationTagPrinterConfig> configs = printerConfig.getLocationTagPrinterConfigs();
			for (LocationTagPrinterConfig config : configs) {
				FormAttributeValue defaultPrinter = config.getDefaultPrinter();
				FormAttributeValue alternatePrinter = config.getAlternatePrinter();
				FormAttributeValue useAlternatePrinter = config.getUseAlternatePrinter();
				chirdlUtilBackportsService.saveFormAttributeValue(defaultPrinter);
				chirdlUtilBackportsService.saveFormAttributeValue(alternatePrinter);
				chirdlUtilBackportsService.saveFormAttributeValue(useAlternatePrinter);
			}
		}
		catch (Exception e) {
			log.error("Error saving printer configurations", e);
			throw new DAOException(e);
		}
	}
	
	public void copyFormAttributeValues(Integer fromFormId, Integer toFormId) throws DAOException {
		Connection con = this.sessionFactory.getCurrentSession().connection();
		PreparedStatement ps = null;
		String sql = "insert into chirdlutilbackports_form_attribute_value(form_id,value,form_attribute_id,location_tag_id,location_id) "
			+ "select ?, b.value, b.form_attribute_id, b.location_tag_id, b.location_id "
			+ "from chirdlutilbackports_form_attribute_value b where b.form_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, toFormId);
			ps.setInt(2, fromFormId);
			ps.executeUpdate();
			con.commit();
		} catch (Exception e) {
			log.error("Error copying form attribute values", e);
			throw new DAOException(e);
		} finally {
			if (ps != null) {
				try {
	                ps.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
		}
	}
	
	public void setClinicUseAlternatePrinters(List<Integer> locationIds, Boolean useAltPrinters) throws DAOException {
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		FormAttribute formAttribute = chirdlUtilBackportsService.getFormAttributeByName("useAlternatePrinter");
		if (formAttribute != null) {
			try {
				for (Integer locationId : locationIds) {
					Integer formAttributeId = formAttribute.getFormAttributeId();
					
					String sql = "select * from chirdlutilbackports_form_attribute_value where "
					        + "form_attribute_id=? and location_id=?";
					SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
					
					qry.setInteger(0, formAttributeId);
					qry.setInteger(1, locationId);
					qry.addEntity(FormAttributeValue.class);
					
					List<FormAttributeValue> list = qry.list();
					
					if (list != null && list.size() > 0) {
						for (FormAttributeValue fav : list) {
							fav.setValue(useAltPrinters.toString());
							chirdlUtilBackportsService.saveFormAttributeValue(fav);
						}
					}
				}
			} 
			catch (Exception e) {
				log.error("Error updating alternate printers", e);
				throw new DAOException(e);
			}
		}
	}
	
	public Boolean isFormEnabledAtClinic(Integer formId, Integer locationId) throws DAOException {
		Boolean enabled = Boolean.FALSE;
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);

		try {
			FormAttribute formAttribute = chirdlUtilBackportsService.getFormAttributeByName("defaultMergeDirectory");	
			if (formAttribute != null) {
				Integer formAttributeId = formAttribute.getFormAttributeId();
				
				String sql = "select * from chirdlutilbackports_form_attribute_value where form_id=? "
				        + "and form_attribute_id=? and location_id=?";
				SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
				
				qry.setInteger(0, formId);
				qry.setInteger(1, formAttributeId);
				qry.setInteger(2, locationId);
				qry.addEntity(FormAttributeValue.class);
				
				List<FormAttributeValue> list = qry.list();
				
				if (list != null && list.size() > 0) {
					enabled = Boolean.TRUE;
				}
				
			}
		}
		catch (Exception e) {
			log.error("Error checking to see if form " + formId + " is enabled in clinic " + locationId, e);
			throw new DAOException(e);
		}
		
		return enabled;
	}
	
	private void setupScannableFormValues(Connection con, Integer formId, String formName, List<String> locationNames, 
		                                  String installationDirectory, String serverName) throws SQLException {
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		
		String scan = installationDirectory + "\\scan\\";
		String images = "\\\\" + serverName + "\\images\\";
		String formNameDrive = "\\" + formName;
		String step1 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat(?,c.name,?),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='defaultExportDirectory' and a.retired=0 and d.location_id=c.location_id and (";

		int i = 0;
		String locationStr = "";
	    while (i < locationNames.size()) {
	    	if (i == 0) {
	    		locationStr += "c.name = ?";
	    	} else {
	    		locationStr += " or c.name = ?";
	    	}
	    	i++;
	    }
					
		step1 += locationStr + ") group by d.location_tag_id,d.location_id";
		
		String step2 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat(?,c.name,?),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='imageDirectory' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		String step3 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),'MRNBarCode',max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d, location c "
			+ "where a.form_id=? and b.name='medRecNumberTag' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		try {
			i = 1;
			ps1 = con.prepareStatement(step1);
			ps1.setString(i++, scan);
			ps1.setString(i++, formNameDrive + "_SCAN");
			ps1.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps1.setString(i++, locationName);
			}
			ps1.executeUpdate();
			
			i = 1;
			ps2 = con.prepareStatement(step2);
			ps2.setString(i++, images);
			ps2.setString(i++, formNameDrive);
			ps2.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps2.setString(i++, locationName);
			}
			ps2.executeUpdate();
			
			i = 1;
			ps3 = con.prepareStatement(step3);
			ps3.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps3.setString(i++, locationName);
			}
			ps3.executeUpdate();
			
			i = 1;
			
		} finally {
			if (ps1 != null) {
				try {
	                ps1.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps2 != null) {
				try {
	                ps2.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps3 != null) {
				try {
	                ps3.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			
		}
	}
	
	private void setupFaxableFormValues(Connection con, Integer formId, String formName, List<String> locationNames, 
		                                  String installationDirectory, String serverName) throws SQLException {
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		
		String scan = installationDirectory + "\\scan\\";
		String images = "\\\\" + serverName + "\\images\\";
		String formNameDrive = "\\" + formName;
		String step1 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat(?,'Fax',?),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='defaultExportDirectory' and a.retired=0 and d.location_id=c.location_id and (";

		int i = 0;
		String locationStr = "";
	    while (i < locationNames.size()) {
	    	if (i == 0) {
	    		locationStr += "c.name = ?";
	    	} else {
	    		locationStr += " or c.name = ?";
	    	}
	    	i++;
	    }
					
		step1 += locationStr + ") group by d.location_tag_id,d.location_id";
		
		String step2 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat(?,'Fax',?),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='imageDirectory' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		String step3 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),'MRNBarCode',max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d, location c "
			+ "where a.form_id=? and b.name='medRecNumberTag' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		String step4 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),'MRNBarCodeBack',max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d, location c "
			+ "where a.form_id=? and b.name='medRecNumberTag2' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";


		try {
			i = 1;
			ps1 = con.prepareStatement(step1);
			ps1.setString(i++, scan);
			ps1.setString(i++, formNameDrive + "_SCAN");
			ps1.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps1.setString(i++, locationName);
			}
			ps1.executeUpdate();
			
			i = 1;
			ps2 = con.prepareStatement(step2);
			ps2.setString(i++, images);
			ps2.setString(i++, formNameDrive);
			ps2.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps2.setString(i++, locationName);
			}
			ps2.executeUpdate();
			
			i = 1;
			ps3 = con.prepareStatement(step3);
			ps3.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps3.setString(i++, locationName);
			}
			ps3.executeUpdate();
			
			i = 1;
			ps4 = con.prepareStatement(step4);
			ps4.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps4.setString(i++, locationName);
			}
			ps4.executeUpdate();
		} finally {
			if (ps1 != null) {
				try {
	                ps1.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps2 != null) {
				try {
	                ps2.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps3 != null) {
				try {
	                ps3.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
			if (ps4 != null) {
				try {
	                ps4.close();
                }
                catch (SQLException e) {
	                log.error("Error closing prepared statement", e);
                }
			}
		}
	}
	
	private void setupScorableFormValues(Connection con, Integer formId, List<String> locationNames, 
		                                  String scoreConfigLoc) throws SQLException {
		PreparedStatement ps = null;
		String locationStr = "";
		int i = 0;
	    while (i < locationNames.size()) {
	    	if (i == 0) {
	    		locationStr += "c.name = ?";
	    	} else {
	    		locationStr += " or c.name = ?";
	    	}
	    	i++;
	    }
	    
		String sql = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),?,max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='scorableFormConfigFile' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		try {
			i = 1;
			ps = con.prepareStatement(sql);
			ps.setString(i++, scoreConfigLoc);
			ps.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps.setString(i++, locationName);
			}
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}
	
	private void setupPrioritizedFormValues(Connection con, Integer formId, Integer numPrioritizedFields, 
	                                        List<String> locationNames) throws SQLException {
		PreparedStatement ps = null;
		String locationStr = "";
		int i = 0;
	    while (i < locationNames.size()) {
	    	if (i == 0) {
	    		locationStr += "c.name = ?";
	    	} else {
	    		locationStr += " or c.name = ?";
	    	}
	    	i++;
	    }
	    
		String sql = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),?,max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='numPrompts' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		try {
			i = 1;
			ps = con.prepareStatement(sql);
			ps.setString(i++, String.valueOf(numPrioritizedFields));
			ps.setInt(i++, formId);
			for (String locationName : locationNames) {
				ps.setString(i++, locationName);
			}
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}
	
	private void copyPrinterConfiguration(Connection con, Integer fromFormId, Integer toFormId, List<String> locationNames) 
	throws SQLException {		
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		String locationStr = "";
		int i = 0;
	    while (i < locationNames.size()) {
	    	if (i == 0) {
	    		locationStr += "c.name = ?";
	    	} else {
	    		locationStr += " or c.name = ?";
	    	}
	    	i++;
	    }
	    
		String sql1 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat_ws('',(select x.value from chirdlutilbackports_form_attribute_value x, chirdlutilbackports_form_attribute y "
					+ "where x.location_id=d.location_id and x.location_tag_id=d.location_tag_id and x.form_id=? and "
					+ "y.name='defaultPrinter' and y.form_attribute_id = x.form_attribute_id)),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='defaultPrinter' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		String sql2 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat_ws('',(select x.value from chirdlutilbackports_form_attribute_value x, chirdlutilbackports_form_attribute y "
					+ "where x.location_id=d.location_id and x.location_tag_id=d.location_tag_id and x.form_id=? and "
					+ "y.name='alternatePrinter' and y.form_attribute_id = x.form_attribute_id)),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='alternatePrinter' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		String sql3 = "INSERT INTO chirdlutilbackports_form_attribute_value "
			+ "(`form_id`, `value`, `form_attribute_id`,location_tag_id,location_Id) "
			+ "select max(form_id),concat_ws('',(select x.value from chirdlutilbackports_form_attribute_value x, chirdlutilbackports_form_attribute y "
					+ "where x.location_id=d.location_id and x.location_tag_id=d.location_tag_id and x.form_id=? and "
					+ "y.name='useAlternatePrinter' and y.form_attribute_id = x.form_attribute_id)),max(form_attribute_id), "
			+ "d.location_tag_id,d.location_id "
			+ "from form a, chirdlutilbackports_form_attribute b, location_tag_map d ,location c where a.form_id=? "
			+ "and b.name='useAlternatePrinter' and a.retired=0 and d.location_id=c.location_id and ("
			+ locationStr + ") group by d.location_tag_id,d.location_id";
		
		try {
			i = 1;
			ps1 = con.prepareStatement(sql1);
			ps1.setInt(i++, fromFormId);
			ps1.setInt(i++, toFormId);
			for (String locationName : locationNames) {
				ps1.setString(i++, locationName);
			}
			ps1.executeUpdate();
			
			i = 1;
			ps2 = con.prepareStatement(sql2);
			ps2.setInt(i++, fromFormId);
			ps2.setInt(i++, toFormId);
			for (String locationName : locationNames) {
				ps2.setString(i++, locationName);
			}
			ps2.executeUpdate();
			
			i = 1;
			ps3 = con.prepareStatement(sql3);
			ps3.setInt(i++, fromFormId);
			ps3.setInt(i++, toFormId);
			for (String locationName : locationNames) {
				ps3.setString(i++, locationName);
			}
			ps3.executeUpdate();
		} finally {
			if (ps1 != null) {
				ps1.close();
			}
			if (ps2 != null) {
				ps2.close();
			}
			if (ps3 != null) {
				ps3.close();
			}
		}
	}
	
	public List<Statistics> getStatByFormInstance(int formInstanceId,
		String formName, Integer locationId)
{
	try
	{
		String sql = "select * from atd_statistics where form_instance_id=? and form_name=? "+
		"and location_id=?";
		SQLQuery qry = this.sessionFactory.getCurrentSession()
				.createSQLQuery(sql);
		qry.setInteger(0, formInstanceId);
		qry.setString(1, formName);
		qry.setInteger(2,locationId);
		qry.addEntity(Statistics.class);
		return qry.list();
	} catch (Exception e)
	{
		this.log.error(Util.getStackTrace(e));
	}
	return null;
}
	
	public List<Statistics> getStatByIdAndRule(int formInstanceId, int ruleId,
		String formName, Integer locationId)
{
	try
	{
		String sql = "select * from atd_statistics where form_instance_id=? "+
		"and rule_id=? and form_name=? and location_id=?";
		SQLQuery qry = this.sessionFactory.getCurrentSession()
				.createSQLQuery(sql);
		qry.setInteger(0, formInstanceId);
		qry.setInteger(1, ruleId);
		qry.setString(2, formName);
		qry.setInteger(3,locationId);
		qry.addEntity(Statistics.class);
		return qry.list();
	} catch (Exception e)
	{
		this.log.error(Util.getStackTrace(e));
	}
	return null;
}
	public List<Statistics> getStatsByEncounterForm(Integer encounterId,String formName)
	{
		try
		{
			String sql = "select * from atd_statistics where obsv_id is not null and encounter_id=? and form_name=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.setString(1, formName);
			qry.addEntity(Statistics.class);
			return qry.list();
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}
	public List<Statistics> getAllStatsByEncounterForm(Integer encounterId,String formName)
	{
		try
		{
			String sql = "select * from atd_statistics where encounter_id=? and form_name=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.setString(1, formName);
			qry.addEntity(Statistics.class);
			return qry.list();
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}
	public List<Statistics> getStatsByEncounterFormNotPrioritized(Integer encounterId,String formName)
	{
		try
		{
			String sql = "select * from atd_statistics where rule_id is null and obsv_id is not null and encounter_id=? and form_name=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.setString(1, formName);
			qry.addEntity(Statistics.class);
			return qry.list();
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public void addStatistics(Statistics statistics)
	{
		try
		{
			this.sessionFactory.getCurrentSession().save(statistics);
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
	}

	public void updateStatistics(Statistics statistics)
	{
		try
		{
			this.sessionFactory.getCurrentSession().update(statistics);
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
	}
	
	/**
	 * This is a method I added to get around lazy initialization errors with patient.getIdentifier() in rules
	 * Auto generated method comment
	 * 
	 * @param patientId
	 * @return
	 */
	public PatientIdentifier getPatientMRN(Integer patientId)
	{
		try
		{
			String sql = "select a.* from patient_identifier a "+
				"inner join patient_identifier_type b on a.identifier_type=b.patient_identifier_type_id "+
				"where patient_id=? and b.name='MRN_OTHER' and preferred=1";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, patientId);
			qry.addEntity(PatientIdentifier.class);
			return (PatientIdentifier) qry.uniqueResult();
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}

	/**
	 * @see org.openmrs.module.atd.db.ATDDAO#getPSFQuestionAnswers(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
    public List<PSFQuestionAnswer> getPSFQuestionAnswers(Integer formInstanceId, Integer locationId, Integer patientId) {
    	try
		{
    		String sql = 
    			"SELECT DISTINCT b.text," +
    			"                a.answer," +
    			"                b.form_instance_id," +
    			"                b.form_id," +
    			"                b.location_Id," +
    			"                a.encounter_id" +
    			"  FROM (SELECT *" +
    			"          FROM atd_statistics a" +
    			"         WHERE     form_name = 'PSF'" +
    			"               AND form_instance_id = ?" +
    			"               AND location_id = ?" +
    			"               AND answer <> 'NoAnswer') a" +
    			"       INNER JOIN form c" +
    			"          ON a.form_name = c.name" +
    			"       INNER JOIN atd_patient_atd_element b" +
    			"          ON     c.form_Id = b.form_id" +
    			"             AND a.form_instance_id = b.form_instance_id" +
    			"             AND a.location_Id = b.location_Id" +
    			"             AND a.rule_id = b.rule_id" +
    			" WHERE b.patient_id = ?";

			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			// Had to do these scalar calls because MySQL is not happy with text types.
			// The following error will occur if this is not done: No Dialect mapping for JDBC type: -1
			qry.addScalar("b.text", Hibernate.STRING);
			qry.addScalar("a.answer");
			qry.addScalar("b.form_instance_id");
			qry.addScalar("b.form_id");
			qry.addScalar("b.location_id");
			qry.addScalar("a.encounter_id");
			qry.setInteger(0, formInstanceId);
			qry.setInteger(1, locationId);
			qry.setInteger(2, patientId);
			List<Object[]> list = qry.list();
			List<PSFQuestionAnswer> returnList = new ArrayList<PSFQuestionAnswer>();
			for (Object[] arry : list) {
				PSFQuestionAnswer pair = new PSFQuestionAnswer();
				pair.setQuestion((String)arry[0]);
				pair.setAnswer((String)arry[1]);
				pair.setFormInstanceId((Integer)arry[2]);
				pair.setFormId((Integer)arry[3]);
				pair.setLocationId((Integer)arry[4]);
				pair.setEncounterId((Integer)arry[5]);
				returnList.add(pair);
			}
			
			return returnList;
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return new ArrayList<PSFQuestionAnswer>();
    }

/**
 * @see org.openmrs.module.atd.db.ATDDAO#getAllConcept()
 */
	public List<ConceptDescriptor> getAllConcepts() throws SQLException {
		List<ConceptDescriptor> cdList = new ArrayList<ConceptDescriptor>();
		Connection con = this.sessionFactory.getCurrentSession().connection();
		String sql = "SELECT distinct a.*, b.name AS \"parent concept\""
                   + "FROM    (SELECT a.name AS name,"
                   + "                c.name AS \"concept class\","
                   + "                d.name AS \"datatype\","
                   + "                e.description AS description,"
                   + "                g.concept_id,"
                   + "                f.units AS units"
                   + "           FROM concept_name a"
                   + "                INNER JOIN concept b"
                   + "                    ON a.concept_id = b.concept_id"
                   + "                INNER JOIN concept_class c"
                   + "                    ON b.class_id = c.concept_class_id"
                   + "                INNER JOIN concept_datatype d"
                   + "                    ON b.datatype_id = d.concept_datatype_id"
                   + "                LEFT JOIN concept_description e"
                   + "                    ON e.concept_id = b.concept_id"
                   + "                LEFT JOIN concept_numeric f"
                   + "                    ON f.concept_id = b.concept_id"
                   + "                LEFT JOIN concept_answer g"
                   + "                    ON g.answer_concept = b.concept_id) a"
                   + "          LEFT JOIN"
                   + "               concept_name b"
                   + "               ON a.concept_id = b.concept_id";
		try {
			java.sql.Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				String name = rs.getString("name");
				String conceptClass = rs.getString("concept class");
				String dataType = rs.getString("datatype");
				String description = rs.getString("description");
				Integer conceptId = rs.getInt("concept_id");
				String units = rs.getString("units");
				String parentConcept = rs.getString("parent concept");
				ConceptDescriptor cd = new ConceptDescriptor();
				cd.setName(name);
				cd.setConceptClass(conceptClass);
				cd.setDatatype(dataType);
				cd.setDescription(description);
				cd.setUnits(units);
				cd.setParentConcept(parentConcept);
				cd.setConceptId(conceptId);
				cdList.add(cd);
			}
		}finally{
			con.close();
			
		}
		
		return cdList;
	}
	
	/**
	 *  @see org.openmrs.module.atd.db.ATDDAO#getAllFormDefinitions()
	 */
	public List<FormDefinitionDescriptor> getAllFormDefinitions() throws SQLException {
		List<FormDefinitionDescriptor> fddList = new ArrayList<FormDefinitionDescriptor>();
		Connection con = this.sessionFactory.getCurrentSession().connection();
		String sql = "SELECT a.name AS form_name, a.description AS form_description, b.name AS field_name, c.name AS field_type, d.name AS concept_name, b.default_value, ff.field_number, e.name AS parent_field_name FROM form a INNER JOIN form_field ff ON ff.form_id = a.form_id"
       + " INNER JOIN field b ON ff.field_id = b.field_id INNER JOIN field_type c ON b.field_type = c.field_type_id LEFT JOIN concept_name d ON b.concept_id = d.concept_id LEFT JOIN (SELECT b.*, a.name FROM    field a INNER JOIN form_field b ON a.field_id = b.field_id) e ON ff.parent_form_field = e.form_field_id AND a.form_id = e.form_id WHERE a.retired = 0";
		try {
			java.sql.Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				String formName = rs.getString("form_name");
				String formDescription = rs.getString("form_description");
				String fieldName = rs.getString("field_name");
				String fieldType = rs.getString("field_type");
				String conceptName = rs.getString("concept_name");
				String defaultValue = rs.getString("default_value");
				int fieldNumber = rs.getInt("field_number");
				String parentFieldName = rs.getString("parent_field_name");
				FormDefinitionDescriptor fdd = new FormDefinitionDescriptor();
				fdd.setFormName(formName);
				fdd.setFormDescription(formDescription);
				fdd.setFieldName(fieldName);
				fdd.setFieldType(fieldType);
				fdd.setConceptName(conceptName);
				fdd.setDefaultValue(defaultValue);
				fdd.setFieldNumber(fieldNumber);
				fdd.setParentFieldName(parentFieldName);
				fddList.add(fdd);
			}
		}finally{
			con.close();
		}
		
		return fddList;
	}
	/**
	 * @see org.openmrs.module.atd.db.ATDDAO#getFormDefinition()
	 */
	public List<FormDefinitionDescriptor> getFormDefinition(int formId) throws SQLException{
		List<FormDefinitionDescriptor> fddList = new ArrayList<FormDefinitionDescriptor>(); 
		Connection con = this.sessionFactory.getCurrentSession().connection();
		String sql = "SELECT a.name AS form_name, a.description AS form_description, b.name AS field_name, c.name AS field_type, d.name AS concept_name, b.default_value, ff.field_number, e.name AS parent_field_name "
				   + "FROM form a INNER JOIN form_field ff ON ff.form_id = a.form_id INNER JOIN field b ON ff.field_id = b.field_id INNER JOIN field_type c ON b.field_type = c.field_type_id LEFT JOIN concept_name d ON b.concept_id = d.concept_id "
				   + "LEFT JOIN (SELECT b.*, a.name FROM field a INNER JOIN form_field b ON a.field_id = b.field_id) e ON ff.parent_form_field = e.form_field_id AND a.form_id = e.form_id  WHERE a.retired = 0 AND a.form_id IN (?)";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, formId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String formName = rs.getString("form_name");
				String formDescription = rs.getString("form_description");
				String fieldName = rs.getString("field_name");
				String fieldType = rs.getString("field_type");
				String conceptName = rs.getString("concept_name");
				String defaultValue = rs.getString("default_value");
				int fieldNumber = rs.getInt("field_number");
				String parentFieldName = rs.getString("parent_field_name");
				FormDefinitionDescriptor fdd = new FormDefinitionDescriptor();
				fdd.setFormName(formName);
				fdd.setFormDescription(formDescription);
				fdd.setFieldName(fieldName);
				fdd.setFieldType(fieldType);
				fdd.setConceptName(conceptName);
				fdd.setDefaultValue(defaultValue);
				fdd.setFieldNumber(fieldNumber);
				fdd.setParentFieldName(parentFieldName);
				fddList.add(fdd);
			}
			
		} finally{
			con.close();
		}
		return fddList;
	}
	
	/**
	 * DWE CHICA-332 4/16/15
	 * 
	 * @see org.openmrs.module.atd.db.ATDDAO#getFormAttributeValueLocationsAndTagsMap(Integer)
	 */
	@Override
	public HashMap<Integer, List<Integer>> getFormAttributeValueLocationsAndTagsMap(Integer formId)
	{
		try
		{
			HashMap<Integer, List<Integer>> returnMap = new HashMap<Integer, List<Integer>>();
			
			// Query for "editable" form attribute values that have been previously configured
			String sql = "SELECT DISTINCT b.location_tag_id, b.location_id " + 
						 "FROM chirdlutilbackports_form_attribute_value b " +
						 "INNER JOIN (SELECT form_attribute_id FROM chirdlutilbackports_form_attribute WHERE name NOT IN('defaultMergeDirectory','defaultExportDirectory','formInstanceIdTag','formInstanceIdTag2','medRecNumberTag','medRecNumberTag2','imageDirectory','numQuestions')) av ON b.form_attribute_id = av.form_attribute_id " +
			             "WHERE b.form_id = ? " +
			             "ORDER BY b.location_tag_id, b.location_id";
			
			SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
			qry.setInteger(0, formId);
			
			List<Object[]> list = qry.list();
			
			if (list != null && list.size() > 0) 
			{
				List<Integer> locTagIds = new ArrayList<Integer>();
				for(Object[] objArray : list)
				{
					Integer locationId = (Integer)objArray[1];
					locTagIds = returnMap.get(locationId);
					if(locTagIds == null) // This is the first location tag Id that is being added to the list, create a new list
					{
						locTagIds = new ArrayList<Integer>();
					}
					
					locTagIds.add((Integer)objArray[0]);
					returnMap.put(locationId, locTagIds);
				}
			}
			
			return returnMap;
		}
		catch(Exception e)
		{
			log.error("Error in method getFormAttributeValueLocationsAndTagsMap. Error loading location ids and location tag ids (form_id = " + formId + ").", e);
		}
		
		return new HashMap<Integer,List<Integer>>();
	}
	
	/**
	 * DWE CHICA-330 4/22/15 
	 *
	 * @see org.openmrs.module.atd.db.ATDDAO#getConceptDescriptorList(int, int, String, boolean, int, String, String, boolean)
	 * 
	 */
	public List<ConceptDescriptor> getConceptDescriptorList(int start, int length, String searchValue, boolean includeRetired, int conceptClassId, String orderByColumn, String ascDesc, boolean exactMatchSearch) 
	{
		List<ConceptDescriptor> cdList = new ArrayList<ConceptDescriptor>();
		try
		{
			SQLQuery qry = getConceptQuery(start, length, searchValue, includeRetired, conceptClassId, orderByColumn, ascDesc, exactMatchSearch);
			List<Object[]> list = qry.list();
			
			if (list != null && list.size() > 0) 
			{
				for(Object[] objArray : list)
				{
					Integer id = objArray[0] == null ? -1 : (Integer)objArray[0];
					String name = objArray[1] == null ? "" : (String)objArray[1];
					String conceptClass = objArray[2] == null ? "" : (String)objArray[2];
					String datatype = objArray[3] == null ? "" : (String)objArray[3];
					String description = objArray[4] == null ? "" : (String)objArray[4];
					String parentConcept = objArray[7] == null ? "" : (String)objArray[7];
					String units = objArray[6] == null ? "" : (String)objArray[6];
					Integer parentId = objArray[5] == null ? -1 : (Integer)objArray[5];
					Timestamp dateCreatedStamp = (Timestamp)objArray[10];
					
					cdList.add(new ConceptDescriptor(name, conceptClass, datatype, description, parentConcept, units, id, parentId, dateCreatedStamp));
				}
			}
			
			return cdList;
		}
		catch(Exception e)
		{
			log.error("Error in method getAllConcepts.", e);
		}
		
		return new ArrayList<ConceptDescriptor>();
	}
	
	/**
	 * DWE CHICA-330 4/23/15 
	 * 
	 * @see org.openmrs.module.atd.db.ATDDAO#getCountConcepts(String, boolean, int, boolean)
	 */
	public int getCountConcepts(String searchValue, boolean includeRetired, int conceptClassId, boolean exactMatchSearch)
	{
		try
		{
			SQLQuery qry = getConceptQuery(-1, -1, searchValue, includeRetired, conceptClassId, "conceptId", "ASC", exactMatchSearch);
			return qry.list().size();
		}
		catch(Exception e)
		{
			log.error("Error in method getCountConcepts.", e);
			return -1;
		}
	}
	
	/**
	 * DWE CHICA-330 4/23/15
	 * Creates a SQLQuery object for use with counting and finding concept records
	 * 
	 * @param start - first result record to start with for paging
	 * @param length - max number of results to return for paging
	 * @param searchValue - search by concept name or parent concept name
	 * @param includeRetired - true to include retired concepts
	 * @param conceptClassId - filter by concept class
	 * @param orderByColumn - order by column
	 * @param ascDesc - ASC or DESC
	 * @param exactMatchSearch - true to perform and exact match search using the searchValue parameter
	 * @return
	 */
	private SQLQuery getConceptQuery(int start, int length, String searchValue, boolean includeRetired, int conceptClassId, String orderByColumn, String ascDesc, boolean exactMatchSearch)
	{
		String whereClause = "";
		boolean needsAnd = false;
		
		if(!searchValue.isEmpty() || !includeRetired || conceptClassId > -1)
		{
			whereClause = " WHERE ";
			if(!searchValue.isEmpty())
			{
				if(exactMatchSearch)
				{
					whereClause += "(a.name = '" + searchValue + "' OR b.name = '" + searchValue + "')";
				}
				else
				{
					whereClause += "(a.name LIKE '%" + searchValue + "%' OR b.name LIKE '%" + searchValue + "%')";
				}
				
				needsAnd = true;
			}
			
			if(!includeRetired)
			{
				if(needsAnd){whereClause += " AND ";}
				whereClause += "a.retired = 0";
				needsAnd = true;
			}
			
			if(conceptClassId > -1)
			{
				if(needsAnd){whereClause += " AND ";}
				whereClause += "a.conceptClassId = " + conceptClassId;
				needsAnd = true;
			}
		}
		
		String orderBy = " ORDER BY ";
		if(orderByColumn.equalsIgnoreCase("name"))
		{
			orderBy += "a.name ";
		}
		else if(orderByColumn.equalsIgnoreCase("parentConcept"))
		{ 
			orderBy += "b.name ";
		}
		else if(orderByColumn.equalsIgnoreCase("formattedDateCreated"))
		{
			orderBy += "a.dateCreated ";
		}
		else
		{
			orderBy += "a.conceptId ";
		}
		orderBy += ascDesc;
		
		String sqlString = "SELECT distinct a.*, b.name AS \"parentConcept\" "
                + "FROM    (SELECT a.concept_id AS conceptId, "
                + "	               a.name AS name, "
                + "                c.name AS \"conceptClass\", "
                + "                d.name AS \"datatype\", "
                + "                e.description AS description, "
                + "                g.concept_id AS parentConceptId, "
                + "                f.units AS units, "
                + "				   con.retired AS retired, "
                + "				   c.concept_class_id AS conceptClassId, "
                + "                con.date_created AS dateCreated  "
                + "           FROM concept_name a "
                + "                INNER JOIN concept con "
                + "                    ON a.concept_id = con.concept_id "
                + "                INNER JOIN concept_class c "
                + "                    ON con.class_id = c.concept_class_id "
                + "                INNER JOIN concept_datatype d "
                + "                    ON con.datatype_id = d.concept_datatype_id "
                + "                LEFT JOIN concept_description e "
                + "                    ON e.concept_id = con.concept_id "
                + "                LEFT JOIN concept_numeric f "
                + "                    ON f.concept_id = con.concept_id "
                + "                LEFT JOIN concept_answer g "
                + "                    ON g.answer_concept = con.concept_id) a "
                + "          LEFT JOIN "
                + "               concept_name b "
                + "               ON a.parentConceptId = b.concept_id " + whereClause + orderBy;
		
		SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sqlString);
		qry.addScalar("conceptId", Hibernate.INTEGER);
		qry.addScalar("name", Hibernate.STRING);
		qry.addScalar("conceptClass", Hibernate.STRING);
		qry.addScalar("datatype", Hibernate.STRING);
		qry.addScalar("description", Hibernate.STRING);
		qry.addScalar("parentConceptId", Hibernate.INTEGER);
		qry.addScalar("units",  Hibernate.STRING);
		qry.addScalar("parentConcept", Hibernate.STRING);
		qry.addScalar("retired", Hibernate.BOOLEAN);
		qry.addScalar("conceptClassId", Hibernate.INTEGER);
		qry.addScalar("dateCreated", Hibernate.TIMESTAMP);
		
		if(start > -1 && length > -1)
		{
			qry.setFirstResult(start);
			qry.setMaxResults(length);
		}
		
		return qry;
	}
	
	/**
     * DWE CHICA-437 
     * Gets a list of obs records where there is a related atd_statistics record with the formFieldId
     * 
     * @param encounterId
     * @param conceptId
     * @param formFieldId
     * @param includeVoidedObs
     * @return
     */
    public List<Obs> getObsWithStatistics(Integer encounterId, Integer conceptId, Integer formFieldId, boolean includeVoidedObs)
    {
    	String voidedString = "";
    	if(!includeVoidedObs)
    	{
    		voidedString = " and a.voided=false";
    	}
    	
		try
		{
			String sql = "select a.* from obs a "+
				"inner join atd_statistics b on a.obs_id=b.obsv_id "+
				"where a.encounter_id=? and a.concept_id=? and b.form_field_id=?" + voidedString;
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.setInteger(1, conceptId);
			qry.setInteger(2, formFieldId);
			qry.addEntity(Obs.class);
			return qry.list();
		} 
		catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
			return new ArrayList<Obs>();
		}
    }
    
    /**
     * DWE CHICA-612
     * Gets a sorted list of all atd_statistics with the encounterId and formName
     * @param encounterId
     * @param formName
     * @param orderAscDesc - ASC or DESC
     * @return sorted list of Statistics
     */
    public List<Statistics> getAllStatsByEncounterForm(Integer encounterId,String formName, String orderAscDesc)
	{
    	Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Statistics.class);
    	criteria.add(Expression.eq("encounterId", encounterId));
    	criteria.add(Expression.eq("formName", formName));
    	
    	if("ASC".equals(orderAscDesc))
    	{
    		criteria.addOrder(Order.asc("statisticsId"));
    	}
    	else if("DESC".equals(orderAscDesc))
    	{
    		criteria.addOrder(Order.desc("statisticsId"));
    	}
		return criteria.list();
	}
    
    /**
     * Checks to see if at least one box is checked for this rule and encounter 
     * in the atd_statistics table
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
    public boolean oneBoxChecked(Integer encounterId, Integer ruleId)
	{
    	boolean oneBoxChecked = false;
    	Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Statistics.class);
    	criteria.add(Expression.eq("encounterId", encounterId));
    	criteria.add(Expression.eq("ruleId", ruleId));
    	criteria.add(Expression.like("answer", "%Y%"));
    	
		List results = criteria.list();
		
		if(results != null && results.size()>0){
			oneBoxChecked = true;
		}
		
		return oneBoxChecked;
	}
}
