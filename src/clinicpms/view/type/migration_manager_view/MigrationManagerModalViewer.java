/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.type.migration_manager_view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.store.Store;
import clinicpms.view.View;
import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JSeparator;

/**
 *
 * @author colin
 */
public class MigrationManagerModalViewer extends View {
    private View.Viewer myViewType = null;
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;

    private InternalFrameAdapter internalFrameAdapter = null;
    private Store.Storage database = null;
    private ArrayList<String> stats = null;
    
    public MigrationManagerModalViewer(View.Viewer myViewType,ActionListener myController,
            EntityDescriptor entityDescriptor, 
            Component parent) {//ViewMode arg
        //initialiseDialogClosing();
        setMyViewType(myViewType);
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        initComponents();
        
        // Try to find a JDesktopPane.
        JLayeredPane toUse = JOptionPane.getDesktopPaneForComponent(parent);
        // If we don't have a JDesktopPane, we try to find a JLayeredPane.
        if (toUse == null)  toUse = JLayeredPane.getLayeredPaneAbove(parent);
        // If this still fails, we throw a RuntimeException.
        if (toUse == null) throw new RuntimeException   ("parentComponent does not have a valid parent");
        
        JDesktopPane x = (JDesktopPane)toUse;
        toUse.add(this);
        this.setLayer(JLayeredPane.MODAL_LAYER);
        centreViewOnDesktop(x.getParent(),this);
        this.initialiseView();
        this.setVisible(true);
        
        /*
        ActionEvent actionEvent = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            ViewController.DesktopViewControllerActionEvent.DISABLE_DESKTOP_CONTROLS_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
        */
        startModal(this);
 
    }
    
    private void startModal(JInternalFrame f) {
        // We need to add an additional glasspane-like component directly
        // below the frame, which intercepts all mouse events that are not
        // directed at the frame itself.
        JPanel modalInterceptor = new JPanel();
        modalInterceptor.setOpaque(false);
        JLayeredPane lp = JLayeredPane.getLayeredPaneAbove(f);
        lp.setLayer(modalInterceptor, JLayeredPane.MODAL_LAYER.intValue());
        modalInterceptor.setBounds(0, 0, lp.getWidth(), lp.getHeight());
        modalInterceptor.addMouseListener(new MouseAdapter(){});
        modalInterceptor.addMouseMotionListener(new MouseMotionAdapter(){});
        lp.add(modalInterceptor);
        f.toFront();

        // We need to explicitly dispatch events when we are blocking the event
        // dispatch thread.
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        try {
            while (! f.isClosed())       {
                if (EventQueue.isDispatchThread())    {
                    // The getNextEventMethod() issues wait() when no
                    // event is available, so we don't need do explicitly wait().
                    AWTEvent ev = queue.getNextEvent();
                    // This mimics EventQueue.dispatchEvent(). We can't use
                    // EventQueue.dispatchEvent() directly, because it is
                    // protected, unfortunately.
                    if (ev instanceof ActiveEvent)  ((ActiveEvent) ev).dispatch();
                    else if (ev.getSource() instanceof Component)  ((Component) ev.getSource()).dispatchEvent(ev);
                    else if (ev.getSource() instanceof MenuComponent)  ((MenuComponent) ev.getSource()).dispatchEvent(ev);
                    // Other events are ignored as per spec in
                    // EventQueue.dispatchEvent
                } else  {
                    // Give other threads a chance to become active.
                    Thread.yield();
                }
            }
        }
        catch (InterruptedException ex) {
            // If we get interrupted, then leave the modal state.
        }
        finally {
            // Clean up the modal interceptor.
            lp.remove(modalInterceptor);

            // Remove the internal frame from its parent, so it is no longer
            // lurking around and clogging memory.
            Container parent = f.getParent();
            if (parent != null) parent.remove(f);
        }
    }
    
    private void centreViewOnDesktop(Container desktopView, JInternalFrame view){
        Insets insets = desktopView.getInsets();
        Dimension deskTopViewDimension = desktopView.getSize();
        Dimension myViewDimension = view.getSize();
        view.setLocation(new Point(
                (int)(deskTopViewDimension.getWidth() - (myViewDimension.getWidth()))/2,
                (int)((deskTopViewDimension.getHeight()-insets.top) - myViewDimension.getHeight())/2));
    }
    
    @Override 
    public EntityDescriptor getEntityDescriptor(){
        return entityDescriptor;
        
    }
    
    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
    
    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }
    
    private ActionListener getMyController(){
         return this.myController;
     }
    private void setMyController(ActionListener value){
        this.myController = value;
    }
    
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    private Store.Storage getDatabase(){
        return database;
    }
    private void setDatabase(Store.Storage value){
        database = value;
    }
    
    public void initialiseView(){
        addInternalFrameClosingListener();
        this.txtMigrationDatabasePath.setText(getEntityDescriptor().getMigrationDescriptor().getTarget().getData());
        mniSelectTargetDatabase.setState(true);
        setDatabase(Store.getStorageType());
        if (getDatabase().equals(Store.Storage.UNDEFINED_DATABASE))
            super.setTitle("Undefined database appointments migration");
        else{
            switch (getDatabase()){
                case ACCESS:
                    super.setTitle("Access database appointments migration"); 
                    break;
                case POSTGRES:
                    super.setTitle("PostgreSQL database appointments migration");
                    break;
                case SQL_EXPRESS:
                    super.setTitle("SQL Express database appointments migration");
                    break;
            }
        }
        //super.repaint();
    }
    
    @Override
    public void addInternalFrameClosingListener(){
        /**
         * Establish an InternalFrameListener for when the view is closed 
         */
        
        internalFrameAdapter = new InternalFrameAdapter(){
            @Override  
            public void internalFrameClosing(InternalFrameEvent e) {
                ActionEvent actionEvent = new ActionEvent(
                        MigrationManagerModalViewer.this,ActionEvent.ACTION_PERFORMED,
                        ViewController.AppointmentViewControllerActionEvent.APPOINTMENTS_VIEW_CLOSED.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        this.addInternalFrameListener(internalFrameAdapter);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        
        if (propertyName.equals(ViewController.MigrationViewPropertyChangeEvents.MIGRATION_ACTION_COMPLETE.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            initialiseStatsDisplay();  
        }
    }
    
    public void initialiseStatsDisplay(){
        String report = "";
        if (this.txaResults.getText().length()>0) report = "\n";
        switch(getEntityDescriptor().getMigrationDescriptor().getMigrationViewRequest()){
            case MIGRATE_APPOINTMENTS_TO_DATABASE:
                report = "MIGRATE APPOINTMENTS TO DATABASE \n"
                + "Total number of appointments = " + getEntityDescriptor().getMigrationDescriptor().getAppointmentsCount() + "\n"
                + "Operation duration = " + normaliseDuration(getEntityDescriptor().getMigrationDescriptor().getMigrationActionDuration()) + "\n";
                break;
            case MIGRATE_PATIENTS_TO_DATABASE:
                report = "MIGRATE PATIENTS TO DATABASE \n"
                + "Total number of patients = " + getEntityDescriptor().getMigrationDescriptor().getPatientsCount() + "\n"
                + "Operation duration = " + normaliseDuration(getEntityDescriptor().getMigrationDescriptor().getMigrationActionDuration()) + "\n";
                break;
            case REMOVE_BAD_APPOINTMENTS_FROM_DATABASE:
                report = "REMOVAL OF BAD APPOINTMENTS FROM DATABASE \n"
                + "Total number of remaining appointments = " + getEntityDescriptor().getMigrationDescriptor().getAppointmentsCount() + "\n"
                + "Operation duration = " + normaliseDuration(getEntityDescriptor().getMigrationDescriptor().getMigrationActionDuration()) + "\n";
                break;
            case TIDY_PATIENT_DATA_IN_DATABASE:
                report = "TIDY UP OF PATIENT DATA \n"
                + "Operation duration = " + normaliseDuration(getEntityDescriptor().getMigrationDescriptor().getMigrationActionDuration()) + "\n";
                break;
        };
        report = this.txaResults.getText() + report;
        this.txaResults.setText(report);
    }
    
    private String normaliseDuration(Duration duration){
        DecimalFormat df = new DecimalFormat("#.###");
        String result = null;
        double seconds = (double)duration.toMillis()/1000;
        double minutes;
        if (seconds > 60){
            minutes = (double)seconds/60;
            //result = String.format("%0.3d minutes", minutes);  
            result = df.format(minutes) + " minutes";
        }
        //else result = String.format("%0.3d seconds", seconds); 
        else result = df.format(seconds) + " seconds";
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem2 = new javax.swing.JMenuItem();
        fchFileChooser = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        txtSelectedAppointmentSourceFile = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtSelectedPatientSourceFile = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaResults = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        txtMigrationDatabasePath = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuAction = new javax.swing.JMenu();
        mniSelectAppointmentCSVFile = new javax.swing.JCheckBoxMenuItem();
        mniSelectPatientCSVFile = new javax.swing.JCheckBoxMenuItem();
        mniSelectTargetDatabase = new javax.swing.JCheckBoxMenuItem();
        mniMigrateAppointmentsToDatabase = new javax.swing.JMenuItem();
        mniMigratePatientsToDatabase = new javax.swing.JMenuItem();
        mniRemoveBadAppointmentsFromDatabase = new javax.swing.JMenuItem();
        mniTidyPatientRecordsOnDatabase = new javax.swing.JMenuItem();
        mniExitDataMigrator = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        jMenuItem2.setText("jMenuItem2");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Selected files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        txtSelectedAppointmentSourceFile.setEditable(false);

        jLabel1.setText("Appointment source file");

        txtSelectedPatientSourceFile.setEditable(false);

        jLabel3.setText("Patient source file");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSelectedAppointmentSourceFile, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                    .addComponent(txtSelectedPatientSourceFile)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(txtSelectedAppointmentSourceFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(6, 6, 6)
                .addComponent(txtSelectedPatientSourceFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Results of data migration actions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        txaResults.setColumns(20);
        txaResults.setRows(5);
        jScrollPane1.setViewportView(txaResults);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Selected target migration database", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtMigrationDatabasePath)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtMigrationDatabasePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mnuAction.setText("Action");

        mniSelectAppointmentCSVFile.setSelected(true);
        mniSelectAppointmentCSVFile.setText("Select appointment CSV file");
        mniSelectAppointmentCSVFile.setState(false);
        mniSelectAppointmentCSVFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSelectAppointmentCSVFileActionPerformed(evt);
            }
        });
        mnuAction.add(mniSelectAppointmentCSVFile);

        mniSelectPatientCSVFile.setSelected(false);
        mniSelectPatientCSVFile.setText("Select patient CSV file");
        mniSelectPatientCSVFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSelectPatientCSVFileActionPerformed(evt);
            }
        });
        mnuAction.add(mniSelectPatientCSVFile);

        mniSelectTargetDatabase.setSelected(true);
        mniSelectTargetDatabase.setText("Select target database");
        mniSelectTargetDatabase.setState(false);
        mniSelectTargetDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSelectTargetDatabaseActionPerformed(evt);
            }
        });
        mnuAction.add(mniSelectTargetDatabase);

        mnuAction.add(new JSeparator());
        mniMigrateAppointmentsToDatabase.setText("Migrate appointments to database");
        mniMigrateAppointmentsToDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniMigrateAppointmentsToDatabaseActionPerformed(evt);
            }
        });
        mnuAction.add(mniMigrateAppointmentsToDatabase);

        mniMigratePatientsToDatabase.setText("Migrate patients to database");
        mniMigratePatientsToDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniMigratePatientsToDatabaseActionPerformed(evt);
            }
        });
        mnuAction.add(mniMigratePatientsToDatabase);

        mniRemoveBadAppointmentsFromDatabase.setText("Remove bad appointment records from database");
        mniRemoveBadAppointmentsFromDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniRemoveBadAppointmentsFromDatabaseActionPerformed(evt);
            }
        });
        mnuAction.add(mniRemoveBadAppointmentsFromDatabase);

        mniTidyPatientRecordsOnDatabase.setText("Tidy patient data in database");
        mniTidyPatientRecordsOnDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniTidyPatientRecordsOnDatabaseActionPerformed(evt);
            }
        });
        mnuAction.add(mniTidyPatientRecordsOnDatabase);
        mnuAction.add(new JSeparator());

        mniExitDataMigrator.setText("Exit data migrator");
        mniExitDataMigrator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExitDataMigratorActionPerformed(evt);
            }
        });
        mnuAction.add(mniExitDataMigrator);

        jMenuBar1.add(mnuAction);

        jMenu2.setText("Edit");

        jMenuItem1.setText("jMenuItem1");
        jMenu2.add(jMenuItem1);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fchFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fchFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mniSelectAppointmentCSVFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSelectAppointmentCSVFileActionPerformed
        int result = this.fchFileChooser.showOpenDialog(new JFrame());
        if (result == fchFileChooser.APPROVE_OPTION) {
            File selectedFile = fchFileChooser.getSelectedFile();
            this.txtSelectedAppointmentSourceFile.setText(selectedFile.getPath());
            mniSelectAppointmentCSVFile.setState(true);
        }
        else mniSelectAppointmentCSVFile.setState(false);
    }//GEN-LAST:event_mniSelectAppointmentCSVFileActionPerformed

    private void mniSelectTargetDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSelectTargetDatabaseActionPerformed
        if (getDatabase().equals(Store.Storage.UNDEFINED_DATABASE)){
            JOptionPane.showMessageDialog(null, 
                "Database has not been defined; file selection aborted", 
                "Database error", 
                JOptionPane.WARNING_MESSAGE);
        }
        else{
            int result = this.fchFileChooser.showOpenDialog(new JFrame());
            if (result == fchFileChooser.APPROVE_OPTION) {
                File selectedFile = fchFileChooser.getSelectedFile();
                this.txtMigrationDatabasePath.setText(selectedFile.getPath());
                mniSelectTargetDatabase.setState(true);
            }
            else mniSelectTargetDatabase.setState(false);
        }
    }//GEN-LAST:event_mniSelectTargetDatabaseActionPerformed

    private void mniMigrateAppointmentsToDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniMigrateAppointmentsToDatabaseActionPerformed
        if (this.mniSelectAppointmentCSVFile.getState() && this.mniSelectTargetDatabase.getState()){
            getEntityDescriptor().getMigrationDescriptor().getAppointment().setData(this.txtSelectedAppointmentSourceFile.getText());
            //getMigrationDescriptor().getPatient().setData(this.txtSelectedPatientSourceFile.getText());
            getEntityDescriptor().getMigrationDescriptor().getTarget().setData(this.txtMigrationDatabasePath.getText());
            getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(ViewController.MigrationViewRequest.MIGRATE_APPOINTMENTS_TO_DATABASE);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    ViewController.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        else{
            if (!this.mniSelectAppointmentCSVFile.getState())
                JOptionPane.showMessageDialog(null, 
                        "Source file has not been selected yet", 
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
            else if (!this.mniSelectTargetDatabase.getState())
                JOptionPane.showMessageDialog(null, 
                        "Target database has not been selected yet", 
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_mniMigrateAppointmentsToDatabaseActionPerformed

    private void mniSelectPatientCSVFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSelectPatientCSVFileActionPerformed
        int result = this.fchFileChooser.showOpenDialog(new JFrame());
        if (result == fchFileChooser.APPROVE_OPTION) {
            File selectedFile = fchFileChooser.getSelectedFile();
            this.txtSelectedPatientSourceFile.setText(selectedFile.getPath());
            mniSelectPatientCSVFile.setState(true);
        }
        else mniSelectAppointmentCSVFile.setState(false);
    }//GEN-LAST:event_mniSelectPatientCSVFileActionPerformed

    private void mniExitDataMigratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExitDataMigratorActionPerformed
        try{
            this.setClosed(true);
        }
        catch (PropertyVetoException e){
            
        }
    }//GEN-LAST:event_mniExitDataMigratorActionPerformed

    private void mniRemoveBadAppointmentsFromDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRemoveBadAppointmentsFromDatabaseActionPerformed
       
        if (this.mniSelectTargetDatabase.getState()){
            getEntityDescriptor().getMigrationDescriptor().getTarget().setData(this.txtMigrationDatabasePath.getText());
            getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(ViewController.MigrationViewRequest.REMOVE_BAD_APPOINTMENTS_FROM_DATABASE);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    ViewController.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        /*
        if (this.mniSelectPatientCSVFile.getState() 
                && this.mniSelectAppointmentCSVFile.getState()
                && this.mniSelectTargetDatabase.getState()){
            getMigrationDescriptor().getPatient().setData(this.txtSelectedPatientSourceFile.getText());
            getMigrationDescriptor().getAppointment().setData(this.txtSelectedAppointmentSourceFile.getText());
            getMigrationDescriptor().getTarget().setData(this.txtSelectedTargetFile.getText());
            getMigrationDescriptor().setMigrationViewRequest(ViewController.MigrationViewRequest.REMOVE_BAD_APPOINTMENTS_FROM_DATABASE);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    ViewController.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        */
        else{
            if (!this.mniSelectPatientCSVFile.getState())
                JOptionPane.showMessageDialog(null, 
                        //"Patient CSV source file has not been selected yet", 
                        "Target database file has not been selected yet",
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
            else if (!this.mniSelectAppointmentCSVFile.getState())
                JOptionPane.showMessageDialog(null, 
                        "Appointment CSV source file has not been selected yet", 
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
            else if (!this.mniSelectTargetDatabase.getState())
                JOptionPane.showMessageDialog(null, 
                        "Target database has not been selected yet", 
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_mniRemoveBadAppointmentsFromDatabaseActionPerformed

    private void mniMigratePatientsToDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniMigratePatientsToDatabaseActionPerformed
        if (this.mniSelectPatientCSVFile.getState() && this.mniSelectTargetDatabase.getState()){
            getEntityDescriptor().getMigrationDescriptor().getPatient().setData(this.txtSelectedPatientSourceFile.getText());
            getEntityDescriptor().getMigrationDescriptor().getTarget().setData(this.txtMigrationDatabasePath.getText());
            getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(ViewController.MigrationViewRequest.MIGRATE_PATIENTS_TO_DATABASE);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    ViewController.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        else{
            if (!this.mniSelectPatientCSVFile.getState())
                JOptionPane.showMessageDialog(null, 
                        "Source file has not been selected yet", 
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
            else if (!this.mniSelectTargetDatabase.getState())
                JOptionPane.showMessageDialog(null, 
                        "Target database has not been selected yet", 
                        "Error", 
                        JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_mniMigratePatientsToDatabaseActionPerformed

    private void mniTidyPatientRecordsOnDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniTidyPatientRecordsOnDatabaseActionPerformed
        getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(ViewController.MigrationViewRequest.TIDY_PATIENT_DATA_IN_DATABASE);
        getEntityDescriptor().getMigrationDescriptor().getTarget().setData(this.txtMigrationDatabasePath.getText());    
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                ViewController.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_mniTidyPatientRecordsOnDatabaseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser fchFileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem mniExitDataMigrator;
    private javax.swing.JMenuItem mniMigrateAppointmentsToDatabase;
    private javax.swing.JMenuItem mniMigratePatientsToDatabase;
    private javax.swing.JMenuItem mniRemoveBadAppointmentsFromDatabase;
    private javax.swing.JCheckBoxMenuItem mniSelectAppointmentCSVFile;
    private javax.swing.JCheckBoxMenuItem mniSelectPatientCSVFile;
    private javax.swing.JCheckBoxMenuItem mniSelectTargetDatabase;
    private javax.swing.JMenuItem mniTidyPatientRecordsOnDatabase;
    private javax.swing.JMenu mnuAction;
    private javax.swing.JTextArea txaResults;
    private javax.swing.JTextField txtMigrationDatabasePath;
    private javax.swing.JTextField txtSelectedAppointmentSourceFile;
    private javax.swing.JTextField txtSelectedPatientSourceFile;
    // End of variables declaration//GEN-END:variables

    
    private void test(){
        try{
            this.setClosed(true);
        }
        catch (PropertyVetoException e){
            
        }
    }
}
