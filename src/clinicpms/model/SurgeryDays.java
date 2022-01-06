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
import clinicpms.store.IPMSStoreAction;

/**
 *
 * @author colin
 */
public class SurgeryDays implements IEntity {
    private IPMSStoreAction store = null;
    private SurgeryDaysValues values = null; 
    
    public SurgeryDays(){
        
    }
    
    public SurgeryDays(SurgeryDaysValues surgeryDaysValues){
        setValues(surgeryDaysValues);
    }
    
    public SurgeryDaysValues getValues(){
        return values;
    }

    public void setValues(SurgeryDaysValues values){
        this.values = values;
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
    
    public SurgeryDaysValues read() throws StoreException{
        store = Store.FACTORY(this); 
        return store.read(new SurgeryDaysValues());
    }
            
    public void update() throws StoreException{
        store = Store.FACTORY(this); 
        store.update(getValues());
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
