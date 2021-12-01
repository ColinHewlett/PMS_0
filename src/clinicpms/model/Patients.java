/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.migration_import_store.CSVStore;
import clinicpms.store.Store;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.IStore;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public class Patients {
    public ArrayList<Patient> getPatients() throws StoreException{
        IStore store = Store.factory();
        return store.readPatients();
    }
}
