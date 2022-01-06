/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysTable;
import clinicpms.model.SurgeryDaysValues;
import clinicpms.model.AppointmentTable;
import clinicpms.model.Appointment;
import clinicpms.model.Appointment.Category;
import clinicpms.model.Appointments;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

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

    public void checkIntegrity()throws StoreException;
    public int countRowsIn(Appointments table)throws StoreException;
    public int countRowsIn(Patients table)throws StoreException;
    public void insert(Appointment a) throws StoreException;
    public void insert(Patient p) throws StoreException;
    public void delete(Appointment a) throws StoreException;
    public void delete(Patient p) throws StoreException;
    public SurgeryDaysValues read(SurgeryDaysValues value) throws StoreException;
    public Appointment read(Appointment a) throws StoreException;
    public Patient read(Patient p) throws StoreException;
    public String read(Store.SelectedTargetStore db)throws StoreException;
    public String readAppointmentCSVPath();
    public ArrayList<Appointment> readAppointments() throws StoreException;
    public ArrayList<Appointment> readAppointments(Patient p, Category c) throws StoreException;
    public ArrayList<Appointment> readAppointmentsFor(LocalDate day) throws StoreException;
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException;
    public String readMigrationTargetStorePath();
    public String readPatientCSVPath();
    public ArrayList<Patient> readPatients() throws StoreException;
    public String readPMSTargetStorePath();
    public void update(Appointment a) throws StoreException;
    public void update(SurgeryDaysValues value) throws StoreException;
    public void update(Patient p) throws StoreException;
    public void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException;
    public void updateAppointmentCSVPath(String path);
    public void updateMigrationTargetStorePath(String path)throws StoreException;
    public void updatePatientCSVPath(String path);
    public void updatePMSTargetStorePath(String path)throws StoreException;
    
    
    
}
