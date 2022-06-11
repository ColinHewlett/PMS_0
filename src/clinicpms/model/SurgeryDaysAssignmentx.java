/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.IStoreAction;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.DayOfWeek;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class SurgeryDaysAssignmentx extends HashMap<DayOfWeek,Boolean> implements IEntity, 
                                                                                 IEntityStoreType{
    private IStoreAction store = null;
    private String day = null;
    private boolean isSurgery = false;
    
    public SurgeryDaysAssignmentx(){
    
    }
    
    public SurgeryDaysAssignmentx(HashMap<DayOfWeek,Boolean> value){
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
    public boolean isAppointmentTable(){
        return false;
    }
    
    @Override
    public boolean isPatientTable(){
        return false;
    }
    
    @Override
    /**
     * not currently implemented
     */
    public void delete() throws StoreException{
        //not currently implemented
    }
    
    
    @Override
    public void create() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        //store.create(this);        
    }
    
    @Override
    public void drop() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        //store.drop(this);        
    }
    
    @Override
    /**
     * implemented for migration purposes only
     * -- 
     */
    public void insert() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        //store.insert(this);
    }
    
    public SurgeryDaysAssignmentx read() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        //return store.read(this);
        return null;
    }
      
    @Override
    public void update() throws StoreException{
        IStoreAction store = Store.FACTORY(this); 
        //store.update(this);
    }
    
    

    public Integer count()throws StoreException{
        SurgeryDaysAssignmentx surgeryDaysAssignment = null;
        try{
            surgeryDaysAssignment = this.read();
            
        }catch (StoreException ex){
            if (!ex.getErrorType().equals(StoreException.ExceptionType.SURGERY_DAYS_TABLE_MISSING_IN_PMS_DATABASE))
                throw ex;
        }
        if (surgeryDaysAssignment==null) return null;
        if (surgeryDaysAssignment.isEmpty()) return 0;
        else return surgeryDaysAssignment.size();
    }
    
    public String getDay(){
        return this.day;
    }
    
    public boolean getIsSurgery(){
        return this.isSurgery;
    }
}
