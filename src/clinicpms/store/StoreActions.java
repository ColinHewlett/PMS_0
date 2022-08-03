/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.EntityStoreType;
import clinicpms.model.Patient;
import clinicpms.model.PatientNotification;
import clinicpms.model.SurgeryDaysAssignment;
import java.util.List;

/**
 *
 * @author colin
 */
public abstract class StoreActions {
    public abstract Integer count(Appointment.Collection collection, Integer appointeeKey)throws StoreException;
    public abstract Integer count(Patient.Collection collection)throws StoreException;
    public abstract Integer count(PatientNotification.Collection collection)throws StoreException;
    public abstract Integer count(SurgeryDaysAssignment surgeryDaysAssignment)throws StoreException;
    
    public abstract void create(Appointment a) throws StoreException;
    public abstract void create(PatientNotification pn) throws StoreException;
    public abstract void create(Patient p )throws StoreException;
    public abstract void create(SurgeryDaysAssignment s)throws StoreException;
    
    public abstract void delete(Appointment a, Integer key) throws StoreException;
    public abstract void delete(Patient p) throws StoreException;
    
    public abstract void drop(Appointment a)throws StoreException;
    public abstract void drop(Patient p)throws StoreException;
    public abstract void drop(SurgeryDaysAssignment s)throws StoreException;
    
    public abstract List<String[]> importEntityFromCSV(EntityStoreType entity) throws StoreException;
    
    public abstract Integer insert(Appointment a, Integer appointeeKey) throws StoreException; 
    public abstract Integer insert(Patient p, Integer key) throws StoreException;
    public abstract Integer insert(PatientNotification pn) throws StoreException;
    public abstract void insert(SurgeryDaysAssignment p) throws StoreException;
    
    public abstract void populate(SurgeryDaysAssignment data)throws StoreException;
    
    public abstract Appointment read(Appointment a, Integer key)throws StoreException ;
    public abstract Appointment read(Appointment.Collection a, Integer key)throws StoreException ;
    public abstract Patient read(Patient p, Integer key) throws StoreException;
    public abstract Patient.Collection read(Patient.Collection value)throws StoreException;
    public abstract PatientNotification read(PatientNotification value, Integer key)throws StoreException;
    public abstract PatientNotification.Collection read(PatientNotification.Collection value, Integer key)throws StoreException;
    public abstract SurgeryDaysAssignment read(SurgeryDaysAssignment value) throws StoreException;

    
    //public abstract String read(Store.SelectedTargetStore db)throws StoreException;

    
    public abstract void update(Appointment a, Integer key, Integer appointeeKee) throws StoreException;
    public abstract void update(SurgeryDaysAssignment value) throws StoreException;
    public abstract void update(Patient p, Integer key, Integer guardianKey) throws StoreException;
    public abstract void update(PatientNotification pn, Integer key, Integer patientKey)throws StoreException;
    public abstract void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException;
}
