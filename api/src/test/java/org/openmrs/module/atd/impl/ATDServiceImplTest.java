package org.openmrs.module.atd.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.dss.DssElement;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import junit.framework.Assert;

public class ATDServiceImplTest extends BaseModuleContextSensitiveTest {
    
    private static final String DATA_SET_PATIENT_STATES = "dbunitFiles/patientStates.xml";
    
    @Before
    public void runBeforeEachTest() throws Exception {
        initializeInMemoryDatabase();
        executeDataSet(DATA_SET_PATIENT_STATES);
        authenticate();
    }
    
    /**
     * @see ATDServiceImpl#updatePatientATD(PatientATD)
     * @verifies add new patient ATD
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
        PatientATD patientATD = atdService.addPatientATD(patientId, formInstance, dssElement, encounterId);
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
     * @see ATDServiceImpl#getPatientATD(FormInstance,int)
     * @verifies get PatientATD
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
        PatientATD patientATD = atdService.addPatientATD(patientId, formInstance, dssElement, encounterId);
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
     * @see ATDServiceImpl#getPatientATD(FormInstance,int)
     * @verifies get PatientATD null
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
     * @see ATDServiceImpl#updatePatientATD(PatientATD)
     * @verifies update Patient ATD
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
        PatientATD patientATD = atdService.addPatientATD(patientId, formInstance, dssElement, encounterId);
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
     * @see ATDServiceImpl#getPatientATDs(FormInstance,List)
     * @verifies get patient ATDs by form instance and field ids
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
        PatientATD patientATD = atdService.addPatientATD(patientId, formInstance, dssElement, encounterId);
        patientATD.setFieldId(fieldId1);
        atdService.updatePatientATD(patientATD);
        map.put(fieldId1, patientATD);
        
        patientATD = atdService.addPatientATD(patientId, formInstance, dssElement, encounterId);
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
     * @see ATDServiceImpl#getPatientATDs(FormInstance,List)
     * @verifies get patient ATDs by form instance and field ids null
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
    
}
