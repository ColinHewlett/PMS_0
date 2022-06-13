/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.TheAppointment;
import clinicpms.model.AppointmentTable;
import clinicpms.model.Appointments;
import clinicpms.model.IEntityStoreType;
import clinicpms.model.EntityStoreType;
import clinicpms.model.Patient;
import clinicpms.model.PatientNotification;
import clinicpms.model.PatientTable;
import clinicpms.model.Patients;
import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.ThePatient;
import clinicpms.model.SurgeryDaysAssignment;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author colin
 */
public interface IStoreAction {
    //public int countRowsIn(Appointment table)throws StoreException;
    public int countRowsIn(Appointments a)throws StoreException;
    //public int countRowsIn(Patient table)throws StoreException;
    public int countRowsIn(Patients p) throws StoreException;
    public int countRowsIn(ThePatient table)throws StoreException;
    public int countRowsIn(SurgeryDaysAssignment table) throws StoreException;
    //public int countRowsIn(SurgeryDaysAssignmentx table)throws StoreException;
    
    public Integer count(TheAppointment.Collection collection)throws StoreException;
    public Integer count(ThePatient.Collection collection)throws StoreException;
    public Integer count(PatientNotification.Collection collection)throws StoreException;
    public Integer count(SurgeryDaysAssignment surgeryDaysAssignment)throws StoreException;
    
    public void create(TheAppointment a) throws StoreException;
    public void create(PatientNotification pn) throws StoreException;
    public void create(ThePatient p )throws StoreException;
    public void create(SurgeryDaysAssignment s)throws StoreException;
    
    public void delete(Appointment a) throws StoreException;
    public void delete(TheAppointment a) throws StoreException;
    public void delete(Patient p) throws StoreException;
    public void delete(ThePatient p) throws StoreException;
    
    public void drop(TheAppointment a)throws StoreException;
    public void drop(Patient p)throws StoreException;
    public void drop(ThePatient p)throws StoreException;
    public void drop(SurgeryDaysAssignment s)throws StoreException;
    
    public List<String[]> importEntityFromCSV(EntityStoreType entity) throws StoreException;
    
    public void insert(Appointment a) throws StoreException;
    public void insert(TheAppointment a) throws StoreException; 
    public void insert(Appointments appointments)throws StoreException;
    public void insert(Patient p) throws StoreException;
    public void insert(Patients patients) throws StoreException;
    public void insert(ThePatient p) throws StoreException;
    public void insert(PatientNotification pn) throws StoreException;
    //public void insert(SurgeryDaysAssignmentx p) throws StoreException;
    public void insert(SurgeryDaysAssignment p) throws StoreException;
    
    public void populate(SurgeryDaysAssignment data)throws StoreException;
    
    public PatientNotification read(PatientNotification value)throws StoreException;
    public PatientNotification.Collection read(PatientNotification.Collection value)throws StoreException;
    //public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentx value) throws StoreException;
    public SurgeryDaysAssignment read(SurgeryDaysAssignment value) throws StoreException;
    public Appointment read(Appointment a) throws StoreException;
    public TheAppointment read(TheAppointment a)throws StoreException ;
    public TheAppointment.Collection read(TheAppointment.Collection a)throws StoreException ;
    public Patient read(Patient p) throws StoreException;
    public ThePatient read(ThePatient p) throws StoreException;
    public ThePatient.Collection read(ThePatient.Collection value)throws StoreException;
    public String read(Store.SelectedTargetStore db)throws StoreException;
    
    public Appointments readAppointments(Patient p, Appointment.Category c) throws StoreException;
    public Appointments readAppointments() throws StoreException;
    public Appointments readAppointmentsFor(LocalDate day) throws StoreException;
    public Appointments readAppointmentsFrom(LocalDate day) throws StoreException;
    
    public Patients readPatients() throws StoreException;
    
    public void update(Appointment a) throws StoreException;
    public void update(TheAppointment a) throws StoreException;
    //public void update(SurgeryDaysAssignmentx value) throws StoreException;
    public void update(SurgeryDaysAssignment value) throws StoreException;
    public void update(Patient p) throws StoreException;
    public void update(ThePatient p) throws StoreException;
    public void update(PatientNotification pn)throws StoreException;
    public void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException;

}
