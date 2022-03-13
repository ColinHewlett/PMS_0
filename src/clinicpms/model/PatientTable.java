/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IMigrationStoreAction;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public class PatientTable extends ArrayList<Patient> implements ITable,
                                                                IEntityStoreType{
    private int count;
    
    public boolean isAppointment(){
        return false;
    }
    
    public boolean isAppointmentDate(){
        return false;
    }
    
    public boolean isAppointments(){
        return false;
    }
    
    public boolean isAppointmentTable(){
        return false;
    }
    
    public boolean isAppointmentTableRowValue(){
        return false;
    }
    
    public boolean isPatient(){
        return false;
    }
    
    public boolean isPatients(){
        return false;
    }
    
    public boolean isPatientTable(){
        return true;
    }
    
    public boolean isPatientTableRowValue(){
        return false;
    }
     
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    @Override
    public void create() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);   
        store.create(this);
    }
    
    @Override
    public void drop()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.drop(this);
    }
    
    @Override
    public void exportToPMS() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.exportToPMS(new Patients());
    }
    
    //05/03/2022 20:09
    @Override
    public void importFromCSV()throws StoreException{
        IEntityStoreType entity = null;
        IMigrationStoreAction store = Store.FACTORY(this);
        this.clear();
        entity = store.importFromCSV(this);
        if (entity.isPatientTable()) this.addAll((PatientTable)entity);
    }
    
    public void insert()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.insert(this);  
    }
    /*
    05/03/2022 20:09
    @Override
    public void populate() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.populate(this);
    } 
    */
    
    @Override
    public IEntityStoreType read()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    public Integer count() {
        Integer result = null;
        IEntityStoreType entity = null;
        Integer rowcount = null;
        try{
            IMigrationStoreAction store = Store.FACTORY(this);
            result = store.countRowsIn(this);
        }catch (StoreException ex){
            /**
             * if MigrationSQL.PATIENT_TABLE_ROW_COUNT is source of exception
             * -- assumed this is cause because the AppointmentTable is currently missing from the database schema 
             */
            if (!ex.getErrorType().equals(StoreException.ExceptionType.PATIENT_TABLE_MISSING_IN_MIGRATION_DATABASE)){
                //throw new StoreException(ex.getMessage(), ex.getErrorType());
                result = null;
            }
        }
        return result;
    }
}
