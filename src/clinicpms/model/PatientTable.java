/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IMigrationStoreAction;

/**
 *
 * @author colin
 */
public class PatientTable implements ITable{
    private int count;
    
    @Override
    public void create() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);   
        store.create(this);
    }
    
    @Override
    public void drop()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.drop(this);
    }
    
    @Override
    public void populate() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.populate(this);
    }  
    
    public int count() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        return store.countRowsInTable(this);
    }
}
