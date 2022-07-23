/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import static clinicpms.controller.ViewController.displayErrorMessage;
import org.apache.commons.io.FilenameUtils;

import clinicpms.model.*;
//import com.healthmarketscience.jackcess.DatabaseBuilder;
//import com.healthmarketscience.jackcess.Database;
import clinicpms.store.StoreException;
import clinicpms.view.views.DesktopView;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author colin
 */
public class DesktopViewController extends ViewController{
    private boolean isDesktopPendingClosure = false;
    private DesktopView view = null;
    private ArrayList<PatientNotificationViewController> patientNotificationViewControllers = null;
    private ArrayList<AppointmentScheduleViewController> appointmentViewControllers = null;
    private ArrayList<PatientViewController> patientViewControllers = null;
    private ArrayList<ImportProgressViewController> importExportProgressViewControllers = null;
    private static Boolean isDataMigrationOptionEnabled = null;
    private PropertyChangeSupport pcSupport = null;
    private EntityDescriptor entityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private int count = 0;
    private int recordCount = 0;
   
    //private HashMap<ViewControllers,ArrayList<ViewController>> viewControllers = null;    
    enum ViewControllers {
                            PATIENT_VIEW_CONTROLLER,
                            APPOINTMENT_VIEW_CONTROLLER,
                            MIGRATION_VIEW_CONTROLLER
                         }
    
    public enum DesktopViewControllerActionEvent {
                                            EXPORT_PROGRESS_VIEW_CONTROLLER_REQUEST,
                                            IMPORT_DATA_FROM_SOURCE,
                                            EXPORT_MIGRATED_DATA,
                                            IMPORT_EXPORT_PATIENT_DATA_COMPLETED,
                                            IMPORT_EXPORT_APPOINTMENT_DATA_COMPLETED,
                                            IMPORT_EXPORT_APPOINTMENT_DATA,
                                            IMPORT_EXPORT_PATIENT_DATA,
                                            IMPORT_EXPORT_MIGRATED_SURGERY_DAYS_ASSIGNMENT,
                                            EXPORT_MIGRATED_SURGERY_DAYS_ASSIGNMENT_COMPLETED,
                                            APPOINTMENT_TABLE_COUNT_REQUEST,
                                            PATIENT_NOTIFICATION_TABLE_COUNT_REQUEST,
                                            PATIENT_TABLE_COUNT_REQUEST,
                                            SURGERY_DAYS_ASSIGNMENT_TABLE_COUNT_REQUEST,
                                            APPOINTMENT_HISTORY_CHANGE_NOTIFICATION,
                                            APPOINTMENT_VIEW_CONTROLLER_REQUEST,
                                            DATABASE_LOCATOR_REQUEST,
                                            MODAL_VIEWER_ACTIVATED,
                                            MODAL_VIEWER_CLOSED,
                                            MIGRATION_VIEW_CONTROLLER_REQUEST,
                                            PATIENT_VIEW_CONTROLLER_REQUEST,
                                            PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST,
                                            PATIENT_NOTIFICATION_VIEW_CONTROLLER_REQUEST,
                                            GET_APPOINTMENT_CSV_PATH_REQUEST,
                                            GET_PATIENT_CSV_PATH_REQUEST,
                                            GET_PMS_STORE_PATH_REQUEST,
                                            SET_APPOINTMENT_CSV_PATH_REQUEST,
                                            SET_PATIENT_CSV_PATH_REQUEST,
                                            NON_PMS_STORE_CREATION_REQUEST,
                                            NON_PMS_STORE_DELETION_REQUEST,
                                            PMS_STORE_COPY_REQUEST,
                                            PMS_STORE_CREATION_REQUEST,
                                            PMS_STORE_DELETION_REQUEST,
                                            PMS_STORE_SELECTION_REQUEST,
                                            //PMS_DATABASE_CREATION_REQUEST,
                                            //PMS_DATABASE_DELETION_REQUEST,
                                            PMS_STORE_RENAME_REQUEST,
                                            //PMS_DATABASE_SELECTION_REQUEST,
                                            SURGERY_DATES_EDITOR_VIEW_CONTROLLER_REQUEST,
                                            VIEW_CLOSE_REQUEST,//raised by Desktop view
                                            VIEW_CLOSED_NOTIFICATION,//raised by internal frame views
                                            DESKTOP_VIEW_INCLUDES_MIGRATION_MANAGEMENT
                                            }
    
    public enum DesktopViewControllerPropertyChangeEvent {
                                            DISABLE_DESKTOP_DATA_CONTROL,
                                            DISABLE_DESKTOP_VIEW_CONTROL,
                                            ENABLE_DESKTOP_DATA_CONTROL,
                                            ENABLE_DESKTOP_VIEW_CONTROL,
                                            DISABLE_DESKTOP_WINDOW_CONTROL,
                                            ENABLE_DESKTOP_WINDOW_CONTROL,
                                            MIGRATION_ACTION_COMPLETE,
                                            APPOINTMENT_CSV_PATH_RECEIVED,
                                            APPOINTMENT_TABLE_COUNT_RECEIVED,
                                            PATIENT_TABLE_COUNT_RECEIVED,
                                            PATIENT_CSV_PATH_RECEIVED,
                                            PATIENT_NOTIFICATION_TABLE_COUNT_RECEIVED,
                                            PMS_STORE_PATH_RECEIVED,
                                            SURGERY_DAYS_ASSIGNMENT_TABLE_COUNT_RECEIVED
                                            }
    
    @Override
    public EntityDescriptor getEntityDescriptorFromView(){
        return this.entityDescriptorFromView;
    }
    
    private void setEntityDescriptorFromView(EntityDescriptor value){
        this.entityDescriptorFromView =  value;
    }
    
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor =  value;
    }
    
    private Boolean getDataMigrationOption(){
        return isDataMigrationOptionEnabled;
    }
    private void setDataMigrationOption(Boolean value){
        isDataMigrationOptionEnabled = value;
    }
            
    
    private DesktopViewController(){
        setEntityDescriptor(new EntityDescriptor());
        /**
         * Constructor for DesktopView takes two arguments
         * -- object reference to view controller (this)
         * -- Boolean signifying whether view enables data migration functions
         */
        view = new DesktopView(this, isDataMigrationOptionEnabled, getEntityDescriptor() );
        view.setSize(1020, 650);
        view.setVisible(true);
        setView(view);
        //view.setContentPane(view);
        pcSupport = new PropertyChangeSupport(this);
        appointmentViewControllers = new ArrayList<>();
        patientViewControllers = new ArrayList<>();
        importExportProgressViewControllers = new ArrayList<>();
        patientNotificationViewControllers = new ArrayList<>();
        
        boolean isPMSStoreDefined;
        if (isDataMigrationOptionEnabled) {
            notifyMigrationActionCompleted();
        }
        else{
            try{
                StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
                String pmsStorePath = storeManager.getPMSStorePath();
                if (pmsStorePath == null) isPMSStoreDefined = false;
                else if (FilenameUtils.getName(pmsStorePath).equals(""))
                    isPMSStoreDefined = false;
                else isPMSStoreDefined = true;
                if (!isPMSStoreDefined){
                    displayErrorMessage("A PMS store has not been defined; ClinicPMS will abort\n"
                            + "Re-enter application in data migration mode by specifying DATA_MIGRATION_ENABLED on the command line",
                            "Desktop View Controller error", JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }
            }catch (StoreException ex){
                displayErrorMessage(ex.getMessage() + "\n"
                        + "Exception handled in Desktop View Controller constructor",
                        "Desktop View Controller error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        String s;
        s = e.getSource().getClass().getSimpleName();
        switch(s){
            case "DesktopView":
                doDesktopViewAction(e);
                 break;
            case "AppointmentScheduleViewController":
                doAppointmentViewControllerAction(e);
                break;
            case "PatientViewController":
                doPatientViewControllerAction(e);
                break;
            case "ImportProgressViewController":
                doImportProgressViewControllerAction(e);
                break;
                /*
            case "MigrationManagerViewController":
                doMigrationManagerViewControllerAction(e);*/
        }
    }
    
    /**
     * ActionEvent responder; action events sent by an ActionViewController include
     * -- APPOINTMENT_HISTORY_CHANGE_NOTIFICATION
     * -- DISABLE_DESKTOP_CONTROLS_REQUEST
     * -- ENABLE_DESKTOP_CONTROLS_REQUEST
     * -- VIEW_CLOSED_NOTIFICATION
     * @param e:ActionEvent received; indicates which ActionCommand from above list was sent
     */
    private void doAppointmentViewControllerAction(ActionEvent e){
        AppointmentScheduleViewController avc = null;
        if (e.getActionCommand().equals(
            DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            /**
             * on reception of VIEW_CLOSED_NOTIFICATION action command
             * -- loops through the active appointment view controllers to find the ActionEvent sender
             * -- attempts to remove this controller from the collection of active controllers; displays error if unable to remove controller
             */
            Iterator<AppointmentScheduleViewController> viewControllerIterator = 
                    this.appointmentViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                avc = viewControllerIterator.next();
                if (avc.equals(e.getSource())){
                    break;
                }
            }
            if (!this.appointmentViewControllers.remove(avc)){
                String message = "Could not find AppointmentViewController in "
                                        + "DesktopViewController collection.";
                displayErrorMessage(message,"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
                */
            }
            else{
                /**
                 * after successfully removing the specified controller and view
                 * -- check to see if Desktop view is waiting to be closed; and continue closure of other controllers and views if so
                 * -- if at this stage there are no appointment or patient view controllers active, re-enable the DesktopView DATA menu and its window close control
                 */
                if (this.isDesktopPendingClosure){
                    this.requestViewControllersToCloseViews();
                }
                
                if (this.appointmentViewControllers.isEmpty() && 
                        this.patientViewControllers.isEmpty()){ 

                }
            }
            
        }
        else if (e.getActionCommand().equals(
                DesktopViewControllerActionEvent.
                        APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString())){
            /**
             * on entry DeskTop View Controller
             * -- knows the appointment view controller's EntityDescriptorFromView stores the newly created or updated appointee object
             * -- desktop view controller can check if any active patient view controllers refer to the same appointee
             * -- if so: controller sends them an APPOINTMENT_HISTORY_CHANGE_NOTIFICATION to refresh their appointment history
             */
            
            /*
            EntityDescriptor edOfPatientWithAppointmentHistoryChange = 
                    ((AppointmentScheduleViewController)e.getSource()).getEntityDescriptorFromView();
            */
            EntityDescriptor edOfPatientWithAppointmentHistoryChange = 
                    ((AppointmentScheduleViewController)e.getSource()).getEntityDescriptorFromView();
            TheAppointment a = edOfPatientWithAppointmentHistoryChange.getRequest().getTheAppointment();
            //21/07/2022 08:39
            //int k2 = edOfPatientWithAppointmentHistoryChange.getTheAppointment().getPatient().getKey();
            //ThePatient patient = edOfPatientWithAppointmentHistoryChange.getRequest().getTheAppointment().getPatient();
            ThePatient patient = edOfPatientWithAppointmentHistoryChange.getTheAppointment().getPatient();
            Iterator<PatientViewController> viewControllerIterator = 
                    this.patientViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                PatientViewController pvc = viewControllerIterator.next();   
                /**
                 * following 2 lines replaced (5/07/2022 8:18)
                 */
                //int k1 = pvc.getEntityDescriptorFromView().getRequest().getPatient().getData().getKey();
                //if (k1==k2){
                //ThePatient patient = new ThePatient(k2);
                //ref 08/07/2022 09:05 if (pvc.getEntityDescriptorFromView().getRequest().getThePatient().equals(patient)){
                if (pvc.getEntityDescriptorFromView().getThePatient().equals(patient)){    
                    /**
                     * Found patient view controller for patient whose appointment history has been changed
                     * -- patient view controller's EntityDescriptor.Request.Patient points to appointee
                     * -- send APPOINTMENT_HISTORY_CHANGE_NOTIFICATION to patient view controller to refresh patient's appointment history
                     */
                    ActionEvent actionEvent = new ActionEvent(
                            this,ActionEvent.ACTION_PERFORMED,
                            DesktopViewControllerActionEvent.APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString());
                    pvc.actionPerformed(actionEvent);
                }
            }  
        }
        else if (e.getActionCommand().equals(
             DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString())){
            
            pcSupport.addPropertyChangeListener(view);
            PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.ENABLE_DESKTOP_VIEW_CONTROL.toString(),
                null,new EntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            
            pcSupport.addPropertyChangeListener(view);
            pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.ENABLE_DESKTOP_WINDOW_CONTROL.toString(),
                null,new EntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            /**
             * on receipt of ENABLE_DESKTOP_CONTROLS_REQUEST
             * -- sends desktop view controller sends message to its view to re-enable VIEW and DATA controls as well as its window closing control
             
            getView().enableViewControl();
            getView().enableWindowCloseControl();
            */
        }
        
            /**
             * on receipt of DISABLE_DESKTOP_CONTROLS_REQUEST
             * -- sends desktop view controller sends message to its view to disable VIEW and DATA controls as well as its window closing control
             
            
            getView().disableViewControl();
            getView().disableWindowClosedControl();
            */
        //}
    }
    private void doPatientViewControllerAction(ActionEvent e){
        PatientViewController pvc = null;
        if (e.getActionCommand().equals(
            DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            Iterator<PatientViewController> viewControllerIterator = 
                    this.patientViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                pvc = viewControllerIterator.next();
                if (pvc.equals(e.getSource())){
                    break;
                }
            }
            if (!this.patientViewControllers.remove(pvc)){
                String message = "Could not find PatientViewController in "
                                        + "DesktopViewController collection.";
                displayErrorMessage(message,"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
                */
            }
            else{
                if (this.isDesktopPendingClosure){
                    this.requestViewControllersToCloseViews();
                }
                if (this.appointmentViewControllers.isEmpty() && 
                        this.patientViewControllers.isEmpty()){ 
                    /**
                     * re-enable view's data menu, if it exists
                     */
                    //getView().enableDataControl();
                    //getView().enableWindowCloseControl();
                }
            }
        }
        
        /**
         * VC receives a request for a new AppointmentVC from a PatientVC
         * -- the PatientVC view's EntityDescriptorFromView object defines an appointment for the selected patient
         * -- the appointment date is used in the construction of a new AppointmentVC and associated appointment schedule view which includes the selected patient's appointment
         */
        else if (e.getActionCommand().equals(
            EntityDescriptor.PatientViewControllerActionEvent.
                    APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString())){
            PatientViewController patientViewController = (PatientViewController)e.getSource();
            Optional<EntityDescriptor> ed = Optional.of(patientViewController.getEntityDescriptorFromView());
            createNewAppointmentViewController(ed);
            
        }
    }
    
    private void doImportProgressViewControllerAction(ActionEvent e){
        DesktopViewControllerActionEvent actionCommand =
                DesktopViewControllerActionEvent.valueOf(e.getActionCommand());
        
        switch (actionCommand){
            case VIEW_CLOSED_NOTIFICATION:
                importExportProgressViewControllers.clear();
                notifyMigrationActionCompleted();
                break;
                
            case IMPORT_EXPORT_PATIENT_DATA:
                try{
                    if (PMSStore.isSelected()) 
                        startBackgroundThread(new ThePatient(), this);
                }catch (StoreException ex){
                    displayErrorMessage(ex.getMessage() + "\nException handled"
                            + " in case EXPORT_MIGRATED_PATIENTS inside "
                            + "doExportProgressViewControllerAction()",
                            "Desktop View Controller error",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;
                
            case IMPORT_EXPORT_APPOINTMENT_DATA:
                try{
                    if (PMSStore.isSelected()) 
                        startBackgroundThread(new TheAppointment(), this);
                }catch (StoreException ex){
                    displayErrorMessage(ex.getMessage() + "\nException handled"
                            + " in case EXPORT_MIGRATED_APPOINTMENTS inside "
                            + "doImportExportProgressViewControllerAction()",
                            "Desktop View Controller error",
                            JOptionPane.WARNING_MESSAGE);    
                }
                break;
        }
    }

       
    
    private void doMigrationManagerViewControllerAction(ActionEvent e){
        /**
         * VIEW_CLOSED_NOTIFICATION -> 
         * -- on closure of migration view controller re- enable both "View" and "Data" menus in the desktop view
         * -- also re-enable the desktop view window closure control ("X")
         */
        if (e.getActionCommand().equals(
            DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            //getView().enableViewControl();
            //getView().enableDataControl();
            //getView().enableWindowCloseControl();
        }
        
    }
    
    /*
    private void doDatabaseLocatorViewControllerAction(ActionEvent e){
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            if (databaseLocatorViewControllers.size()!=1){
                String message = "Unexpected error: more or less than a single database locator view controller located";
                displayErrorMessage(message,"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
            }
            this.databaseLocatorViewControllers.remove(0);
            
        }
    }
    */
    /**
     * 
     * @param e source of event is DesktopView object
     */
    private void doDesktopViewAction(ActionEvent e){ 
        Integer count = null;
        DesktopViewControllerActionEvent actionCommand =
                DesktopViewController.DesktopViewControllerActionEvent.valueOf(e.getActionCommand());
        switch (actionCommand){
            case GET_APPOINTMENT_CSV_PATH_REQUEST:
                doGetPathRequest(actionCommand, e.getSource());
                break;
            case GET_PATIENT_CSV_PATH_REQUEST:
                doGetPathRequest(actionCommand, e.getSource());
                break;
            case GET_PMS_STORE_PATH_REQUEST:
                doGetPathRequest(actionCommand, e.getSource());
                break;
            case APPOINTMENT_TABLE_COUNT_REQUEST:
                count = doAppointmentTableCountRequest();
                getEntityDescriptor().setTableRowCount(count);
                doPropertyChangeEvent(
                        DesktopViewControllerPropertyChangeEvent.APPOINTMENT_TABLE_COUNT_RECEIVED,
                        (PropertyChangeListener)e.getSource());
                break;
            case PATIENT_TABLE_COUNT_REQUEST:
                count = doPatientTableCountRequest();
                getEntityDescriptor().setTableRowCount(count);
                doPropertyChangeEvent(
                        DesktopViewControllerPropertyChangeEvent.PATIENT_TABLE_COUNT_RECEIVED,
                        (PropertyChangeListener)e.getSource());
                break;
            case PATIENT_NOTIFICATION_TABLE_COUNT_REQUEST:
                count = doPatientNotificationTableCountRequest();
                getEntityDescriptor().setTableRowCount(count);
                doPropertyChangeEvent(
                        DesktopViewControllerPropertyChangeEvent.PATIENT_NOTIFICATION_TABLE_COUNT_RECEIVED,
                        (PropertyChangeListener)e.getSource());
                break;
            case SURGERY_DAYS_ASSIGNMENT_TABLE_COUNT_REQUEST:
                count = doSurgeryDaysAssignmentTableCountRequest();
                getEntityDescriptor().setTableRowCount(count);
                doPropertyChangeEvent(
                        DesktopViewControllerPropertyChangeEvent.SURGERY_DAYS_ASSIGNMENT_TABLE_COUNT_RECEIVED,
                        (PropertyChangeListener)e.getSource());
                break;
            case PATIENT_NOTIFICATION_VIEW_CONTROLLER_REQUEST:
                doPatientNotificationViewControllerRequest();
                break;
            case VIEW_CLOSE_REQUEST:{
                doViewCloseRequest();
                break;
            }
            case APPOINTMENT_VIEW_CONTROLLER_REQUEST:{
                doAppointmentViewControllerRequest();
                break;
            }
            case PATIENT_VIEW_CONTROLLER_REQUEST:{
                doPatientViewControllerRequest();
                break;
            }
            case VIEW_CLOSED_NOTIFICATION:{/* user has attempted to close Desktop view*/
                doViewNotificationRequest();
                break;
            }    
            case PMS_STORE_COPY_REQUEST:
                doPMSStoreCopyRequest();
                break;
            case PMS_STORE_CREATION_REQUEST:{
                doPMSStoreCreationRequest();
                break;
            }
            case PMS_STORE_DELETION_REQUEST:{
                doPMSStoreDeletionRequest();
                break;
            }
            case PMS_STORE_RENAME_REQUEST:{
                doPMSStoreRenameRequest();
                break;
            }
            case PMS_STORE_SELECTION_REQUEST:{
                doPMSStoreSelectionRequest();
                break;
            }
            case NON_PMS_STORE_CREATION_REQUEST:{
                doNonPMSStoreCreationRequest();
                break;
            }
            case NON_PMS_STORE_DELETION_REQUEST:{
                doNonPMSStoreDeletionRequest();
                break;
            }
    
            case SET_APPOINTMENT_CSV_PATH_REQUEST:{
                doCSVAppointmentFileRequest();
                break;
            }
            case SET_PATIENT_CSV_PATH_REQUEST:{
                doCSVPatientFileRequest();
                break;
            }
/*
            case EXPORT_MIGRATED_DATA:{
                getEntityDescriptor().getMigrationDescriptor().setExportOperationStatus(true);
                doImportProgressViewControllerRequest();
                break;
            }
            */
            case IMPORT_DATA_FROM_SOURCE:{
                //getEntityDescriptor().getMigrationDescriptor().setImportOperationStatus(true);
                doImportProgressViewControllerRequest();
                //doImportDataFromSource();
                break;
            }
            
        }
        
        
        /*
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.MIGRATE_APPOINTMENT_DBF_TO_CSV.toString())){
            try{
        */
                /**
                 * CSV_APPOINTMENT_FILE_CONVERTER action produces csv appointments file
                 * from the csv version of denApp.dbf
                 * -- refer to AppointmentField enum to see which columns are transferred
                 * -- trailing "\" in a cell removed manually from dbf file before conversion
                 * because the character escapes the following comma delimiter and
                 * lessens the column count by one as a result; which breaks the logic
                 * involved in the conversion
                 * -- headers row removed and any obvious duff lines (blank/rubbish) that might exist
                 */
                /*
                CSVMigrationManager.action(Store.MigrationMethod.CSV_APPOINTMENT_FILE_CONVERTER);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                */
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
                /*
            }
        } 
                */
        

                /**
                 * CSV_PATIENT_FILE_CONVERTER action produces a csv file comprising
                 * appointment records, derived from the csv file imported from the
                 * denPat.dbf file.
                 * -- age and shortened name columns removed and much else (refer to 
                 * PatientField enum for reordering & deletion of columns so same enum 
                 * is used for input and output files in the cnversion
                 * -- headers row removed and any obvious duff lines (blank/rubbish) that might exist
                 * -- no attempt to copy over patient-guardian connections
                 * -- 1904, 8926, 12402 and 13164 = no contact info, removed
                 * -- 20773 "missing name" replaces the blanks in name fields
                 * -- duplicate keys 14155 and 17755 found and duplicate key removed
                 */

                /**
                 * CSV_MIGRATION_INTEGRITY_PROCESS action produces a list of orphaned 
                 * appointments which refer to patient keys which no longer exist; and
                 * uses this to delete the orphaned records from the system.
                 * -- nonExistingPatients.csv lists the patient keys uniquely referred to by
                 * orphaned appointment records. Note this is derived from a set in which
                 * duplicate references (to the same patient key) do not appear in the list (1668 keys)
                 * -- the orphaned appointment.csv lists all references to patient keys that 
                 * do not exist (i.e. non unique list of keys, total 10081
                 * -- the referential integrity between appointments and patients is achieved 
                 * with the deletion of each of the orphaned appointment records in the list
                 * (37794 - 10081 = 27713 remaining appointment records)
                 * 
                 */
                
    }

    private DesktopView getView(){
        return this.view;
    }       
    private void setView(DesktopView view){
        this.view = view;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {   
        
        
        boolean isCommandLineError = false;
        String usageError = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        
            if (args.length == 1){
                if (args[0].equals("DATA_MIGRATION_ENABLED")){
                    isDataMigrationOptionEnabled = true;
                }
                else isDataMigrationOptionEnabled = false;
            }
            else isDataMigrationOptionEnabled = false;
       
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DesktopViewController();
            }
        });   
    }
    
    private void requestViewControllersToCloseViews(){
        if (this.patientViewControllers.size() > 0){
            Iterator<PatientViewController> pvcIterator = patientViewControllers.iterator();
            while(pvcIterator.hasNext()){
                PatientViewController pvc = pvcIterator.next();
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
                pvc.actionPerformed(actionEvent);    
            }
        }
        
        if (this.appointmentViewControllers.size() > 0){
            Iterator<AppointmentScheduleViewController> avcIterator = appointmentViewControllers.iterator();
            while(avcIterator.hasNext()){
                AppointmentScheduleViewController avc = avcIterator.next();
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
                avc.actionPerformed(actionEvent);    
            }
        }
        if ((appointmentViewControllers.isEmpty()) && (patientViewControllers.isEmpty())){
            if (this.isDesktopPendingClosure){
                getView().dispose();
                System.exit(0);
            }
        }
        else{
            //this.requestViewControllersToCloseViews();
        }
         
    }

    private void createNewAppointmentViewController(Optional<EntityDescriptor> ed){
        try{
                appointmentViewControllers.add(
                                            new AppointmentScheduleViewController(this, getView(),ed));
                AppointmentScheduleViewController avc = 
                        appointmentViewControllers.get(appointmentViewControllers.size()-1);
                
                this.getView().getDeskTop().add(avc.getView());
                avc.getView().setVisible(true);
                avc.getView().setTitle("Appointments");
                avc.getView().setClosable(false);
                avc.getView().setMaximizable(false);
                avc.getView().setIconifiable(true);
                avc.getView().setResizable(false);
                avc.getView().setSelected(true);
                avc.getView().setSize(760,600);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
            catch (PropertyVetoException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
    }
    
    private String removeFilenameFrom(String file){
        String result;
        String filename = FilenameUtils.getName(file);
        if (filename.isEmpty())result = file;
        else result = file.substring(0,file.length()- filename.length());
        return result;
    }
    
    /**
     * Ensures specified file has the specified extension
     * -- extract the base name of specified file
     * -- remove the specified filename from the specified file
     * -- recreate the specified file with extracted base name specified extension
     * @param file
     * @param extension
     * @return File modified (if required) file specification
     */
    private File setExtensionFor(File  file, String extension){
        String p = file.getPath();
        String name = FilenameUtils.getBaseName(p);
        p = removeFilenameFrom(file.getPath());
        return new File(p + name + extension);
    }
    
    private File removeIfTrailingAsteriskFromFile(File file){
        String p = file.getPath();
        if (p.substring(p.length()-1).equals("*")) p = p.substring(0, p.length()-2);
        return new File(p);
    }
    
    private File addWildCardIfNoFilenameSpecifiedFor(File file){
        String f = file.getPath();
        if (f.substring(f.length()-1).equals("\\")) f = f + "*";
        return new File(f);
    }
    
    private File addDirectionIfFilenameMissing(String file, String direction){
        File result = new File(file);
        if (FilenameUtils.getBaseName(file).isEmpty()) result = new File(file + direction);
        return result;
    }
    /*
    private void doMigrationActionCompleteResonse1(){
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            this.
            getEntityDescriptor().
                    getMigrationDescriptor().
                    setMigrationDatabaseSelection(storeManager.getPMSStorePath());
            getEntityDescriptor().getMigrationDescriptor().setAppointmentTableCount(new AppointmentTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setPatientTableCount(new PatientTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setSurgeryDaysAssignmentTableCount(new SurgeryDaysAssignmentTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setAppointmentsCount(new Appointments().count());
                    //getEntityDescriptor().getMigrationDescriptor().setPatientsCount(new Patients().count());
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() + "\nMigration database "
                    + "connection failure","DesktopViewController error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doMigrationActionCompleteResponse(boolean includeTableRowCounts){
       String PMSStorePath = null;
       String patientCSVPath = null;
       String appointmentCSVPath = null;
       try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            PMSStorePath = storeManager.getPMSStorePath();
            patientCSVPath = storeManager.getPatientCSVPath();
            appointmentCSVPath = storeManager.getAppointmentCSVPath();
            
            
           //getEntityDescriptor().getMigrationDescriptor().setPMSDatabaseSelection(storeManager.getPMSTargetStorePath());
            
            //if (MigrationDatabase.isSelected()){
                    getEntityDescriptor().getMigrationDescriptor().setAppointmentTableCount(new AppointmentTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setPatientTableCount(new PatientTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setSurgeryDaysAssignmentTableCount(new SurgeryDaysAssignmentTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setAppointmentsCount(new Appointments().count());
                    //getEntityDescriptor().getMigrationDescriptor().setPatientsCount(new Patients().count());
            //}
            
            getEntityDescriptor().
                    getMigrationDescriptor().
                    setPMSDatabaseSelection(PMSDatabase.getPath());
   
            
            pcSupport.addPropertyChangeListener(view);
            PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.MIGRATION_ACTION_COMPLETE.toString(),
                null,getEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            String test = storeManager.getPatientCSVPath();
            if (test==null){}
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() + "\nMigration database "
                    + "connection failure","DesktopViewController error",
                    JOptionPane.WARNING_MESSAGE);
        }  
    }
    */
    private void doViewCloseRequest(){
        String[] options = {"Yes", "No"};
        String message;
        if (!appointmentViewControllers.isEmpty()||!patientViewControllers.isEmpty()){
            message = "At least one patient or appointment view is active. Close application anyway?";
        }
        else {message = "Close The Clinic practice management system?";}
        int close = JOptionPane.showOptionDialog(getView(),
                        message,null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        null);
        if (close == JOptionPane.YES_OPTION){
            this.isDesktopPendingClosure = true;
            if (!this.appointmentViewControllers.isEmpty()||!this.patientViewControllers.isEmpty()){
                requestViewControllersToCloseViews();
            }
            else {
                getView().dispose();
                System.exit(0);
            }    
        }
    }
    
    /**
     * method does following
     * -- constructs a new VC (ExportProgressViewControler)
     */
    private void doImportProgressViewControllerRequest(){
        if (importExportProgressViewControllers.isEmpty()){
            importExportProgressViewControllers.add(
                                    new ImportProgressViewController(this, getView(), getEntityDescriptor()));
            ImportProgressViewController evc = importExportProgressViewControllers.get(importExportProgressViewControllers.size()-1);

            this.getView().getDeskTop().add(evc.getView());
        }else{
            String message = "An export is currently in progress; hence "
                    + "the request for a new export process to start is ignored.";
            displayErrorMessage(message,"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
  
        
    }
    
    private void doAppointmentViewControllerRequest(){
        createNewAppointmentViewController(Optional.ofNullable(null));
        //getView().disableDataControl();
    }
    
    private void doPatientNotificationViewControllerRequest(){
        try{
            patientNotificationViewControllers.add(
                                        new PatientNotificationViewController(this,getView()));
            PatientNotificationViewController pnvc = 
                    patientNotificationViewControllers.get(patientNotificationViewControllers.size()-1);
            
            this.getView().getDeskTop().add(pnvc.getView());
            pnvc.getView().initialiseView();
            
            
        }catch (StoreException ex){
            String message = ex.getMessage();
            JOptionPane.showMessageDialog(this.getView(), 
                    message, "Desktop View Controller error", JOptionPane.WARNING_MESSAGE);
        }
    }
                
    private void doPatientViewControllerRequest(){
        try{
            patientViewControllers.add(
                                    new PatientViewController(this, getView()));
            PatientViewController pvc = patientViewControllers.get(patientViewControllers.size()-1);

            this.getView().getDeskTop().add(pvc.getView());
            pvc.getView().setVisible(true);
            pvc.getView().setClosable(false);
            pvc.getView().setMaximizable(false);
            pvc.getView().setIconifiable(true);
            pvc.getView().setResizable(false);
            pvc.getView().setSelected(true);
            pvc.getView().setSize(700
                    ,585);
            /**
             * disable data menu in the desktop view
             */
            //getView().disableDataControl();
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
        catch (PropertyVetoException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
    }
    
    private void doViewNotificationRequest(){
            System.exit(0);
    }
    
    /**
     * the method copies the currently selected PMS store to a designated destination path
     */
    private void doPMSStoreCopyRequest(){
        StoreManager storeManager = null;
        try{
            FileNameExtensionFilter filter = null;
            if (isPMSStoreDefined()){
                String targetPath = StoreManager.GET_STORE_MANAGER().getPMSStorePath();
                String targetFileName = removeFilenameFrom(targetPath);
                filter = new FileNameExtensionFilter("Access database files", "accdb");
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Copy PMS store");
                chooser.setApproveButtonText("Create PMS copy");
                chooser.setFileFilter(filter);
                chooser.setSelectedFile(new File(targetPath));
                int returnVal = chooser.showOpenDialog(getView());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (file.exists()){
                        int reply = JOptionPane.showConfirmDialog(getView(),
                            "are you sure you want to overwrite the selected destination file ("
                                    + FilenameUtils.getName(file.getPath()) + ")",
                            "Destination file for copying", 
                            JOptionPane.OK_CANCEL_OPTION);
                        if (reply==JOptionPane.OK_OPTION){
                            Files.copy(Paths.get(targetPath),
                                    Paths.get(file.getPath()),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    else {
                        Path destinationPath = Paths.get(file.getPath());
                        Files.copy(Paths.get(targetPath),
                                    destinationPath,
                                    StandardCopyOption.REPLACE_EXISTING);
                    }

                }
            }else{
                displayErrorMessage("PMS store file undefined, attempt to copy aborted.",
                        "Desktop view controller error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }catch(StoreException ex){
            
        }
        catch(IOException ex){
            String message = ex.getMessage();
        }
        
    }
    
    private void doNonPMSStoreCreationRequest(){
        try{
            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            //String targetPath = storeManager.getPMSStorePath();
            //if (targetPath != null) targetPath = removeFilenameFrom(targetPath);
            filter = new FileNameExtensionFilter("Access database files", "accdb");
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Create new store");
            chooser.setApproveButtonText("Create store");
            chooser.setFileFilter(filter);
            //if (targetPath!=null) chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath,"Enter name of PMS store to create"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (FilenameUtils.getExtension(file.getPath()).equals("accdb")) {
                    if (!file.exists()){
                        file = storeManager.createStore(file);
                        String pmsStorePath = storeManager.getPMSStorePath();
                        storeManager.setPMSStorePath(file.getPath());
                        ThePatient patientTable = new ThePatient();
                        patientTable.create();
                        PatientNotification patientNotificationTable = new PatientNotification();
                        patientNotificationTable.create();
                        SurgeryDaysAssignment surgeryDaysAssignmentTable = new SurgeryDaysAssignment();
                        surgeryDaysAssignmentTable.create();
                        surgeryDaysAssignmentTable.insert();
                        TheAppointment appointmentTable = new TheAppointment();
                        appointmentTable.create();
                        storeManager.setPMSStorePath(pmsStorePath);
                       JOptionPane.showMessageDialog(getView(),
                            file.getPath() + " store file has been successfully created",
                            "Desktop view controller message", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    else{
                        String filename = file.toPath().getName(file.toPath().getNameCount()-1).toString();
                        displayErrorMessage("Selected database file to create -> " + 
                                filename + " already exists","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                    }
                }else{
                    String filename = file.toPath().getName(file.toPath().getNameCount()-1).toString();
                    displayErrorMessage("Selected database file -> " + 
                            filename + "has an incorrect or missing extension. "
                                    + "Attempt to create this store has been aborted",
                            "Desktop view controller error",JOptionPane.WARNING_MESSAGE);
                }
                notifyMigrationActionCompleted();
            }    
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doPMSStoreCreationRequest(){
        try{
            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getPMSStorePath();
            if (targetPath != null) targetPath = removeFilenameFrom(targetPath);
            filter = new FileNameExtensionFilter("Access database files", "accdb");
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Create new store");
            chooser.setApproveButtonText("Create store");
            chooser.setFileFilter(filter);
            //if (targetPath!=null) chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath,"Enter name of PMS store to create"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (FilenameUtils.getExtension(file.getPath()).equals("accdb")) {
                    if (!file.exists()){
                        file = storeManager.createStore(file);
                        storeManager.setPMSStorePath(file.getPath());
                        ThePatient patientTable = new ThePatient();
                        patientTable.create();
                        PatientNotification patientNotificationTable = new PatientNotification();
                        patientNotificationTable.create();
                        SurgeryDaysAssignment surgeryDaysAssignmentTable = new SurgeryDaysAssignment();
                        surgeryDaysAssignmentTable.create();
                        surgeryDaysAssignmentTable.insert();
                        TheAppointment appointmentTable = new TheAppointment();
                        appointmentTable.create();
                       JOptionPane.showMessageDialog(getView(),
                            file.getPath() + " store file has been successfully created",
                            "Desktop view controller message", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    else{
                        String filename = file.toPath().getName(file.toPath().getNameCount()-1).toString();
                        displayErrorMessage("Selected database file to create -> " + 
                                filename + " already exists","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                    }
                }else{
                    String filename = file.toPath().getName(file.toPath().getNameCount()-1).toString();
                    displayErrorMessage("Selected database file -> " + 
                            filename + "has an incorrect or missing extension. "
                                    + "Attempt to create this store has been aborted",
                            "Desktop view controller error",JOptionPane.WARNING_MESSAGE);
                }
                notifyMigrationActionCompleted();
            }    
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    } 
    
    private void doPMSStoreRenameRequest(){
        Boolean isPMSStoreCorrectlyDefined = null;
        int returnVal = 0;
        FileNameExtensionFilter filter = null;
        File fromFile = null;
        File toFile = null;
        File file = null;
        String newFilenameInstruction = null;
        String newPMSStorePath = null;
        String pmsStorePath = null;
        String storageType = null;
        
        try{
            if (isPMSStoreDefined()){
                pmsStorePath = StoreManager.GET_STORE_MANAGER().getPMSStorePath();
                storageType = StoreManager.GET_STORE_MANAGER().getStorageType();
                filter = null;
                filter = new FileNameExtensionFilter("Access database files", "accdb");
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Rename PMS store");
                chooser.setApproveButtonText("Define new name");
                chooser.setFileFilter(filter);
                newFilenameInstruction = "Enter new name for "
                        + FilenameUtils.getName(pmsStorePath);
                chooser.setSelectedFile(new File(pmsStorePath));
                returnVal = chooser.showOpenDialog(getView());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    isPMSStoreCorrectlyDefined = true;
                    if (FilenameUtils.getName(file.getPath()).equals(newFilenameInstruction)){
                        isPMSStoreCorrectlyDefined = false;
                    }
                    if (isPMSStoreCorrectlyDefined){
                        String currentPMSStorePath = removeFilenameFrom(pmsStorePath);
                        String selectedFilePath = removeFilenameFrom(file.getPath());
                        if (!selectedFilePath.equals(currentPMSStorePath)) 
                            isPMSStoreCorrectlyDefined = false;
                    }
                    if (isPMSStoreCorrectlyDefined){ 
                        if (!FilenameUtils.getExtension(file.getPath()).equals("accdb"))
                            if (FilenameUtils.getExtension(file.getPath()).isEmpty()){
                                file = new File(file.getPath() + ".accdb");
                                isPMSStoreCorrectlyDefined = true;
                            }
                            else isPMSStoreCorrectlyDefined = false;
                        }        
                    }
                    if (!isPMSStoreCorrectlyDefined) {       
                        displayErrorMessage(
                            "New name defined for the selected PMS store is incorrect",
                                "Desktop view controller error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    else{
                        newPMSStorePath = removeFilenameFrom(pmsStorePath);
                        newPMSStorePath = newPMSStorePath + FilenameUtils.getName(file.getPath());
                        fromFile = new File(pmsStorePath);
                        toFile = new File(newPMSStorePath);
                        if (!fromFile.renameTo(toFile)){
                            displayErrorMessage(
                                "Could not rename " + FilenameUtils.getName(pmsStorePath)
                                    + " to " + FilenameUtils.getName(newPMSStorePath),
                                    "Desktop view controller error",
                                    JOptionPane.WARNING_MESSAGE);
                        }else{
                            StoreManager.GET_STORE_MANAGER().setPMSStorePath(toFile.getPath());

                            JOptionPane.showMessageDialog(getView(),
                                    StoreManager.GET_STORE_MANAGER().getPMSStorePath(),
                                    "Current PMS store path", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            notifyMigrationActionCompleted();
                        }  
                    }
                } 
            else{     
                displayErrorMessage(
                        "A PMS database has not been selected; rename operation aborted",
                        "Desktop view controller error",
                        JOptionPane.WARNING_MESSAGE);
            }
            
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() +"\n Exception handled in AccessStore::doPMSRename()",
                    "Desktop view controller error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doNonPMSStoreDeletionRequest(){
        String pmsStorePath = null;
        String filenameFromPMSStorePath = null;
        String pmsStoreStorePathWithoutFilename = null;
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            pmsStorePath = storeManager.getPMSStorePath();
            String storageType = storeManager.getStorageType();
            FileNameExtensionFilter filter = null;
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            pmsStoreStorePathWithoutFilename = removeFilenameFrom(pmsStorePath);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Delete store flle");
            chooser.setApproveButtonText("Delete file");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(addDirectionIfFilenameMissing(pmsStoreStorePathWithoutFilename,"Select migration database to delete"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                
                if (file.exists()){
                    pmsStorePath = storeManager.getPMSStorePath();
                    filenameFromPMSStorePath = 
                            FilenameUtils.getName(pmsStorePath);
                    if (pmsStorePath.equals(file.getPath())){
                        displayErrorMessage("Unable to delete the currently selected PMS Store. "
                                + "Use the 'delete' in the 'Non-PMS store actions' instead",
                                "Desktop view controller error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if (!file.delete()){
                        displayErrorMessage("Unable to delete the selected file -> "  + 
                                file.getPath(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);  
                    }
                    else
                        JOptionPane.showMessageDialog(getView(), 
                                file.getPath() + " deleted", 
                                "Desktop view controller error", 
                                JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    displayErrorMessage("Selected file -> " + file.getPath() + " cannot be located",
                            "DesktopViewController error",
                            JOptionPane.WARNING_MESSAGE);
                }
                
            }
            
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

        }
    }
    
    /**
     * method checks to ensure a PMS store has been selected and then deletes this file
     */
    private void doPMSStoreDeletionRequest(){
        File file = null;
        StoreManager storeManager = null;
        String pmsStorePath = null;
        String filenameFromPMSStorePath = null;
        String pmsStoreStorePathWithoutFilename = null;
        try{
            storeManager = StoreManager.GET_STORE_MANAGER();
            if (isPMSStoreDefined()){
                pmsStorePath = StoreManager.GET_STORE_MANAGER().getPMSStorePath();
                file = new File(pmsStorePath);
                if (file.exists()){
                    if (file.delete()){
                        pmsStoreStorePathWithoutFilename = removeFilenameFrom(pmsStorePath);
                        storeManager.setPMSStorePath(pmsStoreStorePathWithoutFilename);
                        JOptionPane.showMessageDialog(getView(),
                                file.getPath() + " successfully deleted",
                                "Desktop view controller message", 
                                JOptionPane.INFORMATION_MESSAGE);
                        notifyMigrationActionCompleted();
                    }
                    else{
                        displayErrorMessage("Unable to delete the selected PMS store file -> "  + 
                                file.getPath(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                        storeManager.setPMSStorePath(
                                FilenameUtils.concat(pmsStorePath, filenameFromPMSStorePath)); 
                    }
                }
                else{
                    displayErrorMessage("The selected PMS store file -> " 
                            + file.getPath() + " cannot be located",
                            "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                }
            } else{
                displayErrorMessage("A PMS store has not been selected; attempt to delete PMS store aborted",
                            "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),
                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    /*
    private void doMigrationDatabaseDeletionRequest(){
        String migrationTargetDatabaseStorePath = null;
        String filenameFromMigrationTargetDatabaseStorePath = null;
        String migrationDatabaseStorePathWithoutFilename = null;
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            migrationTargetDatabaseStorePath = storeManager.getMigrationTargetStorePath();
            String storageType = storeManager.getStorageType();
            FileNameExtensionFilter filter = null;
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            migrationDatabaseStorePathWithoutFilename = removeFilenameFrom(migrationTargetDatabaseStorePath);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Delete migration database");
            chooser.setApproveButtonText("Delete database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(addDirectionIfFilenameMissing(migrationDatabaseStorePathWithoutFilename,"Select migration database to delete"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                
                if (file.exists()){
                    migrationTargetDatabaseStorePath = storeManager.getMigrationTargetStorePath();
                    filenameFromMigrationTargetDatabaseStorePath = 
                            FilenameUtils.getName(migrationTargetDatabaseStorePath);
                    if (migrationTargetDatabaseStorePath.equals(file.getPath())){
                            migrationDatabaseStorePathWithoutFilename = removeFilenameFrom(migrationTargetDatabaseStorePath);
                            storeManager.setMigrationTargetStorePath(migrationDatabaseStorePathWithoutFilename);
                    }
                    if (!file.delete()){
                        displayErrorMessage("Unable to delete the migration database file -> "  + 
                                file.getPath(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                        storeManager.setMigrationTargetStorePath(
                                FilenameUtils.concat(migrationTargetDatabaseStorePath, filenameFromMigrationTargetDatabaseStorePath));   
                    }
                    //this.doMigrationActionCompleteResponse(false);
                    notifyMigrationActionCompleted();
                }
                else{
                    displayErrorMessage("Migration database file -> " + file.getPath() + " cannot be located","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                }
                
            }
            
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

        }
    }
    */
    private void doPMSStoreSelectionRequest(){
        try{
            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getPMSStorePath();
            String storageType = storeManager.getStorageType();
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            //File path = addDirectionIfFilenameMissing(targetPath, "Select migration database to use");
            File path = addDirectionIfFilenameMissing(targetPath, "Select database to use as PMS store");
            JFileChooser chooser = new JFileChooser();
            //chooser.setDialogTitle("Select migration database to use");
            chooser.setDialogTitle("Select database to use as PMS store");
            chooser.setApproveButtonText("Select database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(path);
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file.exists()){
                    //storeManager.setMigrationTargetStorePath(file.getPath());
                    storeManager.setPMSStorePath(file.getPath());

                    JOptionPane.showMessageDialog(getView(),
                            storeManager.getPMSStorePath(),
                            "Current PMS store path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    displayErrorMessage("PMS store -> " + file.getPath() + " does not exist","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                } 
                //this.doMigrationActionCompleteResponse(true);
                notifyMigrationActionCompleted();
            }
            

        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

        }
    }
    /*
    private void doMigrationDatabaseSelectionRequest(){
        try{

            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getMigrationTargetStorePath();
            String storageType = storeManager.getStorageType();
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            //File path = addDirectionIfFilenameMissing(targetPath, "Select migration database to use");
            File path = addDirectionIfFilenameMissing(targetPath, "Select persistent storage database to use");
            JFileChooser chooser = new JFileChooser();
            //chooser.setDialogTitle("Select migration database to use");
            chooser.setDialogTitle("Select persistent storage database to use");
            chooser.setApproveButtonText("Select database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(path);
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file.exists()){
                    //storeManager.setMigrationTargetStorePath(file.getPath());
                    storeManager.setStorePath(file.getPath());

                    JOptionPane.showMessageDialog(getView(),
                            storeManager.getMigrationTargetStorePath(),
                            "Current migration database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    displayErrorMessage("Migration database -> " + file.getPath() + " does not exist","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                } 
                //this.doMigrationActionCompleteResponse(true);
                notifyMigrationActionCompleted();
            }
            

        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

        }
    }
  */
    /*
    private void doPMSDatabaseDeletionRequest(){
        String filenameFromPMSTargetDatabaseStorePath = null;
        String pmsDatabaseStorePathWithoutFilename = null;
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String pmsTargetDatabaseStorePath = storeManager.getPMSTargetStorePath();
            String storageType = storeManager.getStorageType();
            FileNameExtensionFilter filter = null;
            filter = new FileNameExtensionFilter("Access database files", "accdb");
            pmsTargetDatabaseStorePath =  removeFilenameFrom(pmsTargetDatabaseStorePath);    
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Delete PMS database");
            chooser.setApproveButtonText("Delete database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(addDirectionIfFilenameMissing(pmsTargetDatabaseStorePath,"Select PMS database to delete"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                
                if (file.exists()){
                    pmsTargetDatabaseStorePath = storeManager.getPMSTargetStorePath();
                    filenameFromPMSTargetDatabaseStorePath = 
                            FilenameUtils.getName(pmsTargetDatabaseStorePath);
                    if (pmsTargetDatabaseStorePath.equals(file.getPath())){
                            pmsDatabaseStorePathWithoutFilename = removeFilenameFrom(pmsTargetDatabaseStorePath);
                            storeManager.setPMSTargetStorePath(pmsDatabaseStorePathWithoutFilename);
                    }
                    if (!file.delete()){
                        displayErrorMessage("Unable to delete the PMS database file -> "  + 
                                file.getPath(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                        storeManager.setPMSTargetStorePath(
                                FilenameUtils.concat(pmsTargetDatabaseStorePath, filenameFromPMSTargetDatabaseStorePath));   
                    }
                this.doMigrationActionCompleteResponse(false);
                }
                else{
                    displayErrorMessage("PMS database file -> " + file.getPath() + " cannot be located","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                }
                
            }
            
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }

    }
    */
    
    /*
    private void doPMSDatabaseSelectionRequest(){
        try{
            File path;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getPMSTargetStorePath();
            String storageType = storeManager.getStorageType();
            FileNameExtensionFilter filter = null;
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select PMS database");
            chooser.setApproveButtonText("Select database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath, "Select PMS database"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file.exists()){
                    storeManager.setPMSTargetStorePath(file.getPath());

                    JOptionPane.showMessageDialog(getView(),
                            storeManager.getPMSTargetStorePath(),
                            "Current PMS database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    displayErrorMessage("PMS database -> " + file.getPath() + " does not exist","DesktopViewController error",JOptionPane.WARNING_MESSAGE);

                }
                this.doMigrationActionCompleteResponse(true);
            }
            

        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    */
     
    
    private void doCSVAppointmentFileRequest(){
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getAppointmentCSVPath();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV source files", "csv");

            File path = new File(targetPath);
            JFileChooser chooser = new JFileChooser(path);
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(path);
            chooser.setDialogTitle("Select appointment CSV file");
            chooser.setApproveButtonText("Select CSV file");
            chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath,"Select appointment CSV file"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                String updatedAppointmentCSVFlePath = chooser.getSelectedFile().getPath();
                /**
                 * 07/12/2021 19:17
                 */
                storeManager.setAppointmentCSVPath(updatedAppointmentCSVFlePath);
                JOptionPane.showMessageDialog(getView(),
                        storeManager.getAppointmentCSVPath(),
                        "Current appointment source CSV file path", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
            //this.doMigrationActionCompleteResponse(true);
            notifyMigrationActionCompleted();
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
        }
    }
    
    private void doCSVPatientFileRequest(){
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getPatientCSVPath();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV source files", "csv");
            File path = new File(targetPath);
            JFileChooser chooser = new JFileChooser(path);
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(path);
            chooser.setDialogTitle("Select appointment CSV file");
            chooser.setApproveButtonText("Select CSV file");
            chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath,"Select patoent CSV file"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                String updatedPatientCSVFilePath = chooser.getSelectedFile().getPath();
                storeManager.setPatientCSVPath(updatedPatientCSVFilePath);
                JOptionPane.showMessageDialog(getView(),
                        storeManager.getPatientCSVPath(),
                        "Current patient source CSV file path", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
            //this.doMigrationActionCompleteResponse(true);
            notifyMigrationActionCompleted();
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
        }
    }

    /**
     * Using the SwingWorker class facilitates communication between concurrent tasks
     * -- the SwingWorker-based task runs on a thread in the background 
     * -- which is designed to fire a property change event to a specified listener when a bound variable changes
     * -- i.e. whenever the setProgress(0..100) changes the value of the bound variable
     * -- the done() method is called when the doInBackground() method completes; and is executed on the Event Despatch 
     * -- which is used to send an action event to the ExportProgressViewController to indicate the completion of the task
     * @param entity:IEntityStoreType, which can be interrogated to determine if a collection of appointment or patient objects  have been specified
     * @param desktopViewController references the DesktopViewController object which is referenced in the Action Event sent in the done90 method  
     */
    private void startBackgroundThread(EntityStoreType entity,DesktopViewController desktopViewController){
        SwingWorker sw1 = new SwingWorker(){
            
            @Override
            protected String doInBackground() throws Exception 
            {
                String result = null;
                int count = 0;
                if (entity.getIsPatient()){
                    ThePatient patientTable = (ThePatient)entity;
                    //patientTable.create();
                    List<String[]>dbfRecords = patientTable.importEntityFromCSV();
                    count = dbfRecords.size();
                    Iterator dbfRecordsIt = dbfRecords.iterator();
                    int recordCount = 0;
                    
                    while(dbfRecordsIt.hasNext()){
                        ThePatient patient = patientTable.convertDBFToPatient((String[])dbfRecordsIt.next());
                        patient.reformat();
                        try{
                            patient.insert();
                        }catch (StoreException ex){
                        displayErrorMessage(ex.getMessage(), "Desktop View Controller error", 
                        JOptionPane.WARNING_MESSAGE);
                        }   
                        recordCount++;
                        if (recordCount <= count){
                            Integer percentage = recordCount*100/count;
                            setProgress(percentage);
                        }
                        else {
                            break;
                        }
                    }
                    dbfRecords.clear();
                    
                }
                
                else if (entity.getIsAppointment()){
                    TheAppointment appointmentTable = (TheAppointment)entity;
                    //appointmentTable.create();
                    List<String[]> dbfRecords = appointmentTable.importEntityFromCSV();
                    setCount(dbfRecords.size());
                    Iterator dbfRecordsIt = dbfRecords.iterator();
                    setRecordCount(0);
                    while(dbfRecordsIt.hasNext()){
                        setRecordCount(getRecordCount()+1);
                        insertAppointments(
                                appointmentTable.convertDBFRecordToAppointments(
                                        (String[])dbfRecordsIt.next()),
                                         appointmentTable);
                        /*
                        appointmentsForThisDBFRecord = appointmentTable.convertDBFRecordToAppointments((String[])dbfRecordsIt.next());
                        
                        */
                    }//end of dbfRecords iteration
                    dbfRecords.clear();
                }
                return result; 
            }
            
            private void insertAppointments(ArrayList<TheAppointment> appointments,
                    TheAppointment appointmentTable) throws StoreException {
                for(TheAppointment appointment : appointments){
                    appointment.insert();
                    if (getRecordCount() <= getCount()){
                        Integer percentage = getRecordCount()*100/getCount();
                        setProgress(percentage);
                    }
                    else {
                        break;
                    }
                }
            }
            
            private int getRecordCount(){
                return recordCount;
            }
            
            private void setRecordCount(int value){
                recordCount = value;
            }
            
            private int getCount(){
                return count;
            }
            
            private void setCount(int value){
                count = value;
            }
            
            /**
             * Invoked when the doInBackground() method completes
             * -- used to send an action event to the ExportProgressViewController signalling task completion
             * -- uses also the specified IEntityStoreTYpe to determine the value of the event sent
             * -- i.e. either EXPORT_MIGRATED_PATIENTS_COMPLETED event or EXPORT_MIGRATED_APPOINTMENTS_COMPLETED event
             */
            @Override
            protected void done(){
                DesktopViewControllerActionEvent event = null;
                if (entity.getIsPatient())event = DesktopViewControllerActionEvent.IMPORT_EXPORT_PATIENT_DATA_COMPLETED;
                if (entity.getIsAppointment())event = DesktopViewControllerActionEvent.IMPORT_EXPORT_APPOINTMENT_DATA_COMPLETED;
                
                ImportProgressViewController evc = importExportProgressViewControllers.get(0);
                if (event!=null){
                    ActionEvent actionEvent = new ActionEvent(
                            desktopViewController,ActionEvent.ACTION_PERFORMED,
                            event.toString());
                    evc.actionPerformed(actionEvent);
                }else{
                    String message = "Unexpected null encountered for event in SwingWorker::done() method";
                    displayErrorMessage(message, "Desktop View Controller error", 
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        
        ImportProgressViewController evc = importExportProgressViewControllers.get(0);
        sw1.addPropertyChangeListener(evc.getView());
        sw1.execute();
    }
    
    private void startBackgroundThread(IEntityStoreType entity, DesktopViewController desktopViewController){
        SwingWorker sw1 = new SwingWorker(){
            
            @Override
            protected String doInBackground() throws Exception 
            {
                String result = null;
                int count = 0;
                /*
                if (entity.isPatients()){
                    Patients patients = (Patients)entity;
                    count = patients.size();
                    Iterator patientsIt = patients.iterator();
                    int recordCount = 0;
                    while(patientsIt.hasNext()){
                        Patient patient = (Patient)patientsIt.next();
                        patient.insert();
                        recordCount++;
                        if (recordCount <= count){
                            Integer percentage = recordCount*100/count;
                            setProgress(percentage);
                        }
                        else break;
                    }
                }
                */
                /*
                if (entity.isPatient()){
                    Patient patientTable = (Patient)entity;
                    //patientTable.create();
                    List<String[]>dbfRecords = patientTable.importFromCSV1();
                    count = dbfRecords.size();
                    Iterator dbfRecordsIt = dbfRecords.iterator();
                    int recordCount = 0;
                    while(dbfRecordsIt.hasNext()){
                        Patient patient = patientTable.convertDBFToPatient((String[])dbfRecordsIt.next());
                        patient.reformat();
                        patientTable.insert(patient);
                        recordCount++;
                        if (recordCount <= count){
                            Integer percentage = recordCount*100/count;
                            setProgress(percentage);
                        }
                        else {
                            break;
                        }
                        
                    }
                    dbfRecords.clear();
                }
                
                else if (entity.isAppointments()){
                    Appointments appointments = (Appointments)entity;
                    count = appointments.size();
                    Iterator appointmentsIt = appointments.iterator();
                    int recordCount = 0;
                    while(appointmentsIt.hasNext()){
                        Appointment appointment = (Appointment)appointmentsIt.next();
                        appointment.insert();
                        recordCount++;
                        if (recordCount <= count){
                            Integer percentage = recordCount*100/count;
                            //publish(percentage);
                            setProgress(percentage);
                        }
                        else break;
                    }
                } 
               
                else if (entity.isAppointmentTable()){
                    TheAppointment appointmentTable = (TheAppointment)entity;
                    //appointmentTable.create();
                    List<String[]> dbfRecords = appointmentTable.importEntityFromCSV();
                    setCount(dbfRecords.size());
                    Iterator dbfRecordsIt = dbfRecords.iterator();
                    setRecordCount(0);
                    while(dbfRecordsIt.hasNext()){
                        setRecordCount(getRecordCount()+1);
                        insertAppointments(
                                appointmentTable.convertDBFRecordToAppointments(
                                        (String[])dbfRecordsIt.next()),
                                         appointmentTable);

                    }//end of dbfRecords iteration
                    dbfRecords.clear();
                }
                */
                return result;  
                
            }
             
            private int getRecordCount(){
                return recordCount;
            }
            
            private void setRecordCount(int value){
                recordCount = value;
            }
            
            private int getCount(){
                return count;
            }
            
            private void setCount(int value){
                count = value;
            }
    /*
            private void insertAppointments(ArrayList<Appointment> appointments,
                    AppointmentTable appointmentTable) throws StoreException {
                for(Appointment appointment : appointments){
                    appointmentTable.insert(appointment);
                    if (getRecordCount() <= getCount()){
                        Integer percentage = getRecordCount()*100/getCount();
                        setProgress(percentage);
                    }
                    else {
                        break;
                    }
                }
            }
            */
            /**
             * Invoked when the doInBackground() method completes
             * -- used to send an action event to the ExportProgressViewController signalling task completion
             * -- uses also the specified IEntityStoreTYpe to determine the value of the event sent
             * -- i.e. either EXPORT_MIGRATED_PATIENTS_COMPLETED event or EXPORT_MIGRATED_APPOINTMENTS_COMPLETED event
             */
            @Override
            protected void done(){
                DesktopViewControllerActionEvent event = null;
                if (entity.isPatients())event = DesktopViewControllerActionEvent.IMPORT_EXPORT_PATIENT_DATA_COMPLETED;
                if (entity.isPatientTable())event = DesktopViewControllerActionEvent.IMPORT_EXPORT_PATIENT_DATA_COMPLETED;
                if (entity.isAppointments())event = DesktopViewControllerActionEvent.IMPORT_EXPORT_APPOINTMENT_DATA_COMPLETED;
                if (entity.isAppointmentTable())event = DesktopViewControllerActionEvent.IMPORT_EXPORT_APPOINTMENT_DATA_COMPLETED;
                ImportProgressViewController evc = importExportProgressViewControllers.get(0);
                if (event!=null){
                    ActionEvent actionEvent = new ActionEvent(
                            desktopViewController,ActionEvent.ACTION_PERFORMED,
                            event.toString());
                    evc.actionPerformed(actionEvent);
                }else{
                    String message = "Unexpected null encountered for event in SwingWorker::done() method";
                    displayErrorMessage(message, "Desktop View Controller error", 
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        
        ImportProgressViewController evc = importExportProgressViewControllers.get(0);
        sw1.addPropertyChangeListener(evc.getView());
        sw1.execute();
    }
    
    /*
    private void doExportMigratedSurgeryDaysAssignment(){
        IEntityStoreType entity = null;
        SurgeryDaysAssignmentx surgeryDaysAssignment = new SurgeryDaysAssignmentx();
        try{
            surgeryDaysAssignment.create();
            SurgeryDaysAssignmentTable surgeryDaysAssignmentTable = new SurgeryDaysAssignmentTable();
            entity = surgeryDaysAssignmentTable.read();
            if (entity!=null){
                if (entity.isSurgeryDaysAssignment()){
                    surgeryDaysAssignment = (SurgeryDaysAssignmentx)entity;
                    //surgeryDaysAssignment.update();
                    surgeryDaysAssignment.insert();
                    doMigrationActionCompleteResponse(true);
                }else{
                    displayErrorMessage("SurgeryDaysAssignment entity expected but not encountered in AccessStore::doExportMigratedData()",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                }
            }else{
                displayErrorMessage("SurgeryDaysAssignment entity expected but null encountered in AccessStore::doExportMigratedData()",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() + "\nException handled in "
                    + "doExportMigratedSurgeyDaysAssignment()",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doExportMigratedAppointments(){
        IEntityStoreType entity = null;
        try{
            Appointment appointment = new Appointment();
            appointment.create();
            AppointmentTable appointmentTable = new AppointmentTable();
            entity = appointmentTable.read();
            if (entity!=null){
                if (entity.isAppointments()){
                    Appointments appointments = (Appointments)entity;
                    startBackgroundThread(appointments, this);
                }
                else{
                    String message = "Unexpected null data type returned when "
                            + "importing records from the appointment table in the "
                            + "selected migration database";
                    displayErrorMessage(message, "Desktop View Controller error", 
                            JOptionPane.WARNING_MESSAGE);
                }
            }else{
                String message = "Unexpected data type returned when "
                            + "importing records from the appointment table in the "
                            + "selected migration database";
                    displayErrorMessage(message, "Desktop View Controller error", 
                            JOptionPane.WARNING_MESSAGE);
            }
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() + "\nException handled in "
                    + "DesktopViewCpntroller::doExportMigratedAppointments()", 
                    "Desktop View Controller error", 
                    JOptionPane.WARNING_MESSAGE);
        }
            
    }
    
    private void doExportMigratedPatients(){
        IEntityStoreType entity = null;
        try{
            Patient patient = new Patient();
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            File file = new File(getEntityDescriptor().getMigrationDescriptor().getPMSDatabaseSelection());
            storeManager.initialiseTargetDatabase(file);
            patient.create();
            PatientTable patientTable = new PatientTable();
            entity = patientTable.read();
            if (entity!=null){
                if (entity.isPatients()){
                    Patients patients = (Patients)entity;
                    startBackgroundThread(patients, this);
                }
                else{
                    String message = "Unexpected null data type returned when "
                            + "importing records from the patient table in the "
                            + "selected migration database";
                    displayErrorMessage(message, "Desktop View Controller error", 
                            JOptionPane.WARNING_MESSAGE);
                }
            }else{
                String message = "Unexpected data type returned when "
                            + "importing records from the patient table in the "
                            + "selected migration database";
                    displayErrorMessage(message, "Desktop View Controller error", 
                            JOptionPane.WARNING_MESSAGE);
            }
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() + "\nException handled in "
                    + "DesktopViewCpntroller::doExportMigratedPatient()", 
                    "Desktop View Controller error", 
                    JOptionPane.WARNING_MESSAGE);
        }
    
    }
    */

    /*
    private void doImportPatientsFromSource(){
        //PatientTable patientTable = new PatientTable();
        ThePatient patientTable = new ThePatient();
        
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String migrationTargetDatabaseStorePath = storeManager.getMigrationTargetStorePath();
            File file = new File(migrationTargetDatabaseStorePath);
            file.delete();
            storeManager.initialiseTargetDatabase(file);
            patientTable.create();
            //new AppointmentTable().create();
            new TheAppointment().create();
            startBackgroundThread(patientTable, this);
            storeManager.closeConnection();
        }catch (StoreException ex){
            JOptionPane.showMessageDialog(null,new ErrorMessagePanel(ex.getMessage()));
        }
        this.doMigrationActionCompleteResponse(true);
    }
    */
    /*
    private void doImportAppointmentsFromSource(){
        //startBackgroundThread(new AppointmentTable(), this);
        startBackgroundThread(new TheAppointment(), this);
        notifyMigrationActionCompleted();
        
        try{
            StoreManager store = StoreManager.GET_STORE_MANAGER();
            
            //this.doMigrationActionCompleteResponse(true);
            notifyMigrationActionCompleted();
        }catch (StoreException ex){
            displayErrorMessage("StoreException: " + ex.getMessage(),
                    "DesktopViewController error",
                    JOptionPane.WARNING_MESSAGE);
        }

    }
    8/
    
    private void doImportSurgeryDaysAssignment(){
         //SurgeryDaysAssignmentTable surgeryDaysAssignmentTable = new SurgeryDaysAssignmentTable();
         SurgeryDaysAssignment surgeryDaysAssignmentTable = new SurgeryDaysAssignment();
         try{
            surgeryDaysAssignmentTable.drop();
            surgeryDaysAssignmentTable.create();
            surgeryDaysAssignmentTable.insert();
         }catch(StoreException ex){
            JOptionPane.showMessageDialog(null,new ErrorMessagePanel(ex.getMessage())); 
         }
         //this.doMigrationActionCompleteResponse(true);
         notifyMigrationActionCompleted();
    }
    
    private boolean isPMSDatabaseSelected(EntityDescriptor entityDescriptor){
        boolean result = false;
        if (FilenameUtils.getName(
                    entityDescriptor.
                            getMigrationDescriptor().
                            getPMSDatabaseSelection())!=null) result = true;
        return result;
    }
    
    private boolean isMigrationDatabaseSelected(EntityDescriptor entityDescriptor){
        boolean result = false;
        if (FilenameUtils.getName(
                    entityDescriptor.
                            getMigrationDescriptor().
                            getMigrationDatabaseSelection())!=null) result = true;
        return result;
    }
    
    static class MigrationDatabase {    
        static String getPath()throws StoreException{ 
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            //String test = FilenameUtils.getName(storeManager.getMigrationTargetStorePath());
            return storeManager.getPMSStorePath();
        }
        
        static boolean isSelected()throws StoreException{
            boolean result = false;
            if (!FilenameUtils.getName(getPath()).isEmpty())result = true;
            return result;
        }  
    }
    
    /*
    static class PMSDatabase {  

        static String getPath()throws StoreException{ 
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();           
            return storeManager.getPMSStorePath();
        }
        
        static boolean isSelected()throws StoreException{
            boolean result = false;
            String test = FilenameUtils.getName(getPath());
            if (!FilenameUtils.getName(getPath()).isEmpty())result = true;
            return result;
        }  
    }
*/
    
    static class PMSStore {  

        static String getPath()throws StoreException{ 
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();           
            return storeManager.getPMSStorePath();
        }
        
        static boolean isSelected()throws StoreException{
            boolean result = false;
            //String test = FilenameUtils.getName(getPath());
            if (!FilenameUtils.getName(getPath()).isEmpty())result = true;
            return result;
        }  
    }
    
    private Integer doAppointmentTableCountRequest(){
        Integer result = null;
        TheAppointment.Collection collection = new TheAppointment().getCollection();
        collection.setScope(TheAppointment.Scope.ALL);
        try{
            result = collection.count();
        }catch (StoreException ex){
            displayErrorMessage(
                    ex.getMessage() + "\n Exception handled in doAppointmentTableCountRequest()",
                    "Desktop View Controller error", JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private Integer doPatientTableCountRequest(){
        Integer result = null;
        ThePatient.Collection collection = new ThePatient().getCollection();
        try{
            result = collection.count();
        }catch (StoreException ex){
            displayErrorMessage(
                    ex.getMessage() + "\n Exception handled in doAppointmentTableCountRequest()",
                    "Desktop View Controller error", JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private Integer doPatientNotificationTableCountRequest(){
        Integer result = null;
        PatientNotification.Collection collection = new PatientNotification().getCollection();
        try{
            collection.setScope(PatientNotification.Scope.ALL);
            result = collection.count();
        }catch (StoreException ex){
            displayErrorMessage(
                    ex.getMessage() + "\n Exception handled in doPatientNotificationTableCountRequest()",
                    "Desktop View Controller error", JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private Integer doSurgeryDaysAssignmentTableCountRequest(){
        Integer result = null;
        SurgeryDaysAssignment surgeryDaysAssignment = new SurgeryDaysAssignment();
        try{
            result = surgeryDaysAssignment.count();
        }catch (StoreException ex){
            displayErrorMessage(
                    ex.getMessage() + "\n Exception handled in doAppointmentTableCountRequest()",
                    "Desktop View Controller error", JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private void doPropertyChangeEvent(DesktopViewControllerPropertyChangeEvent event,
                                        PropertyChangeListener view){
        pcSupport.removePropertyChangeListener(view);
        pcSupport.addPropertyChangeListener(view);
        PropertyChangeEvent pcEvent = new PropertyChangeEvent(
                this,event.toString(),
                null,getEntityDescriptor());
        pcSupport.firePropertyChange(pcEvent);
        //pcSupport.removePropertyChangeListener(view);
    }
    
    private void doGetPathRequest(DesktopViewControllerActionEvent actionCommand,
                                    Object source){
        String path = null;
        DesktopViewControllerPropertyChangeEvent propertyChangeEvent = null;
        try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            switch(actionCommand){
                case GET_APPOINTMENT_CSV_PATH_REQUEST:
                    path = storeManager.getAppointmentCSVPath();
                    getEntityDescriptor().setAppointmentCSVPath(path);
                    propertyChangeEvent = 
                            DesktopViewControllerPropertyChangeEvent.APPOINTMENT_CSV_PATH_RECEIVED;
                    break;
                case GET_PATIENT_CSV_PATH_REQUEST:
                    path = storeManager.getPatientCSVPath();
                    getEntityDescriptor().setPatientCSVPath(path);
                    propertyChangeEvent = 
                            DesktopViewControllerPropertyChangeEvent.PATIENT_CSV_PATH_RECEIVED;
                    break;
                case GET_PMS_STORE_PATH_REQUEST:
                    path = storeManager.getPMSStorePath();
                    getEntityDescriptor().setPMSStorePath(path);
                    propertyChangeEvent = 
                            DesktopViewControllerPropertyChangeEvent.PMS_STORE_PATH_RECEIVED;
                    break;
            }    
            doPropertyChangeEvent(propertyChangeEvent, (PropertyChangeListener)source); 
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage() 
                    + "\nException handled in doGetPathRequest()",
                    "Desktop View Controller error",
                    JOptionPane.WARNING_MESSAGE);
        }
              
    }
    
    private void notifyMigrationActionCompleted(){
        pcSupport.addPropertyChangeListener(view);
        PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
            DesktopViewController.DesktopViewControllerPropertyChangeEvent.MIGRATION_ACTION_COMPLETE.toString(),
            null,getEntityDescriptor());
        pcSupport.firePropertyChange(pcEvent);
        pcSupport.removePropertyChangeListener(view);
    }
    
    private boolean isPMSStoreDefined()throws StoreException{
        boolean result;
        StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
        String pmsStorePath = storeManager.getPMSStorePath();
        if (pmsStorePath != null)
            if (FilenameUtils.getBaseName(pmsStorePath).isEmpty()) result = false;
            else result = true;
        else result = false;
        return result;
    }
    private StoreManager getStoreManager(){
        return StoreManager.GET_STORE_MANAGER();
    }
}