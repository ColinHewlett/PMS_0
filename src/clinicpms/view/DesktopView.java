/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

//import clinicpms.controller.ViewController.DesktopViewControllerActionEvent;
import clinicpms.controller.DesktopViewController;
import clinicpms.controller.DesktopViewController.DesktopViewControllerPropertyChangeEvent;
import clinicpms.controller.EntityDescriptor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.io.File;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 *
 * @author colin
 */
public class DesktopView extends javax.swing.JFrame implements PropertyChangeListener{
    private EntityDescriptor entityDescriptor = null;
    private final String SELECT_VIEW_MENU_TITLE = "Select view";
        private final String APPOINTMENT_VIEW_REQUEST_TITLE = "Appointment";
        private final String PATIENT_VIEW_REQUEST_TITLE = "Patient";
        private final String PATIENT_NOTIFICATION_VIEW_REQUEST = "Patient notifications";
        
        private final String EXIT_VIEW_REQUEST_TITLE = "Exit the Clinic practice management system";
    
    private final String MIGRATION_MANAGEMENT_MENU_TITLE = "MigrationManagement";
        private final String MIGRATION_DATABASE_TITLE = "Migration database";
            private final String MIGRATION_DATABASE_SELECTION_REQUEST_TITLE = "Select migration database to use";
            private final String MIGRATION_DATABASE_CREATION_REQUEST_TITLE = "Create a new migration database";
            private final String MIGRATION_DATABASE_DELETION_REQUEST_TITLE = "Delete migration database";
        private final String CSV_SOURCE_FILES_TITLE = "CSV files";
            private final String APPOINTMENT_CSV_SELECTION_REQUEST_TITLE = "Select appointment CSV file to use";
            private final String PATIENT_CSV_SELECTION_REQUEST_TITLE = "Select patient CSV file to use";
        private final String MIGRATION_DATABASE_CONTENTS_TITLE = "Database contents";
            private final String APPOINTMENT_TABLE_RECORD_COUNT_TITLE = "AppointmentTable ";
            private final String PATIENT_TABLE_RECORD_COUNT_TITLE = "PatientTable ";
            private final String SURGERY_DAYS_TABLE_RECORD_COUNT_TITLE = "SurgeryDaysTable ";
        private final String IMPORT_MIGRATED_DATA_REQUEST_TITLE = "Import data from CSV files";    
        
        private final String PMS_DATABASE_TITLE = "PMS database";
            private final String PMS_DATABASE_SELECTION_REQUEST_TITLE = "Select PMS database to use";
            private final String PMS_DATABASE_CREATION_REQUEST_TITLE = "Create a new PMS database";
            private final String PMS_DATABASE_DELETION_REQUEST_TITLE = "Delete PMS database";
        private final String PMS_DATABASE_CONTENTS_TITLE = "Database contents";
            private final String APPOINTMENT_RECORD_COUNT_TITLE = "Appointment ";
            private final String PATIENT_RECORD_COUNT_TITLE = "Patient ";
            private final String SURGERY_DAYS_RECORD_COUNT_TITLE = "SurgeryDays ";
        private final String EXPORT_MIGRATED_DATA_TO_PMS_REQUEST_TITLE = "Export migrated data to PMS";
    
    private JMenu mnuSelectView = null; 
        private JMenuItem mniAppointmentViewRequest = null;
        private JMenuItem mniPatientViewRequest = null;
        private JMenuItem mniPatientNotificationViewRequest = null;
        private JMenuItem mniExitViewRequest = null;
        
    private JMenu mnuMigrationManagement = null; 
        private JMenu mnuMigrationDatabase2 = null;
            private JMenuItem mniMigrationDatabaseSelectionRequest = null;
            private JMenuItem mniMigrationDatabaseCreationRequest = null;
            private JMenuItem mniMigrationDatabaseDeletionRequest = null; 
        private JMenu mnuCSVSourceFiles = null;
            private JMenuItem mniAppointmentCSVSelectionRequest = null;
            private JMenuItem mniPatientCSVSelectionRequest = null;
        private JMenu mnuMigrationDatabaseContents = null; 
            private JMenuItem mniAppointmentTableRecordCount = null;
            private JMenuItem mniPatientTableRecordCount = null;
            private JMenuItem mniSurgeryDaysTableRecordCount = null;
        private JMenuItem mniImportMigratedDataRequest = null;
        
        private JMenu mnuPMSDatabase2 = null;
            private JMenuItem mniPMSDatabaseSelectionRequest = null;
            private JMenuItem mniPMSDatabaseCreationRequest = null;
            private JMenuItem mniPMSDatabaseDeletionRequest = null; 
        private JMenu mnuPMSDatabaseContents = null; 
            private JMenuItem mniAppointmentRecordCount = null;
            private JMenuItem mniPatientRecordCount = null;
            private JMenuItem mniSurgeryDaysRecordCount = null;
        private JMenuItem mniExportMigratedDataToPMSRequest = null;
        
    private JMenuBar mnbDesktopView = null;
    
    private void addTestMenu(){
        makeSelectViewMenu();
        makeMigrationManagementMenu();
        mnbDesktop.add(mnuSelectView);
        mnbDesktop.add(mnuMigrationManagement);
    }

    private void makeSelectViewMenu(){
        mnuSelectView = new JMenu(SELECT_VIEW_MENU_TITLE);
        mniAppointmentViewRequest = new JMenuItem(APPOINTMENT_VIEW_REQUEST_TITLE);
        mniPatientViewRequest = new JMenuItem(PATIENT_VIEW_REQUEST_TITLE);
        mniPatientNotificationViewRequest = new JMenuItem(PATIENT_NOTIFICATION_VIEW_REQUEST);
        mniExitViewRequest = new JMenuItem(EXIT_VIEW_REQUEST_TITLE);
        mnuSelectView.add(mniAppointmentViewRequest);
        mnuSelectView.add(mniPatientViewRequest);
        mnuSelectView.add(mniPatientNotificationViewRequest);
        mnuSelectView.add(new JSeparator());
        mnuSelectView.add(mniExitViewRequest);
        
        mniAppointmentViewRequest.addActionListener((ActionEvent e) -> mniAppointmentViewRequestActionPerformed());
        mniPatientViewRequest.addActionListener((ActionEvent e) -> mniPatientViewRequestActionPerformed());
        mniPatientNotificationViewRequest.addActionListener((ActionEvent e) -> mniPatientNotificationViewRequestActionPerformed());
        mniExitViewRequest.addActionListener((ActionEvent e) -> mniExitRequestViewActionPerformed());
    }
    
    private void makeMigrationManagementMenu(){
        mnuMigrationManagement = new JMenu(MIGRATION_MANAGEMENT_MENU_TITLE);
        mnuMigrationDatabase2 = new JMenu(MIGRATION_DATABASE_TITLE);
        makeMigrationDatabasePopupMenu();
        mnuCSVSourceFiles = new JMenu(CSV_SOURCE_FILES_TITLE);
        makeCSVSourceFilesPopupMenu();
        mnuMigrationDatabaseContents = new JMenu(MIGRATION_DATABASE_CONTENTS_TITLE);
        makeMigrationDatabaseContentsPopupMenu();
        mniImportMigratedDataRequest = new JMenuItem(IMPORT_MIGRATED_DATA_REQUEST_TITLE);
        mnuPMSDatabase2 = new JMenu(PMS_DATABASE_TITLE);
        makePMSDatabasePopupMenu();
        mnuPMSDatabaseContents = new JMenu(PMS_DATABASE_CONTENTS_TITLE);
        makePMSDatabaseContentsPopupMenu();
        mniExportMigratedDataToPMSRequest = new JMenuItem(EXPORT_MIGRATED_DATA_TO_PMS_REQUEST_TITLE); 
        mnuMigrationManagement.add(mnuMigrationDatabase2);
        mnuMigrationManagement.add(mnuCSVSourceFiles);
        mnuMigrationManagement.add(mnuMigrationDatabaseContents);
        mnuMigrationManagement.add(mniImportMigratedDataRequest);
        mnuMigrationManagement.add(new JSeparator());
        mnuMigrationManagement.add(mnuPMSDatabase2);
        mnuMigrationManagement.add(mnuPMSDatabaseContents);
        mnuMigrationManagement.add(mniExportMigratedDataToPMSRequest);
        
        mniImportMigratedDataRequest.addActionListener((ActionEvent e) -> mniImportMigratedDataRequestActionPerformed());
        mniExportMigratedDataToPMSRequest.addActionListener((ActionEvent e) -> mniExportMigratedDataToPMSRequestActionPerformed());
    }
    
    private void makePMSDatabasePopupMenu(){
        mniPMSDatabaseCreationRequest = new JMenuItem(PMS_DATABASE_CREATION_REQUEST_TITLE);
        mniPMSDatabaseDeletionRequest = new JMenuItem(PMS_DATABASE_DELETION_REQUEST_TITLE);
        mniPMSDatabaseSelectionRequest = new JMenuItem(PMS_DATABASE_SELECTION_REQUEST_TITLE);
        mnuPMSDatabase2.add(mniPMSDatabaseCreationRequest);
        mnuPMSDatabase2.add(mniPMSDatabaseDeletionRequest);
        mnuPMSDatabase2.add(mniPMSDatabaseSelectionRequest);
        
        mniPMSDatabaseCreationRequest.addActionListener((ActionEvent e) -> mniPMSDatabaseCreationRequestActionPerformed());
        mniPMSDatabaseDeletionRequest.addActionListener((ActionEvent e) -> mniPMSDatabaseDeletionRequestActionPerformed());
        mniPMSDatabaseSelectionRequest.addActionListener((ActionEvent e) -> mniPMSDatabaseSelectionRequestActionPerformed());
    }
    
    private void makeMigrationDatabasePopupMenu(){
        mniMigrationDatabaseCreationRequest = new JMenuItem(MIGRATION_DATABASE_CREATION_REQUEST_TITLE);
        mniMigrationDatabaseDeletionRequest = new JMenuItem(MIGRATION_DATABASE_DELETION_REQUEST_TITLE);
        mniMigrationDatabaseSelectionRequest = new JMenuItem(MIGRATION_DATABASE_SELECTION_REQUEST_TITLE);
        mnuMigrationDatabase2.add(mniMigrationDatabaseCreationRequest);
        mnuMigrationDatabase2.add(mniMigrationDatabaseDeletionRequest);
        mnuMigrationDatabase2.add(mniMigrationDatabaseSelectionRequest);
        
        mniMigrationDatabaseCreationRequest.addActionListener((ActionEvent e) -> mniMigrationDatabaseCreationRequestActionPerformed());
        mniMigrationDatabaseDeletionRequest.addActionListener((ActionEvent e) -> mniMigrationDatabaseDeletionRequestActionPerformed());
        mniMigrationDatabaseSelectionRequest.addActionListener((ActionEvent e) -> mniMigrationDatabaseSelectionRequestActionPerformed());
    }
    
    private void makeCSVSourceFilesPopupMenu(){
        this.mniAppointmentCSVSelectionRequest = new JMenuItem(APPOINTMENT_CSV_SELECTION_REQUEST_TITLE);
        this.mniPatientCSVSelectionRequest = new JMenuItem(PATIENT_CSV_SELECTION_REQUEST_TITLE);
        mnuCSVSourceFiles.add(mniAppointmentCSVSelectionRequest);
        mnuCSVSourceFiles.add(mniPatientCSVSelectionRequest);
        
        mniAppointmentCSVSelectionRequest.addActionListener((ActionEvent e) -> mniAppointmentCSVSelectionRequestActionPerformed());
        mniPatientCSVSelectionRequest.addActionListener((ActionEvent e) -> mniPatientCSVSelectionRequestActionPerformed());
    }
    
    private void makeMigrationDatabaseContentsPopupMenu(){
        this.mniAppointmentTableRecordCount = new JMenuItem(APPOINTMENT_TABLE_RECORD_COUNT_TITLE);
        this.mniPatientTableRecordCount = new JMenuItem(PATIENT_TABLE_RECORD_COUNT_TITLE);
        this.mniSurgeryDaysTableRecordCount = new JMenuItem(SURGERY_DAYS_TABLE_RECORD_COUNT_TITLE);
        mnuMigrationDatabaseContents.add(mniAppointmentTableRecordCount);
        mnuMigrationDatabaseContents.add(mniPatientTableRecordCount);
        mnuMigrationDatabaseContents.add(mniSurgeryDaysTableRecordCount);
    }
    
    private void makePMSDatabaseContentsPopupMenu(){
        this.mniAppointmentRecordCount = new JMenuItem(APPOINTMENT_RECORD_COUNT_TITLE);
        this.mniPatientRecordCount = new JMenuItem(PATIENT_RECORD_COUNT_TITLE);
        this.mniSurgeryDaysRecordCount = new JMenuItem(SURGERY_DAYS_RECORD_COUNT_TITLE);
        mnuPMSDatabaseContents.add(mniAppointmentRecordCount);
        mnuPMSDatabaseContents.add(mniPatientRecordCount);
        mnuPMSDatabaseContents.add(mniSurgeryDaysRecordCount);
        
    }
    
    private final String PMS_DATABASE_REQUEST_TITLE = "PMS database"; 
    private final String PMS_DATABASE_CONTENTS_REQUEST_TITLE = "Database contents";

    private JMenuItem mniPatientView = null;
    private JMenuItem mniAppointmentView = null;
    private JMenuItem mniExitView = null;
    
    private ActionListener controller = null;
    
    
    private WindowAdapter windowAdapter = null;  
    private Image img = null;
    
    private JMenu mnuData = null;
    //private JMenuItem mniDatabaseLocator = null;
    private JMenuItem mniMigrationManagerView = null;
    private JMenuItem mniMigrationDatabase = null;
    private JMenuItem mniMigrationDatabaseSelect = null;
    private JMenuItem mniMigrationDatabaseCreate = null;
    private JMenuItem mniMigrationDatabaseDelete = null;
    private JMenuItem mniPMSDatabase = null;
    private JMenuItem mniPMSDatabaseSelect = null;
    private JMenuItem mniPMSDatabaseCreate = null;
    private JMenuItem mniPMSDatabaseDelete = null;
    private boolean closeIsEnabled = true;
    
    private JMenu mnuDataExtra = null;
    private JMenu mnuMigrationDatabase = null;
    private JMenu mnuPMSDatabase = null;
    private JMenu mnuSource = null;
    private JMenuItem mniMigrationTarget = null;
    private JMenuItem mniPMSTarget = null;
    private JMenuItem mniAppointmentCSVFile = null;
    private JMenuItem mniPatientCSVFile = null;
    
    public final String PATIENT_VIEW_HEADER = "Patient";
    public final String APPOINTMENT_VIEW_HEADER = "Appointment";
    /**
     * Listener for window closing events (user selecting the window "X" icon).
     * The listener initialised to DO_NOTHING_ON_CLOSE, in order to pass close request message onto the view controller 
     */
    private void initFrameClosure() {
        this.windowAdapter = new WindowAdapter() {
            // WINDOW_CLOSING event handler
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                /**
                 * viewMenuState variable is checked on receipt of windowClosing event
                 * -- true state indicates the main View menu is operational and closing event message sent to view controller
                 * -- false state indicates the main View menu is currently disabled and therefor no message sent to view controller
                 */
                if (DesktopView.this.closeIsEnabled){
                    /**
                     * When an attempt to close the view (user clicking "X")
                     * the view's controller is notified and will decide whether
                     * to call the view's dispose() method
                     */                   
                    ActionEvent actionEvent = new ActionEvent(DesktopView.this, 
                            ActionEvent.ACTION_PERFORMED,
                            DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
                    DesktopView.this.getController().actionPerformed(actionEvent);
                }
            }
        };

        // when you press "X" the WINDOW_CLOSING event is called but that is it
        // nothing else happens
        this.setDefaultCloseOperation(DesktopView.this.DO_NOTHING_ON_CLOSE);
        // don't forget this
        this.addWindowListener(this.windowAdapter);
    }
    /**
     * 
     * @param controller 
     */
    public DesktopView(ActionListener controller, Boolean isDataMigrationEnabled, EntityDescriptor ed) { 
        this.controller = controller;
        this.entityDescriptor = ed;
        initComponents();
        /**
         * initialise frame closure actions
         */
        initFrameClosure();
        /**
         * MENU initialisation
         
        mniPatientView = new JMenuItem("Patient");
        mniAppointmentView = new JMenuItem("Appointments");
        //mniDatabaseLocator = new JMenuItem("Database locator");
        mniExitView = new JMenuItem("Exit The Clinic practice management system");
        this.mnuView.add(mniPatientView);
        this.mnuView.add(mniAppointmentView);
        //this.mnuView.add(new JSeparator());
        //this.mnuView.add(mniDatabaseLocator);
        * */
        if (isDataMigrationEnabled){
           //addDataExtraMenu();
            makeMigrationManagementMenu();
            mnbDesktop.add(mnuMigrationManagement);   
        }
        else{
            makeSelectViewMenu();
            mnbDesktop.add(mnuSelectView);
        }
        /*
        this.mnuView.add(new JSeparator());
        this.mnuView.add(mniExitView);
        
        mniPatientView.addActionListener((ActionEvent e) -> mniPatientViewActionPerformed());
        mniAppointmentView.addActionListener((ActionEvent e) -> mniAppointmentViewActionPerformed());
        //mniDatabaseLocator.addActionListener((ActionEvent e) -> mniDatabaseLocatorActionPerformed());
        mniExitView.addActionListener((ActionEvent e) -> mniExitViewActionPerformed());
        */
        setContentPaneForInternalFrame();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        DesktopViewControllerPropertyChangeEvent propertyType = 
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.valueOf(e.getPropertyName());
        switch (propertyType){
            case DISABLE_DESKTOP_DATA_CONTROL:
                //this.disableDataControl();
                break;
            case DISABLE_DESKTOP_VIEW_CONTROL:
                this.disableViewControl();
                break;
            case ENABLE_DESKTOP_DATA_CONTROL:
                this.enableDataControl();
                break;
            case ENABLE_DESKTOP_VIEW_CONTROL:
                this.enableViewControl();
                break;
            case MIGRATION_ACTION_COMPLETE:
                setEntityDescriptor((EntityDescriptor)e.getNewValue());
                doMigrationActionCompletePropertyChange();
                break;     
        }
        /*
        if (propertyName.equals(DesktopViewController.DesktopViewControllerPropertyChangeEvent.DISABLE_DESKTOP_DATA_CONTROL.toString())){
            this.disableDataControl();
        }
        else if (propertyName.equals(DesktopViewController.DesktopViewControllerPropertyChangeEvent.DISABLE_DESKTOP_VIEW_CONTROL.toString())){
            this.disableViewControl();
        }
        else if (propertyName.equals(DesktopViewController.DesktopViewControllerPropertyChangeEvent.ENABLE_DESKTOP_DATA_CONTROL.toString())){
            this.enableDataControl();
        }
        else if (propertyName.equals(DesktopViewController.DesktopViewControllerPropertyChangeEvent.ENABLE_DESKTOP_VIEW_CONTROL.toString())){
            this.enableViewControl();
        }
        */
    }
    
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    
    private void doMigrationActionCompletePropertyChange(){
        
        this.mniAppointmentCSVSelectionRequest.setText(this.APPOINTMENT_CSV_SELECTION_REQUEST_TITLE 
                + getEntityDescriptor().getMigrationDescriptor().getAppointmentCSVFilePath());
        this.mniPatientCSVSelectionRequest.setText(this.PATIENT_CSV_SELECTION_REQUEST_TITLE 
                + getEntityDescriptor().getMigrationDescriptor().getPatientCSVFilePath());
        this.mniMigrationDatabaseSelectionRequest.setText(this.MIGRATION_DATABASE_SELECTION_REQUEST_TITLE
                + getEntityDescriptor().getMigrationDescriptor().getMigrationDatabaseSelection());
        this.mniPMSDatabaseSelectionRequest.setText(this.PMS_DATABASE_SELECTION_REQUEST_TITLE
                + getEntityDescriptor().getMigrationDescriptor().getPMSDatabaseSelection());
        
        Integer count = getEntityDescriptor().getMigrationDescriptor().getAppointmentTableCount();
        if (count!=null)
            this.mniAppointmentTableRecordCount.setText(this.APPOINTMENT_TABLE_RECORD_COUNT_TITLE
                + "(records = " + String.valueOf(count) + ")");
        else
            this.mniAppointmentTableRecordCount.setText(this.APPOINTMENT_TABLE_RECORD_COUNT_TITLE
                + "(missing table)");
        
        count = getEntityDescriptor().getMigrationDescriptor().getPatientTableCount();
        if (count!=null)
            this.mniPatientTableRecordCount.setText(this.PATIENT_TABLE_RECORD_COUNT_TITLE
                + "(records = " + String.valueOf(count) + ")");
        else
            this.mniPatientTableRecordCount.setText(this.PATIENT_TABLE_RECORD_COUNT_TITLE
                + "(missing table)");
        
        count = getEntityDescriptor().getMigrationDescriptor().getSurgeryDaysAssignmentTableCount();
        if (count!=null)
            this.mniSurgeryDaysTableRecordCount.setText(this.SURGERY_DAYS_TABLE_RECORD_COUNT_TITLE
                + "(records = " + String.valueOf(count) + ")");
        else
            this.mniSurgeryDaysTableRecordCount.setText(this.SURGERY_DAYS_TABLE_RECORD_COUNT_TITLE
                + "(missing table)");
        
        count = getEntityDescriptor().getMigrationDescriptor().getAppointmentsCount();
        if (count!=null)
            this.mniAppointmentRecordCount.setText(this.APPOINTMENT_RECORD_COUNT_TITLE
                + "(records = " + String.valueOf(count) + ")");
        else
            this.mniAppointmentRecordCount.setText(this.APPOINTMENT_RECORD_COUNT_TITLE
                + "(missing table)");
        
        count = getEntityDescriptor().getMigrationDescriptor().getPatientsCount();
        if (count!=null)
            this.mniPatientRecordCount.setText(this.PATIENT_RECORD_COUNT_TITLE
                + "(records = " + String.valueOf(count) + ")");
        else
            this.mniPatientRecordCount.setText(this.PATIENT_RECORD_COUNT_TITLE
                + "(missing table)");
        
        count = getEntityDescriptor().getMigrationDescriptor().getSurgeryDaysAssignmentCount();
        if (count!=null)
            this.mniSurgeryDaysRecordCount.setText(this.SURGERY_DAYS_RECORD_COUNT_TITLE
                + "(records = " + String.valueOf(count) + ")");
        else
            this.mniSurgeryDaysRecordCount.setText(this.SURGERY_DAYS_RECORD_COUNT_TITLE
                + "(missing table)");
        
    }
    
    private void addMigrationManagementMenu(){
        
    }
    
    private void addDataExtraMenu(){
        
        if (mnuData==null) mnuData = new JMenu("Data management");
        
        mnuMigrationDatabase = new JMenu("Migration database");
        mniMigrationDatabaseSelect = new JMenuItem("Select migration database foruse");
        mniMigrationDatabaseCreate = new JMenuItem("Create new migration database");
        mniMigrationDatabaseDelete = new JMenuItem("Delete existing migration database");
        mnuMigrationDatabase.add(this.mniMigrationDatabaseSelect);
        mnuMigrationDatabase.add(mniMigrationDatabaseCreate);
        mnuMigrationDatabase.add(mniMigrationDatabaseDelete);
        
        mnuPMSDatabase = new JMenu("PMS database");
        mniPMSDatabaseSelect = new JMenuItem("Select PMS database for use");
        mniPMSDatabaseCreate = new JMenuItem("Create new PMS database");
        mniPMSDatabaseDelete = new JMenuItem("Delete existing PMS database");
        mnuPMSDatabase.add(mniPMSDatabaseSelect);
        mnuPMSDatabase.add(mniPMSDatabaseCreate);
        mnuPMSDatabase.add(this.mniPMSDatabaseDelete);
        
        mnuSource = new JMenu("Source CSV files");
        mniAppointmentCSVFile = new JMenuItem("Appointment CSV file");
        mniPatientCSVFile = new JMenuItem("Patient CSV file");
        mnuSource.add(mniAppointmentCSVFile);
        mnuSource.add(mniPatientCSVFile);
        
        mniMigrationManagerView = new JMenuItem("Run migration manager");
        
        mnuData.add(mnuMigrationDatabase);
        mnuData.add(mnuPMSDatabase);
        mnuData.add(mnuSource);
        this.mnuData.add(new JSeparator());
        this.mnuData.add(mniMigrationManagerView);
        
        this.mnbDesktop.add(mnuData);
        
        mniMigrationManagerView.addActionListener((ActionEvent e) -> mniMigrationManagerViewActionPerformed());
        mniMigrationDatabaseSelect.addActionListener((ActionEvent e) -> mniMigrationDatabaseSelectActionPerformed());
        mniMigrationDatabaseCreate.addActionListener((ActionEvent e) -> mniMigrationDatabaseCreateActionPerformed());
        mniMigrationDatabaseDelete.addActionListener((ActionEvent e) -> mniMigrationDatabaseDeleteActionPerformed());
        mniPMSDatabaseSelect.addActionListener((ActionEvent e) -> mniPMSDatabaseSelectActionPerformed());
        mniPMSDatabaseCreate.addActionListener((ActionEvent e) -> mniPMSDatabaseCreateActionPerformed());
        mniPMSDatabaseDelete.addActionListener((ActionEvent e) -> mniPMSDatabaseDeleteActionPerformed());
        //mniPMSDatabase.addActionListener((ActionEvent e) -> mniPMSDatabaseActionPerformed());
        mniAppointmentCSVFile.addActionListener((ActionEvent e) -> mniAppointmentCSVFileActionPerformed());
        mniPatientCSVFile.addActionListener((ActionEvent e) -> mniPatientCSVFileActionPerformed());
        
    }
    
    public void enableWindowCloseControl(){
        this.closeIsEnabled = true;
    }
    
    public void disableWindowClosedControl(){
        this.closeIsEnabled = true;
    }
    /**
     * enable the main View menu  
     */
    public void enableViewControl(){
        this.mnuSelectView.setEnabled(true);
    }
    
    /**
     * disable the main View menu 
     */
    public void disableViewControl(){
        this.mnuSelectView.setEnabled(false);   
    }
    /**
     * enable the main Data menu
     */
    public void enableDataControl(){
        this.mnuMigrationManagement.setEnabled(true);
    }
    
    /**
     * disable the main Data menu 
     */
    public void disableDataControl(){
        //this.mnuMigrationManagement.setEnabled(false);   
    }
    
    public javax.swing.JDesktopPane getDeskTop(){
        return deskTop;
    } 
    private void setContentPaneForInternalFrame(){
        setContentPane(deskTop);
    }
    
    public ActionListener getController(){
        return controller;
    }
    public void setController(DesktopViewController value){
        controller = value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/clinic_desktop.jpg"));
        Image img = icon.getImage();
        deskTop = new javax.swing.JDesktopPane(){
            //@Override
            public void paintComponent(Graphics g){
                //super.paintComponent(grphcs);
                g.drawImage(img, 0,0,getWidth(), getHeight(),this);
            }

        };
        mnbDesktop = new javax.swing.JMenuBar();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        deskTop.setBackground(new java.awt.Color(51, 0, 102));

        javax.swing.GroupLayout deskTopLayout = new javax.swing.GroupLayout(deskTop);
        deskTop.setLayout(deskTopLayout);
        deskTopLayout.setHorizontalGroup(
            deskTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 562, Short.MAX_VALUE)
        );
        deskTopLayout.setVerticalGroup(
            deskTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 378, Short.MAX_VALUE)
        );

        setJMenuBar(mnbDesktop);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(deskTop)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(deskTop)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane deskTop;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuBar mnbDesktop;
    // End of variables declaration//GEN-END:variables
 
    private void mniAppointmentViewRequestActionPerformed() {                                        
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString());
        String s;
        s = actionEvent.getSource().getClass().getSimpleName();
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniAppointmentViewActionPerformed() {                                        
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString());
        String s;
        s = actionEvent.getSource().getClass().getSimpleName();
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniAppointmentCSVFileActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_CSV_APPOINTMENT_FILE_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniAppointmentCSVSelectionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_CSV_APPOINTMENT_FILE_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPatientCSVFileActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_CSV_PATIENT_FILE_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPatientCSVSelectionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_CSV_PATIENT_FILE_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPatientViewRequestActionPerformed() {                                                      
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PATIENT_VIEW_CONTROLLER_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPatientNotificationViewRequestActionPerformed() {                                                      
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PATIENT_NOTIFICATION_VIEW_CONTROLLER_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPatientViewActionPerformed() {                                                      
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PATIENT_VIEW_CONTROLLER_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationManagerViewActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_VIEW_CONTROLLER_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationDatabaseSelectActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_SELECTION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationDatabaseSelectionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_SELECTION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationDatabaseCreateActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_CREATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationDatabaseCreationRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_CREATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationDatabaseDeleteActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_DELETION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniMigrationDatabaseDeletionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_DELETION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseSelectActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PMS_DATABASE_SELECTION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseSelectionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PMS_DATABASE_SELECTION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseCreateActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PMS_DATABASE_CREATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseCreationRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PMS_DATABASE_CREATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseDeleteActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PMS_DATABASE_DELETION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseDeletionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.PMS_DATABASE_DELETION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniDatabaseLocatorActionPerformed() {                                                      
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.DATABASE_LOCATOR_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }

    private void mniExitRequestViewActionPerformed() {  
        /**
         * Menu request to close view is routed to the view controller
         */
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
        DesktopView.this.getController().actionPerformed(actionEvent);
    }
    
    private void mniExitViewActionPerformed() {  
        /**
         * Menu request to close view is routed to the view controller
         */
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
        DesktopView.this.getController().actionPerformed(actionEvent);
    }
    
    private void mniImportMigratedDataRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.IMPORT_DATA_FROM_SOURCE.toString());
        DesktopView.this.getController().actionPerformed(actionEvent);
    }
    
    private void mniExportMigratedDataToPMSRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.EXPORT_MIGRATED_DATA.toString());
        DesktopView.this.getController().actionPerformed(actionEvent);
    }

}
