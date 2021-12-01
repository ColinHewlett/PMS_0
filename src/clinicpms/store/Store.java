/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.Store.TargetDatabase;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.IStore;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;


/**
 *
 * @author colin
 */
public abstract class Store implements IStore {
    
    public enum ExceptionType {  APPOINTEE_NOT_FOUND_EXCEPTION,
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
    
    public enum MigrationMethod   { APPOINTMENTS_COUNT,
                                    APPOINTMENT_TABLE_CREATE,
                                    APPOINTMENT_TABLE_DROP,
                                    APPOINTMENT_TABLE_POPULATE,
                                    APPOINTMENT_TABLE_INTEGRITY_CHECK,
                                    APPOINTMENT_START_TIMES_NORMALISED,
                                    PATIENTS_COUNT,
                                    PATIENT_TABLE_CREATE,
                                    PATIENT_TABLE_DROP,
                                    PATIENT_TABLE_POPULATE,
                                    PATIENT_TABLE_TIDY
                                  }
    
    public enum Storage{ACCESS, 
                        CSV,
                        POSTGRES,
                        SQL_EXPRESS,
                        UNDEFINED_DATABASE}
    
    protected enum AppointmentSQL   {
                            APPOINTMENTS_COUNT,
                            CREATE_APPOINTMENT,
                            DELETE_APPOINTMENT_WITH_KEY,
                            DELETE_APPOINTMENTS_WITH_PATIENT_KEY,
                            READ_APPOINTMENTS,
                            READ_APPOINTMENTS_FOR_DAY,
                            READ_APPOINTMENTS_FROM_DAY,
                            READ_APPOINTMENTS_FOR_PATIENT,
                            READ_APPOINTMENT_WITH_KEY,
                            READ_HIGHEST_KEY,
                            UPDATE_APPOINTMENT}

    protected enum PatientSQL   {CREATE_PATIENT,
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
    
    protected enum MigrationAppointmentSQL {
                            APPOINTMENT_TABLE_CREATE,
                            APPOINTMENT_TABLE_DROP,
                            APPOINTMENT_START_TIME_NORMALISED}
    
    protected enum MigrationPatientSQL {
                            PATIENT_TABLE_CREATE,
                            PATIENT_TABLE_DROP}
    
    public enum TargetDatabase{MIGRATION_DB,
                               PMS_DB}
    
    //public enum TargetConnection{CONNECTION_MIGRATION_DB, CONNECTION_PMS_DB}
    
    
    private static Storage storage = null;
    private static String databaseLocatorPath = null;
    private static String migrationDatabasePath = null;
    private static String pmsDatabasePath = null;
    private static TargetDatabase targetConnection = null;
    protected static Store instance;
    //private TargetsDatabase targetsDatabase = null;
    
    /**
     * uses the initialised storage type to create a brand new instance of 
     * the selected storage type
     * @return
     * @throws StoreException 
     */
    public static IStore factory()throws StoreException{
        IStore result = null;
        switch (getStorageType()){
            case ACCESS: 
                result = AccessStore.getInstance();
                break;
            /*
            case CSV:
                result = CSVStore.getInstance();
                break;
            */
            case POSTGRES:
                result = PostgreSQLStore.getInstance();
                break;
            case SQL_EXPRESS:
                result = SQLExpressStore.getInstance();
                break;
        }
        return result;
    }
    
    /**
     * The TargetConnection getter/setter maintains at all times which datatabase,
     * the PMS or Migration database, should be accessed by the app
     * @param value 
     */
    public static void setTargetConnection(TargetDatabase value){
        targetConnection = value;
    }
    
    public static TargetDatabase getTargetConnection(){
        return targetConnection;
    }
    
    /**
     * initialise by main method via a call to TARGETS_DATABASE OS environment variable
     * @return 
     */
    public static String getDatabaseLocatorPath(){
        return databaseLocatorPath;
    }
    
    public static void setDatabaseLocatorPath(String path){
        databaseLocatorPath = path;
    } 
    
    public static String getMigrationDatabasePath(){
        return migrationDatabasePath;
    }
    
    public static void setMigrationDatabasePath(String path){
        migrationDatabasePath = path;
    }
    
    public static String getPMSDatabasePath(){
        return pmsDatabasePath;
    }
    
    /**
     * update logged 22/11/2021 08:52
     * the target PMS database path is initialised
     * -- create a new instance of the selected Store using the factory method
     * -- this to ensure the new database path takes immediate effect
     * @param path 
     */
    public static void setPMSDatabasePath(String path) throws StoreException{
        if (instance != null) instance = null;
        pmsDatabasePath = path;
        factory();
    }
    
    public static Storage getStorageType(){
        return storage;
    } 
    
    public static void setStorageType(Storage type){
        storage = type;
    }

    /*
    public static TargetsDatabase getTargetsDatabase() throws StoreException{
        return new TargetsDatabase();
    }
    
    public static class TargetsDatabase{
        private Connection connection = null;
        private String message = null;
        
        public TargetsDatabase()throws StoreException{
            connection = getConnection();
            //setDatabaseURL(this.read(1));
            Store.setMigrationDatabasePath(this.read("MIGRATION_DB"));
            Store.setPMSDatabasePath(this.read("PMS_DB"));
        }

        private Connection getConnection()throws StoreException{
            String url = "jdbc:ucanaccess://" + Store.getDatabaseLocatorPath() + ";showSchema=true";
            if (this.connection == null){
                try{
                    this.connection = DriverManager.getConnection(url);  
                }
                catch (SQLException ex){
                    message = ex.getMessage();
                    throw new StoreException("SQLException message -> " + message +"\n"
                            + "StoreException message -> raised trying to connect to the DbLocationStore database",
                    Store.ExceptionType.SQL_EXCEPTION);
                }
            }
            return this.connection;
        }

        public void closeConnection()throws StoreException{
            try{
                if (this.connection!=null){
                    this.connection.close();
                }
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                message = message + "StoreException -> raised in ProgreSQLStore::closeConnection()";
                throw new StoreException(message, Store.ExceptionType.SQL_EXCEPTION);
            }
        }

        public String read(String db)throws StoreException{
            String result = null;
            String sql = "Select location from Target WHERE db = ?;";
            try{
                PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                preparedStatement.setString(1, db);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()){
                    result = rs.getString("location");
                }
                return result;
            }
            catch (SQLException ex){
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                 + "StoreException message -> exception raised during DbLocationStore::read() query",
                ExceptionType.SQL_EXCEPTION);
            }

        }
        
        public String update(String updatedLocation, String db)throws StoreException{
            String sql = "UPDATE Target SET location = ? WHERE db = ?;";
            try{
                PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                preparedStatement.setString(1, updatedLocation);
                preparedStatement.setString(2, db);
                preparedStatement.executeUpdate();
                return read(db);
            }
            catch (SQLException ex){
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                 + "StoreException message -> exception raised during DbLocationStore::update statement",
                ExceptionType.SQL_EXCEPTION);
            }
        }
    }
    
    */
}

