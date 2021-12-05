/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

/**
 *
 * @author colin
 */
public class EntityDescriptor {
    private EntityDescriptor.Appointment appointment = null;
    private EntityDescriptor.Patient patient = null;
    private EntityDescriptor.Patient patientGuardian = null;
    private EntityDescriptor.PatientAppointmentHistory patientAppointmentHistory = null;
    private EntityDescriptor.Request request= null;
    private EntityDescriptor.Appointments appointments = null;
    private EntityDescriptor.Patients patients = null;
    private EntityDescriptor.MigrationDescriptor migrationDescriptor = null;
    private String error = null;

    protected EntityDescriptor() {
        appointment = new EntityDescriptor.Appointment();
        patient = new EntityDescriptor.Patient();
        patientGuardian = new EntityDescriptor.Patient();
        patientAppointmentHistory = new EntityDescriptor.PatientAppointmentHistory();
        appointments = new EntityDescriptor.Appointments();     
        patients = new EntityDescriptor.Patients();  
        request = new EntityDescriptor.Request();
        migrationDescriptor = new EntityDescriptor.MigrationDescriptor();
        error = null;
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
    
    public EntityDescriptor.Patients getPatients(){
        return patients;
    }
    
    public void setPatients (EntityDescriptor.Patients value){
        patients = value;
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
        private HashMap<DayOfWeek,Boolean> surgeryDays = null;
        

        protected Request() {
            appointment = new EntityDescriptor.Appointment();
            patient = new EntityDescriptor.Patient();
            guardian = new EntityDescriptor.Patient();
            day = LocalDate.now();
            duration = Duration.ZERO;   
        }
        
        public EntityDescriptor.Patient getPatient() {
            return patient;
        }
        
        public void setSurgeryDays(HashMap<DayOfWeek,Boolean> value){
            surgeryDays = value;
        }
        
        public HashMap<DayOfWeek,Boolean> getSurgeryDays(){
            return surgeryDays;
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
        private ViewController.MigrationViewRequest migrationViewRequest = null;
        private Duration durationOfMigrationAction = null;

        public MigrationDescriptor(){
            appointment = new Appointment();
            patient = new Patient();
            target = new Target();
        }

        public ViewController.MigrationViewRequest getMigrationViewRequest(){
            return migrationViewRequest;
        }

        public void setMigrationViewRequest(ViewController.MigrationViewRequest  value){
            migrationViewRequest = value;
        }

        public Duration getMigrationActionDuration(){
            return durationOfMigrationAction;
        }
        protected void setMigrationActionDuration(Duration value){
            durationOfMigrationAction = value;
        }

        public  Integer  getAppointmentsCount(){
            return appointmentsCount;
        }

        public void setAppointmentsCount(Integer value){
            appointmentsCount = value;
        }

        public Integer getPatientsCount(){
            return patientsCount;
        }

        public void setPatientsCount(Integer value){
            patientsCount = value;
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