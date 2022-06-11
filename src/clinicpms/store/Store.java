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
import clinicpms.model.EntityStoreType;

/**
 *
 * @author colin
 */
public abstract class Store implements IStoreAction, 
                                       IAppointmentsStoreAction,
                                       IPatientsStoreAction,
                                       ITargetsStoreAction {
    
    protected enum ConnectionMode{ AUTO_COMMIT_OFF, AUTO_COMMIT_ON}
    
    
    protected enum SurgeryDaysSQL {
                                READ_SURGERY_DAYS,
                                UPDATE_SURGERY_DAYS}

    protected enum Storage{ACCESS, 
                        CSV,
                        POSTGRES,
                        SQL_EXPRESS,
                        UNDEFINED_DATABASE}
    
    protected enum EntitySQL {
                            APPOINTMENT,
                            PATIENT,
                            PATIENT_NOTIFICATION,
                            SURGERY_DAYS_ASSIGNMENT,
                            PMS_STORE}
    
    protected enum AppointmentSQL{
                            CREATE_APPOINTMENT_TABLE,
                            COUNT_APPOINTMENTS,
                            DROP_APPOINTMENT_TABLE,
                            DELETE_APPOINTMENT_WITH_KEY,
                            DELETE_APPOINTMENT_WITH_PATIENT_KEY,
                            INSERT_APPOINTMENT,
                            READ_APPOINTMENTS,
                            READ_APPOINTMENTS_FOR_DAY,
                            READ_APPOINTMENTS_FOR_PATIENT,
                            READ_APPOINTMENTS_FROM_DAY,
                            READ_APPOINTMENT_WITH_KEY,
                            READ_HIGHEST_KEY,
                            UPDATE_APPOINTMENT,
                            }
    protected enum PatientNotificationSQL{
                            CREATE_PATIENT_NOTIFICATION, 
                            DROP_PATIENT_NOTIFICATION,
                            DELETE_PATIENT_NOTIFICATION,
                            INSERT_PATIENT_NOTIFICATION,
                            READ_PATIENT_NOTIFICATIONS,
                            READ_PATIENT_NOTIFICATIONS_FOR_PATIENT,
                            READ_PATIENT_NOTIFICATION_WITH_KEY,
                            READ_UNACTIONED_PATIENT_NOTIFICATIONS,
                            READ_PATIENT_NOTIFICATION_HIGHEST_KEY,
                            UPDATE_PATIENT_NOTIFICATION
                            }
    protected enum PatientSQL{
                            COUNT_PATIENTS,
                            CREATE_PATIENT_TABLE,
                            DROP_PATIENT_TABLE,
                            INSERT_PATIENT,
                            READ_PATIENTS,
                            READ_PATIENT_WITH_KEY,
                            UPDATE_PATIENT
                            }
    protected enum SurgeryDaysAssignmentSQL{
                            COUNT_SURGERY_DAYS_ASSIGNMENTS,
                            CREATE_SURGERY_DAYS_ASSIGNMENT_TABLE,
                            DROP_SURGERY_DAYS_ASSIGNMENT_TABLE,
                            READ_SURGERY_DAYS_ASSIGNMENT,
                            INSERT_SURGERY_DAYS_ASSIGNMENT,
                            UPDATE_SURGERY_DAYS_ASSIGNMENT,
                            }
    protected enum TargetSQL{
                            
        
                            }

    protected enum PMSSQL   {
                            
                                READ_HIGHEST_KEY,
                            CREATE_PRIMARY_KEY,
                                APPOINTMENTS_COUNT,
                                
                                 
                                
                                COUNT_APPOINTMENTS,
                                COUNT_APPOINTMENTS_FOR_DAY,
                                COUNT_APPOINTMENTS_FOR_PATIENT,
                                COUNT_APPOINTMENTS_FROM_DAY,
                                CREATE_APPOINTMENT_TABLE,
                                DELETE_APPOINTMENT,
                                DELETE_APPOINTMENTS_FOR_PATIENT,
                                DROP_APPOINTMENT_TABLE,
                                INSERT_APPOINTMENT,
                                READ_APPOINTMENT,
                                READ_APPOINTMENTS,
                                READ_APPOINTMENTS_FOR_DAY,
                                READ_APPOINTMENTS_FOR_PATIENT,
                                READ_APPOINTMENTS_FROM_DAY,
                                READ_APPOINTMENT_NEXT_HIGHEST_KEY,
                                UPDATE_APPOINTMENT,
                                
                                COUNT_PATIENT_NOTIFICATIONS,
                                COUNT_UNACTIONED_PATIENT_NOTIFICATIONS,
                                CREATE_PATIENT_NOTIFICATION_TABLE,
                                INSERT_PATIENT_NOTIFICATION,
                                READ_PATIENT_NOTIFICATIONS,
                                READ_UNACTIONED_PATIENT_NOTIFICATIONS,
                                READ_PATIENT_NOTIFICATION_NEXT_HIGHEST_KEY,
                                READ_PATIENT_NOTIFICATION,
                                READ_PATIENT_NOTIFICATIONS_FOR_PATIENT,
                                UPDATE_PATIENT_NOTIFICATION,
                                DELETE_PATIENT_NOTIFICATION,
                                PATIENT_NOTIFICATION_CREATE, 
                                PATIENT_NOTIFICATION_DROP,
                                
                                COUNT_PATIENTS,
                                CREATE_PATIENT_TABLE,
                                DROP_PATIENT_TABLE,
                                INSERT_PATIENT,
                                READ_PATIENT,
                                READ_PATIENTS,
                                READ_PATIENT_NEXT_HIGHEST_KEY,
                                UPDATE_PATIENT,
                                
                                COUNT_SURGERY_DAYS_ASSIGNMENT,
                                CREATE_SURGERY_DAYS_ASSIGNMENT_TABLE,
                                DROP_SURGERY_DAYS_ASSIGNMENT_TABLE,
                                INSERT_SURGERY_DAYS_ASSIGNMENT,
                                READ_SURGERY_DAYS_ASSIGNMENT,
                                UPDATE_SURGERY_DAYS_ASSIGNMENT,

                                READ_CSV_APPOINTMENT_FILE_LOCATION,
                                READ_CSV_PATIENT_FILE_LOCATION,
                                READ_PMS_STORE_LOCATION,
                                
                                UPDATE_CSV_APPOINTMENT_FILE_LOCATION,
                                UPDATE_CSV_PATIENT_FILE_LOCATION,
                                UPDATE_PMS_STORE_LOCATION
    
                                }
    
                                
    
    protected enum MigrationSQL {
                            APPOINTMENT_TABLE_CREATE,
                            APPOINTMENT_TABLE_ADD_FOREIGN_KEY,
                            APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY,
                            APPOINTMENT_TABLE_DROP,
                            APPOINTMENT_TABLE_INSERT_ROW,
                            APPOINTMENT_TABLE_HIGHEST_KEY,
                            APPOINTMENT_TABLE_READ,
                            APPOINTMENT_TABLE_READ_WITH_KEY,
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
                            SURGERY_DAYS_TABLE_ROW_COUNT,
                            EXPORT_MIGRATED_DATA_TO_PMS
                            }

/*
    protected enum PatientSQL   {INSERT_PATIENT,
                                COUNT_PATIENTS,
                                READ_PATIENTS,
                                READ_HIGHEST_KEY,
                                READ_PATIENT,
                                UPDATE_PATIENT}
*/    
    
    protected enum CSVMigrationMethod  {   
                                        CSV_APPOINTMENT_FILE_CONVERTER,
                                        CSV_PATIENT_FILE_CONVERTER
                                    }

    protected enum SelectedTargetStore{
                                MIGRATION_DB,
                                PMS_DB,
                                STORE_DB,
                                CSV_APPOINTMENT_FILE,
                                CSV_PATIENT_FILE}
    
    private static Storage STORAGE = null;
    private  static String databaseLocatorPath = null;
    /**
     * DEBUG -- following DatabasePath variables updated from private to protected scope
     * which enables access from the concrete store class
     */
    protected String PMSStorePath = null;
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
                //result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                //result = SQLExpressStore.getInstance();
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
    /*
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
    */
    
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
                //result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                //result = SQLExpressStore.getInstance();
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
    /*
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
    */
    private static IStoreAction FACTORY_FOR_STORE_ACTION()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        //IS_PMS_STORE_CURRENTLY_UNDER_CONSTRUCTION = true;
        IStoreAction result = null;
        switch (STORAGE){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            case POSTGRES:
                //result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                //result = SQLExpressStore.getInstance();
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
                //result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                //result = SQLExpressStore.getInstance();
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
    
    protected String getPMSStorePath(){
        return PMSStorePath;
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
    
    protected void setPMSStorePath(String path) throws StoreException{
        PMSStorePath = path;
        FACTORY_FOR_TARGETS_STORE();
    }
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
    /*
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
    */
    
    public static IAppointmentsStoreAction FACTORY(IAppointments appointments) throws StoreException{
        return FACTORY_FOR_APPOINTMENTS_STORE();
    }
    
    /*
    public static IMigrationStoreAction FACTORY(ITable table)throws StoreException{
        return FACTORY_FOR_MIGRATION_STORE();
    }
    */
    
    public static IPatientsStoreAction FACTORY(IPatients patients) throws StoreException{
        return FACTORY_FOR_PATIENTS_STORE();
    }
    
    public static IStoreAction FACTORY (EntityStoreType entity) throws StoreException{
        return FACTORY_FOR_STORE_ACTION();
    }
   
   
    public static IStoreAction FACTORY(IEntity entity)throws StoreException{
        return FACTORY_FOR_STORE_ACTION();
    }

    
    public static ITargetsStoreAction FACTORY(IStoreManager manager)throws StoreException{
        return FACTORY_FOR_TARGETS_STORE();
    }

}

