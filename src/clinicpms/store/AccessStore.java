/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import static clinicpms.controller.ViewController.displayErrorMessage;
import clinicpms.model.EntityStoreType;
import org.apache.commons.io.FilenameUtils;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.util.ImportUtil;
import clinicpms.model.StoreManager;
import clinicpms.model.PatientNotification;
//import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.TableRowValue;
//import clinicpms.model.SurgeryDaysAssignmentx;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.store.Store.SelectedTargetStore;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import java.io.IOException;
import java.io.File;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Iterator;
//import clinicpms.model.IEntityStoreType;
import clinicpms.model.StoreManager;
import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class AccessStore extends Store {

    /**
     * the private interface of AccessStore
     */
    private Connection migrationConnection = null;
    private Connection pmsConnection = null;
    private Connection targetConnection = null;
    private Connection PMSstoreConnection = null;
    private String message = null;
    private int nonExistingPatientsReferencedByAppointmentsCount = 0;
    private int patientCount = 0;
    //private ArrayList<Appointment> appointments = null;

    /**
     * If on entry migration connection is undefined the migration database path
     * is used to make a new connection -- method assumes on entry the migration
     * database path is defined
     *
     * @return Connection object
     * @throws StoreException
     */
    /*
    private Connection getMigrationConnection() throws StoreException {
        String path;
        String errorMessage = null;
        String url;
        try {
            if (super.getMigrationDatabasePath() == null) {
                path = readMigrationTargetStorePath();
                if (path == null) {
                    errorMessage = "Raised in AccessStore::getMigrationConnection() because the database file name to connect to has not been defined";
                } else if (FilenameUtils.getBaseName(path).isEmpty()) {
                        errorMessage = "Raised in AccessStore::getMigrationConnection() because the database file name to connect to has not been defined";
                }
                if (errorMessage != null) {
                    throw new StoreException(errorMessage, StoreException.ExceptionType.MIGRATION_CONNECTION_FAILURE);
                } else {
                    url = "jdbc:ucanaccess://" + getMigrationDatabasePath() + ";showSchema=true";
                    migrationConnection = DriverManager.getConnection(url);
                }
            }
        } catch (SQLException ex) {//message handling added
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::getMigrationConnection() method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        return migrationConnection;
    }//store_package_updates_05_12_21_09_17_devDEBUG
*/
    private Connection getPMSStoreConnection() throws StoreException{
        String url;
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            if (PMSstoreConnection==null){
                String path = storeManager.getPMSStorePath();
                if (path==null){
                    String message = "StoreException -> Connection path has not been defined in getStoreConnection()";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                url = "jdbc:ucanaccess://" + path + ";showSchema=true";
                PMSstoreConnection = DriverManager.getConnection(url);
            }

        } catch (SQLException ex) {//message handling added
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::getPMSStoreConnection() method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }

        return PMSstoreConnection;
        
    }



    /**
     * If on entry target connection is defined the connection is closed
     *
     * @throws StoreException
     */
    private void closeTargetConnection() throws StoreException {
        try {
            /**
             * DEBUG -- use of connection getter avoided to prevent stack
             * overflow (recursive reentry issue)
             */
            if (targetConnection != null) {
                targetConnection.close();
            }
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::closeTargetConnection() method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }//store_package_updates_05_12_21_09_17_devDEBUG

    /**
     * If the target connection is undefined the database locator path is used
     * to make a new connection -- on entry its assumed the database locator
     * path is defined
     *
     * @return Connection object
     * @throws StoreException -- if on entry the database locator path is
     * undefined -- if an SQLException is raised when trying to insert a new
     * connection
     */
    private Connection getTargetConnection() throws StoreException {
        String url = null;
        if (getDatabaseLocatorPath() == null) {
            throw new StoreException(
                    "Unretrievable error: no path specified for the DatabaseLocator store",
                    StoreException.ExceptionType.UNDEFINED_DATABASE);
        }
        if (this.targetConnection == null) {
            url = "jdbc:ucanaccess://" + getDatabaseLocatorPath() + ";showSchema=true";

            try {
                targetConnection = DriverManager.getConnection(url);
                //return targetConnection;
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised in AccessStore::getTargetConnection() method",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
        return targetConnection;
    }//store_package_updates_05_12_21_09_17_devDEBUG

    /**
     * Utility method involved in the "tidy up" of the imported patient's
     * contact details
     *
     * @param value:String
     * @param delimiter:String representing the character used to delimit in the
     * context of the patient's name
     * @return Sting; the processed patient's contact details
     */
    private String capitaliseFirstLetter(String value, String delimiter) {
        ArrayList<String> parts = new ArrayList<>();
        String result = null;
        //value = value.strip();
        if (!delimiter.equals("")) {
            String[] values = value.split(delimiter);
            for (int index = 0; index < values.length; index++) {
                parts.add(capitalisePart(values[index]));
            }
            for (int index = 0; index < parts.size(); index++) {
                if (index == 0) {
                    result = parts.get(index);
                } else if (delimiter.equals("\\s+")) {
                    result = result + " " + parts.get(index);
                } else {
                    result = result + delimiter + parts.get(index);
                }
            }
        } else {
            result = capitalisePart(value);
        }
        return result;
    }

    /**
     * Part of the convenience process used for tidying up the imported
     * patient's contact details
     *
     * @param part:String; part of the string required to be processed
     * @return String; processed part
     */
    private String capitalisePart(String part) {
        String firstLetter = part.substring(0, 1).toUpperCase();
        String otherLetters = part.substring(1).toLowerCase();
        String result = firstLetter + otherLetters;
        return result;
    }

    /**
     * One of a collection of overloaded methods requesting an PMS
     * connection-based SQL statement to be executed
     *
     * @param q:PMSSQL signifying the SQL statement to be processed, and which
 include -- APPOINTMENTS_COUNT -- DELETE_APPOINTMENT --
 DELETE_APPOINTMENTS_FOR_PATIENT --
 DELETE_APPOINTMENTS_FOR_PATIENT -- INSERT_APPOINTMENT --
 READ_APPOINTMENT -- READ_APPOINTMENTS_FOR_DAY --
 READ_APPOINTMENTS_FROM_DAY -- READ_HIGHEST_KEY -- UPDATE_APPOINTMENT
     * @param entity:Object checked casting to either an Appointment or Patient
     * or LocalDate object (null if not required)
     * @return ArrayList<Appointment> containing one or more Appointment
     * objects. -- an Appointment object (using its key property) is used to
     * wrap the row count or highest key value in if requested -- null is
     * returned on an INSERT_APPOINTMENT or UPDATE_APPOINTMENT is requested
     * @throws StoreException wraps the SQLException that can be thrown on
     * execution of the SQL statement
     */
    
    private EntityStoreType runSQL(EntitySQL entitySQL, 
            PMSSQL pmsSQL, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (entitySQL){
            case APPOINTMENT:
                result = doAppointmentPMSSQL(pmsSQL, entity);
                break;
            case PATIENT:
                result = doPatientPMSSQL(pmsSQL, entity);
                break;
            case PATIENT_NOTIFICATION:
                result = doPatientNotificationPMSSQL(pmsSQL, entity);
                break;
            case SURGERY_DAYS_ASSIGNMENT:
                result = doSurgeryDaysAssignmentPMSSQL(pmsSQL, entity);
                break;
            case PMS_STORE:
                result = doPMSStoreSQL(pmsSQL, entity);
                break;
        }
        return result;
    }
    
    private Appointment get(Appointment appointment, ResultSet rs) throws StoreException {
        AppointmentDelegate delegate = new AppointmentDelegate();
        PatientDelegate pDelegate = new PatientDelegate();
        try {
            if (!rs.wasNull()) {
                if (rs.next()){
                    int key = rs.getInt("pid");
                    LocalDateTime start = rs.getObject("Start", LocalDateTime.class);
                    Duration duration = Duration.ofMinutes(rs.getLong("Duration"));
                    String notes = rs.getString("Notes");
                    int patientKey = rs.getInt("PatientKey");
                    delegate.setAppointmentKey(key);
                    delegate.setStart(start);
                    delegate.setDuration(duration);
                    delegate.setNotes(notes);
                    pDelegate.setPatientKey(patientKey);
                    delegate.setPatient(pDelegate);
                    //delegate.setStatus(Appointment.Status.BOOKED);
                }
            }
            return delegate;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(Appointment,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
   
    /**
     * method returns the first record contained in the ResultSet
     * -- if more than one record is returned subsequent records are ignored
     * @param patientNotification
     * @param rs
     * @return PatientNotification
     * @throws StoreException 
     */
    private PatientNotification get(PatientNotification patientNotification, ResultSet rs)throws StoreException{
        PatientNotificationDelegate delegate = new PatientNotificationDelegate();
        PatientDelegate pDelegate = new PatientDelegate();
        try{
             if (!rs.wasNull()){
                rs.next();
                int pid = rs.getInt("pid");
                int patientKey = rs.getInt("patientToNotify");
                LocalDate notificationDate = rs.getObject("notificationDate", LocalDate.class);
                String notificationText = rs.getString("notificationText");
                Boolean isActioned = rs.getBoolean("isActioned");
                delegate.setKey(pid);
                pDelegate.setPatientKey(patientKey);
                delegate.setPatient(pDelegate);
                delegate.setNotificationDate(notificationDate);
                delegate.setNotificationText(notificationText);
                delegate.setIsActioned(isActioned);
            }
            return delegate;
        }catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(PatientNotification,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    private PatientNotification.Collection get(PatientNotification.Collection patientNotifications, ResultSet rs)throws StoreException{
        PatientNotificationDelegate delegate = null;
        PatientDelegate pDelegate = null;
        ArrayList<PatientNotification> collection = new ArrayList<>();
        try{
            if (!rs.wasNull()){
                while (rs.next()){
                   int pid = rs.getInt("pid");
                   int patientKey = rs.getInt("patientToNotify");
                   LocalDate notificationDate = rs.getObject("notificationDate", LocalDate.class);
                   String notificationText = rs.getString("notificationText");
                   Boolean isActioned = rs.getBoolean("isActioned");
                   //PatientNotification patientNotification = new PatientNotification();
                   delegate = new PatientNotificationDelegate();
                   delegate.setKey(pid);
                   pDelegate = new PatientDelegate();
                   pDelegate.setPatientKey(patientKey);
                   delegate.setPatient(pDelegate);
                   delegate.setNotificationDate(notificationDate);
                   delegate.setNotificationText(notificationText);
                   delegate.setIsActioned(isActioned);
                   collection.add(delegate);
                }
                patientNotifications.set(collection);;
            }
            return patientNotifications;
        }catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(PatientNotifications,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    private Appointment get(Appointment.Collection collection, ResultSet rs)throws StoreException{
        ArrayList<Appointment> appointments = new ArrayList<>(); 
        AppointmentDelegate delegate = new AppointmentDelegate();
        PatientDelegate pDelegate = null;
        try{
            if (!rs.wasNull()) {
                while (rs.next()) {
                    int key = rs.getInt("pid");
                    LocalDateTime start = rs.getObject("Start", LocalDateTime.class);
                    Duration duration = Duration.ofMinutes(rs.getLong("Duration"));
                    String notes = rs.getString("Notes");
                    int patientKey = rs.getInt("PatientKey");
                    delegate = new AppointmentDelegate();
                    delegate.setStart(rs.getObject("Start", LocalDateTime.class));
                    delegate.setDuration(Duration.ofMinutes(rs.getLong("Duration")));
                    delegate.setNotes(rs.getString("Notes"));
                    delegate.setAppointmentKey(rs.getInt("pid"));
                    pDelegate = new PatientDelegate();
                    pDelegate.setPatientKey(rs.getInt("PatientKey"));
                    delegate.setPatient(pDelegate);
                    appointments.add(delegate);
                }
                
            }
        }catch (SQLException ex){
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(Appointments.Collection,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        delegate.getCollection().set(appointments);
        return delegate;
    }
    
    /**
     * method
     * @param patient
     * @param rs
     * @return Patient for Patient and - if getIsAGuardian() returns true - another delegate class for the guardian
     * @throws SQLException 
     */
    private Patient getThePatientDetails(PatientDelegate delegate, ResultSet rs) throws SQLException {
        int key = rs.getInt("pid");
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
        if (dob.getYear() == 1899) {
            dob = null;
        }
        int recallFrequency = rs.getInt("recallFrequency");
        LocalDate recallDate = rs.getObject("recallDate", LocalDate.class);
        if (recallDate.getYear() == 1899) {
            recallDate = null;
        }
        boolean isGuardianAPatient = rs.getBoolean("isGuardianAPatient");

        //patient.setKey(key);
        delegate.getName().setTitle(title);
        delegate.getName().setForenames(forenames);
        delegate.getName().setSurname(surname);
        delegate.getAddress().setLine1(line1);
        delegate.getAddress().setLine2(line2);
        delegate.getAddress().setTown(town);
        delegate.getAddress().setCounty(county);
        delegate.getAddress().setPostcode(postcode);
        delegate.setGender(gender);
        delegate.setDOB(dob);
        delegate.setPhone1(phone1);
        delegate.setPhone2(phone2);
        delegate.getRecall().setDentalDate(recallDate);
        delegate.getRecall().setDentalFrequency(recallFrequency);
        delegate.setNotes(notes);
        delegate.setIsGuardianAPatient(isGuardianAPatient);
        if (delegate.getIsGuardianAPatient()) {
            int guardianKey = rs.getInt("guardianKey");
            if (guardianKey > 0) {
                PatientDelegate gDelegate = new PatientDelegate(guardianKey);
                delegate.setGuardian(gDelegate);
            }
        }
        //patient.setKey(key);
        delegate.setPatientKey(key);
        return delegate;
    }

    private Patient get(PatientDelegate patient, ResultSet rs) throws SQLException {
        Patient result = null;
        if (!rs.wasNull()) {
            if (rs.next()) {
                result = getThePatientDetails(patient, rs);
            }
        } else {
            result = null;
        }
        return result;
    }
    
    private SurgeryDaysAssignment get(SurgeryDaysAssignment surgeryDaysAssignment, ResultSet rs) throws StoreException {
        String day = null;
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
                    day = rs.getString("Day");
                    switch (day) {
                        case "Monday":
                            surgeryDaysAssignment.get().put(DayOfWeek.MONDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Tuesday":
                            surgeryDaysAssignment.get().put(DayOfWeek.TUESDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Wednesday":
                            surgeryDaysAssignment.get().put(DayOfWeek.WEDNESDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Thursday":
                            surgeryDaysAssignment.get().put(DayOfWeek.THURSDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Friday":
                            surgeryDaysAssignment.get().put(DayOfWeek.FRIDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Saturday":
                            surgeryDaysAssignment.get().put(DayOfWeek.SATURDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Sunday":
                            surgeryDaysAssignment.get().put(DayOfWeek.SUNDAY, rs.getBoolean("isSurgery"));
                            break;
                    }
                }

            }
            return surgeryDaysAssignment;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(SurgeryDaysAssignment,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    /**
     * method decodes the ResultSet for persistent storage into a Patient.Collection object
 -- the resulting collection of Patient child objects is used to initialise a mother Patient.Collection object
 -- the information contained in Patient object is either in the values of its state fields, excepting its collection field;
 -- or the information is contained in its Collection inner class
 -- This is possibly ambiguous, and can be overcome by making a separate class responsible for the collection
     * @param collection
     * @param rs
     * @return
     * @throws StoreException 
     */
    private Patient.Collection getPatientCollectionDelegate(Patient.Collection collection, ResultSet rs) throws StoreException {
        ArrayList<Patient> patients = new ArrayList<>();
        Patient motherPatient = new Patient();
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
                    Patient childPatient = getThePatientDetails(new PatientDelegate(), rs);
                    patients.add(childPatient);
                }
                motherPatient.getCollection().set(patients);
            }
            return motherPatient.getCollection();
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(ThePatient.Collection,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    private Patient get(Patient.Collection collection, ResultSet rs) throws StoreException {
        ArrayList<Patient> patients = new ArrayList<>();
        Patient motherPatient = new Patient();
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
                    //ThePatient childPatient = new Patient();
                    //childPatient = getThePatientDetails(childPatient, rs);
                    Patient childPatient = getThePatientDetails(new PatientDelegate(), rs);
                    patients.add(childPatient);
                }
                motherPatient.getCollection().set(patients);
            }
            return motherPatient;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(ThePatient.Collection,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    /**
     * Initialises the connection mode accordingly; either AUTO_COMMIT_OFF or
     * AUTO_COMMIT_ON
     *
     * @param mode::ConnectionMode enumeration literal
     * @throws SQLException if the auto commit mode getter/setters fail
     * @throws StoreException if the connection getter fails
     */
    private void setConnectionMode(ConnectionMode mode) throws SQLException, StoreException {
        switch (mode) {
            case AUTO_COMMIT_OFF:
                if (IS_MIGRATION_STORE_ACTION) {
                    /*
                    if (getMigrationConnection().getAutoCommit()) {
                        getMigrationConnection().setAutoCommit(false);
                    }
                    */
                } else if (IS_PMS_STORE_ACTION) {
                    if (getPMSStoreConnection().getAutoCommit()) {
                        getPMSStoreConnection().setAutoCommit(false);
                    }
                }
                break;
            case AUTO_COMMIT_ON:
                if (IS_MIGRATION_STORE_ACTION) {
                    /*
                    if (getMigrationConnection().getAutoCommit()) {
                        getMigrationConnection().setAutoCommit(true);
                    
                    }
                    */
                } else if (IS_PMS_STORE_ACTION) {
                    if (getPMSStoreConnection().getAutoCommit()) {
                        getPMSStoreConnection().setAutoCommit(true);
                    }
                }
                break;
        }
    }

    /**
     * Ends current store transaction with either a commit or rollback
     * transaction request
     *
     * @param state:boolean
     * @throws SQLException in following circumstances if the transaction end
     * request fails
     * @throws StoreException if the call to the connection getter fails
     */
    private void setConnectionState(boolean state) throws SQLException, StoreException {
        if (state) {
            if (IS_MIGRATION_STORE_ACTION) {
                /*
                if (state) {
                    getMigrationConnection().commit();
                } else {
                    getMigrationConnection().rollback();
                }
                */
            } else if (IS_PMS_STORE_ACTION) {
                if (state) {
                    getPMSStoreConnection().commit();
                } else {
                    getPMSStoreConnection().rollback();
                }
            }
        }
    }

    /**
     * The constructor has one task only -- to close all connections to stores
     * that might be already open
     *
     * @throws StoreException
     */
    public AccessStore() throws StoreException {
    }

    /**
     * The static method implements the singleton pattern to ensure only one
     * AccessStore ever exists -- only if the current Store INSTANCE variable is
     * undefined is it defined with a new AccessStore INSTANCE
     *
     * @return AccessStore INSTANCE
     * @throws StoreException
     */
    public static AccessStore getInstance() throws StoreException {
        AccessStore result;
        if (INSTANCE == null) {
            result = new AccessStore();
            INSTANCE = result;
        } else {
            result = (AccessStore) INSTANCE;
        }

        return result;
    }

    @Override
    public void insert(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        try{
            getPMSStoreConnection().setAutoCommit(true);
            runSQL(Store.EntitySQL.SURGERY_DAYS_ASSIGNMENT, 
                PMSSQL.INSERT_SURGERY_DAYS_ASSIGNMENT, surgeryDaysAssignment);
        }catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.insert(SurgeryDaysAssignment)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
               
    }

    /**
     * method requires explicit declaration of the appointee's key value
     * @param appointment
     * @param appointeeKey
     * @return
     * @throws StoreException 
     */
    @Override
    public Integer insert(Appointment appointment,Integer appointeeKey) throws StoreException {
        Integer result = null;
        AppointmentDelegate delegate = new AppointmentDelegate(appointment);
        PatientDelegate pDelegate = new PatientDelegate(delegate.getPatient());
        pDelegate.setPatientKey(appointeeKey);
        delegate.setPatient(pDelegate);
        EntityStoreType value;
        message = "";
        try {
            getPMSStoreConnection().setAutoCommit(true);
            value = runSQL(Store.EntitySQL.APPOINTMENT,
                    PMSSQL.READ_APPOINTMENT_NEXT_HIGHEST_KEY,null);
            if (value.getIsTableRowValue()) {
                delegate.setAppointmentKey(((TableRowValue) value).getValue() + 1);
                runSQL(Store.EntitySQL.APPOINTMENT,PMSSQL.INSERT_APPOINTMENT, delegate);
                return delegate.getAppointmentKey();
            }
            else {
                displayErrorMessage("Unable to calculate a new key value for the new Appointment.\n"
                        + "Error raised in AccessStore::insert(Appointment) : Integer",
                        "Access store error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(Appointment a)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } 
        return result;
    }

    @Override
    /**
     * method attempts to insert a new patient notification record on the database
     * -- it assumes the key of the PatientNotification object is undefined
     * -- it fetches the next highest key value from the database and initialises the PatientNotification object with this
     * -- after creating a new patient notification record the method attempts to read back the record using the key value it defined
     * -- on success the method returns; else throws an exception
     * @param pn; PatientNotification which points to the calling PatientNotification object instance
     * @exception StoreException is thrown 
     * -- [1] if the received PatientNotification object already has a key value
     * -- [2] if patient notification record cannot be read back successfully
     * -- [3] passes on a StoreException error thrown by the database
     */
    public Integer insert(PatientNotification pn)throws StoreException{
        EntityStoreType key = null;
        PatientNotificationDelegate delegate = null;
        PatientDelegate pDelegate = null;
        EntityStoreType entity;
        message = "";
        try {//turn off jdbc driver's auto commit after each SQL statement
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            entity = runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,
                    PMSSQL.READ_PATIENT_NOTIFICATION_NEXT_HIGHEST_KEY,pn);
            delegate = new PatientNotificationDelegate(pn);
            delegate.setKey(((TableRowValue) entity).getValue() + 1);
            //30/07/2022 09:26
            runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,
                    PMSSQL.INSERT_PATIENT_NOTIFICATION, delegate);
            return delegate.getKey();
            /*
            key = runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,
                    PMSSQL.INSERT_PATIENT_NOTIFICATION, delegate);
            return ((TableRowValue)key).getValue();
            */
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(PatientNotification pn)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    /**
     * method supports insertion of patient records with pre-defined key value (data migration app mode), and without pre-defined key values (PMS mode of app)
     * -- the Patient.getIsKeyDefined() method determines if the app is in data migration mode or not
     * -- in PMS app mode the method calculates the next highest key value to use for the insertion
     * @param p Patient
     * @param key Integer value which represents 
     * -- the pre-defined value of the patient (in data migration app mode)
     * -- the key value of the guardian if a guardian exists for this patient (in PMS app mode)
     * @throws StoreException 
     * @return Integer specifying the key value of the new value created
     */
    @Override
    public Integer insert(Patient patient, Integer key) throws StoreException{ 
        EntityStoreType entity = null;
        Integer result = null;
        PatientDelegate delegate = null;
        PatientDelegate gDelegate = null;
        delegate = new PatientDelegate(patient);

        try{
            getPMSStoreConnection().setAutoCommit(true);
            if (!patient.getIsKeyDefined()){
                if (delegate.getIsGuardianAPatient()){
                    gDelegate = new PatientDelegate(delegate.getGuardian());
                    gDelegate.setPatientKey(key);
                }
                else{
                    gDelegate = new PatientDelegate();
                    gDelegate.setPatientKey(0);
                }
                entity = runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENT_NEXT_HIGHEST_KEY, new Patient());
                if (entity.getIsTableRowValue())
                    delegate.setPatientKey(((TableRowValue) entity).getValue() + 1);
            }else{
                delegate.setPatientKey(key);
                gDelegate = new PatientDelegate();
                gDelegate.setPatientKey(0);
            }
            delegate.setGuardian(gDelegate);           
            runSQL(EntitySQL.PATIENT,PMSSQL.INSERT_PATIENT, delegate);
            result =  delegate.getPatientKey();
        }catch (SQLException ex){
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(ThePatient a)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        return result;
    }

   public void delete(Patient patient){
       
   }
    /**
     * 
     * @param a
     * @throws StoreException 
     */
    @Override
    public void delete(Appointment appointment, Integer key) throws StoreException {
        AppointmentDelegate delegate = new AppointmentDelegate(appointment);
        delegate.setAppointmentKey(key);
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            runSQL(EntitySQL.APPOINTMENT, PMSSQL.DELETE_APPOINTMENT, appointment);
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::delete(Appointment a)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } 
    }
   
    /**
     * method fetches the patient notification with the specified key
     * -- the notification's patient that is fetched has only its key value defined; it is the caller's responsibility to issue a request for the patient's other values
     * @param patientNotification
     * @return PatientNotification
     * @throws StoreException in the following cases
     * -- an unexpected value is returned from the store; i.e. not a PatientNotification object
     * -- a patient notification with the specified key value could not be located on the store
     * -- a patient notification key has not been defined
     */
    @Override
    public PatientNotification read(PatientNotification patientNotification, Integer key)throws StoreException{
        PatientNotificationDelegate delegate = new PatientNotificationDelegate();
        PatientDelegate pDelegate = new PatientDelegate();
        EntityStoreType value;
        PatientNotification result;
        delegate.setKey(key);
        try{
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            value = runSQL(Store.EntitySQL.PATIENT_NOTIFICATION, 
                        Store.PMSSQL.READ_PATIENT_NOTIFICATION, 
                        patientNotification);
            if (value!=null){
                if (value.getIsPatientNotification()){
                    result = (PatientNotification)value;
                    return result;
                }else{
                    throw new StoreException(
                        message + "StoreException raised -> unexpected value returned from persistent store "
                            + "in method AccessStore::read(PatientNotification)\n",
                        StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED); 
                }
            }else{
                throw new StoreException(
                    message + "StoreException raised -> could not locate specified patient notification "
                        + "in method AccessStore::read(PatientNotification)\n",
                    StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
        }catch(SQLException ex){
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::read(PatientNotification)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }          
    }
    
    /**
     * method fetches a collection of patient notifications from store
     * -- to enable transfer of the owning patient's key value, a delegate class replaces the patient's in the PatientNotification associated with the collection
     * -- the specified collection object defines the scope of the required collection
     * -- for each notification's patient only the key value is returned; its the responsibility of the caller to issue another read request per notification to fetch the patient's other details, if this is necessary 
     * @param patientNotificationCollection
     * @param key, if the requested collection is for a specific patient
     * -- this is the key value of the owning patient in the associated PatientNotification from which a delagate class will be constructed
     * -- if not a patient-based collection the key value is null
     * @return
     * @throws StoreException 
     */
    @Override
    public PatientNotification.Collection read(PatientNotification.Collection patientNotificationCollection, Integer key)throws StoreException{
        PatientDelegate delegate = null;
        EntityStoreType value = null;
        PatientNotification.Collection result = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            PatientNotification.Scope scope = patientNotificationCollection.getScope();
            switch(scope){
                case UNACTIONED:
                    value = runSQL(EntitySQL.PATIENT_NOTIFICATION,
                                PMSSQL.READ_UNACTIONED_PATIENT_NOTIFICATIONS, 
                                patientNotificationCollection);
                    break;
                case ALL:
                    value = runSQL(EntitySQL.PATIENT_NOTIFICATION,
                                PMSSQL.READ_PATIENT_NOTIFICATIONS, 
                                patientNotificationCollection);
                    break;
                case FOR_PATIENT:
                    delegate = new PatientDelegate(patientNotificationCollection.getPatientNotification().getPatient());
                    delegate.setPatientKey(key);
                    patientNotificationCollection.getPatientNotification().setPatient(delegate);
                    value = runSQL(EntitySQL.PATIENT_NOTIFICATION,
                                PMSSQL.READ_PATIENT_NOTIFICATIONS_FOR_PATIENT, 
                                patientNotificationCollection.getPatient());
                    break;
            }
            if (value!=null){
                    if (value.getIsPatientNotifications()){
                        result = (PatientNotification.Collection)value;
                        
                        return result;
                    }else{
                        throw new StoreException(
                            message + "StoreException raised -> unexpected data type returned from persistent store "
                                + "in method AccessStore::read(PatientNotification.Collection)\n",
                            StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED); 
                    }
            }else{
                throw new StoreException(
                    message + "StoreException raised -> null value returned from persistent store "
                        + "in method AccessStore::read(PatientNotification.Collection)\n",
                    StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::read(PatientNotifications)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } 
    }
    
    
    @Override
    public SurgeryDaysAssignment read(SurgeryDaysAssignment s) throws StoreException {
        SurgeryDaysAssignment surgeryDaysAssignment = null;
        EntityStoreType value = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            value = runSQL(Store.EntitySQL.SURGERY_DAYS_ASSIGNMENT,PMSSQL.READ_SURGERY_DAYS_ASSIGNMENT, null);
            if (value != null) {
                if (value.getIsSurgeryDaysAssignment()) {
                    surgeryDaysAssignment = (SurgeryDaysAssignment) value;
                }
            }
            return surgeryDaysAssignment;
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::read(DurgeryDaysAssignment)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    /*
    public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentx s) throws StoreException {
        SurgeryDaysAssignmentx surgeryDaysAssignment = null;
        IEntityStoreType value = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            value = runSQL(PMSSQL.READ_SURGERY_DAYS_ASSIGNMENT, null);
            if (value != null) {
                if (value.isSurgeryDaysAssignment()) {
                    surgeryDaysAssignment = (SurgeryDaysAssignmentx) value;
                }
            }
            return surgeryDaysAssignment;
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::read(DurgeryDaysAssignment)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }

    }
    */
    public Integer count(PatientNotification.Collection collection)throws StoreException{
        TableRowValue result = null;
        PMSSQL sqlStatement = null;
        switch (collection.getScope()){
            case ALL:
                sqlStatement = PMSSQL.COUNT_PATIENT_NOTIFICATIONS;
                break;
            case UNACTIONED:
                sqlStatement = PMSSQL.COUNT_UNACTIONED_PATIENT_NOTIFICATIONS;
                break;
        }
        result = (TableRowValue)runSQL(EntitySQL.PATIENT_NOTIFICATION, sqlStatement, null);
        return result.getValue();
    }
    
    public Integer count(Appointment.Collection entity, Integer patientKey) throws StoreException{
        TableRowValue result = null;
        PMSSQL sqlStatement = null;
        switch(entity.getScope()){
            case ALL:
                sqlStatement = PMSSQL.COUNT_APPOINTMENTS;
                break;
            case FOR_DAY:
                sqlStatement = PMSSQL.COUNT_APPOINTMENTS_FOR_DAY;
                break;
            case FOR_PATIENT:
                Appointment.Collection appointments = (Appointment.Collection)entity;
                PatientDelegate delegate = new PatientDelegate();
                delegate.setPatientKey(patientKey);
                appointments.getAppointment().setPatient(delegate);
                sqlStatement = PMSSQL.COUNT_APPOINTMENTS_FOR_PATIENT;
                break;
            case FROM_DAY:
                sqlStatement = PMSSQL.COUNT_APPOINTMENTS_FROM_DAY;
                break;       
        }
        result = (TableRowValue)runSQL(EntitySQL.APPOINTMENT, sqlStatement, entity );
        return result.getValue();
    }

    public Integer count(Patient.Collection patients)throws StoreException{
        TableRowValue result = null;
        result = (TableRowValue)runSQL(EntitySQL.PATIENT, PMSSQL.COUNT_PATIENTS, patients );
        return result.getValue();
    }
    
    public Integer count(SurgeryDaysAssignment surgeryDaysAssignment)throws StoreException{
        TableRowValue result = null;
        result = (TableRowValue)runSQL(
                EntitySQL.SURGERY_DAYS_ASSIGNMENT, PMSSQL.COUNT_SURGERY_DAYS_ASSIGNMENT, null );
        return result.getValue();
    }
    
    @Override
    public Appointment read(Appointment.Collection entity, Integer key)throws StoreException{
        boolean isAppointmentsForDay = false;
        PatientDelegate delegate = null;
        EntityStoreType result = null;
        PMSSQL sqlStatement = null;
        switch(entity.getScope()){
            case ALL:
                sqlStatement = PMSSQL.READ_APPOINTMENTS;
                break;
            case FOR_DAY:
                sqlStatement = PMSSQL.READ_APPOINTMENTS_FOR_DAY;
                isAppointmentsForDay = true;
                break;
            case FOR_PATIENT:
                delegate = new PatientDelegate(key);
                entity.getAppointment().setPatient(delegate);
                sqlStatement = PMSSQL.READ_APPOINTMENTS_FOR_PATIENT;
                break;
            case FROM_DAY:
                sqlStatement = PMSSQL.READ_APPOINTMENTS_FROM_DAY;
                break;       
        }
        result = runSQL(EntitySQL.APPOINTMENT, sqlStatement, entity );
        if (isAppointmentsForDay){
            Iterator<Appointment> it = ((Appointment)result).getCollection().get().iterator();
            while (it.hasNext()){
                Appointment appointment = it.next();
                Integer theKey = ((PatientDelegate)appointment.getPatient()).getPatientKey();
                Patient patient = new Patient(((PatientDelegate)appointment.getPatient()).getPatientKey());
                appointment.setPatient(patient.read());
            }
        }
        return (Appointment)result;
    }
    
    public Appointment read(Appointment appointment, Integer key)throws StoreException{
        AppointmentDelegate delegate = new AppointmentDelegate();
        delegate.setAppointmentKey(key);
        EntityStoreType result = null;
        result = runSQL(EntitySQL.APPOINTMENT, PMSSQL.READ_APPOINTMENT, delegate);
        return (Appointment)result;
    }
    
   
    /**
     * Method creates a delegate class to transfer to and from store the key value of the specified Patient.
     * -- the store getter returns this method a fully initialised delegate class which returns the key value of the patient and - if a guardian exists - the guardian's key value
     * @param patient
     * @param key, Integer value of the patient's key 
     * @return Patient
     * @throws StoreException 
     */
    @Override
    public Patient read(Patient patient, Integer key) throws StoreException { 
        PatientDelegate gDelegate = null;
        PatientDelegate delegate = new PatientDelegate(key);
        EntityStoreType entity = null;
        try {//ensure auto commit setting switched on
            getPMSStoreConnection().setAutoCommit(true);
            /**
             * use delegate class to transfer explicitly defined Patient's key value
             * -- note this is the only field required to locate the patient in persistent store
             */
            entity = runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENT, delegate);
            if (entity == null) {
                throw new StoreException(
                        "Could not locate requested patient in "
                                + "AccessStore::read(ThePatient, Integer key, Integer guardianKey )",
                        StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            } 
            return (Patient)entity;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::read(ThePatient p)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public Patient.Collection read(Patient.Collection p) throws StoreException{
        Patient patient = null;
        EntityStoreType value = null;
        Patient.Collection result = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            value = runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENTS,null);
            if (value!=null){
                if (value.getIsPatient()){
                    patient = (Patient)value;
                    return patient.getCollection();
                }else{
                    throw new StoreException(
                        "StoreException raised -> unexpected data type returned from persistent store "
                            + "in method AccessStore::read(ThePatient.Collection)\n",
                        StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED); 
                }
            }else{
                throw new StoreException(
                    "StoreException raised -> null value returned from persistent store "
                        + "in method AccessStore::read(ThePatient.Collection)\n",
                    StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }catch (SQLException ex) {
            message = ex.getMessage() + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::read(ThePatient.Collection)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
   
    
    
    @Override
    /**
     * Explicit manual transaction processing enabled -- updates either the pms
     * or migration target store path with the specified path
     *
     * @param db:SelectedTargetStore specifies which target store (pms or
     * migration) is updated)
     * @param updatedLocation: String specifies the new path value
     * @throws StoreException
     */
    public void update(SelectedTargetStore db, String updatedLocation) throws StoreException {
        boolean result = false;
        String sql = "UPDATE Target SET location = ? WHERE db = ?;";
        try {
            if (getTargetConnection().getAutoCommit()) {
                getTargetConnection().setAutoCommit(false);
            }
            try {
                PreparedStatement preparedStatement = getTargetConnection().prepareStatement(sql);
                preparedStatement.setString(1, updatedLocation);
                preparedStatement.setString(2, db.toString());
                preparedStatement.executeUpdate();
                result = true;
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised in AccessStore::::update(SelectedTargetStore,path) statement",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in getTargetConnection() based autoCommit access in AccessStore::update(SelectedTargetStore,path) method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getTargetConnection().commit();
                } else {
                    getTargetConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore.updaye(SelectedTargetStore, path))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    @Override
    /**
     * update appointment method adopts the delegate mechanism to transfer two key values to store
     * @param appointment
     * @param key Integer value of the appointment key 
     * @param appointeeKey Integer value of the appointment's appointee key
     * @throws StoreException 
     */
    public void update(Appointment appointment, Integer key, Integer appointeeKey) throws StoreException {
        AppointmentDelegate delegate = new AppointmentDelegate(appointment);
        PatientDelegate pDelegate = new PatientDelegate(delegate.getPatient());
        delegate.setAppointmentKey(key);
        pDelegate.setPatientKey(appointeeKey);
        delegate.setPatient(pDelegate);
        try{
            getPMSStoreConnection().setAutoCommit(true);
            runSQL(EntitySQL.APPOINTMENT, PMSSQL.UPDATE_APPOINTMENT, delegate);
        }catch (SQLException ex){
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> "
                    + "unexpected error accessing AutoCommit/commit/rollback "
                    + "setting in AccessStore::update(Appointment, Integer key, Integer appointeeKey)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    @Override
    public void update(Patient patient, Integer key, Integer guardianKey) throws StoreException {
        PatientDelegate delegate = new PatientDelegate(patient);
        delegate.setPatientKey(key);
        if (delegate.getIsGuardianAPatient()){
            PatientDelegate gDelegate = new PatientDelegate(delegate.getGuardian());
            gDelegate.setPatientKey(guardianKey);
            delegate.setGuardian(gDelegate);
        }
        try{
            getPMSStoreConnection().setAutoCommit(true);
            runSQL(EntitySQL.PATIENT, PMSSQL.UPDATE_PATIENT, delegate);
        }catch (SQLException ex){
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> "
                    + "unexpected error accessing AutoCommit/commit/rollback "
                    + "setting in AccessStore::update(Patient, Integer key, Integer guardianKey)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    
    /**
     * method sends the specified pn to persistent store
     * @param pn
     * @throws StoreException if exception arises in transaction control
     */
    @Override
    public void update(PatientNotification pn, Integer key, Integer patientKey)throws StoreException{
        PatientNotificationDelegate delegate = new PatientNotificationDelegate(pn);
        PatientDelegate pDelegate = new PatientDelegate();
        delegate.setKey(key);
        pDelegate.setPatientKey(patientKey);
        delegate.setPatient(pDelegate);
        try {
            if (getPMSStoreConnection().getAutoCommit()) {
                getPMSStoreConnection().setAutoCommit(true);
            }
            runSQL(EntitySQL.PATIENT_NOTIFICATION, PMSSQL.UPDATE_PATIENT_NOTIFICATION,pn);
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::update(Patient)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
   
    @Override
    public void update(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        try {
            if (getPMSStoreConnection().getAutoCommit()) {
                getPMSStoreConnection().setAutoCommit(true);
            }
            runSQL(Store.EntitySQL.SURGERY_DAYS_ASSIGNMENT,
                    PMSSQL.UPDATE_SURGERY_DAYS_ASSIGNMENT, surgeryDaysAssignment);
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::update(HashMap<DayOfWeek,Boolean>)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    public void create(Appointment table) throws StoreException { 
        boolean result = false;
        try {
            getPMSStoreConnection().setAutoCommit(true);
            EntityStoreType value = null;
            runSQL(Store.EntitySQL.APPOINTMENT,PMSSQL.CREATE_APPOINTMENT_TABLE, value);

        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(Appointment))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    public void create(PatientNotification pn) throws StoreException{
        try {
            getPMSStoreConnection().setAutoCommit(true);
            EntityStoreType value = null;
            runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,PMSSQL.CREATE_PATIENT_NOTIFICATION_TABLE, value);
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(PatientNotification))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    
    @Override
    public void create(Patient table) throws StoreException{
        boolean result = false;
        try {
            getPMSStoreConnection().setAutoCommit(true);
            EntityStoreType value = null;
            runSQL(Store.EntitySQL.PATIENT,PMSSQL.CREATE_PATIENT_TABLE, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(Patient))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public void create(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        try {
            getPMSStoreConnection().setAutoCommit(true);
            EntityStoreType value = null;
            runSQL(Store.EntitySQL.SURGERY_DAYS_ASSIGNMENT,PMSSQL.CREATE_SURGERY_DAYS_ASSIGNMENT_TABLE, surgeryDaysAssignment);
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(TheSurgeryDaysTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    public void drop(Appointment table)throws StoreException{
        
    }
    
    
    
    @Override
    public void drop(Patient table) throws StoreException {
    
    }

   
    public void drop(SurgeryDaysAssignment table)throws StoreException{
        try {
            if (getPMSStoreConnection().getAutoCommit()) {
                getPMSStoreConnection().setAutoCommit(true);
            }
            runSQL(Store.EntitySQL.SURGERY_DAYS_ASSIGNMENT,
                    PMSSQL.DROP_SURGERY_DAYS_ASSIGNMENT_TABLE, null);
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.drop(SurgeryDaysAssignment)\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    public List<String[]> importEntityFromCSV(EntityStoreType entity) throws StoreException{
        List<String[]> result = null;
        StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
        if (entity.getIsAppointment()) {
            result = new CSVReader().getAppointmentDBFRecords(storeManager.getAppointmentCSVPath());
        }
        if (entity.getIsPatient()) {
            result = new CSVReader().getPatientDBFRecords(storeManager.getPatientCSVPath());
            
        }
        
        return result;
    }
    /*
    public List<String[]> importFromCSV1(IEntityStoreType entity) throws StoreException {
        List<String[]> result = null;
        if (entity.isAppointmentTable()) {
            result = new CSVReader().getAppointmentDBFRecords(readAppointmentCSVPath());
        }
        if (entity.isPatientTable()) {
            result = new CSVReader().getPatientDBFRecords(readPatientCSVPath());
            
        }
        
        return result;
    }
    */
/*
    public IEntityStoreType importFromCSV(IEntityStoreType entity) throws StoreException {
        IEntityStoreType result = null;
        if (entity.isAppointmentTable()) {
            result = new CSVReaderx().getAppointments(readAppointmentCSVPath());
        }
        if (entity.isPatientTable()) {
            result = new CSVReaderx().getPatients(readPatientCSVPath());
        }
        return result;
    }
    */


    /**
     * Explicit transaction processing enabled for the migration of appointment
     * records -- intention is to lock down the appointment table until the
     * migration of appointment records is complete
     */
    /*
    public void populate(AppointmentTable table) throws StoreException {
        boolean result = false;

        try {
            getMigrationConnection().setAutoCommit(true);
            insertMigratedAppointments(new CSVReaderx().getAppointmentsOldVersion(readAppointmentCSVPath()));//03/12/2021 08:51 update
            IEntityStoreType value = null;
            runSQL(Store.MigrationSQL.APPOINTMENT_TABLE_START_TIME_NORMALISED, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in AccessStore.populate(AppointmentTable) method\n"
                    + "Reason -> unexpected effect on attempt to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getMigrationConnection().commit();
                } else {
                    getMigrationConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore.populate(AppointmentTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }
    */

    /**
     * Explicit transaction processing enabled for the migration of appointment
     * records -- intention is to lock down the appointment table until the
     * migration of appointment records is complete
     */
    /*
    public void populate(PatientTable table)throws StoreException{
        boolean result = false;
        int count;
        try{
            getMigrationConnection().setAutoCommit(true);
            insertMigratedPatients(new CSVReaderx().getPatientsOldVersion(readPatientCSVPath())); //03/12/2021 08:51 update
            count = getPatientTableCount();
            setPatientCount(count);
            migratedPatientsTidied();
            result = true;
        }catch (SQLException ex){
            message = "SQLException message -> " + ex.getMessage() +"\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(PatientTable)\n"
                            + "Reason -> unexpected effect when trying to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
     */
    public void populate(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        try{
            getPMSStoreConnection().setAutoCommit(true);
            runSQL(Store.EntitySQL.SURGERY_DAYS_ASSIGNMENT, 
                PMSSQL.INSERT_SURGERY_DAYS_ASSIGNMENT, surgeryDaysAssignment);
        }catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(SurgeryDaysAssignment)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } 
    }
   
    
    
    public void setPatientCount(int value) {
        patientCount = value;
    }

    public int getNonExistingPatientsReferencedByAppointmentsCount() {
        return nonExistingPatientsReferencedByAppointmentsCount;
    }

    public void setNonExistingPatientsReferencedByAppointmentsCount(int value) {
        nonExistingPatientsReferencedByAppointmentsCount = value;
    }

    

    /**
     * Ensures specified file has the specified extension -- extract the base
     * name of specified file -- remove the specified filename from the
     * specified file -- recreate the specified file with extracted base name
     * specified extension
     *
     * @param file
     * @param extension
     * @return File modified (if required) file specification
     */
    private File setExtensionFor(File file, String extension) {
        String p = file.getPath();
        String name = FilenameUtils.getBaseName(p);
        p = removeFilenameFrom(file.getPath());
        return new File(p + name + extension);
    }

    private String removeFilenameFrom(String file) {
        String result;
        String filename = FilenameUtils.getName(file);
        if (filename.isEmpty()) {
            result = file;
        } else {
            result = file.substring(0, file.length() - filename.length());
        }
        return result;
    }
    
    private EntityStoreType doAppointmentPMSSQL(PMSSQL q, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (q){
            case COUNT_APPOINTMENTS:
                sql = "SELECT COUNT(*) as row_count FROM APPOINTMENT;";
                result = doCount(sql);
                break;
            case COUNT_APPOINTMENTS_FOR_DAY:
                sql = "SELECT COUNT(*) as row_count "
                        + "FROM APPOINTMENT "
                        + "WHERE DatePart(\"yyyy\",a.start) = ? "
                        + "AND  DatePart(\"m\",a.start) = ? "
                        + "AND  DatePart(\"d\",a.start) = ? ;";
                result = doCount(sql);
                break;
            case COUNT_APPOINTMENTS_FOR_PATIENT:
                sql = "SELECT COUNT(*) as row_count "
                        + "FROM APPOINTMENT "
                        + "WHERE PatientKey = ? ;";
                result = doCount(sql);
                break;
            case COUNT_APPOINTMENTS_FROM_DAY:
                sql = "SELECT COUNT(*) as row_count "
                        + "FROM APPOINTMENT "
                        + "WHERE start > ? ;";
                result = doCount(sql);
                break;
            case CREATE_APPOINTMENT_TABLE:
                sql = "CREATE TABLE Appointment ("
                        + "pid LONG PRIMARY KEY, "
                        + "patientKey LONG NOT NULL REFERENCES Patient(pid), "
                        + "start DateTime, "
                        + "duration LONG, "
                        + "notes char);";
                doCreateAppointmentTable(sql);
                break;
            case DELETE_APPOINTMENT:
                sql = "DELETE FROM Appointment WHERE pid = ?;";
                doCancelAppointment(sql, entity);
                break;
                
            case INSERT_APPOINTMENT:
                sql = "INSERT INTO Appointment "
                        + "(PatientKey, Start, Duration, Notes,pid) "
                        + "VALUES (?,?,?,?,?);";
                doInsertAppointment(sql, entity);
                break;
            case READ_APPOINTMENT_NEXT_HIGHEST_KEY:
                sql = "SELECT MAX(pid) as highest_key "
                        + "FROM Appointment;";
                result = doReadAppointmentHighestKey(sql);
                break;
            case READ_APPOINTMENT:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.pid = ?;";
                result = doReadAppointmentWithKey(sql, entity);
                break;
            case READ_APPOINTMENTS:
                result = doReadAppointments(sql,entity);
                break;
            case READ_APPOINTMENTS_FOR_DAY:
                sql = "select *"
                        + "from appointment as a "
                        + "where DatePart(\"yyyy\",a.start) = ? "
                        + "AND  DatePart(\"m\",a.start) = ? "
                        + "AND  DatePart(\"d\",a.start) = ? "
                        + "ORDER BY a.start ASC;";
                result = doReadAppointmentsForDay(sql, entity);
                break;
            case READ_APPOINTMENTS_FROM_DAY:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.Start >= ? "
                        + "ORDER BY a.Start ASC;";
                result = doReadAppointmentsFromDay(sql, entity);
                break;
            case READ_APPOINTMENTS_FOR_PATIENT:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.PatientKey = ? "
                        + "ORDER BY a.Start DESC";
                result = doReadAppointmentsForPatient(sql, entity);
                break;
            case UPDATE_APPOINTMENT:
                sql = "UPDATE Appointment "
                        + "SET PatientKey = ?, "
                        + "Start = ?,"
                        + "Duration = ?,"
                        + "Notes = ?"
                        + "WHERE pid = ? ;";
                doUpdateAppointment(sql, entity);
                break;
        }
        return result;
    }
    
    private EntityStoreType doAppointmentsPMSSQL(PMSSQL q, EntityStoreType entity){
        return null;
    }
    
    private EntityStoreType doPatientPMSSQL(PMSSQL q, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (q){
            case COUNT_PATIENTS:
                sql = "SELECT COUNT(*) as row_count FROM Patient;";
                result = doCount(sql);
                break;
            case CREATE_PATIENT_TABLE:
                sql = "CREATE TABLE Patient ("
                        + "pid Long PRIMARY KEY,"
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
                doCreatePatientTable(sql);
                break;
            case INSERT_PATIENT:
                sql
                    = "INSERT INTO Patient "
                    + "(title, forenames, surname, line1, line2,"
                    + "town, county, postcode,phone1, phone2, gender, dob,"
                    + "isGuardianAPatient,recallFrequency, recallDate, notes,pid, guardianKey) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                doInsertPatient(sql, entity);
                break;
            case READ_PATIENT:
                sql = "SELECT pid, title, forenames, surname, line1, line2, "
                        + "town, county, postcode, gender, dob, isGuardianAPatient, "
                        + "phone1, phone2, recallFrequency, recallDate, notes, guardianKey "
                        + "FROM Patient "
                        + "WHERE pid=?;";
                result = doReadPatientWithKey(sql, entity);
                break;
            case READ_PATIENT_NEXT_HIGHEST_KEY:
                sql = "SELECT MAX(pid) as highest_key "
                        + "FROM Patient;";
                result = doReadHighestKey(sql);
                break;
            case READ_PATIENTS:
                sql = "SELECT * FROM Patient ORDER BY surname, forenames ASC;";;
                result = doReadAllPatients(sql);
                break;
            case UPDATE_PATIENT:
                sql = "UPDATE PATIENT "
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
                    + "WHERE pid = ? ;";
                doUpdatePatient(sql, entity);
                break;
        }
        return result;
    }

    private EntityStoreType doPatientNotificationPMSSQL(PMSSQL q, EntityStoreType entity) throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (q){
            case COUNT_PATIENT_NOTIFICATIONS:
                sql = "SELECT COUNT(*) as row_count FROM PatientNotification;";
                result = doCount(sql);
                break;
            case COUNT_UNACTIONED_PATIENT_NOTIFICATIONS:
                sql = "SELECT COUNT(*) as record_count "
                        + "FROM PatientNotifications "
                        + "WHERE isActioned = false;";
                result = doCount(sql);
            case CREATE_PATIENT_NOTIFICATION_TABLE:
                sql = "CREATE TABLE PatientNotification ("
                        + "pid LONG PRIMARY KEY, "
                        + "patientToNotify LONG NOT NULL REFERENCES Patient(pid), "
                        + "notificationDate DateTime, "
                        + "notificationText char);";
                doCreatePatientNotificationTable(sql);
                break;
            case INSERT_PATIENT_NOTIFICATION:
                sql = "INSERT INTO PatientNotification "
                        + "(patientToNotify, notificationDate, notificationText, pid) "
                        + "VALUES(?,?,?,?);";
                doInsertPatientNotification(sql, entity);
                break;
            case READ_PATIENT_NOTIFICATION_NEXT_HIGHEST_KEY:
                sql = "SELECT MAX(pid) as highest_key "
                        + "FROM PatientNotification;";
                result = doReadHighestKey(sql);
                break;
            case READ_PATIENT_NOTIFICATION:
                sql = "SELECT * "
                        + "FROM PatientNotification "
                        + "WHERE pid = ?;";
                result = doReadPatientNotificationWithKey(sql, entity);
                break;
            case READ_PATIENT_NOTIFICATIONS_FOR_PATIENT:
                sql = "SELECT patientToNotify, notificationDate, notificationText, isActioned, isDeleted pid "
                        + "FROM PatientNotification "
                        + "WHERE patientToNotify = ?;";
                result = doReadPatientNotificationsForPatient(sql, entity);
                break;
            case READ_UNACTIONED_PATIENT_NOTIFICATIONS:
                sql = "SELECT * FROM PatientNotification "
                        + "WHERE IsActioned = false "
                        + "ORDER BY notificationDate DESC;";
                result = doReadPatientNotifications(sql);
                break;
            case READ_PATIENT_NOTIFICATIONS:
                sql = "SELECT * FROM PatientNotification ORDER BY notificationDate DESC;";
                result = doReadPatientNotifications(sql);
                break; 
            case UPDATE_PATIENT_NOTIFICATION:
                sql = "UPDATE PatientNotification "
                        + "SET patientToNotify = ?, "
                        + "notificationDate = ?, "
                        + "notificationText = ?, "
                        + "isActioned = ? "
                        + "WHERE pid = ?;";
                doUpdatePatientNotification(sql, entity);                
        }
        return result;
    }
    
    private void doCreateAppointmentTable(String sql)throws StoreException{
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            preparedStatement.execute();

        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in doCreateAppointmentTable(sql) ",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private void doCreatePatientTable(String sql)throws StoreException{
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised during doCreatePatientTable(sql)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private void doCreatePatientNotificationTable(String sql) throws StoreException{
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised during doCreatePatientNotificationTable(sql)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private void doInsertPatient(String sql, EntityStoreType entity)throws StoreException{
        //ThePatient thePatient = null;
        PatientDelegate delegate = null;
        if (entity != null) {
            if (entity.getIsPatient()) {
                //thePatient = (Patient)entity;
                delegate = (PatientDelegate)entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setString(1, delegate.getName().getTitle());
                    preparedStatement.setString(2, delegate.getName().getForenames());
                    preparedStatement.setString(3, delegate.getName().getSurname());
                    preparedStatement.setString(4, delegate.getAddress().getLine1());
                    preparedStatement.setString(5, delegate.getAddress().getLine2());
                    preparedStatement.setString(6, delegate.getAddress().getTown());
                    preparedStatement.setString(7, delegate.getAddress().getCounty());
                    preparedStatement.setString(8, delegate.getAddress().getPostcode());
                    preparedStatement.setString(9, delegate.getPhone1());
                    preparedStatement.setString(10, delegate.getPhone2());
                    preparedStatement.setString(11, delegate.getGender());
                    if (delegate.getDOB() != null) {
                        preparedStatement.setDate(12, java.sql.Date.valueOf(delegate.getDOB()));
                    } else {
                        preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                    }
                    preparedStatement.setBoolean(13, delegate.getIsGuardianAPatient());
                    preparedStatement.setInt(14, delegate.getRecall().getDentalFrequency());
                    if (delegate.getRecall().getDentalDate() != null) {
                        preparedStatement.setDate(15, java.sql.Date.valueOf(delegate.getRecall().getDentalDate()));
                    } else {
                        preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                    }
                    preparedStatement.setString(16, delegate.getNotes());
                    Integer key = delegate.getPatientKey();
                    preparedStatement.setLong(17, delegate.getPatientKey());
                    preparedStatement.setLong(18,((PatientDelegate)delegate.getGuardian()).getPatientKey());
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doInsertPatient()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> entity invalidly defined, expected patient object, in AccessStore::doInsertPatient()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> entity undefined in AccessStore::doInsertPatient()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private void doInsertPatientNotification(String sql, EntityStoreType entity) throws StoreException{
        PatientNotificationDelegate  delegate = null;
        if (entity != null) {
            if (entity.getIsPatientNotification()) {
                delegate = (PatientNotificationDelegate) entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, ((PatientDelegate)delegate.getPatient()).getPatientKey());
                    preparedStatement.setDate(2, java.sql.Date.valueOf(delegate.getNotificationDate()));
                    preparedStatement.setString(3, delegate.getNotificationText());
                    preparedStatement.setLong(4, delegate.getKey());
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doInsertPatientNotification()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> patient notification defined invalidly in doInsertPatientNotification()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> patient notificaion undefined in doInsertPatientNotification()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private void doUpdatePatientNotification(String sql, EntityStoreType entity) throws StoreException{
        PatientDelegate pDelegate = null;
        PatientNotificationDelegate  delegate = null;
        if (entity != null) {
            if (entity.getIsPatientNotification()) {
                delegate = (PatientNotificationDelegate) entity;
                pDelegate = new PatientDelegate(delegate.getPatient());
                
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, pDelegate.getPatientKey());
                    preparedStatement.setDate(2, java.sql.Date.valueOf(delegate.getNotificationDate()));
                    preparedStatement.setString(3, delegate.getNotificationText());
                    preparedStatement.setBoolean(4, delegate.getIsActioned());
                    preparedStatement.setLong(5, delegate.getKey());
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doUpdatePatientNotification()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> patient notification defined invalidly in doUpdatePatientNotification()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> patient notificaion undefined in doUpdatePatientNotification()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private EntityStoreType doReadAllPatients(String sql) throws StoreException{
        EntityStoreType result = null;
        
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            Patient patient = new Patient();
            result = get(patient.getCollection(), rs);
            return result;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL..) during a READ_ALL_PATIENTS statement",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }   
    }
    
    private EntityStoreType doReadPatientWithKey(String sql, EntityStoreType entity)throws StoreException{
        PatientDelegate delegate = null;
        EntityStoreType result = null;
        Patient patient = null;
        if (entity != null){
            if (entity.getIsPatient()){
                delegate  = (PatientDelegate)entity;
                
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, delegate.getPatientKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(new PatientDelegate(), rs);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadPatientWithKey(sql, EntityStoreType)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> entity not a patient object in AccessStore::doReadPatientWithKey(sql, EntityStoreType)";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> entity undefined in AccessStore::doReadPatientWithKey(sql, EntityStoreType)";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
        return result;
    }
    
    private EntityStoreType doReadPatientWithKeyx(String sql, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        Patient patient = null;
        if (entity != null){
            if (entity.getIsPatient()){
                patient = (Patient)entity;
                
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, ((PatientDelegate)patient).getPatientKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(new PatientDelegate(), rs);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadPatientWithKey(sql, EntityStoreType)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> entity not a patient object in AccessStore::doReadPatientWithKey(sql, EntityStoreType)";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> entity undefined in AccessStore::doReadPatientWithKey(sql, EntityStoreType)";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
        return result;
    }
    
    private EntityStoreType doReadPatientNotifications(String sql)throws StoreException{
        EntityStoreType result = null;
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            result = get(new PatientNotification().getCollection(), rs);
            return result;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::doReadPatientNotifications(sql))",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private EntityStoreType doReadPatientNotificationsForPatient(String sql, EntityStoreType entity)throws StoreException{
        PatientNotification.Collection patientNotifications = null;
        PatientDelegate delegate = null;
        if (entity != null) {
            if (entity.getIsPatientNotifications()) {
                patientNotifications = (PatientNotification.Collection)entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, ((PatientDelegate)delegate).getPatientKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    return get(patientNotifications, rs);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadPatientNotificationsForPatient()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> unexpected entity definition, expecting a patient object, in AccessStore::doReadPatientNotificationForPatient()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> patient notification undefined in Access::doReadPatientNotificationForPatient()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private EntityStoreType doReadPatientNotificationWithKey(String sql, EntityStoreType entity) throws StoreException{
        PatientNotificationDelegate delegate = null;
        if (entity != null) {
            if (entity.getIsPatientNotification()) {
                delegate = (PatientNotificationDelegate) entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, delegate.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    return get(new PatientNotification(), rs);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadPatientNotificationWithKey()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> patient notifiation defined invalidly in AccessStore::doReadPatientNotificationWithKey()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> patient notification undefined in Access::doReadPatientNotificationWithKey()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private EntityStoreType doPMSStoreSQL(PMSSQL q, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        
        String sql = null;
        switch (q){
            case READ_CSV_APPOINTMENT_FILE_LOCATION:
                sql = "Select location from Target WHERE db = 'CSV_APPOINTMENT_FILE';";
                result = (EntityStoreType)doReadFileLocation(sql, (StoreManager.PMS_Store)entity);
                break;
            case READ_CSV_PATIENT_FILE_LOCATION:
                sql = "Select location from Target WHERE db = 'CSV_PATIENT_FILE';";
                result = doReadFileLocation(sql, (StoreManager.PMS_Store)entity);
                break;
            case READ_PMS_STORE_LOCATION:
                sql = "Select location from Target WHERE db = 'STORE_DB';";
                result = doReadFileLocation(sql, (StoreManager.PMS_Store)entity);
                break;
            case UPDATE_CSV_APPOINTMENT_FILE_LOCATION:
                sql = "UPDATE Target SET location = ? WHERE db = 'CSV_APPOINTMENT_FILE';";
                doUpdateFileLocation(sql, (StoreManager.PMS_Store)entity);
                break;
            case UPDATE_CSV_PATIENT_FILE_LOCATION:
                sql = "UPDATE Target SET location = ? WHERE db = 'CSV_PATIENT_FILE';";
                doUpdateFileLocation(sql, (StoreManager.PMS_Store)entity);
                break;
            case UPDATE_PMS_STORE_LOCATION:
                sql = "UPDATE Target SET location = ? WHERE db = 'STORE_DB';";
                doUpdateFileLocation(sql, (StoreManager.PMS_Store)entity);
                break;
        }
        return result;
    }
    
    
    private void doUpdateFileLocation(String sql, StoreManager.PMS_Store pmsStore)throws StoreException{
        try {
            getTargetConnection().setAutoCommit(true);
            PreparedStatement preparedStatement = getTargetConnection().prepareStatement(sql);
            preparedStatement.setString(1, pmsStore.get());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::doUpdateFileLocation(sql, PMS_Store)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private EntityStoreType doReadFileLocation(String sql, StoreManager.PMS_Store pmsStore)throws StoreException{
        String location = null;
        try {
            getTargetConnection().setAutoCommit(false);
            PreparedStatement preparedStatement = getTargetConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                location = rs.getString("location");
                pmsStore.set(location);
            }
            else pmsStore.set(null);
            return pmsStore;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised during doReadFileLocation(sql)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private EntityStoreType doSurgeryDaysAssignmentPMSSQL(PMSSQL q, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (q){
            case COUNT_SURGERY_DAYS_ASSIGNMENT:
                sql = "SELECT COUNT(*) as row_count FROM SurgeryDays;";
                result = doCount(sql);
                break;
            case CREATE_SURGERY_DAYS_ASSIGNMENT_TABLE:
                sql = "CREATE TABLE SurgeryDays ("
                        + "Day Char(10),"
                        + "IsSurgery YesNo);";
                doCreateSurgeryDaysAssignmentTable(sql);
                break;
            case DROP_SURGERY_DAYS_ASSIGNMENT_TABLE:
                sql = "DROP TABLE SurgeryDays;";
                doDropSurgeryDaysAssignmentTable(sql);
                break;
            case READ_SURGERY_DAYS_ASSIGNMENT:
                sql = "SELECT Day, IsSurgery FROM SurgeryDays;";
                result = doReadSurgeryDaysAssignment(sql);
                break;
            case INSERT_SURGERY_DAYS_ASSIGNMENT:
                doInsertSurgeryDaysAssignment(entity);
                break;
            case UPDATE_SURGERY_DAYS_ASSIGNMENT:
                doUpdateSurgeryDaysAssignment(entity);
                      
        }
        return result;
    }
    
    private EntityStoreType doReadHighestKey(String sql) throws StoreException{
        try {
            Integer key = null;
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                key = (int) rs.getLong("highest_key");
            } else {
                key = 0;
            }
            return new TableRowValue(key);
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::doReadHighestKey()",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private void doCreateSurgeryDaysAssignmentTable(String sql)throws StoreException{
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException ex) {

            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised during AccessStore::doCreateSurgeryDaysAssignmentTable()",
                    StoreException.ExceptionType.SQL_EXCEPTION);

        }
    }
    
    private void doDropSurgeryDaysAssignmentTable(String sql) throws StoreException{
        try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a DROP_APPOINTMENT_TABLE data migration operation",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                     */
                }
    }
    
    private void doInsertSurgeryDaysAssignment(EntityStoreType entity ) throws StoreException{
        SurgeryDaysAssignment surgeryDaysAssignment = null;
        if (entity != null) {
            if (entity.getIsSurgeryDaysAssignment()) {
                surgeryDaysAssignment = (SurgeryDaysAssignment)entity;
                for (Entry<DayOfWeek, Boolean> entry : surgeryDaysAssignment.get().entrySet()) {
                    String sql = "INSERT INTO SurgeryDays (Day, IsSurgery) VALUES(?, ?);";
                    try {
                        PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                        preparedStatement.setBoolean(2, entry.getValue());
                        switch (entry.getKey()) {
                            case MONDAY:
                                preparedStatement.setString(1, "Monday");
                                break;
                            case TUESDAY:
                                preparedStatement.setString(1, "Tuesday");
                                break;
                            case WEDNESDAY:
                                preparedStatement.setString(1, "Wednesday");
                                break;
                            case THURSDAY:
                                preparedStatement.setString(1, "Thursday");
                                break;
                            case FRIDAY:
                                preparedStatement.setString(1, "Friday");
                                break;
                            case SATURDAY:
                                preparedStatement.setString(1, "Saturday");
                                break;
                            case SUNDAY:
                                preparedStatement.setString(1, "Sunday");
                                break;
                        }
                        preparedStatement.execute();
                    } catch (SQLException ex) {
                        throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                + "StoreException message -> exception raised in AccessStore::doInsertSurgeryDaysAssignment()",
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }
                }
            } else {
                String message = "StoreException -> entity wrongly defined in AccessStore::doInsertSurgeryDaysAssignment()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> entity undefined in AccessStore::doInsertSurgeryDaysAssignment()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private SurgeryDaysAssignment doReadSurgeryDaysAssignment(String sql)throws StoreException{
        SurgeryDaysAssignment surgeryDaysAssignment = null;
        try {
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null) {
                surgeryDaysAssignment = (SurgeryDaysAssignment) get(new SurgeryDaysAssignment(), rs);
            }
            return surgeryDaysAssignment;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::runSQL(PMS.READ_SURGERY_DAYS)",
                    StoreException.ExceptionType.SURGERY_DAYS_TABLE_MISSING_IN_PMS_DATABASE);
        }
    }
    
    private void doUpdateSurgeryDaysAssignment(EntityStoreType entity)throws StoreException{
        SurgeryDaysAssignment surgeryDaysAssignment = null;
        if (entity != null) {
            if (entity.getIsSurgeryDaysAssignment()) {
                surgeryDaysAssignment = (SurgeryDaysAssignment)entity;
                try {
                    for (Entry<DayOfWeek, Boolean> entry : surgeryDaysAssignment.get().entrySet()) {
                        String sql = "UPDATE SurgeryDays SET IsSurgery = ? WHERE Day = ?;";
                        PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                        preparedStatement.setBoolean(1, entry.getValue());
                        switch (entry.getKey()) {
                            case MONDAY:
                                preparedStatement.setString(2, "Monday");
                                break;
                            case TUESDAY:
                                preparedStatement.setString(2, "Tuesday");
                                break;
                            case WEDNESDAY:
                                preparedStatement.setString(2, "Wednesday");
                                break;
                            case THURSDAY:
                                preparedStatement.setString(2, "Thursday");
                                break;
                            case FRIDAY:
                                preparedStatement.setString(2, "Friday");
                                break;
                            case SATURDAY:
                                preparedStatement.setString(2, "Saturday");
                                break;
                            case SUNDAY:
                                preparedStatement.setString(2, "Sunday");
                                break;
                        }
                        preparedStatement.execute();
                    }

                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doUpdateSurgeryDaysAssignment()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else {
                String message = "StoreException -> entity wrongly defined in AccessStore::doUpdateSurgeryDaysAssignment()";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> entity undefined in AccessStore::doUpdateSurgeryDaysAssignment()";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private void doUpdatePatient(String sql, EntityStoreType entity)throws StoreException{
        if (entity != null){
            if (entity.getIsPatient()){
                PatientDelegate delegate = (PatientDelegate)entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setString(1, delegate.getName().getTitle());
                    preparedStatement.setString(2, delegate.getName().getForenames());
                    preparedStatement.setString(3, delegate.getName().getSurname());
                    preparedStatement.setString(4, delegate.getAddress().getLine1());
                    preparedStatement.setString(5, delegate.getAddress().getLine2());
                    preparedStatement.setString(6, delegate.getAddress().getTown());
                    preparedStatement.setString(7, delegate.getAddress().getCounty());
                    preparedStatement.setString(8, delegate.getAddress().getPostcode());
                    preparedStatement.setString(9, delegate.getPhone1());
                    preparedStatement.setString(10, delegate.getPhone2());
                    preparedStatement.setString(11, delegate.getGender());
                    if (delegate.getDOB() != null) {
                        preparedStatement.setDate(12, java.sql.Date.valueOf(delegate.getDOB()));
                    } else {
                        preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                    }
                    preparedStatement.setBoolean(13, delegate.getIsGuardianAPatient());
                    preparedStatement.setInt(14, delegate.getRecall().getDentalFrequency());
                    if (delegate.getRecall().getDentalDate() != null) {
                        preparedStatement.setDate(15, java.sql.Date.valueOf(delegate.getRecall().getDentalDate()));
                    } else {
                        preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                    }
                    preparedStatement.setString(16, delegate.getNotes());
                    if (delegate.getIsGuardianAPatient()) {
                        preparedStatement.setLong(17, ((PatientDelegate)delegate.getGuardian()).getPatientKey());
                    } else {
                        preparedStatement.setLong(17, 0);
                    }
                    preparedStatement.setLong(18, delegate.getPatientKey());
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doUpdatePatient(sql, EntityStoreType)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else {
                String message = "StoreException -> entity undefined in AccessStore::doUpdatePatient(sql, EntityStoreType)";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } else {
            String message = "StoreException -> entity undefined in AccessStore::doUpdatePatient(sql, EntityStoreType)";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
    }
    
    private void doUpdateAppointment(String sql, EntityStoreType entity)throws StoreException{
        if (entity != null){
            if (entity.getIsAppointment()){
                AppointmentDelegate delegate = (AppointmentDelegate)entity;
                try{
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    if (delegate.getPatient() != null) {
                        preparedStatement.setInt(1, ((PatientDelegate)delegate.getPatient()).getPatientKey());
                    }
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(delegate.getStart()));
                    preparedStatement.setLong(3, delegate.getDuration().toMinutes());
                    preparedStatement.setString(4, delegate.getNotes());
                    preparedStatement.setLong(5, delegate.getAppointmentKey());
                    preparedStatement.executeUpdate();
                }catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                + "StoreException message -> exception raised in AccessStore::doUpdateAppointment(sql, entity",
                                StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = "StoreException -> entity wrongly defined in AccessStore::doUpdateAppointment(sql, entity)";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }else{
            String message = "StoreException -> entity undefined in AccessStore::doUpdateAppointment(sql, entity)";
            throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
        }
            
    }
    
    private void doCancelAppointment(String sql, EntityStoreType entity)throws StoreException{
        if (entity.getIsAppointment()){
            AppointmentDelegate delegate = (AppointmentDelegate)entity;
            try{
                PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                preparedStatement.setInt(1, ((AppointmentDelegate)delegate).getAppointmentKey());
                preparedStatement.executeUpdate();
            }catch (SQLException ex){
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doCancelAppointment(String sql, EntityStoreType entity)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }
    
    private void doInsertAppointment(String sql, EntityStoreType entity)throws StoreException{
        if (entity.getIsAppointment()){
            AppointmentDelegate delegate = (AppointmentDelegate)entity;
            try {
                PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                preparedStatement.setInt(1, ((PatientDelegate)delegate.getPatient()).getPatientKey());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(delegate.getStart()));
                preparedStatement.setLong(3, delegate.getDuration().toMinutes());
                preparedStatement.setString(4, delegate.getNotes());
                preparedStatement.setLong(5, delegate.getAppointmentKey());
                preparedStatement.executeUpdate();
                /* ref to note -> 27/05/2022 08:26
                if (preparedStatement.executeUpdate() == 0){
                    message = "StoreException raised in method "
                            + "AccessStore::doInsertAppointment() "
                            + "because record insertion  failed";
                    throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                */   
            } catch (SQLException ex) {
                if (!(ex.getMessage().contains("foreign key no parent"))
                        && !(ex.getMessage().contains("Missing columns in relationship"))) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::runSQL(PracticeManagementSystemSQL.INSERT_APPOINTMENT)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }
        }
    }
        
    private Appointment doReadAppointmentWithKey(String sql, EntityStoreType entity) throws StoreException{
        Appointment appointment = null;
        if (entity!=null){
            if (entity.getIsAppointment()){
                //appointment = (Appointment)entity;
                AppointmentDelegate delegate = (AppointmentDelegate)entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, delegate.getAppointmentKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    appointment = get(new Appointment(), rs);
                    
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadAppointmentWithKey(sql, EntityStoreType)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }
        }
        return appointment;            
    }

    private TableRowValue doReadAppointmentHighestKey(String sql)throws StoreException{
        try {
            Integer key = null;
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                key = (int) rs.getLong("highest_key");
            } else {
                key = 0;
            }
            return new TableRowValue(key);
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::runSQL(AppointmentSQL..) during execution of an READ_HIGHEST_KEY statement",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private EntityStoreType doReadAppointments(String sql, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        Integer value = null;
        Appointment.Collection appointments = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointments = (Appointment.Collection)entity;
                try{
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(appointments, rs);
                }catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }   
        return result;
    }
    
    private EntityStoreType doReadAppointmentsForDay(String sql, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        Integer value = null;
        Appointment.Collection appointments = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointments = (Appointment.Collection)entity;
                try{
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    LocalDate day = appointments.getAppointment().getStart().toLocalDate();
                    preparedStatement.setInt(1, day.getYear());
                    preparedStatement.setInt(2, day.getMonthValue());
                    preparedStatement.setInt(3, day.getDayOfMonth());
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(appointments, rs);
                }catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }   
        return result;
    }
    
    private EntityStoreType doReadAppointmentsFromDay(String sql, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        Integer value = null;
        Appointment.Collection appointments = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointments = (Appointment.Collection)entity;
                try{
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    LocalDate day = appointments.getAppointment().getStart().toLocalDate();
                    preparedStatement.setDate(1, java.sql.Date.valueOf(day));
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(appointments, rs);
                }catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }   
        return result;
    }
    
    private EntityStoreType doReadAppointmentsForPatient(String sql, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        Integer value = null;
        Appointment.Collection appointments = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointments = (Appointment.Collection)entity;
                try{
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, 
                            ((PatientDelegate)appointments.getAppointment().getPatient()).getPatientKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(appointments, rs);
                }catch (SQLException ex){
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doReadAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doReadAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }   
        return result;
    }
    
    private EntityStoreType doCountAppointmentsForPatient(String sql, EntityStoreType entity)throws StoreException{
        Integer value = null;
        Appointment.Collection appointments = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointments = (Appointment.Collection)entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, 
                            ((PatientDelegate)appointments.getAppointment().getPatient()).getPatientKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next())value = rs.getInt("row_count");
                    else value = 0;
                    return new TableRowValue(value);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doCountAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doCountAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }

        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doCountAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }
    }
    
    private EntityStoreType doCountAppointmentsFromDay(String sql, EntityStoreType entity)throws StoreException{
        Integer value = null;
        Appointment appointment = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointment = (Appointment)entity; 
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setDate(1, 
                            java.sql.Date.valueOf(appointment.getStart().toLocalDate()));
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next())value = rs.getInt("row_count");
                    else value = 0;
                    return new TableRowValue(value);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doCountAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doCountAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }

        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doCountAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }
    }
    private EntityStoreType doCountAppointmentsForDay(String sql, EntityStoreType entity)throws StoreException{
        Appointment appointment = null;
        Integer value = null;
        if (entity != null) {
            if (entity.getIsAppointments()){
                appointment = (Appointment)entity;
                try {
                    PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
                    preparedStatement.setInt(1, appointment.getStart().getYear());
                    preparedStatement.setInt(2, appointment.getStart().getMonthValue());
                    preparedStatement.setInt(3, appointment.getStart().getDayOfMonth());
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next())value = rs.getInt("row_count");
                    else value = 0;
                    return new TableRowValue(value);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::doCountAppointmentsForDay()",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }else{
                String message = 
                        "Unexpected data type specified for entity in AccessStore::doCountAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }

        }else{
            String message = 
                        "Entity data type undefined in AccessStore::doCountAppointmentsForDay()";
                throw new StoreException(
                        message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
        }

    }
    
    private EntityStoreType doCount(String sql)throws StoreException{
        Integer value = null;
        try{
            PreparedStatement preparedStatement = getPMSStoreConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next())value = rs.getInt("row_count");
            else value = 0;
            return new TableRowValue(value);
        }catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::doCount()",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    /**
     * TargetStoreActions
     * -- creates a new database at the specified location
     * -- reads the specified store location
     * -- updates the specified store location
     */
    
    /**
     * Creates a database file as per the received specification
     * @param file; File (database) created according to the received location information
     * @return
     * @throws StoreException 
     */
    @Override
    public File initialiseTargetStore(File file) throws StoreException {
        try {
            file = setExtensionFor(file, ".accdb");
            DatabaseBuilder.create(Database.FileFormat.V2016, file);
            return file;
        } catch (IOException io) {
            String message = "IOException -> raised on attempt to create a new Access database in DesktopControllerActionEvent.MIGRATION_DATABASE_CREATION_REQUEST";
            throw new StoreException(message + "\nStoreException raised in "
                    + "initialiseTargetStore(file = "
                    + file.toString() + ")", StoreException.ExceptionType.IO_EXCEPTION);
        }
    }
    
    /**
     * Reads the PMS store from the specified path
     * @param pmsStore; PMS_Storex which specified the path of the store to be read
     * @return
     * @throws StoreException 
     */
    @Override
    public EntityStoreType read(StoreManager.PMS_Store pmsStore)throws StoreException{
        EntityStoreType value = null;
        switch (pmsStore.getScope()){
            case CSV_APPOINTMENT_FILE:
                value = runSQL(Store.EntitySQL.PMS_STORE, 
                        PMSSQL.READ_CSV_APPOINTMENT_FILE_LOCATION,pmsStore);
                break;
            case CSV_PATIENT_FILE:
                value = runSQL(Store.EntitySQL.PMS_STORE, 
                        PMSSQL.READ_CSV_PATIENT_FILE_LOCATION,pmsStore);
                break;
            case PMS_STORE:
                value = runSQL(Store.EntitySQL.PMS_STORE, 
                        PMSSQL.READ_PMS_STORE_LOCATION,pmsStore);
                break;       
        }
        return value;
    }

    /**
     * Updates the store path of the specified store
     * @param pmsStore; PMS_Storex object specifying the updated store path 
     * @throws StoreException 
     */
    @Override
    public void update(StoreManager.PMS_Store pmsStore)throws StoreException{
        EntityStoreType value = null;
        String url = null;
        switch (pmsStore.getScope()){
            case CSV_APPOINTMENT_FILE:
                runSQL(Store.EntitySQL.PMS_STORE,
                        PMSSQL.UPDATE_CSV_APPOINTMENT_FILE_LOCATION,pmsStore);
                break;
            case CSV_PATIENT_FILE:
                runSQL(Store.EntitySQL.PMS_STORE,
                        PMSSQL.UPDATE_CSV_PATIENT_FILE_LOCATION,pmsStore);
                break;
            case PMS_STORE:
                runSQL(Store.EntitySQL.PMS_STORE,
                        PMSSQL.UPDATE_PMS_STORE_LOCATION,pmsStore);
                try{
                    if (!FilenameUtils.getName(pmsStore.get()).equals("")){
                        url = "jdbc:ucanaccess://" + pmsStore.get() + ";showSchema=true";
                        PMSstoreConnection = DriverManager.getConnection(url);
                    }else getPMSStoreConnection().close();
                }catch (SQLException ex){
                    throw new StoreException(ex.getMessage() + "\n"
                            + "StoreException raised in AccessStore::update(PMS_Store)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
        } 
    }
}
