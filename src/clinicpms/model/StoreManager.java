/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.io.File;
import clinicpms.store.IStoreManagementActions;

/**
 *
 * @author colin
 */
public class StoreManager implements IStoreManager{
    private static StoreManager _INSTANCE = null;

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
    
    /**
     * request for the type of storage system in use for the app
     * @return String
     * @throws StoreException
     */
    public String getStorageType()throws StoreException{
        return Store.GET_STORAGE_TYPE();
    }
    
    @Override
    public String getPMSStorePath()throws StoreException{
        String path = null;
        PMS_Store target = new PMS_Store(this);
        return target.getPMSStorePath();
    }
    
    @Override
    public void setPMSStorePath(String path)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        target.setPMSStorePath(path);
    }
    
    @Override
    public void setPatientCSVPath(String path)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        target.setPatientCSVPath(path);
    }

    @Override
    public void setAppointmentCSVPath(String path)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        target.setAppointmentCSVPath(path);
    }
    
    @Override
    public String getAppointmentCSVPath()throws StoreException{
        PMS_Store target = new PMS_Store(this);
        return target.getAppointmentCSVPath();
    }

    @Override
    public String getPatientCSVPath()throws StoreException{
        PMS_Store target = new PMS_Store(this);
        return target.getPatientCSVPath();
    }

    @Override
    public File createStore(File file)throws StoreException{
        PMS_Store target = new PMS_Store(this);
        return target.createStore(file);
    }
    
    public class PMS_Store extends EntityStoreType{
        private StoreManager storeManager = null;
        private StoreManager.Scope scope = null;
        private String path = null;
        
        private PMS_Store(StoreManager storeManager){
            this.storeManager = storeManager;
        }
        
        public String get(){
            return this.path;
        }
        
        public void set(String value){
            this.path = value;
        }
        
        private StoreManager getStoreManager(){
            return storeManager;
        }
        
        private void setScope(StoreManager.Scope value){
            scope = value;
        }
        
        public StoreManager.Scope getScope(){
            return scope;
        }
        
        public File createStore(File file) throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            return store.initialiseTargetStore(file);
        }
        
        public String getAppointmentCSVPath()throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            setScope(StoreManager.Scope.CSV_APPOINTMENT_FILE);
            store.read(this);
            return get();
        }
        
        public String getPatientCSVPath()throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            setScope(StoreManager.Scope.CSV_PATIENT_FILE);
            store.read(this);
            return get();   
        }
        
        public String getPMSStorePath()throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            setScope(StoreManager.Scope.PMS_STORE);
            store.read(this);
            return get();    
        }
        
        public void setAppointmentCSVPath(String path)throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            setScope(StoreManager.Scope.CSV_APPOINTMENT_FILE);
            set(path);
            store.update(this);    
        }
        
        public void setPatientCSVPath(String path)throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            setScope(StoreManager.Scope.CSV_PATIENT_FILE);
            set(path);
            store.update(this);    
        }
        
        public void setPMSStorePath(String path)throws StoreException{
            IStoreManagementActions store = Store.FACTORY(getStoreManager());
            setScope(StoreManager.Scope.PMS_STORE);
            set(path);
            store.update(this);    
        }
    }
}

 