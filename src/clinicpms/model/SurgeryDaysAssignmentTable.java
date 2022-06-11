/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.DayOfWeek;
import clinicpms.store.IMigrationStoreAction;

/**
 *
 * @author colin
 */
public class SurgeryDaysAssignmentTable {
  /*
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
        store.exportToPMS(new SurgeryDaysAssignmentx());
    }
    
    public void populate() throws StoreException{
        SurgeryDaysAssignmentx surgeryDaysValues = new SurgeryDaysAssignmentx();
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
    
    public Integer count() throws StoreException{
        Integer result = null;
        IEntityStoreType entity = null;
        Integer rowcount = null;
        try{
            IMigrationStoreAction store = Store.FACTORY(this);
            return store.countRowsIn(this);
        }catch (StoreException ex){

            if (!ex.getErrorType().equals(StoreException.ExceptionType.SURGERY_DAYS_TABLE_MISSING_IN_MIGRATION_DATABASE)){
                throw new StoreException(ex.getMessage(), ex.getErrorType());
                //result = null;
            }
        }
        return result;
         
    }
*/
}
