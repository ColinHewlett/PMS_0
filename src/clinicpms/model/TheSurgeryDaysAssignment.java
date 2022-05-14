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
public class TheSurgeryDaysAssignment extends EntityStoreType implements IEntity{
    private IPMSStoreAction store = null;
    private String day = null;
    private boolean isSurgery = false;
    private HashMap<DayOfWeek,Boolean> assignment = new HashMap<>();
    
    private void set(HashMap<DayOfWeek,Boolean> value){
        assignment = value;
    }
    
    public TheSurgeryDaysAssignment(){
        setIsSurgeryDaysAssignment(true);
    }
    
    public TheSurgeryDaysAssignment(HashMap<DayOfWeek,Boolean> value){
        set(value);
        setIsSurgeryDaysAssignment(true);
    }
    
    public HashMap<DayOfWeek,Boolean> get(){
        return assignment;
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
        IPMSStoreAction store = Store.FACTORY((EntityStoreType)this);
        store.create(this);        
    }
    
    @Override
    public void drop() throws StoreException{
        IPMSStoreAction store = Store.FACTORY((EntityStoreType)this);
        store.drop(this);        
    }
    
    @Override
    /**
     * implemented for migration purposes only
     * -- 
     */
    public void insert() throws StoreException{
        IPMSStoreAction store = Store.FACTORY((EntityStoreType)this);
        store.insert(this);
    }
    
    @Override
    public SurgeryDaysAssignmentx read() throws StoreException{
        return null;
    }
    
    public TheSurgeryDaysAssignment readTheSurgeryDaysAssignment() throws StoreException{
        IPMSStoreAction store = Store.FACTORY((EntityStoreType)this);
        return store.read(this);
    }
      
    @Override
    public void update() throws StoreException{
        IPMSStoreAction store = Store.FACTORY((EntityStoreType)this); 
        store.update(this);
    }
    
    

    public Integer count()throws StoreException{
        TheSurgeryDaysAssignment surgeryDaysAssignment = null;
        try{
            surgeryDaysAssignment = this.readTheSurgeryDaysAssignment();
            
        }catch (StoreException ex){
            if (!ex.getErrorType().equals(StoreException.ExceptionType.SURGERY_DAYS_TABLE_MISSING_IN_PMS_DATABASE))
                throw ex;
        }
        if (surgeryDaysAssignment==null) return null;
        if (surgeryDaysAssignment.get().isEmpty()) return 0;
        else return surgeryDaysAssignment.get().size();
    }
    
    public String getDay(){
        return this.day;
    }
    
    public boolean getIsSurgery(){
        return this.isSurgery;
    }
}
