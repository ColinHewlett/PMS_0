/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
/**
 *
 * @author colin
 */
public class AppointmentDateVetoPolicy implements DateVetoPolicy{
    private HashMap<DayOfWeek,Boolean> surgeryDaysAssignment = null;

    public AppointmentDateVetoPolicy(HashMap<DayOfWeek,Boolean> surgeryDaysAssignment){
        setSurgeryDays(surgeryDaysAssignment);
    }
    
    @Override
    /**
    * isDateAllowed, Return true if a date should be allowed, or false if a date should be
    * vetoed.
    * @param date, LocalDate which represents date to be validated or not
    * @return boolean
    */
    public boolean isDateAllowed(LocalDate date) {
        boolean result = getSurgeryDays().get(date.getDayOfWeek());
        return result;
    }

    public LocalDate getNextAvailableDateTo(LocalDate day){
        do {
            day = day.plusDays(1);
        }while(!isDateAllowed(day));
        return day;
    }
    
    public LocalDate getPreviousAvailableDateTo(LocalDate day){
        do {
            day = day.minusDays(1);
        }while(!isDateAllowed(day));
        return day;
    }
    
    public LocalDate getNowDateOrClosestAvailableAfterNow(){
        LocalDate day = LocalDate.now();
        while(!isDateAllowed(day)){
            day = day.plusDays(1);
        }
        return day;
    }
    
    public void setSurgeryDays(HashMap<DayOfWeek,Boolean> value) {
        this.surgeryDaysAssignment = value;
    }
    
    public HashMap<DayOfWeek,Boolean> getSurgeryDays(){
        return this.surgeryDaysAssignment;
    }
}
