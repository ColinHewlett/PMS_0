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
    private Boolean isPatientNotifications = false;
    //private Boolean isPatientNotificationCollection = false;
    private Boolean isPatients = false;
    private Boolean isPatientTable = false;
    private Boolean isTableRowValue = false;
    private Boolean isPMSStore = false;
    private Boolean isSurgeryDaysAssignment = false;
    
    private void resetAll(){
        setIsAppointment(false);
        setIsAppointmentDate(false);
        setIsAppointments(false);
        setIsAppointmentTable(false);
        setIsAppointmentTableRowValue(false);
        setIsPatient(false);
        setIsPatientNotification(false);
        setIsPatientNotifications(false);
        setIsPatients(false);
        setIsPMSStore(false);
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
    public Boolean getIsPatientNotifications(){
        return isPatientNotifications;
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
    public final Boolean getIsPMSStore(){
        return isPMSStore;
    }
    public Boolean getIsSurgeryDaysAssignment(){
        return isSurgeryDaysAssignment;
    }  
    
    protected void setIsAppointment(Boolean value){
        if (value) resetAll();
        isAppointment= value;
    }
    protected void setIsAppointmentDate(Boolean value){
        if (value) resetAll();
        isAppointmentDate = value;
    }
    protected void setIsAppointments(Boolean value){
        if (value) resetAll();
        isAppointments = value;
    }
    protected void setIsAppointmentTable(Boolean value){
        if (value) resetAll();
        isAppointmentTable = value;
    }
    protected void setIsAppointmentTableRowValue(Boolean value){
        if (value) resetAll();
        isAppointmentTableRowValue = value;
    }
    protected void setIsPatient(Boolean value){
        if (value) resetAll();
        isPatient = value;
    }
    protected void setIsPatientNotification(Boolean value){
        if (value) resetAll();
        isPatientNotification = value;
    }
    protected void setIsPatientNotifications(Boolean value){
        if (value) resetAll();
        isPatientNotifications = value;
    }
    protected void setIsPatients(Boolean value){
        if (value) resetAll();
        isPatients = value;
    }
    protected void setIsPatientTable(Boolean value){
        if (value) resetAll();
        isPatientTable = value;
    }
    protected final void setIsTableRowValue(Boolean value){
        if (value) resetAll();
        isTableRowValue = value;
    }
    protected final void setIsPMSStore(Boolean value){
        if (value) resetAll();
        isPMSStore = value;
    }
    protected void setIsSurgeryDaysAssignment(Boolean value){
        if (value) resetAll();
        isSurgeryDaysAssignment = value;
    } 
}
