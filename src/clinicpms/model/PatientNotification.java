/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;


import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import clinicpms.store.IStoreActions;
/**
 *
 * @author colin.hewlett.solutions@gmail.com
 */
public class PatientNotification extends Entity implements IEntityStoreActions {
  
//<editor-fold defaultstate="collapsed" desc="Private and protected state">
    private Integer key = null;
    private Patient patient = null;
    private LocalDate date = null;
    private String notification = null;
    private Boolean isActioned = false;
    private Boolean isDeleted = false;
    private ArrayList<PatientNotification> collection = new ArrayList<>();
    
    protected Integer getKey(){
        return key;
    }
    
    protected void setKey(int value){
        key = value;
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Public interface">
    
//<editor-fold defaultstate="collapsed" desc="Public state and non-persistent store related operations">    
    public PatientNotification(){
        this.setIsPatientNotification(true);  
    }
    
    public PatientNotification(int key){
        this.setIsPatientNotification(true);;
        setKey(key);
    }
    
    public ArrayList<PatientNotification> get(){
        return collection;
    }
        
    public void set(ArrayList<PatientNotification> value){
        collection = value;
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
    
    public void setNotificationText(String value){
        notification = value;
    }
    
    public void setPatient(Patient value){
        patient = value;
    }
    
    public void action()throws StoreException{
        setIsActioned(true);
        this.update();
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Persistent storage related operations">  
    
    /**
     * Counts the number of patient notifications stored on the system; which depends on the current setting of the object's scope setting (all notifications, or just unactioned ones etc)
     * @return Integer, total number of the requested notification type 
     * @throws StoreException 
     */
    @Override
    public Integer count()throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        return store.count(this);
    }
    
    /**
     * Creates a new PatientNotification table in persistent store
     * @throws StoreException 
     */
    @Override
    public void create() throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        store.create(this);
    }
    
    /**
     * Method updates this notification's isDeleted property to true
     * @throws StoreException 
     */
    @Override
    public void delete() throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        store.delete(this, getKey());
    }
    
    /**
     * Not currently implemented
     * @throws StoreException 
     */
    @Override
    public void drop() throws StoreException{
        
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
    @Override
    public void insert() throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        setKey(store.insert(this));
    }
    
    /**
     * scope of entity fetch from store is specified on entry; thus
     * -- SINGLE scope
     * ---- fetches this patient notification from persistent store
     * ---- fields in the returned notification's patient are uninitialised except for the key field
     * -- FOR_PATIENT scope
     * ---- fetches from persistent store patient notifications belonging to this patient notification
     * ---- for all other scopes, fetches all notifications consistent with the scope (typically INACTIONED)
     * 
     * @return PatientNotification
     * @throws StoreException 
     */
    @Override
    public PatientNotification read() throws StoreException{
        Patient p = null;
        PatientNotification result = null;
        PatientNotification patientNotification = null; 
        IStoreActions store = Store.FACTORY(this);
        switch (getScope()){
            case SINGLE:
                patientNotification = store.read(this, getKey());
                p = new Patient(patientNotification.getPatient().getKey());
                p.setScope(Scope.SINGLE);
                patientNotification.setPatient(p.read());
                break;
            default:
                if (getPatient()!=null) set(store.read(this, getPatient().getKey()).get());
                else set(store.read(this, null).get());
                Iterator it = get().iterator();
                while(it.hasNext()){
                    patientNotification = (PatientNotification)it.next();
                    switch(getScope()){ 
                        case FOR_PATIENT:                                
                                patientNotification.setPatient(PatientNotification.this.getPatient());     
                            break;
                        default:
                                p = new Patient(patientNotification.getPatient().getKey());
                                p.setScope(Scope.SINGLE);
                                patientNotification.setPatient(p.read());
                            break;        
                    }
                }       
        }
        return patientNotification;
    }
/*
        public void read()throws StoreException{
            IStoreActions store = Store.FACTORY(this);
            //30/07/2022 09:26
            if (getPatient()!=null) set(store.read(this, getPatient().getKey()).get());
            else set(store.read(this, null).get());
            //set(store.read(this, getPatient().getKey()).get());
            Iterator it = get().iterator();
            switch(getScope()){
                case FOR_PATIENT:
                    while(it.hasNext()) {
                        PatientNotification patientNotification = (PatientNotification)it.next();
                        patientNotification.setPatient(PatientNotification.this.getPatient());     
                    }
                    break;
                default:
                    while(it.hasNext()){
                        PatientNotification patientNotification = (PatientNotification)it.next();
                        Patient patient = new Patient(patientNotification.getPatient().getKey());
                        patientNotification.setPatient(patient.read());
                    }
                    break;        
            }
        }
    */
    public void update()throws StoreException{
        IStoreActions store = Store.FACTORY(this);
        store.update(this, getKey(), getPatient().getKey());
    }
//</editor-fold>
    
//</editor-fold>

}
