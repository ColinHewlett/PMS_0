/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store.stores;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Dictionary;

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
    
    public void closeConnection()throws StoreException{
        
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
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException{
        return null;
    }
    public ArrayList<Patient> readPatients() throws StoreException{
        return null;
    }
    public Patient update(Patient p) throws StoreException{
        return null;
    }
    public Appointment update(Appointment a) throws StoreException{
        return null;
    }
    
    @Override
    public Dictionary<String,Boolean> readSurgeryDays() throws StoreException{
        return null;
    }
    
    @Override
    public Dictionary<String,Boolean> updateSurgeryDays(Dictionary<String,Boolean> d) throws StoreException{
        return null;
    }
    
    @Override
    public IMigrationManager getMigrationManager(){
        return null;
    }
}
