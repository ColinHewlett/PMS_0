/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.Store.SelectedTargetStore;
import clinicpms.model.IEntity;
import clinicpms.model.IEntityCounter;
import clinicpms.model.IStoreManager;
import clinicpms.model.ITable;

/**
 *
 * @author colin
 */
public abstract class Store implements IPMSStoreAction, IMigrationStoreAction, ITargetsStoreAction {
    
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
                                 UNDEFINED_DATABASE}
    
    protected enum SurgeryDaysSQL {APPEND_SURGERY_DAYS,
                                READ_SURGERY_DAYS,
                                UPDATE_SURGERY_DAYS}

    protected enum Storage{ACCESS, 
                        CSV,
                        POSTGRES,
                        SQL_EXPRESS,
                        UNDEFINED_DATABASE}
    
    protected enum AppointmentSQL   {
                            APPOINTMENTS_COUNT,
                            INSERT_APPOINTMENT,
                            DELETE_APPOINTMENT_WITH_KEY,
                            DELETE_APPOINTMENTS_WITH_PATIENT_KEY,
                            READ_APPOINTMENTS,
                            READ_APPOINTMENTS_FOR_DAY,
                            READ_APPOINTMENTS_FROM_DAY,
                            READ_APPOINTMENTS_FOR_PATIENT,
                            READ_APPOINTMENT_WITH_KEY,
                            READ_HIGHEST_KEY,
                            UPDATE_APPOINTMENT}

    protected enum PatientSQL   {INSERT_PATIENT,
                                PATIENTS_COUNT,
                                READ_ALL_PATIENTS,
                                READ_HIGHEST_KEY,
                                READ_PATIENT_WITH_KEY,
                                UPDATE_PATIENT}
    
    
    protected enum CSVMigrationMethod  {   CSV_APPOINTMENT_FILE_CONVERTER,
                                        //CSV_MIGRATION_INTEGRITY_PROCESS,
                                        CSV_PATIENT_FILE_CONVERTER
                                        //ACCESS_PATIENT_PREPROCESS
                                    }
    
    protected enum MigrationSQL {
                            APPOINTMENT_TABLE_START_TIME_NORMALISED,
                            APPOINTMENT_TABLE_CREATE,
                            APPOINTMENT_TABLE_INSERT_ROW,
                            APPOINTMENT_TABLE_DROP,
                            APPOINTMENT_TABLE_HIGHEST_KEY,
                            APPOINTMENT_TABLE_ROW_COUNT,
                            PATIENT_TABLE_ROW_COUNT,
                            PATIENT_TABLE_CREATE,
                            PATIENT_TABLE_INSERT_ROW,
                            PATIENT_TABLE_DROP,
                            SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION,
                            SURGERY_DAYS_TABLE_ROW_COUNT,
                            SURGERY_DAYS_TABLE_CREATE,
                            SURGERY_DAYS_TABLE_DROP}
    
    protected enum SelectedTargetStore{MIGRATION_DB,
                               PMS_DB}
    
    private static Storage storage = null;
    private static String databaseLocatorPath = null;
    /**
     * DEBUG -- following DatabasePath variables updated from private to protected scope
     * which enables access from the concrete store class
     */
    protected static String migrationDatabasePath = null;
    protected static String pmsDatabasePath = null;
    //store_package_updates_05_12_21_09_17_devDEBUG
    private static String appointmentCSVPath = null;
    private static String patientCSVPath = null;
    private static SelectedTargetStore selectedTargetStore = null;
    protected static Store instance = null;
    private static boolean isMigrationStoreCurrentlyUnderConstruction = false;
    private static boolean isPMSStoreCurrentlyUnderConstruction = false;
    private static boolean isTargetsStoreCurrentlyUnderConstruction = false;
    protected static boolean IS_MIGRATION_STORE_ACTION = false;
    protected static boolean IS_PMS_STORE_ACTION = false;
    protected static boolean IS_TARGETS_STORE_ACTION = false;
    
    public static IMigrationStoreAction FACTORY(ITable table)throws StoreException{
        return MIGRATION_STORE_FACTORY();
    }
    
    public static IPMSStoreAction FACTORY(IEntity entity)throws StoreException{
        return PMS_STORE_FACTORY();
    }
    
    public static IPMSStoreAction FACTORY(IEntityCounter count)throws StoreException{
        return PMS_STORE_FACTORY();
    }
    
    public static ITargetsStoreAction FACTORY(IStoreManager manager)throws StoreException{
        return TARGETS_STORE_FACTORY();
    }
    
    /**
     * Selects the storage class to use (Access, PostgresSQL etc)
     * -- ensures storage type and database locator path have been initialised
     * -- the concrete Store class getInstance() method ensures a single instance only of the class exists
     * -- isMigrationStoreCurrentlyUnderConstruction flag prevents re-entry of factory during a factory cycle
     * @return IMigrationStoreAction object
     * @throws StoreException 
     */
    private static IMigrationStoreAction MIGRATION_STORE_FACTORY()throws StoreException{
        initialiseDatabaseLocationPath();
        initialiseStorageType();
        isMigrationStoreCurrentlyUnderConstruction = true;
        IMigrationStoreAction result = null;
        switch (getStorageType()){
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
        setMigrationStoreKind(true);
        isMigrationStoreCurrentlyUnderConstruction = false;
        return result;
    }
    
    /**
     * Selects the storage class to use (Access, PostgresSQL etc)
     * -- ensures storage type and database locator path have been initialised
     * -- the concrete Store class getInstance() method ensures a single instance only of the class exists
     * -- isStoreCurrentlyUnderConstruction flag prevents re-entry of factory during a factory cycle
     * @return IPMSStoreAction object
     * @throws StoreException 
     */
    private static IPMSStoreAction PMS_STORE_FACTORY()throws StoreException{
        initialiseDatabaseLocationPath();
        initialiseStorageType();
        isPMSStoreCurrentlyUnderConstruction = true;
        IPMSStoreAction result = null;
        switch (getStorageType()){
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
        setPMSStoreKind(true);
        isPMSStoreCurrentlyUnderConstruction = false;
        return result;
    }
    
    /**
     * Selects the storage class to use (Access, PostgresSQL etc)
     * -- ensures storage type and database locator path have been initialised
     * -- the concrete Store class getInstance() method ensures a single instance only of the class exists
     * -- isStoreCurrentlyUnderConstruction flag prevents re-entry of factory during a factory cycle
     * @return IPMSStoreAction object
     * @throws StoreException 
     */
    private static ITargetsStoreAction TARGETS_STORE_FACTORY()throws StoreException{
        initialiseDatabaseLocationPath();
        initialiseStorageType();
        isTargetsStoreCurrentlyUnderConstruction = true;
        ITargetsStoreAction result = null;
        switch (getStorageType()){
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
        setTargetsStoreKind(true);
        isTargetsStoreCurrentlyUnderConstruction = false;
        return result;
    }
    
    private static void setMigrationStoreKind(boolean value){
        IS_MIGRATION_STORE_ACTION = value;
        if (value){
            IS_PMS_STORE_ACTION = false;
            IS_TARGETS_STORE_ACTION = false;
        }
    }
    
    private static void setPMSStoreKind(boolean value){
        IS_PMS_STORE_ACTION = value;
        if (value){
            IS_TARGETS_STORE_ACTION = false;
            IS_MIGRATION_STORE_ACTION = false;
        }
    }
    
    private static void setTargetsStoreKind(boolean value){
        IS_TARGETS_STORE_ACTION = value;
        if (value){
            IS_PMS_STORE_ACTION = false;
            IS_MIGRATION_STORE_ACTION = false;
        }
    }
    
    private static void initialiseDatabaseLocationPath(){
        if (getDatabaseLocatorPath()==null) setDatabaseLocatorPath(System.getenv("PMS_TARGETS_STORE_PATH"));
    }
    
    private static void initialiseStorageType(){
        if (getStorageType()==null) {
            switch (System.getenv("PMS_STORE_TYPE")){
                case "ACCESS":
                    setStorageType(Storage.ACCESS);
                    break;
                case "POSTGRES":
                    setStorageType(Storage.POSTGRES);
                    break;
                case "SQL_EXPRESS":
                    setStorageType(Storage.SQL_EXPRESS);
                    break;     
            }
        }  
    }
 
    /**
     * initialised on first entry to FACTORY method
     * @return Storage enumeration literal signifying which store type is in use 
     */
    protected static Storage getStorageType(){
        return storage;
    } 
    protected static void setStorageType(Storage type){
        storage = type;
    }
    
    /**
     * initialised on first entry to FACTORY method
     * @return String representing the path to the database locator store 
     */
    protected static String getDatabaseLocatorPath(){
        return databaseLocatorPath;
    }
    protected static void setDatabaseLocatorPath(String path){
        databaseLocatorPath = path;
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
        if (!isPMSStoreCurrentlyUnderConstruction&&
                !isMigrationStoreCurrentlyUnderConstruction&&
                !isTargetsStoreCurrentlyUnderConstruction){
            if (instance != null) instance = null;
            MIGRATION_STORE_FACTORY();
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
        if (!isPMSStoreCurrentlyUnderConstruction&&
                !isMigrationStoreCurrentlyUnderConstruction&&
                !isTargetsStoreCurrentlyUnderConstruction){
            if (instance != null) instance = null;
            PMS_STORE_FACTORY();
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
    
    
}

