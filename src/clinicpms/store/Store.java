/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.Store.SelectedTargetStore;
import clinicpms.model.IAppointments;
import clinicpms.model.IEntity;
import clinicpms.model.IPatients;
import clinicpms.model.IStoreManager;
import clinicpms.model.ITable;

/**
 *
 * @author colin
 */
public abstract class Store implements IAppointmentsStoreAction, 
                                       IPatientsStoreAction, 
                                       IPMSStoreAction, 
                                       IMigrationStoreAction, 
                                       ITargetsStoreAction {
    
    protected enum ConnectionMode{ AUTO_COMMIT_OFF, AUTO_COMMIT_ON}
    protected enum ExceptionType {  APPOINTEE_NOT_FOUND_EXCEPTION,
                                 IO_EXCEPTION,
                                 CSV_EXCEPTION,
                                 NULL_KEY_EXPECTED_EXCEPTION,
                                 NULL_KEY_EXCEPTION,
                                 INVALID_KEY_VALUE_EXCEPTION,
                                 KEY_FOUND_EXCEPTION,
                                 KEY_NOT_FOUND_EXCEPTION,
                                 SQL_EXCEPTION,
                                 STORE_EXCEPTION,
                                 UNEXPECTED_DATA_TYPE_ENCOUNTERED,
                                 UNDEFINED_DATABASE}
    
    protected enum SurgeryDaysSQL {
                                READ_SURGERY_DAYS,
                                UPDATE_SURGERY_DAYS}

    protected enum Storage{ACCESS, 
                        CSV,
                        POSTGRES,
                        SQL_EXPRESS,
                        UNDEFINED_DATABASE}
    
    protected enum PatientManagementSystemSQL   {
                            APPOINTMENTS_COUNT,
                            DELETE_APPOINTMENT_WITH_KEY,
                            DELETE_APPOINTMENTS_WITH_PATIENT_KEY,
                            INSERT_APPOINTMENT,
                            READ_APPOINTMENTS,
                            READ_APPOINTMENTS_FOR_DAY,
                            READ_APPOINTMENTS_FOR_PATIENT,
                            READ_APPOINTMENTS_FROM_DAY,
                            READ_APPOINTMENT_WITH_KEY,
                            READ_APPOINTMENT_HIGHEST_KEY,
                            UPDATE_APPOINTMENT,
                            INSERT_PATIENT,
                            PATIENTS_COUNT,
                            READ_ALL_PATIENTS,
                            READ_PATIENT_HIGHEST_KEY,
                            READ_PATIENT_WITH_KEY,
                            UPDATE_PATIENT,
                            READ_SURGERY_DAYS,
                            UPDATE_SURGERY_DAYS}
    
    protected enum MigrationSQL {
                            APPOINTMENT_TABLE_CREATE,
                            APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY,
                            APPOINTMENT_TABLE_DROP,
                            APPOINTMENT_TABLE_INSERT_ROW,
                            APPOINTMENT_TABLE_HIGHEST_KEY,
                            APPOINTMENT_TABLE_READ,
                            APPOINTMENT_TABLE_ROW_COUNT,
                            APPOINTMENT_TABLE_START_TIME_NORMALISED,
                            PATIENT_TABLE_CREATE,
                            PATIENT_TABLE_DROP,
                            PATIENT_TABLE_INSERT_ROW,
                            PATIENT_TABLE_READ,
                            PATIENT_TABLE_READ_PATIENT,
                            PATIENT_TABLE_ROW_COUNT,
                            PATIENT_TABLE_UPDATE,
                            SURGERY_DAYS_TABLE_CREATE,
                            SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION,
                            SURGERY_DAYS_TABLE_DROP,
                            SURGERY_DAYS_TABLE_READ,
                            SURGERY_DAYS_TABLE_ROW_COUNT
                            }

    protected enum PatientSQL   {INSERT_PATIENT,
                                PATIENTS_COUNT,
                                READ_ALL_PATIENTS,
                                READ_HIGHEST_KEY,
                                READ_PATIENT_WITH_KEY,
                                UPDATE_PATIENT}
    
    
    protected enum CSVMigrationMethod  {   
                                        CSV_APPOINTMENT_FILE_CONVERTER,
                                        CSV_PATIENT_FILE_CONVERTER
                                    }

    protected enum SelectedTargetStore{
                                MIGRATION_DB,
                                PMS_DB,
                                CSV_APPOINTMENT_FILE,
                                CSV_PATIENT_FILE}
    
    private static Storage STORAGE = null;
    private  static String databaseLocatorPath = null;
    /**
     * DEBUG -- following DatabasePath variables updated from private to protected scope
     * which enables access from the concrete store class
     */
    protected  String migrationDatabasePath = null;
    protected  String pmsDatabasePath = null;
    //store_package_updates_05_12_21_09_17_devDEBUG
    protected  String appointmentCSVPath = null;
    protected  String patientCSVPath = null;
    private  SelectedTargetStore selectedTargetStore = null;
    protected static Store INSTANCE = null;
    private static boolean IS_MIGRATION_STORE_CURRENTLY_UNDER_CONSTRUCTION = false;
    private static boolean IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = false;
    private static boolean IS_TARGETS_STORE_GURRENTLY_UNDER_CONSTRUCTION = false;
    protected static boolean IS_APPOINTMENTS_STORE_ACTION = false;
    protected static boolean IS_MIGRATION_STORE_ACTION = false;
    protected static boolean IS_PATIENTS_STORE_ACTION = false;
    protected static boolean IS_PMS_STORE_ACTION = false;
    protected static boolean IS_TARGETS_STORE_ACTION = false;
    
    
    private static IAppointmentsStoreAction FACTORY_FOR_APPOINTMENTS_STORE()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = true;
        IAppointmentsStoreAction result = null;
        switch (STORAGE){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            case POSTGRES:
                result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                result = SQLExpressStore.getInstance();
                break;
                
        }
        SET_PMS_STORE_ACTION_STATE(true);
        IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = false;
        return result;
    }
    /**
     * Selects the STORAGE class to use (Access, PostgresSQL etc)
 -- ensures STORAGE type and database locator path have been initialised
 -- the concrete Store class getInstance() method ensures a single INSTANCE only of the class exists
 -- IS_MIGRATION_STORE_CURRENTLY_UNDER_CONSTRUCTION flag prevents re-entry of factory during a factory cycle
     * @return IMigrationStoreAction object
     * @throws StoreException 
     */
    private static IMigrationStoreAction FACTORY_FOR_MIGRATION_STORE()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        IS_MIGRATION_STORE_CURRENTLY_UNDER_CONSTRUCTION = true;
        IMigrationStoreAction result = null;
        switch (STORAGE){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            case POSTGRES:
                result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                result = SQLExpressStore.getInstance();
                break;
                
        }
        SET_MIGRATION_STORE_ACTION_STATE(true);
        IS_MIGRATION_STORE_CURRENTLY_UNDER_CONSTRUCTION = false;
        return result;
    }
    
    private static IPatientsStoreAction FACTORY_FOR_PATIENTS_STORE()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = true;
        IPatientsStoreAction result = null;
        switch (STORAGE){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            case POSTGRES:
                result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                result = SQLExpressStore.getInstance();
                break;
                
        }
        SET_PMS_STORE_ACTION_STATE(true);
        IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = false;
        return result;
    }
    
    /**
     * Selects the STORAGE class to use (Access, PostgresSQL etc)
 -- ensures STORAGE type and database locator path have been initialised
 -- the concrete Store class getInstance() method ensures a single INSTANCE only of the class exists
 -- isStoreCurrentlyUnderConstruction flag prevents re-entry of factory during a factory cycle
     * @return IPMSStoreAction object
     * @throws StoreException 
     */
    private static IPMSStoreAction FACTORY_FOR_PMS_STORE()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = true;
        IPMSStoreAction result = null;
        switch (STORAGE){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            case POSTGRES:
                result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                result = SQLExpressStore.getInstance();
                break;
                
        }
        SET_PMS_STORE_ACTION_STATE(true);
        IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = false;
        return result;
    }
    
    /**
     * Selects the STORAGE class to use (Access, PostgresSQL etc)
 -- ensures STORAGE type and database locator path have been initialised
 -- the concrete Store class getInstance() method ensures a single INSTANCE only of the class exists
 -- isStoreCurrentlyUnderConstruction flag prevents re-entry of factory during a factory cycle
     * @return IPMSStoreAction object
     * @throws StoreException 
     */
    private static ITargetsStoreAction FACTORY_FOR_TARGETS_STORE()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        IS_TARGETS_STORE_GURRENTLY_UNDER_CONSTRUCTION = true;
        ITargetsStoreAction result = null;
        switch (STORAGE){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            case POSTGRES:
                result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                result = SQLExpressStore.getInstance();
                break;
                
        }
        SET_TARGETS_STORE_ACTION_STATE(true);
        IS_TARGETS_STORE_GURRENTLY_UNDER_CONSTRUCTION = false;
        return result;
    }
    
    private static void SET_MIGRATION_STORE_ACTION_STATE(boolean value){
        IS_MIGRATION_STORE_ACTION = value;
        if (value){
            IS_PMS_STORE_ACTION = false;
            IS_TARGETS_STORE_ACTION = false;
        }
    }

    private static void SET_PMS_STORE_ACTION_STATE(boolean value){
        IS_PMS_STORE_ACTION = value;
        if (value){
            IS_TARGETS_STORE_ACTION = false;
            IS_MIGRATION_STORE_ACTION = false;
        }
    }
    
    private static void SET_TARGETS_STORE_ACTION_STATE(boolean value){
        IS_TARGETS_STORE_ACTION = value;
        if (value){
            IS_PMS_STORE_ACTION = false;
            IS_MIGRATION_STORE_ACTION = false;
        }
    }
    
    private static void INITIALISE_DATABASE_LOCATOR_PATH(){
        if (databaseLocatorPath==null) databaseLocatorPath = System.getenv("PMS_TARGETS_STORE_PATH");
    }
    
    private static void INITIALISE_STORAGE_TYPE(){
        if (STORAGE==null) {
            switch (System.getenv("PMS_STORE_TYPE")){
                case "ACCESS":
                    STORAGE = Storage.ACCESS;
                    break;
                case "POSTGRES":
                    STORAGE = Storage.POSTGRES;
                    break;
                case "SQL_EXPRESS":
                    STORAGE = Storage.SQL_EXPRESS;
                    break;     
            }
        }  
    }
 
    /**
     * initialised on first entry to FACTORY method
     * @return Storage enumeration literal signifying which store type is in use 
     */
    protected  Storage getStorageType(){
        return STORAGE;
    } 
    
    /**
     * initialised on first entry to FACTORY method
     * @return String representing the path to the database locator store 
     */
    protected   String getDatabaseLocatorPath(){
        return databaseLocatorPath;
    }

    protected String getMigrationDatabasePath(){
        return migrationDatabasePath;
    }
    
    /**
     * update logged 22/11/2021 08:52
     * The target Migration database path is initialised. If the FACTORY() method is not in mid cycle
     * -- the current instance of the concrete Store class, if it exists, is nullified
     * -- this enables the latest migration database path to take effect in the current instance of the app
     * @param path:String 
     */
    protected void setMigrationDatabasePath(String path) throws StoreException{
        migrationDatabasePath = path;
        /**
         * DEBUG -- only if a FACTOTY() method is not currently being executed
         */
        if (!IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION&&
                !IS_MIGRATION_STORE_CURRENTLY_UNDER_CONSTRUCTION&&
                !IS_TARGETS_STORE_GURRENTLY_UNDER_CONSTRUCTION){
            if (INSTANCE != null) INSTANCE = null;
            FACTORY_FOR_TARGETS_STORE();
        }
    }//store_package_updates_05_12_21_09_17_devDEBUG
    
    protected String getPMSDatabasePath(){
        return pmsDatabasePath;
    }
    
    /**
     * update logged 22/11/2021 08:52
     * The target PMS database path is initialised. If the FACTORY() method is not in mid cycle
     * -- the current instance of the concrete Store class, if it exists, is nullified
     * -- this enables the latest PMS database path to take effect in the current instance of the app
     * @param path:String 
     */
    protected void setPMSDatabasePath(String path) throws StoreException{
        pmsDatabasePath = path;
        /**
         * DEBUG -- only if a FACTOTY() method is not currently being executed
         */
        if (!IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION&&
                !IS_MIGRATION_STORE_CURRENTLY_UNDER_CONSTRUCTION&&
                !IS_TARGETS_STORE_GURRENTLY_UNDER_CONSTRUCTION){
            if (INSTANCE != null) INSTANCE = null;
            FACTORY_FOR_TARGETS_STORE();
        }
    }//store_package_updates_05_12_21_09_17_devDEBUG
   
    /**
     * Getters & setters enabling the Controller to update the path to the input data sources for data migration purposes
     * -- note; controller access to any Store method is via the model
     * @return String representing the path to the input data source 
     */
    protected String getAppointmentCSVPath(){
        return appointmentCSVPath;
    }
    protected String getPatientCSVPath(){
        return patientCSVPath;
    }
    protected void setAppointmentCSVPath(String value){
        appointmentCSVPath = value;
    }
    protected void setPatientCSVPath(String value){
        patientCSVPath = value;
    }
    
    public static IAppointmentsStoreAction FACTORY(IAppointments appointments) throws StoreException{
        return FACTORY_FOR_APPOINTMENTS_STORE();
    }
    
    public static IMigrationStoreAction FACTORY(ITable table)throws StoreException{
        return FACTORY_FOR_MIGRATION_STORE();
    }
    
    public static IPatientsStoreAction FACTORY(IPatients patients) throws StoreException{
        return FACTORY_FOR_PATIENTS_STORE();
    }
    
    public static IPMSStoreAction FACTORY(IEntity entity)throws StoreException{
        return FACTORY_FOR_PMS_STORE();
    }
    
    public static ITargetsStoreAction FACTORY(IStoreManager manager)throws StoreException{
        return FACTORY_FOR_TARGETS_STORE();
    }

}

