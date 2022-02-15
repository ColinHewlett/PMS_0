/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import static clinicpms.controller.ViewController.displayErrorMessage;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.AppointmentTable;
import clinicpms.view.View;
import clinicpms.view.DesktopView;
import clinicpms.model.StoreManager;
import clinicpms.model.Appointment;
import clinicpms.model.Appointments;
import clinicpms.model.Patients;
import clinicpms.model.Patient;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.store.StoreException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.time.Duration;
import java.time.Instant;

import javax.swing.JFrame;

import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class MigrationManagerViewController extends ViewController {
    //private MigrationDescriptorx migrationDescriptorFromView = null;
    private ActionListener myController = null;
    private View view = null;
    private JFrame owningFrame = null;
    private PropertyChangeSupport pcSupport = null;
    //private MigrationDescriptorx newMigrationDescriptor = null;
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
        StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
        String migrationTargetPath = storeManager.getMigrationTargetStorePath();
        String targetPath = storeManager.getMigrationTargetStorePath();
        String appointmentCSVFilePath = storeManager.getAppointmentCSVPath();
        String patientCSVFilePath = storeManager.getPatientCSVPath();
        targetPath = targetPath + ";" + storeManager.getStorageType().toString(); //20/11/2021 07:55 update
        //String targetPath = AccessStore.getInstance().getDbLocationStore().read();
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().getMigrationDescriptor().getTarget().setData(targetPath);
        getNewEntityDescriptor().getMigrationDescriptor().setAppointmentCSVFilePath(appointmentCSVFilePath);
        getNewEntityDescriptor().getMigrationDescriptor().setPatientCSVFilePath(patientCSVFilePath);
        getNewEntityDescriptor().getMigrationDescriptor().setMigrationDatabaseSelection(migrationTargetPath);
        
        View.setViewer(View.Viewer.MIGRATION_MANAGER_VIEW);
        getNewEntityDescriptor().getMigrationDescriptor().
                setAppointmentTableCount(new AppointmentTable().count());
        Integer rows = new PatientTable().count();
        getNewEntityDescriptor().getMigrationDescriptor().
        setPatientTableCount(rows);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        
        /**
         * This stage in code reached only when the view (a modal JInternalFrame) has been closed
         * -- does the Desktop view controller require notification of this?
         * -- yes: to re-enable both menus in the desktop view as well as the desktop view window closure control  
         */
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
        this.myController.actionPerformed(actionEvent);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        this.view = (View)e.getSource();
        File file = null;
        BufferedWriter bw = null;
        if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.
                        APPOINTMENTS_VIEW_CLOSED.toString())){
            /**
             * APPOINTMENTS_VIEW_CLOSED
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);   
        }
        else if (e.getActionCommand().equals(
               DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){
        
        }
        else if (e.getActionCommand().equals(
            EntityDescriptor.MigratorViewControllerActionEvent.
                    EXPORT_MIGRATED_DATA_TO_PMS_REQUEST.toString())){
            /**
             * EXPORT_MIGRATED_DATA_TO_PMS_REQUEST store action
             * -- table dropped if it already exists in pms database
             * -- because jackaccess driver s'ware does not replace existing table
             * -- instead creates another similarly named table with the suffix '2'
             */
            try{
                new Appointment().drop();
                new Patient().drop();
                new SurgeryDaysAssignment().drop();
                new AppointmentTable().exportToPMS();
                new PatientTable().exportToPMS();
                new SurgeryDaysAssignmentTable().exportToPMS();
            }catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"MigrationManagerViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.MigratorViewControllerActionEvent.
                        APPOINTMENT_MIGRATOR_REQUEST.toString())){
            
            /**
             * APPOINTMENT_MIGRATOR_REQUEST
             * -- the view's MigrationDescriptor is fetched which defines required migration action and input values for this
             * --
             */
            setEntityDescriptorFromView(getView().getEntityDescriptor());
            String path = getEntityDescriptorFromView().getMigrationDescriptor().getTarget().getData();
            try{
                StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
                //initialiseMigrationSettings();
                storeManager.setAppointmentCSVPath(
                this.getEntityDescriptorFromView().getMigrationDescriptor().getAppointmentCSVFilePath());
                storeManager.setPatientCSVPath(
                this.getEntityDescriptorFromView().getMigrationDescriptor().getPatientCSVFilePath());
                doSelectedDataMigrationAction(
                        getEntityDescriptorFromView().getMigrationDescriptor().getMigrationViewRequest());
                
                /**
                 * removal followed by adding the propertyChangeListener is necessary
                 * -- successive additions of the propertyChangeListener, without first removing the in place propertyChangeListener, results in successive firing of the propertyChangeEvent; on for each time the same listener is added
                 */
                pcSupport.removePropertyChangeListener(this.view);
                pcSupport.addPropertyChangeListener(this.view);
                PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.MigrationViewPropertyChangeEvents.MIGRATION_ACTION_COMPLETE.toString(),
                    null,getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
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
    
    /*
    public MigrationDescriptorx getMigrationDescriptorFromView(){
        return this.migrationDescriptorFromView;
    }
    */
    
    private void setEntityDescriptorFromView(EntityDescriptor e){
        this.entityDescriptorFromView = e;
    }
    
    /*
    public MigrationDescriptorx getNewMigrationDescriptor(){
        return this.newMigrationDescriptor;
    }
    private void setNewMigrationDescriptor(MigrationDescriptorx e){
        this.newMigrationDescriptor = e;
    }
    */
    
    public View getView( ){
        return view;
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    
    private void doSelectedDataMigrationAction(EntityDescriptor.MigrationViewRequest mvr){
        Instant start;
        Instant end;
        Duration duration;
        AppointmentTable appointmentTable = null;
        PatientTable patientTable = null;
        SurgeryDaysAssignmentTable surgeryDaysTable = null;
        ;
        try{
            switch(mvr){
                case POPULATE_APPOINTMENT_TABLE:{
                    start = Instant.now();

                    appointmentTable = new AppointmentTable();
                    appointmentTable.drop();
                    appointmentTable.create();
                    appointmentTable.populate();

                    end = Instant.now();
                    duration = Duration.between(start, end);
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration);
                    //setNewMigrationDescriptor(createNewMigrationDescriptor());
                    getNewEntityDescriptor().getMigrationDescriptor().setAppointmentTableCount(new AppointmentTable().count());
                    break;
                }
                case POPULATE_PATIENT_TABLE:{
                    start = Instant.now();
                    
                    /**
                     * PatientTable cannot be dropped if AppointmentTable exists
                     * -- because of the one to many relationship between the two
                     */
                    appointmentTable = new AppointmentTable();
                    appointmentTable.drop();
                    patientTable = new PatientTable();
                    patientTable.drop();
                    patientTable.create();
                    patientTable.populate();
                    
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration);
                    getNewEntityDescriptor().getMigrationDescriptor().setPatientTableCount(new PatientTable().count());
                    break;
                }
                case REMOVE_BAD_APPOINTMENTS_FROM_DATABASE:{
                    start = Instant.now();
                    //manager.action(Store.MigrationMethod.APPOINTMENT_TABLE_INTEGRITY_CHECK);
                    appointmentTable = new AppointmentTable();
                    appointmentTable.checkIntegrity();
                    end = Instant.now();
                    duration = Duration.between(start, end);
                    getNewEntityDescriptor().getMigrationDescriptor().setMigrationActionDuration(duration); 
                    getNewEntityDescriptor().getMigrationDescriptor().setAppointmentTableCount(new AppointmentTable().count());
                    break;
                }        
            }
        }
        catch (StoreException ex){
            //displayErrorMessage(ex.getMessage(),"MigrationManagerViewController error",JOptionPane.WARNING_MESSAGE);
            JOptionPane.showMessageDialog(null,new ErrorMessagePanel(ex.getMessage()));
        }
        
       
    }
    
    

}
 