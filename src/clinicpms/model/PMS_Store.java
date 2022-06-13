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
public class PMS_Store extends EntityStoreType{
    private StoreManager storeManager = null;
    private Scope scope = null;
    private String path = null;
    
    public enum Scope { 
                        CSV_APPOINTMENT_FILE,
                        CSV_PATIENT_FILE,
                        PMS_STORE};
    
    private StoreManager getStoreManager(){
        return storeManager;
    }
    
    private void setPMSStoreManager(StoreManager manager){
        storeManager = manager;
    }
    
    private void setScope(Scope value){
        scope = value;
    }

    public String get(){
        return this.path;
    }
    
    public void set(String value){
        this.path = value;
    }
    
    public Scope getScope(){
        return scope;
    }
    
    protected PMS_Store(StoreManager manager){
        setPMSStoreManager(manager);
        super.setIsPMSStore(true);
    }
    
    public File createStore(File file) throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        return store.initialiseTargetStore(file);
    }

    public String getAppointmentCSVPath()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        setScope(Scope.CSV_APPOINTMENT_FILE);
        PMS_Store target = (PMS_Store)store.read(this);
        return target.get();
    }
    
    public String getPatientCSVPath()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        setScope(Scope.CSV_PATIENT_FILE);
        PMS_Store target = (PMS_Store)store.read(this);
        return target.get();   
    }
    
    public String getPMSStorePath()throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        setScope(Scope.PMS_STORE);
        PMS_Store target = (PMS_Store)store.read(this);
        return target.get();    
    }
    
    public void setAppointmentCSVPath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        setScope(Scope.CSV_APPOINTMENT_FILE);
        set(path);
        store.update(this);    
    }
    
    public void setPatientCSVPath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        setScope(Scope.CSV_PATIENT_FILE);
        set(path);
        store.update(this);    
    }
    
    public void setPMSStorePath(String path)throws StoreException{
        ITargetsStoreAction store = Store.FACTORY(getStoreManager());
        setScope(Scope.PMS_STORE);
        set(path);
        store.update(this);    
    }
            
    
}
