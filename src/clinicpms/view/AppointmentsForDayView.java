/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import clinicpms.controller.AppointmentViewController;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.controller.ViewController.AppointmentViewControllerActionEvent;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;



/**
 *
 * @author colin
 */
public class AppointmentsForDayView extends View{
    private enum COLUMN{From,Duration,Patient,Notes};
    private JTable tblAppointmentsForDay = null;
    private ActionListener myController = null;
    private Appointments5ColumnTableModel tableModel = null;
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    private DatePickerSettings settings = null;
    private ArrayList<EntityDescriptor.Appointment> appointments = null;
    private DateTimeFormatter emptySlotStartFormat = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm (EEE)");
    private DateTimeFormatter appointmentScheduleFormat = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy ");
    /**
     * 
     * @param e PropertyChangeEvent which supports the following properties
     * --
     */ 
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        if (propertyName.equals(ViewController.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            initialiseViewFromEDCollection();
            /*
            
            */
            
        }
        else if (propertyName.equals(ViewController.AppointmentViewControllerPropertyEvent.APPOINTMENT_SLOTS_FROM_DAY_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            populateEmptySlotAvailabilityTable(getEntityDescriptor().getAppointments());
            refreshAppointmentTableWithCurrentlySelectedDate();
            //initialiseViewFromEDCollection();
            //APPOINTMENT_VIEW_ERROR
        } 
        else if (propertyName.equals(ViewController.AppointmentViewDialogPropertyEvent.APPOINTMENT_VIEW_ERROR.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            populateEmptySlotAvailabilityTable(getEntityDescriptor().getAppointments());
        }
    }
    public void initialiseView(){
        //following action invokes call to controller via DateChange\Listener
        LocalDate day = getEntityDescriptor().getRequest().getDay(); 
        while (!new AppointmentDateVetoPolicy().isDateAllowed(day)){
            day = day.plusDays(1);
        }
        dayDatePicker.setDate(day);
        /**
         * unsure why the following code is necessary to initialise tblAppointments
         * on entry to the view
         
        getEntityDescriptor().getRequest().setDay(AppointmentsForDayView.this.dayDatePicker.getDate());
        ActionEvent actionEvent = new ActionEvent(AppointmentsForDayView.this, 
                ActionEvent.ACTION_PERFORMED,
                AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString());
        AppointmentsForDayView.this.getMyController().actionPerformed(actionEvent);  
        */ 
    }
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }   
    private void initialiseViewFromEDCollection(){
        appointments = getEntityDescriptor().getAppointments().getData();
        populateAppointmentsForDayTable();
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
        dayDatePicker.addDateChangeListener(new DayDatePickerChangeListener());
        setEmptySlotAvailabilityTableListener();
        this.tblAppointments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //setTableHeaderCellBorderRendering();
        
    }
    private void setEmptySlotAvailabilityTableListener(){
        this.tblEmptySlotAvailability.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel lsm = this.tblEmptySlotAvailability.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    doEmptySlotAvailabilityTableRowSelection(selectedRow);
                }
            }
        });
    }
    private void refreshAppointmentTableWithCurrentlySelectedDate(){
        ActionEvent actionEvent = new ActionEvent(AppointmentsForDayView.this, 
                ActionEvent.ACTION_PERFORMED,
                AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString());
        AppointmentsForDayView.this.getMyController().actionPerformed(actionEvent);
        SwingUtilities.invokeLater(new Runnable() 
        {
          public void run()
          {
            AppointmentsForDayView.this.setTitle(
                    AppointmentsForDayView.this.dayDatePicker.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " schedule");
          }
        });
    }
    private void doEmptySlotAvailabilityTableRowSelection(int row){
        String appointmentDate = (String)this.tblEmptySlotAvailability.getModel().getValueAt(row, 0);
        LocalDate start = LocalDateTime.parse(appointmentDate, emptySlotStartFormat).toLocalDate();
        dayDatePicker.setDate(start);
    }
    private void populateAppointmentsForDayTable(){
        Appointments5ColumnTableModel.appointments = appointments;
        int appointmentsCount = appointments.size();
        
        tableModel = new Appointments5ColumnTableModel();
        this.tblAppointments.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblAppointments.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblAppointments.setDefaultRenderer(EntityDescriptor.Patient.class, new AppointmentsTablePatientRenderer());
        this.tblAppointments.setModel(tableModel);
        this.tblAppointments.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        /**
         * configure table header & column widths
         */
        TableColumnModel columnModel = this.tblAppointments.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(190);
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setPreferredWidth(60);
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(2).setPreferredWidth(60);
        columnModel.getColumn(2).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(3).setPreferredWidth(105);
        columnModel.getColumn(3).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(4).setMinWidth(300);
        columnModel.getColumn(4).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        JTableHeader tableHeader = this.tblAppointments.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        this.tblAppointments.setRowSelectionAllowed(true);
        
        //this.tblAppointments.repaint();
        //this.tblAppointments.setRowSelectionInterval(0, 4);
        /*
        DefaultTableCellRenderer renderer = 
                (DefaultTableCellRenderer) tblAppointments.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(0);
        */
        TitledBorder titledBorder = titledBorder = (TitledBorder)this.pnlAppointmentScheduleForDay.getBorder();
        titledBorder.setTitle("Appointment schedule for " 
                + dayDatePicker.getDate().format(appointmentScheduleFormat));
        this.pnlAppointmentScheduleForDay.repaint();
    } 
    private void setTableHeaderCellBorderRendering(){
        JTableHeader tableHeader = this.tblAppointments.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        
        tableHeader = this.tblEmptySlotAvailability.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        
        TableColumnModel columnModel = this.tblAppointments.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(2).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(3).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(4).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        
        columnModel = this.tblEmptySlotAvailability.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));  
    }
    private ActionListener getMyController(){
        return myController;
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlControls = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        dayDatePicker = new com.github.lgooddatepicker.components.DatePicker();
        btnPreviousPracticeDay = new javax.swing.JButton();
        btnNextPracticeDay = new javax.swing.JButton();
        btnScanForEmptySlots = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblEmptySlotAvailability = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btnCreateAppointment = new javax.swing.JButton();
        btnUpdateAppointment = new javax.swing.JButton();
        btnCloseView = new javax.swing.JButton();
        btnCancelSelectedAppointment = new javax.swing.JButton();
        pnlAppointmentScheduleForDay = new javax.swing.JPanel();
        scrAppointmentsForDayTable = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Appointment day selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        settings = new DatePickerSettings();
        dayDatePicker = new com.github.lgooddatepicker.components.DatePicker(settings);
        settings.setFormatForDatesCommonEra(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        settings.setAllowEmptyDates(false);
        settings.setVetoPolicy(new AppointmentDateVetoPolicy());
        settings.setAllowKeyboardEditing(false);

        btnPreviousPracticeDay.setText("view previous practice day");
        btnPreviousPracticeDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousPracticeDayActionPerformed(evt);
            }
        });

        btnNextPracticeDay.setText("view next practice day");
        btnNextPracticeDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPracticeDayActionPerformed(evt);
            }
        });

        btnScanForEmptySlots.setText("Scan for empty slots");
        btnScanForEmptySlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScanForEmptySlotsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnScanForEmptySlots, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPreviousPracticeDay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addComponent(btnNextPracticeDay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dayDatePicker, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dayDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNextPracticeDay)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPreviousPracticeDay)
                .addGap(11, 11, 11)
                .addComponent(btnScanForEmptySlots)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Available empty slots", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(266, 146));

        jScrollPane1.setViewportView(tblEmptySlotAvailability);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlControlsLayout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)))
        );

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

        btnCancelSelectedAppointment.setText("Cancel selected appointment");
        btnCancelSelectedAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelSelectedAppointmentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(btnCreateAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(btnUpdateAppointment)
                .addGap(32, 32, 32)
                .addComponent(btnCancelSelectedAppointment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(32, 32, 32)
                .addComponent(btnCloseView)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateAppointment)
                    .addComponent(btnUpdateAppointment)
                    .addComponent(btnCloseView)
                    .addComponent(btnCancelSelectedAppointment))
                .addContainerGap())
        );

        pnlAppointmentScheduleForDay.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true), "Appointment schedule for", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(

        ));
        scrAppointmentsForDayTable.setViewportView(tblAppointments);

        javax.swing.GroupLayout pnlAppointmentScheduleForDayLayout = new javax.swing.GroupLayout(pnlAppointmentScheduleForDay);
        pnlAppointmentScheduleForDay.setLayout(pnlAppointmentScheduleForDayLayout);
        pnlAppointmentScheduleForDayLayout.setHorizontalGroup(
            pnlAppointmentScheduleForDayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentScheduleForDayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrAppointmentsForDayTable, javax.swing.GroupLayout.PREFERRED_SIZE, 690, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAppointmentScheduleForDayLayout.setVerticalGroup(
            pnlAppointmentScheduleForDayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentScheduleForDayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrAppointmentsForDayTable, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlAppointmentScheduleForDay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlControls, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(pnlAppointmentScheduleForDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
        int row = this.tblAppointments.getSelectedRow();
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

    private void btnNextPracticeDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPracticeDayActionPerformed
        LocalDate day = dayDatePicker.getDate();
        do{
            day = day.plusDays(1);
        }while(!new AppointmentDateVetoPolicy().isDateAllowed(day));
        dayDatePicker.setDate(day);
    }//GEN-LAST:event_btnNextPracticeDayActionPerformed

    private void btnPreviousPracticeDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousPracticeDayActionPerformed
        // TODO add your handling code here:
        LocalDate day = dayDatePicker.getDate();
        do{
            day = day.minusDays(1);
        }while(!new AppointmentDateVetoPolicy().isDateAllowed(day));
        dayDatePicker.setDate(day);
    }//GEN-LAST:event_btnPreviousPracticeDayActionPerformed

    private void btnScanForEmptySlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanForEmptySlotsActionPerformed
        // TODO add your handling code here:
        LocalDate searchStartDate = dayDatePicker.getDate();
        getEntityDescriptor().getRequest().setDay(searchStartDate);
        ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    AppointmentViewControllerActionEvent.EMPTY_SLOT_SCANNER_DIALOG_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnScanForEmptySlotsActionPerformed

    private void btnCancelSelectedAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelSelectedAppointmentActionPerformed
        int row = this.tblAppointments.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for cancellation");
        }
        else if (getEntityDescriptor().getAppointments().getData().get(row).getData().IsEmptySlot()){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for update");
        }
        else{
            initialiseEDSelectionFromView(row);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    AppointmentViewControllerActionEvent.APPOINTMENT_CANCEL_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }//GEN-LAST:event_btnCancelSelectedAppointmentActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelSelectedAppointment;
    private javax.swing.JButton btnCloseView;
    private javax.swing.JButton btnCreateAppointment;
    private javax.swing.JButton btnNextPracticeDay;
    private javax.swing.JButton btnPreviousPracticeDay;
    private javax.swing.JButton btnScanForEmptySlots;
    private javax.swing.JButton btnUpdateAppointment;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.github.lgooddatepicker.components.DatePicker dayDatePicker;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlAppointmentScheduleForDay;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JScrollPane scrAppointmentsForDayTable;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTable tblEmptySlotAvailability;
    // End of variables declaration//GEN-END:variables

    class DayDatePickerChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            getEntityDescriptor().getRequest().setDay(AppointmentsForDayView.this.dayDatePicker.getDate());
            ActionEvent actionEvent = new ActionEvent(AppointmentsForDayView.this, 
                    ActionEvent.ACTION_PERFORMED,
                    AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString());
            AppointmentsForDayView.this.getMyController().actionPerformed(actionEvent);
            SwingUtilities.invokeLater(new Runnable() 
            {
              public void run()
              {
                AppointmentsForDayView.this.setTitle(
                        AppointmentsForDayView.this.dayDatePicker.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " schedule");
              }
            });
        }
    }
    
    private void populateEmptySlotAvailabilityTable(EntityDescriptor.Appointments a){
        EmptySlotAvailability2ColumnTableModel model;
        if (this.tblEmptySlotAvailability!=null){
            jScrollPane1.remove(this.tblEmptySlotAvailability);   
        }
        this.tblEmptySlotAvailability = new JTable(new EmptySlotAvailability2ColumnTableModel());
        jScrollPane1.setViewportView(this.tblEmptySlotAvailability);
        setEmptySlotAvailabilityTableListener();
        model = (EmptySlotAvailability2ColumnTableModel)this.tblEmptySlotAvailability.getModel();
        model.removeAllElements();
//model.fireTableDataChanged();
        Iterator<EntityDescriptor.Appointment> it = a.getData().iterator();
        while (it.hasNext()){
            ((EmptySlotAvailability2ColumnTableModel)this.tblEmptySlotAvailability.getModel()).addElement(it.next());
        }
        //model.fireTableDataChanged();
        JTableHeader tableHeader = this.tblEmptySlotAvailability.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        //tableHeader.setBackground(new Color(0,0,0));
        tableHeader.setOpaque(true);
        
        TableColumnModel columnModel = this.tblEmptySlotAvailability.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        //this.tblEmptySlotAvailability.repaint();
    }
    private void populateEmptySlotAvailabilityTablex(EntityDescriptor.Appointments a){
        
        EmptySlotAvailability2ColumnTableModel.emptySlots = a.getData();
        //this.tblEmptySlotAvailability = new JTable(new EmptySlotAvailability2ColumnTableModel() );
        EmptySlotAvailability2ColumnTableModel.emptySlots = a.getData();
        this.tblEmptySlotAvailability.setModel(new EmptySlotAvailability2ColumnTableModel());
        
        JTableHeader tableHeader = this.tblEmptySlotAvailability.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        
        TableColumnModel columnModel = this.tblEmptySlotAvailability.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
/*
        if (a.getData().isEmpty()){
            EmptySlotAvailability2ColumnTableModel model = (EmptySlotAvailability2ColumnTableModel)tblEmptySlotAvailability.getModel(); 
            int count = model.getRowCount();
            ((DefaultTableModel)tblEmptySlotAvailability.getModel()).setRowCount(0);
        }
*/
        //this.tblEmptySlotAvailability.repaint();
    }

}
