/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public class SQLExpressStore extends Store {
    private static SQLExpressStore instance = null;
    public static SQLExpressStore getInstance(){
        SQLExpressStore result = null;
        if (instance == null) result = new SQLExpressStore();
        else result = instance;
        return result;
    }
    public Appointment create(Appointment a) throws StoreException{
        return null;
    }
    public Patient create(Patient p) throws StoreException{
        return null;
    }
    public void delete(Appointment a) throws StoreException{
        
    }
    public void delete(Patient p) throws StoreException{
        
    }
    public Appointment read(Appointment a) throws StoreException{
        return null;
    }
    public Patient read(Patient p) throws StoreException{
        return null;
    }
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException{
        return null;
    }
    public ArrayList<Appointment> readAppointments(Patient p, Appointment.Category c) throws StoreException{
        return null;
    }
    public Patient update(Patient p) throws StoreException{
        return null;
    }
    public Appointment update(Appointment a) throws StoreException{
        return null;
    }
}
