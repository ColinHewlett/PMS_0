/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.store.AccessStore;
import clinicpms.store.Store;
import clinicpms.store.Store.ExceptionType;
import clinicpms.store.DbLocationStorex;
import clinicpms.store.Store.Storage;
import clinicpms.store.exceptions.StoreException;
import clinicpms.view.DesktopView;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import javax.swing.border.Border;
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
    private ArrayList<DatabaseLocatorViewController> databaseLocatorViewControllers = null;
    private ArrayList<MigrationManagerViewController> migrationViewControllers = null;
    private static Boolean isDataMigrationOptionEnabled = null;
   
    //private HashMap<ViewControllers,ArrayList<ViewController>> viewControllers = null;    
    enum ViewControllers {
                            PATIENT_VIEW_CONTROLLER,
                            APPOINTMENT_VIEW_CONTROLLER,
                            MIGRATION_VIEW_CONTROLLER
                         }
    @Override
    public EntityDescriptor getEntityDescriptorFromView(){
        return null;
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
        view = new DesktopView(this, isDataMigrationOptionEnabled );
        view.setSize(1020, 650);
        view.setVisible(true);
        setView(view);
        //view.setContentPane(view);
        
        appointmentViewControllers = new ArrayList<>();
        patientViewControllers = new ArrayList<>();
        databaseLocatorViewControllers = new ArrayList<>();
        migrationViewControllers = new ArrayList<>();
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
            case "DatabaseLocatorViewController":
                doDatabaseLocatorViewControllerAction(e);
                break;
            case "MigrationManagerViewController":
                doMigrationManagerViewControllerAction(e);
        }
    }
    
    private void doAppointmentViewControllerAction(ActionEvent e){
        AppointmentViewController avc = null;
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
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
                ViewController.DesktopViewControllerActionEvent.
                        APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString())){
            /**
             * on APPOINTMENT_HISTORY_CHANGE_NOTIFICATION from an appointment view controller
             * -- desktop view controller checks if any patient view controllers active which refer to same patient
             * -- yes: send them an APPOINTMENT_HISTORY_CHANGE_NOTIFICATION to refresh their appointment history
             */
            EntityDescriptor edOfPatientWithAppointmentHistoryChange = ((AppointmentViewController)e.getSource()).getEntityDescriptorFromView();
            PatientViewController pvc = null;
            Iterator<PatientViewController> viewControllerIterator = 
                    this.patientViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                pvc = viewControllerIterator.next();               
                int k1 = pvc.getEntityDescriptorFromView().getRequest().getPatient().getData().getKey().intValue();
                int k2 = edOfPatientWithAppointmentHistoryChange.getRequest().getPatient().getData().getKey().intValue();
                if (pvc.getEntityDescriptorFromView().getRequest().getPatient().getData().getKey().intValue() == 
                        edOfPatientWithAppointmentHistoryChange.getRequest().getPatient().getData().getKey().intValue()){
                    /**
                     * Found patient view controller for patient whose appointment history has been changed
                     * -- patient view controller's EntityDescriptor.Request.Patient points to appointee
                     * -- send PATIENT_REQUEST to patient view controller to refresh display of patient data
                     */
                    ActionEvent actionEvent = new ActionEvent(
                            this,ActionEvent.ACTION_PERFORMED,
                            ViewController.DesktopViewControllerActionEvent.APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString());
                    pvc.actionPerformed(actionEvent);
                }
            }  
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    ENABLE_DESKTOP_CONTROLS_REQUEST.toString())){
            getView().enableViewControl();
            getView().enableWindowCloseControl();
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    DISABLE_DESKTOP_CONTROLS_REQUEST.toString())){
            getView().disableViewControl();
            getView().disableWindowClosedControl();
        }
    }
    private void doPatientViewControllerAction(ActionEvent e){
        PatientViewController pvc = null;
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
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
            ViewController.PatientViewControllerActionEvent.
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
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            getView().enableViewControl();
            getView().enableDataControl();
            getView().enableWindowCloseControl();
        }
        
    }
    
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
    /**
     * 
     * @param e source of event is DesktopView object
     */
    private void doDesktopViewAction(ActionEvent e){  
        if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString())){
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
        /**
         * MIGRATION_VIEW_CONTROLLER_REQUEST -> 
         * -- on receipt of this request the menu option to raise the request in the view must be enabled
         * ---- therefore neither appointment or patient view controllers can be active either
         * ---- nor can there be an active migration view controller, else the view's "data menu would be disabled
         * -- after constructing a MigrationViewController the "View" menu option in the view is disabled
         * -- as is the "Data" option in the view which prevents multiple copies of the migration view controller
         */
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.MIGRATION_VIEW_CONTROLLER_REQUEST.toString())){
            getView().disableDataControl();
            getView().disableViewControl();
            createNewMigrationViewController();
            
            
        }
        /**
         * APPOINTMENT_VIEW_CONTROLLER_REQUEST -> allowed
         * -- if migration view controller active this request would never have been made (menu option disabled)
         * -- action posted to migration view controller to disable its "data" menu
         */
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString())){
            createNewAppointmentViewController(Optional.ofNullable(null));
            /**
             * disable data menu in the desktop view, if it exists
             */
            getView().disableDataControl();
            
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    PATIENT_VIEW_CONTROLLER_REQUEST.toString())){
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
        else if (e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.DATABASE_LOCATOR_REQUEST.toString())){
            if (!databaseLocatorViewControllers.isEmpty()){
                JOptionPane.showInternalMessageDialog(view.getContentPane(), "Only one Database Locator can be active");
            }
            else{
                try{
                    databaseLocatorViewControllers.add(
                                            new DatabaseLocatorViewController(this, getView()));
                    DatabaseLocatorViewController dvc = databaseLocatorViewControllers.get(0);

                    this.getView().getDeskTop().add(dvc.getView());
                    dvc.getView().setVisible(true);
                    dvc.getView().setClosable(false);
                    dvc.getView().setMaximizable(false);
                    dvc.getView().setIconifiable(true);
                    dvc.getView().setResizable(false);
                    dvc.getView().setSelected(true);
                    dvc.getView().setSize(600,250);
                }

                catch (PropertyVetoException ex){
                    displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                    /*
                    JOptionPane.showMessageDialog(getView(),
                                              new ErrorMessagePanel(ex.getMessage()));
                    */
                }
            }

        }
        /**
         * user has attempted to close the desktop view
         */
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            System.exit(0);
        }
        /**
         * SET_MIGRATION_DATABASE_LOCATION_REQUEST raised by user in the desktop view
         * -- this is accomplished via a file dialog directly without the need for s view and a separate controller
         * 
         */
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.SET_MIGRATION_DATABASE_LOCATION_REQUEST.toString())){
            /**
             * SET_MIGRATION_DATABASE_LOCATION_REQUEST ->
             * -- configure a file chooser to select the file and folder the current setting in the TARGETS_DATABASE for the migration database
             * -- use a standard dialog to inform the user of the results of the update
             */
            try{
                if (Store.getMigrationDatabasePath()==null){
                    AccessStore.getInstance();
                }
                String targetPath = Store.getMigrationDatabasePath();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Access database files", "accdb");
                File path = new File(targetPath);
                JFileChooser chooser = new JFileChooser(path);
                chooser.setFileFilter(filter);
                chooser.setSelectedFile(path);
                int returnVal = chooser.showOpenDialog(getView());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    String check1 = chooser.getSelectedFile().getPath();
                    AccessStore.getInstance().getTargetsDatabase().update(check1,"MIGRATION_DB");
                    Store.setMigrationDatabasePath(AccessStore.getInstance().getTargetsDatabase().read("MIGRATION_DB"));
                    JOptionPane.showMessageDialog(getView(),
                            Store.getMigrationDatabasePath(),
                            "Current migration database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
            }
        }
        /**
         * SET_PMS_DATABASE_LOCATION_REQUEST raised by user in the desktop view
         * -- this is accomplished via a file dialog directly without the need for s view and a separate controller
         */
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.SET_PMS_DATABASE_LOCATION_REQUEST.toString())){
            /**
             * SET_PMS_DATABASE_LOCATION_REQUEST ->
             * -- configure a file chooser to select the file and folder of the current migration database setting in the TARGETS_DATABASE
             * -- use a standard dialog to inform the user of the results of the update
             */
            try{
                if (Store.getPMSDatabasePath()==null){
                    AccessStore.getInstance();
                }
                String targetPath = Store.getPMSDatabasePath();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Access database files", "accdb");
                File path = new File(targetPath);
                JFileChooser chooser = new JFileChooser(path);
                chooser.setFileFilter(filter);
                chooser.setSelectedFile(path);
                int returnVal = chooser.showOpenDialog(getView());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    String check1 = chooser.getSelectedFile().getPath();
                    AccessStore.getInstance().getTargetsDatabase().update(check1,"PMS_DB");
                    Store.setPMSDatabasePath(AccessStore.getInstance().getTargetsDatabase().read("PMS_DB"));
                    JOptionPane.showMessageDialog(getView(),
                            Store.getPMSDatabasePath(),
                            "Current PMS database path", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
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
            /**
             * Location of the targets database (DbLocation.accb) now stored as an OS environment variable
             */
            String s = System.getenv("TARGETS_DATABASE");
            Store.setDatabaseLocatorPath(System.getenv("TARGETS_DATABASE"));
            /**
             * checks first command line argument -> persistent store format
             */
            if (args.length > 0){
                switch (args[0]){
                    case "ACCESS":
                        Store.setStorageType(Storage.ACCESS);
                        break;
                    case "CSV":
                        Store.setStorageType(Storage.CSV);
                        break;
                    case "POSTGRES":
                        Store.setStorageType(Storage.POSTGRES);
                        break;
                    case "SQL_EXPRESS":
                        Store.setStorageType(Storage.SQL_EXPRESS);
                        break;
                    default:
                        Store.setStorageType(Storage.UNDEFINED_DATABASE);
                        usageError = "usage error: target database format has not been defined";
                        isCommandLineError = true;
                }
            }
            else {
                isCommandLineError = true;
                usageError = "usage error: expects at least 1 command line parameters which define the target persistent store format.";
            }
           
            /**
             * checks for 2nd command line argument -> if present enables access in app to data migration function
             */
            if (!isCommandLineError){
                if (args.length > 1){
                    if (args[1].equals("DATA_MIGRATION_ENABLED")){
                        isDataMigrationOptionEnabled = true;
                    }
                    else isDataMigrationOptionEnabled = false;
                }
                else isDataMigrationOptionEnabled = false;
            }
            
             
            /**
             * checks second command line argument -> location of DbLocation.accb which defines persistent store location
             */
            /*
            if (!isCommandLineError){
                if (args.length > 1){
                    String path = args[1];
                    File targetDB = new File(path);
                    if (!targetDB.exists()){
                        isCommandLineError = true;
                        usageError = "usage error: cannot locate the specified target database file";    
                    }
                    else Store.setDatabaseLocatorPath(args[1]);
                }
                else {
                    isCommandLineError = true;
                    usageError = "usage error: 2nd command line argument expected defining location DbLocation.accb";
                }
            }
            */
       
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            /*
            javax.swing.UIManager.getDefaults().put("TableHeader.cellBorder",new LineBorder(Color.RED,2));
            border = javax.swing.UIManager.getBorder("TableHeader.cellBorder");
            */
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        //start PMS app if no errors processing command line
        if (!isCommandLineError){
            //Schedule a job for the event-dispatching thread:
            //creating and showing this application's GUI.
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new DesktopViewController();
                }
            });
        }
        
        else { // or exit with usage error message
            System.out.println(usageError);
            System.exit(0);
        }
        
    }
    
    private void requestViewControllersToCloseViews(){
        if (this.patientViewControllers.size() > 0){
            Iterator<PatientViewController> pvcIterator = patientViewControllers.iterator();
            while(pvcIterator.hasNext()){
                PatientViewController pvc = pvcIterator.next();
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
                pvc.actionPerformed(actionEvent);    
            }
        }
        
        if (this.appointmentViewControllers.size() > 0){
            Iterator<AppointmentViewController> avcIterator = appointmentViewControllers.iterator();
            while(avcIterator.hasNext()){
                AppointmentViewController avc = avcIterator.next();
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
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
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
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
    
    
}
