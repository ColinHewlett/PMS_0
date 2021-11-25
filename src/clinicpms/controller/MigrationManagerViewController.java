/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.view.type.migration_manager_view.MigrationManagerModalViewer;
import clinicpms.view.View;
import clinicpms.view.DesktopView;
import clinicpms.store.stores.AccessStore;
import clinicpms.store.stores.migration_import_store.CSVStore;
import clinicpms.store.stores.Store;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.stores.IStore;
import clinicpms.store.stores.IMigrationManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import javax.swing.JFrame;

import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class MigrationManagerViewController extends ViewController {
    private MigrationDescriptor migrationDescriptorFromView = null;
    private ActionListener myController = null;
    private View view = null;
    private JFrame owningFrame = null;
    private PropertyChangeSupport pcSupport = null;
    private MigrationDescriptor newMigrationDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    
    /**
     * The current location of app's persistent store (in DbLocation.accdb) used to initialises EntityDescriptor sent to new Migration View
     * -- update 20/11/2021 07:55 provides view with info on target storage type
     * @param controller
     * @param desktopView
     * @throws StoreException 
     */
    public MigrationManagerViewController(DesktopViewController controller, DesktopView desktopView)throws StoreException{
        setMyController(controller);
        this.owningFrame = desktopView;
        pcSupport = new PropertyChangeSupport(this);
        /**
         * 22/11/2021 19:48 update
         * -- replace "AccessStore.getInstance().getTargetsDatabase()" with Store.getTargetsDatabase()
         */
        String targetPath = Store.getTargetsDatabase().read("MIGRATION_DB");
        targetPath = targetPath + ";" + Store.getStorageType().toString(); //20/11/2021 07:55 update
        //String targetPath = AccessStore.getInstance().getDbLocationStore().read();
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().getMigrationDescriptor().getTarget().setData(targetPath);
        
        
        View.setViewer(View.Viewer.MIGRATION_MANAGER_VIEW);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        
        /**
         * This stage in code reached only when the view (a modal JInternalFrame) has been closed
         * -- does the Desktop view controller require notification of this?
         * -- yes: to re-enable both menus in the desktop view as well as the desktop view window closure control  
         */
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
        this.myController.actionPerformed(actionEvent);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        this.view = (View)e.getSource();
        File file = null;
        BufferedWriter bw = null;
        if (e.getActionCommand().equals(
                AppointmentViewControllerActionEvent.
                        APPOINTMENTS_VIEW_CLOSED.toString())){
            /**
             * APPOINTMENTS_VIEW_CLOSED
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);   
        }
        else if (e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.DISABLE_DESKTOP_CONTROLS_REQUEST.toString())){
        
        }
        else if (e.getActionCommand().equals(
                ViewController.MigratorViewControllerActionEvent.
                        APPOINTMENT_MIGRATOR_REQUEST.toString())){
            /**
             * APPOINTMENT_MIGRATOR_REQUEST
             * -- the view's MigrationDescriptor is fetched which defines required migration action and input values for this
             * --
             */
            setEntityDescriptorFromView(getView().getEntityDescriptor());
            String path = getEntityDescriptorFromView().getMigrationDescriptor().getTarget().getData();
            try{
                
                initialiseMigrationSettings();
               
                /**
                 * Store factory returns the database driver  selected by a command line value
                 */
                IStore store = Store.factory(); 
                IMigrationManager  manager = store.getMigrationManager();
                this.doSelectedDataMigrationAction(
                        getEntityDescriptorFromView().getMigrationDescriptor().getMigrationViewRequest(), manager);
                
                /**
                 * removal followed by adding the propertyChangeListener is necessary
                 * -- successive additions of the propertyChangeListener, without first removing the in place propertyChangeListener, results in successive firing of the propertyChangeEvent; on for each time the same listener is added
                 */
                pcSupport.removePropertyChangeListener(this.view);
                pcSupport.addPropertyChangeListener(this.view);
                PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                    MigrationViewPropertyChangeEvents.MIGRATION_ACTION_COMPLETE.toString(),
                    null,getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
            }
            catch (IOException ex){
               JOptionPane.showMessageDialog(null,
                                          new ErrorMessagePanel(ex.getMessage())); 

            }
            catch (StoreException ex){
                    JOptionPane.showMessageDialog(null,
                                              new ErrorMessagePanel(ex.getMessage()));
            }
        }
    }
   
    private EntityDescriptor getNewEntityDescriptor(){
        return this.newEntityDescriptor;
    }
    private void setNewEntityDescriptor(EntityDescriptor value){
        this.newEntityDescriptor = value;
    }
    private EntityDescriptor getOldEntityDescriptor(){
        return this.oldEntityDescriptor;
    }
    private void setOldEntityDescriptor(EntityDescriptor e){
        this.oldEntityDescriptor = e;
    }
    
    public EntityDescriptor getEntityDescriptorFromView(){
        return this.entityDescriptorFromView;
    }
    
    public MigrationDescriptor getMigrationDescriptorFromView(){
        return this.migrationDescriptorFromView;
    }
    private void setEntityDescriptorFromView(EntityDescriptor e){
        this.entityDescriptorFromView = e;
    }
    
    public MigrationDescriptor getNewMigrationDescriptor(){
        return this.newMigrationDescriptor;
    }
    private void setNewMigrationDescriptor(MigrationDescriptor e){
        this.newMigrationDescriptor = e;
    }
    
    public View getView( ){
        return view;
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    
    /**
     * method performs execution of one of the following actions selected by the received MigrationViewRequest
     * -- MIGRATE_APPOINTMENTS_TO_DATABASE -> creates new Appointment table in selected database, populated by Appointment records derived from the specified appointments CSV file
     * -- MIGRATE_PATIENTS_TO_DATABASE -> creates new Patient table in selected database, populated by Patient records derived from the specified patients CSV file
     * -- REMOVE_BAD_APPOINTMENTS_FROM_DATABASE -> removes from Appointment table records which refer to a patient record (appointee) which does not exist in the Patient table
     * -- TIDY_PATIENT_DATA_IN_DATABASE -> tidies up existing data in Patient table (appropriate upper/lower case conversions, normalised gender labels etc)
     * each action(s) is timed and resulting records from operation (patient or appointment records) counted
     * a new migration descriptor is initialised with the following values for reference by the view
     * -- the path of the appointment CSV source file
     * -- the path of the patient CSV source file
     * -- the path of the target database
     * -- the currently selected MigrationViewRequest value 
     * -- the duration of the requested operation (action)
     * -- the number of records counted after the operation (action); either appointment or patient records in database
     * @param mvr, MigrationViewRequest switch expression which selects action(s) to perform by the configured MigrationManager 
     * @param manager, IMigrationManager responsible for execution of selected migration action(s)
     */
    private void doSelectedDataMigrationAction(ViewController.MigrationViewRequest mvr, IMigrationManager manager){
        Instant start;
        Instant end;
        Duration duration;
        try{
            switch(mvr){
                case MIGRATE_APPOINTMENTS_TO_DATABASE:{
                    start = Instant.now();
                    ArrayList<Appointment> appointments = CSVStore.migrateAppointments();
                    manager.setAppointments(appointments);
                    manager.action(Store.MigrationMethod.APPOINTMENT_TABLE_DROP);
                    manager.action(Store.MigrationMethod.APPOINTMENT_TABLE_CREATE);
                    manager.action(Store.MigrationMethod.APPOINTMENT_TABLE_POPULATE); 
                    manager.action(Store.MigrationMethod.APPOINTMENT_START_TIMES_NORMALISED);
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration);
                    //setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewEntityDescriptor().getMigrationDescriptor().setAppointmentsCount(manager.getAppointmentCount());
                    break;
                }
                case MIGRATE_PATIENTS_TO_DATABASE:{
                    start = Instant.now();
                    ArrayList<Patient> patients = CSVStore.migratePatients();
                    manager.setPatients(patients);
                    manager.action(Store.MigrationMethod.PATIENT_TABLE_DROP);
                    manager.action(Store.MigrationMethod.PATIENT_TABLE_CREATE);
                    manager.action(Store.MigrationMethod.PATIENT_TABLE_POPULATE);
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    //setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration);
                    getNewEntityDescriptor().getMigrationDescriptor().setPatientsCount(manager.getPatientCount());
                    break;
                }
                case REMOVE_BAD_APPOINTMENTS_FROM_DATABASE:{
                    start = Instant.now();
                    manager.action(Store.MigrationMethod.APPOINTMENT_TABLE_INTEGRITY_CHECK);
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    //setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration); 
                    getNewEntityDescriptor().getMigrationDescriptor().setAppointmentsCount(manager.getAppointmentCount());
                    
/*
       MigrationDescriptor result = new MigrationDescriptor();
       result.getAppointment().setData(getMigrationDescriptorFromView().getAppointment().getData());
       result.getPatient().setData(getMigrationDescriptorFromView().getPatient().getData());
       result.setTarget(getMigrationDescriptorFromView().getTarget());
       result.setMigrationViewRequest(getMigrationDescriptorFromView().getMigrationViewRequest());
                    */

                    break;
                }
                case TIDY_PATIENT_DATA_IN_DATABASE:{
                    start = Instant.now();
                    manager.action(Store.MigrationMethod.PATIENT_TABLE_TIDY);
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    //setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration); 
                    break;
                }
            }
        }
        catch (StoreException ex){
            JOptionPane.showMessageDialog(null,
                                      new ErrorMessagePanel(ex.getMessage()));
        }
       
    }
    
    /**
     * initialiseMigrationSettings() fetchs from the view's migration descriptor 
     * -- the appointment source csv file path; this is sent to the CSVStore class responsible for the conversion to appointment objects from the CSV file
     * -- the patient source csv file; this is sent to the CSVStore class responsible for handling the conversion to patient objects from the CSV file
     * -- the target database file path; this is used to create a new file containing the path in a defined location local to the compiled program
     * 
     * @throws IOException from an abortive attempt to create a file which defines the database path
     */
    private void initialiseMigrationSettings()throws IOException{
        File file;
        BufferedWriter bw;
        CSVStore.setAppointmentCSVPath(
                this.getEntityDescriptorFromView().getMigrationDescriptor().getAppointment().getData());
        CSVStore.setPatientCSVPath(this.getEntityDescriptorFromView().getMigrationDescriptor().getPatient().getData());

    }

}
 