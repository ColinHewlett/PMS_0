/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.ITargetsStoreAction;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.io.File;

/**
 *
 * @author colin
 */
public class TheStoreManager implements IStoreManager{
    private static TheStoreManager _INSTANCE = null;

    public static TheStoreManager GET_STORE_MANAGER() {
        if (_INSTANCE == null){
            _INSTANCE = new TheStoreManager();
        }
        return _INSTANCE;
    }
    
    /**
     * request for the type of storage system in use for the app
     * @return String
     * @throws StoreException
     */
    public String getStorageType()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        return store.getStoreType();
    }
    
    public String getPMSStorePath()throws StoreException{
        String path = null;
        PMS_Store target = new PMS_Store(this);
        return target.getPMSStorePath();
    }
    
    public void setPMSStorePath(String path)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        target.setPMSStorePath(path);
    }
    
    public void setPatientCSVPath(String path)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        target.setPatientCSVPath(path);
    }

    public void setAppointmentCSVPath(String path)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        target.setAppointmentCSVPath(path);
    }
    
    public String getAppointmentCSVPath()throws StoreException{
        PMS_Store target = new PMS_Store(this);
        return target.getAppointmentCSVPath();
    }

    public String getPatientCSVPath()throws StoreException{
        PMS_Store target = new PMS_Store(this);
        return target.getPatientCSVPath();
    }
    
    public void copyFile(File fromFile, File toFile){
        
    }
    
    public File createStore(File file)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        return target.createStore(file);
    }
    
    public void closeConnection()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        //store.closeMigrationConnection();
    }

  
}

