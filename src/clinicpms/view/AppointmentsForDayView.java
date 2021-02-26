/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.controller.ViewController.AppointmentViewControllerActionEvent;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyVetoException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeEvent;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author colin
 */
public class AppointmentsForDayView extends View{
    private enum COLUMN{From,Duration,Patient,Notes};
    private JTable tblAppointmentsForDay = null;
    private ActionListener myController = null;
    private AppointmentsTableModel tableModel = null;
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;

    /**
     * 
     * @param e PropertyChangeEvent which supports the following properties
     * --
     */ 
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        if (propertyName.equals(ViewController.AppointmentViewControllerPropertyEvent.APPOINTMENTS_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            initialiseViewFromEDCollection();
        }
    }
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }   
    private void initialiseViewFromEDCollection(){
        ArrayList<EntityDescriptor.Appointment> appointments = 
                getEntityDescriptor().getAppointments().getData();
        tableModel = new AppointmentsTableModel(appointments);
        this.tblAppointmentsForDay.setModel(tableModel);
    }
    private void initialiseEDSelectionFromView(int row){
        if (row > -1){
            getEntityDescriptor().getRequest().setAppointment(
                    getEntityDescriptor().getAppointments().getData().get(row));
        }
    }

    /**
     * 
     * @param controller
     * @param ed 
     */
    public AppointmentsForDayView(ActionListener controller, EntityDescriptor ed) {
        this.myController = controller;
        this.setEntityDescriptor(ed);
        //setView(this);
        initComponents();
        
        List<EntityDescriptor.Appointment> appointments = new ArrayList<EntityDescriptor.Appointment>();
        this.tableModel = new AppointmentsTableModel(appointments);
        this.tblAppointmentsForDay = new JTable(tableModel);
        this.scrAppointmentsForDayTable.add(this.tblAppointmentsForDay);
        this.tblAppointmentsForDay.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblAppointmentsForDay.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblAppointmentsForDay.setDefaultRenderer(EntityDescriptor.Patient.class, new AppointmentsTablePatientRenderer());
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.From.ordinal()).setPreferredWidth(40);
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.Duration.ordinal()).setPreferredWidth(40);
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.Patient.ordinal()).setPreferredWidth(150);
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.Notes.ordinal()).setPreferredWidth(350);
        //this.initialiseViewFromEDCollection();
        
        DatePicker appointmentDayPicker = new DatePicker();
        appointmentDayPicker.addDateChangeListener(new AppointmentsForDayView.AppointmentDateChangeListener());
        
        pnlControls.add(appointmentDayPicker);
        this.txtAppointmentDay.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlControls.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                appointmentDayPicker.openPopup();
            }
        });

    }
    private ActionListener getMyController(){
        return myController;
    }
    
    public void addInternalFrameClosingListener(){
        /**
         * Establish an InternalFrameListener for when the view is closed 
         */
        
        internalFrameAdapter = new InternalFrameAdapter(){
            @Override  
            public void internalFrameClosing(InternalFrameEvent e) {
                ActionEvent actionEvent = new ActionEvent(
                        AppointmentsForDayView.this,ActionEvent.ACTION_PERFORMED,
                        ViewController.AppointmentViewControllerActionEvent.APPOINTMENTS_VIEW_CLOSED.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        this.addInternalFrameListener(internalFrameAdapter);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrAppointmentsForDayTable = new javax.swing.JScrollPane();
        pnlControls = new javax.swing.JPanel();
        txtAppointmentDay = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnCreateAppointment = new javax.swing.JButton();
        btnUpdateAppointment = new javax.swing.JButton();
        btnCloseView = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        txtAppointmentDay.setText("jTextField1");

        jLabel1.setText("Select day of appointments");

        btnCreateAppointment.setText("Create new appointment");
        btnCreateAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateAppointmentActionPerformed(evt);
            }
        });

        btnUpdateAppointment.setText("Update selected appointment");
        btnUpdateAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateAppointmentActionPerformed(evt);
            }
        });

        btnCloseView.setText("Close view");
        btnCloseView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseViewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAppointmentDay, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnCreateAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnUpdateAppointment)
                .addGap(30, 30, 30)
                .addComponent(btnCloseView)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlControlsLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtAppointmentDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreateAppointment)
                    .addComponent(btnUpdateAppointment)
                    .addComponent(btnCloseView))
                .addGap(2, 2, 2))
        );

        jMenu1.setText("File");

        jMenuItem1.setText("jMenuItem1");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrAppointmentsForDayTable)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(scrAppointmentsForDayTable, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCreateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateAppointmentActionPerformed
        initialiseEDSelectionFromView(-1);
        ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                AppointmentViewControllerActionEvent.APPOINTMENT_CREATE_VIEW_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnCreateAppointmentActionPerformed

    private void btnUpdateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateAppointmentActionPerformed
        int row = this.tblAppointmentsForDay.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for update");
        }
        else if (getEntityDescriptor().getAppointments().getData().get(row).getData().IsEmptySlot()){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for update");
        }
        else{
            initialiseEDSelectionFromView(row);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    AppointmentViewControllerActionEvent.APPOINTMENT_UPDATE_VIEW_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }//GEN-LAST:event_btnUpdateAppointmentActionPerformed

    private void btnCloseViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseViewActionPerformed

        try{
            this.setClosed(true);
        }
        catch (PropertyVetoException e){
            
        }
    }//GEN-LAST:event_btnCloseViewActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCloseView;
    private javax.swing.JButton btnCreateAppointment;
    private javax.swing.JButton btnUpdateAppointment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JScrollPane scrAppointmentsForDayTable;
    private javax.swing.JTextField txtAppointmentDay;
    // End of variables declaration//GEN-END:variables

    class AppointmentDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date == null) {
                //if date field cleared, make equal to now()
                date = LocalDate.now();
            }
            if (!getEntityDescriptor().getRequest().getDay().equals(date)){
                //only if selected date different from selection in EntityDescriptor
                getEntityDescriptor().getRequest().setDay(date);
                ActionEvent actionEvent = new ActionEvent(this, 
                        ActionEvent.ACTION_PERFORMED,
                        AppointmentViewControllerActionEvent.APPOINTMENTS_REQUEST.toString());
                getMyController().actionPerformed(actionEvent);
            }
            
        }
    }

}
