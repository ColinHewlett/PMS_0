/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
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
public class PatientAppointmentContactView extends View {
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    private ActionListener myController = null;
    private JTable tblPatientAppointmentContacts = null;

    /**
     * Creates new form PatientAppointmentContactList
     */
    public PatientAppointmentContactView(ActionListener myController, EntityDescriptor value) {
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
                        PatientAppointmentContactView.this,ActionEvent.ACTION_PERFORMED,
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
            setSize(850,350);
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
    
    private void populatePatientAppointmentContactsTable(EntityDescriptor.Appointments a){
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
        Iterator<EntityDescriptor.Appointment> it = a.getData().iterator();
        while (it.hasNext()){
            ((PatientAppointmentContactView6ColumnTableModel)this.tblPatientAppointmentContacts.getModel()).addElement(it.next());
        }
        //model.fireTableDataChanged();
        JTableHeader tableHeader = this.tblPatientAppointmentContacts.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        
        this.tblPatientAppointmentContacts.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblPatientAppointmentContacts.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblPatientAppointmentContacts.setDefaultRenderer(EntityDescriptor.Patient.class, new AppointmentsTablePatientRenderer());
        
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

        setPreferredSize(new java.awt.Dimension(762, 557));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrPatientAppointmentContactView, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrPatientAppointmentContactView, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane scrPatientAppointmentContactView;
    // End of variables declaration//GEN-END:variables
}
