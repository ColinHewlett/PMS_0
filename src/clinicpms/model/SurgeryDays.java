/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.IStore;
import clinicpms.store.exceptions.StoreException;
import java.time.DayOfWeek;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class SurgeryDays {
    private IStore store = null;
    
    public SurgeryDays() throws StoreException{
       store = Store.factory(); 
    }

    public HashMap<DayOfWeek, Boolean> read() throws StoreException{
        return store.read(new HashMap<>());
    }
    
    public HashMap<DayOfWeek,Boolean> update(HashMap<DayOfWeek,Boolean> value) throws StoreException{
        return store.update(value);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
