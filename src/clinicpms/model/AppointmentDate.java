/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import java.time.LocalDate;
/**
 *
 * @author colin
 */
public class AppointmentDate implements IEntityStoreType{
    private LocalDate value = null;
    
    @Override
    public final boolean isAppointment(){
        return false;
    }
    
    @Override
    public final boolean isAppointmentDate(){
        return true;
    }
    
    @Override
    public final boolean isPatient(){
        return false;
    }
    
    @Override
    public final boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    public AppointmentDate(LocalDate date){
        value = date;
    }
    
    public LocalDate getValue(){
        return value;
    }
}
