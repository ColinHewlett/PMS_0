/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import java.time.DayOfWeek;
import java.time.LocalDate;
/**
 *
 * @author colin
 */
public class AppointmentDateVetoPolicy implements DateVetoPolicy{
    /**
    * isDateAllowed, Return true if a date should be allowed, or false if a date should be
    * vetoed.
    * @param date, LocalDate which represents date to be validated or not
    * @return boolean
    */
    @Override
    public boolean isDateAllowed(LocalDate date) {

        // Allow appointment only on tuesdays, thursdays and fridays
        if ((date.getDayOfWeek() != DayOfWeek.TUESDAY) &&
                (date.getDayOfWeek() != DayOfWeek.THURSDAY) &&
                (date.getDayOfWeek() != DayOfWeek.FRIDAY)) {
            return false;
        }
        // Allow all other days.
        return true;
    }
    
}
