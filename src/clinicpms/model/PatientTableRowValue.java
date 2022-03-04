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
public class PatientTableRowValue implements IEntityStoreType {
    private Integer value = null;
    
    public PatientTableRowValue(int value){
        this.value = value;
    }
    
    public Integer getValue(){
        return this.value;
    }
    
    @Override
    public boolean isAppointment(){
        return true;
    }
    
    @Override
    public boolean isAppointments(){
        return false;
    }
    
    @Override
    public boolean isAppointmentTable(){
        return false;
    }
    
    @Override
    public boolean isPatientTable(){
        return false;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public final boolean isAppointmentTableRowValue(){
        return false;
    }

    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isPatients(){
        return false;
    }
    
    @Override
    public final boolean isPatientTableRowValue(){
        return true;
    }
    
    @Override
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
}
