/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;


import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IPMSStoreAction;
import java.time.LocalDate;
import java.util.ArrayList;
/**
 *
 * @author colin
 */
public class PatientNotification extends EntityStoreType {
    private PatientNotification.Collection collection = null;
    private Integer key = null;
    private Patient patient = null;
    private LocalDate date = null;
    private String notification = null;
    private Boolean isActioned = false;
    private Boolean isDeleted = false;
    private Scope scope = null;
    
    
    public enum Scope{ALL,UNACTIONED,ALL_BY_KEY, UNACTIONED_BY_KEY};
    
    public Collection getCollection(){
        return collection;
    }
    private void setCollection(Collection value){
        collection = value;
    }
    
    public PatientNotification(){
        this.setIsPatientNotification(true);
        setCollection(new Collection());
        
    }
    
    public PatientNotification(int key){
        this.setIsPatientNotification(true);
        setCollection(new Collection());
        setKey(key);
    }
    
    /*
    public PatientNotification read()throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
*/
    
    public Boolean getIsActioned(){
        return isActioned;
    }
    
    public Boolean getIsDeleted(){
        return isDeleted;
    }
    
    public LocalDate getNotificationDate(){
        return date;
    }
    
    public Integer getKey(){
        return key;
    }
    
    public String getNotificationText(){
        return notification;
    }
    
    public Patient getPatient(){
        return patient;
    }
    
    public void setIsActioned(boolean value){
        isActioned = value;
    }
    
    public void setIsDeleted(boolean value){
        isDeleted = value;
    }
    
    public void setNotificationDate(LocalDate value){
        date = value;
    }
    
    public void setKey(int value){
        key = value;
    }
    
    public void setNotificationText(String value){
        notification = value;
    }
    
    public void setPatient(Patient value){
        patient = value;
    }
    
    /**
     * method sends message to store to insert this patient notification
     * -- the store returns the key value of the inserted notification
     * -- this is used to initialise this patient notification's key
     * -- redundant op because store initialises notification's key value anyway
     * -- but store object might not; i.e. not a contractual obligation in store to do so
     * -- whereas this way a key value us expected back from the store
     * @throws StoreException 
     */
    public void insert() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.insert(this);
    }
    
    public PatientNotification read() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    public class Collection extends EntityStoreType{
        private ArrayList<PatientNotification> collection = new ArrayList<>();
        
        private Collection(){
            this.setIsPatientNotificationCollection(true);
        }
        
        public Scope getScope(){
            return scope;
        }
        
        public void setScope(Scope value){
            scope = value;
        }
        
        public ArrayList<PatientNotification> get(){
            return collection;
        }
        
        public void set(ArrayList<PatientNotification> value){
            collection = value;
        }

        public void read()throws StoreException{
            IPMSStoreAction store = Store.FACTORY(this);
            /**
             * is the next line of code redundant?
             * -- if the PatientNotification.Collection object is the same as "this" one
             */
            set(store.read(this).get());
        }
    }
}
