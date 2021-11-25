/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.stores.migration_import_store.CSVStore;
import clinicpms.store.stores.Store;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.stores.IStore;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 *
 * @author colin
 */
public class Appointment {
    public static enum Status{BOOKED,UNBOOKED};
    private Integer key = null;
    private LocalDateTime start = null;
    private Duration duration  = null;
    private String notes = null;
    private Patient patient = null;
    private Category category = null;
    private Status status = Appointment.Status.BOOKED;
    
    public static enum Category{DENTAL, HYGIENE, ALL}

    public Appointment() {
    } //constructor creates a new Appointment record

    /**
     * 
     * @param key 
     */
    public Appointment( int key) {
        this.key = key;
    }
    
    public Appointment create() throws StoreException{
        IStore store = Store.factory();
        return store.create(this);  
    }
    
    public void delete() throws StoreException{
        IStore store = Store.factory();
        store.delete(this);
    }
    
    public Appointment read() throws StoreException{
        IStore store = Store.factory();
        return store.read(this);
    }
    
    public Appointment update() throws StoreException{ 
        IStore store = Store.factory();
        return store.update(this);
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
