/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.IPMSStoreAction;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.DayOfWeek;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class SurgeryDaysAssignment extends HashMap<DayOfWeek,Boolean> implements IEntity, IEntityStoreType{
    private IPMSStoreAction store = null;
    
    public SurgeryDaysAssignment(){
    
    }
    
    public SurgeryDaysAssignment(HashMap<DayOfWeek,Boolean> value){
        putAll(value);
    }
    
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
        return false;
    }
    
    @Override
    public final boolean isPatientTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysAssignment(){
        return true;
    }
    
    @Override
    /**
     * not currently implemented
     */
    public void delete() throws StoreException{
        //not currently implemented
    }
    
    @Override
    /**
     * not currently implemented
     */
    public void insert() throws StoreException{
        //not currently implemented
    }
    
    public SurgeryDaysAssignment read() throws StoreException{
        store = Store.FACTORY(this); 
        return store.read(this);
    }
            
    public void update() throws StoreException{
        store = Store.FACTORY(this); 
        store.update(this);
    }

}
