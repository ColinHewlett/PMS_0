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
}
