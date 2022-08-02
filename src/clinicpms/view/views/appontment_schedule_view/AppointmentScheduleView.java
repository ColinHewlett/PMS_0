/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.appontment_schedule_view;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.view.AppointmentDateVetoPolicy;
import clinicpms.view.TableHeaderCellBorderRenderer;
import clinicpms.view.views.empty_slot_scanner_view.EmptySlotAvailability2ColumnTableModel;
import clinicpms.controller.EntityDescriptor;
import clinicpms.view.View;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingUtilities;



/**
 *
 * @author colin
 */
public class AppointmentScheduleView extends View{
    private View.Viewer myViewType = null;
    private enum COLUMN{From,Duration,ThePatient,Notes};
    private ActionListener myController = null;
    private Appointments5ColumnTableModel tableModel = null;
    private EntityDescriptor entityDescriptor = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    private DatePickerSettings settings = null;
    private ArrayList<Appointment> appointments = null;
    //private final DateTimeFormatter emptySlotStartFormat = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm (EEE)");
    private final DateTimeFormatter appointmentScheduleFormat = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy ");
    private AppointmentDateVetoPolicy vetoPolicy = null;
    /**
     * 
     * @param e PropertyChangeEvent which supports the following properties
     * --
     */ 
    @Override
    public void propertyChange(PropertyChangeEvent e){
        EntityDescriptor.AppointmentViewControllerPropertyEvent propertyName = 
                EntityDescriptor.AppointmentViewControllerPropertyEvent.valueOf(e.getPropertyName());
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
        switch (propertyName){
            case APPOINTMENTS_FOR_DAY_RECEIVED:
                initialiseViewFromEDCollection();
                break;
            case SURGERY_DAYS_ASSIGNMENT_RECEIVED:
                updateDatePickerSettings();
                break;
            case NON_SURGERY_DAY_EDIT_RECEIVED:
                temporarilySuspendDatePickerDateVetoPolicy(getEntityDescriptor().getRequest().getDay());
                break;
            case APPOINTMENT_SLOTS_FROM_DAY_RECEIVED:
                populateEmptySlotAvailabilityTable(getEntityDescriptor().getAppointments());
                /**
                 * without the next lines the appointments table is unconscious of being selected?!)
                 */
                getEntityDescriptor().getRequest().setDay(dayDatePicker.getDate());
                refreshAppointmentTableWithCurrentlySelectedDate();
                break;
            case APPOINTMENT_SCHEDULE_ERROR_RECEIVED:
                populateEmptySlotAvailabilityTable(getEntityDescriptor().getAppointments());
                break;
                
        }
    }
    
    private void updateDatePickerSettings(){
        DatePickerSettings dps = null;
        this.vetoPolicy = new AppointmentDateVetoPolicy(getEntityDescriptor().getRequest().getSurgeryDaysAssignmentValue());
        dps = dayDatePicker.getSettings();
        dps.setVetoPolicy(vetoPolicy);
    }
    
    private void temporarilySuspendDatePickerDateVetoPolicy(LocalDate day){
        DatePickerSettings dps = dayDatePicker.getSettings();
        HashMap<DayOfWeek, Boolean> allDaysSurgeryDays = new HashMap<>();
        allDaysSurgeryDays.put(DayOfWeek.MONDAY, true);
        allDaysSurgeryDays.put(DayOfWeek.TUESDAY, true);
        allDaysSurgeryDays.put(DayOfWeek.WEDNESDAY, true);
        allDaysSurgeryDays.put(DayOfWeek.THURSDAY, true);
        allDaysSurgeryDays.put(DayOfWeek.FRIDAY, true);
        allDaysSurgeryDays.put(DayOfWeek.SATURDAY, true);
        allDaysSurgeryDays.put(DayOfWeek.SUNDAY, true);
        dps.setVetoPolicy(new AppointmentDateVetoPolicy(allDaysSurgeryDays));
        dayDatePicker.setDate(day);
        refreshAppointmentTableWithCurrentlySelectedDate();
        dps.setVetoPolicy(vetoPolicy);
    }
    @Override
    public void initialiseView(){
        //following action invokes call to controller via DateChange\Listener
        this.vetoPolicy = new AppointmentDateVetoPolicy(getEntityDescriptor().getSurgeryDaysAssignment());
        DatePickerSettings dps = dayDatePicker.getSettings();
        dps.setVetoPolicy(vetoPolicy);
       
        /**
         * -- Valid date?
         * ---- yes: 
         * ------> proceed as normal
         * ---- no: 
         * ------> temporarily make all days surgery days
         * ----  
         */
        //LocalDate day = vetoPolicy.getNowDateOrClosestAvailableAfterNow();
        LocalDate day = getEntityDescriptor().getRequest().getDay();
        if (!vetoPolicy.isDateAllowed(day)){
            HashMap<DayOfWeek, Boolean> allDaysSurgeryDays = new HashMap<>();
            allDaysSurgeryDays.put(DayOfWeek.MONDAY, true);
            allDaysSurgeryDays.put(DayOfWeek.TUESDAY, true);
            allDaysSurgeryDays.put(DayOfWeek.WEDNESDAY, true);
            allDaysSurgeryDays.put(DayOfWeek.THURSDAY, true);
            allDaysSurgeryDays.put(DayOfWeek.FRIDAY, true);
            allDaysSurgeryDays.put(DayOfWeek.SATURDAY, true);
            allDaysSurgeryDays.put(DayOfWeek.SUNDAY, true);
            dps.setVetoPolicy(new AppointmentDateVetoPolicy(allDaysSurgeryDays));
            dayDatePicker.setDate(day);
            refreshAppointmentTableWithCurrentlySelectedDate();
            dps.setVetoPolicy(vetoPolicy);
        }
        dayDatePicker.setDate(day);
        refreshAppointmentTableWithCurrentlySelectedDate();
    }
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }   
    private void initialiseViewFromEDCollection(){
        appointments = getEntityDescriptor().getAppointments();
        populateAppointmentsForDayTable();
    }
    /**
     * The abstract table model includes a getElementAt(int row) method which returns the element at the row defined index of an ArrayList<EntityDescriptor.Appointment> maintained in the table model
     * @param row selected row in table
     * update 30/07/2021 19:53
     */
    private void initialiseEDRequestFromView(int row){
        getEntityDescriptor().getRequest().setAppointment(tableModel.getElementAt(row));    
    }

    /**
     * 
     * @param myViewType
     * @param controller
     * @param ed 
     */
    public AppointmentScheduleView(View.Viewer myViewType, ActionListener controller, EntityDescriptor ed) {
        this.setMyViewType(myViewType);
        this.myController = controller;
        this.setEntityDescriptor(ed);
        initComponents();
        dayDatePicker.addDateChangeListener(new DayDatePickerChangeListener());
        setEmptySlotAvailabilityTableListener();
        setAppointmentTableListener();
        mniScheduleContactList.addActionListener((ActionEvent e) -> mniScheduleContactListActionPerformed(e));
        this.mniPrintAppointmentDayList.addActionListener((ActionEvent e) -> mniPrintAppointmentDayListActionPerformed(e));
        this.mniCloseView.addActionListener((ActionEvent e) -> mniCloseViewActionPerformed(e));
        //mniSurgeryDaysEditor.addActionListener((ActionEvent e) -> mniSurgeryDaysEditorActionPerformed(e));
        this.mniSelectNonSurgeryDay.addActionListener((ActionEvent e) -> mniSelectNonSurgeryDayActionPerformed(e));
    }
    
    private void mniCloseViewActionPerformed(ActionEvent e){
        this.btnCloseViewActionPerformed(e);
    }
    private void mniPrintAppointmentDayListActionPerformed(ActionEvent e){
        //code to output schedule to connected printer
        LocalDate day = dayDatePicker.getDate();
        MessageFormat header = new MessageFormat("Appointment schedule for "  + dayDatePicker.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + "\n");
        //String header = MessageFormat.format("Appointment schedule for {0,date,short}", dayDatePicker.getDate());
        try {
            tblAppointments.print(JTable.PrintMode.FIT_WIDTH, header, null);
        } catch (java.awt.print.PrinterException ex) {
            System.err.format("Cannot print %s%n", ex.getMessage());
        }
    }
    private void mniSurgeryDaysEditorActionPerformed(ActionEvent e){
        ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    EntityDescriptor.AppointmentViewControllerActionEvent.SURGERY_DAYS_EDITOR_VIEW_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }
    
    private void mniScheduleContactListActionPerformed(ActionEvent e){
        LocalDate day = this.dayDatePicker.getDate();
        getEntityDescriptor().getRequest().setDay(day);
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.AppointmentViewControllerActionEvent.PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }
    
    private void mniSelectNonSurgeryDayActionPerformed(ActionEvent e){
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.AppointmentViewControllerActionEvent.NON_SURGERY_DAY_SCHEDULE_VIEW_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
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
    
    private void setAppointmentTableListener(){
        this.tblAppointments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel lsm = this.tblAppointments.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                /*
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    doEmptySlotAvailabilityTableRowSelection(selectedRow);
                }
                */
            }
        });
    }
    private void refreshAppointmentTableWithCurrentlySelectedDate(){
        ActionEvent actionEvent = new ActionEvent(AppointmentScheduleView.this, 
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString());
        AppointmentScheduleView.this.getMyController().actionPerformed(actionEvent);
        SwingUtilities.invokeLater(new Runnable() 
        {
          public void run()
          {
            AppointmentScheduleView.this.setTitle(AppointmentScheduleView.this.getEntityDescriptor().getRequest().getDay().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " Appointment schedule");
                    //AppointmentsForDayView.this.dayDatePicker.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " schedule");
          }
        });
    }
    
    private void doEmptySlotAvailabilityTableRowSelection(int row){
        Appointment appointment = 
                ((EmptySlotAvailability2ColumnTableModel)this.tblEmptySlotAvailability.getModel()).getElementAt(row);
        LocalDate start = appointment.getStart().toLocalDate();
        DatePickerSettings dps = dayDatePicker.getSettings();
        if (!dps.getVetoPolicy().isDateAllowed(start)){
            temporarilySuspendDatePickerDateVetoPolicy(appointment.getStart().toLocalDate());
        }
        dayDatePicker.setDate(start);   
    }
    
    private void populateAppointmentsForDayTable(){
        if (tableModel == null) tableModel = new Appointments5ColumnTableModel();
        tableModel.removeAllElements();
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()){
            tableModel.addElement(it.next());
        }
        
        this.tblAppointments.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblAppointments.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblAppointments.setDefaultRenderer(Patient.class, new AppointmentsTablePatientRenderer());
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

        TitledBorder titledBorder = (TitledBorder)this.pnlAppointmentScheduleForDay.getBorder();
        titledBorder.setTitle("Appointment schedule for " 
                + dayDatePicker.getDate().format(appointmentScheduleFormat));
        this.pnlAppointmentScheduleForDay.repaint();
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
                        AppointmentScheduleView.this,ActionEvent.ACTION_PERFORMED,
                        EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENTS_VIEW_CLOSED.toString());
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
        btnScanForEmptySlots = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnNowDay = new javax.swing.JButton();
        btnNextPracticeDay = new javax.swing.JButton();
        btnPreviousPracticeDay = new javax.swing.JButton();
        dayDatePicker = new com.github.lgooddatepicker.components.DatePicker();
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
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuOptions = new javax.swing.JMenu();
        mniScheduleContactList = new javax.swing.JMenuItem();
        mniPrintAppointmentDayList = new javax.swing.JMenuItem();
        mniCloseView = new javax.swing.JMenuItem();
        mnuSurgeryDayEdit = new javax.swing.JMenu();
        mniSurgeryDaysEditor = new javax.swing.JMenuItem();
        mniSelectNonSurgeryDay = new javax.swing.JMenuItem();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Appointment day selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        btnScanForEmptySlots.setText("scan ahead for empty slots");
        btnScanForEmptySlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScanForEmptySlotsActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setPreferredSize(new java.awt.Dimension(200, 58));

        btnNowDay.setText("now");
        btnNowDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNowDayActionPerformed(evt);
            }
        });

        btnNextPracticeDay.setText(">>");
        btnNextPracticeDay.setMinimumSize(new java.awt.Dimension(60, 23));
        btnNextPracticeDay.setPreferredSize(new java.awt.Dimension(62, 23));
        btnNextPracticeDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPracticeDayActionPerformed(evt);
            }
        });

        btnPreviousPracticeDay.setText("<<");
        btnPreviousPracticeDay.setPreferredSize(new java.awt.Dimension(93, 23));
        btnPreviousPracticeDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousPracticeDayActionPerformed(evt);
            }
        });

        settings = new DatePickerSettings();
        dayDatePicker = new com.github.lgooddatepicker.components.DatePicker(settings);
        settings.setFormatForDatesCommonEra(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        settings.setAllowEmptyDates(false);
        //settings.setVetoPolicy(new AppointmentDateVetoPolicy());
        settings.setAllowKeyboardEditing(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnNowDay, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPreviousPracticeDay, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnNextPracticeDay, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dayDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addComponent(dayDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNextPracticeDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPreviousPracticeDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNowDay))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(btnScanForEmptySlots, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnScanForEmptySlots, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Available empty slots", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(266, 146));

        jScrollPane1.setViewportView(tblEmptySlotAvailability);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlControlsLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 183, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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

        pnlAppointmentScheduleForDay.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Appointment schedule for ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(

        ));
        scrAppointmentsForDayTable.setViewportView(tblAppointments);

        javax.swing.GroupLayout pnlAppointmentScheduleForDayLayout = new javax.swing.GroupLayout(pnlAppointmentScheduleForDay);
        pnlAppointmentScheduleForDay.setLayout(pnlAppointmentScheduleForDayLayout);
        pnlAppointmentScheduleForDayLayout.setHorizontalGroup(
            pnlAppointmentScheduleForDayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentScheduleForDayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrAppointmentsForDayTable, javax.swing.GroupLayout.PREFERRED_SIZE, 692, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAppointmentScheduleForDayLayout.setVerticalGroup(
            pnlAppointmentScheduleForDayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentScheduleForDayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrAppointmentsForDayTable, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addContainerGap())
        );

        mnuOptions.setText("List");

        mniScheduleContactList.setText("List appointee contact details");
        mnuOptions.add(mniScheduleContactList);

        mniPrintAppointmentDayList.setText("Print appointments for today");
        mnuOptions.add(mniPrintAppointmentDayList);
        mnuOptions.add(new JSeparator());

        mniCloseView.setText("Close view");
        mnuOptions.add(mniCloseView);

        jMenuBar1.add(mnuOptions);

        mnuSurgeryDayEdit.setText("Edit");

        mniSurgeryDaysEditor.setText("Define which days are surgery days");
        mniSurgeryDaysEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSurgeryDaysEditorActionPerformed(evt);
            }
        });
        mnuSurgeryDayEdit.add(mniSurgeryDaysEditor);

        mniSelectNonSurgeryDay.setText("Open schedule on a non-surgery day");
        mnuSurgeryDayEdit.add(mniSelectNonSurgeryDay);

        jMenuBar1.add(mnuSurgeryDayEdit);

        setJMenuBar(jMenuBar1);

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
                .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(pnlAppointmentScheduleForDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCreateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateAppointmentActionPerformed
         ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_CREATE_VIEW_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnCreateAppointmentActionPerformed

    private void btnUpdateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateAppointmentActionPerformed
        int row = this.tblAppointments.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for update");
        }
        //30/07/2022 09:26
        else if (!((Appointments5ColumnTableModel)this.tblAppointments.getModel()).getElementAt(row).getPatient().getIsKeyDefined()){
        //else if (!getEntityDescriptor().getAppointments().get(row).getPatient().getIsKeyDefined()){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for update");
        }
        else{
            initialiseEDRequestFromView(row);
            ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_UPDATE_VIEW_REQUEST.toString());
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
        day = this.vetoPolicy.getNextAvailableDateTo(day);
        dayDatePicker.setDate(day);         
    }//GEN-LAST:event_btnNextPracticeDayActionPerformed

    private void btnPreviousPracticeDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousPracticeDayActionPerformed
        // TODO add your handling code here:
        LocalDate day = dayDatePicker.getDate();
        day = this.vetoPolicy.getPreviousAvailableDateTo(day);
        dayDatePicker.setDate(day);
    }//GEN-LAST:event_btnPreviousPracticeDayActionPerformed

    private void btnScanForEmptySlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanForEmptySlotsActionPerformed
        // TODO add your handling code here:
        LocalDate searchStartDate = dayDatePicker.getDate();
        getEntityDescriptor().getRequest().setDay(searchStartDate);
        ActionEvent actionEvent = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED,
                    EntityDescriptor.AppointmentViewControllerActionEvent.EMPTY_SLOT_SCANNER_DIALOG_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnScanForEmptySlotsActionPerformed

    private void btnCancelSelectedAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelSelectedAppointmentActionPerformed
        //DateTimeFormatter format24Hour = DateTimeFormatter.ofPattern("HH:mm");
        String name = null;
        LocalDateTime start = null;
        LocalTime from = null;
        Long duration;
        int row = this.tblAppointments.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for cancellation");
        }
        //30/07/2022 09:26
        else if (!((Appointments5ColumnTableModel)this.tblAppointments.getModel()).getElementAt(row).getPatient().getIsKeyDefined()){
        //else if (!getEntityDescriptor().getAppointments().get(row).getPatient().getIsKeyDefined()){
            JOptionPane.showMessageDialog(this, "An appointment has not been selected for cancellation");
        }
        else{
            int OKToCancelAppointment;
            initialiseEDRequestFromView(row);
            //20/07/2022 08:16 update
            //start = getEntityDescriptor().getRequest().getAppointment().getData().getStart();
            start = getEntityDescriptor().getRequest().getAppointment().getStart();
            from = start.toLocalTime();
            //20/07/2022 08:16 update
            //duration = getEntityDescriptor().getRequest().getAppointment().getData().getDuration().toMinutes();
            duration = getEntityDescriptor().getRequest().getAppointment().getDuration().toMinutes();
            LocalTime to = from.plusMinutes(duration);
            /**
             * 09/11/2021 16:53 [1.f]log notes -> update below because getRequest().getPatient() no longer initialised 
             * with appointee details; instead request.appointment.appointee.data is
             */
            //20/07/2022 08:16 update
            //name = getEntityDescriptor().getRequest().getAppointment().getAppointee().getData().getForenames();
            name = getEntityDescriptor().getRequest().getAppointment().getPatient().getName().getForenames();
            //name = getEntityDescriptor().getRequest().getPatient().getData().getForenames();
            if (!name.isEmpty())name = name + " ";
            //20/07/2022 08:16 update
            //name = name + getEntityDescriptor().getRequest().getAppointment().getAppointee().getData().getSurname();
            name = name + getEntityDescriptor().getRequest().getAppointment().getPatient().getName().getSurname();
            from.format(DateTimeFormatter.ofPattern("HH:mm"));
            String[] options = {"Yes", "No"};
            OKToCancelAppointment = JOptionPane.showOptionDialog(this,
                            "Are you sure you want to cancel the appointment for patient "
                                    + name + " from " + from.format(DateTimeFormatter.ofPattern("HH:mm")) 
                                    + " to " + to.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    + ".",null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            null);
            if (OKToCancelAppointment==JOptionPane.YES_OPTION){
                ActionEvent actionEvent = new ActionEvent(this, 
                        ActionEvent.ACTION_PERFORMED,
                        EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_CANCEL_REQUEST.toString());
                this.getMyController().actionPerformed(actionEvent);
            }
        }
    }//GEN-LAST:event_btnCancelSelectedAppointmentActionPerformed

    private void btnNowDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNowDayActionPerformed
        // TODO add your handling code here:
        LocalDate day = this.vetoPolicy.getNowDateOrClosestAvailableAfterNow();
        dayDatePicker.setDate(day);
    }//GEN-LAST:event_btnNowDayActionPerformed
/*
    private void mniSurgeryDaysEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSurgeryDaysEditorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mniSurgeryDaysEditorActionPerformed
*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelSelectedAppointment;
    private javax.swing.JButton btnCloseView;
    private javax.swing.JButton btnCreateAppointment;
    private javax.swing.JButton btnNextPracticeDay;
    private javax.swing.JButton btnNowDay;
    private javax.swing.JButton btnPreviousPracticeDay;
    private javax.swing.JButton btnScanForEmptySlots;
    private javax.swing.JButton btnUpdateAppointment;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.github.lgooddatepicker.components.DatePicker dayDatePicker;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem mniCloseView;
    private javax.swing.JMenuItem mniPrintAppointmentDayList;
    private javax.swing.JMenuItem mniScheduleContactList;
    private javax.swing.JMenuItem mniSelectNonSurgeryDay;
    private javax.swing.JMenuItem mniSurgeryDaysEditor;
    private javax.swing.JMenu mnuOptions;
    private javax.swing.JMenu mnuSurgeryDayEdit;
    private javax.swing.JPanel pnlAppointmentScheduleForDay;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JScrollPane scrAppointmentsForDayTable;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTable tblEmptySlotAvailability;
    // End of variables declaration//GEN-END:variables

    class DayDatePickerChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            //LocalDate date = event.getNewDate();
            getEntityDescriptor().getRequest().setDay(AppointmentScheduleView.this.dayDatePicker.getDate());
            ActionEvent actionEvent = new ActionEvent(AppointmentScheduleView.this, 
                    ActionEvent.ACTION_PERFORMED,
                    EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString());
            AppointmentScheduleView.this.getMyController().actionPerformed(actionEvent);
            SwingUtilities.invokeLater(new Runnable() 
            {
              public void run()
              {
                AppointmentScheduleView.this.setTitle(AppointmentScheduleView.this.dayDatePicker.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " schedule");
              }
            });
        }
    }
    
   

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }
    
    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
    
    private void populateEmptySlotAvailabilityTable(ArrayList<Appointment> a){
        //ref 21/07/2022 08:39 update
        if (a==null) a = new ArrayList<Appointment>();
        EmptySlotAvailability2ColumnTableModel model;
        if (this.tblEmptySlotAvailability!=null){
            jScrollPane1.remove(this.tblEmptySlotAvailability);   
        }
        this.tblEmptySlotAvailability = new JTable(new EmptySlotAvailability2ColumnTableModel());
        jScrollPane1.setViewportView(this.tblEmptySlotAvailability);
        setEmptySlotAvailabilityTableListener();
        model = (EmptySlotAvailability2ColumnTableModel)this.tblEmptySlotAvailability.getModel();
        model.removeAllElements();
        Iterator<Appointment> it = a.iterator();
        while (it.hasNext()){
            ((EmptySlotAvailability2ColumnTableModel)this.tblEmptySlotAvailability.getModel()).addElement(it.next());
        }

        JTableHeader tableHeader = this.tblEmptySlotAvailability.getTableHeader();
        tableHeader.setBackground(new Color(220,220,220));
        tableHeader.setOpaque(true);
        
        TableColumnModel columnModel = this.tblEmptySlotAvailability.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        columnModel.getColumn(1).setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
    }
}
