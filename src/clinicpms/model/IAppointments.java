/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;
import clinicpms.model.Appointment;
import clinicpms.model.Appointment.Category;
import clinicpms.model.Patient;
import clinicpms.store.StoreException;
import java.util.ArrayList;
import java.time.LocalDate;
/**
 *
 * @author colin
 */
public interface IAppointments {
    public int count();
    public void read() throws StoreException;
    public void readForDay(LocalDate day) throws StoreException;
    public void readForPatient(Patient p,Category c ) throws StoreException;
    public void readFromDay(LocalDate day) throws StoreException;
    
}
