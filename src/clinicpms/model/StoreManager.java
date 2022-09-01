/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.io.File;
import clinicpms.store.IStoreActions;

/**
 *
 * @author colin
 */
public class StoreManager implements IStoreClient{
    private static StoreManager _INSTANCE = null;
    private String path = null;
    private Scope scope = null;

    public static StoreManager GET_STORE_MANAGER() {
        if (_INSTANCE == null){
            _INSTANCE = new StoreManager();
        }
        return _INSTANCE;
    }
    
    public enum Scope { 
                        CSV_APPOINTMENT_FILE,
                        CSV_PATIENT_FILE,
                        PMS_STORE};
    
    public StoreManager.Scope getScope(){
        return scope;
    }
    
    private void setScope(StoreManager.Scope value){
        scope = value;
    }
        
    public String getPath(){
        return path;
    }
    
    public void setPath(String value){
        path = value;
    }

    /**
     * request for the type of storage system in use for the app
     * @return String
     * @throws StoreException
     */
    public String getStorageType()throws StoreException{
        return Store.GET_STORAGE_TYPE();
    }
    
    public File createStore(File file) throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        return store.initialiseTargetStore(file);
    }

    public String getAppointmentCSVPath()throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setScope(Scope.CSV_APPOINTMENT_FILE);
        store.read(this);
        return getPath();
    }

    public String getPatientCSVPath()throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setScope(Scope.CSV_PATIENT_FILE);
        store.read(this);
        return getPath();   
    }

    public String getPMSStorePath()throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setScope(Scope.PMS_STORE);
        store.read(this);
        return getPath();    
    }

    public void setAppointmentCSVPath(String path)throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setScope(Scope.CSV_APPOINTMENT_FILE);
        setPath(path);
        store.update(this);    
    }

    public void setPatientCSVPath(String path)throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setScope(Scope.CSV_PATIENT_FILE);
        setPath(path);
        store.update(this);    
    }

    public void setPMSStorePath(String path)throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setScope(Scope.PMS_STORE);
        setPath(path);
        store.update(this);    
    }
}

 