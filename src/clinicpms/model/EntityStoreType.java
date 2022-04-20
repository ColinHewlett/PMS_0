/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import  java.util.ArrayList;
/**
 *
 * @author colin
 */
public class EntityStoreType{
    
    private Boolean isAppointment = false;
    private Boolean isAppointmentDate = false;
    private Boolean isAppointments = false;
    private Boolean isAppointmentTable = false;
    private Boolean isAppointmentTableRowValue = false;
    private Boolean isPatient = false;
    private Boolean isPatientNotification = false;
    //private Boolean isPatientNotifications = false;
    private Boolean isPatientNotificationCollection = false;
    private Boolean isPatients = false;
    private Boolean isPatientTable = false;
    private Boolean isTableRowValue = false;
    private Boolean isSurgeryDaysAssignment = false;
    
    private void resetAll(){
        setIsAppointment(false);
        setIsAppointmentDate(false);
        setIsAppointments(false);
        setIsAppointmentTable(false);
        setIsAppointmentTableRowValue(false);
        setIsPatient(false);
        setIsPatientNotification(false);
        setIsPatientNotificationCollection(false);
        setIsPatients(false);
        setIsPatientTable(false);
        setIsTableRowValue(false);
        setIsSurgeryDaysAssignment(false);
    }
    
    public Boolean getIsAppointment(){
        return isAppointment;
    }
    public Boolean getIsAppointmentDate(){
        return isAppointmentDate;
    }
    public Boolean getIsAppointments(){
        return isAppointments;
    }
    public Boolean getIsAppointmentTable(){
        return isAppointmentTable;
    }
    public Boolean getIsAppointmentTableRowValue(){
        return isAppointmentTableRowValue;
    }
    public Boolean getIsPatient(){
        return isPatient;
    }
    public Boolean getIsPatientNotification(){
        return isPatientNotification;
    }
    public Boolean getIsPatientNotificationCollection(){
        return isPatientNotificationCollection;
    }
    public Boolean getIsPatients(){
        return isPatients;
    }
    public Boolean getIsPatientTable(){
        return isPatientTable;
    }
    public final Boolean getIsTableRowValue(){
        return isTableRowValue;
    }
    public Boolean getIsSurgeryDaysAssignment(){
        return isSurgeryDaysAssignment;
    }  
    
    public void setIsAppointment(Boolean value){
        if (value) resetAll();
        isAppointment= value;
    }
    public void setIsAppointmentDate(Boolean value){
        if (value) resetAll();
        isAppointmentDate = value;
    }
    public void setIsAppointments(Boolean value){
        if (value) resetAll();
        isAppointments = value;
    }
    public void setIsAppointmentTable(Boolean value){
        if (value) resetAll();
        isAppointmentTable = value;
    }
    public void setIsAppointmentTableRowValue(Boolean value){
        if (value) resetAll();
        isAppointmentTableRowValue = value;
    }
    public void setIsPatient(Boolean value){
        if (value) resetAll();
        isPatient = value;
    }
    public void setIsPatientNotification(Boolean value){
        if (value) resetAll();
        isPatientNotification = value;
    }
    public void setIsPatientNotificationCollection(Boolean value){
        if (value) resetAll();
        isPatientNotificationCollection = value;
    }
    public void setIsPatients(Boolean value){
        if (value) resetAll();
        isPatients = value;
    }
    public void setIsPatientTable(Boolean value){
        if (value) resetAll();
        isPatientTable = value;
    }
    public final void setIsTableRowValue(Boolean value){
        if (value) resetAll();
        isTableRowValue = value;
    }
    public void setIsSurgeryDaysAssignment(Boolean value){
        if (value) resetAll();
        isSurgeryDaysAssignment = value;
    } 
    
    //public abstract ArrayList<?> getCollection();
    
    //public abstract void setCollection(ArrayList<?> value );
}
