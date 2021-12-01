/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store.stores;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Dictionary;

/**
 *
 * @author colin
 */
public class PostgreSQLStore extends Store {
    
    public enum AppointmentSQL   {
                            CREATE_APPOINTMENT,
                            DELETE_APPOINTMENT_WITH_KEY,
                            READ_APPOINTMENTS_FOR_DAY,
                            READ_APPOINTMENTS_FROM_DAY,
                            READ_APPOINTMENTS_FOR_PATIENT,
                            READ_APPOINTMENT_WITH_KEY,
                            READ_HIGHEST_KEY,
                            UPDATE_APPOINTMENT}

    public enum PatientSQL   {CREATE_PATIENT,
                                READ_ALL_PATIENTS,
                                READ_HIGHEST_KEY,
                                READ_PATIENT_WITH_KEY,
                                UPDATE_PATIENT}

    private static PostgreSQLStore instance;
    private Connection connection = null;
    private String message = null;
    private AccessStore.MigrationManager migrationManager = null;
    private TargetsDatabaseManager targetsDatabaseManager = null;
    
    String databaseURL = "jdbc:postgresql://localhost/ClinicPMS?user=colin";
    
    DateTimeFormatter ymdFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private void setConnection(Connection con){
        this.connection = con;
    }
    private Connection getConnection()throws StoreException{
        Connection result = null;
        if (connection == null){
            try{
                connection = DriverManager.getConnection(databaseURL);
            }
            catch (SQLException ex){
                message = ex.getMessage();
                throw new StoreException("SQLException message -> " + message +"\n"
                        + "StoreException message -> raised trying to connect to the PostgreSQL database",
                ExceptionType.SQL_EXCEPTION);
            }
        }
        return connection;
    }
    
    public void closeConnection()throws StoreException{
        try{
            if (connection!=null){
                connection.close();
            }
        }
        catch (SQLException ex){
            message = "SQLException -> " + ex.getMessage() + "\n";
            message = message + "StoreException -> raised in ProgreSQLStore::closeConnection()";
            throw new StoreException(message, ExceptionType.SQL_EXCEPTION);
        }
    }
    
    public PostgreSQLStore()throws StoreException{
        connection = getConnection();
    }
    
    public static PostgreSQLStore getInstance()throws StoreException{
        PostgreSQLStore result = null;
        if (instance == null) result = new PostgreSQLStore();
        else result = instance;
        return result;
    }
    public Appointment create(Appointment a) throws StoreException{
        return null;
    }
    public Patient create(Patient p) throws StoreException{
        return null;
    }
    public void delete(Appointment a) throws StoreException{
        
    }
    public void delete(Patient p) throws StoreException{
        
    }
    public Appointment read(Appointment a) throws StoreException{
        return null;
    }
    public Patient read(Patient p) throws StoreException{
        return null;
    }
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException{
        return null;
    }
    public ArrayList<Appointment> readAppointments(Patient p, Appointment.Category c) throws StoreException{
        return null;
    }
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException{
        return null;
    }
    public ArrayList<Patient> readPatients() throws StoreException{
        return null;
    }
    public Patient update(Patient p) throws StoreException{
        return null;
    }
    public Appointment update(Appointment a) throws StoreException{
        return null;
    }
    
    @Override
    public Dictionary<String,Boolean> readSurgeryDays() throws StoreException{
        return null;
    }
    
    @Override
    public Dictionary<String,Boolean> updateSurgeryDays(Dictionary<String,Boolean> d) throws StoreException{
        return null;
    }
    
    public class TargetsDatabaseManager implements ITargetsDatabaseManager{
        private Connection connection = null;
        private String message = null;
        
        public TargetsDatabaseManager()throws StoreException{
            connection = getConnection();
            Store.setMigrationDatabasePath(this.read(TargetDatabase.MIGRATION_DB));
            Store.setPMSDatabasePath(this.read(TargetDatabase.PMS_DB));
        }
        
        /**
         * Store.getDatabaseLocatorPath() initialised using TARGETS_DATABASE environment variable (main method)
         * @return
         * @throws StoreException 
         */
        public Connection getConnection()throws StoreException{
            String url = "jdbc:ucanaccess://" + Store.getDatabaseLocatorPath() + ";showSchema=true";
            if (this.connection == null){
                try{
                    this.connection = DriverManager.getConnection(url);  
                }
                catch (SQLException ex){
                    message = ex.getMessage();
                    throw new StoreException("SQLException message -> " + message +"\n"
                            + "StoreException message -> raised trying to connect to the DbLocationStore database using AccessStore",
                    ExceptionType.SQL_EXCEPTION);
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
                message = message + "StoreException -> raised in AccessStore::closeConnection()";
                throw new StoreException(message, ExceptionType.SQL_EXCEPTION);
            }
        }

        /**
         * fetches the database path in the specified row of the targets database (DbLocation.accb)
         * @param db, Integer
         * @return String defining the path to the selected database file
         * @throws StoreException 
         */
        public String read(TargetDatabase db)throws StoreException{
            String result = null;
            String sql = "Select location from Target WHERE db = ?;";
            try{
                PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                preparedStatement.setString(1, db.toString());
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
        
        public String update(String updatedLocation, TargetDatabase db)throws StoreException{
            String sql = "UPDATE Target SET location = ? WHERE db = ?;";
            try{
                PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                preparedStatement.setString(1, updatedLocation);
                preparedStatement.setString(2, db.toString());
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
    
    @Override
    public IMigrationManager getMigrationManager(){
        return null;
    }
    
    public TargetsDatabaseManager getTargetsDatabaseManager() throws StoreException{
        if (targetsDatabaseManager == null) targetsDatabaseManager = new TargetsDatabaseManager();
        return targetsDatabaseManager;
    }
}
