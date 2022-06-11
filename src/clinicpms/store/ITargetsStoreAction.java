/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.PMS_Store;
import clinicpms.model.EntityStoreType;
import java.io.File;


/**
 *
 * @author colin
 */
public interface  ITargetsStoreAction {
    //public void closeMigrationConnection() throws StoreException;
    public String getStoreType();
    //public String readAppointmentCSVPath() throws StoreException;
    //public String readMigrationTargetStorePath()throws StoreException;
    //public String readPatientCSVPath() throws StoreException;
    //public String readPMSTargetStorePath()throws StoreException;
    //public String readPMSStorePath()throws StoreException;
    //public void updateStorePath(String path)throws StoreException;
    //public void updateMigrationTargetStorePath(String path)throws StoreException;
    //public void updatePMSTargetStorePath(String path)throws StoreException;
    //public void updateAppointmentCSVPath(String path)throws StoreException;
    //public void updatePatientCSVPath(String path)throws StoreException;
    
    public EntityStoreType read(PMS_Store target)throws StoreException;
    public void update(PMS_Store target) throws StoreException;
    public File initialiseTargetStore(File path)throws StoreException;
}
