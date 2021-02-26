/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.CSVStore;
import clinicpms.store.AccessStore;
import clinicpms.store.PostgreSQLStore;
import clinicpms.store.SQLExpressStore;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.Store;
import clinicpms.store.interfaces.IStore;

/**
 *
 * @author colin
 */
public class StorageFactory {
    public StorageFactory(Store.Storage type)  throws StoreException{
        IStore store = switch (type){
            case CSV -> CSVStore.getInstance();
            case ACCESS -> AccessStore.getInstance();
            case POSTGRES -> PostgreSQLStore.getInstance();
            case SQL_EXPRESS -> SQLExpressStore.getInstance();  
        };
    }
}
