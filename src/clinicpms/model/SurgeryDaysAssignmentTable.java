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
import clinicpms.store.IMigrationStoreAction;

/**
 *
 * @author colin
 */
public class SurgeryDaysAssignmentTable implements ITable{
  
    
    public void create() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.create(this);
    }
    
    public void drop()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.drop(this);
    }
    
    @Override
    public void exportToPMS() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.exportToPMS(new SurgeryDaysAssignment());
    }
    
    public void populate() throws StoreException{
        SurgeryDaysAssignment surgeryDaysValues = new SurgeryDaysAssignment();
        //HashMap<DayOfWeek, Boolean> initialContents = new HashMap<>();
        surgeryDaysValues.put(DayOfWeek.MONDAY, Boolean.TRUE);
        surgeryDaysValues.put(DayOfWeek.TUESDAY, Boolean.TRUE);
        surgeryDaysValues.put(DayOfWeek.WEDNESDAY, Boolean.TRUE);
        surgeryDaysValues.put(DayOfWeek.THURSDAY, Boolean.TRUE);
        surgeryDaysValues.put(DayOfWeek.FRIDAY, Boolean.TRUE);
        surgeryDaysValues.put(DayOfWeek.SATURDAY, Boolean.FALSE);
        surgeryDaysValues.put(DayOfWeek.SUNDAY, Boolean.FALSE);
        
        IMigrationStoreAction store = Store.FACTORY(this);
        store.populate(surgeryDaysValues);
    }
    
    public IEntityStoreType read()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    /*
    public int count()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.countRowsIn(this);
    }
*/
}