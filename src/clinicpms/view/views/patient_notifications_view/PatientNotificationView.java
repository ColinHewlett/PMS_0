/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.patient_notifications_view;

import clinicpms.view.View;
//import clinicpms.view.views.appontment_schedule_view.AppointmentsTableLocalDateTimeRenderer;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.model.PatientNotification;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class PatientNotificationView extends View implements ItemListener {
    private View.Viewer myViewType = null;
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    private ActionListener myController = null;
    private JTable tblPatientNotifications = null;
    private TableCellRenderer patientNotificationTableDefaultRenderer = null;

    private final String DISPLAY_UNACTIONED_NOTIFICATIONS_TEXT = "display unactioned notifications";
    private final String DISPLAY_ALL_NOTIFICATIONS_TEXT = "display all notifications";
    private final String UI_UNACTIONED_NOTIFICATIONS_TITLE = "Outstanding patient notifications";
    private final String UI_ALL_NOTIFICATIONS_TITLE = "Patient notifications";
    
    private String UITitle = UI_UNACTIONED_NOTIFICATIONS_TITLE;

    private String getUITitle(){
        return UITitle;
    }
    private void setUITitle(String title){
        UITitle = title;
    }
    private void setEntityDescriptor(EntityDescriptor value){
        entityDescriptor = value;
    }
    
    private TableCellRenderer getPatientNotificationTableDefaultRenderer(){
        return patientNotificationTableDefaultRenderer;
    }
    
    private void setPatientNotificationTableDefaultRenderer(TableCellRenderer renderer){
        patientNotificationTableDefaultRenderer = renderer;
    }

    /**
     * 
     * @param myViewType
     * @param myController
     * @param value 
     */
    public PatientNotificationView(View.Viewer myViewType, 
            ActionListener myController, 
            EntityDescriptor value) {
        this.setMyViewType(myViewType);
        this.myController = myController;
        setEntityDescriptor(value);
        initComponents();     
    }
    
    @Override
    public void itemStateChanged(ItemEvent e){
        EntityDescriptor.PatientNotificationViewControllerActionEvent request = null;
        String text = ((JRadioButton)e.getItem()).getText();
        switch(text){
            case DISPLAY_UNACTIONED_NOTIFICATIONS_TEXT:
                if (e.getStateChange() == ItemEvent.SELECTED)
                    request = EntityDescriptor.
                            PatientNotificationViewControllerActionEvent.
                            UNACTIONED_PATIENT_NOTIFICATIONS_REQUEST;
                            setUITitle(UI_UNACTIONED_NOTIFICATIONS_TITLE);
                break;
            case DISPLAY_ALL_NOTIFICATIONS_TEXT:
                if (e.getStateChange() == ItemEvent.SELECTED)
                    request = EntityDescriptor.
                            PatientNotificationViewControllerActionEvent.
                            PATIENT_NOTIFICATIONS_REQUEST;
                            setUITitle(UI_ALL_NOTIFICATIONS_TITLE);
                break;
        }
        if (request!=null){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                request.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }
    
    /**
     * Establish an InternalFrameListener for when the view is closed 
     * Setting DISPOSE_ON_CLOSE action when the window "X" is clicked, fires
     * InternalFrameEvent.INTERNAL_FRAME_CLOSED event for the listener to let 
     * the view controller know what's happening
     */
    @Override
    public void addInternalFrameClosingListener(){
        /**
         * Establish an InternalFrameListener for when the view is closed 
         */
        internalFrameAdapter = new InternalFrameAdapter(){
            @Override  
            public void internalFrameClosed(InternalFrameEvent e) {
                ActionEvent actionEvent = new ActionEvent(
                        PatientNotificationView.this,ActionEvent.ACTION_PERFORMED,
                        ViewController.PatientAppointmentContactListViewControllerActionEvent.PATIENT_APPOINTMENT_CONTACT_VIEW_CLOSED.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        this.addInternalFrameListener(internalFrameAdapter);
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Method processes the PropertyChangeEvent its received from the view
     * controller
     * @param e PropertyChangeEvent 
     */
    @Override
    public void propertyChange(PropertyChangeEvent e){
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
        EntityDescriptor.
                PatientNotificationViewControllerPropertyChangeEvent
                propertyName = EntityDescriptor.
                        PatientNotificationViewControllerPropertyChangeEvent.
                        valueOf(e.getPropertyName());
        switch (propertyName){
            case RECEIVED_PATIENT_NOTIFICATIONS:
                populatePatientNotificationTable(
                        getEntityDescriptor().getPatientNotifications(),false);
                setTitle(getUITitle());
                break;
               case RECEIVED_UNACTIONED_NOTIFICATIONS:
                populatePatientNotificationTable(
                        getEntityDescriptor().getPatientNotifications(),true);
                setTitle(getUITitle());
                this.rdbDisplayUnactionedNotifications.setSelected(true);
                break;
        }
    }
    
    /**
     * class assumes this method will be called by the controller; and initialises
     * -- window properties
     * -- unactioned notifications for display
     * -- radio button event listeners 
     * -- notifications table properties
     * -- patient selector control from collection of patients stored in the EntutyDsecriptor
     * -- sends controller request for unactioned notifications on the system
     * 
     */
    @Override
    public void initialiseView(){
        try{
            setVisible(true);
            setTitle(getUITitle());
            setClosable(true);
            setMaximizable(false);
            setIconifiable(true);
            setResizable(false);
            setSelected(true);
            setSize(850,450);
        }
        catch (PropertyVetoException ex){
            
        }
        this.rdbDisplayUnactionedNotifications.setSelected(true);
        this.rdbDisplayAllNotifications.addItemListener(this);
        this.rdbDisplayUnactionedNotifications.addItemListener(this);
        this.tblPatientNotifications = new JTable(new PatientNotificationView4ColumnTableModel());
        setPatientNotificationTableDefaultRenderer(this.tblPatientNotifications.getDefaultRenderer(LocalDate.class));
        scrPatientNotificationView.setViewportView(this.tblPatientNotifications);
        ViewController.setJTableColumnProperties(
                tblPatientNotifications, 
                scrPatientNotificationView.getPreferredSize().width, 
                12,23,15,50);
        this.tblPatientNotifications.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //this.tblPatientNotifications.setAutoCreateRowSorter(true);
        ActionEvent actionEvent = new ActionEvent(
            this,ActionEvent.ACTION_PERFORMED,
            EntityDescriptor.PatientNotificationViewControllerActionEvent.UNACTIONED_PATIENT_NOTIFICATIONS_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
        
    }
    
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return entityDescriptor;
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    
    private void populatePatientNotificationTable(
            ArrayList<PatientNotification> patientNotifications, boolean toAddRenderer){
        this.tblPatientNotifications.setAutoCreateRowSorter(false);
        PatientNotificationView4ColumnTableModel model = 
                (PatientNotificationView4ColumnTableModel)this.tblPatientNotifications.getModel();
        model.removeAllElements();
//model.fireTableDataChanged();
        Iterator<PatientNotification> it = patientNotifications.iterator();
        while (it.hasNext()){
            ((PatientNotificationView4ColumnTableModel)this.tblPatientNotifications.getModel()).addElement(it.next());
        }
        if (toAddRenderer)
            this.tblPatientNotifications.setDefaultRenderer(LocalDate.class, new PatientNotificationTableOverdueLocalDateRenderer());
        else 
            this.tblPatientNotifications.setDefaultRenderer(LocalDate.class, new PatientNotificationTableLocalDateRenderer());
        this.tblPatientNotifications.setAutoCreateRowSorter(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        scrPatientNotificationView = new javax.swing.JScrollPane();
        rdbDisplayUnactionedNotifications = new javax.swing.JRadioButton();
        rdbDisplayAllNotifications = new javax.swing.JRadioButton();
        btnCloseView = new javax.swing.JButton();
        btnActionSelectedNotifications = new javax.swing.JButton();
        btnAddNewNotification = new javax.swing.JButton();
        btnEditSelectedNotification = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(762, 557));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(rdbDisplayUnactionedNotifications);
        rdbDisplayUnactionedNotifications.setSelected(true);
        rdbDisplayUnactionedNotifications.setText(DISPLAY_UNACTIONED_NOTIFICATIONS_TEXT);

        buttonGroup1.add(rdbDisplayAllNotifications);
        rdbDisplayAllNotifications.setText(DISPLAY_ALL_NOTIFICATIONS_TEXT);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrPatientNotificationView)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(175, 175, 175)
                .addComponent(rdbDisplayUnactionedNotifications)
                .addGap(94, 94, 94)
                .addComponent(rdbDisplayAllNotifications)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrPatientNotificationView, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbDisplayUnactionedNotifications)
                    .addComponent(rdbDisplayAllNotifications))
                .addContainerGap())
        );

        btnCloseView.setText("Close view");
        btnCloseView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseViewActionPerformed(evt);
            }
        });

        btnActionSelectedNotifications.setText("Action selected notifications");
        btnActionSelectedNotifications.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActionSelectedNotificationsActionPerformed(evt);
            }
        });

        btnAddNewNotification.setText("Add a new notificion");
        btnAddNewNotification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNewNotificationActionPerformed(evt);
            }
        });

        btnEditSelectedNotification.setText("Edit selected notification");
        btnEditSelectedNotification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditSelectedNotificationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnAddNewNotification)
                        .addGap(70, 70, 70)
                        .addComponent(btnEditSelectedNotification)
                        .addGap(70, 70, 70)
                        .addComponent(btnActionSelectedNotifications)
                        .addGap(70, 70, 70)
                        .addComponent(btnCloseView)
                        .addGap(30, 30, 30))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCloseView)
                    .addComponent(btnActionSelectedNotifications)
                    .addComponent(btnAddNewNotification)
                    .addComponent(btnEditSelectedNotification))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseViewActionPerformed
        // TODO add your handling code here:
        try{
            this.setClosed(true);
        }
        catch (PropertyVetoException ex){
            
        }
    }//GEN-LAST:event_btnCloseViewActionPerformed

    /**
     * Request to action or remove selected notifications from the list of unactioned notifications is sent to the View Controller
     * -- an ArrayList<PatientNotification> is included in the EntityDescription::setPatientNotifications() method which identifies the selected notifications
     * @param evt 
     */
    private void btnActionSelectedNotificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActionSelectedNotificationsActionPerformed
        boolean isError = false;
        if (this.tblPatientNotifications.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(this, "no notifications have been selected and so cannot be actioned/removed from the list");
            isError = true;
        }
        if (!isError){
            int response = JOptionPane.NO_OPTION;
            String message ="Are you sure you want to remove (action) all selected notifications from the list?";
            response = JOptionPane.showConfirmDialog(this,message, "Action selected patient notifications", JOptionPane.YES_NO_OPTION);
            if (response==JOptionPane.YES_OPTION){
                PatientNotificationView4ColumnTableModel model = (PatientNotificationView4ColumnTableModel)this.tblPatientNotifications.getModel();
                ArrayList<PatientNotification> patientNotifications = new ArrayList<>();
                int selectedRows[] = tblPatientNotifications.getSelectedRows();
                for (int row : selectedRows){
                    PatientNotification patientNotification = (PatientNotification)model.getPatientNotifications().get(row);
                    patientNotifications.add(patientNotification);
                }
                getEntityDescriptor().getRequest().setPatientNotifications(patientNotifications);
                ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    EntityDescriptor.PatientNotificationViewControllerActionEvent.ACTION_PATIENT_NOTIFICATION_REQUEST.toString());
                this.getMyController().actionPerformed(actionEvent); 
            }
        }
         
    }//GEN-LAST:event_btnActionSelectedNotificationsActionPerformed

    private void btnAddNewNotificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNewNotificationActionPerformed
        // TODO add your handling code here:
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.PatientNotificationViewControllerActionEvent.CREATE_PATIENT_NOTIFICATION_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnAddNewNotificationActionPerformed

    private void btnEditSelectedNotificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditSelectedNotificationActionPerformed
        // TODO add your handling code here:
        boolean isError = false;
        if (this.tblPatientNotifications.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(this, "A notifification to edit has not been selected");
            isError = true;
        }
        if (!isError){
            PatientNotificationView4ColumnTableModel model = 
                (PatientNotificationView4ColumnTableModel)this.tblPatientNotifications.getModel();
            getEntityDescriptor().getRequest().setPatientNotification(
                    model.getElementAt(this.tblPatientNotifications.getSelectedRow()));
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    EntityDescriptor.PatientNotificationViewControllerActionEvent.UPDATE_PATIENT_NOTIFICATION_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        
    }//GEN-LAST:event_btnEditSelectedNotificationActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActionSelectedNotifications;
    private javax.swing.JButton btnAddNewNotification;
    private javax.swing.JButton btnCloseView;
    private javax.swing.JButton btnEditSelectedNotification;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton rdbDisplayAllNotifications;
    private javax.swing.JRadioButton rdbDisplayUnactionedNotifications;
    private javax.swing.JScrollPane scrPatientNotificationView;
    // End of variables declaration//GEN-END:variables

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }

    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
}
