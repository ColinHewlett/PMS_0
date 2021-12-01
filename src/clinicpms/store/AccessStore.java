/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.Store;
import clinicpms.store.ITargetsDatabaseManager;
import clinicpms.store.IMigrationManager;
import static clinicpms.controller.ViewController.displayErrorMessage;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JOptionPane;


/**
 *
 * @author colin
 */
public class AccessStore extends Store {
    

    //private static AccessStore instance;#
    //private static Store instance;
    private Connection connection = null;
    private Connection migrationConnection = null;
    private Connection pmsConnection = null;
    private String message = null;
    private static String databaseURL = null;
    /*
    String databaseURL = "jdbc:ucanaccess://c://users//colin//OneDrive//documents"
            + "//Databases//Access//ClinicPMS.accdb;showSchema=true";
    */
    
    private MigrationManager migrationManager = null;
    private TargetsDatabaseManager targetsDatabaseManager = null;
    
    DateTimeFormatter ymdFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private void setConnection(Connection con){
        this.connection = con;
    }
    
    /**
     * The inherited Store.getTargetConnection() is used to fetch which connection is currently active
     * -- if target connection has not yet been defined a connection the PMS database is assumed
     * @return Connection object
     * @throws StoreException if an SQLException is raised by the connection attempt
     */
    private Connection getConnection()throws StoreException{
        String url = null;
        Connection result = null;
        if (getTargetConnection()!=null){
            switch(getTargetConnection()){
                case MIGRATION_DB:
                    if (getMigrationDatabasePath() != null){
                        url = "jdbc:ucanaccess://" + getMigrationDatabasePath() + ";showSchema=true";
                    }
                    else{
                        new TargetsDatabaseManager();
                        url = "jdbc:ucanaccess://" + getMigrationDatabasePath() + ";showSchema=true";
                    }
                    if (migrationConnection == null){
                        try{
                            migrationConnection = DriverManager.getConnection(url);
                            result = migrationConnection;
                        }
                        catch (SQLException ex){
                            message = ex.getMessage();
                            throw new StoreException("SQLException message -> " + message +"\n"
                                    + "StoreException message -> raised trying to connect to the Access migration database",
                            ExceptionType.SQL_EXCEPTION);
                        }
                    }
                    result = migrationConnection;
                    break;
                case PMS_DB:
                    if (getPMSDatabasePath() != null){
                        url = "jdbc:ucanaccess://" + getPMSDatabasePath() + ";showSchema=true";
                    }
                    else{
                        new TargetsDatabaseManager();
                        url = "jdbc:ucanaccess://" + getPMSDatabasePath() + ";showSchema=true";
                    }
                    if (pmsConnection == null){
                        try{
                            pmsConnection = DriverManager.getConnection(url);
                            result = pmsConnection;
                        }
                        catch (SQLException ex){
                            message = ex.getMessage();
                            throw new StoreException("SQLException message -> " + message +"\n"
                                    + "StoreException message -> raised trying to connect to the Access PMS database",
                            ExceptionType.SQL_EXCEPTION);
                        }
                    }
                    else result = pmsConnection;
                    break;
            }
        }
        else{
            if (getPMSDatabasePath() != null){
                url = "jdbc:ucanaccess://" + getPMSDatabasePath() + ";showSchema=true";
            }
            else{
                new TargetsDatabaseManager();
                url = "jdbc:ucanaccess://" + getPMSDatabasePath() + ";showSchema=true";
            }
            if (pmsConnection == null){
                try{
                    pmsConnection = DriverManager.getConnection(url);
                    result = pmsConnection;
                }
                catch (SQLException ex){
                    message = ex.getMessage();
                    throw new StoreException("SQLException message -> " + message +"\n"
                            + "StoreException message -> raised trying to connect to the Access PMS database",
                    ExceptionType.SQL_EXCEPTION);
                }
            } 
            else result = pmsConnection;
        }
        return result;
    }
    
    public void closeConnection()throws StoreException{
        String connectionName = null;
        try{
            switch(getTargetConnection()){
                case MIGRATION_DB:
                    if (migrationConnection!=null) {
                        connectionName = "migration database";
                        migrationConnection.close();
                    }
                case PMS_DB:
                    if (migrationConnection!=null) {
                        connectionName = "PMS database";
                        pmsConnection.close();
                    }
            }
        }
        catch (SQLException ex){
            message = "SQLException -> " + ex.getMessage() + "\n";
            message = message + "StoreException -> raised in AccessStore::closeConnection() to close the " + connectionName;
            throw new StoreException(message, ExceptionType.SQL_EXCEPTION);
        }
    }
    
    public void setDatabaseURL(String url){
        databaseURL = url;
    }
    
    public String getDatabaseURL()throws StoreException{
        return databaseURL;
    }
    
    /**
     * 22/11/2021 19:48 update
     * -- TargetsDatabase inner class removed 
     * @return
     * @throws StoreException 
     */
    /*
    public TargetsDatabase getTargetsDatabase() throws StoreException{
        return new TargetsDatabase();
    }
    */
    
    /**
     * The constructor has one task only -> to fetch a connection to the database
     * -- different instances of the app will maintain separate connections to the database
     * @throws StoreException 
     */
    public AccessStore()throws StoreException{
        connection = getConnection();
    }
    
    /**
     * Singleton pattern implemented
     * -- only if the current instance variable in Store is null is a new
     * -- instance of the AccessStore type constructed
     * @return
     * @throws StoreException 
     */
    public static AccessStore getInstance() throws StoreException{
        AccessStore result = null;
        if (instance == null) {
            result = new AccessStore();
            instance = result;
        }
        else result = (AccessStore)instance;
        
        return result;
    }
    
    @Override
    public Appointment create(Appointment a) throws StoreException{
        ArrayList<Appointment> value = null;
        Appointment appointment = null;
        Patient patient = null;
        message = "";
        try{//turn off jdbc driver's auto commit after each SQL statement
            getConnection().setAutoCommit(false);
            value = runSQL(AppointmentSQL.READ_HIGHEST_KEY, 
                    new Appointment(), new ArrayList<Appointment>());
            a.setKey(value.get(0).getKey()+1);
            runSQL(AppointmentSQL.CREATE_APPOINTMENT, a, new ArrayList<Appointment>());
            value = runSQL(AppointmentSQL.READ_APPOINTMENT_WITH_KEY, a, new ArrayList<Appointment>());
            try{
                if (value.isEmpty()){
                    message = "StoreException raised in method AccessStore::create(Appointment a)\n"
                                    + "Reason -> newly created appointment record could not be found";
                    getConnection().rollback();
                    throw new StoreException(message,ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                else {
                    appointment = value.get(0);
                    patient = new Patient(appointment.getPatient().getKey());
                    //patient = create(patient);
                    patient = read(patient);
                    /**
                     * 09/11/2021 16:53 logged "bug"
                     * patient = create(patient); replaced with patient = read(patient)
                     */
                    appointment.setPatient(patient);
                    getConnection().commit();
                }
            }
            catch (SQLException ex){
                message = message + "SQLException message -> " + ex.getMessage() +"\n";
                try{
                    connection.setAutoCommit(true);
                }
                catch(SQLException exe){
                    message = message + "SQLException message -> " + ex.getMessage() +"\n"; 
                }
                finally{
                    throw new StoreException(
                        message + "StoreException raised in method AccessStore::create(Appointment a)\n"
                                + "Cause -> unexpected effect when transaction/auto commit statement executed",
                        ExceptionType.SQL_EXCEPTION);
                }
            }
        }
        catch (SQLException ex){
            message = "SQLException message -> " + ex.getMessage() +"\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::create(Appointment a)\n"
                            + "Reason -> unexpected effect getConnection().setAutoCommit(false) executed",
                    ExceptionType.SQL_EXCEPTION);
        }
        finally{
            try{
                getConnection().setAutoCommit(false);
                return appointment;
            }
            catch (SQLException ex){
                 message = "SQLException message -> " + ex.getMessage() +"\n";
                 throw new StoreException(
                    message + "StoreException raised in method AccessStore::create(Appointment a)\n"
                            + "Reason -> unexpected effect getConnection().setAutoCommit(false) executed",
                    ExceptionType.SQL_EXCEPTION);
            }
        }
    }
    @Override
    /**
     * --adding transaction statements explicitly to SQL used requires catching the SQLException potentially thrown
     * --note: the jdbc driver uses transactions anyway, and auto commits after every SQL statement it executes
     * --strategy of Store modules is that generated SQLExceptions are wrapped as StoreExceptions
     * --the explicit transaction potentially generates additional SQLExceptions in the current method
     * --so nested try-catch blocks are used to wrap the additional exceptions as StoreExceptions if generated
     */
    public Patient create(Patient p) throws StoreException{
        ArrayList<Patient> value = null;
        Patient patient = null;
        message = "";
        try{//turn off jdbc driver's auto commit after each SQL statement
            getConnection().setAutoCommit(false); 
            value = runSQL(PatientSQL.READ_HIGHEST_KEY,new Patient(), new ArrayList<Patient>());
            p.setKey(value.get(0).getKey()+1);
            runSQL(PatientSQL.CREATE_PATIENT, p, new ArrayList<>());
            value = runSQL(PatientSQL.READ_PATIENT_WITH_KEY, p, new ArrayList<Patient>());
            try{
                if (value.isEmpty()){
                    message = "StoreException raised in method AccessStore::create(Patient p)\n"
                                    + "Reason -> newly created patient record could not be found";
                    getConnection().rollback();
                    throw new StoreException(message,ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                else {
                    patient = value.get(0);
                    getConnection().commit();
                }
            }
            catch (SQLException ex){
                message = message + "SQLException message -> " + ex.getMessage() +"\n";
                try{
                    connection.setAutoCommit(true);
                }
                catch(SQLException exe){
                    message = message + "SQLException message -> " + ex.getMessage() +"\n"; 
                }
                finally{
                    throw new StoreException(
                        message + "StoreException raised in method AccessStore::create(Patient p)\n"
                                + "Cause -> unexpected effect when transaction/auto commit statement executed",
                        ExceptionType.SQL_EXCEPTION);
                }
            } 
        }
        catch (SQLException ex){
            message = "SQLException message -> " + ex.getMessage() +"\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::create(Patient p)\n"
                            + "Reason -> unexpected effect getConnection().setAutoCommit(false) executed",
                    ExceptionType.SQL_EXCEPTION);
        }
        finally{
            try{
                getConnection().setAutoCommit(false);
                return patient;
            }
            catch (SQLException ex){
                 message = "SQLException message -> " + ex.getMessage() +"\n";
                 throw new StoreException(
                    message + "StoreException raised in method AccessStore::create(Patient p)\n"
                            + "Reason -> unexpected effect getConnection().setAutoCommit(false) executed",
                    ExceptionType.SQL_EXCEPTION);
            }
        }
    }
    public void delete(Appointment a) throws StoreException{
        runSQL(AppointmentSQL.DELETE_APPOINTMENT_WITH_KEY, a, new ArrayList<Appointment>());
        ArrayList value = runSQL(AppointmentSQL.READ_APPOINTMENT_WITH_KEY, a, new ArrayList<Appointment>());
        if (value.size()!=0){
            String message = 
                    "Unsuccesful attempt to delete appointment record (key = "
                    + String.valueOf(a.getKey()) + ")";
            throw new StoreException(message, ExceptionType.KEY_FOUND_EXCEPTION);
        }
    }
    public void delete(Patient p) throws StoreException{
        
    }
    
    
    
    public Appointment read(Appointment a) throws StoreException{
        ArrayList<Patient> patients = null;
        ArrayList<Appointment> appointments = 
                runSQL(AppointmentSQL.READ_APPOINTMENT_WITH_KEY,a,  new ArrayList<Appointment>());
        Appointment appointment = appointments.get(0);
        if (appointment.getPatient()!=null){
            patients = runSQL(PatientSQL.READ_PATIENT_WITH_KEY,appointment.getPatient(), new ArrayList<Patient>());
            Patient patient = patients.get(0);
            appointment.setPatient(patient);
        }
        return appointment;
    }
    @Override
    public Patient read(Patient p) throws StoreException{
        try{//ensure auto commit setting switched on
            if (!getConnection().getAutoCommit()){
                getConnection().setAutoCommit(true);
            }
            ArrayList<Patient> patients = 
                runSQL(PatientSQL.READ_PATIENT_WITH_KEY,p,  new ArrayList<Patient>());
            if (patients.isEmpty()){//patient with this key not found
                throw new StoreException(
                        "Could not find patient with key = " + String.valueOf(p.getKey()), 
                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                Patient patient = patients.get(0);
                if (patient.getGuardian()!=null){
                    patients = runSQL(PatientSQL.READ_PATIENT_WITH_KEY, patient.getGuardian(), new ArrayList<Patient>());  
                }
                patient.setGuardian(patients.get(0));
                return patient;
            }
        }
        catch (SQLException ex){
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::read(Patient p)",
            ExceptionType.SQL_EXCEPTION);
        }
    }
    /**
     * 
     * @return
     * @throws StoreException 
     */
    public ArrayList<Appointment> readAppointments() throws StoreException{
        try{//ensure auto commit setting switched on
            if (!getConnection().getAutoCommit()){
                getConnection().setAutoCommit(true);
            }
            ArrayList<Appointment> appointments = 
                runSQL(AppointmentSQL.READ_APPOINTMENTS, new Object(),new ArrayList<Appointment>());
            Iterator<Appointment> it = appointments.iterator();
            while(it.hasNext()){
                Appointment appointment = it.next();
                Patient p = read(appointment.getPatient());
                appointment.setPatient(p);
            }
            return appointments;
        }
        catch (SQLException ex){
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::readAppointments(LocalDate d)",
            ExceptionType.SQL_EXCEPTION);
        }
    }
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException{
        ArrayList<Appointment> appointments = 
                runSQL(AppointmentSQL.READ_APPOINTMENTS_FROM_DAY,day, new ArrayList<Appointment>());
        Iterator<Appointment> it = appointments.iterator();
        while(it.hasNext()){
            Appointment appointment = it.next();
            Patient p =read(appointment.getPatient());
            appointment.setPatient(p);
        }
        return appointments;
    }
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException{
        ArrayList<Appointment> appointments = 
                runSQL(AppointmentSQL.READ_APPOINTMENTS_FOR_DAY,day, new ArrayList<Appointment>());
        Iterator<Appointment> it = appointments.iterator();
        while(it.hasNext()){
            Appointment appointment = it.next();
            Patient p =read(appointment.getPatient());
            appointment.setPatient(p);
        }
        return appointments;
    }
    public ArrayList<Appointment> readAppointments(Patient p, Appointment.Category c) throws StoreException{
        ArrayList<Appointment> appointments = 
                runSQL(AppointmentSQL.READ_APPOINTMENTS_FOR_PATIENT,p, new ArrayList<Appointment>());
        return appointments;
    }
    public ArrayList<Patient> readPatients() throws StoreException{
        ArrayList<Patient> patients = 
                runSQL(PatientSQL.READ_ALL_PATIENTS,new Patient(), new ArrayList<Patient>());
        return patients;
    }
    
    public Patient update(Patient p) throws StoreException{
        runSQL(PatientSQL.UPDATE_PATIENT, p, new ArrayList<Patient>());
        Patient updatedPatient = read(p);
        return updatedPatient;
    }
    public Appointment update(Appointment a) throws StoreException{
        runSQL(AppointmentSQL.UPDATE_APPOINTMENT, a, new ArrayList<Appointment>());
        Appointment updatedAppointment = read(a);
        return updatedAppointment;
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
                    String notes = rs.getString("notes");
                    LocalDate dob = rs.getObject("dob", LocalDate.class);
                    if (dob.getYear() == 1899){
                        dob = null;
                    }
                    int recallFrequency = rs.getInt("recallFrequency");
                    LocalDate recallDate = rs.getObject("recallDate", LocalDate.class);
                    if (recallDate.getYear() == 1899){
                        recallDate = null;
                    }
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
                    patient.setNotes(notes);
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
                    int patientKey = rs.getInt("PatientKey");
                    Appointment appointment = new Appointment();
                    appointment.setKey(key);
                    appointment.setStart(start);
                    appointment.setDuration(duration);
                    appointment.setNotes(notes);
                    appointment.setPatient(new Patient(patientKey));
                    appointment.setStatus(Appointment.Status.BOOKED);
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
    
    private ArrayList<Patient> runSQL(
            PatientSQL q, Object entity, ArrayList<Patient> result)throws StoreException{
        Patient patient = (Patient)entity;
        String sql = null;
        switch (q){
            case READ_HIGHEST_KEY: sql =
                "SELECT MAX(key) as highest_key "
                + "FROM Patient;";
                break;
            case CREATE_PATIENT: sql =
                "INSERT INTO Patient "
                + "(title, forenames, surname, line1, line2,"
                + "town, county, postcode,phone1, phone2, gender, dob,"
                + "isGuardianAPatient, recallFrequency, recallDate, notes,key) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                break;
            case READ_PATIENT_WITH_KEY: sql =
                "SELECT key, title, forenames, surname, line1, line2, "
                + "town, county, postcode, gender, dob, isGuardianAPatient, "
                + "phone1, phone2, recallFrequency, recallDate, notes, guardianKey "
                + "FROM Patient "
                + "WHERE key=?;";
                break;
            case READ_ALL_PATIENTS: 
                sql = "SELECT * FROM Patient ORDER BY surname, forenames ASC;";
                break;
            case UPDATE_PATIENT: sql =
                "UPDATE PATIENT "
                + "SET title = ?, "
                + "forenames = ?,"
                + "surname = ?,"
                + "line1 = ?,"
                + "line2 = ?,"
                + "town = ?,"
                + "county = ?,"
                + "postcode = ?,"
                + "phone1 = ?,"
                + "phone2 = ?,"
                + "gender = ?,"
                + "dob = ?,"
                + "isGuardianAPatient = ?,"
                + "recallFrequency = ?,"
                + "recallDate = ?,"
                + "notes = ?,"
                + "guardianKey = ? "
                + "WHERE key = ? ;";
                break;
            case PATIENTS_COUNT: 
                sql = "SELECT COUNT(*) as record_count "
                + "FROM Patient;";
                break;
        }
        switch (q){
            case READ_HIGHEST_KEY:
                try{
                    Integer key = null;
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()){
                        key = (int)rs.getLong("highest_key");
                    }
                    patient.setKey(key);
                    result.add(patient);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a READ_HIGHEST_KEY query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case CREATE_PATIENT:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    /*
                    if (patient.getName().getTitle()!=null) 
                        preparedStatement.setString(1, patient.getName().getTitle());
                    else preparedStatement.setString(1, "");
                    */
                    preparedStatement.setString(1, patient.getName().getTitle());
                    preparedStatement.setString(2, patient.getName().getForenames());
                    preparedStatement.setString(3, patient.getName().getSurname());
                    preparedStatement.setString(4, patient.getAddress().getLine1());
                    preparedStatement.setString(5, patient.getAddress().getLine2());
                    preparedStatement.setString(6, patient.getAddress().getTown());
                    preparedStatement.setString(7, patient.getAddress().getCounty());
                    preparedStatement.setString(8, patient.getAddress().getPostcode());
                    preparedStatement.setString(9, patient.getPhone1());
                    preparedStatement.setString(10, patient.getPhone2());
                    preparedStatement.setString(11, patient.getGender());
                    if (patient.getDOB()!=null) preparedStatement.setDate(12, java.sql.Date.valueOf(patient.getDOB()));
                    else preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899,1,1)));
                    preparedStatement.setBoolean(13, patient.getIsGuardianAPatient());
                    preparedStatement.setInt(14, patient.getRecall().getDentalFrequency());
                    if (patient.getRecall().getDentalDate()!=null) 
                        preparedStatement.setDate(15, java.sql.Date.valueOf(patient.getRecall().getDentalDate()));
                    else preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899,1,1)));
                    preparedStatement.setString(16, patient.getNotes());
                    preparedStatement.setLong(17, patient.getKey());
                    preparedStatement.executeUpdate();
                    //Connection connection = getConnection();
                    //connection.close();
                    //this.setConnection(null);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during an CREATE_PATIENT statement",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_PATIENT_WITH_KEY:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patient.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    result = getPatientsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a READ_PATIENT_WITH_KEY query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_ALL_PATIENTS:
                try{
                   PreparedStatement preparedStatement = getConnection().prepareStatement(sql); 
                   ResultSet rs = preparedStatement.executeQuery();
                   result = getPatientsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a READ_ALL_PATIENTS query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case UPDATE_PATIENT:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setString(1, patient.getName().getTitle());
                    preparedStatement.setString(2, patient.getName().getForenames());
                    preparedStatement.setString(3, patient.getName().getSurname());
                    preparedStatement.setString(4, patient.getAddress().getLine1());
                    preparedStatement.setString(5, patient.getAddress().getLine2());
                    preparedStatement.setString(6, patient.getAddress().getTown());
                    preparedStatement.setString(7, patient.getAddress().getCounty());
                    preparedStatement.setString(8, patient.getAddress().getPostcode());
                    preparedStatement.setString(9, patient.getPhone1());
                    preparedStatement.setString(10, patient.getPhone2());
                    preparedStatement.setString(11, patient.getGender());
                    if (patient.getDOB()!=null) preparedStatement.setDate(12, java.sql.Date.valueOf(patient.getDOB()));
                    else preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899,1,1)));
                    preparedStatement.setBoolean(13, patient.getIsGuardianAPatient());
                    preparedStatement.setInt(14, patient.getRecall().getDentalFrequency());
                    if (patient.getRecall().getDentalDate()!=null) 
                        preparedStatement.setDate(15, java.sql.Date.valueOf(patient.getRecall().getDentalDate()));
                    else preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899,1,1)));
                    preparedStatement.setString(16, patient.getNotes());
                    if (patient.getGuardian()!=null){
                        preparedStatement.setLong(17, patient.getGuardian().getKey());
                    }
                    else preparedStatement.setLong(17, 0);
                    preparedStatement.setLong(18, patient.getKey());
                    preparedStatement.executeUpdate();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during an UPDATE_PATIENT statement",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case PATIENTS_COUNT:
                try{
                    Integer key = null;
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()){
                        key = (int)rs.getLong("record_count");
                    }
                    patient.setKey(key);
                    result.add(patient);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during an APPOINTMENTS_COUNT in the Appointment table",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
        }
        return result;            
    }
    private ArrayList<Appointment> runSQL(
            AppointmentSQL q, Object entity, ArrayList<Appointment> appointments) throws StoreException{
        ArrayList<Appointment> records = appointments;
        
        Appointment appointment = null;
        LocalDate day = null;
        Patient patient = null;
        if (entity instanceof Appointment) appointment = (Appointment)entity;
        else if (entity instanceof LocalDate) day = (LocalDate)entity;
        else if (entity instanceof Patient) patient = (Patient)entity;
        
        String sql = null; 
        switch (q){
            case APPOINTMENTS_COUNT:
                sql = "SELECT COUNT(*) as record_count "
                + "FROM Appointment;";
                break;
            case READ_HIGHEST_KEY: 
                sql = "SELECT MAX(key) as highest_key "
                + "FROM Appointment;";
                break;
            case DELETE_APPOINTMENT_WITH_KEY:
                sql = "DELETE FROM Appointment WHERE Key = ?;";
                break;
            case DELETE_APPOINTMENTS_WITH_PATIENT_KEY:
                sql = "DELETE FROM Appointment a WHERE a.patientKey = ?;";
                break;
            case CREATE_APPOINTMENT:
                sql = "INSERT INTO Appointment "
                + "(PatientKey, Start, Duration, Notes,Key) "
                + "VALUES (?,?,?,?,?);";
                break;
            case READ_APPOINTMENT_WITH_KEY:
                sql = "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes "
                + "FROM Appointment AS a "
                + "WHERE a.Key = ?;";
                break;
            case READ_APPOINTMENTS_FROM_DAY:
                sql = "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes " +
                "FROM Appointment AS a " +
                "WHERE a.Start >= ? "
                + "ORDER BY a.Start ASC;";
                break;
            case READ_APPOINTMENTS_FOR_PATIENT:
                sql = "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes " +
                "FROM Appointment AS a " +
                "WHERE a.PatientKey = ? " +
                "ORDER BY a.Start DESC"; 
                break;
            case READ_APPOINTMENTS:
                sql = "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes " +
                "FROM Appointment AS a; ";
                break;
            case READ_APPOINTMENTS_FOR_DAY:
                sql = "select *"
                + "from appointment as a "
                + "where DatePart(\"yyyy\",a.start) = ? "
                + "AND  DatePart(\"m\",a.start) = ? "
                + "AND  DatePart(\"d\",a.start) = ? "
                + "ORDER BY a.start ASC;"; 
                break;
            case UPDATE_APPOINTMENT:
                sql = "UPDATE Appointment "
                + "SET PatientKey = ?, "
                + "Start = ?,"
                + "Duration = ?,"
                + "Notes = ?"
                + "WHERE key = ? ;";
        }
        switch (q){
            case APPOINTMENTS_COUNT:
                try{
                    Integer key = null;
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()){
                        key = (int)rs.getLong("record_count");
                    }
                    appointment.setKey(key);
                    records.add(appointment);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during an APPOINTMENTS_COUNT in the Appointment table",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_HIGHEST_KEY:
                try{
                    Integer key = null;
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()){
                        key = (int)rs.getLong("highest_key");
                    }
                    appointment.setKey(key);
                    records.add(appointment);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a READ_HIGHEST_KEY from Appointment table",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case DELETE_APPOINTMENT_WITH_KEY:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, appointment.getKey());
                    preparedStatement.executeUpdate();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a DELETE APPOINTMENT WITH KEY from Appointment table",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case DELETE_APPOINTMENTS_WITH_PATIENT_KEY:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, appointment.getPatient().getKey());
                    preparedStatement.execute();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a data migration op using DELETE APPOINTMENTS WITH PATIENT_KEY from Appointment table",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case CREATE_APPOINTMENT:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, appointment.getPatient().getKey());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(appointment.getStart()));
                    preparedStatement.setLong(3, appointment.getDuration().toMinutes());
                    preparedStatement.setString(4, appointment.getNotes());
                    preparedStatement.setLong(5, appointment.getKey());
                    preparedStatement.executeUpdate();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during an CREATE_APPOINTMENT statement",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_APPOINTMENT_WITH_KEY:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, appointment.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    records = getAppointmentsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a READ_APPOINTMENT_WITH_KEY query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_APPOINTMENTS:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    records = getAppointmentsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a READ_ALL_APPOINTMENTS query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_APPOINTMENTS_FROM_DAY:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setDate(1, java.sql.Date.valueOf(day));
                    ResultSet rs = preparedStatement.executeQuery();
                    records = getAppointmentsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a READ_APPOINTMENTS_FROM_DAY query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_APPOINTMENTS_FOR_PATIENT:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patient.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    records = getAppointmentsFromRS(rs);
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a READ_APPOINTMENTS_FOR_PATIENT query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case READ_APPOINTMENTS_FOR_DAY:
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
                            + "StoreException message -> exception raised during a READ_APPOINTMENTS_FOR_DAY query",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            case UPDATE_APPOINTMENT:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, appointment.getPatient().getKey());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(appointment.getStart()));
                    preparedStatement.setLong(3, appointment.getDuration().toMinutes());
                    preparedStatement.setString(4, appointment.getNotes());
                    preparedStatement.setLong(5, appointment.getKey());
                    preparedStatement.executeUpdate();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during an UPDATE_APPOINTMENT statement",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
        }
        return records;
    }
    
    public void tidyPatientImportedDate()throws StoreException{
        //for each record in patient
        ArrayList<Patient> updatedPatients = null;
        ArrayList<Patient> patients = runSQL(PatientSQL.READ_ALL_PATIENTS,
                new Patient(), new ArrayList<Patient>());
        Iterator<Patient> patientsIT = patients.iterator();
        while (patientsIT.hasNext()){
            Patient patient = patientsIT.next();
            Patient patient1 = doCapitaliseFirstLetterOnly(patient);
            patient.getName().setForenames(patient1.getName().getForenames());
            patient.getName().setSurname(patient1.getName().getSurname());
            patient.getName().setTitle(patient1.getName().getTitle());
            patient.getAddress().setLine1(patient1.getAddress().getLine1());
            patient.getAddress().setLine2(patient1.getAddress().getLine2());
            patient.getAddress().setTown(patient1.getAddress().getTown());
            patient.getAddress().setCounty(patient1.getAddress().getCounty());
            if(patient.getGender()==null) patient.setGender("");
            patient1 = updateGender(patient);
            patient.setGender(patient1.getGender());
            runSQL(PatientSQL.UPDATE_PATIENT, patient, new ArrayList<Patient>());
        } 
    }
    
    private Patient doCapitaliseFirstLetterOnly(Patient patient){
        String cappedForenames = "";
        String cappedSurname = "";
        String cappedTitle = "";
        String cappedLine1 = "";
        String cappedLine2 = "";
        String cappedTown = "";
        String cappedCounty = "";
        
        if (patient.getAddress().getLine1() == null) patient.getAddress().setLine1("");
        if (patient.getAddress().getLine1().length()>0){
            //cappedLine1 = patient.getAddress().getLine1().strip();
            cappedLine1 = patient.getAddress().getLine1();
            if (cappedLine1.contains("-")){ 
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "-");
                if (cappedLine1.contains(" ")){
                   cappedLine1 = capitaliseFirstLetter(cappedLine1, "\\s+"); 
                }
            }
            else if (cappedLine1.contains(" ")) 
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "\\s+");
            else
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "");
        }
        
        if (patient.getAddress().getLine2() == null) patient.getAddress().setLine2("");
        if (patient.getAddress().getLine2().length()>0){
            //cappedLine2 = patient.getAddress().getLine2().strip();
            cappedLine2 = patient.getAddress().getLine2();
            if (cappedLine2.contains("-")){ 
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "-");
                if (cappedLine2.contains(" ")){
                   cappedLine2 = capitaliseFirstLetter(cappedLine2, "\\s+"); 
                }
            }
            else if (cappedLine2.contains(" ")) 
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "\\s+");
            else
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "");
        }
        
        if (patient.getAddress().getTown() == null) patient.getAddress().setTown("");
        if (patient.getAddress().getTown().length()>0){
            //cappedTown = patient.getAddress().getTown().strip();
            cappedTown = patient.getAddress().getTown();
            if (cappedTown.contains("-")){ 
                cappedTown = capitaliseFirstLetter(cappedTown, "-");
                if (cappedTown.contains(" ")){
                   cappedTown = capitaliseFirstLetter(cappedTown, "\\s+"); 
                }
            }
            else if (cappedTown.contains(" ")) 
                cappedTown = capitaliseFirstLetter(cappedTown, "\\s+");
            else
                cappedTown = capitaliseFirstLetter(cappedTown, "");
        }
        
        if (patient.getAddress().getCounty() == null) patient.getAddress().setCounty("");
        if (patient.getAddress().getCounty().length()>0){
            //cappedCounty = patient.getAddress().getCounty().strip();
            cappedCounty = patient.getAddress().getCounty();
            if (cappedCounty.contains("-")){ 
                cappedCounty = capitaliseFirstLetter(cappedCounty, "-");
                if (cappedCounty.contains(" ")){
                   cappedCounty = capitaliseFirstLetter(cappedCounty, "\\s+"); 
                }
            }
            else if (cappedCounty.contains(" ")) 
                cappedCounty = capitaliseFirstLetter(cappedCounty, "\\s+");
            else
                cappedCounty = capitaliseFirstLetter(cappedCounty, "");
        }
        
        if (patient.getName().getSurname() == null) patient.getName().setSurname("");
        if (patient.getName().getSurname().length()>0){
            //cappedSurname = patient.getName().getSurname().strip();
            cappedSurname = patient.getName().getSurname();
            if (cappedSurname.contains("-")){ 
                cappedSurname = capitaliseFirstLetter(cappedSurname, "-");
                if (cappedSurname.contains(" ")){
                   cappedSurname = capitaliseFirstLetter(cappedSurname, "\\s+"); 
                }
            }
            else if (cappedSurname.contains(" ")) 
                cappedSurname = capitaliseFirstLetter(cappedSurname, "\\s+");
            else
                cappedSurname = capitaliseFirstLetter(cappedSurname, "");
        }
        if (patient.getName().getForenames() == null) patient.getName().setForenames("");
        if (patient.getName().getForenames().length()>0){
            //cappedForenames = patient.getName().getForenames().strip();
            cappedForenames = patient.getName().getForenames();
            if (cappedForenames.contains("-")){ 
                cappedForenames = capitaliseFirstLetter(cappedForenames, "-");
                if (cappedForenames.contains(" ")){
                   cappedForenames = capitaliseFirstLetter(cappedForenames, "\\s+"); 
                }
            }
            else if (cappedForenames.contains(" ")) 
                cappedForenames = capitaliseFirstLetter(cappedForenames, "\\s+");
            else
                cappedForenames = capitaliseFirstLetter(cappedForenames, "");
        }
        if (patient.getName().getTitle() == null) patient.getName().setTitle("");
        if (patient.getName().getTitle().length()>0){
            //cappedTitle = patient.getName().getTitle().strip();
            cappedTitle = patient.getName().getTitle();
            cappedTitle = capitaliseFirstLetter(cappedTitle, "");
        }
        Patient p = new Patient();
        p.getName().setSurname(cappedSurname);
        p.getName().setForenames(cappedForenames);
        p.getName().setTitle(cappedTitle);
        p.getAddress().setLine1(cappedLine1);
        p.getAddress().setLine2(cappedLine2);
        p.getAddress().setTown(cappedTown);
        p.getAddress().setCounty(cappedCounty);
        return p;
    }
    private Patient updateGender(Patient patient){
        switch (patient.getGender()){
            case "M":
                patient.setGender("Male");
                break;
            case "F":
                patient.setGender("Female");
                break;
        }
        return patient;
    }
    private String capitaliseFirstLetter(String value, String delimiter){
        ArrayList<String> parts = new ArrayList<>();
        String result = null;
        //value = value.strip();
        if (!delimiter.equals("")){
            String[] values = value.split(delimiter);
            for (int index = 0; index < values.length; index++){
                parts.add(capitalisePart(values[index]));
            }
            for (int index = 0;index < parts.size();index++){
                if (index == 0){
                    result = parts.get(index);
                }
                else if (delimiter.equals("\\s+")){
                    result = result + " " + parts.get(index);
                }
                else{
                    result = result + delimiter + parts.get(index);
                }
            }
        }
        else{
            result = capitalisePart(value);
        }
        return result;
    }
    private String capitalisePart(String part){
        String result = null;
        String firstLetter =  null;
        String otherLetters = null;
        firstLetter = part.substring(0,1).toUpperCase();
        otherLetters = part.substring(1).toLowerCase();
        result =  firstLetter + otherLetters;
        return result;
    }
    
    @Override
    public Dictionary<String,Boolean> readSurgeryDays() throws StoreException{
        Dictionary<String,Boolean> surgeryDays = new Hashtable<String,Boolean>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String sql = "Select Day, IsSurgery from SurgeryDays WHERE Day = ?;";
        try{
            PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
            for (int key = 0; key<days.length;key++){
                preparedStatement.setString(1, days[key]);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next())
                    surgeryDays.put(rs.getString("Day"),rs.getBoolean("IsSurgery"));
                else{
                    message = "Unexpected error: could not locate a record with key = " + days[key];
                    throw new StoreException(message, ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                } 
            }
            return surgeryDays;
        }
        catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
             + "StoreException message -> exception raised during AccessStore::readSurgeryDays statement",
            ExceptionType.SQL_EXCEPTION);
        } 
    }
    
    @Override
    public Dictionary<String,Boolean> updateSurgeryDays(Dictionary<String,Boolean> d) throws StoreException{
        String day = null;
        try{
            String sql = "UPDATE SurgeryDays SET IsSurgery = ? WHERE Day = ?;";
            PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
            for (Enumeration<String>  key = d.keys(); key.hasMoreElements();){
                day = key.nextElement();
                preparedStatement.setString(2, day);
                preparedStatement.setBoolean(1, d.get(day));
                preparedStatement.executeUpdate();
            }
            return readSurgeryDays();
        }
        catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
             + "StoreException message -> exception raised during AccessStorage::updateSurgeryDays statement",
            ExceptionType.SQL_EXCEPTION);
        }  
    }
    
    public TargetsDatabaseManager getTargetsDatabaseManager() throws StoreException{
        if (targetsDatabaseManager == null) targetsDatabaseManager = new TargetsDatabaseManager();
        return targetsDatabaseManager;
    }

    public MigrationManager getMigrationManager(){
        if (migrationManager == null) migrationManager = new MigrationManager();
        return migrationManager;
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
    
    public class MigrationManager implements IMigrationManager{
        private ArrayList<Appointment> appointments = null;
        private ArrayList<Patient> patients = null;
        private int filteredAppointmentCount = 0;
        private int unfilteredAppointmentCount = 0;
        private int nonExistingPatientsReferencedByAppointmentsCount = 0;
        private int patientCount = 0;
        private int appointmentCount = 0;
        private Duration duration = null;
        
        @Override 
        public Duration getMigrationActionDuration(){
            return duration;
        }
        
        @Override
        public void setMigrationActionDuration(Duration value){
            duration = value;
        }
        
        @Override
        public int getAppointmentCount(){
            return appointmentCount; 
        }
        
        @Override
        public void setAppointmentCount(int value){
            appointmentCount = value;
        }
        
        @Override
        public int getPatientCount(){
            return patientCount; 
        }
        
        @Override
        public void setPatientCount(int value){
            patientCount = value;
        }
        
        @Override
        public int getFilteredAppointmentCount(){
            return filteredAppointmentCount; 
        }
        
        @Override
        public void setFilteredAppointmentCount(int value){
            filteredAppointmentCount = value;
        }
        
        @Override
        public int getUnfilteredAppointmentCount(){
            return unfilteredAppointmentCount; 
        }
        
        @Override
        public void setUnfilteredAppointmentCount(int value){
            unfilteredAppointmentCount = value;
        }
        
        @Override
        public int getNonExistingPatientsReferencedByAppointmentsCount(){
            return nonExistingPatientsReferencedByAppointmentsCount;
        }
        
        @Override
        public void setNonExistingPatientsReferencedByAppointmentsCount(int value){
            nonExistingPatientsReferencedByAppointmentsCount = value;
        }
        
        @Override
        public ArrayList<Appointment> getAppointments(){
            return appointments;
        }

        @Override
        public ArrayList<Patient> getPatients(){
            return patients;
        }

        @Override
        public void setAppointments(ArrayList<Appointment> value){
            appointments = value;
        }

        @Override
        public void setPatients(ArrayList<Patient> value){
            patients = value;
        }
        
        /**
         * Every migration method accessing the migration database is processed by the MigrationManager.action() method. Each persistent storage type embeds a MigrationManager fit for its purpose.
         * -- before any migration method is accessed the Store.setTargetConnection() is sent a CONNECTION_MIGRATION_DB message
         * ---- if this fails because the currently defined migration database path has not been defined, the user will be informed
         * -- after the selected migration method has been executed the Store.setTargetConnection is sent CONNECTION_PMS_DB message
         * @param mm, the MigrationMethods (enum) are in order of execution in the switch statement
         * -- in particular, the patient must be populated before the APPOINTMENT_TABLE_INTEGRITY_CHECK
         * @throws StoreException 
         */
        @Override
        public void action(Store.MigrationMethod mm)throws StoreException{
            setTargetConnection(TargetDatabase.MIGRATION_DB);
            int count = 0;
            
            /**
             * log 21/11/2021 07:26 -> to improve portability of code across different storage types
             */
            switch (mm){ 
                case APPOINTMENT_TABLE_DROP:
                        dropAppointmentTable();
                    break;
                case APPOINTMENT_TABLE_CREATE: 
                    createAppointmentTable();
                    break;
                case APPOINTMENT_TABLE_POPULATE: 
                    insertMigratedAppointments(getAppointments());
                    count = getAppointmentsCount();
                    this.setAppointmentCount(count);
                    break;
                case PATIENT_TABLE_DROP:
                    dropPatientTable();
                    break;
                case PATIENT_TABLE_CREATE:
                    createPatientTable(); 
                    break;
                case PATIENT_TABLE_POPULATE: 
                    insertMigratedPatients(getPatients());
                    count = getPatientsCount();
                    this.setPatientCount(count);
                    break;
                case APPOINTMENT_TABLE_INTEGRITY_CHECK:
                    ArrayList<Appointment> the_appointments = readAppointments();
                    this.appointments = the_appointments;
                    this.setAppointmentCount(the_appointments.size());
                    migratedAppointmentsIntegrityCheck();
                    count = getAppointmentsCount();
                    this.setAppointmentCount(count);
                    break;
                case APPOINTMENT_START_TIMES_NORMALISED:
                    normaliseAppointmentStartTimes();
                    break;
                case PATIENT_TABLE_TIDY: 
                    migratedPatientsTidied();
                    break;

            }
            setTargetConnection(TargetDatabase.PMS_DB);
        }
        
        /**
         * Should be renamed dataMigrationReadAppointments()
         * It differs from AccessStore::readAppointments() because that method for each 
         * Appointment object read, reads in the associated Patient object and embeds this 
         * in the Appointment record.However at this stage in the data migration no
         * Patient records (table) will exist
         *
         * @return ArrayList<Appointment>
         * @throws StoreException 
         */
        private ArrayList<Appointment> readAppointments() throws StoreException{
            try{//ensure auto commit setting switched on
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                ArrayList<Appointment> the_appointments = 
                    runSQL(AppointmentSQL.READ_APPOINTMENTS, new Object(),new ArrayList<Appointment>());
                Iterator<Appointment> it = the_appointments.iterator();
                return the_appointments;
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::readAppointments(LocalDate d)",
                ExceptionType.SQL_EXCEPTION);
            }
        }
        private int getPatientsCount() throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                Patient patient = new Patient();
                ArrayList<Patient> value = runSQL(PatientSQL.PATIENTS_COUNT, 
                patient, new ArrayList<>());
                return value.get(0).getKey();
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::getPatientCount()",
                ExceptionType.SQL_EXCEPTION);
            } 
        }
        private int getAppointmentsCount()throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                Appointment appointment = new Appointment();
                ArrayList<Appointment> value = runSQL(AppointmentSQL.APPOINTMENTS_COUNT, 
                appointment, new ArrayList<>());
                return value.get(0).getKey();
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::getAppointmentCount()",
                ExceptionType.SQL_EXCEPTION);
            }      
        }
        
        public void createAppointmentTable()throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                runSQL(MigrationAppointmentSQL.APPOINTMENT_TABLE_CREATE);
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::createAppointmentTable()",
                ExceptionType.SQL_EXCEPTION);
            }  
        }
        
        public void createPatientTable()throws StoreException {
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                runSQL(MigrationPatientSQL.PATIENT_TABLE_CREATE);
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::createPatientTable()",
                ExceptionType.SQL_EXCEPTION);
            }  
        }
        
        public void dropAppointmentTable()throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                runSQL(MigrationAppointmentSQL.APPOINTMENT_TABLE_DROP);
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::dropAppointmentTable()",
                ExceptionType.SQL_EXCEPTION);
            }  
        }
        
        public void dropPatientTable() throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                runSQL(MigrationPatientSQL.PATIENT_TABLE_DROP);
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::dropPatientTable()",
                ExceptionType.SQL_EXCEPTION);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"Undefined patient table",JOptionPane.WARNING_MESSAGE);
            }
        }

        private void insertMigratedPatients(ArrayList<Patient> patients)throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                Iterator<Patient> it = patients.iterator();
                while(it.hasNext()){
                    Patient patient = it.next();
                    Patient p = dataMigrationCreate(patient);
                }
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::insertMigratedPatients()",
                ExceptionType.SQL_EXCEPTION);
            }  
        }
       
        /**
         * -- method establishes a set of patient keys (no duplicates) referenced as appointees by the appointments collection
         * -- each patient key is used to read in the corresponding patient object
         * -- when a corresponding patient object for a given key is not found the appointment object is added to another collection (non existing patient records)
         * -- the 'orphaned' appointment records are then deleted from the system (deleteOrphanedAppointmentsFromAppointments())
         * @param appointments,collection of Appointment objects
         * @throws StoreException, used to identify when appointments refer to a non-existing patient key 
         */
        private void migratedAppointmentsIntegrityCheck()throws StoreException{
            Integer key = null;
            HashSet<Integer> nonExistingPatientsReferencedbyAppointments = new HashSet<>();
            HashSet<Integer> patientSet = new HashSet<>();
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                Iterator<Appointment> it = appointments.iterator();
                while (it.hasNext()){
                    Appointment appointment = it.next();
                    key = appointment.getPatient().getKey();
                    patientSet.add(key);
                }
                Iterator<Integer> patientSetIt = patientSet.iterator();
                while (patientSetIt.hasNext()){
                    key = patientSetIt.next();
                    try{
                        read(new Patient(key));
                        patientCount++;
                    }
                    catch (StoreException ex){
                        if (ex.getErrorType().equals(ExceptionType.KEY_NOT_FOUND_EXCEPTION)){
                            nonExistingPatientsReferencedbyAppointments.add(key);
                        }
                        else{//if not a KEY_NOT_FOUND_EXCEPTION pass StoreException on
                            throw new StoreException(ex.getMessage(),ExceptionType.STORE_EXCEPTION);
                        }
                    }
                }
                this.setNonExistingPatientsReferencedByAppointmentsCount(nonExistingPatientsReferencedbyAppointments.size());
                deleteOrphanedAppointmentsFromAppointments(nonExistingPatientsReferencedbyAppointments);
            }
            catch (SQLException ex){
                message = "SQLException -> " + ex.getMessage() + "\n";
                throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore.MigrationManager::migratedAppointmentsIntegrityCheck()",
                ExceptionType.SQL_EXCEPTION);
            }
        }
        
        public void migratedPatientsTidied()throws StoreException{
            ArrayList<Patient> updatedPatients = null;
            ArrayList<Patient> patients = runSQL(PatientSQL.READ_ALL_PATIENTS,
                    new Patient(), new ArrayList<Patient>());
            Iterator<Patient> patientsIT = patients.iterator();
            while (patientsIT.hasNext()){
                Patient patient = patientsIT.next();
                Patient patient1 = doCapitaliseFirstLetterOnly(patient);
                patient.getName().setForenames(patient1.getName().getForenames());
                patient.getName().setSurname(patient1.getName().getSurname());
                patient.getName().setTitle(patient1.getName().getTitle());
                patient.getAddress().setLine1(patient1.getAddress().getLine1());
                patient.getAddress().setLine2(patient1.getAddress().getLine2());
                patient.getAddress().setTown(patient1.getAddress().getTown());
                patient.getAddress().setCounty(patient1.getAddress().getCounty());
                if(patient.getGender()==null) patient.setGender("");
                patient1 = updateGender(patient);
                patient.setGender(patient1.getGender());
                runSQL(PatientSQL.UPDATE_PATIENT, patient, new ArrayList<Patient>());
            } 
        }
        
        private void normaliseAppointmentStartTimes() throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                runSQL(Store.MigrationAppointmentSQL.APPOINTMENT_START_TIME_NORMALISED);
            }
            catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                        message + "StoreException raised in method AccessStore.MigrationManager::normaliseAppointmentStartTimes()\n"
                                + "Reason -> unexpected effect when getConnection().setAutoCommit() state changed",
                        ExceptionType.SQL_EXCEPTION);
            }
        }
        
        /**
         * create(Patient p) not appropriate because it generates a new (next highest) key for patient record.
         * dataMigrationCreate(Patient p) imports existing records with a predefined key
         * @param p existing Patient object to be inserted into patient table
         * @return Patient
         * @throws StoreException 
         */
        private Patient dataMigrationCreate(Patient p) throws StoreException{
            ArrayList<Patient> value;
            Patient patient;
            message = "";
            try{//turn off jdbc driver's auto commit after each SQL statement
                if (getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(false); 
                } 
                runSQL(PatientSQL.CREATE_PATIENT, p, new ArrayList<>());
                value = runSQL(PatientSQL.READ_PATIENT_WITH_KEY, p, new ArrayList<Patient>());
                if (value.isEmpty()){
                    message = "StoreException raised in method AccessStore.MigrationManager::dataMigrationCreate(Patient p)\n"
                                    + "Reason -> newly created patient record could not be found";
                    getConnection().rollback();
                    throw new StoreException(message,ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                else {
                    patient = value.get(0);
                    getConnection().commit();
                    return patient;
                }
            }
            catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                        message + "StoreException raised in method AccessStore::create(Patient p)\n"
                                + "Reason -> unexpected effect when getConnection().setAutoCommit() state changed",
                        ExceptionType.SQL_EXCEPTION);
            }
        }
        
        /**
         * create(Appointment a) inappropriate because its tries to fetch an existing patient from the Patient's table as the appointee with the patient key stored in the 
         * dataMigrationCreate(Appointment a) used because the Patient table is not populated at this stage of the data migration
         * @param a Appointment to be inserted into Appointment table
         * @return Appointment 
         * @throws StoreException 
         */
        private Appointment dataMigrationCreate(Appointment a) throws StoreException{
            ArrayList<Appointment> value;
            Appointment appointment;
            Patient patient;
            message = "";
            
            try{//turn off jdbc driver's auto commit after each SQL statement
                if (getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(false); 
                }
                value = runSQL(AppointmentSQL.READ_HIGHEST_KEY, 
                        new Appointment(), new ArrayList<Appointment>());
                a.setKey(value.get(0).getKey()+1);
                runSQL(AppointmentSQL.CREATE_APPOINTMENT, a, new ArrayList<Appointment>());
                value = runSQL(AppointmentSQL.READ_APPOINTMENT_WITH_KEY, a, new ArrayList<Appointment>());
                if (value.isEmpty()){
                    message = "StoreException raised in method AccessStore::create(Appointment a)\n"
                                    + "Reason -> newly created appointment record could not be found";
                    getConnection().rollback();
                    throw new StoreException(message,ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                else {
                    appointment = value.get(0);
                    patient = new Patient(appointment.getPatient().getKey());
                    appointment.setPatient(patient);
                    getConnection().commit();
                    return appointment;
                }
            }    
            catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                        message + "StoreException raised in method AccessStore.MigrationManager::create(Appointment a)\n"
                                + "Reason -> unexpected effect when getConnection().setAutoCommit() state changed",
                        ExceptionType.SQL_EXCEPTION);
            }
        }
        
        /**
         * populates the Appointment table with appointment records
         * -- uses a slightly different version of the create(Appointment a) method
         * ---- this version does not return a fully fledged Appointment object to the caller
         * ------ that involves fetching the patient object from the Patient table
         * ------ but during data migration the Appointment table includes appointee references not in the patient table
         * -- note also: a bug in the create(Appointment a) method calls create(Patient) rather than read(Patient) for this purpose
         * 
         * @param appointments
         * @return int count of the number of appointment records
         * @throws StoreException 
         */
        private void insertMigratedAppointments(ArrayList<Appointment> appointments)throws StoreException{
            try{
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                ArrayList<Appointment> as = new ArrayList<>();
                Iterator<Appointment> it = appointments.iterator();
                while(it.hasNext()){
                    Appointment appointment = it.next();
                    Appointment a = dataMigrationCreate(appointment);
                }
            }
            catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                        message + "StoreException raised in method AccessStore.MigrationManager::insertMigratedAppointments(ArrayList<Appointment> appointments)\n"
                                + "Reason -> unexpected effect when getConnection().setAutoCommit() state changed",
                        ExceptionType.SQL_EXCEPTION);
            }
        }
        
        /**
         * the method deletes from the appointment table each appointment which references a non existing patient record
         * @param nonExistingPatientKeys, HashSet of uniquely defined patient keys which do not exist in the Patient table
         * @throws StoreException 
         */
        private void deleteOrphanedAppointmentsFromAppointments(HashSet<Integer> nonExistingPatientKeys)throws StoreException{
            Iterator<Integer> nonExistingPatientKeysIt = nonExistingPatientKeys.iterator();
            while (nonExistingPatientKeysIt.hasNext()){
                Integer nonExistingPatientKey = nonExistingPatientKeysIt.next();
                deleteAppointmentsWithPatientKey(nonExistingPatientKey);
            }
        }
        
        /**
         * deletes the appointment records which reference the specified patient key
         * @param key, Integer -- the specified patient key
         */
        public void deleteAppointmentsWithPatientKey(Integer key ) throws StoreException{
            try{//ensure auto commit setting switched on
                if (!getConnection().getAutoCommit()){
                    getConnection().setAutoCommit(true);
                }
                Appointment a = new Appointment();
                a.setPatient(new Patient(key));
                runSQL(AppointmentSQL.DELETE_APPOINTMENTS_WITH_PATIENT_KEY, a, new ArrayList<Appointment>());
            }
            catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                        message + "StoreException raised in method AccessStore.MigrationManager::deleteAppointmentsWithPatientKey(Integer key)\n"
                                + "Reason -> unexpected effect when getConnection().setAutoCommit() state changed",
                        ExceptionType.SQL_EXCEPTION);
            }
        }
    }
    
    private void runSQL(MigrationAppointmentSQL q)throws StoreException{
        String sql = null;
                switch(q){
                    case APPOINTMENT_TABLE_CREATE:
                        sql = "CREATE TABLE Appointment ("
                        + "key LONG, "
                        + "patientKey LONG, "
                        + "start DateTime, "
                        + "duration LONG, "
                        + "notes char);";
                        break;
                    case APPOINTMENT_TABLE_DROP:
                        sql = "DROP TABLE Appointment;";
                        break;
                    case APPOINTMENT_START_TIME_NORMALISED:
                        sql = "UPDATE Appointment "
                        + "SET start = DateAdd('h',12,[start]) "
                        + "WHERE DatePart('h',start)<8;";  
                        break;
                }
        
        switch (q){
            case APPOINTMENT_TABLE_CREATE:{
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.execute();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_TABLE_CREATE data migration operation",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case APPOINTMENT_TABLE_DROP:{
                /**
                 * Update to fix problem when trying to drop a non-existent appointment table
                 * -- run a query on MSysObjects table to see if table exists or not
                 * -- documented solution doesn't work (Access doesn't like the "sys" prefix)
                 *
                String sql_ = "SELECT COUNT(*) as the_count FROM sys.MSysObjects WHERE name = 'Appointment';";
                */
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.execute();
                    /*
                    if (rs.next()){
                        if ((int)rs.getLong("the_count") == 1){
                            preparedStatement = getConnection().prepareStatement(sql);
                            preparedStatement.executeUpdate();
                        }
                    }
                    */
                }
                catch (SQLException ex){
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_TABLE_DROP data migration operation",
                    ExceptionType.SQL_EXCEPTION);
                    */
                }
                break;
            }
            case APPOINTMENT_START_TIME_NORMALISED:{
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                }
                catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_START_TIME_NORMALISED data migration operation",
                    ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
    
        }            
    }
    
    private void runSQL(MigrationPatientSQL q)throws StoreException{
        String sql = null;
        switch(q){

            case PATIENT_TABLE_CREATE:
                sql = "CREATE TABLE Patient ("
                + "key Long,"
                + "title Char(10),"
                + "forenames Char(25), "
                + "surname Char(25), "
                + "line1 Char(30), "
                + "line2 Char(30), "
                + "town Char(25), "
                + "county Char(25), "
                + "postcode Char(15), "
                + "phone1 Char(30), "
                + "phone2 Char(30), "
                + "gender Char(10), "
                + "dob DateTime,"
                + "isGuardianAPatient YesNo,"
                + "recallFrequency Byte, "
                + "recallDate DateTime, "
                + "notes Char(255), "
                + "guardianKey Long);";
                break;
            case PATIENT_TABLE_DROP:
                sql = "DROP TABLE Patient;";
                break;
        }
        switch (q){
            case PATIENT_TABLE_CREATE:
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.execute();
                }
                catch (SQLException ex){
                    
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a PATIENT_TABLE_CREATE data migration operation",
                    ExceptionType.SQL_EXCEPTION);
                    
                }
                break;
            case PATIENT_TABLE_DROP:
                /**
                 * -- given the issues with trying to drop a non-existing table in Access using Ucanaccess driver
                 * -- when an SQLException is caught nothing is done
                 */
                try{
                    PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                }
                catch (SQLException ex){
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a PATIENT_TABLE_DROP data migration operation",
                    ExceptionType.SQL_EXCEPTION);
                    */
                }
                break;
        }
    }
}
