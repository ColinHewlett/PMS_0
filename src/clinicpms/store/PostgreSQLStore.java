/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Patient;
import clinicpms.model.PatientNotification;
import clinicpms.model.SurgeryDaysAssignment;
import java.io.File;
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



    public void insert(Patient p) throws StoreException{
        
    }

    public void update(PatientNotification p) throws StoreException{
        
    }

    public void update(Patient p) throws StoreException{
        
    }

    
    
    

    
    
    
    

    public Dictionary<String,Boolean> updateSurgeryDays(Dictionary<String,Boolean> d) throws StoreException{
        return null;
    }
    
    /**
     * 05/12/2021 11:00 updates included at end of storage type class
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

    public void drop(Patient p)throws StoreException{
        
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
