/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.model.ThePatient;
import clinicpms.model.PatientNotification;
import clinicpms.model.TheSurgeryDaysAssignment;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class EntityDescriptor {
    
    private ArrayList<PatientNotification> patientNotifications = null;
    private PatientNotification patientNotification;
    private EntityDescriptor.Appointment appointment = null;
    private EntityDescriptor.Patient patient = null;
    private ThePatient thePatient = null;
    private EntityDescriptor.Patient patientGuardian = null;
    private EntityDescriptor.PatientAppointmentHistory patientAppointmentHistory = null;
    private EntityDescriptor.Request request= null;
    private EntityDescriptor.Appointments appointments = null;
    private EntityDescriptor.Patients patients = null;
    //private clinicpms.model.Patients thePatients = null;
    private ArrayList<ThePatient> thePatients = null;
    private HashMap<DayOfWeek,Boolean> surgeryDaysAssignment = null;
    private EntityDescriptor.MigrationDescriptor migrationDescriptor = null;
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
                                            APPOINTMENT_CANCEL_COMPLETE,
                                            APPOINTMENTS_FOR_DAY_RECEIVED,
                                            APPOINTMENT_SLOTS_FROM_DAY_RECEIVED,
                                            APPOINTMENT_FOR_DAY_ERROR,
                                            SURGERY_DAYS_UPDATE_RECEIVED,
                                            RECEIVED_SURGERY_DAYS_ASSIGNMENT,
                                            NON_SURGERY_DAY_EDIT_RECEIVED
                                            }
    
    public static enum AppointmentViewControllerActionEvent {
                                            APPOINTMENT_CANCEL_REQUEST,/*of selected appt*/
                                            APPOINTMENT_CREATE_VIEW_REQUEST,
                                            APPOINTMENT_UPDATE_VIEW_REQUEST,/*of selected appt*/
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
                                            APPOINTMENT_RECEIVED,
                                            APPOINTMENT_VIEW_ERROR
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
        appointment = new EntityDescriptor.Appointment();
        patient = new EntityDescriptor.Patient();
        patientGuardian = new EntityDescriptor.Patient();
        patientAppointmentHistory = new EntityDescriptor.PatientAppointmentHistory();
        appointments = new EntityDescriptor.Appointments();     
        patients = new EntityDescriptor.Patients();  
        request = new EntityDescriptor.Request();
        migrationDescriptor = new EntityDescriptor.MigrationDescriptor();
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

    public EntityDescriptor.Appointment getAppointment() {
        return appointment;
    }
    
    protected void setAppointment(EntityDescriptor.Appointment value) {
        this.appointment = value;
    }

    public EntityDescriptor.Patient getPatient() {
        return patient;
    }
    
    protected void setPatient(EntityDescriptor.Patient value){
        patient = value;
    }
    
    /*
    update 30/07/2021 09:05
    public EntityDescriptor.Patient getPatientGuardian(){
        return patientGuardian;
    }
    
    protected void setPatientGuardian(EntityDescriptor.Patient value){
        patientGuardian = value;
    }
    */
    
    public EntityDescriptor.PatientAppointmentHistory getPatientAppointmentHistory(){
        return patientAppointmentHistory;
    }
    
    protected void setPatientAppointmentHistory(EntityDescriptor.PatientAppointmentHistory value){
        patientAppointmentHistory = value;
    }

    public EntityDescriptor.Request getRequest(){
        return request;
    }
    
    public EntityDescriptor.Appointments getAppointments(){
        return appointments;
    }
    
    public void setAppointments(EntityDescriptor.Appointments value){
        appointments = value;
    }
    
    public ThePatient getThePatient() {
        return thePatient;
    }
    
    protected void setThePatient(ThePatient value){
        thePatient = value;
    }
    
    public EntityDescriptor.Patients getPatients(){
        return patients;
    }
    
    public ArrayList<ThePatient> getThePatients(){
        return thePatients;
    }
    
    public void setPatients (EntityDescriptor.Patients value){
        patients = value;
    }
    
    public void setThePatients (ArrayList<ThePatient> value){
        thePatients = value;
    }
    
    public MigrationDescriptor getMigrationDescriptor(){
        return this.migrationDescriptor;
    }

    /**
     * EntityDescriptor.Appointment inner class
     */
    public class Appointment {
        private RenderedAppointment data = null;
        private EntityDescriptor.Patient appointee = null;
        
        protected Appointment(){
            data = new RenderedAppointment();
            appointee = new EntityDescriptor.Patient();
        }

        public RenderedAppointment getData() {
            return data;
        }

        public EntityDescriptor.Patient getAppointee(){
            return appointee;
        }
        
        protected void setData(RenderedAppointment value) {
            data = value;
        }

        protected void setAppointee(EntityDescriptor.Patient value){
            appointee = value;
        }
        
        @Override
        public String toString(){
            DateTimeFormatter customFormatter = 
                    DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a");
            LocalDateTime startDateTime = getData().getStart();
                
            return customFormatter.format(startDateTime);
        }
        
        @Override
        public boolean equals(Object obj) 
        { 
            // if both the object references are  
            // referring to the same object. 
            if(this == obj) 
                    return true; 

            // checks if the comparison involves 2 objecs of the same type 
            if(obj == null || obj.getClass()!= this.getClass()) 
                return false; 

            // type casting of the argument.  
            EntityDescriptor.Appointment appointment = (EntityDescriptor.Appointment) obj;

            // comparing the state of argument with  
            // the state of 'this' Object. 
            return (appointment.getData().getKey().equals(this.getData().getKey())); 
        } 

        @Override
        public int hashCode() 
        { 
            // the patient.key() value is returned as this object's hashcode 
            if (this.getData().getKey()!=null){
                return this.getData().getKey();
            }
            else{
                return -1;
            }
        }
    }

    public class Patient {
        private RenderedPatient data = null;
        private EntityDescriptor.Patient patientGuardian = null;

        protected Patient() {
            data = new RenderedPatient();
            //patientGuardian = new EntityDescriptor.Patient();
        }
        
        protected void setData(RenderedPatient value) {
            data = value;
        }

        public RenderedPatient getData() {
            return data;
        }
        
        public EntityDescriptor.Patient getPatientGuardian(){
            return patientGuardian;
        }

        protected void setPatientGuardian(EntityDescriptor.Patient value){
            patientGuardian = value;
        }
        
        private String capitaliseFirstLetter(String value, String delimiter){
            String result = null;
            String part1 = null;
            String part2 = null;
            //value = value.strip();
            if (!delimiter.equals("")){
                /*
                if (getData().getKey()==20615){
                    String test = "";
                    test = "\\s+";
                }
*/

                String[] values = value.split(delimiter); 
                part1 = capitalisePart(values[0]);
                if (values.length>1) part2 = capitalisePart(values[1]);
            }
            else part1 = capitalisePart(value);
            
            if (part2 == null) result = part1;
            else {
                if (delimiter.equals("\\s+")){
                    result = part1 + " " + part2;
                }
                else result = part1 + delimiter + part2;
            }
            return result;
        }
        private String capitalisePart(String part){
            if (part.length() == 0){
                part = part + part;
            }
            String firstLetter =  null;
            String otherLetters = null;
            firstLetter = part.substring(0,1).toUpperCase();
            otherLetters = part.substring(1).toLowerCase();
            return firstLetter + otherLetters;
        }
        
        @Override
        /**
         * re-defines default format patient name display
         * -- basically: "surname, forename"
         * -- first letter of surname and any subsequent part is capitalised
         * -- first letter of forename and any subsequent part is capitalised 
         * 
         */
        public String toString(){
            String cappedName = null;
            if (getData().getSurname().length()>0){
                //if (getData().getSurname().strip().contains("-")) 
                if (getData().getSurname().contains("-"))
                    cappedName = capitaliseFirstLetter(getData().getSurname(), "-");
                //else if (getData().getSurname().strip().contains(" "))
                else if (getData().getSurname().contains(" "))
                    cappedName = capitaliseFirstLetter(getData().getSurname(), "\\s+");
                else
                    cappedName = capitaliseFirstLetter(getData().getSurname(), "");
            }
            if (getData().getForenames().length()>0){
                if (cappedName!=null){
                    //if (getData().getForenames().strip().contains("-")) 
                    if (getData().getForenames().contains("-"))
                        cappedName = cappedName + ", " + capitaliseFirstLetter(getData().getForenames(), "-");
                    //else if (getData().getForenames().strip().contains(" ")) 
                    else if (getData().getForenames().contains(" "))
                        cappedName = cappedName + ", " + capitaliseFirstLetter(getData().getForenames(), "\\s+");
                    else cappedName = cappedName + ", " + capitaliseFirstLetter(getData().getForenames(), "");
                }
                else{
                    //if (getData().getForenames().strip().contains("-")) 
                    if (getData().getForenames().contains("-")) 
                        cappedName = ", " + capitaliseFirstLetter(getData().getForenames(), "-");
                    //else if (getData().getForenames().strip().contains(" ")) 
                    else if (getData().getForenames().contains(" "))
                        cappedName = ", " + capitaliseFirstLetter(getData().getForenames(), "\\s+");
                    else cappedName = ", " + capitaliseFirstLetter(getData().getForenames(), "");
                }
            }
            if (getData().getTitle().length()>0){
                if (cappedName!=null)
                    cappedName = cappedName + " (" + capitaliseFirstLetter(getData().getTitle(), "") + ")";
                else cappedName = "(" + capitaliseFirstLetter(getData().getTitle(), "") + ")";
            }
            return cappedName;
        }
        
        @Override
        public boolean equals(Object obj) 
        { 
            // if both the object references are  
            // referring to the same object. 
            if(this == obj) 
                return true; 

            // checks if the comparison involves 2 objecs of the same type 
            if(obj == null || obj.getClass()!= this.getClass()) 
                return false; 

            // type casting of the argument.  
            EntityDescriptor.Patient patient = (EntityDescriptor.Patient) obj; 

            // comparing the state of argument with  
            // the state of 'this' Object. 
            return (patient.getData().getKey().equals(this.getData().getKey())); 
        } 

        @Override
        public int hashCode() 
        { 
            // the patient.key() value is returned as this object's hashcode 
            if (this.getData().getKey()!=null){
                return this.getData().getKey();
            }
            else{
                return -1;
            }
        } 

    }
        
    public class PatientGuardian{
        RenderedPatient data = null;
        
        protected PatientGuardian() {
            data = new RenderedPatient();
        }
        
        protected void setData(RenderedPatient value) {
            data = value;
        }

        public RenderedPatient getData() {
            return data;
        }
        
        @Override
        public String toString(){
            String name = null;
            if (getData().getTitle() != null){
            name = name + getData().getTitle();
        }
        if (getData().getForenames() != null){
            if (name!=null){
                name = name + " " + getData().getForenames();
            }
            else{
                name = getData().getForenames();
            }
        }
        if (getData().getSurname()!=null){
            if (name!=null){
                name = name + " " + getData().getSurname();
            }
            else {
                name = getData().getSurname();
            }
        }
        return name;
        }
        
        @Override
        public boolean equals(Object obj) 
        { 
            // if both the object references are  
            // referring to the same object. 
            if(this == obj) 
                return true; 

            // checks if the comparison involves 2 objecs of the same type 
            if(obj == null || obj.getClass()!= this.getClass()) 
                return false; 

            // type casting of the argument.  
            EntityDescriptor.PatientGuardian patient = (EntityDescriptor.PatientGuardian) obj; 

            // comparing the state of argument with  
            // the state of 'this' Object. 
            return (patient.getData().getKey().equals(this.getData().getKey())); 
        } 

        @Override
        public int hashCode() 
        { 
            // the patient.key() value is returned as this object's hashcode 
            if (this.getData().getKey()!=null){
                return this.getData().getKey();
            }
            else{
                return -1;
            }
        }
    }

    public class PatientAppointmentHistory{
        private ArrayList<EntityDescriptor.Appointment> dentalAppointments = null;
        private ArrayList<EntityDescriptor.Appointment> hygieneAppointments = null;
        
        protected PatientAppointmentHistory(){
            dentalAppointments = new ArrayList<>();
            hygieneAppointments = new ArrayList<>();
        }
        
        public ArrayList<EntityDescriptor.Appointment> getDentalAppointments(){
            return dentalAppointments;
        }
        
        public ArrayList<EntityDescriptor.Appointment> getHygieneAppointments(){
            return hygieneAppointments;
        }
        
        protected void setDentalAppointments(ArrayList<EntityDescriptor.Appointment> value){
            dentalAppointments = value;
        }
        
        protected void setHygieneAppointments(ArrayList<EntityDescriptor.Appointment> value){
            hygieneAppointments = value;
        }
    } 

    public class Appointments{
        private ArrayList<EntityDescriptor.Appointment> data = null;
        
        public Appointments(){
            data = new ArrayList<>();
        }
        
        public ArrayList<EntityDescriptor.Appointment> getData(){
            return data;
        }
        
        public void setData(ArrayList<EntityDescriptor.Appointment> value){
            data = value;
        } 
    }
    
    public class Patients{
        private ArrayList<EntityDescriptor.Patient> data = null;
        
        public Patients(){
            data = new ArrayList<>();
        }
        
        public ArrayList<EntityDescriptor.Patient> getData(){
            return data;
        }
        
        public void setData(ArrayList<EntityDescriptor.Patient> value){
            data = value;
        } 
    }
    public class Request {
        
        private EntityDescriptor.Patient patient = null;
        private EntityDescriptor.Appointment appointment = null;
        private EntityDescriptor.Patient guardian = null;
        private LocalDate day = null;
        private Duration duration = null;
        private String databaseLocation = null;
        private ArrayList<PatientNotification> patientNotifications = null;
        private PatientNotification patientNotification = null;
        private TheSurgeryDaysAssignment surgeryDaysAssignment = null;
        
        private HashMap<DayOfWeek,Boolean> surgeryDaysAssignmentValue = null;


        protected Request() {
            appointment = new EntityDescriptor.Appointment();
            patient = new EntityDescriptor.Patient();
            guardian = new EntityDescriptor.Patient();
            day = LocalDate.now();
            duration = Duration.ZERO; 
            HashMap<DayOfWeek,Boolean> surgeryDaysAssignmentValue = new HashMap<>();
            
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
        
        public EntityDescriptor.Patient getPatient() {
            return patient;
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
        
        public void setPatient(EntityDescriptor.Patient value){
            patient = value;
        }
        
        public EntityDescriptor.Appointment getAppointment() {
            return appointment;
        }
        
        public void setAppointment(EntityDescriptor.Appointment value){
            appointment = value;
        }
        
        public EntityDescriptor.Patient getPatientGuardian() {
            return guardian;
        }
        
        public void setGuardian(EntityDescriptor.Patient value){
            guardian = value;
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
    
    public class MigrationDescriptor {
        private MigrationDescriptor.Appointment appointment = null;
        private MigrationDescriptor.Patient patient = null;
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

        public MigrationDescriptor(){
            appointment = new Appointment();
            patient = new Patient();
            target = new Target();
        }
        
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

        public Appointment getAppointment(){
            return appointment;
        }

        public Patient getPatient(){
            return patient;
        }

        public Target getTarget(){
            return target;
        }

        public void setTarget(Target value){
            target = value;
        }

        public class Appointment{
            private String data = null;
            public String getData(){
                return data;
            }
            public void setData(String value){
                data = value;
            }
        }

        public class Patient{
            private String data = null;
            public String getData(){
                return data;
            }
            public void setData(String value){
                data = value;
            }
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

        public class Appointments{
            private ArrayList<clinicpms.model.Appointment> appointments = null;

            public ArrayList<clinicpms.model.Appointment> getData(){
                return appointments;
            } 
        }

        public class Patients{
            private ArrayList<clinicpms.model.Patient> patients = null;

            public ArrayList<clinicpms.model.Patient> getData(){
                return patients;
            }
        }
    }
}