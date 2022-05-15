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
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
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
    
    private final String MIGRATION_MANAGEMENT_MENU_TITLE = "Migration management";
        private final String MIGRATION_DATABASE_TITLE = "Database actions";
            private final String COPY_SELECTED_DATABASE_REQUEST_TITLE = "Copy selected database";
            private final String SELECT_DATABASE_REQUEST_TITLE = "Select database to use";
            private final String CREATE_DATABASE_REQUEST_TITLE = "Create a new database";
            private final String DELETE_DATABASE_REQUEST_TITLE = "Delete database";
        private final String CSV_SOURCE_FILES_TITLE = "CSV files";
            private final String APPOINTMENT_CSV_SELECTION_REQUEST_TITLE = "Select appointment CSV file to use";
            private final String PATIENT_CSV_SELECTION_REQUEST_TITLE = "Select patient CSV file to use";
        private final String DATABASE_CONTENTS_TITLE = "Database contents";
            private final String APPOINTMENT_TABLE_RECORD_COUNT_TITLE = "Appointment table ";
            private final String PATIENT_TABLE_RECORD_COUNT_TITLE = "Patient table ";
            private final String SURGERY_DAYS_TABLE_RECORD_COUNT_TITLE = "SurgeryDaysAssignment table ";
        private final String IMPORT_DATA_REQUEST_TITLE = "Import data from CSV files";    
        
    private JMenu mnuSelectView = null; 
        private JMenuItem mniAppointmentViewRequest = null;
        private JMenuItem mniPatientViewRequest = null;
        private JMenuItem mniPatientNotificationViewRequest = null;
        private JMenuItem mniExitViewRequest = null;
        
    private JMenu mnuMigrationManagement = null; 
        private JMenu mnuDatabaseActions = null;
            private JMenuItem mniSelectDatabaseRequest = null;
            private JMenuItem mniCreateDatabaseRequest = null;
            private JMenuItem mniDeleteDatabaseRequest = null; 
            private JMenuItem mniCopySelectedDatabaseRequest = null; 
        private JMenu mnuCSVSourceFiles = null;
            private JMenuItem mniAppointmentCSVSelectionRequest = null;
            private JMenuItem mniPatientCSVSelectionRequest = null;
        private JMenu mnuDatabaseContents = null; 
            private JMenuItem mniAppointmentTableRecordCount = null;
            private JMenuItem mniPatientTableRecordCount = null;
            private JMenuItem mniSurgeryDaysTableRecordCount = null;
        private JMenuItem mniImportMigratedDataRequest = null;

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
        mnuDatabaseActions = new JMenu(MIGRATION_DATABASE_TITLE);
        makeMigrationDatabasePopupMenu();
        mnuCSVSourceFiles = new JMenu(CSV_SOURCE_FILES_TITLE);
        makeCSVSourceFilesPopupMenu();
        mnuDatabaseContents = new JMenu(DATABASE_CONTENTS_TITLE);
        makeMigrationDatabaseContentsPopupMenu();
        mniImportMigratedDataRequest = new JMenuItem(IMPORT_DATA_REQUEST_TITLE); 
        mniExitViewRequest = new JMenuItem(EXIT_VIEW_REQUEST_TITLE);
        mnuMigrationManagement.add(mnuDatabaseActions);
        mnuMigrationManagement.add(mnuCSVSourceFiles);
        mnuMigrationManagement.add(mnuDatabaseContents);
        mnuMigrationManagement.add(mniImportMigratedDataRequest);
        mnuMigrationManagement.add(new JSeparator());
        mnuMigrationManagement.add(mniExitViewRequest);
        
        mniImportMigratedDataRequest.addActionListener(
                (ActionEvent e) -> mniImportMigratedDataRequestActionPerformed());
        mniExitViewRequest.addActionListener(
                (ActionEvent e) -> mniExitRequestViewActionPerformed());
        
    }
    
    private void makeMigrationDatabasePopupMenu(){
        mniCopySelectedDatabaseRequest = new JMenuItem(COPY_SELECTED_DATABASE_REQUEST_TITLE);
        mniCreateDatabaseRequest = new JMenuItem(CREATE_DATABASE_REQUEST_TITLE);
        mniDeleteDatabaseRequest = new JMenuItem(DELETE_DATABASE_REQUEST_TITLE);
        mniSelectDatabaseRequest = new JMenuItem(SELECT_DATABASE_REQUEST_TITLE);
        mnuDatabaseActions.add(mniCopySelectedDatabaseRequest);
        mnuDatabaseActions.add(mniCreateDatabaseRequest);
        mnuDatabaseActions.add(mniDeleteDatabaseRequest);
        mnuDatabaseActions.add(mniSelectDatabaseRequest);
        
        mniCopySelectedDatabaseRequest.addActionListener((ActionEvent e) -> mniCopySelectedDatabaseRequestActionPerformed());
        mniCreateDatabaseRequest.addActionListener((ActionEvent e) -> mniCreateDatabaseRequestActionPerformed());
        mniDeleteDatabaseRequest.addActionListener((ActionEvent e) -> mniDeleteDatabaseRequestActionPerformed());
        mniSelectDatabaseRequest.addActionListener((ActionEvent e) -> mniSelectDatabaseRequestActionPerformed());
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
        mnuDatabaseContents.add(mniAppointmentTableRecordCount);
        mnuDatabaseContents.add(mniPatientTableRecordCount);
        mnuDatabaseContents.add(mniSurgeryDaysTableRecordCount);
    }
  
    private ActionListener controller = null;
    private WindowAdapter windowAdapter = null;  
    private final boolean closeIsEnabled = true;

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
        this.setDefaultCloseOperation(DesktopView.DO_NOTHING_ON_CLOSE);
        // don't forget this
        this.addWindowListener(this.windowAdapter);
    }
    
    /**
     * 
     * @param controller
     * @param isDataMigrationEnabled
     * @param ed 
     */
    public DesktopView(ActionListener controller, Boolean isDataMigrationEnabled, EntityDescriptor ed) { 
        this.controller = controller;
        this.entityDescriptor = ed;
        initComponents();
        /**
         * initialise frame closure actions
         */
        initFrameClosure();
        if (isDataMigrationEnabled){
            makeMigrationManagementMenu();
            mnbDesktop.add(mnuMigrationManagement);   
        }
        else{
            makeSelectViewMenu();
            mnbDesktop.add(mnuSelectView);
        }
        setContentPaneForInternalFrame();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        DesktopViewControllerPropertyChangeEvent propertyType = 
                DesktopViewController.DesktopViewControllerPropertyChangeEvent.valueOf(e.getPropertyName());
        switch (propertyType){
            /*
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
            */
            case MIGRATION_ACTION_COMPLETE:
                setEntityDescriptor((EntityDescriptor)e.getNewValue());
                doMigrationActionCompletePropertyChange();
                break;     
        }
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
        this.mniSelectDatabaseRequest.setText(this.SELECT_DATABASE_REQUEST_TITLE
                + getEntityDescriptor().getMigrationDescriptor().getMigrationDatabaseSelection());
        
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
        
        /*
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
        */
        
    }

    /*
    public void enableWindowCloseControl(){
        this.closeIsEnabled = true;
    }
    
    public void disableWindowClosedControl(){
        this.closeIsEnabled = true;
    }

    public void enableViewControl(){
        this.mnuSelectView.setEnabled(true);
    }

    public void disableViewControl(){
        this.mnuSelectView.setEnabled(false);   
    }

    public void enableDataControl(){
        this.mnuMigrationManagement.setEnabled(true);
    }

    public void disableDataControl(){
        //this.mnuMigrationManagement.setEnabled(false);   
    }
    */
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
  
    private void mniAppointmentCSVSelectionRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_CSV_APPOINTMENT_FILE_REQUEST.toString());
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
    
    private void mniCopySelectedDatabaseRequestActionPerformed(){
        
    }
    
    private void mniSelectDatabaseRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_SELECTION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniCreateDatabaseRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_CREATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
   
    private void mniDeleteDatabaseRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MIGRATION_DATABASE_DELETION_REQUEST.toString());
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

    private void mniImportMigratedDataRequestActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.IMPORT_DATA_FROM_SOURCE.toString());
        DesktopView.this.getController().actionPerformed(actionEvent);
    }
    
}
