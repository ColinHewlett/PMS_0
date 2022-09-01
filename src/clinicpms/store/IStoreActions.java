/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Entity;
import clinicpms.model.PatientNotification;
import clinicpms.model.Patient;
import clinicpms.model.StoreManager;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.model.IStoreClient;
import java.io.File;
import java.util.List;

/**
 *
 * @author colin
 */
public interface IStoreActions {  
    public Integer count(Appointment appointment, Integer appointeeKey)throws StoreException;
    public Integer count(Patient patient)throws StoreException;
    public Integer count(PatientNotification patientNotification)throws StoreException;
    public Integer count(SurgeryDaysAssignment surgeryDaysAssignment)throws StoreException;
    
    public void create(Appointment a) throws StoreException;
    public void create(PatientNotification pn) throws StoreException;
    public void create(Patient p )throws StoreException;
    public void create(SurgeryDaysAssignment s)throws StoreException;
    
    public void delete(Appointment a, Integer key) throws StoreException;
    public void delete(Patient p) throws StoreException;
    
    public void drop(Appointment a)throws StoreException;
    public void drop(Patient p)throws StoreException;
    public void drop(SurgeryDaysAssignment s)throws StoreException;
    
    public List<String[]> importEntityFromCSV(Entity entity) throws StoreException;
    
    public Integer insert(Appointment a, Integer appointeeKey) throws StoreException; 
    public Integer insert(Patient p, Integer key) throws StoreException;
    public Integer insert(PatientNotification pn) throws StoreException;
    public void insert(SurgeryDaysAssignment p) throws StoreException;
    
    public void populate(SurgeryDaysAssignment data)throws StoreException;
    
    public Appointment read(Appointment a, Integer key)throws StoreException ;
    public Patient read(Patient p, Integer key) throws StoreException;
    public PatientNotification read(PatientNotification value, Integer key)throws StoreException;
    public SurgeryDaysAssignment read(SurgeryDaysAssignment value) throws StoreException;

    public void update(Appointment a, Integer key, Integer appointeeKee) throws StoreException;
    public void update(SurgeryDaysAssignment value) throws StoreException;
    public void update(Patient p, Integer key, Integer guardianKey) throws StoreException;
    public void update(PatientNotification pn, Integer key, Integer patientKey)throws StoreException;

    /**
     * store location management operations
     */
    public IStoreClient read(StoreManager client)throws StoreException;
    public void update(StoreManager manager) throws StoreException;
    public File initialiseTargetStore(File path)throws StoreException;
}
