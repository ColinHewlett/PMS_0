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
import clinicpms.model.PatientNotification;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.AppointmentTable;
import clinicpms.model.AppointmentTableRowValue;
import clinicpms.model.PatientTableRowValue;
import clinicpms.model.TableRowValue;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.store.Store.SelectedTargetStore;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.ThePatient;
import clinicpms.model.Patients;
import clinicpms.model.AppointmentDate;
import clinicpms.model.Appointments;
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
import clinicpms.model.IEntityStoreType;
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
    private String message = null;
    private int nonExistingPatientsReferencedByAppointmentsCount = 0;
    private int patientCount = 0;
    private ArrayList<Appointment> appointments = null;

    /**
     * If on entry migration connection is undefined the migration database path
     * is used to make a new connection -- method assumes on entry the migration
     * database path is defined
     *
     * @return Connection object
     * @throws StoreException
     */
    private Connection getMigrationConnection() throws StoreException {
        String path;
        String errorMessage = null;
        String url;
        try {
            if (getMigrationDatabasePath() == null) {
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

    /**
     * If on entry pms connection is undefined the pms database path is used to
     * make a new connection -- method assumes on entry the pms database path is
     * defined
     *
     * @return Connection object
     * @throws StoreException
     */
    private Connection getPMSConnection() throws StoreException {
        try {
            if (this.pmsConnection == null) {
                /**
                 * DEBUG -- add check and action in case getPMSDatabasePath() is
                 * null
                 */
                if (getPMSDatabasePath() == null) {
                    readPMSTargetStorePath();
                }
                //DEBUG end
                String url = "jdbc:ucanaccess://" + getPMSDatabasePath() + ";showSchema=true";
                pmsConnection = DriverManager.getConnection(url);
            }

        } catch (SQLException ex) {//message handling added
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::getPMSConnection() method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }

        return pmsConnection;
    }//store_package_updates_05_12_21_09_17_devDEBUG

    /**
     * If on entry migration connection is defined the connection is closed
     *
     * @throws StoreException
     */
    public void closeMigrationConnection() throws StoreException {
        try {
            /**
             * DEBUG -- use of connection getter avoided to prevent stack
             * overflow (recursive reentry issue)
             */
            if (migrationConnection != null) {
                migrationConnection.commit();
                migrationConnection.close();
            }
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::closeMigrationConnection() method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }//store_package_updates_05_12_21_09_17_devDEBUG

    /**
     * If on entry migration connection is defined the connection is closed
     *
     * @throws StoreException
     */
    private void closePMSConnection() throws StoreException {
        try {
            /**
             * DEBUG -- use of connection getter avoided to prevent stack
             * overflow (recursive reentry issue)
             */
            if (pmsConnection != null) {
                pmsConnection.close();
            }
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::closePMSConnection() method",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }//store_package_updates_05_12_21_09_17_devDEBUG

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
     * include -- APPOINTMENTS_COUNT -- DELETE_APPOINTMENT_WITH_KEY --
     * DELETE_APPOINTMENTS_WITH_PATIENT_KEY --
     * DELETE_APPOINTMENTS_WITH_PATIENT_KEY -- INSERT_APPOINTMENT --
     * READ_APPOINTMENT_WITH_KEY -- READ_APPOINTMENTS_FOR_DAY --
     * READ_APPOINTMENTS_FROM_DAY -- READ_HIGHEST_KEY -- UPDATE_APPOINTMENT
     * @param entity:Object checked casting to either an Appointment or Patient
     * or LocalDate object (null if not required)
     * @return ArrayList<Appointment> containing one or more Appointment
     * objects. -- an Appointment object (using its key property) is used to
     * wrap the row count or highest key value in if requested -- null is
     * returned on an INSERT_APPOINTMENT or UPDATE_APPOINTMENT is requested
     * @throws StoreException wraps the SQLException that can be thrown on
     * execution of the SQL statement
     */
    private EntityStoreType runSQL(EntitySQL entitySQL, PMSSQL pmsSQL, EntityStoreType entity)throws StoreException{
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
        }
        return result;
    }
    
    private IEntityStoreType runSQL(PMSSQL q, IEntityStoreType entity) throws StoreException {
        IEntityStoreType result = null;

        String sql = null;
        switch (q) {
            case SURGERY_DAYS_TABLE_CREATE:
                sql = "CREATE TABLE SurgeryDays ("
                        + "Day Char(10),"
                        + "IsSurgery YesNo);";
                break;
            case APPOINTMENT_TABLE_DROP:
                sql = "DROP TABLE Appointment;";
                break;
            case APPOINTMENT_TABLE_CREATE:
                sql = "CREATE TABLE Appointment ("
                        + "pid LONG PRIMARY KEY, "
                        + "patientKey LONG NOT NULL REFERENCES Patient(pid), "
                        + "start DateTime, "
                        + "duration LONG, "
                        + "notes char);";
                break;
            case PATIENT_TABLE_DROP:
                sql = "DROP TABLE Patient;";
                break;
            case PATIENT_TABLE_CREATE:
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
                break;
            case SURGERY_DAYS_TABLE_DROP:
                sql = "DROP TABLE SurgeryDays;";
                break;
            case APPOINTMENTS_COUNT:
                sql = "SELECT COUNT(*) as record_count "
                        + "FROM Appointment;";
                break;
            case READ_APPOINTMENT_HIGHEST_KEY:
                sql = "SELECT MAX(pid) as highest_key "
                        + "FROM Appointment;";
                break;
            case DELETE_APPOINTMENT_WITH_KEY:
                sql = "DELETE FROM Appointment WHERE pid = ?;";
                break;
            case DELETE_APPOINTMENTS_WITH_PATIENT_KEY:
                sql = "DELETE FROM Appointment a WHERE a.patientKey = ?;";
                break;
            case INSERT_APPOINTMENT:
                sql = "INSERT INTO Appointment "
                        + "(PatientKey, Start, Duration, Notes,pid) "
                        + "VALUES (?,?,?,?,?);";
                break;
            case READ_APPOINTMENT_WITH_KEY:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.pid = ?;";
                break;
            case READ_APPOINTMENTS_FROM_DAY:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.Start >= ? "
                        + "ORDER BY a.Start ASC;";
                break;
            case READ_APPOINTMENTS_FOR_PATIENT:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.PatientKey = ? "
                        + "ORDER BY a.Start DESC";
                break;
            case READ_APPOINTMENTS:
                sql = "SELECT * "
                        + "FROM Appointment; ";
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
                        + "WHERE pid = ? ;";
                break;
            case READ_PATIENT_HIGHEST_KEY:
                sql
                        = "SELECT MAX(pid) as highest_key "
                        + "FROM Patient;";
                break;
            case INSERT_PATIENT:
                sql
                        = "INSERT INTO Patient "
                        + "(title, forenames, surname, line1, line2,"
                        + "town, county, postcode,phone1, phone2, gender, dob,"
                        + "isGuardianAPatient, recallFrequency, recallDate, notes,pid) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                break;
            case READ_PATIENT_WITH_KEY:
                sql
                        = "SELECT pid, title, forenames, surname, line1, line2, "
                        + "town, county, postcode, gender, dob, isGuardianAPatient, "
                        + "phone1, phone2, recallFrequency, recallDate, notes, guardianKey "
                        + "FROM Patient "
                        + "WHERE pid=?;";
                break;
            case READ_ALL_PATIENTS:
                sql = "SELECT * FROM Patient ORDER BY surname, forenames ASC;";
                break;
            case UPDATE_PATIENT:
                sql
                        = "UPDATE PATIENT "
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
                break;
            case PATIENTS_COUNT:
                sql = "SELECT COUNT(*) as record_count "
                        + "FROM Patient;";
                break;
            case READ_SURGERY_DAYS:
                sql = "SELECT Day, IsSurgery FROM SurgeryDays;";
                break;
        }
        switch (q) {
            case CREATE_PRIMARY_KEY: {
                String table = null;
                if (entity != null) {
                    if (entity.isAppointments()) {
                        table = "Appointment";
                        sql = "ALTER TABLE AppointmentTable "
                                + "ADD PRIMARY KEY (pid);";
                    } else if (entity.isPatients()) {
                        table = "Patient";
                        sql = "ALTER TABLE Patient "
                                + "ADD PRIMARY KEY (pid);";
                    }
                    try {
                        PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                        preparedStatement.execute();

                    } catch (SQLException ex) {
                        throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                + "StoreException -> exception raised during a PracticeManagementSystem.CREATE_PRIMARY_KEY (",
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }

                }
                break;
            }
            case APPOINTMENT_TABLE_DROP: {
                /**
                 * Update to fix problem when trying to drop a non-existent
                 * appointment table -- run a query on MSysObjects table to see
                 * if table exists or not -- documented solution doesn't work
                 * (Access doesn't like the "sys" prefix)
                 *
                 * String sql_ = "SELECT COUNT(*) as the_count FROM
                 * sys.MSysObjects WHERE name = 'Appointment';";
                 */
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    if (ex.getMessage().contains("object not found: APPOINTMENT")) {
                        throw new StoreException("StoreException -> " + ex.getMessage(),
                                StoreException.ExceptionType.DROP_APPOINTMENT_TABLE_EXCEPTION);
                    } else {
                        throw new StoreException("StoreException -> " + ex.getMessage(),
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }
                }
                break;
            }
            case APPOINTMENT_TABLE_CREATE: {
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.execute();

                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::runSQL(PracticeManagementSystemSQL.APPOINTMENT_CREATE) ",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case SURGERY_DAYS_TABLE_CREATE: {
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {

                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during runSQL(PMSSQL.SURGERY_DAYS_TABLE_CREATE)",
                            StoreException.ExceptionType.SQL_EXCEPTION);

                }
                break;
            }
            case PATIENT_TABLE_DROP: {
                /**
                 * Update to fix problem when trying to drop a non-existent
                 * appointment table -- run a query on MSysObjects table to see
                 * if table exists or not -- documented solution doesn't work
                 * (Access doesn't like the "sys" prefix)
                 *
                 * String sql_ = "SELECT COUNT(*) as the_count FROM
                 * sys.MSysObjects WHERE name = 'Appointment';";
                 */
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    if (ex.getMessage().contains("object not found: PATIENT")) {
                        throw new StoreException("StoreException -> " + ex.getMessage(),
                                StoreException.ExceptionType.DROP_PATIENT_TABLE_EXCEPTION);
                    } else {
                        throw new StoreException("StoreException -> " + ex.getMessage(),
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }
                }
                break;
            }
            case PATIENT_TABLE_CREATE: {
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a runSQL(PracticeManagementSystemSQL.PATIENT_TABLE_CREATE)",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case SURGERY_DAYS_TABLE_DROP: {
                /**
                 * Update to fix problem when trying to drop a non-existent
                 * appointment table -- run a query on MSysObjects table to see
                 * if table exists or not -- documented solution doesn't work
                 * (Access doesn't like the "sys" prefix)
                 *
                 * String sql_ = "SELECT COUNT(*) as the_count FROM
                 * sys.MSysObjects WHERE name = 'Appointment';";
                 */
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_TABLE_DROP data migration operation",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                     */
                }
                break;
            }
            case APPOINTMENTS_COUNT: {
                try {
                    Integer key = null;
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt("record_count");
                        result = new AppointmentTableRowValue(count);
                    }
                } catch (SQLException ex) {
                    if (ex.getMessage().contains("missing table")) {
                        throw new StoreException("StoreException -> " + ex.getMessage(),
                                StoreException.ExceptionType.APPOINTMENT_TABLE_MISSING_IN_PMS_DATABASE);
                    } else {
                        throw new StoreException("StoreException -> " + ex.getMessage(),
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }
                }
                break;
            }
            case READ_APPOINTMENT_HIGHEST_KEY: {
                try {
                    Integer key = null;
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        key = (int) rs.getLong("highest_key");
                    } else {
                        key = 0;
                    }
                    result = new AppointmentTableRowValue(key);
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::runSQL(AppointmentSQL..) during execution of an READ_HIGHEST_KEY statement",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case DELETE_APPOINTMENT_WITH_KEY: {
                Appointment appointment = null;
                if (entity != null) {
                    if (entity.isAppointment()) {
                        appointment = (Appointment) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setInt(1, appointment.getKey());
                            preparedStatement.executeUpdate();
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL..) during execution of an DELETE_APPOINTMENT_WITH_KEY statemente",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.DELETE_APPOINTMENT_WITH_KEY query";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.DELETE_APPOINTMENT_WITH_KEY query";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case DELETE_APPOINTMENTS_WITH_PATIENT_KEY: {
                Appointment appointment = null;
                if (entity != null) {
                    if (entity.isAppointment()) {
                        appointment = (Appointment) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            if (appointment != null) {
                                if (appointment.getPatient().getKey() != null) {
                                    preparedStatement.setInt(1, appointment.getPatient().getKey());
                                }
                                preparedStatement.execute();
                            }
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.DELETE_APPOINTMENTS_WITH_PATIENT_KEY) query",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.DELETE_APPOINTMENTS_WITH_PATIENT_KEY query";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.DELETE_APPOINTMENTS_WITH_PATIENT_KEY query";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case INSERT_SURGERY_DAYS_ASSIGNMENT: {
                SurgeryDaysAssignment surgeryDaysAssignment = null;
                if (entity != null) {
                    if (entity.isSurgeryDaysAssignment()) {
                        for (Entry<DayOfWeek, Boolean> entry : surgeryDaysAssignment.entrySet()) {
                            sql = "INSERT INTO SurgeryDays (Day, IsSurgery) VALUES(?, ?);";
                            try {
                                PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
                                        + "StoreException message -> exception raised in AccessStore::runSQL(PMSSQL.INSERT_SURGERY_DAYS_ASSIGNMENT)",
                                        StoreException.ExceptionType.SQL_EXCEPTION);
                            }
                        }
                    } else {
                        String message = "StoreException -> entity wrongly defined in AccessStore::runSQL(PMSSQL.INSERT_SURGERY_DAYS_ASSIGNMENT)";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> entitu undefined in AccessStore::runSQL(PMSSQL.INSERT_SURGERY_DAYS_ASSIGNMENT)";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case INSERT_APPOINTMENT: {
                Appointment appointment = null;
                if (entity != null) {
                    if (entity.isAppointment()) {
                        appointment = (Appointment) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setInt(1, appointment.getPatient().getKey());
                            preparedStatement.setTimestamp(2, Timestamp.valueOf(appointment.getStart()));
                            preparedStatement.setLong(3, appointment.getDuration().toMinutes());
                            preparedStatement.setString(4, appointment.getNotes());
                            preparedStatement.setLong(5, appointment.getKey());
                            preparedStatement.executeUpdate();
                        } catch (SQLException ex) {
                            if (!(ex.getMessage().contains("foreign key no parent"))
                                    && !(ex.getMessage().contains("Missing columns in relationship"))) {
                                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                        + "StoreException message -> exception raised in AccessStore::runSQL(PracticeManagementSystemSQL.INSERT_APPOINTMENT)",
                                        StoreException.ExceptionType.SQL_EXCEPTION);
                            }
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.INSERT_APPOINTMENT";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.INSERT_APPOINTMENT";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_APPOINTMENT_WITH_KEY: {
                Appointment appointment = null;
                if (entity != null) {
                    if (entity.isAppointment()) {
                        appointment = (Appointment) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setLong(1, appointment.getKey());
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(new Appointment(), rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_APPOINTMENT_WITH_KEY)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENT_WITH_KEY";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENT_WITH_KEY";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_APPOINTMENTS:
                try {
                PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                result = get(new Appointments(), rs);
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_APPOINTMENTS)",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
            break;
            case READ_APPOINTMENTS_FROM_DAY: {
                LocalDate day = null;
                if (entity != null) {
                    if (entity.isAppointmentDate()) {
                        day = ((AppointmentDate) entity).getValue();
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setDate(1, java.sql.Date.valueOf(day));
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(new Appointments(), rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_APPOINTMENTS_FROM_DAY)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENTS_FROM_DAY";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENTS_FROM_DAY";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_APPOINTMENTS_FOR_PATIENT: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setLong(1, patient.getKey());
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(new Appointments(), rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_APPOINTMENTS_FOR_PATIENT)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENTS_FOR_PATIENT";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENTS_FOR_PATIENT";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_APPOINTMENTS_FOR_DAY: {
                LocalDate day = null;
                if (entity != null) {
                    if (entity.isAppointmentDate()) {
                        day = ((AppointmentDate) entity).getValue();
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setInt(1, day.getYear());
                            preparedStatement.setInt(2, day.getMonthValue());
                            preparedStatement.setInt(3, day.getDayOfMonth());
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(new Appointments(), rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_APPOINTMENTS_FOR_DAY)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENTS_FOR_DAY";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENTS_FOR_DAY";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case UPDATE_APPOINTMENT: {
                Appointment appointment = null;
                if (entity != null) {
                    if (entity.isAppointment()) {
                        appointment = (Appointment) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            if (appointment.getPatient() != null) {
                                preparedStatement.setInt(1, appointment.getPatient().getKey());
                            }
                            preparedStatement.setTimestamp(2, Timestamp.valueOf(appointment.getStart()));
                            preparedStatement.setLong(3, appointment.getDuration().toMinutes());
                            preparedStatement.setString(4, appointment.getNotes());
                            preparedStatement.setLong(5, appointment.getKey());
                            preparedStatement.executeUpdate();
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.UPDATE_APPOINTMENT)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.UPDATE_APPOINTMENT";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.UPDATE_APPOINTMENT";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_PATIENT_HIGHEST_KEY:
                try {
                Integer key = null;
                PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    key = (int) rs.getLong("highest_key");
                } else {
                    key = 0;
                }
                result = new PatientTableRowValue(key);
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_PATIENT_HIGHEST_KEY)",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
            break;
            case INSERT_PATIENT: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
                            if (patient.getDOB() != null) {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(patient.getDOB()));
                            } else {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setBoolean(13, patient.getIsGuardianAPatient());
                            preparedStatement.setInt(14, patient.getRecall().getDentalFrequency());
                            if (patient.getRecall().getDentalDate() != null) {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(patient.getRecall().getDentalDate()));
                            } else {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setString(16, patient.getNotes());
                            preparedStatement.setLong(17, patient.getKey());
                            preparedStatement.executeUpdate();
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.INSERT_PATIENT)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.INSERT_PATIENT";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.INSERT_PATIENT";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_PATIENT_WITH_KEY: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                            preparedStatement.setLong(1, patient.getKey());
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(new Patient(), rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.READ_PATIENT_WITH_KEY)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_PATIENT_WITH_KEY";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_PATIENT_WITH_KEY";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case READ_ALL_PATIENTS:
                try {
                PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                result = get(new Patients(), rs);
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL..) during a READ_ALL_PATIENTS statement",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
            break;
            case UPDATE_PATIENT: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
                            if (patient.getDOB() != null) {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(patient.getDOB()));
                            } else {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setBoolean(13, patient.getIsGuardianAPatient());
                            preparedStatement.setInt(14, patient.getRecall().getDentalFrequency());
                            if (patient.getRecall().getDentalDate() != null) {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(patient.getRecall().getDentalDate()));
                            } else {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setString(16, patient.getNotes());
                            if (patient.getGuardian() != null) {
                                preparedStatement.setLong(17, patient.getGuardian().getKey());
                            } else {
                                preparedStatement.setLong(17, 0);
                            }
                            preparedStatement.setLong(18, patient.getKey());
                            preparedStatement.executeUpdate();
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL.UPDATE_PATIENT)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.UPDATE_PATIENT";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.UPDATE_PATIENT";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }
            case PATIENTS_COUNT:
                try {
                Integer key = null;
                PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt("record_count");
                    result = new PatientTableRowValue(count);
                }
            } catch (SQLException ex) {
                if (ex.getMessage().contains("missing table")) {
                    throw new StoreException("StoreException -> " + ex.getMessage(),
                            StoreException.ExceptionType.PATIENT_TABLE_MISSING_IN_PMS_DATABASE);
                } else {
                    throw new StoreException("StoreException -> " + ex.getMessage(),
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            }
            break;
            case READ_SURGERY_DAYS: {
                SurgeryDaysAssignment surgeryDaysAssignment = null;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs != null) {
                        surgeryDaysAssignment = (SurgeryDaysAssignment) get(new SurgeryDaysAssignment(), rs);
                    }
                    result = surgeryDaysAssignment;
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::runSQL(PMS.READ_SURGERY_DAYS)",
                            StoreException.ExceptionType.SURGERY_DAYS_TABLE_MISSING_IN_PMS_DATABASE);
                }
                break;
            }
            case UPDATE_SURGERY_DAYS: {
                SurgeryDaysAssignment surgeryDaysAssignment = null;
                if (entity != null) {
                    if (entity.isSurgeryDaysAssignment()) {
                        surgeryDaysAssignment = (SurgeryDaysAssignment) entity;
                        try {
                            for (Entry<DayOfWeek, Boolean> entry : surgeryDaysAssignment.entrySet()) {
                                sql = "UPDATE SurgeryDays SET IsSurgery = ? WHERE Day = ?;";
                                sql = "INSERT INTO SurgeryDays (IsSurgery,Day) VALUES(?, ?);";

                                PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
                                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL,HashMap<>) during execution of UPDATE_SURGERY_DAYS statement",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_SURGERY_DAYS";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_SURGERY_DAYS";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
            }
        }
        return result;
    }

    /**
     * One of a collection of overloaded methods requesting an PMS
     * connection-based SQL statement to be processed
     *
     * @param q:SurgeryDaysSQL signifying the SQL statement to be processed, and
     * which include -- READ_SURGERY_DAYS -- UPDATE_SURGERY_DAYS
     * @param map:SurgeryDaysAssignment which contains the values of rows
     * updated in the SurgeryDays table
     * @return SurgeryDaysAssignment; values read back from table which will be
     * null if update of values executed
     * @throws StoreException wraps the SQLException that can be thrown
     */
    private SurgeryDaysAssignment runSQL(SurgeryDaysSQL q, SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        SurgeryDaysAssignment result = null;
        String sql;
        switch (q) {
            case READ_SURGERY_DAYS:
                sql = "SELECT Day, IsSurgery FROM SurgeryDays;";
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs != null) {
                        result = getSurgeryDaysFromRS(rs);
                    } else {
                        message = "Unexpected error: could not locate record of surgery days";
                        throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                    }
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::runSQL(SurgeryDaysSQL,HashMap<>) during execution of READ_SURGERY_DAYS statement",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            case UPDATE_SURGERY_DAYS:
                //HashMap<DayOfWeek, Boolean> map = surgeryDaysAssignment.getValue();
                for (Entry<DayOfWeek, Boolean> entry : surgeryDaysAssignment.entrySet()) {
                    sql = "UPDATE SurgeryDays SET IsSurgery = ? WHERE Day = ?";
                    try {
                        PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
                    } catch (SQLException ex) {
                        throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                + "StoreException message -> exception raised in AccessStore::runSQL(SurgeryDaysSQL,HashMap<>) during execution of UPDATE_SURGERY_DAYS statement",
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }
                }
        }
        return result;
    }

    /**
     * One of a collection of overloaded methods requesting a migration
     * connection-based SQL statement to be executed
     *
     * @param q:MigrationSQL signifying the SQL statement to be processed
     * @param value:IEntityStoreType; which is checked for being either an
     * Appointment, Patient, or SurgeryDaysAssignment object -- value parameter
     * could also be null, when an IEntityype is not specified
     * @return Integer, which could be null if the selected SL statement never
     * returns a value (e.g a DROP)
     * @throws StoreException
     */
    private IEntityStoreType runSQL(MigrationSQL q, IEntityStoreType entity) throws StoreException {
        IEntityStoreType result = null;
        //SurgeryDaysAssignment surgeryDaysAssignment = null;

        String sql = null;
        switch (q) {
            case SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION:
                sql = "INSERT INTO SurgeryDaysTable "
                        + "(Day,IsSurgery) VALUES(?,?)";
                break;
            case SURGERY_DAYS_TABLE_ROW_COUNT:
                sql = "SELECT COUNT(*) as row_count FROM SurgeryDaysTable;";
                break;
            case APPOINTMENT_TABLE_ROW_COUNT:
                sql = "SELECT COUNT(*) as row_count FROM AppointmentTable;";
                break;
            case APPOINTMENT_TABLE_CREATE:
                sql = "CREATE TABLE AppointmentTable ("
                        + "pid LONG PRIMARY KEY, "
                        + "patientKey LONG NOT NULL REFERENCES PatientTable(pid), "
                        + "start DateTime, "
                        + "duration LONG, "
                        + "notes char);";
                break;
            case APPOINTMENT_TABLE_ADD_FOREIGN_KEY:
                sql = "ALTER TABLE AppointmentTable "
                        + "ADD CONSTRAINT [x]  "
                        + "FOREIGN KEY(patientKey) "
                        + "REFERENCES PatientTable(pid) "
                        + "ON DELETE CASCADE";
                break;
            case APPOINTMENT_TABLE_INSERT_ROW:
                sql = "INSERT INTO AppointmentTable "
                        + "(PatientKey, Start, Duration, Notes,pid) "
                        + "VALUES (?,?,?,?,?);";
                break;
            case APPOINTMENT_TABLE_DROP:
                sql = "DROP TABLE AppointmentTable;";
                break;
            case APPOINTMENT_TABLE_HIGHEST_KEY:
                sql = "SELECT MAX(pid) as highest_key "
                        + "FROM AppointmentTable;";
                break;
            case APPOINTMENT_TABLE_START_TIME_NORMALISED:
                sql = "UPDATE AppointmentTable "
                        + "SET start = DateAdd('h',12,[start]) "
                        + "WHERE DatePart('h',start)<8;";
                break;
            case APPOINTMENT_TABLE_READ:
                sql = "SELECT * FROM AppointmentTable;";
                break;
            case APPOINTMENT_TABLE_READ_WITH_KEY:
                sql = "SELECT a.pid, a.Start, a.PatientKey, a.Duration, a.Notes "
                        + "FROM Appointment AS a "
                        + "WHERE a.pid = ?;";
                break;
            case APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY:
                sql = "DELETE FROM AppointmentTable a WHERE a.patientKey = ?;";
                break;
            case PATIENT_TABLE_ROW_COUNT:
                sql = "SELECT COUNT(*) as row_count FROM PatientTable;";
                break;
            case PATIENT_TABLE_CREATE:
                sql = "CREATE TABLE PatientTable ("
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
                break;
            case PATIENT_TABLE_INSERT_ROW:
                sql = "INSERT INTO PatientTable "
                        + "(title, forenames, surname, line1, line2,"
                        + "town, county, postcode,phone1, phone2, gender, dob,"
                        + "isGuardianAPatient, recallFrequency, recallDate, notes,pid) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                break;
            case PATIENT_TABLE_DROP:
                sql = "DROP TABLE PatientTable;";
                break;
            case PATIENT_TABLE_READ:
                sql = "SELECT * FROM PatientTable;";
                break;
            case PATIENT_TABLE_READ_PATIENT:
                sql = "SELECT * FROM PatientTable WHERE pid = ?;";
                break;
            case PATIENT_TABLE_UPDATE:
                sql
                        = "UPDATE PatientTable "
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
                break;
            case SURGERY_DAYS_TABLE_CREATE:
                sql = "CREATE TABLE SurgeryDaysTable ("
                        + "Day Char(10),"
                        + "IsSurgery YesNo);";
                break;
            case SURGERY_DAYS_TABLE_DROP:
                sql = "DROP TABLE SurgeryDaysTable;";
                break;
            case SURGERY_DAYS_TABLE_READ:
                sql = "SELECT * FROM SurgeryDaysTable;";
                break;

        }

        switch (q) {
            case EXPORT_MIGRATED_DATA_TO_PMS: {
                String table = null;
                String selectedPMSDatabase = getPMSDatabasePath();;
                ResultSet rs;
                Statement statement;
                if (entity != null) {
                    if (entity.isAppointments()) {
                        sql = "SELECT * FROM AppointmentTable;";
                        table = "Appointment";
                    } else if (entity.isPatients()) {
                        sql = "SELECT * FROM PatientTable;";
                        table = "Patient";
                    } else if (entity.isSurgeryDaysAssignment()) {
                        sql = "SELECT * FROM SurgeryDaysTable;";
                        table = "SurgeryDays";
                    }
                    try {
                        statement = getMigrationConnection().createStatement();
                        rs = statement.executeQuery(sql);
                        Database db = DatabaseBuilder.open(new File(selectedPMSDatabase));
                        ImportUtil.importResultSet(rs, db, table);
                    } catch (IOException ex) {
                        String message = ex.getMessage() + "\n";
                        throw new StoreException(message + "StoreException -> raised during runSQL(MigrationSQL.EXPORT_MIGRATED_DATA_TO_PMS)",
                                StoreException.ExceptionType.IO_EXCEPTION);
                    } catch (SQLException ex) {
                        String message = ex.getMessage() + "\n";
                        throw new StoreException(message + "StoreException -> raised during runSQL(MigrationSQL.EXPORT_MIGRATED_DATA_TO_PMS)",
                                StoreException.ExceptionType.SQL_EXCEPTION);
                    }
                }
                break;
            }
            case SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION: {
                SurgeryDaysAssignment surgeryDaysAssignment = null;
                if (entity != null) {
                    if (entity.isSurgeryDaysAssignment()) {
                        surgeryDaysAssignment = (SurgeryDaysAssignment) entity;
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                            for (Entry<DayOfWeek, Boolean> entry : surgeryDaysAssignment.entrySet()) {
                                //PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                                switch (entry.getKey()) {
                                    case MONDAY:
                                        preparedStatement.setString(1, "Monday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                    case TUESDAY:
                                        preparedStatement.setString(1, "Tuesday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                    case WEDNESDAY:
                                        preparedStatement.setString(1, "Wednesday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                    case THURSDAY:
                                        preparedStatement.setString(1, "Thursday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                    case FRIDAY:
                                        preparedStatement.setString(1, "Friday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                    case SATURDAY:
                                        preparedStatement.setString(1, "Saturday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                    case SUNDAY:
                                        preparedStatement.setString(1, "Sunday");
                                        preparedStatement.setBoolean(2, entry.getValue());
                                        break;
                                }
                                preparedStatement.execute();
                            }
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(MigrationSQL.SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> surgeryDaysAssignment undefined in MigrationQL.SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in MigrationQL.SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION ";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }

            case SURGERY_DAYS_TABLE_ROW_COUNT: {
                int count = 0;
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        count = rs.getInt("row_count");
                        result = new AppointmentTableRowValue(count);
                    }
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during runSQL(MigrationSQL.SURGERY_DAYS_TABLE_ROW_COUNT)",
                            StoreException.ExceptionType.SURGERY_DAYS_TABLE_MISSING_IN_MIGRATION_DATABASE);
                }
                break;
            }

            case APPOINTMENT_TABLE_ROW_COUNT: {
                int count = 0;
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        count = rs.getInt("row_count");
                        result = new AppointmentTableRowValue(count);
                    }
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during an APPOINTMENT_TABLE_ROW_COUNT data migration operation",
                            StoreException.ExceptionType.APPOINTMENT_TABLE_MISSING_IN_MIGRATION_DATABASE);
                }
                break;
            }
            case APPOINTMENT_TABLE_ADD_FOREIGN_KEY:
                try {
                /*
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.execute();
                 */
                Statement statement = getMigrationConnection().createStatement();
                statement.execute(sql);
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised in AccessStore::runSQL(MigrationSQL.APPOINTMENT_TABLE_ADD_FOREIGN_KEY)",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
            case APPOINTMENT_TABLE_CREATE: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.execute();

                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised in AccessStore::runSQL(MigrationSQL.APPOINTMENT_TABLE_CREATE) ",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case APPOINTMENT_TABLE_INSERT_ROW:
                Appointment appointment = null;
                int test;
                if (entity != null) {
                    if (entity.isAppointment()) {
                        appointment = (Appointment) entity;
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                            preparedStatement.setInt(1, appointment.getPatient().getKey());
                            preparedStatement.setTimestamp(2, Timestamp.valueOf(appointment.getStart()));
                            preparedStatement.setLong(3, appointment.getDuration().toMinutes());
                            preparedStatement.setString(4, appointment.getNotes());
                            preparedStatement.setLong(5, appointment.getKey());
                            preparedStatement.execute();
                        } /**
                         * Could be that appointment record references a patient
                         * key that does not exist. In which case the records is
                         * ignored. Any other exception's message will be
                         * displayed
                         */
                        catch (SQLException ex) {
                            if (!(ex.getMessage().contains("integrity constraint violation:"))) {
                                displayErrorMessage("SQLException message -> "
                                        + ex.getMessage() + "\n"
                                        + "Exception raised in AccessStore::"
                                        + "runSQL(MigrationSQL.APPOINTMENT_TABLE_INSERT_ROW)",
                                        "Access Store error", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    } else {
                        String message = "StoreException -> entity incorrectly defined in AccessStore::runSQL(MigrationSQL.APPOINTMENT_TABLE_INSERT_ROW";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> entity undefined in AccessStore::runSQL(MigrationSQL.APPOINTMENT_TABLE_INSERT_ROW";
                    throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                break;
            case APPOINTMENT_TABLE_DROP: {
                /**
                 * Update to fix problem when trying to drop a non-existent
                 * appointment table -- run a query on MSysObjects table to see
                 * if table exists or not -- documented solution doesn't work
                 * (Access doesn't like the "sys" prefix)
                 *
                 * String sql_ = "SELECT COUNT(*) as the_count FROM
                 * sys.MSysObjects WHERE name = 'Appointment';";
                 */
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_TABLE_DROP data migration operation",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                     */
                }
                break;
            }
            case APPOINTMENT_TABLE_HIGHEST_KEY:
                try {
                Integer key = null;
                PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    key = (int) rs.getLong("highest_key");
                }
                result = new AppointmentTableRowValue(key);
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised during a READ_HIGHEST_KEY from Appointment table",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
            break;
            case APPOINTMENT_TABLE_START_TIME_NORMALISED: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a APPOINTMENT_START_TIME_NORMALISED data migration operation",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case APPOINTMENT_TABLE_READ: {
                Appointments appointments = new Appointments();
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    appointments = get(appointments, rs);
                    result = appointments;
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a APPOINTMENT_TABLE_READ data migration operation",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case APPOINTMENT_TABLE_READ_WITH_KEY: {
                AppointmentTable appointmentTable = null;
                if (entity != null) {
                    if (entity.isAppointmentTable()) {
                        appointmentTable = (AppointmentTable) entity;
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                            preparedStatement.setLong(1, appointmentTable.getKey());
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(new AppointmentTable(), rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(MigrationSQL.APPOINTMENT_TABLE_READ_WITH_KEY)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENT_WITH_KEY";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> appointment undefined in PatientManagementSystemSQL.READ_APPOINTMENT_WITH_KEY";
                    throw new StoreException(message, StoreException.ExceptionType.NULL_KEY_EXCEPTION);
                }
                break;
            }

            case APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY:
                if (entity != null) {
                    if (entity.isPatientTableRowValue()) {
                        Integer patientKey = ((PatientTableRowValue) entity).getValue();
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                            preparedStatement.setInt(1, patientKey);
                            preparedStatement.execute();
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(MigratiionSQL.APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> incorrectly defined patient key in Access::runSQL(MigrationSQL.APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY";
                    throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                break;
            case PATIENT_TABLE_ROW_COUNT: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt("row_count");
                        result = new PatientTableRowValue(count);
                    }
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a PATIENT_TABLE_ROW_COUNT data migration operation",
                            StoreException.ExceptionType.PATIENT_TABLE_MISSING_IN_MIGRATION_DATABASE);
                }
                break;
            }
            case PATIENT_TABLE_CREATE: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a APPOINTMENT_START_TIME_NORMALISED data migration operation",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case PATIENT_TABLE_INSERT_ROW: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
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
                            if (patient.getDOB() != null) {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(patient.getDOB()));
                            } else {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setBoolean(13, patient.getIsGuardianAPatient());
                            preparedStatement.setInt(14, patient.getRecall().getDentalFrequency());
                            if (patient.getRecall().getDentalDate() != null) {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(patient.getRecall().getDentalDate()));
                            } else {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setString(16, patient.getNotes());
                            preparedStatement.setLong(17, patient.getKey());
                            preparedStatement.execute();
                        } catch (SQLException ex) { //14/03/2022 17:23 code update
                            message = ex.getMessage() + "\n Raised in "
                                    + "AccessStore::runSQL(MigrationSQL.PATIENT_TABLE_INSERT_ROW)";
                            displayErrorMessage(message, "Access Store error", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.PATIENT_TABLE_INSERT_ROW";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.PATIENT_TABLE_INSERT_ROW";
                    throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                break;
            }
            case PATIENT_TABLE_DROP: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_START_TIME_NORMALISED data migration operation",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                     */

                }
                break;
            }
            case PATIENT_TABLE_READ: {
                Patients patients = new Patients();
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    Patients the_patients = get(patients, rs);
                    result = the_patients;
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a APPOINTMENT_TABLE_READ data migration operation",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case PATIENT_TABLE_READ_PATIENT: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                            preparedStatement.setInt(1, patient.getKey());
                            ResultSet rs = preparedStatement.executeQuery();
                            result = get(patient, rs);
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(MigrationSQL.PATIENT_TABLE_READ_TABLE)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.PATIENT_TABLE_READ_TABLE";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.PATIENT_TABLE_READ_TABLE";
                    throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                break;
            }
            case SURGERY_DAYS_TABLE_CREATE: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a APPOINTMENT_START_TIME_NORMALISED data migration operation",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
            case PATIENT_TABLE_UPDATE: {
                Patient patient = null;
                if (entity != null) {
                    if (entity.isPatient()) {
                        patient = (Patient) entity;
                        try {
                            PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
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
                            if (patient.getDOB() != null) {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(patient.getDOB()));
                            } else {
                                preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setBoolean(13, patient.getIsGuardianAPatient());
                            preparedStatement.setInt(14, patient.getRecall().getDentalFrequency());
                            if (patient.getRecall().getDentalDate() != null) {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(patient.getRecall().getDentalDate()));
                            } else {
                                preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                            }
                            preparedStatement.setString(16, patient.getNotes());
                            if (patient.getGuardian() != null) {
                                preparedStatement.setLong(17, patient.getGuardian().getKey());
                            } else {
                                preparedStatement.setLong(17, 0);
                            }
                            preparedStatement.setLong(18, patient.getKey());
                            preparedStatement.executeUpdate();
                        } catch (SQLException ex) {
                            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                                    + "StoreException message -> exception raised in AccessStore::runSQL(MigrationSQL.PATIENT_TABLE_UPDATE)",
                                    StoreException.ExceptionType.SQL_EXCEPTION);
                        }
                    } else {
                        String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.PATIENT_TABLE_UPDATE";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                } else {
                    String message = "StoreException -> undefined patient key in Access::runSQL(MigrationSQL.PATIENT_TABLE_UPDATE";
                    throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                break;
            }
            case SURGERY_DAYS_TABLE_DROP: {
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    /*
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                     + "StoreException message -> exception raised during a APPOINTMENT_START_TIME_NORMALISED data migration operation",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                     */
                }
                break;
            }
            case SURGERY_DAYS_TABLE_READ: {
                SurgeryDaysAssignment s = new SurgeryDaysAssignment();
                try {
                    PreparedStatement preparedStatement = getMigrationConnection().prepareStatement(sql);
                    ResultSet rs = preparedStatement.executeQuery();
                    s = get(s, rs);
                    result = s;
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                            + "StoreException message -> exception raised during a APPOINTMENT_TABLE_READ data migration operation",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
                break;
            }
        }
        return result;
    }

    /**
     * Explicit manual transaction processing enabled -- updates which days are
     * surgery days on the system
     *
     * @param value:HashMap<SayOfWeek, Boolean> the new set of surgery days used
     * to update currently stored values
     * @throws StoreException
     */
    private SurgeryDaysAssignment getSurgeryDaysFromRS(ResultSet rs) throws SQLException {
        SurgeryDaysAssignment result = new SurgeryDaysAssignment();
        if (!rs.wasNull()) {
            while (rs.next()) {
                switch (rs.getString("Day")) {
                    case "Monday":
                        result.put(DayOfWeek.MONDAY, rs.getBoolean("IsSurgery"));
                        break;
                    case "Tuesday":
                        result.put(DayOfWeek.TUESDAY, rs.getBoolean("IsSurgery"));
                        break;
                    case "Wednesday":
                        result.put(DayOfWeek.WEDNESDAY, rs.getBoolean("IsSurgery"));
                        break;
                    case "Thursday":
                        result.put(DayOfWeek.THURSDAY, rs.getBoolean("IsSurgery"));
                        break;
                    case "Friday":
                        result.put(DayOfWeek.FRIDAY, rs.getBoolean("IsSurgery"));
                        break;
                    case "Saturday":
                        result.put(DayOfWeek.SATURDAY, rs.getBoolean("IsSurgery"));
                    case "Sunday":
                        result.put(DayOfWeek.SUNDAY, rs.getBoolean("IsSurgery"));
                }
            }
        }
        return result;
    }

    private ArrayList<Patient> getPatientsFromRS(ResultSet rs) throws SQLException {
        ArrayList<Patient> result = new ArrayList<>();
        if (!rs.wasNull()) {
            while (rs.next()) {
                Patient patient = new Patient();
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
                if (patient.getIsGuardianAPatient()) {
                    if (guardianKey > 0) {
                        Patient p = new Patient(guardianKey);
                        patient.setGuardian(p);
                    }
                }
                patient.setNotes(notes);
                result.add(patient);
            }
        }
        return result;
    }

    private Appointment get(Appointment appointment, ResultSet rs) throws StoreException {
        try {
            if (!rs.wasNull()) {
                rs.next();
                int key = rs.getInt("pid");
                LocalDateTime start = rs.getObject("Start", LocalDateTime.class);
                Duration duration = Duration.ofMinutes(rs.getLong("Duration"));
                String notes = rs.getString("Notes");
                int patientKey = rs.getInt("PatientKey");
                appointment.setKey(key);
                appointment.setStart(start);
                appointment.setDuration(duration);
                appointment.setNotes(notes);
                appointment.setPatient(new Patient(patientKey));
                appointment.setStatus(Appointment.Status.BOOKED);
            }
            return appointment;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(Appointments,ResultSet)",
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
        try{
            if (!rs.wasNull()){
                rs.next();
                int pid = rs.getInt("pid");
                int patientKey = rs.getInt("patientToNotify");
                LocalDate notificationDate = rs.getObject("notificationDate", LocalDate.class);
                String notificationText = rs.getString("notificationText");
                Boolean isActioned = rs.getBoolean("isActioned");
                patientNotification.setKey(pid);
                patientNotification.setPatient(new ThePatient(patientKey));
                patientNotification.setNotificationDate(notificationDate);
                patientNotification.setNotificationText(notificationText);
                patientNotification.setIsActioned(isActioned);
            }
            return patientNotification;
        }catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(PatientNotification,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    private PatientNotification.Collection get(PatientNotification.Collection patientNotificationCollection, ResultSet rs)throws StoreException{
        ArrayList<PatientNotification> collection = new ArrayList<>();
        try{
            if (!rs.wasNull()){
                while (rs.next()){
                   int pid = rs.getInt("pid");
                   int patientKey = rs.getInt("patientToNotify");
                   LocalDate notificationDate = rs.getObject("notificationDate", LocalDate.class);
                   String notificationText = rs.getString("notificationText");
                   Boolean isActioned = rs.getBoolean("isActioned");
                   PatientNotification patientNotification = new PatientNotification();
                   patientNotification.setKey(pid);
                   patientNotification.setPatient(new ThePatient(patientKey));
                   patientNotification.setNotificationDate(notificationDate);
                   patientNotification.setNotificationText(notificationText);
                   patientNotification.setIsActioned(isActioned);
                   collection.add(patientNotification);
                }
                patientNotificationCollection.set(collection);
            }
            return patientNotificationCollection;
        }catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(PatientNotifications,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    private Appointments get(Appointments appointments, ResultSet rs) throws StoreException {
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
                    int key = rs.getInt("pid");
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
                    appointments.add(appointment);
                }
            }
            return appointments;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(Appointments,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    private ThePatient getThePatientDetails(ThePatient patient, ResultSet rs) throws SQLException {
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
        patient.setNotes(notes);
        patient.setIsGuardianAPatient(isGuardianAPatient);
        if (patient.getIsGuardianAPatient()) {
            int guardianKey = rs.getInt("guardianKwey");
            if (guardianKey > 0) {
                ThePatient p = new ThePatient(guardianKey);
                patient.setGuardian(p);
            }
        }
        return patient;
    }

    private Patient getPatientDetails(Patient patient, ResultSet rs) throws SQLException {
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
        patient.setNotes(notes);
        patient.setIsGuardianAPatient(isGuardianAPatient);
        if (patient.getIsGuardianAPatient()) {
            int guardianKey = rs.getInt("guardianKwey");
            if (guardianKey > 0) {
                Patient p = new Patient(guardianKey);
                patient.setGuardian(p);
            }
        }
        return patient;
    }
    
    private ThePatient get(ThePatient patient, ResultSet rs) throws SQLException {
        ThePatient result = null;
        if (!rs.wasNull()) {
            if (rs.next()) {
                patient = getThePatientDetails(patient, rs);
                result = patient;
            }
        } else {
            result = null;
        }
        return result;
    }

    private Patient get(Patient patient, ResultSet rs) throws SQLException {
        Patient result = null;
        if (!rs.wasNull()) {
            if (rs.next()) {
                patient = getPatientDetails(patient, rs);
                result = patient;
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
                            surgeryDaysAssignment.put(DayOfWeek.MONDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Tuesday":
                            surgeryDaysAssignment.put(DayOfWeek.TUESDAY, rs.getBoolean("isSurgery"));
                        case "Wednesday":
                            surgeryDaysAssignment.put(DayOfWeek.WEDNESDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Thursday":
                            surgeryDaysAssignment.put(DayOfWeek.THURSDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Friday":
                            surgeryDaysAssignment.put(DayOfWeek.FRIDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Saturday":
                            surgeryDaysAssignment.put(DayOfWeek.SATURDAY, rs.getBoolean("isSurgery"));
                            break;
                        case "Sunday":
                            surgeryDaysAssignment.put(DayOfWeek.SUNDAY, rs.getBoolean("isSurgery"));
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
     * method decodes the ResultSet for persistent storage into a ThePatient.Collection object
     * -- the resulting collection of ThePatient child objects is used to initialise a mother ThePatient.Collection object
     * -- the information contained in ThePatient object is either in the values of its state fields, excepting its collection field;
     * -- or the information is contained in its Collection inner class
     * -- This is possibly ambiguous, and can be overcome by making a separate class responsible for the collection
     * @param collection
     * @param rs
     * @return
     * @throws StoreException 
     */
    private ThePatient.Collection get(ThePatient.Collection collection, ResultSet rs) throws StoreException {
        ArrayList<ThePatient> patients = new ArrayList<>();
        ThePatient motherPatient = new ThePatient();
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
                    ThePatient childPatient = new ThePatient();
                    childPatient = getThePatientDetails(childPatient, rs);
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

    private Patients get(Patients patients, ResultSet rs) throws StoreException {
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
                    Patient patient = new Patient();
                    patient = getPatientDetails(patient, rs);
                    patients.add(patient);
                }
            }
            return patients;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException -> raised in Access::get(Patient,ResultSet)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    private IEntityStoreType get(IEntityStoreType value, ResultSet rs) throws StoreException {
        IEntityStoreType result = null;
        if (value != null) {
            if (value.isAppointment()) {

            } else if (value.isAppointments()) {
                try {
                    if (!rs.wasNull()) {
                        while (rs.next()) {
                            int key = rs.getInt("pid");
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
                        }
                        result = null;
                    }
                } catch (SQLException ex) {
                    throw new StoreException("SQLException message -> " + ex.getMessage() + "\n",
                            StoreException.ExceptionType.SQL_EXCEPTION);
                }
            } else if (value.isPatient()) {

            } else if (value.isPatients()) {

            }
        }
        return result;
    }

    private ArrayList<Appointment> getAppointmentsFromRS(ResultSet rs) throws StoreException {
        ArrayList<Appointment> result = new ArrayList<>();
        try {
            if (!rs.wasNull()) {
                while (rs.next()) {
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
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        return result;
    }

    private Patient doCapitaliseFirstLetterOnly(Patient patient) {
        String cappedForenames = "";
        String cappedSurname = "";
        String cappedTitle = "";
        String cappedLine1 = "";
        String cappedLine2 = "";
        String cappedTown = "";
        String cappedCounty = "";

        if (patient.getAddress().getLine1() == null) {
            patient.getAddress().setLine1("");
        }
        if (patient.getAddress().getLine1().length() > 0) {
            //cappedLine1 = patient.getAddress().getLine1().strip();
            cappedLine1 = patient.getAddress().getLine1();
            if (cappedLine1.contains("-")) {
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "-");
                if (cappedLine1.contains(" ")) {
                    cappedLine1 = capitaliseFirstLetter(cappedLine1, "\\s+");
                }
            } else if (cappedLine1.contains(" ")) {
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "\\s+");
            } else {
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "");
            }
        }

        if (patient.getAddress().getLine2() == null) {
            patient.getAddress().setLine2("");
        }
        if (patient.getAddress().getLine2().length() > 0) {
            //cappedLine2 = patient.getAddress().getLine2().strip();
            cappedLine2 = patient.getAddress().getLine2();
            if (cappedLine2.contains("-")) {
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "-");
                if (cappedLine2.contains(" ")) {
                    cappedLine2 = capitaliseFirstLetter(cappedLine2, "\\s+");
                }
            } else if (cappedLine2.contains(" ")) {
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "\\s+");
            } else {
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "");
            }
        }

        if (patient.getAddress().getTown() == null) {
            patient.getAddress().setTown("");
        }
        if (patient.getAddress().getTown().length() > 0) {
            //cappedTown = patient.getAddress().getTown().strip();
            cappedTown = patient.getAddress().getTown();
            if (cappedTown.contains("-")) {
                cappedTown = capitaliseFirstLetter(cappedTown, "-");
                if (cappedTown.contains(" ")) {
                    cappedTown = capitaliseFirstLetter(cappedTown, "\\s+");
                }
            } else if (cappedTown.contains(" ")) {
                cappedTown = capitaliseFirstLetter(cappedTown, "\\s+");
            } else {
                cappedTown = capitaliseFirstLetter(cappedTown, "");
            }
        }

        if (patient.getAddress().getCounty() == null) {
            patient.getAddress().setCounty("");
        }
        if (patient.getAddress().getCounty().length() > 0) {
            //cappedCounty = patient.getAddress().getCounty().strip();
            cappedCounty = patient.getAddress().getCounty();
            if (cappedCounty.contains("-")) {
                cappedCounty = capitaliseFirstLetter(cappedCounty, "-");
                if (cappedCounty.contains(" ")) {
                    cappedCounty = capitaliseFirstLetter(cappedCounty, "\\s+");
                }
            } else if (cappedCounty.contains(" ")) {
                cappedCounty = capitaliseFirstLetter(cappedCounty, "\\s+");
            } else {
                cappedCounty = capitaliseFirstLetter(cappedCounty, "");
            }
        }

        if (patient.getName().getSurname() == null) {
            patient.getName().setSurname("");
        }
        if (patient.getName().getSurname().length() > 0) {
            //cappedSurname = patient.getName().getSurname().strip();
            cappedSurname = patient.getName().getSurname();
            if (cappedSurname.contains("-")) {
                cappedSurname = capitaliseFirstLetter(cappedSurname, "-");
                if (cappedSurname.contains(" ")) {
                    cappedSurname = capitaliseFirstLetter(cappedSurname, "\\s+");
                }
            } else if (cappedSurname.contains(" ")) {
                cappedSurname = capitaliseFirstLetter(cappedSurname, "\\s+");
            } else {
                cappedSurname = capitaliseFirstLetter(cappedSurname, "");
            }
        }
        if (patient.getName().getForenames() == null) {
            patient.getName().setForenames("");
        }
        if (patient.getName().getForenames().length() > 0) {
            //cappedForenames = patient.getName().getForenames().strip();
            cappedForenames = patient.getName().getForenames();
            if (cappedForenames.contains("-")) {
                cappedForenames = capitaliseFirstLetter(cappedForenames, "-");
                if (cappedForenames.contains(" ")) {
                    cappedForenames = capitaliseFirstLetter(cappedForenames, "\\s+");
                }
            } else if (cappedForenames.contains(" ")) {
                cappedForenames = capitaliseFirstLetter(cappedForenames, "\\s+");
            } else {
                cappedForenames = capitaliseFirstLetter(cappedForenames, "");
            }
        }
        if (patient.getName().getTitle() == null) {
            patient.getName().setTitle("");
        }
        if (patient.getName().getTitle().length() > 0) {
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

    private Patient updateGender(Patient patient) {
        switch (patient.getGender()) {
            case "M":
                patient.setGender("Male");
                break;
            case "F":
                patient.setGender("Female");
                break;
        }
        return patient;
    }

    /**
     * populates the Appointment table with appointment records -- uses a
     * slightly different version of the insert(Appointment a) method ---- this
     * version does not return a fully fledged Appointment object to the caller
     * ------ that involves fetching the patient object from the Patient table
     * ------ but during data migration the Appointment table includes appointee
     * references not in the patient table -- note also: a bug in the
     * insert(Appointment a) method calls insert(Patient) rather than
     * read(Patient) for this purpose
     *
     * @param appointments
     * @return int count of the number of appointment records
     * @throws StoreException
     */
    private void insertMigratedAppointments(ArrayList<Appointment> appointments) throws StoreException {
        Integer result = null;
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()) {
            Appointment appointment = it.next();
            IEntityStoreType entity = null;
            IEntityStoreType value = runSQL(MigrationSQL.APPOINTMENT_TABLE_HIGHEST_KEY, appointment);
            if (value.isAppointmentTableRowValue()) {
                result = ((AppointmentTableRowValue) value).getValue();
                appointment.setKey(result + 1);
                runSQL(MigrationSQL.APPOINTMENT_TABLE_INSERT_ROW, appointment);
            } else {
                throw new StoreException("Unexpected data type returned from call to runSQL(MigrationSQL.APPOINTMENT_TABLE_HIGHEST_KEY) during execution AccessStore::insertMigratedAppointments()",
                        StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        }
    }

    /**
     * Important:- method returns count of Patient rows in the currently
     * selected store -- hence count returned will be of rows in the currently
     * selected store's Patient table
     *
     * @return
     * @throws StoreException
     */
    /*
    private int getPatientsCount() throws StoreException{
        Patient patient = new Patient();
        ArrayList<Patient> value = runSQL(PatientSQL.PATIENTS_COUNT, 
        patient);
        return value.get(0).getKey();
    }
     */
    private int getPatientTableCount() throws StoreException {
        int count = 0;
        count = ((PatientTableRowValue) runSQL(MigrationSQL.PATIENT_TABLE_ROW_COUNT, null)).getValue();
        return count;
    }

    /**
     * -- method establishes a set of patient keys (no duplicates) referenced as
     * appointees by the appointments collection -- each patient key is used to
     * read in the corresponding patient object -- when a corresponding patient
     * object for a given key is not found the appointment object is added to
     * another collection (non existing patient records) -- the 'orphaned'
     * appointment records are then deleted from the system
     * (deleteOrphanedAppointmentsFromAppointments())
     *
     * @param appointments,collection of Appointment objects
     * @throws StoreException, used to identify when appointments refer to a
     * non-existing patient key
     */
    private void migratedAppointmentsIntegrityCheck() throws StoreException {
        Integer key;
        HashSet<Integer> nonExistingPatientsReferencedbyAppointments = new HashSet<>();
        HashSet<Integer> patientSet = new HashSet<>();

        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()) {
            Appointment appointment = it.next();
            key = appointment.getPatient().getKey();
            patientSet.add(key);
        }
        Iterator<Integer> patientSetIt = patientSet.iterator();
        while (patientSetIt.hasNext()) {
            key = patientSetIt.next();
            try {
                read(new Patient(key));
                patientCount++;
            } catch (StoreException ex) {
                if (ex.getErrorType().equals(StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION)) {
                    nonExistingPatientsReferencedbyAppointments.add(key);
                } else {//if not a KEY_NOT_FOUND_EXCEPTION pass StoreException on
                    throw new StoreException(ex.getMessage(), StoreException.ExceptionType.STORE_EXCEPTION);
                }
            }
        }
        this.setNonExistingPatientsReferencedByAppointmentsCount(nonExistingPatientsReferencedbyAppointments.size());
        Iterator<Integer> nonExistingPatientKeysIt = nonExistingPatientsReferencedbyAppointments.iterator();
        while (nonExistingPatientKeysIt.hasNext()) {
            Integer nonExistingPatientKey = nonExistingPatientKeysIt.next();
            //deleteAppointmentWithPatientKey(nonExistingPatientKey);
        }

    }

    private boolean isMigrationActionStore() {
        return Store.IS_MIGRATION_STORE_ACTION;
    }

    private boolean isPMSActionStore() {
        return Store.IS_PMS_STORE_ACTION;
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
                    if (getMigrationConnection().getAutoCommit()) {
                        getMigrationConnection().setAutoCommit(false);
                    }
                } else if (IS_PMS_STORE_ACTION) {
                    if (getPMSConnection().getAutoCommit()) {
                        getPMSConnection().setAutoCommit(false);
                    }
                }
                break;
            case AUTO_COMMIT_ON:
                if (IS_MIGRATION_STORE_ACTION) {
                    if (getMigrationConnection().getAutoCommit()) {
                        getMigrationConnection().setAutoCommit(true);
                    }
                } else if (IS_PMS_STORE_ACTION) {
                    if (getPMSConnection().getAutoCommit()) {
                        getPMSConnection().setAutoCommit(true);
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
                if (state) {
                    getMigrationConnection().commit();
                } else {
                    getMigrationConnection().rollback();
                }
            } else if (IS_PMS_STORE_ACTION) {
                if (state) {
                    getPMSConnection().commit();
                } else {
                    getPMSConnection().rollback();
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
        closeMigrationConnection();
        closePMSConnection();
        closeTargetConnection();
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
    /**
     * used for exporting data into PMS database from Migration database
     */
    public void insert(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            runSQL(PMSSQL.INSERT_SURGERY_DAYS_ASSIGNMENT, surgeryDaysAssignment);
        } catch (SQLException ex) {

        }
    }

    @Override
    /**
     * Inserts specified Appointment object into store's collection of
     * appointment records. -- the success of the insertion is checked by
     * following insertion with an attempt to read back the inserted record
     *
     * @param a:Appointment to insert in store table
     * @throws StoreException in following circumstances -- on setting the mode
     * of the connection via the setConnectionMode() message sent ---- this
     * arises directly if the sent message's connection getter throws a
     * StoreException ---- or indirectly if the sent message's call to the
     * autoCommit getter/setter raises an SQLException, which is then wrapped in
     * a Store|Exception -- on unsuccessful attempt to read back the inserted
     * record
     */
    public void insert(Appointment a) throws StoreException {
        boolean result = false;
        IEntityStoreType value;
        Appointment appointment;
        Patient patient;
        message = "";

        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            value = runSQL(PMSSQL.READ_APPOINTMENT_HIGHEST_KEY,
                    null);
            if (value.isAppointmentTableRowValue()) {
                a.setKey(((AppointmentTableRowValue) value).getValue() + 1);
                runSQL(PMSSQL.INSERT_APPOINTMENT, a);
                value = runSQL(PMSSQL.READ_APPOINTMENT_WITH_KEY, a);
                if (value == null) {
                    message = "StoreException raised in method AccessStore::create(Appointment a)\n"
                            + "Reason -> newly created appointment record could not be found";
                    result = false;
                    throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                result = true;
            }
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(Appointment a)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.create(Appointment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    public void insert(AppointmentTable table) throws StoreException {
        boolean result = false;
        IEntityStoreType value;
        Appointment appointment;
        Patient patient;
        message = "";

        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            value = runSQL(MigrationSQL.APPOINTMENT_TABLE_HIGHEST_KEY,
                    null);
            if (value.isAppointmentTableRowValue()) {
                table.setKey(((AppointmentTableRowValue) value).getValue() + 1);
                runSQL(MigrationSQL.APPOINTMENT_TABLE_INSERT_ROW, table);
                value = runSQL(MigrationSQL.APPOINTMENT_TABLE_READ_WITH_KEY, table);
                if (value == null) {
                    message = "StoreException raised in method AccessStore::create(AppointmentTable)\n"
                            + "Reason -> newly created appointment record could not be found";
                    result = false;
                    throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
                result = true;
            }
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(Appointment a)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.create(Appointment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
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
    public void insert(PatientNotification pn)throws StoreException{
        EntityStoreType entity;
        message = "";
        try {//turn off jdbc driver's auto commit after each SQL statement
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            /**
             * allows possibility that patient notification's key value already initialised
             * -- if this results in a duplicated key in the store, an exception will be thrown
             */
            if (pn.getKey() == null) {
                entity = runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,
                        PMSSQL.READ_PATIENT_NOTIFICATION_HIGHEST_KEY,pn);
                pn.setKey(((TableRowValue) entity).getValue() + 1);
                runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,
                        PMSSQL.INSERT_PATIENT_NOTIFICATION, pn);
                entity = runSQL(Store.EntitySQL.PATIENT_NOTIFICATION,
                        PMSSQL.READ_PATIENT_NOTIFICATION_WITH_KEY, pn);
                if (entity == null) {
                    message = "StoreException raised in method AccessStore::insert(PatientNotification p)\n"
                            + "Reason -> newly created patient record could not be found";
                    throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
            }
            else{
                message = "StoreException raised in method AccessStore::insert(PatientNotification p)\n"
                            + "Reason -> The key of the patient notification to be inserted has already been defined";
                    throw new StoreException(message, StoreException.ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
            } 
            
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(PatientNotification pn)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    @Override
    public void insert(ThePatient p) throws StoreException {
        EntityStoreType entity;
        message = "";
        try {//turn off jdbc driver's auto commit after each SQL statement
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);

            if (p.getKey() == null) {
                entity = runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENT_HIGHEST_KEY, new ThePatient());
                p.setKey(((TableRowValue) entity).getValue() + 1);
                runSQL(EntitySQL.PATIENT,PMSSQL.INSERT_PATIENT, p);
                entity = (ThePatient) runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENT_WITH_KEY, p);
                if (entity == null) {
                    message = "StoreException raised in method AccessStore::insert(ThePatient p)\n"
                            + "Reason -> newly created patient record could not be found";
                    throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
                }
            }
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(ThePatient a)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } 
    }
    
    @Override
    /**
     * Inserts specified Patient object into store's collection of patient
     * records. -- the success of the insertion is checked by following
     * insertion with an attempt to read back the inserted record
     *
     * @param p:Patient to insert in store table -- if patient key is not
     * defined a new key (highest existing key + 1) is created -- allowing the
     * possibility of the key to be defined makes the method
     * migration-compatible
     * @throws StoreException in following circumstances -- on setting the mode
     * of the connection via the setConnectionMode() message sent ---- this
     * arises directly if the sent message's connection getter throws a
     * StoreException ---- or indirectly if the sent message's call to the
     * autoCommit getter/setter raises an SQLException, which is then wrapped in
     * a Store|Exception -- on unsuccessful attempt to read back the inserted
     * record
     */
    public void insert(Patient p) throws StoreException {
        boolean result = false;
        IEntityStoreType entity;
        message = "";
        try {//turn off jdbc driver's auto commit after each SQL statement
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);

            if (p.getKey() == null) {
                entity = runSQL(PMSSQL.READ_PATIENT_HIGHEST_KEY, new Patient());
                p.setKey(((PatientTableRowValue) entity).getValue() + 1);
            }
            runSQL(PMSSQL.INSERT_PATIENT, p);
            entity = (Patient) runSQL(PMSSQL.READ_PATIENT_WITH_KEY, p);
            if (entity == null) {
                message = "StoreException raised in method AccessStore::create(Patient p)\n"
                        + "Reason -> newly created patient record could not be found";
                throw new StoreException(message, StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            } else {
                result = true;
            }
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::create(Patient a)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.create(Patient))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    /**
     * 14/03/2022 17:23 code update method inserts the specified patient into
     * the patient table in the selected migration database
     *
     * @param table:PatientTable
     * @param patient:Patient
     * @throws StoreException
     */
    public void insert(PatientTable table, Patient patient) throws StoreException {
        boolean result = false;
        IEntityStoreType entity;
        message = "";
        try {//turn off jdbc driver's auto commit after each SQL statement
            //getMigrationConnection().setAutoCommit(false);
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);

            runSQL(MigrationSQL.PATIENT_TABLE_INSERT_ROW, patient);
            result = true;

        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(PatientTable)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.insert(PatientTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    public void insert(AppointmentTable table, Appointment appointment) throws StoreException {
        boolean result = false;
        IEntityStoreType entity;
        message = "";
        try {//turn off jdbc driver's auto commit after each SQL statement
           // setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            entity = runSQL(MigrationSQL.APPOINTMENT_TABLE_HIGHEST_KEY,
                    null);
            if (entity.isAppointmentTableRowValue()) {
                appointment.setKey(((AppointmentTableRowValue) entity).getValue() + 1);
                runSQL(MigrationSQL.APPOINTMENT_TABLE_INSERT_ROW, appointment);
                //getMigrationConnection().setAutoCommit(false);
                //getMigrationConnection().setAutoCommit(true);
                result = true;
            }

        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::insert(AppointmentTable)\n"
                    + "Cause -> unexpected effect when transaction/auto commit statement executed",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.insert(AppointmentTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Deletes specified Appointment object in store's collection of appointment
     * records. -- the success of the deletion is checked by following deletion
     * with request to read back the deleted record
     *
     * @param a:Appointment to delete from store table
     * @throws StoreException in following circumstances -- on setting the mode
     * of the connection via the setConnectionMode() message sent ---- this
     * arises directly if the sent message's connection getter throws a
     * StoreException ---- or indirectly if the sent message's call to the
     * autoCommit getter/setter raises an SQLException, which is then wrapped in
     * a Store|Exception -- on failure to delete the requested record
     */
    public void delete(Appointment a) throws StoreException {
        boolean result = false;
        try {//turn off jdbc driver's auto commit after each SQL statement
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            runSQL(PMSSQL.DELETE_APPOINTMENT_WITH_KEY, a);
            result = true;
            /*
            ArrayList value = runSQL(PMSSQL.READ_APPOINTMENT_WITH_KEY, a);
            if (value.isEmpty()){
                message = 
                        "Unsuccesful attempt to delete appointment record (key = "
                        + String.valueOf(a.getKey()) + ")";
                throw new StoreException(message, StoreException.ExceptionType.KEY_FOUND_EXCEPTION);
            }
             */
        } catch (SQLException ex) {
            message = message + "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore::delete(Appointment a)\n"
                    + "Cause -> exception raised in call to setConnectionMode(AUTO_COMMIT_OFF)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.delete(Appointment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }

    }
    
    @Override
    /**
     * not currently implemented
     */
    public void delete(ThePatient p) throws StoreException {

    }

    @Override
    /**
     * not currently implemented
     */
    public void delete(Patient p) throws StoreException {

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
    public PatientNotification read(PatientNotification patientNotification)throws StoreException{
        EntityStoreType value;
        PatientNotification result;
        if (patientNotification.getKey()!= null){
            try{
                setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
                value = runSQL(Store.EntitySQL.PATIENT_NOTIFICATION, 
                            Store.PMSSQL.READ_PATIENT_NOTIFICATION_WITH_KEY, 
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
        }else{
            throw new StoreException(
                    message + "StoreException raised -> undefined key in specified patient notification "
                        + "in method AccessStore::read(PatientNotification)\n",
                    StoreException.ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
    }
    
    /**
     * method fetches a collection of patient notifications from store
     * -- the specified collection object defines the scope of the required collection
     * -- for each notification's patient only the key value is returned; its the responsibility of the caller to issue another read request per notification to fetch the patient's other details, if this is necessary 
     * @param patientNotificationCollection
     * @return
     * @throws StoreException 
     */
    @Override
    public PatientNotification.Collection read(PatientNotification.Collection patientNotificationCollection)throws StoreException{
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
                                PMSSQL.READ_ALL_PATIENT_NOTIFICATIONS, 
                                patientNotificationCollection);
                    break;
                case ALL_FOR_PATIENT:
                    value = runSQL(EntitySQL.PATIENT_NOTIFICATION,
                                PMSSQL.READ_PATIENT_NOTIFICATIONS_FOR_PATIENT, 
                                patientNotificationCollection);
                    break;
            }
            if (value!=null){
                    if (value.getIsPatientNotificationCollection()){
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
        IEntityStoreType value = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            value = runSQL(PMSSQL.READ_SURGERY_DAYS, null);
            if (value != null) {
                if (value.isSurgeryDaysAssignment()) {
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

    @Override
    /**
     * Explicit manual transaction processing enabled -- reads the appointment
     * stored on the system specified by the key in the parameter
     *
     * @param a:Appointment specifying the key
     * @return Appointment
     * @throws StoreException
     */
    public Appointment read(Appointment a) throws StoreException {
        boolean result = false;
        IEntityStoreType entity = null;
        Appointment appointment = null;
        try {//ensure auto commit setting switched on
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            entity = runSQL(PMSSQL.READ_APPOINTMENT_WITH_KEY, a);
            if (entity.isAppointment()) {
                appointment = (Appointment) entity;
                if (appointment.getPatient() != null) {
                    entity = runSQL(PMSSQL.READ_PATIENT_WITH_KEY, appointment.getPatient());
                    if (entity.isPatient()) {
                        appointment.setPatient((Patient) entity);
                    }
                }
                result = true;
            }
            return appointment;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::read(Appointment)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getPMSConnection().commit();
                } else {
                    getPMSConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.read(Appointment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }
    
    @Override
    public ThePatient read(ThePatient p) throws StoreException {
        EntityStoreType entity = null;
        boolean result = false;
        ThePatient thePatient = null;
        try {//ensure auto commit setting switched on
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(true);
            }
            entity
                    = runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENT_WITH_KEY, p);
            if (entity == null) {
                //if (patients.isEmpty()){//patient with this key not found
                throw new StoreException(
                        "Could not find patient with key = " + String.valueOf(p.getKey() + " in AccessStore::read(ThePatient)"),
                        StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            } else {
                ThePatient g = null;
                thePatient = (ThePatient) entity;
                //patient = patients.get(0);
                if (thePatient.getGuardian() != null) {
                    entity = runSQL(EntitySQL.PATIENT,PMSSQL.READ_PATIENT_WITH_KEY, thePatient.getGuardian());
                    if (entity != null) {
                        g = (ThePatient) entity;
                        thePatient.setGuardian(g);
                    }
                }
            }
            result = true;
            return thePatient;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::read(ThePatient p)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public ThePatient.Collection read(ThePatient.Collection p) throws StoreException{
        EntityStoreType value = null;
        ThePatient.Collection result = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            value = runSQL(EntitySQL.PATIENT,PMSSQL.READ_ALL_PATIENTS,null);
            if (value!=null){
                if (value.getIsPatients()){
                    result = (ThePatient.Collection)value;
                    return result;
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
     * Explicit manual transaction processing enabled -- reads the patient
     * stored on the system specified by key in parameter
     *
     * @param Patient specifying key (and probably nothing else)
     * @return Patient
     * @throws StoreException
     */
    public Patient read(Patient p) throws StoreException {
        IEntityStoreType entity = null;
        boolean result = false;
        Patient patient = null;
        try {//ensure auto commit setting switched on
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(false);
            }
            entity
                    = runSQL(PMSSQL.READ_PATIENT_WITH_KEY, p);
            if (entity == null) {
                //if (patients.isEmpty()){//patient with this key not found
                throw new StoreException(
                        "Could not find patient with key = " + String.valueOf(p.getKey() + " in AccessStore::read(Patient)"),
                        StoreException.ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            } else {
                Patient g = null;
                patient = (Patient) entity;
                //patient = patients.get(0);
                if (patient.getGuardian() != null) {
                    entity = runSQL(PMSSQL.READ_PATIENT_WITH_KEY, patient.getGuardian());
                    if (entity != null) {
                        g = (Patient) entity;
                        patient.setGuardian(g);
                    }
                }
            }
            result = true;
            return patient;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::read(Patient p)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getPMSConnection().commit();
                } else {
                    getPMSConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.read(Patient))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Explicit manual transaction processing enabled -- reads from either the
     * pms or migration store the current path of the store
     *
     * @param db:SelectedTargetStore specifies which target store (pms or
     * migration) is read
     * @throws StoreException
     */
    public String read(SelectedTargetStore db) throws StoreException {
        boolean result = false;
        String location = null;
        String sql = "Select location from Target WHERE db = ?;";
        try {
            if (getTargetConnection().getAutoCommit()) {
                getTargetConnection().setAutoCommit(false);
            }
            try {
                PreparedStatement preparedStatement = getTargetConnection().prepareStatement(sql);
                preparedStatement.setString(1, db.toString());
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    location = rs.getString("location");
                }
                result = true;
                return location;
            } catch (SQLException ex) {
                throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                        + "StoreException message -> exception raised during DbLocationStore::read() query",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.read(SelectedTargetStore))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                /**
                 * DEBUG - references should be to getTargetConnection
                 */
                if (result) {
                    getTargetConnection().commit();
                } else {
                    getTargetConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore.read(SelectedTargetStore))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }//store_package_updates_05_12_21_09_17_devDEBUG

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

    /**
     * Automatic transaction processing enabled -- reads all appointments stored
     * on the system
     *
     * @return ArrayList; collection of appointments
     * @throws StoreException, used to wrap SQLException if thrown
     */
    @Override
    public Appointments readAppointments() throws StoreException {
        boolean result = false;
        IEntityStoreType readAppointments = null;
        Appointments appointments = null;
        try {//ensure auto commit setting switched on
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            readAppointments = runSQL(PMSSQL.READ_APPOINTMENTS, null);
            if (readAppointments != null) {
                if (readAppointments.isAppointments()) {
                    appointments = (Appointments) readAppointments;
                }
                Iterator<Appointment> it = appointments.iterator();
                while (it.hasNext()) {
                    Appointment appointment = it.next();
                    Patient p = read(appointment.getPatient());
                    appointment.setPatient(p);
                }
            }
            result = true;
            return appointments;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in Access::readAppointments() arising from call to setConnectionMode()",
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
                        message + "StoreException raised in finally clause of method AccessStore::readAppointments()\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    /**
     * Automatic transaction processing enabled -- reads all appointments stored
     * on the system from specified date
     *
     * @param day:LocalDate specifying the date from which appointments are read
     * back from the system
     * @return ArrayList; collection of appointments
     * @throws StoreException
     */
    @Override
    public Appointments readAppointmentsFrom(LocalDate day) throws StoreException {
        boolean result = false;
        IEntityStoreType readAppointments = null;
        Appointments appointments = null;

        try {//ensure auto commit setting switched on
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            readAppointments = runSQL(PMSSQL.READ_APPOINTMENTS_FROM_DAY, new AppointmentDate(day));
            if (readAppointments != null) {
                if (readAppointments.isAppointments()) {
                    appointments = (Appointments) readAppointments;
                    Iterator<Appointment> it = appointments.iterator();
                    while (it.hasNext()) {
                        Appointment appointment = it.next();
                        Patient p = read(appointment.getPatient());
                        appointment.setPatient(p);
                    }
                } else {
                    throw new StoreException("Exception raised in Access::readAppointmentsFrom()",
                            StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                result = true;
                return appointments;
            } else {
                result = true;
                return appointments;
            }
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::readAppointmentsFrom(LocalDate d)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore::readAppointmentsFrom()\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Automatic transaction processing enabled -- reads all appointments stored
     * on the system on a specified date
     *
     * @param day:LocalDate specifying the date on which appointments are read
     * back from the system
     * @return ArrayList<Appointment>
     * @throws StoreException
     */
    public Appointments readAppointmentsFor(LocalDate day) throws StoreException {
        boolean result = false;
        IEntityStoreType readAppointments = null;
        Appointments appointments = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            readAppointments = runSQL(PMSSQL.READ_APPOINTMENTS_FOR_DAY, new AppointmentDate(day));
            if (readAppointments != null) {
                if (readAppointments.isAppointments()) {
                    appointments = (Appointments) readAppointments;
                    Iterator<Appointment> it = appointments.iterator();
                    while (it.hasNext()) {
                        Appointment appointment = it.next();
                        Patient p = read(appointment.getPatient());
                        appointment.setPatient(p);
                    }
                } else {
                    throw new StoreException("Exception raised in Access::readAppointmentsFrom()",
                            StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                result = true;
                return appointments;
            } else {
                result = true;
                return appointments;
            }
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing setConnectionMode(AUTO_COMMIT_ON) in AccessStore::readAppointmentsFor(LocalDate d)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore::readAppointmentsFrom()\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Automatic transaction processing enabled -- reads all appointments stored
     * on the system for a specified patient
     *
     * @param p:Patient specifying the patient stored appointments on the system
     * belong to
     * @param c: Appointment.Category (dental or hygiene appointments, ignored
     * on this version of app)
     * @return ArrayList<Appointment>
     * @throws StoreException
     */
    public Appointments readAppointments(Patient p, Appointment.Category c) throws StoreException {
        boolean result = false;
        IEntityStoreType readAppointments = null;
        Appointments appointments = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            readAppointments = runSQL(PMSSQL.READ_APPOINTMENTS_FOR_PATIENT, p);
            if (readAppointments != null) {
                if (readAppointments.isAppointments()) {
                    appointments = (Appointments) readAppointments;
                    Iterator<Appointment> it = appointments.iterator();
                    while (it.hasNext()) {
                        Appointment appointment = it.next();
                        Patient patient = read(appointment.getPatient());
                        appointment.setPatient(patient);
                    }
                } else {
                    throw new StoreException("Exception raised in Access::readAppointments(Patient, Category)",
                            StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                }
                result = true;
                return appointments;
            } else {
                result = true;
                return appointments;
            }
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::readAppointments(Patient, Category)",
                    StoreException.ExceptionType.SQL_EXCEPTION);

        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore::readAppointments(Patient, Category)\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Automatic transaction processing enabled -- reads all patients stored on
     * the system
     *
     * @return ArrayList<Patien t>
     * @throws StoreException
     */
    public Patients readPatients() throws StoreException {
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON); //autoCommit() calls replaced 
            Patients readPatients = (Patients) runSQL(PMSSQL.READ_ALL_PATIENTS, new Patient());
            return readPatients;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::readPatients()",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    } //store_package_updates_05_12_21_09_17_devDEBUG

    @Override
    public void update(ThePatient p) throws StoreException {
        
    }
    
    /**
     * method sends the specified pn to persistent store
     * @param pn
     * @throws StoreException if exception arises in transaction control
     */
    @Override
    public void update(PatientNotification pn)throws StoreException{
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(true);
            }
            runSQL(EntitySQL.PATIENT_NOTIFICATION, PMSSQL.UPDATE_PATIENT_NOTIFICATION,pn);
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::update(Patient)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }
    
    @Override
    /**
     * Explicit manual transaction processing enabled -- updates the specified
     * by key patient
     *
     * @param p:Patient specified the patient to be updated on the system
     * @throws StoreException
     */
    public void update(Patient p) throws StoreException {
        boolean result = false;
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(false);
            }
            runSQL(PMSSQL.UPDATE_PATIENT, p);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::update(Patient)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getPMSConnection().commit();
                } else {
                    getPMSConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.update(Patient))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Explicit manual transaction processing enabled -- updates the specified
     * by key appointment stored on the system
     *
     * @param a:Appointment specifies which appointment on the system is to be
     * updated
     * @throws StoreException
     */
    public void update(Appointment a) throws StoreException {
        boolean result = false;
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(false);
            }
            runSQL(PMSSQL.UPDATE_APPOINTMENT, a);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::readAppointments(LocalDate d)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getPMSConnection().commit();
                } else {
                    getPMSConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause AccessStore.delete(Appointment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    /**
     * Explicit manual transaction processing enabled -- updates which days are
     * surgery days on the system
     *
     * @param value:HashMap<SayOfWeek, Boolean> the new set of surgery days used
     * to update currently stored values
     * @throws StoreException
     */
    public void update(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        boolean result = false;
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(true);
            }
            runSQL(PMSSQL.UPDATE_SURGERY_DAYS, surgeryDaysAssignment);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException -> " + ex.getMessage() + "\n";
            throw new StoreException(message + "StoreException -> unexpected error accessing AutoCommit/commit/rollback setting in AccessStore::update(HashMap<DayOfWeek,Boolean>)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getPMSConnection().commit();
                else getPMSConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause AccessStore.update(HashMap<DayOfWeek,Boolean>))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);    
            }
        } 
         */
    }

    public void tidyPatientImportedDate() throws StoreException {
        Patients patients = (Patients) runSQL(PMSSQL.READ_ALL_PATIENTS,
                new Patient());
        Iterator<Patient> patientsIT = patients.iterator();
        while (patientsIT.hasNext()) {
            Patient patient = patientsIT.next();
            Patient patient1 = doCapitaliseFirstLetterOnly(patient);
            patient.getName().setForenames(patient1.getName().getForenames());
            patient.getName().setSurname(patient1.getName().getSurname());
            patient.getName().setTitle(patient1.getName().getTitle());
            patient.getAddress().setLine1(patient1.getAddress().getLine1());
            patient.getAddress().setLine2(patient1.getAddress().getLine2());
            patient.getAddress().setTown(patient1.getAddress().getTown());
            patient.getAddress().setCounty(patient1.getAddress().getCounty());
            if (patient.getGender() == null) {
                patient.setGender("");
            }
            patient1 = updateGender(patient);
            patient.setGender(patient1.getGender());
            runSQL(PMSSQL.UPDATE_PATIENT, patient);
        }
    }

    /**
     * 05/12/2021 11:00 updates included at end of storage type class
     */
    /**
     *
     * @param table
     * @throws StoreException
     */
    @Override
    public void create(AppointmentTable table) throws StoreException {
        boolean result = false;
        try {
            getMigrationConnection().setAutoCommit(true);
            /*
            if (getMigrationConnection().getAutoCommit()){
                getMigrationConnection().setAutoCommit(false);
            }
             */
            IEntityStoreType value = null;
            runSQL(MigrationSQL.APPOINTMENT_TABLE_CREATE, value);
            /**
             * log entry 10/02/2022 19:01 fir further info
             * runSQL(MigrationSQL.APPOINTMENT_TABLE_ADD_FOREIGN_KEY, value);
             */

        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(AppointmentTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.create(AppointmentTable))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }

    @Override
    public void create(Appointment table) throws StoreException {
        boolean result = false;
        try {
            getPMSConnection().setAutoCommit(true);
            /*
            if (getMigrationConnection().getAutoCommit()){
                getMigrationConnection().setAutoCommit(false);
            }
             */
            IEntityStoreType value = null;
            runSQL(PMSSQL.APPOINTMENT_TABLE_CREATE, value);

        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(Appointment))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.create(AppointmentTable))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }

    @Override
    /**
     * Explicit transaction processing enabled for attempt to drop the Patient
     * migration table
     */
    public void create(PatientTable table) throws StoreException {
        boolean result = false;
        try {
            /*
            if (getMigrationConnection().getAutoCommit()){
                getMigrationConnection().setAutoCommit(false);
            }
             */
            getMigrationConnection().setAutoCommit(true);
            IEntityStoreType value = null;
            runSQL(MigrationSQL.PATIENT_TABLE_CREATE, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(PatientTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.create(PatentTable))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }
    
    @Override
    public void create(ThePatient table) throws StoreException{
        
    }

    @Override
    /**
     * Explicit transaction processing enabled for attempt to drop the Patient
     * migration table
     */
    public void create(Patient table) throws StoreException {
        boolean result = false;
        try {
            /*
            if (getMigrationConnection().getAutoCommit()){
                getMigrationConnection().setAutoCommit(false);
            }
             */
            getPMSConnection().setAutoCommit(false);
            IEntityStoreType value = null;
            runSQL(PMSSQL.PATIENT_TABLE_CREATE, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(Patient))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                if (result) {
                    getPMSConnection().commit();
                } else {
                    getPMSConnection().rollback();
                }
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore.create(PatentTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }

    }

    @Override
    public void create(SurgeryDaysAssignment s) throws StoreException {
        try {
            getPMSConnection().setAutoCommit(true);
            IEntityStoreType value = null;
            runSQL(PMSSQL.SURGERY_DAYS_TABLE_CREATE, value);
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(SurgeryDaysTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    /**
     * Explicit transaction processing enabled for attempt to drop the
     * SurgeryDays table migration table
     */
    public void create(SurgeryDaysAssignmentTable table) throws StoreException {
        boolean result = false;
        try {
            if (getMigrationConnection().getAutoCommit()) {
                getMigrationConnection().setAutoCommit(false);
            }
            IEntityStoreType value = null;
            runSQL(MigrationSQL.SURGERY_DAYS_TABLE_CREATE, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.create(SurgeryDaysTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
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
                        message + "StoreException raised in finally clause of method AccessStore.create(SurgeryDaysTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    public void drop(Appointment table) throws StoreException {
        boolean result = false;
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(true);
            }
            IEntityStoreType value = null;
            runSQL(PMSSQL.APPOINTMENT_TABLE_DROP, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.drop(Appointment)\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.drop(Appointment))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }
    
    @Override
    public void drop(ThePatient table) throws StoreException {
    
    }

    @Override
    public void drop(Patient table) throws StoreException {
        boolean result = false;
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(true);
            }
            IEntityStoreType value = null;
            runSQL(PMSSQL.PATIENT_TABLE_DROP, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.drop(Patient)\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.drop(Patient))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }

    public void drop(SurgeryDaysAssignment table) throws StoreException {
        boolean result = false;
        try {
            if (getPMSConnection().getAutoCommit()) {
                getPMSConnection().setAutoCommit(true);
            }
            IEntityStoreType value = null;
            runSQL(PMSSQL.SURGERY_DAYS_TABLE_DROP, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.drop(SurgeryDaysAssignment)\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.drop(SurgeryDaysAssignment))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }

    @Override
    /**
     * Explicit transaction processing enabled for attempt to drop the
     * Appointment migration table
     */
    public void drop(AppointmentTable table) throws StoreException {
        boolean result = false;
        try {
            if (getMigrationConnection().getAutoCommit()) {
                getMigrationConnection().setAutoCommit(false);
            }
            IEntityStoreType value = null;
            runSQL(MigrationSQL.APPOINTMENT_TABLE_DROP, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised AccessStore.drop(AppointmentTable)\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
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
                        message + "StoreException raised in finally clause of method AccessStore.drop(AppointmentTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    @Override
    /**
     * Explicit transaction processing enabled for attempt to drop the Patient
     * migration table
     */
    public void drop(PatientTable table) throws StoreException {
        boolean result = false;
        try {
            if (getMigrationConnection().getAutoCommit()) {
                getMigrationConnection().setAutoCommit(true);
            }
            IEntityStoreType value = null;
            runSQL(MigrationSQL.PATIENT_TABLE_DROP, value);

        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in AccessStore.MigrationManager::drop(PatientTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                if (result) getMigrationConnection().commit();
                else getMigrationConnection().rollback();
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.drop(PatientTable))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }

    @Override
    /**
     * Explicit transaction processing enabled for the attempt to drop the
     * SurgeryDays migration table
     */
    public void drop(SurgeryDaysAssignmentTable table) throws StoreException {
        boolean result = false;
        try {
            if (getMigrationConnection().getAutoCommit()) {
                getMigrationConnection().setAutoCommit(false);
            }
            IEntityStoreType value = null;
            runSQL(MigrationSQL.SURGERY_DAYS_TABLE_DROP, value);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in AccessStore.drop(SurgeryDaysTable))\n"
                    + "Reason -> unexpected effect when auto commit state disabled",
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
                        message + "StoreException raised in finally clause of method AccessStore.drop(SurgeryDaysTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

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
    public void populate(SurgeryDaysAssignmentTable surgeryDaysAssignmentTable) throws StoreException {
        boolean result = false;
        try {
            if (getMigrationConnection().getAutoCommit()) {
                getMigrationConnection().setAutoCommit(false);
            }
            runSQL(MigrationSQL.SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION, null);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(HashMap<DayOfWeek,Boolean> surgeryDays)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
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
                        message + "StoreException raised in finally clause of method AccessStore.populate(SurgeryDaysAssignment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    @Override
    public void populate(SurgeryDaysAssignment surgeryDaysAssignment) throws StoreException {
        boolean result = false;
        try {
            if (getMigrationConnection().getAutoCommit()) {
                getMigrationConnection().setAutoCommit(false);
            }
            runSQL(MigrationSQL.SURGERY_DAYS_TABLE_DEFAULT_INITIALISATION, surgeryDaysAssignment);
            result = true;
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(HashMap<DayOfWeek,Boolean> surgeryDays)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
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
                        message + "StoreException raised in finally clause of method AccessStore.populate(SurgeryDaysAssignment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    @Override
    public void checkIntegrity() throws StoreException {
        boolean result = false;
        IEntityStoreType entity;
        Appointments appointments = null;
        Integer key;
        HashSet<Integer> nonExistingPatientsReferencedbyAppointments = new HashSet<>();
        HashSet<Integer> patientSet = new HashSet<>();

        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            entity = runSQL(MigrationSQL.APPOINTMENT_TABLE_READ, new Appointments());
            if (entity.isAppointments()) {
                appointments = (Appointments) entity;
                Iterator<Appointment> it = appointments.iterator();
                while (it.hasNext()) {
                    Appointment appointment = it.next();
                    key = appointment.getPatient().getKey();
                    patientSet.add(key);
                }
                Iterator<Integer> patientSetIt = patientSet.iterator();
                while (patientSetIt.hasNext()) {
                    key = patientSetIt.next();
                    entity = runSQL(MigrationSQL.PATIENT_TABLE_READ_PATIENT, new Patient(key));
                    if (entity == null) {
                        nonExistingPatientsReferencedbyAppointments.add(key);
                    } else if (!entity.isPatient()) {
                        String message = "StoreException -> entity incorrectly defined in AccessStore::checkIntegrity, while executing MigrationSQL.PATIENT_TABLE_READ_PATIENT";
                        throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
                    }
                }
                /**
                 * deletes appointment records which reference non existing
                 * patients
                 */
                this.setNonExistingPatientsReferencedByAppointmentsCount(nonExistingPatientsReferencedbyAppointments.size());
                Iterator<Integer> nonExistingPatientKeysIt = nonExistingPatientsReferencedbyAppointments.iterator();
                while (nonExistingPatientKeysIt.hasNext()) {
                    Integer nonExistingPatientKey = nonExistingPatientKeysIt.next();
                    runSQL(MigrationSQL.APPOINTMENT_TABLE_DELETE_APPOINTMENT_WITH_PATIENT_KEY,
                            new PatientTableRowValue(nonExistingPatientKey));
                }
                result = true;
            } else {
                String message = "StoreException -> appointments undefined in AccessStore::checkIntegrity, while executing MigrationSQL.APPOINTMENT_TABLE_READ";
                throw new StoreException(message, StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.checkIntegrity()\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
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
                        message + "StoreException raised in finally clause of method AccessStore.populate(SurgeryDaysAssignment))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    @Override
    public void updateMigrationTargetStorePath(String path) throws StoreException {
        update(SelectedTargetStore.MIGRATION_DB, path);
        setMigrationDatabasePath(read(SelectedTargetStore.MIGRATION_DB));
    }

    @Override
    public void updatePMSTargetStorePath(String path) throws StoreException {
        update(SelectedTargetStore.PMS_DB, path);
        setPMSDatabasePath(read(SelectedTargetStore.PMS_DB));
    }

    @Override
    public void updateAppointmentCSVPath(String path) throws StoreException {
        update(SelectedTargetStore.CSV_APPOINTMENT_FILE, path);
        //setAppointmentCSVPath(read(SelectedTargetStore.CSV_APPOINTMENT_FILE));
    }

    @Override
    public void updatePatientCSVPath(String path) throws StoreException {
        update(SelectedTargetStore.CSV_PATIENT_FILE, path);
        //setPatientCSVPath(read(SelectedTargetStore.CSV_PATIENT_FILE));
    }

    @Override
    public String readMigrationTargetStorePath() throws StoreException {
        /**
         * DEBUG -- adds check and action on initialisation of
         * migrationDatabasePath -- and direct access to store database path
         * variable to avoid use of store setter
         */
        if (getMigrationDatabasePath() == null) {
            migrationDatabasePath = read(SelectedTargetStore.MIGRATION_DB);
        }
        setMigrationDatabasePath(migrationDatabasePath);
        return getMigrationDatabasePath();
    }//store_package_updates_05_12_21_09_17_devDEBUG

    @Override
    public String readPMSTargetStorePath() throws StoreException {
        /**
         * DEBUG -- adds check and action on initialisation of
         * migrationDatabasePath -- and direct access to store database path
         * variable to avoid use of store setter
         */
        if (getPMSDatabasePath() == null) {
            pmsDatabasePath = read(SelectedTargetStore.PMS_DB);
        }
        return getPMSDatabasePath();
    }//store_package_updates_05_12_21_09_17_devDEBUG

    @Override
    public String readAppointmentCSVPath() throws StoreException {
        return read(SelectedTargetStore.CSV_APPOINTMENT_FILE);
    }

    @Override
    public String readPatientCSVPath() throws StoreException {
        return read(SelectedTargetStore.CSV_PATIENT_FILE);
    }

    @Override
    public String getStoreType() {
        return getStorageType().toString();
    }

    public int countRowsIn(SurgeryDaysAssignmentTable table) throws StoreException {
        int count;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            count = ((AppointmentTableRowValue) runSQL(MigrationSQL.SURGERY_DAYS_TABLE_ROW_COUNT, null)).getValue();
            return count;
        } catch (SQLException e) {
            String text = "SQLException message -> " + e.getMessage() + "\n";
            throw new StoreException(text + "Exception encountered in AccessStore::countRowsIn(AppointmentTable)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public int countRowsIn(AppointmentTable table) throws StoreException {
        int count;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            count = ((AppointmentTableRowValue) runSQL(MigrationSQL.APPOINTMENT_TABLE_ROW_COUNT, null)).getValue();
            return count;
        } catch (SQLException e) {
            String text = "SQLException message -> " + e.getMessage() + "\n";
            throw new StoreException(text + "Exception encountered in AccessStore::countRowsIn(AppointmentTable)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public int countRowsIn(PatientTable table) throws StoreException {
        Integer count = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            count = ((PatientTableRowValue) runSQL(MigrationSQL.PATIENT_TABLE_ROW_COUNT, null)).getValue();
            return count;
        } catch (SQLException e) {
            String text = "SQLException message -> " + e.getMessage() + "\n";
            throw new StoreException(text + "Exception encountered in AccessStore::countRowsIn(PatientTable)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    public int countRowsIn(Appointments appointments) throws StoreException {
        Integer count = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            count = ((AppointmentTableRowValue) runSQL(PMSSQL.APPOINTMENTS_COUNT, null)).getValue();
            return count;
        } catch (SQLException e) {
            String text = "SQLException message -> " + e.getMessage() + "\n";
            throw new StoreException(text + "Exception encountered in AccessStore::countRowsIn(Appontments)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }

    }

    public int countRowsIn(Patients patients) throws StoreException {
        Integer count = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            count = ((PatientTableRowValue) runSQL(PMSSQL.PATIENTS_COUNT, null)).getValue();
            return count;
        } catch (SQLException e) {
            String text = "SQLException message -> " + e.getMessage() + "\n";
            throw new StoreException(text + "Exception encountered in AccessStore::countRowsIn(Patients)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }

    }

    public void setPatientCount(int value) {
        patientCount = value;
    }

    /*
    public void migratedPatientsTidied()throws StoreException{
        Patients patients = null;
        IEntityStoreType entity = runSQL(Store.MigrationSQL.PATIENT_TABLE_READ,new Patient());
        if (entity.isPatients()) patients = (Patients)entity; 
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
            runSQL(Store.MigrationSQL.PATIENT_TABLE_UPDATE, patient);
        } 
    }
     */
    public int getNonExistingPatientsReferencedByAppointmentsCount() {
        return nonExistingPatientsReferencedByAppointmentsCount;
    }

    public void setNonExistingPatientsReferencedByAppointmentsCount(int value) {
        nonExistingPatientsReferencedByAppointmentsCount = value;
    }

    @Override
    public Appointments read(AppointmentTable table) throws StoreException {
        boolean result = false;
        IEntityStoreType entity = null;
        Appointments appointments = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            entity = runSQL(MigrationSQL.APPOINTMENT_TABLE_READ, null);
            if (entity.isAppointments()) {
                appointments = (Appointments) entity;
                result = true;
                return appointments;
            } else {
                throw new StoreException("StoreException raised because unexpected data type returned from runSQL(MigrationSQL.APPOINT_TABLE_READ while executing Access::read(AppointmentTable)()",
                        StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(PatientTable)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore.read(PatientsTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);

            }
        }
    }

    @Override
    public Patients read(PatientTable table) throws StoreException {
        boolean result = false;
        IEntityStoreType entity = null;
        Patients patients = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_OFF);
            entity = runSQL(MigrationSQL.PATIENT_TABLE_READ, null);
            if (entity.isPatients()) {
                patients = (Patients) entity;
                result = true;
                return patients;
            } else {
                throw new StoreException("StoreException raised because unexpected data type returned from runSQL(MigrationSQL.SURGERY_DAYS_TABLE_READ while executing Access::read(PatientTable)()",
                        StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(PatientTable)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        } finally {
            try {
                setConnectionState(result);
            } catch (SQLException ex) {
                message = "SQLException message -> " + ex.getMessage() + "\n";
                throw new StoreException(
                        message + "StoreException raised in finally clause of method AccessStore.read(PatientsTable))\n"
                        + "Reason -> unexpected effect when terminating the current transaction",
                        StoreException.ExceptionType.SQL_EXCEPTION);
            }
        }
    }

    @Override
    public SurgeryDaysAssignment read(SurgeryDaysAssignmentTable table) throws StoreException {
        boolean result = false;
        IEntityStoreType entity = null;
        SurgeryDaysAssignment surgeryDaysAssignment = null;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            entity = runSQL(MigrationSQL.SURGERY_DAYS_TABLE_READ, null);
            if (entity.isSurgeryDaysAssignment()) {
                surgeryDaysAssignment = (SurgeryDaysAssignment) entity;
                result = true;
                return surgeryDaysAssignment;
            } else {
                throw new StoreException("StoreException raised because unexpected data type returned from runSQL(MigrationSQL.SURGERY_DAYS_TABLE_READ while executing Access::read(SurgeryDaysAssignmentTable)()",
                        StoreException.ExceptionType.UNEXPECTED_DATA_TYPE_ENCOUNTERED);
            }
        } catch (SQLException ex) {
            message = "SQLException message -> " + ex.getMessage() + "\n";
            throw new StoreException(
                    message + "StoreException raised in method AccessStore.populate(PatientTable)\n"
                    + "Reason -> unexpected effect when trying to change the auto commit state",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
        /*
        finally{
            try{
                setConnectionState(result);
            }catch (SQLException ex){
                message = "SQLException message -> " + ex.getMessage() +"\n";
                throw new StoreException(
                    message + "StoreException raised in finally clause of method AccessStore.read(PatientsTable))\n"
                            + "Reason -> unexpected effect when terminating the current transaction",
                    StoreException.ExceptionType.SQL_EXCEPTION);
                
            }
        }
         */
    }

    @Override
    public void exportToPMS(Appointments table) throws StoreException {
        boolean result = false;
        try {
            getMigrationConnection().setAutoCommit(true);
            //setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            runSQL(MigrationSQL.EXPORT_MIGRATED_DATA_TO_PMS, table);
            //setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            //getPMSConnection().setAutoCommit(true);
            //runSQL(PMSSQL.CREATE_PRIMARY_KEY,table);
            result = true;
        } catch (SQLException ex) {
            String message = ex.getMessage() + "\n";
            throw new StoreException("StoreException raised on manipulation if autocommit state in AccessStore::exportToPMS(Appointments)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public void exportToPMS(Patients table) throws StoreException {
        boolean result = false;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            runSQL(MigrationSQL.EXPORT_MIGRATED_DATA_TO_PMS, table);
            result = true;
        } catch (SQLException ex) {
            String message = ex.getMessage() + "\n";
            throw new StoreException("StoreException raised on manipulation if autocommit state in AccessStore::exportToPMS(Patients)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public void exportToPMS(SurgeryDaysAssignment table) throws StoreException {
        boolean result = false;
        try {
            setConnectionMode(ConnectionMode.AUTO_COMMIT_ON);
            runSQL(MigrationSQL.EXPORT_MIGRATED_DATA_TO_PMS, table);
            result = true;
        } catch (SQLException ex) {
            String message = ex.getMessage() + "\n";
            throw new StoreException("StoreException raised on manipulation if autocommit state in AccessStore::exportToPMS(SurgeryDaysAssignment)",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }
    }

    @Override
    public void insert(Appointments appointments) throws StoreException {
        int count = 0;
        Iterator<Appointment> appointmentsIterator = appointments.iterator();
        while (appointmentsIterator.hasNext()) {
            try {
                insert(appointmentsIterator.next());
                count = count + 1;
            } catch (StoreException ex) {
                String message = "StoreException raised in AccessStore::insert(Appointment)\n";
                throw new StoreException(message + "AccessStore.insert(Appointments) had completed "
                        + String.valueOf(count) + " record insertions before the exception arose", StoreException.ExceptionType.STORE_EXCEPTION);
            }
        }
    }

    @Override
    public void insert(Patients patients) throws StoreException {
        int count = 0;
        Iterator<Patient> patientsIterator = patients.iterator();
        while (patientsIterator.hasNext()) {
            try {
                insert(patientsIterator.next());
                count = count + 1;
            } catch (StoreException ex) {
                String message = "StoreException raised in AccessStore::insert(Patient)\n";
                throw new StoreException(message + "AccessStore.insert(Patients) had completed "
                        + String.valueOf(count) + " record insertions before the exception arose", StoreException.ExceptionType.STORE_EXCEPTION);
            }
        }
    }

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
    
    private EntityStoreType doAppointmentPMSSQL(PMSSQL q, EntityStoreType entity){
        return null;
    }
    
    private EntityStoreType doAppointmentsPMSSQL(PMSSQL q, EntityStoreType entity){
        return null;
    }
    
    private EntityStoreType doPatientPMSSQL(PMSSQL q, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (q){
            case INSERT_PATIENT:
                sql
                    = "INSERT INTO Patient "
                    + "(title, forenames, surname, line1, line2,"
                    + "town, county, postcode,phone1, phone2, gender, dob,"
                    + "isGuardianAPatient, recallFrequency, recallDate, notes,pid) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                doInsertPatient(sql, entity);
                break;
            case READ_ALL_PATIENTS:
                sql = "SELECT * FROM Patient ORDER BY surname, forenames ASC;";;
                result = doReadAllPatients(sql);
                break;
            case READ_PATIENT_WITH_KEY:
                sql = "SELECT pid, title, forenames, surname, line1, line2, "
                        + "town, county, postcode, gender, dob, isGuardianAPatient, "
                        + "phone1, phone2, recallFrequency, recallDate, notes, guardianKey "
                        + "FROM Patient "
                        + "WHERE pid=?;";
                result = doReadPatientWithKey(sql, entity);
                break;
        }
        return result;
    }
    
    private EntityStoreType doPatientsPMSSQL(PMSSQL q, EntityStoreType entity){
        return null;
    }
    
    private EntityStoreType doPatientNotificationPMSSQL(PMSSQL q, EntityStoreType entity) throws StoreException{
        EntityStoreType result = null;
        String sql = null;
        switch (q){
            case INSERT_PATIENT_NOTIFICATION:
                sql = "INSERT INTO PatientNotification "
                        + "(patientToNotify, notificationDate, notificationText, pid) "
                        + "VALUES(?,?,?,?);";
                result = doInsertPatientNotification(sql, entity);
                break;
            case READ_PATIENT_NOTIFICATION_HIGHEST_KEY:
                sql = "SELECT MAX(pid) as highest_key "
                        + "FROM PatientNotification;";
                result = doReadHighestKey(sql);
                break;
            case READ_PATIENT_NOTIFICATION_WITH_KEY:
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
            case READ_ALL_PATIENT_NOTIFICATIONS:
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
    
    private void doInsertPatient(String sql, EntityStoreType entity)throws StoreException{
        ThePatient thePatient;
        if (entity != null) {
            if (entity.getIsPatient()) {
                thePatient = (ThePatient) entity;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.setString(1, thePatient.getName().getTitle());
                    preparedStatement.setString(2, thePatient.getName().getForenames());
                    preparedStatement.setString(3, thePatient.getName().getSurname());
                    preparedStatement.setString(4, thePatient.getAddress().getLine1());
                    preparedStatement.setString(5, thePatient.getAddress().getLine2());
                    preparedStatement.setString(6, thePatient.getAddress().getTown());
                    preparedStatement.setString(7, thePatient.getAddress().getCounty());
                    preparedStatement.setString(8, thePatient.getAddress().getPostcode());
                    preparedStatement.setString(9, thePatient.getPhone1());
                    preparedStatement.setString(10, thePatient.getPhone2());
                    preparedStatement.setString(11, thePatient.getGender());
                    if (thePatient.getDOB() != null) {
                        preparedStatement.setDate(12, java.sql.Date.valueOf(thePatient.getDOB()));
                    } else {
                        preparedStatement.setDate(12, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                    }
                    preparedStatement.setBoolean(13, thePatient.getIsGuardianAPatient());
                    preparedStatement.setInt(14, thePatient.getRecall().getDentalFrequency());
                    if (thePatient.getRecall().getDentalDate() != null) {
                        preparedStatement.setDate(15, java.sql.Date.valueOf(thePatient.getRecall().getDentalDate()));
                    } else {
                        preparedStatement.setDate(15, java.sql.Date.valueOf(LocalDate.of(1899, 1, 1)));
                    }
                    preparedStatement.setString(16, thePatient.getNotes());
                    preparedStatement.setLong(17, thePatient.getKey());
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
    
    private EntityStoreType doInsertPatientNotification(String sql, EntityStoreType entity) throws StoreException{
        PatientNotification  patientNotification = null;
        if (entity != null) {
            if (entity.getIsPatientNotification()) {
                patientNotification = (PatientNotification) entity;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patientNotification.getPatient().getKey());
                    preparedStatement.setDate(2, java.sql.Date.valueOf(patientNotification.getNotificationDate()));
                    preparedStatement.setString(3, patientNotification.getNotificationText());
                    preparedStatement.setLong(4, patientNotification.getKey());
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

        return null;
    }
    
    private void doUpdatePatientNotification(String sql, EntityStoreType entity) throws StoreException{
        PatientNotification  patientNotification = null;
        if (entity != null) {
            if (entity.getIsPatientNotification()) {
                patientNotification = (PatientNotification) entity;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patientNotification.getPatient().getKey());
                    preparedStatement.setDate(2, java.sql.Date.valueOf(patientNotification.getNotificationDate()));
                    preparedStatement.setString(3, patientNotification.getNotificationText());
                    preparedStatement.setBoolean(4, patientNotification.getIsActioned());
                    preparedStatement.setLong(5, patientNotification.getKey());
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
            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            ThePatient patient = new ThePatient();
            result = get(patient.getCollection(), rs);
            return result;
        } catch (SQLException ex) {
            throw new StoreException("SQLException message -> " + ex.getMessage() + "\n"
                    + "StoreException message -> exception raised in AccessStore::runSQL(PatientManagementSystemSQL..) during a READ_ALL_PATIENTS statement",
                    StoreException.ExceptionType.SQL_EXCEPTION);
        }   
    }
    
    private EntityStoreType doReadPatientWithKey(String sql, EntityStoreType entity)throws StoreException{
        EntityStoreType result = null;
        ThePatient patient = null;
        if (entity != null){
            if (entity.getIsPatient()){
                patient = (ThePatient)entity;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patient.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    result = get(new ThePatient(), rs);
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
            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
        PatientNotification patientNotification = null;
        if (entity != null) {
            if (entity.getIsPatient()) {
                ThePatient patient = (ThePatient) entity;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patient.getKey());
                    ResultSet rs = preparedStatement.executeQuery();
                    patientNotification = new PatientNotification();
                    return get(patientNotification.getCollection(), rs);
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
        PatientNotification patientNotification = null;
        if (entity != null) {
            if (entity.getIsPatientNotification()) {
                patientNotification = (PatientNotification) entity;
                try {
                    PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
                    preparedStatement.setLong(1, patientNotification.getKey());
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
    
    private EntityStoreType doSurgeryDaysAssignmentPMSSQL(PMSSQL q, EntityStoreType entity){
        return null;
    }
    
    private EntityStoreType doReadHighestKey(String sql) throws StoreException{
        try {
            Integer key = null;
            PreparedStatement preparedStatement = getPMSConnection().prepareStatement(sql);
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
}
