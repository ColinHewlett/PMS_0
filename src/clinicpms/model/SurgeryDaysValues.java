/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import java.time.DayOfWeek;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class SurgeryDaysValues extends HashMap<DayOfWeek,Boolean> implements IEntityType{
    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysValues(){
        return true;
    }
}
