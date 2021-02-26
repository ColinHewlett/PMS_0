/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.view.interfaces.IView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
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
/**
 *
 * @author colin
 */
public class AppointmentViewDialog extends javax.swing.JDialog 
                                   implements IView,PropertyChangeListener{
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    private ViewController.ViewMode viewMode = null;
    private final String CREATE_BUTTON = "Create appointment";
    private final String UPDATE_BUTTON = "Update appointment";
    private final String[] times =   {"08:00","08:05","08:10","08:15","08:20","08:25",
                                "08:30","08:35","08:40","08:45","08:50","08:55",
                                "09:00","09:05","09:10","09:15","09:20","09:25",
                                "09:30","09:35","09:40","09:45","09:50","09:55",
                                "10:00","10:05","10:10","10:15","10:20","10:25",
                                "10:30","10:35","10:40","10:45","10:50","10:55",
                                "11:00","11:05","11:10","11:15","11:20","11:25",
                                "11:30","11:35","11:40","11:45","11:50","11:55",
                                "12:00","12:05","12:10","12:15","12:20","12:25",
                                "12:30","12:35","12:40","12:45","12:50","12:55",
                                "13:00","13:05","13:10","13:15","13:20","13:25",
                                "13:30","13:35","13:40","13:45","13:50","13:55",
                                "14:00","14:05","14:10","14:15","14:20","14:25",
                                "14:30","14:35","14:40","14:45","14:50","14:55",
                                "15:00","15:05","15:10","15:15","15:20","15:25",
                                "15:30","15:35","15:40","15:45","15:50","15:55",
                                "16:00","16:05","16:10","16:15","16:20","16:25",
                                "16:30","16:35","16:40","16:45","16:50","16:55",
                                "17:00","17:05","17:10","17:15","17:20","17:25",
                                "17:30","17:35","17:40","17:45","17:50","17:55",
                                "18:00","18:05","18:10","18:15","18:20","18:25",
                                "18:30","18:35","18:40","18:45","18:50","18:55"};

    /**
     * 
     * @param myController ActionListener, reference to view controller
     * @param entityDescriptor EntityDescriptor object includes collection of serialised patient objects, 
     * a selected day, and a serialised appointment if view is of an existing appointment
     * @param owner JFrame, required by JDialog object
     * @param viewMode ViewMode, indicates which mode the dialog is in: CREATE or UPDATE
     */ 
    public AppointmentViewDialog(ActionListener myController,
            EntityDescriptor entityDescriptor, 
            JFrame owner,
            ViewController.ViewMode viewMode) {
        super(owner, true);
        initialiseDialogClosing();
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        setViewMode(viewMode);
        initComponents();
        this.spnStartTime.setModel(new SpinnerListModel(reverseTimes(this.times)));
        this.spnStartTime.setValue(getDefaultTime());
        initialiseViewMode(getViewMode());
        if (getViewMode().equals(ViewController.ViewMode.UPDATE)){
            initialiseViewFromED();
        }
        else this.populatePatientSelector(this.cmbSelectPatient);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
        if (e.getPropertyName().equals(
                ViewController.AppointmentViewDialogPropertyEvent.APPOINTMENT_RECEIVED.toString())){
            initialiseViewFromED();
        }
    }
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
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
                if (AppointmentViewDialog.this.getDefaultCloseOperation()==JDialog.DO_NOTHING_ON_CLOSE){
                    if (checkOKToCloseDialog()==JOptionPane.YES_OPTION){
                        AppointmentViewDialog.this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        ActionEvent actionEvent = new ActionEvent(this,
                                ActionEvent.ACTION_PERFORMED,
                                ViewController.AppointmentViewDialogActionEvent.
                                        APPOINTMENT_VIEW_CLOSE_REQUEST.toString());
                        getMyController().actionPerformed(actionEvent);
                    }
                } 
            }
        });
    }
    private void initialiseEntityDescriptorFromView(){
        getEntityDescriptor().getRequest().setPatient(
                (EntityDescriptor.Patient)this.cmbSelectPatient.getSelectedItem());
        getEntityDescriptor().getRequest().getAppointment().getData().
                setStart(getStartDateTime());
        getEntityDescriptor().getRequest().getAppointment().getData().
                setDuration(Duration.ofMinutes(getDurationFromView()));
        getEntityDescriptor().getRequest().getAppointment().getData().
                setNotes(this.txtNotes.getText());
    }
    private LocalDateTime getStartDateTime(){
        return getEntityDescriptor().getRequest().getDay().atTime(
                (int)this.spnDurationHours.getValue(),(int)this.spnDurationMinutes.getValue());
    }
    private long getDurationFromView(){
        return ((int)this.spnDurationHours.getValue() * 60) + 
                ((int)this.spnDurationMinutes.getValue());
    }
    /**
     * On entry the local EntityDescriptor.Appointment is initialised 
     */
    private void initialiseViewFromED(){
        DateTimeFormatter hhmmFormat = DateTimeFormatter.ofPattern("HH:mm");
        this.spnStartTime.setValue(getEntityDescriptor().getAppointment().getData().getStart().format(hhmmFormat)); 
        this.spnDurationHours.setValue(getHoursFromDuration(getEntityDescriptor().getAppointment().getData().getDuration().toMinutes()));
        this.spnDurationMinutes.setValue(getMinutesFromDuration(getEntityDescriptor().getAppointment().getData().getDuration().toMinutes()));
        this.txtNotes.setText(getEntityDescriptor().getAppointment().getData().getNotes());
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
    }
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener value){
        this.myController = value;
    }
    
    /**
     * If on entry the current time is earlier than the first 'time' in the list, 
     * the first time ("08:00") is returned, if the current time is later than 
     * all the times in the list the last time in the list ("18:55") is returned;
     * otherwise the first instance of a time later than the current time is
     * returned
     * @return String value from a SpinnerListModel in which items are string
     * representations of LocalTime objects. Note the SpinnerDateModel if used
     * creates a list of times for each minute of the day, which is far too many. 
     * Hence the approach taken using the SpinnerListModel, the items of which 
     * are string representations of LocalTime objects incremented in 5 minute
     * intervals
     */
    private String getDefaultTime(){
        String result;
        LocalTime now = LocalTime.now();
        //check if current time is before the first appointment time
        if (now.compareTo(LocalTime.parse("08:00")) == -1){
            result = "08:00";
        }
        else{
            SpinnerListModel model = (SpinnerListModel)this.spnStartTime.getModel();
            model.setValue("08:00");
            result = (String)model.getNextValue();
            while(result != null) {
                LocalTime t = LocalTime.parse(result);
                if (now.compareTo(t) == -1){
                    break;
                }
                result = (String)model.getNextValue();  
            }
        }
        if (result==null){
            result = "18:55";
        }
        return result;
    }
    private String[] reverseTimes(String[] t){
        String[] timesReversed = new String[192];
        int index2 = 0;
        for (int index = times.length - 1; index > -1; index--){
            timesReversed[index2] = times[index];
            index2 = index2 + 1;
        }
        return timesReversed;
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
        spnStartTime = new javax.swing.JSpinner();
        lblDialogForAppointmentDefinitionTitle4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        spnDurationHours = new javax.swing.JSpinner(new SpinnerNumberModel(0,0,8,1));
        spnDurationMinutes = new javax.swing.JSpinner(new SpinnerNumberModel(0,0,55,5));
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbSelectPatient = new javax.swing.JComboBox<EntityDescriptor.Patient>();
        jPanel2 = new javax.swing.JPanel();
        btnCreateUpdateAppointment = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Appointment editor");

        pnlAppointmentDetails.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255)));

        lblDialogForAppointmentDefinitionTitle1.setText("Patient");

        lblDialogForAppointmentDefinitionTitle2.setText("Start time");

        lblDialogForAppointmentDefinitionTitle4.setText("Notes");

        txtNotes.setColumns(20);
        txtNotes.setRows(5);
        jScrollPane1.setViewportView(txtNotes);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255)), "Duration"));

        jLabel1.setText("hours");

        jLabel2.setText("minutes");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
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

        javax.swing.GroupLayout pnlAppointmentDetailsLayout = new javax.swing.GroupLayout(pnlAppointmentDetails);
        pnlAppointmentDetails.setLayout(pnlAppointmentDetailsLayout);
        pnlAppointmentDetailsLayout.setHorizontalGroup(
            pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                        .addComponent(lblDialogForAppointmentDefinitionTitle4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAppointmentDetailsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAppointmentDetailsLayout.createSequentialGroup()
                                .addComponent(lblDialogForAppointmentDefinitionTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(spnStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(78, 78, 78))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAppointmentDetailsLayout.createSequentialGroup()
                        .addComponent(lblDialogForAppointmentDefinitionTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
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
                    .addComponent(spnStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(lblDialogForAppointmentDefinitionTitle4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addContainerGap())
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
                .addGap(18, 18, 18)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlAppointmentDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlAppointmentDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_btnCancelActionPerformed
    private void btnCreateUpdateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateUpdateAppointmentActionPerformed
        initialiseEntityDescriptorFromView();
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle1;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle2;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle4;
    private javax.swing.JPanel pnlAppointmentDetails;
    private javax.swing.JSpinner spnDurationHours;
    private javax.swing.JSpinner spnDurationMinutes;
    private javax.swing.JSpinner spnStartTime;
    private javax.swing.JTextArea txtNotes;
    // End of variables declaration//GEN-END:variables
}
