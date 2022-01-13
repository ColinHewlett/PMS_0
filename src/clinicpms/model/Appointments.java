/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.model.Appointment.Category;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.LocalDate;
import java.util.ArrayList;
import clinicpms.store.IPMSStoreAction;

/**
 *
 * @author colin
 */
public class Appointments implements IEntityCollecton{

    /**
     * 
     * @param p Patient object
     * @param t Category enumeration constant
     * @return ArrayList of Appointment objects
     * @throws StoreException 
     */
    public ArrayList<Appointment> getAppointmentsFor(Patient p, Category t) throws StoreException{
        IPMSStoreAction store = Store.FACTORY(new Appointment());
        return store.readAppointments(p, t);
    }
    
    /**
     * 
     * @param day LocalDate object
     * @return ArrayList of Appointment objects
     * @throws StoreException 
     */
    public ArrayList<Appointment> getAppointmentsFor(LocalDate day) throws StoreException{
        IPMSStoreAction store = Store.FACTORY(new Appointment());
        return store.readAppointmentsFor(day);
    }
    
    public ArrayList<Appointment>getAppointmentsFrom(LocalDate day) throws StoreException{
        IPMSStoreAction store = Store.FACTORY(new Appointment());
        return store.readAppointmentsFrom(day);
    }
    
    public int count()throws StoreException{
        IPMSStoreAction store = Store.FACTORY(new Appointment());
        return store.countRowsIn(this);
    }
}
