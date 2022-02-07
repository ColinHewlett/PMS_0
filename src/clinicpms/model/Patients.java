/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.util.ArrayList;
import clinicpms.store.IPatientsStoreAction;

/**
 *
 * @author colin
 */
public class Patients extends ArrayList<Patient> implements IPatients, IEntityStoreType{
    
    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isAppointments(){
        return false;
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
        return true;
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
        IPatientsStoreAction store = Store.FACTORY(this);
        store.insert(this);
    }
    
    @Override
    public void read() throws StoreException{
        IPatientsStoreAction store = Store.FACTORY(new Patients());
        clear();
        addAll(store.readPatients());
    }
    
    public int count(){
        return size();
    }
}
