/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;


import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IStoreAction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
/**
 *
 * @author colin
 */
public class PatientNotification extends EntityStoreType {
    private PatientNotification.Collection collection = null;
    private Integer key = null;
    private ThePatient patient = null;
    private LocalDate date = null;
    private String notification = null;
    private Boolean isActioned = false;
    private Boolean isDeleted = false;
    private Scope scope = null;

    public enum Scope{ALL,UNACTIONED,ALL_FOR_PATIENT, UNACTIONED_FOR_PATIENT};
    
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
    
    public ThePatient getPatient(){
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
    
    public void setPatient(ThePatient value){
        patient = value;
    }
    
    public void action()throws StoreException{
        setIsActioned(true);
        this.update();
    }
    
    public void create() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        store.create(this);
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
        IStoreAction store = Store.FACTORY(this);
        store.insert(this);
    }
    
    /**
     * method fetches from persistent store a patient notification object
     * -- the concrete store class returns a patient object empty of all values except patient's key value
     * -- to return a fully initialised patient per notification object, the key value must be used to fetch from store the patient object
     * @return PatientNotification
     * @throws StoreException 
     */
    public PatientNotification read() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        PatientNotification patientNotification = store.read(this);
        ThePatient patient = new ThePatient(patientNotification.getPatient().getKey());
        patientNotification.setPatient(patient.read());
        return patientNotification;
    }
    
    public void update()throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        store.update(this);
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
        
        public ThePatient getPatient(){
            return PatientNotification.this.getPatient();
        }
        
        public Integer count()throws StoreException{
            IStoreAction store = Store.FACTORY(this);
            return store.count(this);
        }

        /**
         * method fetches from  store either all notifications for this notification's patient; or all (typically unactioned only) patient notifications recorded on the system
         * -- an additional read to fetch the patient details is not required for the former case
         * -- in the latter case, an additional read from store fetches the patient details associated with the notification 
         * @throws StoreException 
         */
        public void read()throws StoreException{
            IStoreAction store = Store.FACTORY(this);
            set(store.read(this).get());
            Iterator it = get().iterator();
            switch(getScope()){
                case ALL_FOR_PATIENT:
                    while(it.hasNext()) {
                        PatientNotification patientNotification = (PatientNotification)it.next();
                        patientNotification.setPatient(PatientNotification.this.getPatient());     
                    }
                    break;
                default:
                    while(it.hasNext()){
                        PatientNotification patientNotification = (PatientNotification)it.next();
                        ThePatient patient = new ThePatient(patientNotification.getPatient().getKey());
                        patientNotification.setPatient(patient.read());
                    }
                    break;        
            }
        }
    }
}
