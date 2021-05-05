/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Dictionary;
/**
 *
 * @author colin
 */
public class AppointmentDateVetoPolicy implements DateVetoPolicy{
    private Dictionary<String,Boolean> surgeryDays = null;
    /**
    * isDateAllowed, Return true if a date should be allowed, or false if a date should be
    * vetoed.
    * @param date, LocalDate which represents date to be validated or not
    * @return boolean
    */
    
    public AppointmentDateVetoPolicy(Dictionary<String,Boolean> surgeryDays){
        setSurgeryDays(surgeryDays);
    }
    
    @Override
    public boolean isDateAllowed(LocalDate date) {
        String day = null;
        switch(date.getDayOfWeek().toString()){
            case "MONDAY":
                day = "Monday";
                break;
            case "TUESDAY":
                day = "Tuesday";
                break;
            case "WEDNESDAY":
                day = "Wednesday";
                break;
            case "THURSDAY":
                day = "Thursday";
                break;
            case "FRIDAY":
                day = "Friday";
                break;
            case "SATURDAY":
                day = "Saturday";
                break;
            case "SUNDAY":
                day = "Sunday";
                break;
        }
        boolean result = getSurgeryDays().get(day);
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
    
    public void setSurgeryDays(Dictionary<String,Boolean> value) {
        this.surgeryDays = value;
    }
    
    public Dictionary<String,Boolean> getSurgeryDays(){
        return this.surgeryDays;
    }
}
