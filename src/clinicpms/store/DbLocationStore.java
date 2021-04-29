/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

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

/**
 *
 * @author colin
 */
public class DbLocationStore extends Store{
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

    private static DbLocationStore instance;
    private Connection connection = null;
    private String message = null;
    
    String databaseURL = "jdbc:postgresql://localhost/ClinicPMS?user=colin";
    
    DateTimeFormatter ymdFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private void setConnection(Connection con){
        this.connection = con;
    }
    private Connection getConnection()throws StoreException{
        String url = "jdbc:ucanaccess://" + Store.getDatabaseLocatorPath() + ";showSchema=true";
        if (connection == null){
            try{
                connection = DriverManager.getConnection(url);
            }
            catch (SQLException ex){
                message = ex.getMessage();
                throw new StoreException("SQLException message -> " + message +"\n"
                        + "StoreException message -> raised trying to connect to the DbLocationStore database",
                Store.ExceptionType.SQL_EXCEPTION);
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
            throw new StoreException(message, Store.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    public DbLocationStore()throws StoreException{
        connection = getConnection();
    }
    
    public static DbLocationStore getInstance()throws StoreException{
        DbLocationStore result = null;
        if (instance == null) result = new DbLocationStore();
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
    
    public String read()throws StoreException{
        String result = null;
        String sql = "Select Location from LOCATION WHERE id = 1;";
        try{
            PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
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
    
    public String update(String updatedLocation)throws StoreException{
        String sql = "UPDATE LOCATION SET Location = ? WHERE ID = 1;";
        try{
            PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
            preparedStatement.setString(1, updatedLocation);
            preparedStatement.executeUpdate();
            return read();
        }
        catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
             + "StoreException message -> exception raised during DbLocationStore::update statement",
            ExceptionType.SQL_EXCEPTION);
        }
    }
}
