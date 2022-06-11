/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.util.ArrayList;
import clinicpms.store.IPatientsStoreAction;
import clinicpms.store.IStoreAction;
import java.util.List;

/**
 *
 * @author colin
 */
public class Patients extends ArrayList<Patient> implements IPatients, IEntityStoreType{
    
    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isAppointments(){
        return false;
    }
    
    @Override
    public boolean isAppointmentTable(){
        return false;
    }
    
    @Override
    public boolean isPatientTable(){
        return false;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public final boolean isAppointmentTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isPatients(){
        return true;
    }
    
    @Override
    public final boolean isPatientTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    @Override
    public void insert() throws StoreException{
        IPatientsStoreAction store = Store.FACTORY(this);
        store.insert(this);
    }
    
    @Override
    public void read() throws StoreException{
        IPatientsStoreAction store = Store.FACTORY(new Patients());
        clear();
        addAll(store.readPatients());
    }
    
    public Integer count()throws StoreException{
    Integer result = null;
        try{
            IPatientsStoreAction store = Store.FACTORY(new Patients());
            result = store.countRowsIn(this);
        }catch (StoreException ex){
            /**
             * if MigrationSQL.PATIENT_TABLE_ROW_COUNT is source of exception
             * -- assumed this is cause because the AppointmentTable is currently missing from the database schema 
             */
            if (!ex.getErrorType().equals(StoreException.ExceptionType.PATIENT_TABLE_MISSING_IN_MIGRATION_DATABASE)){
                //throw new StoreException(ex.getMessage(), ex.getErrorType());
                return null;
            }
        }
        return result;
    }
    
    
}
