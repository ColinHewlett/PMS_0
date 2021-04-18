/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.constants.ClinicPMS;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.view.interfaces.IView;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
/**
 *
 * @author colin
 */
public class AppointmentEditorDialog extends AppointmentViewDialog{
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    private ViewController.ViewMode viewMode = null;
    private final String CREATE_BUTTON = "Create appointment";
    private final String UPDATE_BUTTON = "Update appointment";
    private DateTimeFormatter appointmentScheduleFormat = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy ");

    /**
     * 
     * @param myController ActionListener, reference to view controller
     * @param entityDescriptor EntityDescriptor object includes collection of serialised patient objects, 
     * a selected day, and a serialised appointment if view is of an existing appointment
     * @param owner JFrame, required by JDialog object
     * @param viewMode ViewMode, indicates which mode the dialog is in: CREATE or UPDATE
     */ 
    public AppointmentEditorDialog(ActionListener myController,
            EntityDescriptor entityDescriptor, 
            JFrame owner,
            ViewController.ViewMode viewMode) {
        super(owner, true);
        initialiseDialogClosing();
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        setViewMode(viewMode);
        initComponents();
        initialiseViewMode(getViewMode());
        
        /*
        if (getViewMode().equals(ViewController.ViewMode.UPDATE)){
            initialiseViewFromED();
        }
        else this.populatePatientSelector(this.cmbSelectPatient);
        */
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
        if (e.getPropertyName().equals(
                ViewController.AppointmentViewDialogPropertyEvent.APPOINTMENT_RECEIVED.toString())){
            initialiseViewFromED();
        }
        else if (e.getPropertyName().equals(
            ViewController.AppointmentViewDialogPropertyEvent.APPOINTMENT_VIEW_ERROR.toString())){
            ViewController.displayErrorMessage(getEntityDescriptor().getError(),
                                               "Appointment editor dialog error",
                                               JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    
    public void initialiseView(){
        LocalDate day = getEntityDescriptor().getRequest().getDay();
        this.cmbSelectStartTime.setRenderer(new SelectStartTimeLocalDateTimeRenderer());
        //this.cmbSelectStartTime.setEditable(false);
        
        populateSelectStartTime(day);
        populatePatientSelector(this.cmbSelectPatient);
        this.cmbSelectPatient.setEditable(false);
        if (getViewMode().equals(ViewController.ViewMode.UPDATE)){
            this.cmbSelectStartTime.setSelectedItem(
                    getEntityDescriptor().getAppointment().getData().getStart());
            this.spnDurationHours.setValue(getHoursFromDuration(getEntityDescriptor().getAppointment().getData().getDuration().toMinutes()));
            this.spnDurationMinutes.setValue(getMinutesFromDuration(getEntityDescriptor().getAppointment().getData().getDuration().toMinutes()));
            this.txaNotes.setText(getEntityDescriptor().getAppointment().getData().getNotes());
            this.cmbSelectPatient.setSelectedItem(getEntityDescriptor().getAppointment().getAppointee());
        }
        else this.cmbSelectStartTime.setSelectedIndex(0);
        //else this.cmbSelectStartTime.setSelectedIndex(0);
        this.setTitle("Appointment editor for " + day.format(appointmentScheduleFormat));
    }
    private void populateSelectStartTime(LocalDate day){
        DefaultComboBoxModel<LocalDateTime> model = new DefaultComboBoxModel<>();
        LocalDateTime value = day.atTime(ClinicPMS.FIRST_APPOINTMENT_SLOT);
        do{
            model.addElement(value);
            value = value.plusMinutes(5);   
        }while(!value.isAfter(day.atTime(ClinicPMS.LAST_APPOINTMENT_SLOT)));
        this.cmbSelectStartTime.setModel(model);
    }
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    private ViewController.ViewMode getViewMode(){
        return this.viewMode;
    }
    private void setViewMode(ViewController.ViewMode value){
        this.viewMode = value;
    }
    private void initialiseViewMode(ViewController.ViewMode value){
        setViewMode(value);
        if (getViewMode().equals(ViewController.ViewMode.CREATE)){
            this.btnCreateUpdateAppointment.setText(CREATE_BUTTON);
        }
        else if (getViewMode().equals(ViewController.ViewMode.UPDATE)){
            this.btnCreateUpdateAppointment.setText(UPDATE_BUTTON);
        }
    }
    /**
     * The configured window listener is entered when [1] the cancel button is clicked,
     * which despatches a WINDOW_CLOSING event, and [2] when the dialog window "X" is clicked 
     * On entry to listener 
     * -> if dialog default closing behaviour is DO_NOTHING_ON_CLOSE, which is configured during dialog construction, user is prompted to confirm the closing of dialog
     * ->-> on receipt of user confirmation an APPOINTMENT_VIEW_CLOSE_REQUEST action event is sent to the view controller
     * Its the responsibility of the view controller to re-configure the dialog to DISPOSE_ON_CLOSE, and then despatch a WINDOW_CLOSING  event 
     * 
     */
    private void initialiseDialogClosing(){
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (AppointmentEditorDialog.this.getDefaultCloseOperation()==JDialog.DO_NOTHING_ON_CLOSE){
                    if (checkOKToCloseDialog()==JOptionPane.YES_OPTION){
                        AppointmentEditorDialog.this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        ActionEvent actionEvent = new ActionEvent(AppointmentEditorDialog.this,
                                ActionEvent.ACTION_PERFORMED,
                                ViewController.AppointmentViewDialogActionEvent.
                                        APPOINTMENT_VIEW_CLOSE_REQUEST.toString());
                        AppointmentEditorDialog.this.getMyController().actionPerformed(actionEvent);
                    }
                } 
            }
        });
    }
    private void initialiseEntityDescriptorFromView(){
        //get the appointment with  which the view was initialised (in particular the appointment key)
        getEntityDescriptor().getRequest().setAppointment(
                    getEntityDescriptor().getAppointment());
        //update this from current state of view
        getEntityDescriptor().getRequest().setPatient(
                (EntityDescriptor.Patient)this.cmbSelectPatient.getSelectedItem());
        getEntityDescriptor().getRequest().getAppointment().getData().
                setStart((LocalDateTime)this.cmbSelectStartTime.getSelectedItem());
        getEntityDescriptor().getRequest().getAppointment().getData().
                setDuration(getDurationFromView());
        getEntityDescriptor().getRequest().getAppointment().getData().
                setNotes(this.txaNotes.getText());
    }
    private Duration getDurationFromView(){
        return Duration.ofMinutes(
                ((int)this.spnDurationHours.getValue() * 60) + 
                ((int)this.spnDurationMinutes.getValue()));
    }
    /**
     * On entry the local EntityDescriptor.Appointment is initialised 
     */
    private void initialiseViewFromED(){
        DateTimeFormatter hhmmFormat = DateTimeFormatter.ofPattern("HH:mm");
        //this.spnStartTime.setValue(getEntityDescriptor().getAppointment().getData().getStart().format(hhmmFormat)); 
        this.spnDurationHours.setValue(getHoursFromDuration(getEntityDescriptor().getAppointment().getData().getDuration().toMinutes()));
        this.spnDurationMinutes.setValue(getMinutesFromDuration(getEntityDescriptor().getAppointment().getData().getDuration().toMinutes()));
        this.txaNotes.setText(getEntityDescriptor().getAppointment().getData().getNotes());
        populatePatientSelector(this.cmbSelectPatient);
        if (!getEntityDescriptor().getAppointment().getData().IsEmptySlot()){
            this.cmbSelectPatient.setSelectedItem(getEntityDescriptor().getAppointment().getAppointee());
        }
    }
    private Integer getHoursFromDuration(long duration){
        return (int)duration / 60;
    }
    private Integer getMinutesFromDuration(long duration){
        return (int)duration % 60;
    }
    private void populatePatientSelector(JComboBox<EntityDescriptor.Patient> selector){
        DefaultComboBoxModel<EntityDescriptor.Patient> model = 
                new DefaultComboBoxModel<>();
        ArrayList<EntityDescriptor.Patient> patients = 
                getEntityDescriptor().getPatients().getData();
        Iterator<EntityDescriptor.Patient> it = patients.iterator();
        while (it.hasNext()){
            EntityDescriptor.Patient patient = it.next();
            model.addElement(patient);
        }
        selector.setModel(model);
        selector.setSelectedIndex(-1);
    }
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener value){
        this.myController = value;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAppointmentDetails = new javax.swing.JPanel();
        lblDialogForAppointmentDefinitionTitle1 = new javax.swing.JLabel();
        lblDialogForAppointmentDefinitionTitle2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        spnDurationHours = new javax.swing.JSpinner(new SpinnerNumberModel(0,0,8,1));
        spnDurationMinutes = new javax.swing.JSpinner(new SpinnerNumberModel(0,0,55,5));
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbSelectPatient = new javax.swing.JComboBox<EntityDescriptor.Patient>();
        cmbSelectStartTime = new javax.swing.JComboBox<LocalDateTime>();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txaNotes = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        btnCreateUpdateAppointment = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Appointment editor");

        pnlAppointmentDetails.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblDialogForAppointmentDefinitionTitle1.setText("Patient");

        lblDialogForAppointmentDefinitionTitle2.setText("Start time");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Duration"));

        jLabel1.setText("hours");

        jLabel2.setText("minutes");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnDurationMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(spnDurationHours, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnDurationHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnDurationMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        cmbSelectPatient.setModel(new javax.swing.DefaultComboBoxModel<EntityDescriptor.Patient>());
        cmbSelectPatient.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cmbSelectStartTime.setModel(new javax.swing.DefaultComboBoxModel<LocalDateTime>());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Notes"));

        txaNotes.setColumns(20);
        txaNotes.setRows(5);
        jScrollPane2.setViewportView(txaNotes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlAppointmentDetailsLayout = new javax.swing.GroupLayout(pnlAppointmentDetails);
        pnlAppointmentDetails.setLayout(pnlAppointmentDetailsLayout);
        pnlAppointmentDetailsLayout.setHorizontalGroup(
            pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAppointmentDetailsLayout.createSequentialGroup()
                        .addComponent(lblDialogForAppointmentDefinitionTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                        .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAppointmentDetailsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                                .addComponent(lblDialogForAppointmentDefinitionTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(cmbSelectStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(70, 70, 70))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAppointmentDetailsLayout.setVerticalGroup(
            pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDialogForAppointmentDefinitionTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDialogForAppointmentDefinitionTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSelectStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnCreateUpdateAppointment.setText("Update appointment");
        btnCreateUpdateAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateUpdateAppointmentActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnCreateUpdateAppointment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(33, 33, 33)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateUpdateAppointment)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlAppointmentDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlAppointmentDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_btnCancelActionPerformed
    private void btnCreateUpdateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateUpdateAppointmentActionPerformed
        int OKToSaveAppointment = JOptionPane.YES_OPTION;
        evt = null;
        initialiseEntityDescriptorFromView();
        /**
         * check if an appointee has been defined
         * -- note this is defined in ed.getRequest().getPatient() 
         * -- appointee for appointment is not defined in ed.getRequest().getAppointment().getAppointee()!!
         * check if a non zero duration value has been defined
         * check if no notes have been defined if still ok to save appointment
         */
        if (getEntityDescriptor().getRequest().getPatient()== null){
            JOptionPane.showMessageDialog(this, "A patient has not been selected for this appointment");
        }
        else if (getEntityDescriptor().getAppointment().getData().getDuration().isZero()){
            JOptionPane.showMessageDialog(this, "Defined duration for appointment must be longer than zero minutes");
        }
        else {
            if (getEntityDescriptor().getRequest().getAppointment().getData().getNotes().isEmpty()){
                String[] options = {"Yes", "No"};
                OKToSaveAppointment = JOptionPane.showOptionDialog(this,
                                "No notes defined for appointment. Save anyway?",null,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                options,
                                null);
            }
            if (OKToSaveAppointment==JOptionPane.YES_OPTION){
                switch (getViewMode()){
                    case CREATE -> {
                        evt = new ActionEvent(AppointmentEditorDialog.this,
                                ActionEvent.ACTION_PERFORMED,
                                ViewController.AppointmentViewDialogActionEvent.
                                        APPOINTMENT_VIEW_CREATE_REQUEST.toString());
                        AppointmentEditorDialog.this.getMyController().actionPerformed(evt); 
                    }
                    case UPDATE -> {
                        evt = new ActionEvent(AppointmentEditorDialog.this,
                                ActionEvent.ACTION_PERFORMED,
                                ViewController.AppointmentViewDialogActionEvent.
                                        APPOINTMENT_VIEW_UPDATE_REQUEST.toString());
                        AppointmentEditorDialog.this.getMyController().actionPerformed(evt); 

                    }
                }
            }
        }       
    }//GEN-LAST:event_btnCreateUpdateAppointmentActionPerformed
    private int checkOKToCloseDialog(){
        String[] options = {"Yes", "No"};
        int close = JOptionPane.showOptionDialog(this,
                        "Any unsaved changes to appointment view will be lost. Cancel anyway?",null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        null);
        return close;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreateUpdateAppointment;
    private javax.swing.JComboBox<EntityDescriptor.Patient> cmbSelectPatient;
    private javax.swing.JComboBox<LocalDateTime> cmbSelectStartTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle1;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle2;
    private javax.swing.JPanel pnlAppointmentDetails;
    private javax.swing.JSpinner spnDurationHours;
    private javax.swing.JSpinner spnDurationMinutes;
    private javax.swing.JTextArea txaNotes;
    // End of variables declaration//GEN-END:variables
}
