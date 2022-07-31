/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.PatientNotification;

//import clinicpms.model.SurgeryDaysAssignmentx;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.model.TheAppointment;
import clinicpms.model.ThePatient;

/**
 * Defines simple interface for a CVS database to support the following breadth
 * of queries. 
 * A production system would use an RDBMS, never a collection CVS files.
 * -- all patients (returned if query = null and Patient arg has a null key)
 * -- patients due for recall in this month (query = "DentalRecallDate=dd/mm/yyyy")
 * -- all appointments (returned if query = null and Appointment arg has a null key)
 * -- appointments on this date (query = "Start=dd/mm/yyyy")
 * -- appointments scheduled for this patient (query = "Patient=dd/mm/yyyy)
 * -- Query syntax  can be primitive
 * -- Patient queries will be returned in name order
 * -- Appointment queries will be returned time ordered.
 * @author colin
 */
public interface IPMSStoreAction {
    public Integer count(TheAppointment.Collection entity)throws StoreException;
    //public void create(Appointment a) throws StoreException;
    //public void create(Patient p) throws StoreException;
    public void create(ThePatient p )throws StoreException;
    //public void create(SurgeryDaysAssignmentx s)throws StoreException;
    public void create(SurgeryDaysAssignment s)throws StoreException;
    //public void insert(Appointment a) throws StoreException;
    //public void insert(Patient p) throws StoreException;
    public void insert(ThePatient p) throws StoreException;
    public void insert(PatientNotification pn) throws StoreException;
    //public void insert(SurgeryDaysAssignmentx p) throws StoreException;
    public void insert(SurgeryDaysAssignment p) throws StoreException;
    //public void delete(Appointment a) throws StoreException;
    //public void delete(Patient p) throws StoreException;
    public void delete(ThePatient p) throws StoreException;
    public PatientNotification read(PatientNotification value)throws StoreException;
    public PatientNotification.Collection read(PatientNotification.Collection value)throws StoreException;
    //public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentx value) throws StoreException;
    public SurgeryDaysAssignment read(SurgeryDaysAssignment value) throws StoreException;
    //public Appointment read(Appointment a) throws StoreException;
    //public Patient read(Patient p) throws StoreException;
    public ThePatient read(ThePatient p) throws StoreException;
    public ThePatient.Collection read(ThePatient.Collection value)throws StoreException;
    public String read(Store.SelectedTargetStore db)throws StoreException;
    //public void update(Appointment a) throws StoreException;
    //public void update(SurgeryDaysAssignmentx value) throws StoreException;
    public void update(SurgeryDaysAssignment value) throws StoreException;
    //public void update(Patient p) throws StoreException;
    public void update(ThePatient p) throws StoreException;
    public void update(PatientNotification pn)throws StoreException;
    public void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException;
    //public void drop(Appointment a)throws StoreException;
    //public void drop(Patient p)throws StoreException;
    public void drop(ThePatient p)throws StoreException;
    //public void drop(SurgeryDaysAssignmentx s)throws StoreException;
    public void drop(SurgeryDaysAssignment s)throws StoreException;
    
    
    
}
