/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

/**
 *
 * @author colin
 */
public class AppointmentTableRowValue implements IEntityStoreType {
    private Integer value = null;
    
    public Integer getValue(){
        return this.value;
    }
    
    public AppointmentTableRowValue(Integer value){
        this.value = value;
    }

    @Override
    public final boolean isAppointment(){
        return false;
    }
    
    @Override
    public final boolean isAppointments(){
        return false;
    }
    
    @Override
    public final boolean isAppointmentTableRowValue(){
        return true;
    }
    
    @Override
    public final boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public final boolean isPatient(){
        return false;
    }
    
    @Override
    public final boolean isPatients(){
        return false;
    }
    
    @Override
    public final boolean isPatientTableRowValue(){
        return false;
    }
    
    @Override
    public final boolean isSurgeryDaysAssignment(){
        return false;
    }
}