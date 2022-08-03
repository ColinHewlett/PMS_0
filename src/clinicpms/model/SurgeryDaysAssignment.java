/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.DayOfWeek;
import java.util.HashMap;
import clinicpms.store.IStoreActions;

/**
 *
 * @author colin
 */
public class SurgeryDaysAssignment extends EntityStoreType {
    private IStoreActions store = null;
    private String day = null;
    private boolean isSurgery = false;
    private HashMap<DayOfWeek,Boolean> assignment = new HashMap<>();
    
    private void set(HashMap<DayOfWeek,Boolean> value){
        assignment = value;
    }
    
    public SurgeryDaysAssignment(){
        super.setIsSurgeryDaysAssignment(true);
    }
    
    public SurgeryDaysAssignment(HashMap<DayOfWeek,Boolean> value){
        set(value);
        super.setIsSurgeryDaysAssignment(true);
    }
    
    public HashMap<DayOfWeek,Boolean> get(){
        return assignment;
    }

    public void delete() throws StoreException{
        //not currently implemented
    }
    public Integer count() throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        return store.count(this);
    }
    
    public void create() throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        store.create(this);        
    }
    
    public void drop() throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        store.drop(this);        
    }
    
    /**
     * implemented for migration purposes only
     * -- 
     */
    public void insert() throws StoreException{
        get().put(DayOfWeek.MONDAY, Boolean.TRUE);
        get().put(DayOfWeek.TUESDAY, Boolean.TRUE);
        get().put(DayOfWeek.WEDNESDAY, Boolean.TRUE);
        get().put(DayOfWeek.THURSDAY, Boolean.TRUE);
        get().put(DayOfWeek.FRIDAY, Boolean.TRUE);
        get().put(DayOfWeek.SATURDAY, Boolean.FALSE);
        get().put(DayOfWeek.SUNDAY, Boolean.FALSE);
        IStoreActions store = Store.FACTORY((EntityStoreType)this);
        store.insert(this);
    }
    
    /*
    public void populate() throws StoreException{
        //TheSurgeryDaysAssignment surgeryDaysValues = new SurgeryDaysAssignment();
        //HashMap<DayOfWeek, Boolean> initialContents = new HashMap<>();
        get().put(DayOfWeek.MONDAY, Boolean.TRUE);
        get().put(DayOfWeek.TUESDAY, Boolean.TRUE);
        get().put(DayOfWeek.WEDNESDAY, Boolean.TRUE);
        get().put(DayOfWeek.THURSDAY, Boolean.TRUE);
        get().put(DayOfWeek.FRIDAY, Boolean.TRUE);
        get().put(DayOfWeek.SATURDAY, Boolean.FALSE);
        get().put(DayOfWeek.SUNDAY, Boolean.FALSE);
        
        IStoreActions store = Store.FACTORY(this);
        store.populate(this);
    }
    */
    /*
    public SurgeryDaysAssignmentx read() throws StoreException{
        return null;
    }
*/
    
    public SurgeryDaysAssignment readTheSurgeryDaysAssignment() throws StoreException{
        IStoreActions store = Store.FACTORY((EntityStoreType)this);
        return store.read(this);
    }
      
    public void update() throws StoreException{
        IStoreActions store = Store.FACTORY((EntityStoreType)this); 
        store.update(this);
    }
    
    

    public Integer count1()throws StoreException{
        SurgeryDaysAssignment surgeryDaysAssignment = null;
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
