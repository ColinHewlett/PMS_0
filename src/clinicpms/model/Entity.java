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
public class Entity implements IStoreClient{
    
    private Boolean isAppointment = false;
    private Boolean isAppointmentDate = false;
    private Boolean isPatient = false;
    private Boolean isPatientNotification = false;
    private Boolean isTableRowValue = false;
    private Boolean isPMSStore = false;
    private Boolean isSurgeryDaysAssignment = false;
    private Scope scope = null;
    private Integer value = null;
    
    private void resetAll(){
        setIsAppointment(false);
        setIsPatient(false);
        setIsPatientNotification(false);
        setIsPMSStore(false);
        setIsTableRowValue(false); 
        setIsSurgeryDaysAssignment(false);
    }
    
    /**
     * defines the scope of the Entity.read() operation
     */
    public enum Scope { ALL,
                        FOR_DAY,
                        FOR_PATIENT,
                        FROM_DAY,
                        SINGLE,
                        UNACTIONED,
                        }
    
    public Integer getValue(){
        return value;
    }
    
    public void setValue(Integer v){
        value = v;
    }
    
    public Scope getScope(){
        return scope;
    }
    
    public void setScope(Scope value){
        scope = value;
    }
    
    public Boolean getIsAppointment(){
        return isAppointment;
    }
    public Boolean getIsAppointmentDate(){
        return isAppointmentDate;
    }

    public Boolean getIsPatient(){
        return isPatient;
    }
    public Boolean getIsPatientNotification(){
        return isPatientNotification;
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

    protected void setIsPatient(Boolean value){
        if (value) resetAll();
        isPatient = value;
    }
    protected void setIsPatientNotification(Boolean value){
        if (value) resetAll();
        isPatientNotification = value;
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
