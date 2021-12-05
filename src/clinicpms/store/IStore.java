/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Appointment.Category;
import clinicpms.model.Patient;
import com.opencsv.exceptions.CsvException;
import clinicpms.model.Patient;
import clinicpms.model.SurgeryDays;
import clinicpms.store.exceptions.StoreException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Dictionary;
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
import java.io.IOException;
public interface IStore {

    public void closeConnection()throws StoreException;
    public Appointment create(Appointment a) throws StoreException;
    public Patient create(Patient p) throws StoreException;
    public void delete(Appointment a) throws StoreException;
    public void delete(Patient p) throws StoreException;
    public IMigrationManager getMigrationManager();
    public ITargetsDatabaseManager getTargetsDatabaseManager() throws StoreException;
    public HashMap<DayOfWeek,Boolean> read(HashMap<DayOfWeek,Boolean> value) throws StoreException;
    public Appointment read(Appointment a) throws StoreException;
    public Patient read(Patient p) throws StoreException;
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException;
    public ArrayList<Appointment> readAppointments(Patient p, Category c) throws StoreException;
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException;
    public ArrayList<Patient> readPatients() throws StoreException;
    public Dictionary<String,Boolean> readSurgeryDays() throws StoreException;
    public HashMap<DayOfWeek,Boolean> update(HashMap<DayOfWeek,Boolean> value) throws StoreException;
    public Patient update(Patient p) throws StoreException;
    public Appointment update(Appointment a) throws StoreException;
    
}
