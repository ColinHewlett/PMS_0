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
    public void insert(PatientNotification pn)throws StoreException{

    }
    
    @Override
    public void insert(PatientTable p, Patient patient) throws StoreException{
        
    }
    
    @Override
    public void insert(AppointmentTable a, Appointment appointment) throws StoreException{
        
    }
    
    @Override
    public void insert(Appointment a) throws StoreException{
        
    }
    
    @Override
    public void insert(ThePatient p) throws StoreException{
        
    }
    
    @Override
    public void insert(Patient p) throws StoreException{
        
    }
    
    @Override
    public void delete(Appointment a) throws StoreException{
        
    }
    
    @Override
    public void delete(ThePatient p) throws StoreException{
        
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
    public ThePatient.Collection read(ThePatient.Collection p) throws StoreException{
        return null;
    }
    
    @Override
    public ThePatient read(ThePatient p) throws StoreException{
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
    public void update(PatientNotification p) throws StoreException{
        
    }
    
    @Override
    public void update(ThePatient p) throws StoreException{
        
    }
    
    @Override
    public void update(Patient p) throws StoreException{
        
    }
    
    @Override
    public void update(Appointment a) throws StoreException{
        
    }
    
    @Override
    public void update(SurgeryDaysAssignmentx value){

    }

    @Override
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
    
    @Override
    public int countRowsIn(SurgeryDaysAssignmentTable p){
        return 0;
    }
    
    @Override
    public int countRowsIn(Appointments a){
        return 0;
    }
    
    @Override
    public int countRowsIn(Patients p){
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
     * Populates the surgery days table in the migration store from the specified HashMap collection of values
     * @param data:HashMap<DayOfWeek,Boolean>
     * @throws StoreException 
     */
    @Override
    public void populate(SurgeryDaysAssignmentx data)throws StoreException{
        
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
    public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentTable table){
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
    public PatientNotification read(PatientNotification value)throws StoreException{
        return null;
    }
    
    @Override
    public PatientNotification.Collection read(PatientNotification.Collection value)throws StoreException{
        return null;
    }
    
    @Override
    public void exportToPMS(Appointments appointments) throws StoreException{
        
    }
    
    @Override
    public void exportToPMS(Patients patients) throws StoreException{
        
    }
    
    @Override
    public void exportToPMS(SurgeryDaysAssignmentx surgeryDaysAssignment) throws StoreException{
        
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
    
    @Override
    public File initialiseTargetStore(File path)throws StoreException{
        return null;
    }
    
    @Override
    public void closeMigrationConnection() throws StoreException{
        
    }
    
    @Override
    public void drop(TheSurgeryDaysAssignment table)throws StoreException{
        
    }
    
    @Override
    public void update(TheSurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        
    }
    
    @Override
    public TheSurgeryDaysAssignment read(TheSurgeryDaysAssignment s) throws StoreException {
        return null;
    }
    
    @Override
    public void create(TheSurgeryDaysAssignment s) throws StoreException {
        
    }
    
    @Override
    public void insert(TheSurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        
    }
}
