/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Appointments;
import clinicpms.model.ThePatient;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.model.AppointmentTable;
import clinicpms.model.IEntityStoreType;
import clinicpms.model.PatientNotification;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.SurgeryDaysAssignmentx;
import clinicpms.model.TheSurgeryDaysAssignment;
import java.io.File;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Dictionary;

/**
 *
 * @author colin
 */
//public class PostgreSQLStore extends Store {
    public class PostgreSQLStore {
    String databaseURL = "jdbc:postgresql://localhost/ClinicPMS?user=colin";

    
    public PostgreSQLStore()throws StoreException{
        //connection = getConnection();
    }
    
    public static PostgreSQLStore getInstance()throws StoreException{
        PostgreSQLStore result = null;
        //if (INSTANCE == null) result = new PostgreSQLStore();
        //else result = (PostgreSQLStore)INSTANCE;
        return result;
    }
    

    public void insert(PatientNotification pn)throws StoreException{

    }



    public void insert(Appointment a) throws StoreException{
        
    }

    public void insert(ThePatient p) throws StoreException{
        
    }

    public void insert(Patient p) throws StoreException{
        
    }
    
    
    public void delete(Appointment a) throws StoreException{
        
    }
    
    
    public void delete(ThePatient p) throws StoreException{
        
    }
    
    
    public void delete(Patient p) throws StoreException{
        
    }
    
    
    public String read(Store.SelectedTargetStore db)throws StoreException{
        return null;
    }
    
    
    public Appointment read(Appointment a) throws StoreException{
        return null;
    }
    
    
    public ThePatient.Collection read(ThePatient.Collection p) throws StoreException{
        return null;
    }
    
    
    public ThePatient read(ThePatient p) throws StoreException{
        return null;
    }
    
    
    public Patient read(Patient p) throws StoreException{
        return null;
    }

    
    /**
     * Automatic transaction processing enabled
     * -- reads all appointments stored on the system
     * @return ArrayList<Appointment>
     * @throws StoreException 
     */
    public Appointments readAppointments() throws StoreException{
        return null;
    }
    
    
    public Appointments readAppointmentsFor(LocalDate day) throws StoreException{
        return null;
    }
    
    
    public Appointments readAppointments(Patient p, Appointment.Category c) throws StoreException{
        return null;
    }
    
    
    public Appointments readAppointmentsFrom(LocalDate day) throws StoreException{
        return null;
    }
    
    
    public Patients readPatients() throws StoreException{
        return null;
    }
    
    
    public void update(Store.SelectedTargetStore db, String updatedLocation)throws StoreException{
        
    }
    
    
    public void update(PatientNotification p) throws StoreException{
        
    }
    
    
    public void update(ThePatient p) throws StoreException{
        
    }
    
    
    public void update(Patient p) throws StoreException{
        
    }
    
    
    public void update(Appointment a) throws StoreException{
        
    }
    
    
    public void update(SurgeryDaysAssignmentx value){

    }

    
    public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentx value) throws StoreException{
        return null;
    }
    
    public SurgeryDaysAssignmentx readSurgeryDays() throws StoreException{
        return null;
    }

    public Dictionary<String,Boolean> updateSurgeryDays(Dictionary<String,Boolean> d) throws StoreException{
        return null;
    }
    
    /**
     * 05/12/2021 11:00 updates included at end of storage type class
     */


    
    
    
    public int countRowsIn(Appointments a){
        return 0;
    }
    
    
    public int countRowsIn(Patients p){
        return 0;
    }

    
    /**
     * Fetches the selected storage type used by the app
     * @return String representing the storage type
     */
    
    public String getStoreType(){
        //return getStorageType().toString();
        return null;
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
    
    public String readAppointmentCSVPath(){
        return null;
    }
    
    /**
     * Fetches the selected path to the CSV file of imported patient data
     * @return String representing the path
     */
    
    public String readPatientCSVPath(){
        return null;
    }
    
    /**
     * Updates the path to the CSV file of i ported appointment data
     * -- stored as a memory image only and not in persistent store
     * @param path:String representing the updated path value 
     */
    
    public void updateAppointmentCSVPath(String path){
        
    }
    
    /**
     * Updates the path to the CSV file of imported patient data
     * -- stored as a memory image only and not in persistent store
     * @param path:String representing the updated path value 
     */
    
    public void updatePatientCSVPath(String path){
        
    }
    
    
    public void updateMigrationTargetStorePath(String path){
        
    }
    
    
    public void updatePMSTargetStorePath(String path){
        
    }
    
    
    public String readPMSTargetStorePath(){
        return null;
    }
    
    
    public String readMigrationTargetStorePath(){
        return null;
    }


    
    
    public PatientNotification read(PatientNotification value)throws StoreException{
        return null;
    }
    
    
    public PatientNotification.Collection read(PatientNotification.Collection value)throws StoreException{
        return null;
    }

    
    public void insert(Appointments appointments) throws StoreException{
        
    }
    
    public void insert(Patients patients) throws StoreException{
        
    }
    
    public void insert(SurgeryDaysAssignmentx surgeryDaysAssignment) throws StoreException{
        
    }
    
    public void drop(Appointment a)throws StoreException{
        
    }
    public void drop(ThePatient p)throws StoreException{
        
    }
    public void drop(Patient p)throws StoreException{
        
    }
    public void drop(SurgeryDaysAssignmentx s)throws StoreException{
        
    }
    public void create(Appointment a)throws StoreException{
        
    }
    public void create(ThePatient p)throws StoreException{
        
    }
    public void create(Patient p)throws StoreException{
        
    }
    public void create(SurgeryDaysAssignmentx s)throws StoreException{
        
    }
    
    public IEntityStoreType importFromCSV(IEntityStoreType entity)throws StoreException{
        return null;
    }
    
    public List<String[]> importFromCSV1(IEntityStoreType entity)throws StoreException{
        return null;
    }
    
    
    public File initialiseTargetStore(File path)throws StoreException{
        return null;
    }
    
    
    public void closeMigrationConnection() throws StoreException{
        
    }
    
    
    public void drop(TheSurgeryDaysAssignment table)throws StoreException{
        
    }
    
    
    public void update(TheSurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        
    }
    
    
    public TheSurgeryDaysAssignment read(TheSurgeryDaysAssignment s) throws StoreException {
        return null;
    }
    
    
    public void create(TheSurgeryDaysAssignment s) throws StoreException {
        
    }
    
    
    public void insert(TheSurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        
    }
}
