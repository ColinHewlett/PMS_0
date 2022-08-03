/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.StoreManager;
import clinicpms.model.EntityStoreType;
import java.io.File;


/**
 *
 * @author colin
 */
public interface  IStoreManagementActions {   
    public EntityStoreType read(StoreManager.PMS_Store target)throws StoreException;
    public void update(StoreManager.PMS_Store target) throws StoreException;
    public File initialiseTargetStore(File path)throws StoreException;
}
