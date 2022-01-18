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
public class AppointmentDate implements IEntityType{
    private LocalDate value = null;
    
    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return true;
    }
    
    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    public AppointmentDate(LocalDate date){
        value = date;
    }
    
    public LocalDate getValue(){
        return value;
    }
}
