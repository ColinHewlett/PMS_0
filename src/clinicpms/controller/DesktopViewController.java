/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import org.apache.commons.io.FilenameUtils;

import clinicpms.model.*;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Database;
import clinicpms.model.StoreManager;
import clinicpms.store.StoreException;
import clinicpms.view.DesktopView;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class DesktopViewController extends ViewController{
    private boolean isDesktopPendingClosure = false;
    private DesktopView view = null;
    private ArrayList<AppointmentViewController> appointmentViewControllers = null;
    private ArrayList<PatientViewController> patientViewControllers = null;
    //private ArrayList<DatabaseLocatorViewController> databaseLocatorViewControllers = null;
    private ArrayList<MigrationManagerViewController> migrationViewControllers = null;
    private static Boolean isDataMigrationOptionEnabled = null;
    private PropertyChangeSupport pcSupport = null;
    private EntityDescriptor entityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
   
    //private HashMap<ViewControllers,ArrayList<ViewController>> viewControllers = null;    
    enum ViewControllers {
                            PATIENT_VIEW_CONTROLLER,
                            APPOINTMENT_VIEW_CONTROLLER,
                            MIGRATION_VIEW_CONTROLLER
                         }
    
    public enum DesktopViewControllerActionEvent {
                                            IMPORT_DATA_FROM_SOURCE,
                                            EXPORT_MIGRATED_DATA,
                                            APPOINTMENT_HISTORY_CHANGE_NOTIFICATION,
                                            APPOINTMENT_VIEW_CONTROLLER_REQUEST,
                                            DATABASE_LOCATOR_REQUEST,
                                            MODAL_VIEWER_ACTIVATED,
                                            MODAL_VIEWER_CLOSED,
                                            MIGRATION_VIEW_CONTROLLER_REQUEST,
                                            PATIENT_VIEW_CONTROLLER_REQUEST,
                                            PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST,
                                            SET_CSV_APPOINTMENT_FILE_REQUEST,
                                            SET_CSV_PATIENT_FILE_REQUEST,
                                            MIGRATION_DATABASE_CREATION_REQUEST,
                                            MIGRATION_DATABASE_DELETION_REQUEST,
                                            MIGRATION_DATABASE_SELECTION_REQUEST,
                                            PMS_DATABASE_CREATION_REQUEST,
                                            PMS_DATABASE_DELETION_REQUEST,
                                            PMS_DATABASE_SELECTION_REQUEST,
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
                                            MIGRATION_ACTION_COMPLETE
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
        
        /**
         * Constructor for DesktopView takes two arguments
         * -- object reference to view controller (this)
         * -- Boolean signifying whether view enables data migration functions
         */
        view = new DesktopView(this, isDataMigrationOptionEnabled, new EntityDescriptor() );
        view.setSize(1020, 650);
        view.setVisible(true);
        setView(view);
        //view.setContentPane(view);
        pcSupport = new PropertyChangeSupport(this);
        appointmentViewControllers = new ArrayList<>();
        patientViewControllers = new ArrayList<>();
        //databaseLocatorViewControllers = new ArrayList<>();
        migrationViewControllers = new ArrayList<>();
        
        if (isDataMigrationOptionEnabled) this.doMigrationActionCompleteResponse(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        String s;
        s = e.getSource().getClass().getSimpleName();
        switch(s){
            case "DesktopView":
                doDesktopViewAction(e);
                 break;
            case "AppointmentViewController":
                doAppointmentViewControllerAction(e);
                break;
            case "PatientViewController":
                doPatientViewControllerAction(e);
                break;
            /*
            case "DatabaseLocatorViewController":
                doDatabaseLocatorViewControllerAction(e);
                break;
            */
            case "MigrationManagerViewController":
                doMigrationManagerViewControllerAction(e);
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
        AppointmentViewController avc = null;
        if (e.getActionCommand().equals(
            DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            /**
             * on reception of VIEW_CLOSED_NOTIFICATION action command
             * -- loops through the active appointment view controllers to find the ActionEvent sender
             * -- attempts to remove this controller from the collection of active controllers; displays error if unable to remove controller
             */
            Iterator<AppointmentViewController> viewControllerIterator = 
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
                    /**
                     * re-enable view's data menu, if it exists
                     */
                    
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
                    
                    getView().enableDataControl();
                    getView().enableWindowCloseControl();
                    
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
            EntityDescriptor edOfPatientWithAppointmentHistoryChange = ((AppointmentViewController)e.getSource()).getEntityDescriptorFromView();
            int k2 = edOfPatientWithAppointmentHistoryChange.getAppointment().getAppointee().getData().getKey();
            Iterator<PatientViewController> viewControllerIterator = 
                    this.patientViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                PatientViewController pvc = viewControllerIterator.next();               
                int k1 = pvc.getEntityDescriptorFromView().getRequest().getPatient().getData().getKey();
                if (k1==k2){
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
                Color.BLACK,Color.BLUE);
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            
            pcSupport.addPropertyChangeListener(view);
            pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.ENABLE_DESKTOP_WINDOW_CONTROL.toString(),
                Color.BLACK,Color.BLUE);
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            /**
             * on receipt of ENABLE_DESKTOP_CONTROLS_REQUEST
             * -- sends desktop view controller sends message to its view to re-enable VIEW and DATA controls as well as its window closing control
             
            getView().enableViewControl();
            getView().enableWindowCloseControl();
            */
        }
        /*
        else if (e.getActionCommand().equals(
            DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){
            
            pcSupport.addPropertyChangeListener(view);
            PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.DISABLE_DESKTOP_VIEW_CONTROL.toString(),
                Color.BLACK,Color.BLUE);
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            
            pcSupport.addPropertyChangeListener(view);
            pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.DISABLE_DESKTOP_WINDOW_CONTROL.toString(),
                Color.BLACK,Color.BLUE);
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
        */
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
                    getView().enableDataControl();
                    getView().enableWindowCloseControl();
                }
            }
        }
        else if (e.getActionCommand().equals(
            EntityDescriptor.PatientViewControllerActionEvent.
                    APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString())){
            PatientViewController patientViewController = (PatientViewController)e.getSource();
            Optional<EntityDescriptor> ed = Optional.of(patientViewController.getEntityDescriptorFromView());
            createNewAppointmentViewController(ed);
            
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
            getView().enableViewControl();
            getView().enableDataControl();
            getView().enableWindowCloseControl();
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
        DesktopViewControllerActionEvent actionCommand =
                DesktopViewController.DesktopViewControllerActionEvent.valueOf(e.getActionCommand());
        switch (actionCommand){
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
            case IMPORT_DATA_FROM_SOURCE:{
                doImportDataFromSource();
                break;
            }    
            case MIGRATION_DATABASE_CREATION_REQUEST:{
                doMigrationDatabaseCreationRequest();
                break;
            }
            case MIGRATION_DATABASE_DELETION_REQUEST:{
                doMigrationDatabaseDeletionRequest();
                break;
            }
            case MIGRATION_DATABASE_SELECTION_REQUEST:{
                doMigrationDatabaseSelectionRequest();
                break;
            }
            case PMS_DATABASE_CREATION_REQUEST:{
                doPMSDatabaseCreationRequest();
                break;
            } 
            case PMS_DATABASE_DELETION_REQUEST:{
                doPMSDatabaseDeletionRequest();
                break;
            }
            case PMS_DATABASE_SELECTION_REQUEST:{
                doPMSDatabaseSelectionRequest();
                break;
            }
            case SET_CSV_APPOINTMENT_FILE_REQUEST:{
                doCSVAppointmentFileRequest();
                break;
            }
            case SET_CSV_PATIENT_FILE_REQUEST:{
                doCSVPatientFileRequest();
                break;
            }
            case EXPORT_MIGRATED_DATA:{
                try{
                    doExportMigratedData();
                }catch (StoreException ex){
                    displayErrorMessage(ex.getMessage() + "\nRaised in doExportMigratedData()","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                }
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
            Iterator<AppointmentViewController> avcIterator = appointmentViewControllers.iterator();
            while(avcIterator.hasNext()){
                AppointmentViewController avc = avcIterator.next();
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
    
    private void createNewMigrationViewController(){
        try{
            this.migrationViewControllers.add(new MigrationManagerViewController(this, getView()));
            MigrationManagerViewController mvc = 
                        migrationViewControllers.get(migrationViewControllers.size()-1);
        }
        catch(StoreException ex){
                displayErrorMessage(ex.getMessage() + "\nUnable to create MigrationManagerViewController","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
        finally{
            getView().enableDataControl();
            getView().enableWindowCloseControl();
        }
    }
    
    private void createNewAppointmentViewController(Optional<EntityDescriptor> ed){
        try{
                appointmentViewControllers.add(
                                            new AppointmentViewController(this, getView(),ed));
                AppointmentViewController avc = 
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
    
    private void doMigrationActionCompleteResponse(boolean includeTableRowCounts){
       setEntityDescriptor(new EntityDescriptor());
       try{
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            this.
            getEntityDescriptor().
                    getMigrationDescriptor().
                    setMigrationDatabaseSelection(MigrationDatabase.getPath());
              
            getEntityDescriptor().
                    getMigrationDescriptor().
                    setAppointmentCSVFilePath(storeManager.getAppointmentCSVPath());
            getEntityDescriptor().
                    getMigrationDescriptor().
                    setPatientCSVFilePath(storeManager.getPatientCSVPath());
            getEntityDescriptor().getMigrationDescriptor().setPMSDatabaseSelection(storeManager.getPMSTargetStorePath());
            
            if (MigrationDatabase.isSelected()){
                    getEntityDescriptor().getMigrationDescriptor().setAppointmentTableCount(new AppointmentTable().count());
                    getEntityDescriptor().getMigrationDescriptor().setPatientTableCount(new PatientTable().count());
                    getEntityDescriptor().getMigrationDescriptor().setSurgeryDaysAssignmentTableCount(new SurgeryDaysAssignmentTable().count());
                    //getEntityDescriptor().getMigrationDescriptor().setAppointmentsCount(new Appointments().count());
                    //getEntityDescriptor().getMigrationDescriptor().setPatientsCount(new Patients().count());
            }
            
            getEntityDescriptor().
                    getMigrationDescriptor().
                    setPMSDatabaseSelection(PMSDatabase.getPath());
            if (PMSDatabase.isSelected()){
                getEntityDescriptor().getMigrationDescriptor().setAppointmentsCount(new Appointments().count());
                getEntityDescriptor().getMigrationDescriptor().setPatientsCount(new Patients().count());
                getEntityDescriptor().getMigrationDescriptor().setSurgeryDaysAssignmentCount(new SurgeryDaysAssignment().count());
                
            }
            pcSupport.addPropertyChangeListener(view);
            PropertyChangeEvent pcEvent = new PropertyChangeEvent(this,
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.MIGRATION_ACTION_COMPLETE.toString(),
                null,getEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
            String test = storeManager.getPatientCSVPath();
            if (test==null){}
        }catch (StoreException ex){
            displayErrorMessage("Migration database connection failure","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }  
    }
    
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
            if (this.appointmentViewControllers.isEmpty()||this.patientViewControllers.isEmpty()){
                requestViewControllersToCloseViews();
            }
            else {
                getView().dispose();
                System.exit(0);
            }    
        }
    }
    
    private void doAppointmentViewControllerRequest(){
        createNewAppointmentViewController(Optional.ofNullable(null));
        getView().disableDataControl();
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
            getView().disableDataControl();
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
                
    private void doMigrationDatabaseCreationRequest(){
        try{
            /**
             * 07/12/2021 19:17 updates
             */
            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getMigrationTargetStorePath();
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            /**
             * display contents of currently selected folder
             * but do not display the currently selected file (if NY)
             */

            targetPath = removeFilenameFrom(targetPath);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Create migration database");
            chooser.setApproveButtonText("Create database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath,"Enter name of migration database to create"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.exists()){
                    file = setExtensionFor(file, ".accdb");
                    DatabaseBuilder.create(Database.FileFormat.V2016, file);
                    storeManager.setMigrationTargetStorePath(file.getPath());
                    PatientTable patientTable = new PatientTable();
                    patientTable.create();
                    AppointmentTable appointmentTable = new AppointmentTable();
                    appointmentTable.create();

                    SurgeryDaysAssignmentTable surgeryDaysAssignmentTable = new SurgeryDaysAssignmentTable();
                    surgeryDaysAssignmentTable.create();
                    surgeryDaysAssignmentTable.populate();

                   JOptionPane.showMessageDialog(getView(),
                        storeManager.getMigrationTargetStorePath(),
                        "Current migration database path", 
                        JOptionPane.INFORMATION_MESSAGE);

                }
                else{
                    String filename = file.toPath().getName(file.toPath().getNameCount()-1).toString();
                    displayErrorMessage("Migration database file -> " + 
                            filename + " already exists","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                    JOptionPane.showMessageDialog(getView(),
                            storeManager.getMigrationTargetStorePath(),
                            "Current migration database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                this.doMigrationActionCompleteResponse(true);
            }
               
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
        catch (IOException ex){
            String message = "IOException -> raised on attempt to create a new Access database in DesktopControllerActionEvent.MIGRATION_DATABASE_CREATION_REQUEST";
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
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
                    /**
                     * if the migration database to be deleted is also the selected migration database
                     * -- remove the selected filename from the selected migration database path
                     * Delete database file selected for deletion
                     * if attempt to delete the database fails
                     * -- restore the original selected migration database path
                     */
                    
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
                    this.doMigrationActionCompleteResponse(false);
                }
                else{
                    displayErrorMessage("Migration database file -> " + file.getPath() + " cannot be located","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                    /*JOptionPane.showMessageDialog(getView(),
                                              new ErrorMessagePanel(ex.getMessage()));
                    */
                }
                
            }
            
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
    }
    
    private void doMigrationDatabaseSelectionRequest(){
        try{
            /**
             * 07/12/2021 19:17 updates
             */
            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getMigrationTargetStorePath();
            String storageType = storeManager.getStorageType();
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            File path = addDirectionIfFilenameMissing(targetPath, "Select migration database to use");
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select migration database to use");
            chooser.setApproveButtonText("Select database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(path);
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file.exists()){
                    storeManager.setMigrationTargetStorePath(file.getPath());

                    JOptionPane.showMessageDialog(getView(),
                            storeManager.getMigrationTargetStorePath(),
                            "Current migration database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    displayErrorMessage("Migration database -> " + file.getPath() + " does not exist","DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                } 
                this.doMigrationActionCompleteResponse(true);
            }
            

        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
    }
    
    private void doPMSDatabaseCreationRequest(){
        try{
            /**
             * 07/12/2021 19:17 updates
             */
            FileNameExtensionFilter filter = null;
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();
            String targetPath = storeManager.getPMSTargetStorePath();
            String storageType = storeManager.getStorageType();
            filter = new FileNameExtensionFilter("Access database files", "accdb");

            /**
             * display contents of currently selected folder
             * but not the currently selected file
             */
            targetPath = removeFilenameFrom(targetPath);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Create PMS database");
            chooser.setApproveButtonText("Create database");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(addDirectionIfFilenameMissing(targetPath,"Enter name of PMS database to create"));
            int returnVal = chooser.showOpenDialog(getView());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.exists()){
                    file = setExtensionFor(file, ".accdb");
                    Database db = DatabaseBuilder.create(Database.FileFormat.V2016, file);
                    storeManager.setPMSTargetStorePath(file.getPath());
                    
                    Patient patient = new Patient();
                    patient.create();
                    Appointment appointment = new Appointment();
                    appointment.create();

                    SurgeryDaysAssignment surgeryDaysAssignment = new SurgeryDaysAssignment();
                    surgeryDaysAssignment.create();
                    

                    JOptionPane.showMessageDialog(getView(),
                            storeManager.getPMSTargetStorePath(),
                            "Current migration database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    displayErrorMessage("PMS database file -> " + file.getPath() + " cannot be created because it already exists",
                            "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                    /*JOptionPane.showMessageDialog(getView(),
                                              new ErrorMessagePanel(ex.getMessage()));
                    */
                }
                this.doMigrationActionCompleteResponse(true);  
            }
              
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
        catch (IOException ex){
            String message = "IOException -> raised when attempting to create a new PMS database in DesktopControllerActionEvent.MIGRATION_DATABASE_CREATION_REQUEST";
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
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
                    /**
                     * if the migration database to be deleted is also the selected migration database
                     * -- remove the selected filename from the selected migration database path
                     * Delete database file selected for deletion
                     * if attempt to delete the database fails
                     * -- restore the original selected migration database path
                     */
                    
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
                    /*JOptionPane.showMessageDialog(getView(),
                                              new ErrorMessagePanel(ex.getMessage()));
                    */
                }
                
            }
            
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }

    }
    
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
                    /*JOptionPane.showMessageDialog(getView(),
                                              new ErrorMessagePanel(ex.getMessage()));
                    */
                }
                this.doMigrationActionCompleteResponse(true);
            }
            

        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            /*
            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            */
        }
    }
     
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
            this.doMigrationActionCompleteResponse(true);
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
            this.doMigrationActionCompleteResponse(true);
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);

            JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
        }
    }
    
    private void doExportMigratedData()throws StoreException{
        boolean isError = false;
        /**
         * can only continue if both a migration and PMS database target has been selected 
         */
        try{
            if (MigrationDatabase.isSelected()){
                if (PMSDatabase.isSelected()){
                    Patient patient = new Patient();
                    Appointment appointment = new Appointment();
                    SurgeryDaysAssignment surgeryDaysAssignment = new SurgeryDaysAssignment();
                    IEntityStoreType entity = null;
                    
                    /**
                     * recreate the currently selected PMS target database
                     * -- assumption if file already exists it will overwrite it with a new database
                     */
                    File file = new File(getEntityDescriptor().getMigrationDescriptor().getPMSDatabaseSelection());
                    Database db = DatabaseBuilder.create(Database.FileFormat.V2016, file);
                    
                    /**
                     * create a set of new tables
                     * -- patient before appointment table because of dependency
                     */
                    patient.create();
                    
                    if (!isError){
                        PatientTable patientTable = new PatientTable();
                        entity = patientTable.read();
                        if (entity!=null){
                            if (entity.isPatients()){
                                Patients patients = (Patients)entity;
                                Iterator<Patient> itPatients = patients.iterator(); 
                                while(itPatients.hasNext()) {
                                    patient = (Patient)itPatients.next();
                                    patient.insert();
                                }
                            }else{
                                isError = true;
                                displayErrorMessage("Patient entity expected but not encountered in AccessStore::doExportMigratedData()",
                                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                            }
                        }else{
                            isError = true;
                            displayErrorMessage("Patient entity expected but null encountered in AccessStore::doExportMigratedData()",
                                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    
                    surgeryDaysAssignment.create();
                    if (!isError){
                        SurgeryDaysAssignmentTable surgeryDaysAssignmentTable = new SurgeryDaysAssignmentTable();
                        entity = surgeryDaysAssignmentTable.read();
                        if (entity!=null){
                            if (entity.isSurgeryDaysAssignment()){
                                surgeryDaysAssignment = (SurgeryDaysAssignment)entity;
                                surgeryDaysAssignment.update();
                            }else{
                                isError = true;
                                displayErrorMessage("SurgeryDaysAssignment entity expected but not encountered in AccessStore::doExportMigratedData()",
                                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                            }
                        }else{
                            isError = true;
                            displayErrorMessage("SurgeryDaysAssignment entity expected but null encountered in AccessStore::doExportMigratedData()",
                                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    
                    appointment.create();
                    if (!isError){
                        AppointmentTable appointmentTable = new AppointmentTable();
                        entity = appointmentTable.read();
                        if (entity!=null){
                            if (entity.isAppointments()){
                                Appointments appointments = (Appointments)entity;
                                Iterator<Appointment> itAppointments = appointments.iterator(); 
                                while(itAppointments.hasNext()) {
                                    appointment = (Appointment)itAppointments.next();
                                    appointment.insert();
                                }
                            }else{
                                displayErrorMessage("Appointment entity expected but not encountered in AccessStore::doExportMigratedData()",
                                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                            }
                        }else{
                            displayErrorMessage("Appointment entity expected but null encountered in AccessStore::doExportMigratedData()",
                                    "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                        }
                    }
                
                }else{
                    isError = true;
                    displayErrorMessage("PMS database has not been selected",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                }  
            }else{
                isError = true;
                displayErrorMessage("Migration database has not been selected",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE); 
            }   
            this.doMigrationActionCompleteResponse(true);
            
        }catch (StoreException ex){
            displayErrorMessage("SQLException -> " + ex.getMessage() + "\nStoreException raised in AccessStore::doExportMigratedData",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }catch (IOException ex){
            displayErrorMessage("IOException -> " + ex.getMessage() + "\nStoreException raised in AccessStore::doExportMigratedData",
                        "DesktopViewController error",JOptionPane.WARNING_MESSAGE);
        }
        
    }
    
    private void doImportDataFromSource(){
        AppointmentTable appointmentTable = new AppointmentTable();
        PatientTable patientTable = new PatientTable();
        SurgeryDaysAssignmentTable surgeryDaysAssignmentTable = new SurgeryDaysAssignmentTable();
        

        try{
            appointmentTable.drop();
            patientTable.drop();
            surgeryDaysAssignmentTable.drop();

            patientTable.create();
            patientTable.populate();

            appointmentTable.create();
            appointmentTable.populate();

            surgeryDaysAssignmentTable.create();
            surgeryDaysAssignmentTable.populate();

            this.doMigrationActionCompleteResponse(true);
        }catch (StoreException ex){
            //displayErrorMessage(ex.getMessage(),"MigrationManagerViewController error",JOptionPane.WARNING_MESSAGE);
            JOptionPane.showMessageDialog(null,new ErrorMessagePanel(ex.getMessage()));
        }  
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
            String test = FilenameUtils.getName(storeManager.getMigrationTargetStorePath());
            return storeManager.getMigrationTargetStorePath();
        }
        
        static boolean isSelected()throws StoreException{
            boolean result = false;
            if (!FilenameUtils.getName(getPath()).isEmpty())result = true;
            return result;
        }  
    }
    
    static class PMSDatabase {  

        static String getPath()throws StoreException{ 
            StoreManager storeManager = StoreManager.GET_STORE_MANAGER();           
            return storeManager.getPMSTargetStorePath();
        }
        
        static boolean isSelected()throws StoreException{
            boolean result = false;
            String test = FilenameUtils.getName(getPath());
            if (!FilenameUtils.getName(getPath()).isEmpty())result = true;
            return result;
        }  
    }
}
