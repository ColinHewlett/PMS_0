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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;


/**
 *
 * @author colin
 */
public class AccessStore extends Store {
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

    private static AccessStore instance;
    private Connection connection = null;
    private String message = null;
    
    String databaseURL = "jdbc:ucanaccess://c://users//colin//OneDrive//documents"
            + "//Databases//Access//ClinicPMS.accdb;showSchema=true";
    
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
                        + "StoreException message -> raised trying to connect to the Access database",
                ExceptionType.SQL_EXCEPTION);
            }
        }
        return connection;
    }
    
    public AccessStore()throws StoreException{
        connection = getConnection();
    }
    public static AccessStore getInstance() throws StoreException{
        AccessStore result = null;
        if (instance == null) {
            result = new AccessStore();
            instance = result;
        }
        else result = instance;
        
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
                    patient = create(patient);
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
    public Patient read(Patient p) throws StoreException{
        ArrayList<Patient> patients = 
                runSQL(PatientSQL.READ_PATIENT_WITH_KEY,p,  new ArrayList<Patient>());
        Patient patient = patients.get(0);
        if (patient.getGuardian()!=null){
            patients = runSQL(PatientSQL.READ_PATIENT_WITH_KEY, patient.getGuardian(), new ArrayList<Patient>());  
        }
        patient.setGuardian(patients.get(0));
        return patient;
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
        String sql =
                switch (q){
                    case READ_HIGHEST_KEY ->
                "SELECT MAX(key) as highest_key "
                + "FROM Patient;";
                    case CREATE_PATIENT ->
                "INSERT INTO Patient "
                + "(title, forenames, surname, line1, line2,"
                + "town, county, postcode,phone1, phone2, gender, dob,"
                + "isGuardianAPatient, recallFrequency, recallDate, notes,key) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                    case READ_PATIENT_WITH_KEY ->
                "SELECT key, title, forenames, surname, line1, line2, "
                + "town, county, postcode, gender, dob, isGuardianAPatient, "
                + "phone1, phone2, recallFrequency, recallDate, notes, guardianKey "
                + "FROM Patient "
                + "WHERE key=?;";
                    case READ_ALL_PATIENTS -> "SELECT * FROM Patient ORDER BY surname, forenames ASC;";
                    case UPDATE_PATIENT ->
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
                + "WHERE key = ? ;";};
        switch (q){
            case READ_HIGHEST_KEY -> {
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
            }
            case CREATE_PATIENT -> {
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
            }
            case READ_PATIENT_WITH_KEY -> {
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
            }
            case READ_ALL_PATIENTS ->{
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
            }
            case UPDATE_PATIENT ->{
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
            }
        }
        return result;            
    }
    private ArrayList<Appointment> runSQL(
            AppointmentSQL q, Object entity, ArrayList<Appointment> appointments) throws StoreException{
        ArrayList<Appointment> records = appointments;
        String sql = null;
        sql = 
                switch (q){
                    case READ_HIGHEST_KEY ->
                "SELECT MAX(key) as highest_key "
                + "FROM Appointment;";
                    case DELETE_APPOINTMENT_WITH_KEY ->
                "DELETE FROM Appointment WHERE Key = ?;";
                    case CREATE_APPOINTMENT ->
                "INSERT INTO Appointment "
                + "(PatientKey, Start, Duration, Notes,Key) "
                + "VALUES (?,?,?,?,?);";
                    case READ_APPOINTMENT_WITH_KEY ->
                "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes "
                + "FROM Appointment AS a "
                + "WHERE a.Key = ?;";
                    case READ_APPOINTMENTS_FROM_DAY ->
                "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes " +
                "FROM Appointment AS a " +
                "WHERE a.Start >= ? "
                + "ORDER BY a.Start ASC;";
                    case READ_APPOINTMENTS_FOR_PATIENT ->
                "SELECT a.Key, a.Start, a.PatientKey, a.Duration, a.Notes " +
                "FROM Appointment AS a " +
                "WHERE a.PatientKey = ? " +
                "ORDER BY a.Start DESC";    
                    case READ_APPOINTMENTS_FOR_DAY ->
                "select *"
                + "from appointment as a "
                + "where DatePart(\"yyyy\",a.start) = ? "
                + "AND  DatePart(\"m\",a.start) = ? "
                + "AND  DatePart(\"d\",a.start) = ? "
                + "ORDER BY a.start ASC;";  
                        
                    case UPDATE_APPOINTMENT -> 
                "UPDATE Appointment "
                + "SET PatientKey = ?, "
                + "Start = ?,"
                + "Duration = ?,"
                + "Notes = ?"
                + "WHERE key = ? ;";
        };
        switch (q){
            
            case READ_HIGHEST_KEY -> {
                Appointment appointment = (Appointment)entity;
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
            }
            case DELETE_APPOINTMENT_WITH_KEY -> {
                Appointment appointment = (Appointment)entity;
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
            }
            case CREATE_APPOINTMENT -> {
                Appointment appointment = (Appointment)entity;
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
            }
            case READ_APPOINTMENT_WITH_KEY -> {
                Appointment appointment = (Appointment)entity;
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
            }
            case READ_APPOINTMENTS_FROM_DAY -> {
                LocalDate day = (LocalDate)entity;
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
                
            }
            case READ_APPOINTMENTS_FOR_PATIENT -> {
                Patient patient = (Patient)entity;
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
            }
            case READ_APPOINTMENTS_FOR_DAY -> {
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
                            + "StoreException message -> exception raised during a READ_APPOINTMENTS_FOR_DAY query",
                    ExceptionType.SQL_EXCEPTION);
                }
            }
            case UPDATE_APPOINTMENT -> {
                Appointment appointment = (Appointment)entity;
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
            }
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
            cappedLine1 = patient.getAddress().getLine1().strip();
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
            cappedLine2 = patient.getAddress().getLine2().strip();
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
            cappedTown = patient.getAddress().getTown().strip();
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
            cappedCounty = patient.getAddress().getCounty().strip();
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
            cappedSurname = patient.getName().getSurname().strip();
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
            cappedForenames = patient.getName().getForenames().strip();
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
            cappedTitle = patient.getName().getTitle().strip();
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
            case "M" -> patient.setGender("Male");
            case "F" -> patient.setGender("Female");
        }
        return patient;
    }
    private String capitaliseFirstLetter(String value, String delimiter){
        ArrayList<String> parts = new ArrayList<>();
        String result = null;
        value = value.strip();
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


}
