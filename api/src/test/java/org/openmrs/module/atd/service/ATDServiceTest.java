package org.openmrs.module.atd.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.dss.DssElement;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

public class ATDServiceTest extends BaseModuleContextSensitiveTest {
    
    private static final String DATA_SET_PATIENT_STATES = "dbunitFiles/patientStates.xml";
    
    @BeforeEach
    public void runBeforeEachTest() throws Exception {
        initializeInMemoryDatabase();
        executeDataSet(DATA_SET_PATIENT_STATES);
        authenticate();
    }
    
    /**
     * Test to make sure that all service methods have the Authorized annotation
     * @throws Exception
     */
    @Test
    @SkipBaseSetup
    public void checkAuthorizationAnnotations() throws Exception {
        Method[] allMethods = ATDService.class.getDeclaredMethods();
        for (Method method : allMethods) {
            if (Modifier.isPublic(method.getModifiers())) {
                Authorized authorized = method.getAnnotation(Authorized.class);
                assertNotNull(authorized, "Authorized annotation not found on method " + method.getName());
            }
        }
    }
    
    /**
     * @see ATDService#addPatientATD(int,FormInstance,DssElement,Integer)
     */
    @Test
    public void addPatientATD_shouldAddNewPatientATD() throws Exception {
        Integer patientId = 1;
        FormInstance formInstance = new FormInstance();
        formInstance.setFormId(1);
        formInstance.setFormInstanceId(1);
        formInstance.setLocationId(1);
        Integer ruleId = 1;
        Result result = new Result();
        String text = "prompt text";
        result.setValueText(text);
        Integer encounterId = 1;
        DssElement dssElement = new DssElement(result, ruleId);
        
        ATDService atdService = Context.getService(ATDService.class);
        PatientATD patientATD = atdService.addPatientATD(formInstance, dssElement, encounterId, patientId);
        PatientATD expectedPatientATD = new PatientATD();
        expectedPatientATD.setPatientId(patientId);
        expectedPatientATD.setFormInstance(formInstance);
        expectedPatientATD.setEncounterId(encounterId);
        
        assertEquals(expectedPatientATD.getPatientId(), patientATD.getPatientId());
        assertEquals(expectedPatientATD.getFormInstance(), patientATD.getFormInstance());
        assertEquals(expectedPatientATD.getEncounterId(), patientATD.getEncounterId());
        assertEquals(ruleId, patientATD.getRule().getRuleId());
        assertEquals(text, patientATD.getText());
        assertNotNull(patientATD.getAtdId());
        assertNotNull(patientATD.getCreationTime());
    }
    
    /**
     * @see ATDService#createStatistics(Statistics)
     */
    @Test
    public void createStatistics_shouldCreateStatistics() throws Exception {
        Integer ruleId = 1;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        
        ATDService atdService = Context.getService(ATDService.class);
        statistics = atdService.createStatistics(statistics);
        
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
    }
    
    /**
     * @see ATDService#getPatientATD(FormInstance,int)
     */
    @Test
    public void getPatientATD_shouldGetPatientATD() throws Exception {
        Integer patientId = 1;
        FormInstance formInstance = new FormInstance();
        formInstance.setFormId(1);
        formInstance.setFormInstanceId(1);
        formInstance.setLocationId(1);
        Integer ruleId = 1;
        Result result = new Result();
        String text = "prompt text";
        result.setValueText(text);
        Integer encounterId = 1;
        Integer fieldId = 1;
        DssElement dssElement = new DssElement(result, ruleId);
        
        ATDService atdService = Context.getService(ATDService.class);
        PatientATD patientATD = atdService.addPatientATD(formInstance, dssElement, encounterId, patientId);
        patientATD.setFieldId(fieldId);
        atdService.updatePatientATD(patientATD);
        PatientATD expectedPatientATD = atdService.getPatientATD(formInstance, fieldId);
        
        assertEquals(expectedPatientATD.getPatientId(), patientATD.getPatientId());
        assertEquals(expectedPatientATD.getFormInstance(), patientATD.getFormInstance());
        assertEquals(expectedPatientATD.getEncounterId(), patientATD.getEncounterId());
        assertEquals(expectedPatientATD.getRule(), patientATD.getRule());
        assertEquals(expectedPatientATD.getText(), patientATD.getText());
        assertEquals(expectedPatientATD.getAtdId(), patientATD.getAtdId());
        assertEquals(expectedPatientATD.getCreationTime(), patientATD.getCreationTime());
        assertEquals(expectedPatientATD.getFieldId(), patientATD.getFieldId());
    }
    
    /**
     * @see ATDService#getPatientATD(FormInstance,int)
     */
    @Test
    public void getPatientATD_shouldGetPatientATDNull() throws Exception {
        FormInstance formInstance = new FormInstance();
        formInstance.setFormId(1);
        formInstance.setFormInstanceId(1);
        formInstance.setLocationId(1);
        Integer fieldId = 1;
        ATDService atdService = Context.getService(ATDService.class);
        
        PatientATD expectedPatientATD = atdService.getPatientATD(formInstance, fieldId);
        assertNull(expectedPatientATD);
    }
    
    /**
     * @see ATDService#getPatientATDs(FormInstance,List)
     */
    @Test
    public void getPatientATDs_shouldGetPatientATDsByFormInstanceAndFieldIds() throws Exception {
        Integer patientId = 1;
        FormInstance formInstance = new FormInstance();
        formInstance.setFormId(1);
        formInstance.setFormInstanceId(1);
        formInstance.setLocationId(1);
        Integer ruleId = 1;
        Result result = new Result();
        String text = "prompt text";
        result.setValueText(text);
        Integer encounterId = 1;
        Integer fieldId1 = 1;
        Integer fieldId2 = 2;
        DssElement dssElement = new DssElement(result, ruleId);
        HashMap<Integer, PatientATD> map = new HashMap<Integer, PatientATD>();
        
        ATDService atdService = Context.getService(ATDService.class);
        PatientATD patientATD = atdService.addPatientATD(formInstance, dssElement, encounterId, patientId);
        patientATD.setFieldId(fieldId1);
        atdService.updatePatientATD(patientATD);
        map.put(fieldId1, patientATD);
        
        patientATD = atdService.addPatientATD(formInstance, dssElement, encounterId, patientId);
        patientATD.setFieldId(fieldId2);
        atdService.updatePatientATD(patientATD);
        map.put(fieldId2, patientATD);
        
        ArrayList<Integer> fieldIds = new ArrayList<Integer>();
        fieldIds.add(fieldId1);
        fieldIds.add(fieldId2);
        List<PatientATD> expectedPatientATDs = atdService.getPatientATDs(formInstance, fieldIds);
        
        for (PatientATD expectedPatientATD : expectedPatientATDs) {
            patientATD = map.get(expectedPatientATD.getFieldId());
            assertEquals(expectedPatientATD.getPatientId(), patientATD.getPatientId());
            assertEquals(expectedPatientATD.getFormInstance(), patientATD.getFormInstance());
            assertEquals(expectedPatientATD.getEncounterId(), patientATD.getEncounterId());
            assertEquals(expectedPatientATD.getRule(), patientATD.getRule());
            assertEquals(expectedPatientATD.getText(), patientATD.getText());
            assertEquals(expectedPatientATD.getAtdId(), patientATD.getAtdId());
            assertEquals(expectedPatientATD.getCreationTime(), patientATD.getCreationTime());
            assertEquals(expectedPatientATD.getFieldId(), patientATD.getFieldId());
        }
    }
    
    /**
     * @see ATDService#getPatientATDs(FormInstance,List)
     */
    @Test
    public void getPatientATDs_shouldGetPatientATDsByFormInstanceAndFieldIdsNull() throws Exception {
        FormInstance formInstance = new FormInstance();
        formInstance.setFormId(1);
        formInstance.setFormInstanceId(1);
        formInstance.setLocationId(1);
        ArrayList<Integer> fieldIds = new ArrayList<Integer>();
        Integer fieldId1 = 1;
        Integer fieldId2 = 2;
        fieldIds.add(fieldId1);
        fieldIds.add(fieldId2);
        ATDService atdService = Context.getService(ATDService.class);
        
        List<PatientATD> expectedPatientATDs = atdService.getPatientATDs(formInstance, fieldIds);
        assertTrue(expectedPatientATDs.isEmpty());
    }
    
    /**
     * @see ATDService#getStatByFormInstance(int,String,Integer)
     */
    @Test
    public void getStatByFormInstance_shouldGetStatisticsByFormInstance() throws Exception {
        Integer ruleId = 1;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        
        ATDService atdService = Context.getService(ATDService.class);
        atdService.createStatistics(statistics);
        List<Statistics> statisticsList = atdService.getStatByFormInstance(formInstanceId, formName, locationId);
        assertEquals(1, statisticsList.size());
        
        statistics = statisticsList.get(0);
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
    }
    
    /**
     * @see ATDService#getStatByFormInstance(int,String,Integer)
     */
    @Test
    public void getStatByFormInstance_shouldGetStatisticsByFormInstanceNull() throws Exception {
        Integer formInstanceId = 1;
        
        String formName = "PSF";
        Integer locationId = 1;
        
        ATDService atdService = Context.getService(ATDService.class);
        List<Statistics> statisticsList = atdService.getStatByFormInstance(formInstanceId, formName, locationId);
        assertTrue(statisticsList.isEmpty());
    }
    
    /**
     * @see ATDService#getStatsByEncounterForm(Integer,String)
     */
    @Test
    public void getStatsByEncounterForm_shouldGetStatisticsByEncounterIdAndFormName() throws Exception {
        Integer ruleId = 1;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Integer obsvId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        statistics.setObsvId(obsvId); //obsvId must be non null to return results
        
        ATDService atdService = Context.getService(ATDService.class);
        atdService.createStatistics(statistics);
        List<Statistics> statisticsList = atdService.getStatsByEncounterForm(encounterId, formName);
        assertEquals(1, statisticsList.size());
        
        statistics = statisticsList.get(0);
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertEquals(obsvId, statistics.getObsvId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
    }
    
    /**
     * @see ATDService#getStatsByEncounterForm(Integer,String)
     */
    @Test
    public void getStatsByEncounterForm_shouldGetStatisticsByEncounterIdAndFormNameNull() throws Exception {
        Integer encounterId = 1;
        String formName = "PSF";
        
        ATDService atdService = Context.getService(ATDService.class);
        List<Statistics> statisticsList = atdService.getStatsByEncounterForm(encounterId, formName);
        assertTrue(statisticsList.isEmpty());
    }
    
    /**
     * @see ATDService#getStatsByEncounterFormNotPrioritized(Integer,String)
     */
    @Test
    public void getStatsByEncounterFormNotPrioritized_shouldGetStatisticsForNonprioritizedRulesByEncounterIdAndFormName()
            throws Exception {
        Integer ruleId = null;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Integer obsvId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        statistics.setObsvId(obsvId); //obsvId must be non null to return results
        
        ATDService atdService = Context.getService(ATDService.class);
        atdService.createStatistics(statistics);
        List<Statistics> statisticsList = atdService.getStatsByEncounterFormNotPrioritized(encounterId, formName);
        assertEquals(1, statisticsList.size());
        
        statistics = statisticsList.get(0);
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertEquals(obsvId, statistics.getObsvId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
    }
    
    /**
     * @see ATDService#getStatsByEncounterFormNotPrioritized(Integer,String)
     */
    @Test
    public void getStatsByEncounterFormNotPrioritized_shouldGetStatisticsForNonprioritizedRulesByEncounterIdAndFormNameNull()
            throws Exception {
        Integer ruleId = 1;
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        
        ATDService atdService = Context.getService(ATDService.class);
        atdService.createStatistics(statistics);
        List<Statistics> statisticsList = atdService.getStatsByEncounterFormNotPrioritized(encounterId, formName);
        assertTrue(statisticsList.isEmpty());
    }
    
    /**
     * @see ATDService#updatePatientATD(PatientATD)
     */
    @Test
    public void updatePatientATD_shouldUpdatePatientATD() throws Exception {
        Integer patientId = 1;
        FormInstance formInstance = new FormInstance();
        formInstance.setFormId(1);
        formInstance.setFormInstanceId(1);
        formInstance.setLocationId(1);
        Integer ruleId = 1;
        Result result = new Result();
        String text = "prompt text";
        result.setValueText(text);
        Integer encounterId = 1;
        Integer fieldId = 1;
        DssElement dssElement = new DssElement(result, ruleId);
        
        ATDService atdService = Context.getService(ATDService.class);
        PatientATD patientATD = atdService.addPatientATD(formInstance, dssElement, encounterId, patientId);
        assertEquals(patientId, patientATD.getPatientId());
        assertEquals(formInstance, patientATD.getFormInstance());
        assertEquals(encounterId, patientATD.getEncounterId());
        assertEquals(ruleId, patientATD.getRule().getRuleId());
        assertEquals(text, patientATD.getText());
        assertNotNull(patientATD.getAtdId());
        assertNotNull(patientATD.getCreationTime());
        assertEquals(null, patientATD.getFieldId());
        patientATD.setFieldId(fieldId);
        atdService.updatePatientATD(patientATD);
        assertEquals(patientId, patientATD.getPatientId());
        assertEquals(formInstance, patientATD.getFormInstance());
        assertEquals(encounterId, patientATD.getEncounterId());
        assertEquals(ruleId, patientATD.getRule().getRuleId());
        assertEquals(text, patientATD.getText());
        assertNotNull(patientATD.getAtdId());
        assertNotNull(patientATD.getCreationTime());
        assertEquals(fieldId, patientATD.getFieldId());
    }
    
    /**
     * @see ATDService#updateStatistics(Statistics)
     */
    @Test
    public void updateStatistics_shouldUpdateStatistics() throws Exception {
        Integer ruleId = 1;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        
        ATDService atdService = Context.getService(ATDService.class);
        statistics = atdService.createStatistics(statistics);
        
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, Calendar.JULY, 23);
        statistics.setPrintedTimestamp(calendar.getTime());
        
        atdService.updateStatistics(statistics);
        statistics = atdService.getStatByFormInstance(formInstanceId, formName, locationId).get(0);
        
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertNotNull(statistics.getStatisticsId());
        assertEquals(calendar.getTime().toString(), statistics.getPrintedTimestamp().toString());
    }
    
    /**
     * @see ATDService#getStatByIdAndRule(int,int,String,Integer)
     */
    @Test
    public void getStatByIdAndRule_shouldGetStatisticsByIdAndRule() throws Exception {
        Integer ruleId = 1;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        
        ATDService atdService = Context.getService(ATDService.class);
        atdService.createStatistics(statistics);
        List<Statistics> statisticsList = atdService.getStatByIdAndRule(formInstanceId, ruleId, formName, locationId);
        assertEquals(1, statisticsList.size());
        
        statistics = statisticsList.get(0);
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
    }
    
    /**
     * @see ATDService#getStatByIdAndRule(int,int,String,Integer)
     */
    @Test
    public void getStatByIdAndRule_shouldGetStatisticsByIdAndRuleNull() throws Exception {
        Integer formInstanceId = 1;
        Integer ruleId = 1;
        String formName = "PSF";
        Integer locationId = 1;
        
        ATDService atdService = Context.getService(ATDService.class);
        List<Statistics> statisticsList = atdService.getStatByIdAndRule(formInstanceId, ruleId, formName, locationId);
        assertTrue(statisticsList.isEmpty());
    }
    
    /**
     * @see ATDService#getStatsByEncounterRule(Integer,Integer)
     */
    @Test
    public void getStatsByEncounterRule_shouldGetStatisticsByEncounterIdAndRuleId() throws Exception {
        Integer ruleId = 1;
        
        Integer priority = 1;
        Integer formInstanceId = 1;
        Integer locationTagId = 1;
        Integer questionPosition = 1;
        String formName = "PSF";
        Integer locationId = 1;
        Integer encounterId = 1;
        Integer patientId = 1;
        Date birthDate = new Date();
        String age = Util.adjustAgeUnits(birthDate, null);
        
        Statistics statistics = new Statistics();
        statistics.setAgeAtVisit(age);
        statistics.setPriority(priority);
        statistics.setFormInstanceId(formInstanceId);
        statistics.setLocationTagId(locationTagId);
        statistics.setPosition(questionPosition);
        
        statistics.setRuleId(ruleId);
        statistics.setPatientId(patientId);
        statistics.setFormName(formName);
        statistics.setEncounterId(encounterId);
        statistics.setLocationId(locationId);
        
        ATDService atdService = Context.getService(ATDService.class);
        atdService.createStatistics(statistics);
        List<Statistics> statisticsList = atdService.getStatsByEncounterRule(encounterId, ruleId);
        assertEquals(1, statisticsList.size());
        
        statistics = statisticsList.get(0);
        assertEquals(age, statistics.getAgeAtVisit());
        assertEquals(encounterId, statistics.getEncounterId());
        assertEquals(formInstanceId, statistics.getFormInstanceId());
        assertEquals(formName, statistics.getFormName());
        assertEquals(locationId, statistics.getLocationId());
        assertEquals(locationTagId, statistics.getLocationTagId());
        assertEquals(patientId, statistics.getPatientId());
        assertEquals(questionPosition, statistics.getPosition());
        assertEquals(priority, statistics.getPriority());
        assertEquals(ruleId, statistics.getRuleId());
        assertNotNull(statistics.getStatisticsId());
        assertNull(statistics.getPrintedTimestamp());
    }
    
    /**
     * @see ATDService#getStatsByEncounterRule(Integer,Integer)
     */
    @Test
    public void getStatsByEncounterRule_shouldGetStatisticsByEncounterIdAndRuleIdNull() throws Exception {
        Integer encounterId = 1;
        Integer ruleId = 1;
        
        ATDService atdService = Context.getService(ATDService.class);
        List<Statistics> statisticsList = atdService.getStatsByEncounterRule(encounterId, ruleId);
        assertTrue(statisticsList.isEmpty());
    }
}
