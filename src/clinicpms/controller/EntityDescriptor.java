/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.PatientNotification;
import clinicpms.model.SurgeryDaysAssignment;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class EntityDescriptor {
    
     
    private Appointment theAppointment = null;
    private PatientNotification patientNotification;
    private Patient thePatient = null;
    
    private ArrayList<Appointment> theAppointments = null;
    private ArrayList<Patient> thePatients = null;
    private ArrayList<PatientNotification> patientNotifications = null;
    
    
    private HashMap<DayOfWeek,Boolean> surgeryDaysAssignment = null;
    private EntityDescriptor.Request request= null;

    private String error = null;
    private Integer tableRowCount = null;
    private String appointmentCSVPath = null;
    private String patientCSVPath = null;
    private String pmsStorePath = null;
    
    public static enum AppointmentField {ID,
                                KEY,
                                APPOINTMENT_PATIENT,
                                START,
                                DURATION,
                                NOTES}
    
    public static enum AppointmentViewControllerPropertyEvent {
                                            //APPOINTMENT_CANCEL_COMPLETE,
                                            APPOINTMENTS_FOR_DAY_RECEIVED,
                                            APPOINTMENT_SLOTS_FROM_DAY_RECEIVED,
                                            //APPOINTMENT_FOR_DAY_ERROR,
                                            APPOINTMENT_SCHEDULE_ERROR_RECEIVED,
                                            //SURGERY_DAYS_UPDATE_RECEIVED,
                                            SURGERY_DAYS_ASSIGNMENT_RECEIVED,
                                            NON_SURGERY_DAY_EDIT_RECEIVED
                                            }
    
    public static enum AppointmentViewControllerActionEvent {
                                            APPOINTMENT_CANCEL_REQUEST,/*of selected appt*/
                                            APPOINTMENT_CREATE_VIEW_REQUEST,
                                              APPOINTMENT_CREATE_REQUEST,
                                            APPOINTMENT_UPDATE_VIEW_REQUEST,/*of selected appt*/
                                              APPOINTMENT_UPDATE_REQUEST,
                                            APPOINTMENTS_VIEW_CLOSED,
                                            APPOINTMENTS_FOR_DAY_REQUEST,/*triggered by day selection*/
                                            APPOINTMENT_SLOTS_FROM_DATE_REQUEST,
                                            EMPTY_SLOT_SCANNER_DIALOG_REQUEST,
                                            MODAL_VIEWER_ACTIVATED,
                                            MODAL_VIEWER_CLOSED,
                                            PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST,
                                            NON_SURGERY_DAY_SCHEDULE_VIEW_REQUEST,
                                            NON_SURGERY_DAY_SCHEDULE_EDIT_REQUEST,
                                            SURGERY_DAYS_EDIT_REQUEST,
                                            SURGERY_DAYS_EDITOR_VIEW_REQUEST,
                                            REQUEST_SURGERY_DAYS_ASSIGNMENT
                                            }
    
    public static enum AppointmentViewDialogActionEvent {
                                            APPOINTMENT_VIEW_CLOSE_REQUEST,
                                            APPOINTMENT_VIEW_CREATE_REQUEST,
                                            APPOINTMENT_VIEW_UPDATE_REQUEST,
                                            }
    public static enum AppointmentViewDialogPropertyEvent {
                                            //APPOINTMENT_RECEIVED,
                                            //APPOINTMENT_VIEW_ERROR
                                            }
    
    public static enum PatientField {
                              KEY,
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
                              IS_GUARDIAN_A_PATIENT,
                              GUARDIAN,
                              NOTES,
                              DENTAL_RECALL_DATE,
                              HYGIENE_RECALL_DATE,
                              DENTAL_RECALL_FREQUENCY,
                              HYGIENE_RECALL_FREQUENCY,
                              DENTAL_APPOINTMENT_HISTORY,
                              HYGIENE_APPOINTMENT_HISTORY}
    
    public static enum ImportExportProgressViewControllerActionEvent{
                                IMPORT_EXPORT_START_REQUEST,
                                IMPORT_START_REQUEST,
                                READY_FOR_RECEIPT_OF_APPOINTMENT_PROGRESS,
                                READY_FOR_RECEIPT_OF_PATIENT_PROGRESS,
                                IMPORT_EXPORT_SURGERY_DAYS_ASSIGNMENT,
                                IMPORT_EXPORT_PROGRESS_CLOSE_NOTIFICATION}
    
    public static enum ImportExportProgressViewControllerPropertyChangeEvent{
                                progress,
                                state,
                                PREPARE_FOR_EXPORT_OPERATION,
                                PREPARE_FOR_IMPORT_OPERATION,
                                PREPARE_FOR_RECEIPT_OF_PATIENT_PROGRESS,
                                PREPARE_FOR_RECEIPT_OF_APPOINTMENT_PROGRESS,
                                OPERATION_COMPLETED}
    public static enum PatientNotificationViewControllerActionEvent{
                                            ACTION_PATIENT_NOTIFICATION_REQUEST,
                                            CREATE_PATIENT_NOTIFICATION_REQUEST,
                                            UPDATE_PATIENT_NOTIFICATION_REQUEST,
                                            UNACTIONED_PATIENT_NOTIFICATIONS_REQUEST,
                                            PATIENT_NOTIFICATIONS_REQUEST,
                                            PATIENT_NOTIFICATION_EDITOR_CLOSE_VIEW_REQUEST,
                                            PATIENT_NOTIFICATION_EDITOR_CREATE_NOTIFICATION_REQUEST,
                                            PATIENT_NOTIFICATION_EDITOR_UPDATE_NOTIFICATION_REQUEST,
                                            MODAL_VIEWER_ACTIVATED,
                                            MODAL_VIEWER_DEACTIVATED
                                            }   
    public static enum PatientNotificationViewControllerPropertyChangeEvent{
                                            RECEIVED_ALL_PATIENT_NOTIFICATIONS,
                                            RECEIVED_UNACTIONED_NOTIFICATIONS,
                                            RECEIVED_PATIENT_NOTIFICATION,
                                            RECEIVED_PATIENT_NOTIFICATIONS,
                                            RECEIVED_PATIENTS
                                            }                                     
    public static enum PatientViewControllerActionEvent {
                                            MODAL_VIEWER_ACTIVATED,
                                            NULL_PATIENT_REQUEST,
                                            PATIENT_REQUEST,
                                            PATIENTS_REQUEST,
                                            PATIENT_VIEW_CLOSED,
                                            PATIENT_VIEW_CREATE_REQUEST,
                                            PATIENT_VIEW_UPDATE_REQUEST,
                                            PATIENT_GUARDIAN_REQUEST,
                                            PATIENT_GUARDIANS_REQUEST,
                                            APPOINTMENT_VIEW_CONTROLLER_REQUEST
                                            }
    public static enum PatientViewControllerPropertyEvent {
                                            NULL_PATIENT_RECEIVED,
                                            PATIENT_RECEIVED,
                                            PATIENTS_RECEIVED,
                                            PATIENT_GUARDIANS_RECEIVED}
    
    public enum MigrationViewRequest{   POPULATE_APPOINTMENT_TABLE,
                                        COUNT_APPOINTMENT_TABLE,
                                        POPULATE_PATIENT_TABLE,
                                        COUNT_PATIENT_TABLE,
                                        REMOVE_BAD_APPOINTMENTS_FROM_DATABASE,
                                        TIDY_PATIENT_DATA_IN_DATABASE,
                                        APPOINTMENT_TABLE_INTEGITY_CHECK}
    
    public enum MigrationViewPropertyChangeEvents{MIGRATION_ACTION_COMPLETE}

    public enum MigratorViewControllerActionEvent{  APPOINTMENT_MIGRATOR_REQUEST, 
                                                    PATIENT_MIGRATOR_REQUEST,
                                                    EXPORT_MIGRATED_DATA_TO_PMS_REQUEST};

    protected EntityDescriptor() {  
        request = new EntityDescriptor.Request();
        patientNotifications = new ArrayList<PatientNotification>();
        surgeryDaysAssignment = new HashMap<DayOfWeek,Boolean>(); 
        error = null;
    }
    
    public String getAppointmentCSVPath(){
        return appointmentCSVPath;
    }
    
    public void setAppointmentCSVPath(String value){
        appointmentCSVPath = value;
    }
    
    public String getPatientCSVPath(){
        return patientCSVPath;
    }
    
    public void setPatientCSVPath(String value){
        patientCSVPath = value;
    }
    
    public String getPMSStorePath(){
        return pmsStorePath;
    }
    
    public void setPMSStorePath(String value){
        pmsStorePath = value;
    }
    
    public Integer getTableRowCount(){
        return tableRowCount;
    }
    
    public void setTableRowCount(Integer value){
        tableRowCount = value;
    }
    
    public HashMap<DayOfWeek,Boolean> getSurgeryDaysAssignment(){
        return surgeryDaysAssignment;
    }
    
    public void setSurgeryDaysAssignment(HashMap<DayOfWeek,Boolean> value){
        surgeryDaysAssignment = value;
    }

    public PatientNotification getPatientNotification(){
        return patientNotification;
    }
    
    public void setPatientNotification(PatientNotification value){
        this.patientNotification = value;
    }
    
    public ArrayList<PatientNotification> getPatientNotifications(){
        return patientNotifications;
    }
    
    public void setPatientNotifications(ArrayList<PatientNotification> patientNotifications){
        this.patientNotifications = patientNotifications;
    }
    
    public String getError(){
        return error;
    }
    
    protected void setError(String message){
        error = message;
    }
    
    public Appointment getAppointment() {
        return theAppointment;
    }
    
    protected void setAppointment(Appointment value) {
        theAppointment = value;
    }

    public EntityDescriptor.Request getRequest(){
        return request;
    }
    
    public ArrayList<Appointment> getAppointments(){
        return theAppointments;
    }
    
    public void setAppointments(ArrayList<Appointment> value){
        theAppointments = value;
    }
    
    public Patient getPatient() {
        return thePatient;
    }
    
   
    protected void setPatient(Patient value){
        thePatient = value;
    }
    
    /*
    
    public EntityDescriptor.Patients getPatients(){
        return patients;
    }
    */
    public ArrayList<Patient> getPatients(){
        return thePatients;
    }
    
    /*
    public void setPatients (EntityDescriptor.Patients value){
        patients = value;
    }
    */
    public void setPatients (ArrayList<Patient> value){
        thePatients = value;
    }
    
    
    public class Request {
        private Patient thePatient = null;
        private Patient theGuardian = null;
        private LocalDate day = null;
        private Duration duration = null;
        private String databaseLocation = null;
        private ArrayList<PatientNotification> patientNotifications = null;
        private PatientNotification patientNotification = null;
        private SurgeryDaysAssignment surgeryDaysAssignment = null;
        
        private HashMap<DayOfWeek,Boolean> surgeryDaysAssignmentValue = null;


        protected Request() {
            theAppointment = new Appointment();
            thePatient = new Patient();
            theGuardian = new Patient();
            day = LocalDate.now();
            duration = Duration.ZERO; 
            HashMap<DayOfWeek,Boolean> surgeryDaysAssignmentValue = new HashMap<>();
            
        }
        
        public Patient getTheGuardian(){
            return theGuardian;
        }
        
        public void setTheGuardian(Patient patient){
            theGuardian = patient;
        }
        
        public Appointment getAppointment(){
            return theAppointment;
        }
        
        public void setAppointment(Appointment value){
            theAppointment = value;
        }
        
        public Patient getPatient(){
            return thePatient;
        }
        
        public void setPatient(Patient patient){
            thePatient = patient;
        }
        
        public PatientNotification getPatientNotification(){
            return patientNotification;
        }
        
        public void setPatientNotification(PatientNotification value){
            patientNotification = value;
        }
        
        public ArrayList<PatientNotification> getPatientNotifications(){
            return patientNotifications;
        }
        
        public void setPatientNotifications(ArrayList<PatientNotification> value){
            patientNotifications = value;
        }

        public void setSurgeryDaysAssignmentValue(HashMap<DayOfWeek,Boolean> value){
            surgeryDaysAssignmentValue = value;
        }
        
        public HashMap<DayOfWeek,Boolean> getSurgeryDaysAssignmentValue(){
            return surgeryDaysAssignmentValue;
        }
        
        public void setDatabaseLocation(String value){
            databaseLocation = value;
        }
        
        public String getDatabaseLocation(){
            return databaseLocation;
        }

        public LocalDate getDay(){
            return day;
        }
        
        public Duration getDuration(){
            return duration;
        }
        
        public void setDuration(Duration value){
            duration = value;
        }
        
        public void setDay(LocalDate value){
            this.day = value;
        }
    }
    
    public class MigrationDescriptorx {
        private Target target = null;
        private Integer appointmentsCount = null;
        private Integer patientsCount = null;
        private Integer appointmentsTableCount = null;
        private Integer surgeryDaysAssignmentCount = null;
        private Integer surgeryDaysAssignmentTableCount = null;
        private Integer patientsTableCount = null;
        private MigrationViewRequest migrationViewRequest = null;
        private Duration durationOfMigrationAction = null;
        private String appointmentCSVFilePath = null;
        private String patientCSVFilePath = null;
        private String migrationDatabaseSelection = null;
        private String PMSDatabaseSelection = null;
        private boolean importOperationStatus = false;
        private boolean exportOperationStatus = true;

        public void setImportOperationStatus(boolean value){
            this.importOperationStatus = value;
            this.exportOperationStatus = !value;
        }
        
        public void setExportOperationStatus(boolean value){
            this.exportOperationStatus = value;
            this.importOperationStatus = !value;
        }
        
        public boolean getImportOperationStatus(){
            return importOperationStatus;
        }
        
        public boolean getExportOperationStatus(){
            return exportOperationStatus;
        }
        
        public String getAppointmentCSVFilePath(){
            return appointmentCSVFilePath;
        }
        
        public String getPatientCSVFilePath(){
            return patientCSVFilePath;
        }
        
        public String getMigrationDatabaseSelection(){
            return migrationDatabaseSelection;
        }
        
        public void setMigrationDatabaseSelection(String value){
            migrationDatabaseSelection = value;
        }
        
        public String getPMSDatabaseSelection(){
            return PMSDatabaseSelection;
        }
        
        public void setPMSDatabaseSelection(String value){
            PMSDatabaseSelection = value;
        }
        
        public void setAppointmentCSVFilePath(String value){
            appointmentCSVFilePath = value;
        }
        
        public void setPatientCSVFilePath(String value){
            patientCSVFilePath = value;
        }
        
        
        

        public MigrationViewRequest getMigrationViewRequest(){
            return migrationViewRequest;
        }

        public void setMigrationViewRequest(MigrationViewRequest  value){
            migrationViewRequest = value;
        }

        public Duration getMigrationActionDuration(){
            return durationOfMigrationAction;
        }
        protected void setMigrationActionDuration(Duration value){
            durationOfMigrationAction = value;
        }

        public Integer getSurgeryDaysAssignmentTableCount(){
            return surgeryDaysAssignmentTableCount;
        }
        
        public void setSurgeryDaysAssignmentTableCount(Integer value){
            surgeryDaysAssignmentTableCount = value;
        }
        
        public  Integer  getAppointmentTableCount(){
            return appointmentsTableCount;
        }

        protected void setAppointmentTableCount(Integer value){
            appointmentsTableCount = value;
        }
        
        public  Integer  getAppointmentsCount(){
            return appointmentsCount;
        }

        protected void setAppointmentsCount(Integer value){
            appointmentsCount = value;
        }

        public Integer getPatientTableCount(){
            return patientsTableCount;
        }

        protected void setPatientTableCount(Integer value){
            patientsTableCount = value;
        }
        
        public Integer getPatientsCount(){
            return patientsCount;
        }

        protected void setPatientsCount(Integer value){
            patientsCount = value;
        }
        
        public Integer getSurgeryDaysAssignmentCount(){
            return surgeryDaysAssignmentCount;
        }

        protected void setSurgeryDaysAssignmentCount(Integer value){
            surgeryDaysAssignmentCount = value;
        }

        public Target getTarget(){
            return target;
        }

        public void setTarget(Target value){
            target = value;
        }
        
        public class Target{
            private String data = null;
            public String getData(){
                return data;
            }
            public void setData(String value){
                data = value;
            }
        }  
    }
}