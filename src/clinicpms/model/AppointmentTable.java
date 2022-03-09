/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IMigrationStoreAction;
import clinicpms.store.IPMSStoreAction;
import clinicpms.model.IEntityStoreType;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public class AppointmentTable extends ArrayList<Appointment>implements ITable,
                                                                        IEntityStoreType{
    private int count;
    private Integer key = null;
    
    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public boolean isAppointments(){
        return false;
    }
    
    @Override
    public boolean isAppointmentTable(){
        return true;
    }
    
    @Override
    public boolean isPatientTable(){
        return false;
    }
    
    @Override
    public boolean isAppointmentTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isPatient(){
        return false;
    }
    
    @Override
    public boolean isPatients(){
        return false;
    }
    
    @Override
    public boolean isPatientTableRowValue(){
        return false;
    }
     
    @Override
    public boolean isSurgeryDaysAssignment(){
        return false;
    } 
            
    public Integer count() throws StoreException{
        Integer result = null;
        IEntityStoreType entity = null;
        Integer rowcount = null;
        try{
            IMigrationStoreAction store = Store.FACTORY(this);
            return store.countRowsIn(this);
        }catch (StoreException ex){
            /**
             * if MigrationSQL.APPOINTMENT_TABLE_ROW_COUNT is source of exception
             * -- assumed this is cause because the AppointmentTable is currently missing from the database schema 
             */
            if (!ex.getErrorType().equals(StoreException.ExceptionType.APPOINTMENT_TABLE_MISSING_IN_MIGRATION_DATABASE)){
                //throw new StoreException(ex.getMessage(), ex.getErrorType());
                result = null;
            }
        }
        return result;
         
    }
    
    public void create() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.create(this);
    }
    
    public void drop()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.drop(this);
    }
    
    public void insert()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.insert(this);  
    }
    
    //05/03/2022 20:09
    @Override
    public void importFromCSV()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        this.clear();
        this.addAll((AppointmentTable)store.importFromCSV((IEntityStoreType)this));
    }
    /*
    05/03/2022 20:09
    
    public void populate()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.populate(this);
    }
    */
    
    public IEntityStoreType read() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    public void exportToPMS() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.exportToPMS(new Appointments());
    }
    
    public void checkIntegrity()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.checkIntegrity();
    }
    
    public Integer getKey() {
        return key;
    }
    public void setKey(Integer key) {
        this.key = key;
    }
}
