/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IMigrationStoreAction;
import clinicpms.store.IPMSStoreAction;
import clinicpms.model.IEntityStoreType;

/**
 *
 * @author colin
 */
public class AppointmentTable implements ITable{
    private int count;
    
    public Integer count() throws StoreException{
        Integer result = null;
        IEntityStoreType entity = null;
        Integer rowcount = null;
        try{
            IMigrationStoreAction store = Store.FACTORY(this);
            return store.countRowsIn(this);
        }catch (StoreException ex){
            /**
             * if MigrationSQL.APPOINTMENT_TABLE_ROW_COUNT is source of exception
             * -- assumed this is cause because the AppointmentTable is currently missing from the database schema 
             */
            if (!ex.getErrorType().equals(StoreException.ExceptionType.APPOINTMENT_TABLE_MISSING_IN_MIGRATION_DATABASE)){
                //throw new StoreException(ex.getMessage(), ex.getErrorType());
                result = null;
            }
        }
        return result;
         
    }
    
    public void create() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.create(this);
    }
    
    public void drop()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.drop(this);
    }

    public void populate()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.populate(this);
    }
    
    public IEntityStoreType read() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    public void exportToPMS() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.exportToPMS(new Appointments());
    }
    
    public void checkIntegrity()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.checkIntegrity();
    }
}
