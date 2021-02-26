/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;


/**
 *
 * @author colin
 */
public class AccessStore extends Store {
    public enum AppointmentQuery   {
                            FETCH_APPOINTMENTS_FOR_DAY,
                            FETCH_APPOINTMENTS_FOR_PATIENT}
    public enum PatientQuery   {FETCH_PATIENT_WITH_KEY}

    private static AccessStore instance;
    private Connection connection = null;
    private String message = null;
    
    String databaseURL = "jdbc:ucanaccess://c://users//colin//OneDrive//documents"
            + "//Databases//Access//ClinicPMS.accdb;showSchema=true";
    
    private Connection getConnection()throws StoreException{
        Connection result = null;
        if (connection == null){
            try{
                result = DriverManager.getConnection(databaseURL);
            }
            catch (SQLException ex){
                message = ex.getMessage();
                throw new StoreException("SQLException message -> " + message +"\n"
                        + "StoreException message -> raised trying to connect to the Access database",
                ExceptionType.SQL_EXCEPTION);
            }
        }
        else result = connection;
        return result;
    }
    
    public AccessStore()throws StoreException{
        connection = getConnection();
    }
    public static AccessStore getInstance() throws StoreException{
        AccessStore result = null;
        if (instance == null) result = new AccessStore();
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
        ArrayList<Patient> patients = 
                runQuery(PatientQuery.FETCH_PATIENT_WITH_KEY,p,  new ArrayList<Patient>());
        return patients.get(0);
    }
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException{
        ArrayList<Appointment> appointments = 
                runQuery(AppointmentQuery.FETCH_APPOINTMENTS_FOR_PATIENT,day, new ArrayList<Appointment>());
        return appointments;
    }
    public ArrayList<Appointment> readAppointments(Patient p, Appointment.Category c) throws StoreException{
        ArrayList<Appointment> appointments = 
                runQuery(AppointmentQuery.FETCH_APPOINTMENTS_FOR_PATIENT,p, new ArrayList<Appointment>());
        return appointments;
    }
    public Patient update(Patient p) throws StoreException{
        return null;
    }
    public Appointment update(Appointment a) throws StoreException{
        return null;
    }
    private ArrayList<Patient> getPatientsFromRS(ResultSet rs) throws StoreException{
        ArrayList<Patient> result = new ArrayList<>();
        try{
            if (!rs.wasNull()){
                while(rs.next()){
                    Patient patient = new Patient();
                    int key = rs.getInt("key");
                    String title = rs.getString("title");
                    String forenames = rs.getString("forenames");
                    String surname = rs.getString("surname");
                    String line1 = rs.getString("line1");
                    String line2 = rs.getString("line2");
                    String town = rs.getString("town");
                    String county = rs.getString("county");
                    String postcode = rs.getString("postcode");
                    String phone1 = rs.getString("phone1");
                    String phone2 = rs.getString("phone2");
                    String gender = rs.getString("gender");
                    LocalDate dob = rs.getObject("dob", LocalDate.class);
                    int recallFrequency = rs.getInt("recallFrequency");
                    LocalDate recallDate = rs.getObject("recallDate", LocalDate.class);
                    boolean isGuardianAPatient = rs.getBoolean("isGuardianAPatient");
                    Integer guardianKey = rs.getInt("guardianKey");  

                    patient.setKey(key);
                    patient.getName().setTitle(title);
                    patient.getName().setForenames(forenames);
                    patient.getName().setSurname(surname);
                    patient.getAddress().setLine1(line1);
                    patient.getAddress().setLine2(line2);
                    patient.getAddress().setTown(town);
                    patient.getAddress().setCounty(county);
                    patient.getAddress().setPostcode(postcode);
                    patient.setGender(gender);
                    patient.setDOB(dob);
                    patient.setPhone1(phone1);
                    patient.setPhone2(phone2);
                    patient.getRecall().setDentalDate(recallDate);
                    patient.getRecall().setDentalFrequency(recallFrequency);
                    patient.setIsGuardianAPatient(isGuardianAPatient); 
                    if (patient.getIsGuardianAPatient()){
                        if (guardianKey>0){
                            Patient p = new Patient(guardianKey);
                            patient.setGuardian(p);
                        }
                    }
                    result.add(patient);
                }
            }
        }
        catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n",
                    ExceptionType.SQL_EXCEPTION);
        }
        return result;
    }
    private ArrayList<Appointment> getAppointmentsFromRS(ResultSet rs)throws StoreException{
        ArrayList<Appointment> result = new ArrayList<>();
        try{
            if (!rs.wasNull()){
                while(rs.next()){
                    int key = rs.getInt("Key");
                    LocalDateTime start = rs.getObject("Start", LocalDateTime.class);
                    Duration duration = Duration.ofMinutes(rs.getLong("Duration"));
                    String notes = rs.getString("Notes"); 
                    Appointment appointment = new Appointment();
                    appointment.setKey(key);
                    appointment.setStart(start);
                    appointment.setDuration(duration);
                    appointment.setNotes(notes);
                    result.add(appointment);
                }
            }  
        }
        catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n",
                    ExceptionType.SQL_EXCEPTION);
        }
        return result;
    }
    
    private ArrayList<Patient> runQuery(
            PatientQuery q, Object entity, ArrayList<Patient> patients)throws StoreException{
        Patient patient = (Patient)entity;
        ArrayList<Patient> records = patients;
        String sql =
                switch (q){
                    case FETCH_PATIENT_WITH_KEY ->
                "SELECT key, title, forenames, surname, line1, line2, "
                + "town, county, postcode, gender, dob, isGuardianAPatient, "
                + "phone1, phone2, recallFrequency, recallDate, notes, guardianKey "
                + "FROM Patient "
                + "WHERE key=?;";};
        try{
            PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
            preparedStatement.setLong(1, patient.getKey());
            ResultSet rs = preparedStatement.executeQuery();
            records = getPatientsFromRS(rs);
        }
        catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a FETCH_PATIENT_WITH_KEY query",
                    ExceptionType.SQL_EXCEPTION);
        }
        return records;            
    }
    private ArrayList<Appointment> runQuery(
            AppointmentQuery q, Object entity, ArrayList<Appointment> appointments) throws StoreException{
        ArrayList<Appointment> records = appointments;
        String sql = null;
        sql = 
                switch (q){
                    case FETCH_APPOINTMENTS_FOR_PATIENT ->
                "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes " +
                "FROM Appointment AS a " +
                "WHERE a.PatientKey = ? " +
                "ORDER BY a.Start DESC";
                        
                    case FETCH_APPOINTMENTS_FOR_DAY ->
                "select *"
                + "from appointment as a "
                + "where DatePart(\"yyyy\",a.start) = ? "
                + "AND  DatePart(\"m\",a.start) = ? "
                + "AND  DatePart(\"d\",a.start) = ?;";                
        };
        switch (q){
            case FETCH_APPOINTMENTS_FOR_PATIENT -> {
                Patient patient = (Patient)entity;
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patient.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    records = getAppointmentsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a FETCH_APPOINTMENTS_FOR_PATIENT query",
                    ExceptionType.SQL_EXCEPTION);
                }
            }
            case FETCH_APPOINTMENTS_FOR_DAY -> {
                LocalDate day = (LocalDate)entity;
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, day.getYear());
                    preparedStatement.setInt(2, day.getMonthValue());
                    preparedStatement.setInt(3, day.getDayOfMonth());
                    ResultSet rs = preparedStatement.executeQuery();
                    records = getAppointmentsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a FETCH_APPOINTMENTS_FOR_DAY query",
                    ExceptionType.SQL_EXCEPTION);
                }
            }
        }
        return records;
    }

}
