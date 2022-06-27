/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.TheAppointment;

/**
 *
 * @author colin
 */
public class AppointmentDelegate extends TheAppointment{
    
    protected AppointmentDelegate(){
        super();
    }
    
    protected AppointmentDelegate(TheAppointment appointment){
        super.setDuration(appointment.getDuration());
        super.setNotes(appointment.getNotes());
        super.setPatient(appointment.getPatient());
        super.setStart(appointment.getStart());
        super.setStatus(appointment.getStatus());
    }
    
    protected Integer getAppointmentKey(){
        return super.getKey();
    }
    protected void setAppointmentKey(Integer key){
        super.setKey(key);
    }
    
}