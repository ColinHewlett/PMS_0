/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Patient;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.model.PatientNotification;
import java.io.File;
import java.time.LocalDate;
import java.util.Dictionary;
import java.util.List;

/**
 *
 * @author colin
 */
//public class SQLExpressStore extends Store {
public class SQLExpressStore  {    
    //private static SQLExpressStore INSTANCE = null;
    public static SQLExpressStore getInstance(){
        SQLExpressStore result = null;
        //if (INSTANCE == null) result = new SQLExpressStore();
        //else result = (SQLExpressStore)INSTANCE;
        return result;
    }
    
    public void insert(PatientNotification pn)throws StoreException{
        
    }

    
    public void insert(Patient p) throws StoreException{
        
    }

    public void delete(Patient p) throws StoreException{
        
    }

    public Patient read(Patient p) throws StoreException{
        return null;
    }
  
    public PatientNotification read(PatientNotification value)throws StoreException{
        return null;
    }
    
    public void update(PatientNotification p) throws StoreException{
        
    }
    
    
    public void update(Patient p) throws StoreException{
        
    }
    
 
    
    

    
    
    
    public Dictionary<String,Boolean> readSurgeryDays() throws StoreException{
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
     * @param table:AppointmenTable 
    * @return 
     */
    /*
    
    public int countRowsIn(AppointmentTable table){
        return 0;
    }
    */
    /**
     * 
     * @param p Patients
     * @return 
     */
    /*
    
    public int countRowsIn(PatientTable p){
        return 0;
    }
    */

    /**
     * Creates an appointment table in the migration store
     * @param table:AppointmentTable)
     * @throws StoreException 
     */
    /*
    
    public void create(AppointmentTable table)throws StoreException{
        
    }
    */
    /**
     * Creates a patient table in the migration store
     * @param table:PatientTable
     * @throws StoreException 
     */
    /*
    
    public void create(PatientTable table)throws StoreException{
        
    }
    */
    /**
     * Creates a SurgeryDays table in the migration store
     * @param table:SurgeryDaysAssignmentTable
     * @throws StoreException 
     */
    /*
    
    public void create(SurgeryDaysAssignmentTable table)throws StoreException{
        
    }
    */
    /**
     * drops the current appointment table (if any) in the migration store
     * @param table:AppointmentTable
     * @throws StoreException 
     */
    /*
    
    public void drop(AppointmentTable table)throws StoreException{
        
    }
    */
    /**
     * Drops the current patient table (if any) in the migration store
     * @param table:PatientTable
     * @throws StoreException 
     */
    /*
    
    public void drop(PatientTable table)throws StoreException{
        
    }
    */
    /**
     * Drops the current SurgeryDaysAssignmentTable (if any) in the migration store
     * @param table:SurgeyDaysTable
     * @throws StoreException 
     */
    /*
    
    public void drop(SurgeryDaysAssignmentTable table)throws StoreException{
        
    }
    */
    
    
    
    
    /**
     * Fetches the selected storage type used by the app
     * @return String representing the storage type
     */
    
    public String getStoreType(){
        //return getStorageType().toString();
        return null;
    }
    
    /**
     * Checks the integrity of the data stored in the appointment table
     * -- ensures no appointment refers to a patient key that does not exist in the patient table
     * -- if an appointment does; the appointment is deleted
     * @throws StoreException 
     */
    /*
    
    public void checkIntegrity()throws StoreException{
        
    }
    */
    
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
    
    

    public void drop(Patient p)throws StoreException{
        
    }

    public void create(PatientNotification pn)throws StoreException{
        
    }
    public void create(Patient p)throws StoreException{
        
    }

    
    public File initialiseTargetStore(File path)throws StoreException{
        return null;
    }
    
    
    public void closeMigrationConnection() throws StoreException{
        
    }

    
    public void drop(SurgeryDaysAssignment table)throws StoreException{
        
    }
    
    
    public void update(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        
    }
    
    
    public SurgeryDaysAssignment read(SurgeryDaysAssignment s) throws StoreException {
        return null;
    }
    
    
    public void create(SurgeryDaysAssignment s) throws StoreException {
        
    }
    
    
    public void insert(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        
    }
}
