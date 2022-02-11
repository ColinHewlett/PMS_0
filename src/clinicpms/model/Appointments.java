/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.IAppointmentsStoreAction;
import clinicpms.model.Appointment.Category;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public class Appointments extends ArrayList<Appointment> implements IAppointments, 
                                                                    IEntityStoreType{

    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isAppointments(){
        return true;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public final boolean isAppointmentTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isPatients(){
        return false;
    }
    
    @Override
    public final boolean isPatientTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    @Override
    public void insert() throws StoreException{
        IAppointmentsStoreAction store = Store.FACTORY(this);
        store.insert(this);
    }
    
    /**
     * 
     * @param p Patient object
     * @param t Category enumeration constant
     * @throws StoreException 
     */
    @Override
    public void readForPatient(Patient p, Category t) throws StoreException{
        IAppointmentsStoreAction store = Store.FACTORY(new Appointments());
        clear();
        addAll(store.readAppointments(p, t));
    }
    
    /**
     * 
     * @param day LocalDate object
     * @throws StoreException 
     */
    @Override
    public void readForDay(LocalDate day) throws StoreException{
        IAppointmentsStoreAction store = Store.FACTORY(new Appointments());
        clear();
        addAll(store.readAppointmentsFor(day));
    }
    
    @Override
    public void readFromDay(LocalDate day) throws StoreException{
        IAppointmentsStoreAction store = Store.FACTORY(new Appointments());
        clear();
        addAll(store.readAppointmentsFrom(day));
    }
    
    @Override
    public void read()throws StoreException{
        IAppointmentsStoreAction store = Store.FACTORY(new Appointments());
        clear();
        addAll(store.readAppointments());
    }
    
    @Override
    public int count(){
        return size();
    }
}
