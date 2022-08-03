/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.EntityStoreType;
import clinicpms.model.StoreManager;
import java.io.File;

/**
 *
 * @author colin
 */
public abstract class StoreManagementActions {
    public abstract EntityStoreType read(StoreManager.PMS_Store target)throws StoreException;
    public abstract void update(StoreManager.PMS_Store target) throws StoreException;
    public abstract File initialiseTargetStore(File path)throws StoreException;
}
