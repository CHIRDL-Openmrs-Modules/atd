package org.openmrs.module.atd.db.hibernate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.db.ATDDAO;
import org.openmrs.module.atd.hibernateBeans.ATDError;
import org.openmrs.module.atd.hibernateBeans.FormAttribute;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Program;
import org.openmrs.module.atd.hibernateBeans.ProgramTagMap;
import org.openmrs.module.atd.hibernateBeans.Session;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateAction;
import org.openmrs.module.atd.hibernateBeans.StateMapping;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.chirdlutil.util.Util;

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

	public boolean tableExists(String tableName)
	{
		try
		{
			Connection con = this.sessionFactory.getCurrentSession()
					.connection();
			DatabaseMetaData dbmd = con.getMetaData();

			// Check if table exists

			ResultSet rs = dbmd.getTables(null, null, tableName, null);

			if (rs.next())
			{
				return true;
			}
		} catch (Exception e)
		{
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
		return false;
	}

	public void executeSql(String sql)
	{
		Connection con = this.sessionFactory.getCurrentSession().connection();
		try
		{
			Statement stmt = con.createStatement();
			stmt.execute(sql);
			con.commit();
		} catch (Exception e)
		{
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
	}
	
	private Integer insertFormInstance(Integer formId, Integer locationId) {
		try {
			FormService formService = Context.getFormService();
			Form form = formService.getForm(formId);
			String formName = form.getName();
			// This is a work around since I couldn't get hibernate to create
			// a two column auto-generated key
			StatelessSession session = this.sessionFactory.openStatelessSession();
			Transaction tx = session.beginTransaction();
			Connection con = session.connection();
			int rowsInserted = 0;
			String sql = "insert into atd_form_instance(form_instance_id,form_id,location_id) "
			        + " select max(form_instance_id),?,? from (select form_instance_id from  ("
			        + " select max(form_instance_id)+1 as form_instance_id,location_id "
			        + " from atd_form_instance where location_id=? and form_id in "
			        + " (select form_id from form where name=?) group by location_id)a" + " union select 1 from dual)a";
			PreparedStatement stmt = null;
			try {
				stmt = con.prepareStatement(sql);
				stmt.setInt(1, formId);
				stmt.setInt(2, locationId);
				stmt.setInt(3, locationId);
				stmt.setString(4, formName);
				
				rowsInserted = stmt.executeUpdate();
			}
			catch (Exception e) {
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}finally{
				try {
	                if(stmt != null){
	                	stmt.close();
	                }
	                if(tx != null){
	                	tx.commit();
	                }
	                if(session != null){
	                	session.close();
	                }
                }
                catch (Exception e) {
	                log.error("Error generated", e);
                }
			}
			
			return rowsInserted;
			
		}
		catch (Exception e) {
			log.error(Util.getStackTrace(e));
		}
		return 0;
	}


	public FormInstance addFormInstance(Integer formId, Integer locationId) {
		PreparedStatement stmt = null;
		Transaction tx = null;
		StatelessSession session = null;
		try {
			Integer rowsInserted = insertFormInstance(formId, locationId);
			
			if (rowsInserted > 0) {
				FormService formService = Context.getFormService();
				Form form = formService.getForm(formId);
				String formName = form.getName();
				session = this.sessionFactory.openStatelessSession();
				tx = session.beginTransaction();
				Connection con = session.connection();
				String sql = "select max(form_instance_id) as form_instance_id,"
				        + "? as form_id,location_id from atd_form_instance where location_id=? "
				        + "and form_id in (select form_id from form where name=?) " + "group by location_id";
			

				stmt = con.prepareStatement(sql);
				stmt.setInt(1, formId);
				stmt.setInt(2, locationId);
				stmt.setString(3, formName);
				
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					Integer formInstanceId = rs.getInt(1);
					formId = rs.getInt(2);
					locationId = rs.getInt(3);
					FormInstance formInstance = new FormInstance(locationId,formId,formInstanceId);
					return formInstance;
				}
			}
			
		}
		catch (Exception e) {
			log.error(Util.getStackTrace(e));
		}finally{
			try {
                if(stmt != null){
                	stmt.close();
                }
                if(tx != null){
                	tx.commit();
                }
                if(session != null){
                	session.close();
                }
            }
            catch (Exception e) {
                log.error("Error generated", e);
            }
		}
		return null;
	}

	public PatientATD addPatientATD(PatientATD patientATD) {
		StatelessSession sess = this.sessionFactory.openStatelessSession();
		Transaction tx = null;
		try {
			tx = sess.beginTransaction();
			
			sess.insert(patientATD);
			
			tx.commit();
		}
		catch (Exception e) {
			if (tx != null)
				tx.rollback();
			log.error("", e);
		}
		finally {
			if(sess != null){
				sess.close();
			}
		}
		return patientATD;
	}

	public PatientATD getPatientATD(FormInstance formInstance, int fieldId)
	{
		try
		{
			DssService dssService = Context.getService(DssService.class);
			String sql = "select * from atd_patient_atd_element "
					+ "where form_instance_id=? and form_id=? and location_id=? and field_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0,formInstance.getFormInstanceId());
			qry.setInteger(1, formInstance.getFormId());
			qry.setInteger(2, formInstance.getLocationId());
			qry.setInteger(3, fieldId);
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

	private FormAttribute getFormAttributeByName(String formAttributeName)
	{
		try
		{
			String sql = "select * from atd_form_attribute where name=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, formAttributeName);
			qry.addEntity(FormAttribute.class);

			List<FormAttribute> list = qry.list();

			if (list != null && list.size() > 0)
			{
				return list.get(0);
			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public State getStateByName(String stateName)
	{
		try
		{
			String sql = "select * from atd_state where name=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, stateName);
			qry.addEntity(State.class);

			List<State> list = qry.list();

			if (list != null && list.size() > 0)
			{
				return list.get(0);
			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public FormAttributeValue getFormAttributeValue(Integer formId,
			String formAttributeName, Integer locationTagId, Integer locationId)
	{
		try
		{
			FormAttribute formAttribute = this
					.getFormAttributeByName(formAttributeName);

			if (formAttribute != null)
			{
				Integer formAttributeId = formAttribute.getFormAttributeId();

				String sql = "select * from atd_form_attribute_value where form_id=? "+
							"and form_attribute_id=? and location_tag_id=? and location_id=?";
				SQLQuery qry = this.sessionFactory.getCurrentSession()
						.createSQLQuery(sql);

				qry.setInteger(0, formId);
				qry.setInteger(1, formAttributeId);
				qry.setInteger(2, locationTagId);
				qry.setInteger(3, locationId);
				qry.addEntity(FormAttributeValue.class);

				List<FormAttributeValue> list = qry.list();

				if (list != null && list.size() > 0)
				{
					return list.get(0);
				}

			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public Session getSession(int sessionId)
	{
		try
		{
			String sql = "select * from atd_session where session_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, sessionId);
			qry.addEntity(Session.class);
			return (Session) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public List<PatientState> getPatientStatesWithForm(int sessionId)
	{
		try
		{
			String sql = "select * from atd_patient_state where session_id=? and form_id is not null and retired=? order by start_time desc,end_time desc";
			SQLQuery qry = null;

			qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
			qry.setInteger(0, sessionId);
			qry.setBoolean(1,false);
			qry.addEntity(PatientState.class);
			return qry.list();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public PatientState getPrevPatientStateByAction(
			int sessionId, int patientStateId,String action)
	{
		try
		{
			String sql = "select * from atd_patient_state where session_id=? and patient_state_id < ? and retired=?"
					+ " order by patient_state_id desc";
			SQLQuery qry = null;
			qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
			qry.setInteger(0, sessionId);
			qry.setInteger(1, patientStateId);
			qry.setBoolean(2, false);
			qry.addEntity(PatientState.class);
			List<PatientState> patientStates = qry.list();
			StateAction stateAction = null;
			
			for(PatientState patientState:patientStates){
				stateAction = patientState.getState().getAction();
				if(stateAction != null){
					if(stateAction.getActionName().equalsIgnoreCase(action)){
						return patientState;
					}
				}
			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}

		return null;
	}

	public StateMapping getStateMapping(State initialState, Program program)
	{
		try
		{
			String sql = "select * from atd_state_mapping where initial_state=? and program_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, initialState.getStateId());
			qry.setInteger(1, program.getProgramId());
			qry.addEntity(StateMapping.class);
			return (StateMapping) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public Program getProgramByNameVersion(String name, String version)
	{
		try
		{
			String sql = "select * from atd_program where name=? and version=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, name);
			qry.setString(1, version);
			qry.addEntity(Program.class);
			return (Program) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public Program getProgram(Integer programId)
	{
		try
		{
			String sql = "select * from atd_program where program_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, programId);
			qry.addEntity(Program.class);
			return (Program) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public Session addSession(Session session)
	{
		try
		{
			this.sessionFactory.getCurrentSession().save(session);
			return session;
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public Session updateSession(Session session)
	{
		try
		{
			this.sessionFactory.getCurrentSession().save(session);
			return session;
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public PatientState addUpdatePatientState(PatientState patientState) {
		StatelessSession sess = this.sessionFactory.openStatelessSession();
		Transaction tx = null;
		try {
			tx = sess.beginTransaction();

			//if there is no patient state id, that means this
			//is a new state that needs to be inserted
			//otherwise update
			if(patientState.getPatientStateId() == null){
				sess.insert(patientState);
			}else{
				sess.update(patientState);
			}
			
			tx.commit();
		}
		catch (Exception e) {
			if (tx != null)
				tx.rollback();
			log.error("", e);
		}
		finally {
			if(sess != null){
				sess.close();
			}
		}
		
		return patientState;
	}

	public StateAction getStateActionByName(String action)
	{
		try
		{
			String sql = "select * from atd_state_action where action_name=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, action);
			qry.addEntity(StateAction.class);
			return (StateAction) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<PatientState> getPatientStateByEncounterState(Integer encounterId, Integer stateId) {
		StatelessSession sess = this.sessionFactory.openStatelessSession();
		Transaction tx = null;
		try {
			tx = sess.beginTransaction();
			String sql = "select * from atd_patient_state where session_id in "
			        + "(select session_id from atd_session where encounter_id=?) and "
			        + "state=? order by start_time desc, end_time desc";
			SQLQuery qry = sess.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.setInteger(1, stateId);
			qry.addEntity(PatientState.class);
			
			return qry.list();
			
		}
		catch (Exception e) {
			if (tx != null)
				tx.rollback();
			log.error("", e);
		}
		finally {
			if (sess != null) {
				sess.close();
			}
		}
		return null;
	}

	public List<PatientState> getPatientStateBySessionState(Integer sessionId,
			Integer stateId)
	{

		try
		{
			String sql = "select * from atd_patient_state where session_id=? and state=? and retired=? order by start_time desc, end_time desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, sessionId);
			qry.setInteger(1, stateId);
			qry.setBoolean(2, false);
			qry.addEntity(PatientState.class);

			return qry.list();

		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<PatientState> getPatientStatesBySession(Integer sessionId,boolean isRetired)
	{

	try
	{
		String sql = "select * from atd_patient_state where session_id=? and retired=? order by start_time desc, end_time desc";
		SQLQuery qry = this.sessionFactory.getCurrentSession()
				.createSQLQuery(sql);
		qry.setInteger(0, sessionId);
		qry.setBoolean(1, isRetired);
		qry.addEntity(PatientState.class);

		return qry.list();

	} catch (Exception e)
	{
		log.error(Util.getStackTrace(e));
	}
	return null;
}

	
	public PatientState getPatientStateByEncounterFormAction(Integer encounterId,
			Integer formId, String action)
	{

		try
		{
			// limit to states for the session that match the form id
			String sql = "select * from atd_patient_state where session_id in "+
						"(select session_id from atd_session where encounter_id=?) "+
						"and form_id=? and retired=? order by start_time desc, end_time desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.setInteger(1, formId);
			qry.setBoolean(2, false);
			qry.addEntity(PatientState.class);

			List<PatientState> states = qry.list();

			// return the most recent state with the given action
			for (PatientState state : states)
			{
				StateAction stateAction = state.getState().getAction();
				if (stateAction != null
						&& stateAction.getActionName().equalsIgnoreCase(action))
				{
					return state;
				}
			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<PatientState> getPatientStatesByFormInstance(FormInstance formInstance, boolean isRetired) {
		
		try {
			// limit to states for the session that match the form id
			String sql = "select * from atd_patient_state where form_instance_id=? "
			        + "and form_id=? and location_id=? and retired=? " 
			        + "order by start_time desc, end_time desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
			qry.setInteger(0, formInstance.getFormInstanceId());
			qry.setInteger(1, formInstance.getFormId());
			qry.setInteger(2, formInstance.getLocationId());
			qry.setBoolean(3, isRetired);
			qry.addEntity(PatientState.class);
			
			return qry.list();
		}
		catch (Exception e) {
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public PatientState getPatientStateByFormInstanceAction(FormInstance formInstance,
			 String action)
	{

		try
		{
			// limit to states for the session that match the form id
			List<PatientState> states = getPatientStatesByFormInstance(formInstance,false);

			// return the most recent state with the given action
			for (PatientState state : states)
			{
				StateAction stateAction = state.getState().getAction();
				if (stateAction != null
						&& stateAction.getActionName().equalsIgnoreCase(action))
				{
					return state;
				}
			}
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<PatientState> getPatientStateByFormInstanceState(FormInstance formInstance, State state) {
		StatelessSession sess = this.sessionFactory.openStatelessSession();
		Transaction tx = null;
		try {
			tx = sess.beginTransaction();
			
			Integer stateId = state.getStateId();
			// limit to states for the session that match the form id
			String sql = "select * from atd_patient_state where form_instance_id=? "
			        + "and form_id=? and location_id=? and retired=? and state=? "
			        + "order by start_time desc, end_time desc";
			SQLQuery qry = sess.createSQLQuery(sql);
			qry.setInteger(0, formInstance.getFormInstanceId());
			qry.setInteger(1, formInstance.getFormId());
			qry.setInteger(2, formInstance.getLocationId());
			qry.setBoolean(3, false);
			qry.setInteger(4, stateId);
			qry.addEntity(PatientState.class);
			
			return qry.list();
		}
		catch (Exception e) {
			if (tx != null)
				tx.rollback();
			log.error("", e);
		}
		finally {
			if(sess != null){
				sess.close();
			}
		}
		return null;
	}

	public List<PatientState> getUnfinishedPatientStateByStateName(
			String stateName, Date optionalDateRestriction, 
			Integer locationTagId, Integer locationId)
	{
		try
		{
			State state = this.getStateByName(stateName);
			String dateRestriction = "";
			if (optionalDateRestriction != null)
			{
				dateRestriction = " and start_time >= ?";
			}
			String sql = "select * from atd_patient_state where state in "
					+ "(?) and end_time is null and retired=? and location_tag_id=? " 
					+ " and location_id=? "+dateRestriction
					+ " order by start_time desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, state.getStateId());
			qry.setBoolean(1,false);
			qry.setInteger(2,locationTagId);
			qry.setInteger(3, locationId);
			qry.addEntity(PatientState.class);
			if (optionalDateRestriction != null)
			{
				qry.setDate(3, optionalDateRestriction);
			}
			return qry.list();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<PatientState> getUnfinishedPatientStateByStateSession(
		String stateName,Integer sessionId)
{
	try
	{
		State state = this.getStateByName(stateName);
		
		String sql = "select * from atd_patient_state where state in "
				+ "(?) and end_time is null and retired=? and session_id=? "
				+ " order by start_time desc";
		SQLQuery qry = this.sessionFactory.getCurrentSession()
				.createSQLQuery(sql);
		qry.setInteger(0, state.getStateId());
		qry.setBoolean(1,false);
		qry.setInteger(2,sessionId);
		qry.addEntity(PatientState.class);

		return qry.list();
	} catch (Exception e)
	{
		log.error(Util.getStackTrace(e));
	}
	return null;
}

	public List<PatientState> getUnfinishedPatientStatesAllPatients(Date optionalDateRestriction, 
			Integer locationTagId, Integer locationId)
	{
		try
		{
			String dateRestriction = "";
			if (optionalDateRestriction != null)
			{
				dateRestriction = " and start_time >= ?";
			}
			String sql = "select * from atd_patient_state where end_time is null "+
					"and retired=? and location_tag_id=? and location_id=?"
					+ dateRestriction + " order by start_time desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.addEntity(PatientState.class);
			qry.setBoolean(0, false);
			qry.setInteger(1, locationTagId);
			qry.setInteger(2,locationId);
			if (optionalDateRestriction != null)
			{
				qry.setDate(3, optionalDateRestriction);
			}
			return qry.list();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public PatientState getLastUnfinishedPatientState(Integer sessionId)
	{
		try
		{
			String sql = "select * from atd_patient_state where session_id=? "
					+ " and end_time is null and retired=? order by start_time desc, patient_state_id desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, sessionId);
			qry.setBoolean(1, false);
			qry.addEntity(PatientState.class);
			List<PatientState> states = qry.list();
			if (states != null && states.size() > 0)
			{
				return states.get(0);
			}
			return null;
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public PatientState getLastPatientState(Integer sessionId)
	{
		try
		{
			String sql = "select * from atd_patient_state where session_id=? "
					+ " and retired=? order by start_time desc,end_time desc, patient_state_id desc";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, sessionId);
			qry.setBoolean(1, false);
			qry.addEntity(PatientState.class);
			List<PatientState> states = qry.list();
			if (states != null && states.size() > 0)
			{
				return states.get(0);
			}
			return null;
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public State getState(Integer stateId){
		try
		{
			String sql = "select * from atd_state where state_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, stateId);
			qry.addEntity(State.class);
			
			return (State) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	private List<String> getListMappedStates(Integer programId,String startStateName){
		
		List<String> orderedStateNames = new ArrayList<String>();
		String sql = "Select * from atd_state_mapping where program_id=?";
		SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
		qry.addEntity(StateMapping.class);
		qry.setInteger(0, programId);
		List<StateMapping> mappings = qry.list();
		
		HashMap<String,StateMapping> stateMap = new HashMap<String,StateMapping>();
		
		for(StateMapping mapping:mappings){
			stateMap.put(mapping.getInitialState().getName(), mapping);
		}
		
		StateMapping mapping = null;
		
		if(startStateName != null){
			orderedStateNames.add(startStateName);
			mapping = stateMap.get(startStateName);
			
			while(mapping != null){
				startStateName = mapping.getNextState().getName();
				orderedStateNames.add(startStateName);
				mapping = stateMap.get(startStateName);
			}
		}
		return orderedStateNames;
	}
	
	/**
	 * Please DONT pass the program object to this method.
	 * I received lazy initialization exceptions from the
	 * greaseboard when I did this. tmdugan
	 * @param optionalDateRestriction
	 * @param programId
	 * @param startStateName
	 * @param locationTagId
	 * @return
	 */
	public List<PatientState> getLastPatientStateAllPatients(
			Date optionalDateRestriction,Integer programId,String startStateName, 
			Integer locationTagId, Integer locationId)
	{
		try
		{
			ATDService atdService = Context.getService(ATDService.class);
			List<PatientState> patientStates = new ArrayList<PatientState>();
			String dateRestriction = "";
			if (optionalDateRestriction != null)
			{
				dateRestriction = " and start_time >= ?";
			} 
			
			String sql = "select aps.* from atd_patient_state aps, atd_session atds, encounter e" +
			" where aps.session_id = atds.session_id and atds.encounter_id = e.encounter_id " +
			" and retired=? and location_tag_id=? and aps.location_id=? "+dateRestriction+" order by e.encounter_datetime desc,start_time desc";
			
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);

			qry.setBoolean(0, false);
			qry.setInteger(1, locationTagId);
			qry.setInteger(2, locationId);
			if (optionalDateRestriction != null)
			{
				qry.setDate(3, optionalDateRestriction);
			}
			qry.addEntity(PatientState.class);
			List<PatientState> states = qry.list();
			LinkedHashMap<Integer,LinkedHashMap<String,PatientState>> patientStateMap = 
				new LinkedHashMap<Integer,LinkedHashMap<String,PatientState>>();
			LinkedHashMap<String,PatientState> stateNameMap = null;
			for(PatientState patientState:states){
				Integer sessionId = patientState.getSessionId();
				Integer encounterId = atdService.getSession(sessionId).getEncounterId();
				stateNameMap = patientStateMap.get(encounterId);
				if(stateNameMap == null){
					stateNameMap = new LinkedHashMap<String,PatientState>();
				}
				if(stateNameMap.get(patientState.getState().getName())==null){
					stateNameMap.put(patientState.getState().getName(), patientState);
				}
				patientStateMap.put(encounterId,stateNameMap);
			}
			
			List<String> mappedStateNames = getListMappedStates(programId,startStateName);
			
			//look at the state chain in reverse order
			//find the latest unfinished state in the chain for the given patient
			for (Integer encounterId : patientStateMap.keySet())
			{
				stateNameMap = patientStateMap.get(encounterId);
				for (int i = mappedStateNames.size() - 1; i >= 0; i--)
				{
					String currStateName = mappedStateNames.get(i);
					PatientState currPatientState = stateNameMap.get(currStateName);
					if(currPatientState != null){
						patientStates.add(currPatientState);
						break;
					}
				}
			}
			return patientStates;
			
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public ArrayList<String> getExportDirectories()
	{
		try
		{
			String sql = "select distinct value from atd_form_attribute_value where form_attribute_id in "
					+ "(select form_attribute_id from atd_form_attribute where name='defaultExportDirectory')";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.addScalar("value");
			List<String> list = qry.list();

			ArrayList<String> exportDirectories = new ArrayList<String>();
			for (String currResult : list)
			{
				exportDirectories.add(currResult);
			}

			return exportDirectories;
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}

	public List<State> getStatesByActionName(String actionName)
	{
		try
		{
			String sql = "select * from atd_state where state_action_id="
					+ "(select state_action_id from atd_state_action where action_name=?)";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, actionName);
			qry.addEntity(State.class);
			return qry.list();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public PatientState getPatientState(Integer patientStateId){
		try
		{
			String sql = "select * from atd_patient_state where patient_state_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, patientStateId);
			qry.addEntity(PatientState.class);
			return (PatientState) qry.uniqueResult();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	public void updatePatientStates(Date thresholdDate){
	try
	{
		//retire all unretired states before threshold date
		String sql = "update atd_patient_state " +
		"set retired=?,date_retired=NOW() " + 
		"where start_time < ? and retired=?";		// To speed process of atd initialization we should retire any state (complete or incomplete)
		SQLQuery qry = this.sessionFactory.getCurrentSession()
		.createSQLQuery(sql);

		qry.setBoolean(0,true);
		qry.setDate(1, thresholdDate);
		qry.setBoolean(2,false);
		qry.executeUpdate();
		
		//retire all the other states for the encounters of the retired states
		sql = "update atd_patient_state a, (select session_id from atd_session "+
		"where encounter_id in (select encounter_id from atd_session where session_id "+
		"in (select session_id from atd_patient_state where retired=?)))b "+
		"set a.retired=?,date_retired=NOW() where a.session_id=b.session_id and retired=?";
		
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
	
	public List<PatientState> getAllRetiredPatientStatesWithForm(Date thresholdDate)
	{
		try
		{
			String sql = "select * from atd_patient_state where form_id is not null and form_instance_id is not null "+
						"and retired=? and start_time < ?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);

			qry.setBoolean(0, true);
			qry.setDate(1, thresholdDate);
			qry.addEntity(PatientState.class);
			return qry.list();
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<Session> getSessionsByEncounter(int encounterId)
	{
		try
		{
			String sql = "select * from atd_session where encounter_id=? ";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, encounterId);
			qry.addEntity(Session.class);
			return (List<Session>) qry.list();
			
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public Program getProgram(Integer locationTagId,Integer locationId)
	{
		try
		{
			String sql = "select * from atd_program_tag_map where location_id=? and location_tag_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setInteger(0, locationId);
			qry.setInteger(1,locationTagId);
			qry.addEntity(ProgramTagMap.class);
			ProgramTagMap map = (ProgramTagMap) qry.uniqueResult();
			
			if(map != null){
				return map.getProgram();
			}
			
		} catch (Exception e)
		{
			log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<PatientState> getPatientStatesWithFormInstances(String formName, Integer encounterId){
		
		String formRestriction = null;
		if (formName != null)
		{
			formRestriction = " and form_id in (select form_id from form where name=? )";
		} 
		String sql = "select atdps.* from atd_patient_state atdps " 
			+ " where session_id in (select session_id  from atd_session where encounter_id=? ) "
			+ formRestriction + " and form_instance_id is not null order by end_time desc" ;
		SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
		qry.setInteger(0, encounterId);
		qry.setString(1, formName);
		qry.addEntity(PatientState.class);
		return qry.list();
	}

	public void saveError(ATDError error)
	{
		try
		{
			this.sessionFactory.getCurrentSession().save(error);
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
	}
	
	public List<ATDError> getATDErrorsByLevel(String errorLevel,
			Integer sessionId)
	{
		try
		{
			String sql = "select * from atd_error where level=? and session_id=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, errorLevel);
			qry.setInteger(1, sessionId);
			qry.addEntity(ATDError.class);
			return qry.list();
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public List<FormAttributeValue> getFormAttributeValuesByValue(String value)
	{
		try
		{
			String sql = "select * from atd_form_attribute_value where value=?";
			SQLQuery qry = this.sessionFactory.getCurrentSession()
					.createSQLQuery(sql);
			qry.setString(0, value);
			qry.addEntity(FormAttributeValue.class);
			return qry.list();
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}
	
	public Integer getErrorCategoryIdByName(String name)
	{
		try
		{
			Connection con = this.sessionFactory.getCurrentSession()
					.connection();
			String sql = "select error_category_id from atd_error_category where name=?";
			try
			{
				PreparedStatement stmt = con.prepareStatement(sql);
				stmt.setString(1, name);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
				{
					return rs.getInt(1);
				}

			} catch (Exception e)
			{

			}
			return null;
		} catch (Exception e)
		{
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}
}
