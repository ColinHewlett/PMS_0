/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.LocalDateTime;
import java.time.Duration;
import clinicpms.store.IPMSStoreAction;

/**
 *
 * @author colin
 */
public class Appointment  implements IEntity, IEntityType{
    public static enum Status{BOOKED,UNBOOKED};
    private Integer key = null;
    private LocalDateTime start = null;
    private Duration duration  = null;
    private String notes = null;
    private Patient patient;
    private Category category = null;
    private Status status = Appointment.Status.BOOKED;
    
    public static enum Category{DENTAL, HYGIENE, ALL}
    
    @Override
    public boolean isAppointment(){
        return true;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysValues(){
        return false;
    }

    public Appointment(){
    } //constructor creates a new Appointment record

    /**
     * 
     * @param key 
     */
    public Appointment( int key) {
        this.key = key;
    }
    
    @Override
    public void insert() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.insert(this);  
    }
    
    @Override
    public void delete() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.delete(this);
    }
    
    public Appointment read() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    @Override
    public void update() throws StoreException{ 
        IPMSStoreAction store = Store.FACTORY(this);
        store.update(this);
    }

    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    
    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration  duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getKey() {
        return key;
    }
    public void setKey(Integer key) {
        this.key = key;
    }
    public Appointment.Status getStatus(){
        return this.status;
    }
    public void setStatus(Appointment.Status value){
        this.status = value;
    }       

    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}
