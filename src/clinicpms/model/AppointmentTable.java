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

/**
 *
 * @author colin
 */
public class AppointmentTable implements ITable{
    private int count;
    
    public int count() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.countRowsInTable(this);
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
    
    public void checkIntegrity()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.checkIntegrity();
    }
}
