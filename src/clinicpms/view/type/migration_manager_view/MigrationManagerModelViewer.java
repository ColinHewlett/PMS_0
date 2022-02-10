/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.type.migration_manager_view;

import clinicpms.controller.EntityDescriptor;
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
import java.text.DecimalFormat;
import java.time.Duration;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author colin
 */
public class MigrationManagerModelViewer extends View {
    private ActionListener myController = null;
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    private View.Viewer myViewType = null;

    public final String ENSURE_NO_ORPHANED_APPOINTMENTS = "Referential integrity check";
    public final String MIGRATE_APPOINTMENT_CSV_FILE_HEADER = "Populate appointment from CSV file";
    public final String EXPORT_MIGRATED_DATA_TO_PMS = "Export migrated data to PMS database";
    public final String MIGRATE_PATIENT_CSV_FILE_HEADER = "Populate patient from CSV file";
    public final String SELECTED_APPOINTMENT_CSV_FILE_HEADER = "Selected appointments CSV file -> ";
    public final String SELECTED_PATIENT_CSV_FILE_HEADER = "Selected patients CSV file -> ";
    public final String SELECTED_MIGRATION_DATABASE_HEADER = "Selected target migration database -> ";
    public final String SELECTED_PMS_DATABASE_HEADER = "Selected target PMS database -> ";
    
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
    
    private void initialiseStatsDisplay(){
        String report = "";
        if (this.txaResults.getText().length()>0) report = "\n";
        switch(getEntityDescriptor().getMigrationDescriptor().getMigrationViewRequest()){
            case POPULATE_APPOINTMENT_TABLE:
                report = "MIGRATE APPOINTMENTS TO DATABASE \n"
                + "Total number of appointments = " + getEntityDescriptor().getMigrationDescriptor().getAppointmentTableCount() + "\n"
                + "Operation duration = " + normaliseDuration(getEntityDescriptor().getMigrationDescriptor().getMigrationActionDuration()) + "\n";
                break;
            case POPULATE_PATIENT_TABLE:
                report = "MIGRATE PATIENTS TO DATABASE \n"
                + "Total number of patients = " + getEntityDescriptor().getMigrationDescriptor().getPatientTableCount() + "\n"
                + "Operation duration = " + normaliseDuration(getEntityDescriptor().getMigrationDescriptor().getMigrationActionDuration()) + "\n";
                break;
            case REMOVE_BAD_APPOINTMENTS_FROM_DATABASE:
                report = "REMOVAL OF BAD APPOINTMENTS FROM DATABASE \n"
                + "Total number of remaining appointments = " + getEntityDescriptor().getMigrationDescriptor().getAppointmentTableCount() + "\n"
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaResults = new javax.swing.JTextArea();
        mnbMigrationManagement = new javax.swing.JMenuBar();
        mnuMigrationActions = new javax.swing.JMenu();
        mniMigrateAppointmentsCSV = new javax.swing.JMenuItem();
        mniMigrateFromPatientsCSV = new javax.swing.JMenuItem();
        mniEnsureNoOrphanedAppointments = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mniExportMigrationToPMS = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mniCloseMigrationManager = new javax.swing.JMenuItem();
        mnuCSVSourceFiles = new javax.swing.JMenu();
        mniSelectedAppointmentsCSVFilePath = new javax.swing.JMenuItem();
        mniSelectedPatientsCSVFilePath = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mniSelectedTargetMigrationDatabase = new javax.swing.JMenuItem();

        jLabel1.setText("Summary of selected migration activity");

        txaResults.setColumns(20);
        txaResults.setRows(5);
        jScrollPane1.setViewportView(txaResults);

        mnuMigrationActions.setText("Migration actions");

        mniMigrateAppointmentsCSV.setText("migrate from appointments csv");
        mniMigrateAppointmentsCSV.setText(MIGRATE_APPOINTMENT_CSV_FILE_HEADER);
        mniMigrateAppointmentsCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniMigrateAppointmentsCSVActionPerformed(evt);
            }
        });
        mnuMigrationActions.add(mniMigrateAppointmentsCSV);

        mniMigrateFromPatientsCSV.setText("migrate from Patients csv");
        mniMigrateFromPatientsCSV.setText(MIGRATE_PATIENT_CSV_FILE_HEADER);
        mniMigrateFromPatientsCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniMigrateFromPatientsCSVActionPerformed(evt);
            }
        });
        mnuMigrationActions.add(mniMigrateFromPatientsCSV);

        mniEnsureNoOrphanedAppointments.setText("Ensure referential integrity");
        mniEnsureNoOrphanedAppointments.setText(ENSURE_NO_ORPHANED_APPOINTMENTS);
        mniEnsureNoOrphanedAppointments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniEnsureNoOrphanedAppointmentsActionPerformed(evt);
            }
        });
        mnuMigrationActions.add(mniEnsureNoOrphanedAppointments);
        mnuMigrationActions.add(jSeparator2);

        mniExportMigrationToPMS.setText("Export migrated data to PMS database");
        mniExportMigrationToPMS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExportMigrationToPMSActionPerformed(evt);
            }
        });
        mnuMigrationActions.add(mniExportMigrationToPMS);
        mnuMigrationActions.add(jSeparator3);

        mniCloseMigrationManager.setText("Close migration manager");
        mniCloseMigrationManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniCloseMigrationManagerActionPerformed(evt);
            }
        });
        mnuMigrationActions.add(mniCloseMigrationManager);

        mnbMigrationManagement.add(mnuMigrationActions);

        mnuCSVSourceFiles.setText("Migration files");

        mniSelectedAppointmentsCSVFilePath.setText("Selected appointments CSV file ->");
        mniSelectedAppointmentsCSVFilePath.setText(SELECTED_APPOINTMENT_CSV_FILE_HEADER);
        mniSelectedAppointmentsCSVFilePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSelectedAppointmentsCSVFilePathActionPerformed(evt);
            }
        });
        mnuCSVSourceFiles.add(mniSelectedAppointmentsCSVFilePath);

        mniSelectedPatientsCSVFilePath.setText("Selected patients CSV file ->");
        mniSelectedPatientsCSVFilePath.setText(SELECTED_PATIENT_CSV_FILE_HEADER);
        mniSelectedPatientsCSVFilePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSelectedPatientsCSVFilePathActionPerformed(evt);
            }
        });
        mnuCSVSourceFiles.add(mniSelectedPatientsCSVFilePath);
        mnuCSVSourceFiles.add(jSeparator1);

        mniSelectedTargetMigrationDatabase.setText("Selected target migration database ->");
        mniSelectedTargetMigrationDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSelectedTargetMigrationDatabaseActionPerformed(evt);
            }
        });
        mnuCSVSourceFiles.add(mniSelectedTargetMigrationDatabase);

        mnbMigrationManagement.add(mnuCSVSourceFiles);

        setJMenuBar(mnbMigrationManagement);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(134, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mniSelectedAppointmentsCSVFilePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSelectedAppointmentsCSVFilePathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mniSelectedAppointmentsCSVFilePathActionPerformed

    private void mniSelectedPatientsCSVFilePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSelectedPatientsCSVFilePathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mniSelectedPatientsCSVFilePathActionPerformed

    private void mniSelectedTargetMigrationDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSelectedTargetMigrationDatabaseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mniSelectedTargetMigrationDatabaseActionPerformed

    private void mniMigrateAppointmentsCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniMigrateAppointmentsCSVActionPerformed
        getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(EntityDescriptor.MigrationViewRequest.POPULATE_APPOINTMENT_TABLE);
            ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_mniMigrateAppointmentsCSVActionPerformed

    private void mniMigrateFromPatientsCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniMigrateFromPatientsCSVActionPerformed
        getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(EntityDescriptor.MigrationViewRequest.POPULATE_PATIENT_TABLE);
            ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_mniMigrateFromPatientsCSVActionPerformed

    private void mniEnsureNoOrphanedAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniEnsureNoOrphanedAppointmentsActionPerformed
        getEntityDescriptor().getMigrationDescriptor().setMigrationViewRequest(EntityDescriptor.MigrationViewRequest.REMOVE_BAD_APPOINTMENTS_FROM_DATABASE);
        ActionEvent actionEvent = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            EntityDescriptor.MigratorViewControllerActionEvent.APPOINTMENT_MIGRATOR_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_mniEnsureNoOrphanedAppointmentsActionPerformed

    private void mniExportMigrationToPMSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExportMigrationToPMSActionPerformed
        ActionEvent actionEvent = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            EntityDescriptor.MigratorViewControllerActionEvent.EXPORT_MIGRATED_DATA_TO_PMS_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_mniExportMigrationToPMSActionPerformed

    private void mniCloseMigrationManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCloseMigrationManagerActionPerformed
        // TODO add your handling code here:
        try{
            this.setClosed(true);
        }catch (PropertyVetoException ex){

        }
    }//GEN-LAST:event_mniCloseMigrationManagerActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuBar mnbMigrationManagement;
    private javax.swing.JMenuItem mniCloseMigrationManager;
    private javax.swing.JMenuItem mniEnsureNoOrphanedAppointments;
    private javax.swing.JMenuItem mniExportMigrationToPMS;
    private javax.swing.JMenuItem mniMigrateAppointmentsCSV;
    private javax.swing.JMenuItem mniMigrateFromPatientsCSV;
    private javax.swing.JMenuItem mniSelectedAppointmentsCSVFilePath;
    private javax.swing.JMenuItem mniSelectedPatientsCSVFilePath;
    private javax.swing.JMenuItem mniSelectedTargetMigrationDatabase;
    private javax.swing.JMenu mnuCSVSourceFiles;
    private javax.swing.JMenu mnuMigrationActions;
    private javax.swing.JTextArea txaResults;
    // End of variables declaration//GEN-END:variables

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
        String message;
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
            //message = ex.getMessage();
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
    /**
     * Creates new form MigrationManagerModelViewer
     */
    public MigrationManagerModelViewer(View.Viewer myViewType,ActionListener myController,
            EntityDescriptor entityDescriptor, 
            Component parent) {
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
        initialiseRecordCounts();
        this.setVisible(true);

        startModal(this);
 
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
                        MigrationManagerModelViewer.this,ActionEvent.ACTION_PERFORMED,
                        EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENTS_VIEW_CLOSED.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        this.addInternalFrameListener(internalFrameAdapter);
    }

    @Override
    public void initialiseView(){
        this.mniSelectedAppointmentsCSVFilePath.setText(this.SELECTED_APPOINTMENT_CSV_FILE_HEADER 
                + getEntityDescriptor().getMigrationDescriptor().getAppointmentCSVFilePath());
        this.mniSelectedPatientsCSVFilePath.setText(this.SELECTED_PATIENT_CSV_FILE_HEADER 
                + getEntityDescriptor().getMigrationDescriptor().getPatientCSVFilePath());
        this.mniSelectedTargetMigrationDatabase.setText(this.SELECTED_MIGRATION_DATABASE_HEADER 
                + getEntityDescriptor().getMigrationDescriptor().getTargetMigrationDatabaseURL());
        this.mniExportMigrationToPMS.setText(this.EXPORT_MIGRATED_DATA_TO_PMS);
    }
    
    @Override 
    public EntityDescriptor getEntityDescriptor(){
        return entityDescriptor;   
    }
    
    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        int count;
        String propertyName = e.getPropertyName();
        
        if (propertyName.equals(EntityDescriptor.MigrationViewPropertyChangeEvents.MIGRATION_ACTION_COMPLETE.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            initialiseStatsDisplay(); 
            initialiseRecordCounts();
        }
    }
  
    private void initialiseRecordCounts(){
        Integer count;
        count = getEntityDescriptor().getMigrationDescriptor().getAppointmentTableCount();
        if (count!=null)
            this.mniMigrateAppointmentsCSV.setText(
                this.MIGRATE_APPOINTMENT_CSV_FILE_HEADER + " ("
                        + String.valueOf(count) + " records)");
        else
            this.mniMigrateAppointmentsCSV.setText(
                this.MIGRATE_APPOINTMENT_CSV_FILE_HEADER + " (table not yet created)");
        
        count = getEntityDescriptor().getMigrationDescriptor().getPatientTableCount();
        if (count!=null)
            this.mniMigrateFromPatientsCSV.setText(
                this.MIGRATE_PATIENT_CSV_FILE_HEADER + " ("
                        + String.valueOf(count) + " records)");
        else
            this.mniMigrateFromPatientsCSV.setText(
                this.MIGRATE_PATIENT_CSV_FILE_HEADER + " (table not yet created)");
        
    }
}
