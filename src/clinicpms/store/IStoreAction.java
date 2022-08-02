/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.TheAppointment;
import clinicpms.model.EntityStoreType;
import clinicpms.model.PatientNotification;
import clinicpms.model.ThePatient;
import clinicpms.model.SurgeryDaysAssignment;
import java.util.List;

/**
 *
 * @author colin
 */
public interface IStoreAction {
    public int countRowsIn(ThePatient table)throws StoreException;
    public int countRowsIn(SurgeryDaysAssignment table) throws StoreException;   
    public Integer count(TheAppointment.Collection collection, Integer appointeeKey)throws StoreException;
    public Integer count(ThePatient.Collection collection)throws StoreException;
    public Integer count(PatientNotification.Collection collection)throws StoreException;
    public Integer count(SurgeryDaysAssignment surgeryDaysAssignment)throws StoreException;
    
    public void create(TheAppointment a) throws StoreException;
    public void create(PatientNotification pn) throws StoreException;
    public void create(ThePatient p )throws StoreException;
    public void create(SurgeryDaysAssignment s)throws StoreException;
    
    public void delete(TheAppointment a, Integer key) throws StoreException;
    public void delete(ThePatient p) throws StoreException;
    
    public void drop(TheAppointment a)throws StoreException;
    public void drop(ThePatient p)throws StoreException;
    public void drop(SurgeryDaysAssignment s)throws StoreException;
    
    public List<String[]> importEntityFromCSV(EntityStoreType entity) throws StoreException;
    
    public Integer insert(TheAppointment a, Integer appointeeKey) throws StoreException; 
    public Integer insert(ThePatient p, Integer key) throws StoreException;
    //public Integer insert(ThePatient p) throws StoreException;
    public Integer insert(PatientNotification pn) throws StoreException;
    //public void insert(SurgeryDaysAssignmentx p) throws StoreException;
    public void insert(SurgeryDaysAssignment p) throws StoreException;
    
    public void populate(SurgeryDaysAssignment data)throws StoreException;
    
    public TheAppointment read(TheAppointment a, Integer key)throws StoreException ;
    public TheAppointment read(TheAppointment.Collection a, Integer key)throws StoreException ;
    public ThePatient read(ThePatient p, Integer key) throws StoreException;
    public ThePatient.Collection read(ThePatient.Collection value)throws StoreException;
    public PatientNotification read(PatientNotification value, Integer key)throws StoreException;
    public PatientNotification.Collection read(PatientNotification.Collection value, Integer key)throws StoreException;
    //public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentx value) throws StoreException;
    public SurgeryDaysAssignment read(SurgeryDaysAssignment value) throws StoreException;

    
    public String read(Store.SelectedTargetStore db)throws StoreException;

    
    //public void update(Appointment a) throws StoreException;
    public void update(TheAppointment a, Integer key, Integer appointeeKee) throws StoreException;
    //public void update(SurgeryDaysAssignmentx value) throws StoreException;
    public void update(SurgeryDaysAssignment value) throws StoreException;
    //public void update(Patient p) throws StoreException;
    public void update(ThePatient p, Integer key, Integer guardianKey) throws StoreException;
    public void update(PatientNotification pn, Integer key, Integer patientKey)throws StoreException;
    public void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException;

}
