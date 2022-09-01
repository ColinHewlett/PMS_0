/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.IStoreClient;

/**
 *
 * @author colin
 */
public abstract class Store implements IStoreActions {
    
    protected enum ConnectionMode{ AUTO_COMMIT_OFF, AUTO_COMMIT_ON}

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
 
    protected enum PMSSQL   {
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
                                DELETE_PATIENT_NOTIFICATION,
                                DROP_PATIENT_NOTIFICATION_TABLE,
                                INSERT_PATIENT_NOTIFICATION,
                                READ_PATIENT_NOTIFICATION,
                                READ_PATIENT_NOTIFICATIONS,
                                READ_UNACTIONED_PATIENT_NOTIFICATIONS,
                                READ_PATIENT_NOTIFICATION_NEXT_HIGHEST_KEY,
                                READ_PATIENT_NOTIFICATIONS_FOR_PATIENT,
                                UPDATE_PATIENT_NOTIFICATION,
                                
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

                                /**
                                 * following define data migration SQL statements
                                 */
                                READ_CSV_APPOINTMENT_FILE_LOCATION,
                                READ_CSV_PATIENT_FILE_LOCATION,
                                READ_PMS_STORE_LOCATION,
                                
                                UPDATE_CSV_APPOINTMENT_FILE_LOCATION,
                                UPDATE_CSV_PATIENT_FILE_LOCATION,
                                UPDATE_PMS_STORE_LOCATION
    
                                }

    private static Storage STORAGE = null;
    private  static String databaseLocatorPath = null;
    /**
     * DEBUG -- following DatabasePath variables updated from private to protected scope
     * which enables access from the concrete store class
     */
    protected String PMSStorePath = null;
    protected  String migrationDatabasePath = null;
    protected  String pmsDatabasePath = null;
    protected  String appointmentCSVPath = null;
    protected  String patientCSVPath = null;
    protected static Store INSTANCE = null;
    //protected static boolean IS_APPOINTMENTS_STORE_ACTION = false;
    //protected static boolean IS_MIGRATION_STORE_ACTION = false;
    //protected static boolean IS_PATIENTS_STORE_ACTION = false;
    //protected static boolean IS_PMS_STORE_ACTION = false;
    //protected static boolean IS_TARGETS_STORE_ACTION = false;
    
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
    public static String GET_STORAGE_TYPE(){
        return STORAGE.toString();
    } 
    
    /**
     * initialised on first entry to FACTORY method
     * @return String representing the path to the database locator store 
     */
    protected  String getDatabaseLocatorPath(){
        return databaseLocatorPath;
    }
    
    public static IStoreActions FACTORY (IStoreClient client) throws StoreException{
        return (IStoreActions)FACTORY();
    }

    private static Object FACTORY()throws StoreException{
        INITIALISE_DATABASE_LOCATOR_PATH();
        INITIALISE_STORAGE_TYPE();
        Object result = null;
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
        return result;
    }

}

