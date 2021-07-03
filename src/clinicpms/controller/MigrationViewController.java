/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.view.MigrationModalViewer;
import clinicpms.view.View;
import clinicpms.view.base.DesktopView;
import clinicpms.store.AccessStore;
import clinicpms.store.CSVStore;
import clinicpms.store.Store;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.interfaces.IStore;
import clinicpms.store.interfaces.IMigrationManager;
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
public class MigrationViewController extends ViewController {
    private MigrationDescriptor migrationDescriptorFromView = null;
    private ActionListener myController = null;
    private View view = null;
    private JFrame owningFrame = null;
    private PropertyChangeSupport pcSupport = null;
    private MigrationDescriptor newMigrationDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    
    public MigrationViewController(DesktopViewController controller, DesktopView desktopView)throws StoreException{
        setMyController(controller);
        this.owningFrame = desktopView;
        pcSupport = new PropertyChangeSupport(this);
        String targetPath = AccessStore.getInstance().getDbLocationStore().read();
        MigrationDescriptor md = new MigrationDescriptor();
        md.getTarget().setData(targetPath);
        View.setViewer(View.Viewer.APPOINTMENT_SCHEDULE_VIEW);
            this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        
        /*
        pcSupport.addPropertyChangeListener(view);
        //centre appointments view relative to desktop;
        super.centreViewOnDesktop(desktopView, view);
        this.view.addInternalFrameClosingListener(); 
        this.view.initialiseView();
        */
    }
    
    /**
     * Executes requested action from view
     * -- APPOINTMENTS_VIEW_CLOSED
     * ----> sends VIEW_CLOSED_NOTIFICATION action to the DesktopViewController
     * -- APPOINTMENT_MIGRATOR_REQUEST
     * ----> the view's MigrationDescriptor is fetched which defines required migration action and input values sent by view for this
     * ----> initiates execution of requested action via the factory creation of an appropriate IMigrationManger in the Store package 
     * ----> returns on completion a newly initialised MigrationDescriptor with the results of the operation via a PropertyChangeSupport firePropertyChange message
     * Catches and handles both IOException and StoreException that might arise
     * @param e, ActionEvent 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
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
                ViewController.MigratorViewControllerActionEvent.
                        APPOINTMENT_MIGRATOR_REQUEST.toString())){
            /**
             * APPOINTMENT_MIGRATOR_REQUEST
             * -- the view's MigrationDescriptor is fetched which defines required migration action and input values for this
             * --
             */
            setEntityDescriptorFromView(getView().getEntityDescriptor());
            String path = getMigrationDescriptorFromView().getTarget().getData();
            try{
                
                initialiseMigrationSettings();
                /**
                 * Static call to AccessStore which initialise its DbLocation database (inner class) with the selected target database path for the app
                 */
                AccessStore.getInstance().getDbLocationStore().update(getMigrationDescriptorFromView().getTarget().getData());
                /**
                 * Store factory returns the database driver  selected by a command line value
                 */
                IStore store = Store.factory(); 
                IMigrationManager  manager = store.getMigrationManager();
                this.doSelectedDataMigrationAction(
                        getMigrationDescriptorFromView().getMigrationViewRequest(), manager);
                PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                    MigrationViewPropertyChangeEvents.MIGRATION_ACTION_COMPLETE.toString(),
                    getMigrationDescriptorFromView(),getNewMigrationDescriptor());
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
                    setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewMigrationDescriptor().setMigrationActionDuration(duration);
                    getNewMigrationDescriptor().setAppointmentsCount(manager.getAppointmentCount());
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
                    setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewMigrationDescriptor().setMigrationActionDuration(duration);
                    getNewMigrationDescriptor().setPatientsCount(manager.getPatientCount());
                    break;
                }
                case REMOVE_BAD_APPOINTMENTS_FROM_DATABASE:{
                    start = Instant.now();
                    manager.action(Store.MigrationMethod.APPOINTMENT_TABLE_INTEGRITY_CHECK);
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewMigrationDescriptor().setMigrationActionDuration(duration); 
                    getNewMigrationDescriptor().setAppointmentsCount(manager.getAppointmentCount());
                    break;
                }
                case TIDY_PATIENT_DATA_IN_DATABASE:{
                    start = Instant.now();
                    manager.action(Store.MigrationMethod.PATIENT_TABLE_TIDY);
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewMigrationDescriptor().setMigrationActionDuration(duration); 
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
                this.getMigrationDescriptorFromView().getAppointment().getData());
        CSVStore.setPatientCSVPath(this.getMigrationDescriptorFromView().getPatient().getData());
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        file = new File ("database/databasePath.txt");
        if (file.exists()){
            file.delete();
        }
        if (file.createNewFile()){
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(getMigrationDescriptorFromView().getTarget().getData());
            bw.close();
        }
    }
    
    private MigrationDescriptor createNewMigrationDescriptor(){
       MigrationDescriptor result = new MigrationDescriptor();
       result.getAppointment().setData(getMigrationDescriptorFromView().getAppointment().getData());
       result.getPatient().setData(getMigrationDescriptorFromView().getPatient().getData());
       result.setTarget(getMigrationDescriptorFromView().getTarget());
       result.setMigrationViewRequest(getMigrationDescriptorFromView().getMigrationViewRequest());
       return result;
    }
}
 