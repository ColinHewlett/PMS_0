/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.schedule_contact_details_view;

import clinicpms.view.TableHeaderCellBorderRenderer;
import clinicpms.view.views.appontment_schedule_view.AppointmentsTablePatientRenderer;
import clinicpms.view.views.appontment_schedule_view.AppointmentsTableLocalDateTimeRenderer;
import clinicpms.view.views.appontment_schedule_view.AppointmentsTableDurationRenderer;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.view.View;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author colin
 */
public class ScheduleContactDetailsView extends View {
    private View.Viewer myViewType = null;
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    private ActionListener myController = null;
    private JTable tblPatientAppointmentContacts = null;

    /**
     * 
     * @param myViewType
     * @param myController
     * @param value 
     */
    public ScheduleContactDetailsView(View.Viewer myViewType, ActionListener myController, EntityDescriptor value) {
        this.setMyViewType(myViewType);
        this.myController = myController;
        this.entityDescriptor = value;
        initComponents();
        this.populatePatientAppointmentContactsTable(getEntityDescriptor().getAppointments());
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
                        ScheduleContactDetailsView.this,ActionEvent.ACTION_PERFORMED,
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
        
    }
    
    @Override
    public void initialiseView(){
        try{
            setVisible(true);
            setTitle("Patient contact list for appointments on " + getEntityDescriptor().getRequest().getDay().format(DateTimeFormatter.ofPattern("EEEE, MMM dd")));
            setClosable(true);
            setMaximizable(false);
            setIconifiable(true);
            setResizable(false);
            setSelected(true);
            setSize(850,375);
        }
        catch (PropertyVetoException ex){
            
        }
    }
    
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return entityDescriptor;
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    
    private void populatePatientAppointmentContactsTable(ArrayList<Appointment> a){
        PatientAppointmentContactView6ColumnTableModel model;
        if (this.tblPatientAppointmentContacts!=null){
            this.scrPatientAppointmentContactView.remove(this.tblPatientAppointmentContacts);   
        }
        this.tblPatientAppointmentContacts = new JTable(new PatientAppointmentContactView6ColumnTableModel());
        scrPatientAppointmentContactView.setViewportView(this.tblPatientAppointmentContacts);
        //setEmptySlotAvailabilityTableListener();
        model = (PatientAppointmentContactView6ColumnTableModel)this.tblPatientAppointmentContacts.getModel();
        model.removeAllElements();
//model.fireTableDataChanged();
        Iterator<Appointment> it = a.iterator();
        while (it.hasNext()){
            ((PatientAppointmentContactView6ColumnTableModel)this.tblPatientAppointmentContacts.getModel()).addElement(it.next());
        }
        //model.fireTableDataChanged();
        JTableHeader tableHeader = this.tblPatientAppointmentContacts.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        
        this.tblPatientAppointmentContacts.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblPatientAppointmentContacts.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblPatientAppointmentContacts.setDefaultRenderer(Patient.class, new AppointmentsTablePatientRenderer());
        
        /**
         * configure table header & column widths
         */
        TableColumnModel columnModel = this.tblPatientAppointmentContacts.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(190);
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setPreferredWidth(60);
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(2).setPreferredWidth(60);
        columnModel.getColumn(2).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(3).setPreferredWidth(105);
        columnModel.getColumn(3).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(4).setPreferredWidth(400);
        columnModel.getColumn(4).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(5).setPreferredWidth(75);
        columnModel.getColumn(5).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        scrPatientAppointmentContactView = new javax.swing.JScrollPane();
        btnCloseView = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(762, 557));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrPatientAppointmentContactView, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrPatientAppointmentContactView, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnCloseView.setText("Close view");
        btnCloseView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseViewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCloseView)
                .addGap(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCloseView)
                .addGap(20, 20, 20))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCloseView;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane scrPatientAppointmentContactView;
    // End of variables declaration//GEN-END:variables

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }

    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
}
