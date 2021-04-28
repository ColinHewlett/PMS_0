/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import java.time.Duration;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
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
