/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.patient_notification_editor_view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.model.Patient;
import clinicpms.model.PatientNotification;
import clinicpms.view.TableHeaderCellBorderRenderer;
import clinicpms.view.View;
//import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
//import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
//import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Color;
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
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;


/**
 *
 * @author colin
 */
public class PatientNotificationEditorModalViewer extends View {
    private View.Viewer myViewType = null;
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    private ViewController.ViewMode viewMode = null;
    
    private void populatePatientNotificationHistoryTable(ArrayList<PatientNotification> patientNotifications){
        PatientNotificationView2ColumnTableModel model = 
                (PatientNotificationView2ColumnTableModel)this.tblPatientNotificationHistory.getModel();
        model.removeAllElements();
//model.fireTableDataChanged();
        Iterator<PatientNotification> it = patientNotifications.iterator();
        while (it.hasNext()){
            ((PatientNotificationView2ColumnTableModel)this.tblPatientNotificationHistory.getModel()).addElement(it.next());
        }
        this.tblPatientNotificationHistory.setDefaultRenderer(LocalDate.class, new PatientNotificationEditorTableLocalDateRenderer());
    }
    
    /**
     * method initialises the patient notification history table with the values contained in the entity descriptor
     */
    private void doReceivedPatientNotifications(){
        ArrayList<PatientNotification> patientNotifications =
                getEntityDescriptor().getPatientNotifications();
        populatePatientNotificationHistoryTable(patientNotifications);
    }
    
    /**
     * On entry method assumes the patient selector has been initialised
     * -- the received patient notification is used to initialise the ui
     * 
     */
    private void doReceivedPatientNotification(){
        PatientNotification patientNotification = getEntityDescriptor().getPatientNotification();
        if (patientNotification==null) {
            this.cmbSelectPatient.setSelectedIndex(-1);
            this.rdbNotificationUnactioned.setSelected(false);
            setViewMode(ViewController.ViewMode.Create);
        }
        else {
            setViewMode(ViewController.ViewMode.Update);
            cmbSelectPatient.setSelectedItem(patientNotification.getPatient());
            dpNotificationDate.setDate(patientNotification.getNotificationDate());
            txaNotificationText.setText(patientNotification.getNotificationText());
            if (patientNotification.getIsActioned())
                this.rdbNotificationActioned.setSelected(true);
            else this.rdbNotificationUnactioned.setSelected(true);
            this.cmbSelectPatient.setEditable(false);
            this.btnCreateUpdatePatientNotification.setText(ViewController.ViewMode.Update.toString());
        }
    }
    
    private void populatePatientSelector(JComboBox<Patient> selector){
        DefaultComboBoxModel<Patient> model = 
                new DefaultComboBoxModel<>();
        ArrayList<Patient> patients = 
                getEntityDescriptor().getPatients();
        Iterator<Patient> it = patients.iterator();
        while (it.hasNext()){
            Patient patient = it.next();
            model.addElement(patient);
        }
        selector.setModel(model);
        selector.setSelectedIndex(-1);
        
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
    
     /**
     * On entry the local EntityDescriptor.Appointment is initialised 
     */
    private void initialiseViewFromED(){
        
    }
    
    private void centreViewOnDesktop(Container desktopView, JInternalFrame view){
        Insets insets = desktopView.getInsets();
        Dimension deskTopViewDimension = desktopView.getSize();
        Dimension myViewDimension = view.getSize();
        view.setLocation(new Point(
                (int)(deskTopViewDimension.getWidth() - (myViewDimension.getWidth()))/2,
                (int)((deskTopViewDimension.getHeight()-insets.top) - myViewDimension.getHeight())/2));
    }

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }
    
    /**
     * Creates new form PatientNotificationEditorModalViewer
     */
    public PatientNotificationEditorModalViewer(View.Viewer myViewType, ActionListener myController,
            EntityDescriptor entityDescriptor, 
            Component parent) {//ViewMode arg
        //initialiseDialogClosing();
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        setMyViewType(myViewType);
        initComponents();
        ViewController.setJTableColumnProperties(
                tblPatientNotificationHistory, jScrollPane2.getPreferredSize().width, 20,80);
        initialiseViewMode();
        // Try to find a JDesktopPane.
        JLayeredPane toUse = JOptionPane.getDesktopPaneForComponent(parent);
        // If we don't have a JDesktopPane, we try to find a JLayeredPane.
        if (toUse == null)  toUse = JLayeredPane.getLayeredPaneAbove(parent);
        // If this still fails, we throw a RuntimeException.
        if (toUse == null) throw new RuntimeException   ("parentComponent does not have a valid parent");
        this.setClosable(true);
        JDesktopPane x = (JDesktopPane)toUse;
        toUse.add(this);
        this.setLayer(JLayeredPane.MODAL_LAYER);
        centreViewOnDesktop(x.getParent(),this);
        //this.initialiseView();
        //this.setVisible(true);
        
        
        ActionEvent actionEvent = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            EntityDescriptor.PatientViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
        this.getMyController().actionPerformed(actionEvent);
        
        startModal(this);
    }
    
    
    
    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
    
    @Override
    public void addInternalFrameClosingListener(){
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
        EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent propertyName =
                EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent.valueOf(e.getPropertyName());
        switch (propertyName){
            case RECEIVED_PATIENT_NOTIFICATION:
                doReceivedPatientNotification();
                break;
            case RECEIVED_PATIENT_NOTIFICATIONS:
                doReceivedPatientNotifications();
                break;
        }
    }
    
    @Override
    public void initialiseView(){
        /**
         * initialise ui
         */
        setVisible(true);
        populatePatientSelector(this.cmbSelectPatient);
        doReceivedPatientNotification(); 
        doReceivedPatientNotifications();
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener value){
        this.myController = value;
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
        switch (viewMode){
            case Create:
                btnCreateUpdatePatientNotification.
                        setText(ViewController.ViewMode.Create.toString());
                this.pnlRadioButtons.setVisible(false);
                break;
            case Update:
                btnCreateUpdatePatientNotification.
                        setText(ViewController.ViewMode.Update.toString());        
        }
    }
    private void initialiseViewMode(){
        
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
        cmbSelectPatient = new javax.swing.JComboBox<Patient>();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dpNotificationDate = new com.github.lgooddatepicker.components.DatePicker();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaNotificationText = new javax.swing.JTextArea();
        pnlRadioButtons = new javax.swing.JPanel();
        rdbNotificationUnactioned = new javax.swing.JRadioButton();
        rdbNotificationActioned = new javax.swing.JRadioButton();
        btnCreateUpdatePatientNotification = new javax.swing.JButton();
        btnCloseView = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblPatientNotificationHistory = new javax.swing.JTable();
        btnDeletePatientNotification = new javax.swing.JButton();

        buttonGroup1.add(rdbNotificationUnactioned);
        buttonGroup1.add(rdbNotificationActioned);

        setTitle("Patient notification editor");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select patient", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        cmbSelectPatient.setModel(new javax.swing.DefaultComboBoxModel<Patient>());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(137, 137, 137))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 24, Short.MAX_VALUE))
        );

        cmbSelectPatient.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Notification details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel2.setText("Notification date");

        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        settings.setAllowKeyboardEditing(false);
        dpNotificationDate.setSettings(settings);

        jLabel1.setText("Notification text");

        txaNotificationText.setColumns(20);
        txaNotificationText.setRows(5);
        txaNotificationText.setPreferredSize(new java.awt.Dimension(166, 74));
        jScrollPane1.setViewportView(txaNotificationText);

        pnlRadioButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "patient has been notified", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        rdbNotificationUnactioned.setText("no");
        rdbNotificationUnactioned.setSelected(true);

        rdbNotificationActioned.setText("yes");

        javax.swing.GroupLayout pnlRadioButtonsLayout = new javax.swing.GroupLayout(pnlRadioButtons);
        pnlRadioButtons.setLayout(pnlRadioButtonsLayout);
        pnlRadioButtonsLayout.setHorizontalGroup(
            pnlRadioButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRadioButtonsLayout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(rdbNotificationUnactioned)
                .addGap(65, 65, 65)
                .addComponent(rdbNotificationActioned)
                .addGap(79, 79, 79))
        );
        pnlRadioButtonsLayout.setVerticalGroup(
            pnlRadioButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRadioButtonsLayout.createSequentialGroup()
                .addGroup(pnlRadioButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbNotificationUnactioned)
                    .addComponent(rdbNotificationActioned))
                .addGap(0, 4, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlRadioButtons, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dpNotificationDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dpNotificationDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(pnlRadioButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        btnCreateUpdatePatientNotification.setText("Create/Update");
        btnCreateUpdatePatientNotification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateUpdatePatientNotificationActionPerformed(evt);
            }
        });

        btnCloseView.setText("Close view");
        btnCloseView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseViewActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Notification history", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        tblPatientNotificationHistory.setModel(new PatientNotificationView2ColumnTableModel()
        );
        TableColumnModel columnModel = tblPatientNotificationHistory.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(10);
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        jScrollPane2.setViewportView(tblPatientNotificationHistory);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        btnDeletePatientNotification.setText("Delete");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(btnCreateUpdatePatientNotification)
                        .addGap(31, 31, 31)
                        .addComponent(btnDeletePatientNotification)
                        .addGap(31, 31, 31)
                        .addComponent(btnCloseView)))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCloseView)
                    .addComponent(btnCreateUpdatePatientNotification)
                    .addComponent(btnDeletePatientNotification))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCreateUpdatePatientNotificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateUpdatePatientNotificationActionPerformed
        if (getViewMode().equals(ViewController.ViewMode.Create))
            doRequestNewPatientNotification();
        else doRequestUpdatePatientNotification();
    }//GEN-LAST:event_btnCreateUpdatePatientNotificationActionPerformed

    private void btnCloseViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseViewActionPerformed
        doViewCloseAction();
    }//GEN-LAST:event_btnCloseViewActionPerformed

    private void doViewCloseAction(){
        String message = "Are you sure you want to close the notification editor window?";
        int response = JOptionPane.showConfirmDialog(
                this, message, "Patient notification editor", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.PatientNotificationViewControllerActionEvent.MODAL_VIEWER_DEACTIVATED.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }
    
    private void doRequestNewPatientNotification(){
        if (doValidatePatientNotificationRequest()){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.PatientNotificationViewControllerActionEvent.
                        PATIENT_NOTIFICATION_EDITOR_CREATE_NOTIFICATION_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }       
    }
    
    private void doRequestUpdatePatientNotification(){
        if (doValidatePatientNotificationRequest()){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.PatientNotificationViewControllerActionEvent.
                        PATIENT_NOTIFICATION_EDITOR_UPDATE_NOTIFICATION_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }
    
    private boolean doValidatePatientNotificationRequest(){
        boolean result = true;
        if (this.cmbSelectPatient.getSelectedItem()==null){
            result = false;
            JOptionPane.showMessageDialog(
                    this, "A patient has not been selected", 
                    "Patient notification editor error",
                    JOptionPane.WARNING_MESSAGE);
        }
        if (result){
            if (this.dpNotificationDate.getDate()==null){
                result = false;
                JOptionPane.showMessageDialog(
                    this, "A valid notificaion date has not been defined", 
                    "Patient notification editor error",
                    JOptionPane.WARNING_MESSAGE);
            }    
        }
        if (result){
            if (this.txaNotificationText.getText().isEmpty()){
                result = false;
                JOptionPane.showMessageDialog(
                    this, "No notificaion text has not been defined", 
                    "Patient notification editor error",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        if (result){
            PatientNotification patientNotification;
            if (getViewMode().equals(ViewController.ViewMode.Create))
                patientNotification = new PatientNotification();
            else
                patientNotification = getEntityDescriptor().getPatientNotification();
            patientNotification.setPatient((Patient)this.cmbSelectPatient.getSelectedItem());
            patientNotification.setNotificationDate(this.dpNotificationDate.getDate());
            patientNotification.setNotificationText(this.txaNotificationText.getText());
            patientNotification.setIsActioned(rdbNotificationActioned.isSelected());
            getEntityDescriptor().getRequest().setPatientNotification(patientNotification);
        }
        else getEntityDescriptor().getRequest().setPatientNotification(null);
        return result;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCloseView;
    private javax.swing.JButton btnCreateUpdatePatientNotification;
    private javax.swing.JButton btnDeletePatientNotification;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<Patient> cmbSelectPatient;
    private com.github.lgooddatepicker.components.DatePicker dpNotificationDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlRadioButtons;
    private javax.swing.JRadioButton rdbNotificationActioned;
    private javax.swing.JRadioButton rdbNotificationUnactioned;
    private javax.swing.JTable tblPatientNotificationHistory;
    private javax.swing.JTextArea txaNotificationText;
    // End of variables declaration//GEN-END:variables
}
