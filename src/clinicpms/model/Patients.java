/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.util.ArrayList;
import clinicpms.store.IPMSStoreAction;

/**
 *
 * @author colin
 */
public class Patients implements IEntityCollecton{
    public ArrayList<Patient> getPatients() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(new Patient());
        return store.readPatients();
    }
    
    public int count()throws StoreException{
        IPMSStoreAction store = Store.FACTORY(new Patient());
        return store.countRowsIn(this);
    }
}
