/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.StoreException;
import clinicpms.store.Store;
import clinicpms.store.ITargetsStoreAction;
import java.io.File;

/**
 * StoreManager responsibilities
 * -- enable access to following store data
 * ---- type of storage system currently in use
 * ---- read/write access to the migration target store path
 * ---- read/write access to the PMS target store path
 * ---- read/write access to the CVS import data files
 * @author colin
 */
public class StoreManager implements IStoreManager{
    private static StoreManager _INSTANCE = null;

    public static StoreManager GET_STORE_MANAGER() throws StoreException{
        if (_INSTANCE == null){
            _INSTANCE = new StoreManager();
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

    /**
     * request to update migration store path
     * @throws StoreException
     */
    public void setMigrationTargetStorePath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        store.updateMigrationTargetStorePath(path);
    }
    
    /**
     * request for the current migration store path
     * @return String
     * @throws StoreException 
     */
    public String getMigrationTargetStorePath() throws StoreException{
        String path = null;
        ITargetsStoreAction store = Store.FACTORY(this);
        path = store.readMigrationTargetStorePath();
        return path;
    }
    
    /**
     * request to update the PMS target store path 
     * @param path:String
     * @throws StoreException 
     */
    public void setPMSTargetStorePath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        store.updatePMSTargetStorePath(path);
    }
    
    /**
     * request for the current PMS target store path
     * @return String
     * @throws StoreException 
     */
    public String getPMSTargetStorePath() throws StoreException{
        String path = null;
        ITargetsStoreAction store = Store.FACTORY(this);
        path = store.readPMSTargetStorePath();
        return path;
    }
    
    /**
     * request to update the patient's CSV import file used to migrate data
     * @param path : String 
     * @throws StoreException 
     */
    public void setPatientCSVPath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        store.updatePatientCSVPath(path);
    }
    
    /**
     * request to update the appointment's CSV import file used to migrate data
     * @param path : String 
     * @throws StoreException
     */
    public void setAppointmentCSVPath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        store.updateAppointmentCSVPath(path);
    }
    
    /**
     * request the current appointment's CSV file used for migrated data
     * @return String 
     * @throws StoreException
     */
    public String getAppointmentCSVPath()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        return store.readAppointmentCSVPath();
    }
    
    /**
     * request the current patient's CSV file used for migrated data
     * @return String 
     * @throws StoreException
     */
    public String getPatientCSVPath()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        return store.readPatientCSVPath();
    }
    
    public File initialiseTargetDatabase(File file)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        return store.initialiseTargetStore(file);
    }
    
    public void closeConnection()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(this);
        store.closeMigrationConnection();
    }

  
}
