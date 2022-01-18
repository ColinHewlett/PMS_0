/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

//import clinicpms.controller.ViewController.DesktopViewControllerActionEvent;
import clinicpms.controller.DesktopViewController;
import clinicpms.controller.ViewController;
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
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 *
 * @author colin
 */
public class DesktopView extends javax.swing.JFrame implements PropertyChangeListener{
    
    private ActionListener controller = null;
    private JMenuItem mniPatientView = null;
    private JMenuItem mniAppointmentView = null;
    
    private JMenuItem mniSurgeryDaysSelector = null;
    private JMenuItem mniExitView = null;
    
    private WindowAdapter windowAdapter = null;  
    private Image img = null;
    
    private JMenu mnuData = null;
    private JMenuItem mniDatabaseLocator = null;
    private JMenuItem mniMigrationManagerView = null;
    private JMenuItem mniMigrationDatabase = null;
    private JMenuItem mniPMSDatabase = null;
    private boolean closeIsEnabled = true;
    
    
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
    public DesktopView(ActionListener controller, Boolean isDataMigrationEnabled) { 
        this.controller = controller;       
        initComponents();
        /**
         * initialise frame closure actions
         */
        initFrameClosure();
        /**
         * MENU initialisation
         */
        mniPatientView = new JMenuItem("Patient");
        mniAppointmentView = new JMenuItem("Appointments");
        mniDatabaseLocator = new JMenuItem("Database locator");
        mniExitView = new JMenuItem("Exit The Clinic practice management system");
        this.mnuView.add(mniPatientView);
        this.mnuView.add(mniAppointmentView);
        //this.mnuView.add(new JSeparator());
        //this.mnuView.add(mniDatabaseLocator);
        if (isDataMigrationEnabled)addMigrationManagerMenu();
        this.mnuView.add(new JSeparator());
        this.mnuView.add(mniExitView);
        
        mniPatientView.addActionListener((ActionEvent e) -> mniPatientViewActionPerformed());
        mniAppointmentView.addActionListener((ActionEvent e) -> mniAppointmentViewActionPerformed());
        mniDatabaseLocator.addActionListener((ActionEvent e) -> mniDatabaseLocatorActionPerformed());
        mniExitView.addActionListener((ActionEvent e) -> mniExitViewActionPerformed());
        setContentPaneForInternalFrame();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
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
    }
    
    private void addMigrationManagerMenu(){
        mniMigrationManagerView = new JMenuItem("Run migration manager");
        mniMigrationDatabase = new JMenuItem("Select migration database");
        this.mniPMSDatabase = new JMenuItem("Select PMS database");
        //this.mnuView.add(new JSeparator());
        if (mnuData == null) mnuData = new JMenu("Data");
        this.mnuData.add(mniMigrationManagerView);
        this.mnuData.add(new JSeparator());
        this.mnuData.add(mniMigrationDatabase);
        this.mnuData.add(mniPMSDatabase);
        this.mnbDesktop.add(mnuData);
        mniMigrationManagerView.addActionListener((ActionEvent e) -> mniMigrationManagerViewActionPerformed());
        mniMigrationDatabase.addActionListener((ActionEvent e) -> mniMigrationDatabaseActionPerformed());
        mniPMSDatabase.addActionListener((ActionEvent e) -> mniPMSDatabaseActionPerformed());
    }
    /*
    @Override
    public javax.swing.JDesktopPane getContentPane(){
        return deskTop;
    }
    */
    
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
        this.mnuView.setEnabled(true);
    }
    
    /**
     * disable the main View menu 
     */
    public void disableViewControl(){
        this.mnuView.setEnabled(false);   
    }
    /**
     * enable the main Data menu
     */
    public void enableDataControl(){
        this.mnuData.setEnabled(true);
    }
    
    /**
     * disable the main Data menu 
     */
    public void disableDataControl(){
        this.mnuData.setEnabled(false);   
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
        mnuView = new javax.swing.JMenu();

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
            .addGap(0, 359, Short.MAX_VALUE)
        );

        mnuView.setText("View");
        mnbDesktop.add(mnuView);

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
    private javax.swing.JMenu mnuView;
    // End of variables declaration//GEN-END:variables

    
    private void mniAppointmentViewActionPerformed() {                                        
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString());
        String s;
        s = actionEvent.getSource().getClass().getSimpleName();
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
    
    private void mniMigrationDatabaseActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_MIGRATION_DATABASE_LOCATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniPMSDatabaseActionPerformed(){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.SET_PMS_DATABASE_LOCATION_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
    }
    
    private void mniDatabaseLocatorActionPerformed() {                                                      
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.DATABASE_LOCATOR_REQUEST.toString());
        this.getController().actionPerformed(actionEvent);
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

}
