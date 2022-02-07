/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Appointments;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.model.AppointmentTable;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.SurgeryDaysAssignment;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Dictionary;

/**
 *
 * @author colin
 */
public class PostgreSQLStore extends Store {

    String databaseURL = "jdbc:postgresql://localhost/ClinicPMS?user=colin";

    
    public PostgreSQLStore()throws StoreException{
        //connection = getConnection();
    }
    
    public static PostgreSQLStore getInstance()throws StoreException{
        PostgreSQLStore result;
        if (INSTANCE == null) result = new PostgreSQLStore();
        else result = (PostgreSQLStore)INSTANCE;
        return result;
    }
    
    @Override
    public void insert(Appointment a) throws StoreException{
        
    }
    
    @Override
    public void insert(Patient p) throws StoreException{
        
    }
    
    @Override
    public void delete(Appointment a) throws StoreException{
        
    }
    
    @Override
    public void delete(Patient p) throws StoreException{
        
    }
    
    @Override
    public String read(Store.SelectedTargetStore db)throws StoreException{
        return null;
    }
    
    @Override
    public Appointment read(Appointment a) throws StoreException{
        return null;
    }
    
    @Override
    public Patient read(Patient p) throws StoreException{
        return null;
    }

    @Override
    /**
     * Automatic transaction processing enabled
     * -- reads all appointments stored on the system
     * @return ArrayList<Appointment>
     * @throws StoreException 
     */
    public Appointments readAppointments() throws StoreException{
        return null;
    }
    
    @Override
    public Appointments readAppointmentsFor(LocalDate day) throws StoreException{
        return null;
    }
    
    @Override
    public Appointments readAppointments(Patient p, Appointment.Category c) throws StoreException{
        return null;
    }
    
    @Override
    public Appointments readAppointmentsFrom(LocalDate day) throws StoreException{
        return null;
    }
    
    @Override
    public Patients readPatients() throws StoreException{
        return null;
    }
    
    @Override
    public void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException{
        
    }
    
    @Override
    public void update(Patient p) throws StoreException{
        
    }
    
    @Override
    public void update(Appointment a) throws StoreException{
        
    }
    
    @Override
    public void update(SurgeryDaysAssignment value){

    }

    @Override
    public SurgeryDaysAssignment read(SurgeryDaysAssignment value) throws StoreException{
        return null;
    }
    
    public SurgeryDaysAssignment readSurgeryDays() throws StoreException{
        return null;
    }

    public Dictionary<String,Boolean> updateSurgeryDays(Dictionary<String,Boolean> d) throws StoreException{
        return null;
    }
    
    /**
     * 05/12/2021 11:00 updates included at end of storage type class
     */
    
    /**
     * 
     * @param a:Appointments
     * @return 
     */
    @Override
    public int countRowsIn(AppointmentTable a){
        return 0;
    }

    
    /**
     * 
     * @param p Patients
     * @return 
     */
    @Override
    public int countRowsIn(PatientTable p){
        return 0;
    }
 
    /**
     * Creates an appointment table in the migration store
     * @param table:AppointmentTable)
     * @throws StoreException 
     */
    @Override
    public void create(AppointmentTable table)throws StoreException{
        
    }
    
    /**
     * Creates a patient table in the migration store
     * @param table:PatientTable
     * @throws StoreException 
     */
    @Override
    public void create(PatientTable table)throws StoreException{
        
    }
    
    /**
     * Creates a SurgeryDays table in the migration store
     * @param table:SurgeryDaysAssignmentTable
     * @throws StoreException 
     */
    @Override
    public void create(SurgeryDaysAssignmentTable table)throws StoreException{
        
    }
    
    /**
     * drops the current appointment table (if any) in the migration store
     * @param table:AppointmentTable
     * @throws StoreException 
     */
    @Override
    public void drop(AppointmentTable table)throws StoreException{
        
    }
    
    /**
     * Drops the current patient table (if any) in the migration store
     * @param table:PatientTable
     * @throws StoreException 
     */
    @Override
    public void drop(PatientTable table)throws StoreException{
        
    }
    
    /**
     * Drops the current SurgeryDaysAssignmentTable (if any) in the migration store
     * @param table:SurgeyDaysTable
     * @throws StoreException 
     */
    @Override
    public void drop(SurgeryDaysAssignmentTable table)throws StoreException{
        
    }
    
    /**
     * Populates the appointment table in the migration store with the imported Appointment objects 
     * @param table:AppointmentTable
     * @throws StoreException 
     */
    @Override
    public void populate(AppointmentTable table)throws StoreException{
        
    }
    
    /**
     * Populates the patient table in the migration store with the imported Patient objects 
     * @param table:PatientTable
     * @throws StoreException 
     */
    @Override
    public void populate(PatientTable table)throws StoreException{
        
    }
    
    /**
     * Populates the surgery days table in the migration store from the specified HashMap collection of values
     * @param data:HashMap<DayOfWeek,Boolean>
     * @throws StoreException 
     */
    @Override
    public void populate(SurgeryDaysAssignment data)throws StoreException{
        
    }
    
    /**
     * Fetches the selected storage type used by the app
     * @return String representing the storage type
     */
    @Override
    public String getStoreType(){
        return getStorageType().toString();
    }
    
    /**
     * Checks the integrity of the data stored in the appointment table
     * -- ensures no appointment refers to a patient key that does not exist in the patient table
     * -- if an appointment does; the appointment is deleted
     * @throws StoreException 
     */
    @Override
    public void checkIntegrity()throws StoreException{
        
    }
    
    /**
     * Convenience method that normalises imported appointment start times
     * @throws StoreException 
     */
    public void normaliseAppointmentStartTimes()throws StoreException{
 
    }
    
    /**
     * Fetches the selected path to the CSV file of imported appointment data
     * @return String representing the path
     */
    @Override
    public String readAppointmentCSVPath(){
        return null;
    }
    
    /**
     * Fetches the selected path to the CSV file of imported patient data
     * @return String representing the path
     */
    @Override
    public String readPatientCSVPath(){
        return null;
    }
    
    /**
     * Updates the path to the CSV file of i ported appointment data
     * -- stored as a memory image only and not in persistent store
     * @param path:String representing the updated path value 
     */
    @Override
    public void updateAppointmentCSVPath(String path){
        
    }
    
    /**
     * Updates the path to the CSV file of imported patient data
     * -- stored as a memory image only and not in persistent store
     * @param path:String representing the updated path value 
     */
    @Override
    public void updatePatientCSVPath(String path){
        
    }
    
    @Override
    public void updateMigrationTargetStorePath(String path){
        
    }
    
    @Override
    public void updatePMSTargetStorePath(String path){
        
    }
    
    @Override
    public String readPMSTargetStorePath(){
        return null;
    }
    
    @Override
    public String readMigrationTargetStorePath(){
        return null;
    }

    @Override
    public SurgeryDaysAssignment read(SurgeryDaysAssignmentTable table){
        return null;
    }
    
    @Override
    public Appointments read(AppointmentTable table){
        return null;
    }
    
    @Override
    public Patients read(PatientTable table){
        return null;
    }
    
    @Override
    public void exportToPMS(Appointments appointments) throws StoreException{
        
    }
    
    @Override
    public void exportToPMS(Patients patients) throws StoreException{
        
    }
    
    @Override
    public void exportToPMS(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException{
        
    }
    
    public void insert(Appointments appointments) throws StoreException{
        
    }
    
    public void insert(Patients patients) throws StoreException{
        
    }
    
    public void insert(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException{
        
    }
    
    public void drop(Appointment a)throws StoreException{
        
    }
    public void drop(Patient p)throws StoreException{
        
    }
    public void drop(SurgeryDaysAssignment s)throws StoreException{
        
    }
}
