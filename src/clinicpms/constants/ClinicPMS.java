/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.constants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;

/**
 *
 * @author colin
 */
public class ClinicPMS {
    /*
    public static final String[] APPOINTMENT_COLUMNS = {"Key",
                                                        "Patient",
                                                        "Start",
                                                        "Duration",
                                                        "Notes",
                                                        "Entity"};
    public static final String[] PATIENT_COLUMNS = {"Key",
                                                    "Title",
                                                    "Forenames",
                                                    "Surname",
                                                    "Line1",
                                                    "Line2",
                                                    "Town",
                                                    "County",
                                                    "Postcode",
                                                    "Gender",
                                                    "DOB",
                                                    "IsGuardianAPatient",
                                                    "Guardian",
                                                    "LastDentalAppointment",
                                                    "NextDentalAppointment",
                                                    "NextHygieneAppointment",
                                                    "DentalRecallDate",
                                                    "HygieneRecallDate",
                                                    "DentalRecallFrequency",
                                                    "HygieneRecallFrequency",
                                                    "Notes",
                                                    "Entity"};
    */
    /*
    public enum AppointmentField {KEY,
                                   PATIENT,
                                   DAY,
                                   TIME,
                                   DURATION,
                                   NOTES}
    */
    /*                                                    
    public enum PatientField  {KEY,
                                TITLE,
                                FORENAMES,
                                SURNAME,
                                LINE1,
                                LINE2,
                                TOWN,
                                COUNTY,
                                POSTCODE,
                                PHONE1,
                                PHONE2,
                                GENDER,
                                DOB,
                                IS_GUARDIAN_A_PARENT,
                                GUARDIAN,
                                LAST_DENTAL_APPOINTMENT,
                                NEXT_DENTAL_APPOINTMENT,
                                NEXT_HYGIENE_APPOINTMENT,
                                DENTAL_RECALL_DATE,
                                HYGIENE_RECALL_DATE,
                                NOTES}
    
    
    public enum DesktopViewControllerActionEvent {
                                    
                                    DESKTOP_PATIENTS_LIST_REQUEST,
                                    DESKTOP_EXIT_REQUEST}
    
    public enum PatientViewControllerType {
                                    PATIENT_CONSTRUCTOR,
                                    PATIENT_EDITOR;
    }
    
    

    public enum PatientViewControllerActionEvent {
                                            PATIENT_SELECTION_ERROR,
                                            PATIENT_SELECTION_PERFORMED,
                                            PATIENT_SELECTION_CANCELLED,
                                            PATIENT_RECORDS_REQUEST,
                                            PATIENT_CONSTRUCTOR_VIEW_REQUEST,
                                            PATIENT_EDITOR_VIEW_REQUEST,
                                            PATIENT_CREATE_REQUEST,
                                            PATIENT_UPDATE_REQUEST,
                                            PATIENT_EXIT_VIEW_REQUEST}
    
    public enum PatientViewPropertyEvent {PATIENT_RECORDS_RECEIVED,
                                           PATIENT_RECORD_RECEIVED}

    public enum AppointmentViewControllerActionEvent {
                                        APPOINTMENT_FOR_PATIENT_RECORDS_REQUEST,
                                        APPOINTMENT_FOR_DAY_RECORDS_REQUEST}
    
    public enum AppointmentViewPropertyEvent {APPOINTMENT_RECORDS_RECEIVED,
                                               APPOINTMENT_RECORD_RECEIVED}

    
    public static final LocalTime FIRST_APPOINTMENT_SLOT = LocalTime.of(9,0);
  */
    public static final LocalTime LAST_APPOINTMENT_SLOT = LocalTime.of(17,0);
  /*  
    public static final int LAST_APPOINTMENT_DURATION = 30;
    
    public static final String APPOINTMENTS_VIEW_REQUEST = "Create new appointments view";

    
    public static final int IO_EXCEPTION = 1;
    public static final int CSV_EXCEPTION = 2;
    public static final int NULL_KEY_EXPECTED_EXCEPTION = 3;
    public static final int NULL_KEY_EXCEPTION = 4;
    public static final int INVALID_KEY_VALUE_EXCEPTION = 5;
    public static final int KEY_NOT_FOUND_EXCEPTION = 6;
    
    */
}
