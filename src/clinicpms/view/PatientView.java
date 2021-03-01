/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.RenderedPatient;
import clinicpms.controller.ViewController;
import clinicpms.controller.ViewController.PatientField;
import clinicpms.controller.ViewController.PatientViewControllerActionEvent;
import clinicpms.controller.ViewController.PatientViewControllerPropertyEvent;
import clinicpms.view.exceptions.CrossCheckErrorException;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.awt.Color;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Iterator;
//import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.border.TitledBorder;
//import javax.swing.event.InternalFrameListener;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


/**
 * 
 * -- The view receives an image of the patient details in the received
 * EntityDescriptor.Patient, which also encapsulates a patient's guardian (if 
 * exists) and appointment history
 * -- The view sends an updated image of the patient in 
 * EntityDescriptor.Selection.Patient 
 * -- 
 * -- The view receives a collection of all patients on the system in the
 * received EntityDescriptor.Collection.Patients
 * @author colin
 */
public class PatientView extends View
                                    {
    private enum BorderTitles { Appointment_history,
                                Contact_details,
                                Guardian_details,
                                Recall_details,
                                Notes}
    private enum TitleItem {Mr,
                            Miss,
                            Mrs,
                            Ms,
                            Dr}
    private enum GenderItem {Male,
                             Female,
                             Trans}
    private enum YesNoItem {No,
                            Yes}
    private enum ViewMode {CREATE,
                           UPDATE}
    private enum Category{DENTAL, HYGIENE}
    private ViewMode viewMode = ViewMode.CREATE;
    private static final String CREATE_BUTTON = "Create new patient";
    private static final String UPDATE_BUTTON = "Update patient";

    //state variable which support the IView interface
    DateTimeFormatter dmyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dmyhhmmFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");
    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("MMM/yy");
    DefaultTableModel appointmentHistoryModel = new DefaultTableModel();
    
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    //private JTable tblAppointmentsForDay = null;
    private AppointmentsSingleColumnTableModel tableModel = null;
    private InternalFrameAdapter internalFrameAdapter = null;

    /**
     * 
     * @param myController ActionListener
     * @param ed EntityDescriptor
     */
    public PatientView(ActionListener myController, EntityDescriptor ed) {
        initComponents();
        /**
         * initialise appointment history table look and feel
         */
        
        /**
         * Establish an InternalFrameListener for when the view is closed 
         */
        new InternalFrameAdapter(){
            @Override  
            public void internalFrameClosed(InternalFrameEvent e) {
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        /**
         * Determines action when the window "X" is clicked, which will fire an
         * InternalFrameEvent.INTERNAL_FRAME_CLOSED event for the above
         * listener to let the view controller know what's happening
         */
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        //initialise the spinner controls
        this.spnDentalRecallFrequency.setModel(new SpinnerNumberModel(6,0,12,3));
        //this.spnHygieneRecallFrequency.setModel(new SpinnerNumberModel(6,0,12,3));
        setMyController(myController);
        setEntityDescriptor(ed);
        populatePatientSelector(this.cmbSelectPatient); //populate list of patients to select from
        this.cmbSelectPatient.addActionListener((ActionEvent e) -> cmbSelectPatientActionPerformed());
        populatePatientSelector(this.cmbSelectGuardian); //populate list of patients to select from

        initialiseViewMode(ViewMode.CREATE);//view start off in CREATE mode

        dobPicker = new DatePicker();
        dobPicker.addDateChangeListener(new DOBDateChangeListener());
        dentalRecallPicker = new DatePicker();
        dentalRecallPicker.addDateChangeListener(new DentalRecallDateChangeListener());
        
        pnlContactDetails.add(dobPicker);
        pnlRecallDetails.add(dentalRecallPicker);
        
        txtDOB.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlContactDetails.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                dobPicker.openPopup();
            }
            
        });
        
        txtDentalRecallDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlRecallDetails.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                dentalRecallPicker.openPopup();
            }
        });
    }
    
    public void addInternalFrameClosingListener(){
        /**
         * Establish an InternalFrameListener for when the view is closed 
         */
        
        internalFrameAdapter = new InternalFrameAdapter(){
            @Override  
            public void internalFrameClosing(InternalFrameEvent e) {
                ActionEvent actionEvent = new ActionEvent(
                        PatientView.this,ActionEvent.ACTION_PERFORMED,
                        ViewController.PatientViewControllerActionEvent.PATIENT_VIEW_CLOSED.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        this.addInternalFrameListener(internalFrameAdapter);
    }
    
    private void initialiseViewMode(ViewMode value){
        setViewMode(value);
        if (getViewMode().equals(ViewMode.CREATE)){
            this.btnCreateUpdatePatient.setText(CREATE_BUTTON);
        }
        else if (getViewMode().equals(ViewMode.UPDATE)){
            this.btnCreateUpdatePatient.setText(UPDATE_BUTTON);
        }
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
    
    private ViewMode getViewMode(){
        return viewMode;
    }
    private void setViewMode(ViewMode value){
        viewMode = value;
    }
    
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }

    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    
    /**
     * Method processes the PropertyChangeEvent its received from the view
     * controller
     * @param e PropertyChangeEvent
     * -- PATIENT_RECORDS_RECEIVED the received EntityDescriptor.Collection object 
     * contains the collection of all the patients recorded on the system
     * -- PATIENT_RECORD_RECEIVED the new EntityDescriptor.Patient contains the 
     * full details of a patient as a result of the view controller having
     * received a request from the view to either create a new patient, update 
     * an existing patient, or fetch the details of a newly selected patient. 
     */
    @Override
    public void propertyChange(PropertyChangeEvent e){

        if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENT_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            EntityDescriptor ed = getEntityDescriptor();
            this.btnCreateUpdatePatient.setText(UPDATE_BUTTON);
            initialisePatientViewComponentFromED();   
        }
        else if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENTS_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            populatePatientSelector(this.cmbSelectPatient);
            populatePatientSelector(this.cmbSelectGuardian);
        }
        
        /**
         * The view checks the details it requested in the create / update 
         * patient message to the view controller, tally with what it receives
         * back from the controller 
         */
        else if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENT_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            EntityDescriptor oldEntity = (EntityDescriptor)e.getOldValue();
            try{
                crossCheck(getEntityDescriptor().getPatient(),oldEntity.getPatient());
            }
            catch (CrossCheckErrorException ex){
                //UnpecifiedError action
            }
        }
    }

    private void crossCheck(EntityDescriptor.Patient newPatientValues, 
            EntityDescriptor.Patient oldPatientValues) throws CrossCheckErrorException {
        String errorMessage = null;
        boolean isCrossCheckError = false;
        String errorType = null;
        ArrayList<String> errorLog = new ArrayList<>();
        boolean isTitle = false;
        boolean isForenames = false;
        boolean isSurname = false;
        boolean isLine1 = false;
        boolean isLine2 = false;
        boolean isTown = false;
        boolean isCounty = false;
        boolean isPostcode = false;
        boolean isPhone1 = false;
        boolean isPhone2 = false;
        boolean isGender = false;
        boolean isDOB = false;
        boolean isGuardianAPatient = false;
        boolean isNotes = false;
        boolean isDentalRecallDate = false;
        boolean isHygieneRecallDate = false;
        boolean isDentalRecallFrequency = false;
        boolean isHygieneRecallFrequency = false;
        boolean isLastDentalAppointment = false;
        boolean isNextDentalAppointment = false;
        boolean isNextHygieneAppointment = false;
         
        for (int index = 0; index < 2; index ++){
            for (PatientField pf: PatientField.values()){
                switch (pf){
                    case TITLE -> {if (newPatientValues.getData().getTitle().equals(
                            oldPatientValues.getData().getTitle())){isTitle = true;}}
                    case FORENAMES -> {if (newPatientValues.getData().getForenames().equals(
                            oldPatientValues.getData().getForenames())){isForenames = true;}}
                    case SURNAME -> {if (newPatientValues.getData().getSurname().equals(
                            oldPatientValues.getData().getSurname())){isSurname = true;}}
                    case LINE1 -> {if (newPatientValues.getData().getLine1().equals(
                            oldPatientValues.getData().getLine1())){isLine1 = true;}}
                    case LINE2 -> {if (newPatientValues.getData().getLine2().equals(
                            oldPatientValues.getData().getLine2())){isLine2 = true;}}
                    case TOWN -> {if (newPatientValues.getData().getTown().equals(
                            oldPatientValues.getData().getTown())){isTown = true;}}
                    case COUNTY -> {if (newPatientValues.getData().getCounty().equals(
                            oldPatientValues.getData().getCounty())){isCounty = true;}}
                    case POSTCODE -> {if (newPatientValues.getData().getPostcode().equals(
                            oldPatientValues.getData().getPostcode())){isPostcode = true;}}
                    case PHONE1 -> {if (newPatientValues.getData().getPhone1().equals(
                            oldPatientValues.getData().getPhone1())){isPhone1 = true;}}
                    case PHONE2 -> {if (newPatientValues.getData().getPhone2().equals(
                            oldPatientValues.getData().getPhone2())){isPhone2 = true;}}
                    case GENDER -> {if (newPatientValues.getData().getGender().equals(
                            oldPatientValues.getData().getGender())){isGender = true;}}
                    case DOB -> {if ((newPatientValues.getData().getDOB().compareTo(
                            oldPatientValues.getData().getDOB())) == 0){isDOB = true;}}
                    case IS_GUARDIAN_A_PATIENT -> {if (newPatientValues.getData().getIsGuardianAPatient() &&
                            oldPatientValues.getData().getIsGuardianAPatient()){isGuardianAPatient = true;}}
                    case NOTES -> {if (newPatientValues.getData().getNotes().equals(
                            oldPatientValues.getData().getNotes())){isNotes = true;}}
                    case DENTAL_RECALL_DATE -> {if (newPatientValues.getData().getDentalRecallDate().equals(
                            oldPatientValues.getData().getDentalRecallDate())){isDentalRecallDate = true;}}
                    case HYGIENE_RECALL_DATE -> {if (newPatientValues.getData().getHygieneRecallDate().equals(
                            oldPatientValues.getData().getHygieneRecallDate())){isHygieneRecallDate = true;}}
                    case DENTAL_RECALL_FREQUENCY -> {if (newPatientValues.getData().getDentalRecallFrequency()==
                            oldPatientValues.getData().getDentalRecallFrequency()){isDentalRecallFrequency = true;}}
                    case HYGIENE_RECALL_FREQUENCY -> {if (newPatientValues.getData().getHygieneRecallFrequency()==
                            oldPatientValues.getData().getHygieneRecallFrequency()){isHygieneRecallFrequency = true;}}

                }
                if (errorType == null){
                    errorType = "patient";
                }
                else {
                    errorType = "guardian";
                }
                
                errorMessage = "Errors in cross check of requested " + errorType + " details and received " + errorType + "details listed below\n";
                if (!isTitle) {errorMessage = errorMessage + errorType + 
                        ".title field\n"; isCrossCheckError = true;} 
                if (!isForenames) {errorMessage = errorMessage + errorType + 
                        ".forenames field\n"; isCrossCheckError = true;} 
                if (!isSurname) {errorMessage = errorMessage + errorType + 
                        ".surname field\n"; isCrossCheckError = true;} 
                if (!isLine1) {errorMessage = errorMessage + errorType + 
                        ".line1 field\n"; isCrossCheckError = true;} 
                if (!isLine2) {errorMessage = errorMessage + errorType + 
                        ".line2 field\n"; isCrossCheckError = true;} 
                if (!isTown) {errorMessage = errorMessage + errorType + 
                        ".town field\n"; isCrossCheckError = true;} 
                if (!isCounty) {errorMessage = errorMessage + errorType + 
                        ".county field\n"; isCrossCheckError = true;}
                if (!isPostcode) {errorMessage = errorMessage + errorType + 
                        ".line1 field\n"; isCrossCheckError = true;} 
                if (!isPhone1) {errorMessage = errorMessage + errorType + 
                        ".phone1 field\n"; isCrossCheckError = true;} 
                if (!isPhone2) {errorMessage = errorMessage + errorType + 
                        ".phone2 field\n"; isCrossCheckError = true;}
                if (!isGender) {errorMessage = errorMessage + errorType + 
                        ".gender field\n"; isCrossCheckError = true;}
                if (!isDOB) {errorMessage = errorMessage + errorType + 
                        ".dob field\n"; isCrossCheckError = true;}
                if (!isGuardianAPatient) {errorMessage = errorMessage + errorType + 
                        ".isGuardianAParent field\n"; isCrossCheckError = true;}
                if (!isNotes) {errorMessage = errorMessage + errorType + 
                        ".notes field\n"; isCrossCheckError = true;}
                if (!isDentalRecallDate) {errorMessage = errorMessage + errorType + 
                        ".dentalRecalldate field\n"; isCrossCheckError = true;}
                if (!isHygieneRecallDate) {errorMessage = errorMessage + errorType + 
                        ".hygieneRecalldate field\n"; isCrossCheckError = true;}
                if (!isDentalRecallFrequency) {errorMessage = errorMessage + errorType + 
                        ".dentalRecallFrequency field\n"; isCrossCheckError = true;}
                if (!isHygieneRecallFrequency) {errorMessage = errorMessage + errorType + 
                        ".hygieneRecallFrequency field\n"; isCrossCheckError = true;}
                if (!isLastDentalAppointment){errorMessage = errorMessage + errorType + 
                        ".lastDentalAppointment field\n"; isCrossCheckError = true;}
                if (!isNextDentalAppointment){errorMessage = errorMessage + errorType + 
                        ".nextDentalAppointment field\n"; isCrossCheckError = true;}
                if (!isNextHygieneAppointment){errorMessage = errorMessage + errorType + 
                        ".NextHygieneAppointment field\n"; isCrossCheckError = true;}
                
            }
            errorLog.add(errorMessage);
            
            /**
             * break process anyway if there is no guardian details to process 
             */
            if (!newPatientValues.getData().getIsGuardianAPatient()){
                break;
            }
            
            //re-initialise error markers to process guardian details
            isTitle = false;
            isForenames = false;
            isSurname = false;
            isLine1 = false;
            isLine2 = false;
            isTown = false;
            isCounty = false;
            isPostcode = false;
            isPhone1 = false;
            isPhone2 = false;
            isGender = false;
            isDOB = false;
            isGuardianAPatient = false;
            isNotes = false;
            isDentalRecallDate = false;
            isHygieneRecallDate = false;
            isDentalRecallFrequency = false;
            isHygieneRecallFrequency = false;
            isLastDentalAppointment = false;
            isNextDentalAppointment = false;
            isNextHygieneAppointment = false;
        }
        if (isCrossCheckError){
            String message = null;
            Iterator<String> it = errorLog.iterator();
            while(it.hasNext()){
                message = it.next();
                message = message + "\n";
            }
            throw new CrossCheckErrorException(message);
        }
    }
    /**
     * The method initialises the guardian component of the view state from the 
     * current entity state
     */
    private void initialisePatientGuardianViewComponentFromED(){
        //populatePatientSelector(this.cmbSelectGuardian);
        if (getEntityDescriptor().getPatientGuardian().getData()!=null){
            this.cmbSelectGuardian.setEnabled(true);
            this.cmbSelectGuardian.setSelectedItem(
                    getEntityDescriptor().getPatientGuardian());
        }
        else{
            this.cmbSelectGuardian.setSelectedIndex(-1);
            this.cmbSelectGuardian.setEnabled(false);
        }
    }
    private void populateAppointmentsHistoryTable(ArrayList<EntityDescriptor.Appointment> appointments, String header){
        
            
        AppointmentsSingleColumnTableModel.appointments = appointments;
        tableModel = new AppointmentsSingleColumnTableModel();
        this.tblAppointmentHistory.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblAppointmentHistory.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblAppointmentHistory.setModel(tableModel);
        this.tblAppointmentHistory.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel columnModel = this.tblAppointmentHistory.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(105);
        columnModel.getColumn(1).setPreferredWidth(105);
        columnModel.getColumn(2).setPreferredWidth(253);
        JTableHeader tableHeader = this.tblAppointmentHistory.getTableHeader();
        tableHeader.setBackground(Color.LIGHT_GRAY);
        tableHeader.setOpaque(false);
        DefaultTableCellRenderer renderer = 
                (DefaultTableCellRenderer) tblAppointmentHistory.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(0);

        if (appointments.size() > 0){
            this.tblAppointmentHistory.setVisible(true);
            this.scrAppointmentHistory.getColumnHeader().setVisible(true);
            TitledBorder titledBorder = (TitledBorder)this.pnlAppointmentHistory.getBorder();
            titledBorder.setTitle(
                    BorderTitles.Appointment_history.toString().replace('_', ' ') + 
                            " (" + String.valueOf(appointments.size() + ")"));
            this.pnlAppointmentHistory.repaint();
        }
        else {
            this.tblAppointmentHistory.setVisible(false);
            this.scrAppointmentHistory.getColumnHeader().setVisible(false);
                //this.tblAppointmentHistory = null;
               //this.tblAppointmentHistory.setVisible(false); 
            TitledBorder titledBorder = (TitledBorder)this.pnlAppointmentHistory.getBorder();
            titledBorder.setTitle(BorderTitles.Appointment_history.toString().replace('_', ' '));
            this.scrAppointmentHistory.repaint();
        }
    }
    /**
     * The method initialises the patient view's appointment history view 
     * component from the EntityDescriptor.Patient object
     */
    private void initialisePatientAppointmentHistoryViewFromED(Category category){
        ArrayList<EntityDescriptor.Appointment> appointments = new ArrayList<>();
        String headerTitle = switch (category){
            case DENTAL-> {appointments = 
                    getEntityDescriptor().getPatientAppointmentHistory().getDentalAppointments();
                yield "Dental appointments";
            }
            case HYGIENE-> {appointments = 
                    getEntityDescriptor().getPatientAppointmentHistory().getHygieneAppointments();
                yield "Hygiene appointments";
            }   
        };
        populateAppointmentsHistoryTable(appointments, headerTitle);
    }
    private int getAge(LocalDate dob){
        return Period.between(dob, LocalDate.now()).getYears();
    }
    /**
     * The method initialises the patient component of the view state from the
     * current entity state
     */
    private void initialisePatientViewComponentFromED(){      
        RenderedPatient patient = getEntityDescriptor().getPatient().getData();
        setCounty(patient.getCounty());
        setLine1(patient.getLine1());
        setLine2(patient.getLine2());
        setPostcode(patient.getPostcode());
        setTown(patient.getTown());
        setDOB(patient.getDOB());
        txtAGE.setText(String.valueOf(getAge(patient.getDOB())));
        setDentalRecallDate(patient.getDentalRecallDate());
        setDentalRecallFrequency(patient.getDentalRecallFrequency());
        setForenames(patient.getForenames());
        //setHygieneRecallDate(patient.getHygieneRecallDate());
        //setHygieneRecallFrequency(patient.getHygieneRecallFrequency());
        setGender(patient.getGender());
        setIsGuardianAPatient(patient.getIsGuardianAPatient());
        setNotes(patient.getNotes());
        setPatientTitle(patient.getTitle());
        setPhone1(patient.getPhone1());
        setPhone2(patient.getPhone2());
        setSurname(patient.getSurname());
        
        this.setTitle(getSurname());
        
        initialisePatientGuardianViewComponentFromED();
        initialisePatientAppointmentHistoryViewFromED(Category.DENTAL);
    }
    private void initialiseEntityFromView(){
        getEntityDescriptor().getRequest().getPatient().getData().setCounty((getCounty()));
        getEntityDescriptor().getRequest().getPatient().getData().setDentalRecallDate(getDentalRecallDate());
        getEntityDescriptor().getRequest().getPatient().getData().setDOB(getDOB());
        getEntityDescriptor().getRequest().getPatient().getData().setForenames(getForenames());
        getEntityDescriptor().getRequest().getPatient().getData().setGender(getGender());
        getEntityDescriptor().getRequest().getPatient().getData().setDentalRecallDate(getDentalRecallDate());
        getEntityDescriptor().getRequest().getPatient().getData().setDentalRecallFrequency(getDentalRecallFrequency());
        //getEntityDescriptor().getRequest().getPatient().getData().setHygieneRecallDate(getHygieneRecallDate());
       //getEntityDescriptor().getRequest().getPatient().getData().setHygieneRecallFrequency(getHygieneRecallFrequency());
        getEntityDescriptor().getRequest().getPatient().getData().setIsGuardianAPatient(getIsGuardianAPatient());
        getEntityDescriptor().getRequest().getPatient().getData().setLine1(getLine1());
        getEntityDescriptor().getRequest().getPatient().getData().setLine2(getLine2());
        getEntityDescriptor().getRequest().getPatient().getData().setNotes(getNotes());
        getEntityDescriptor().getRequest().getPatient().getData().setPhone1(getPhone1());
        getEntityDescriptor().getRequest().getPatient().getData().setPhone2(getPhone2());
        getEntityDescriptor().getRequest().getPatient().getData().setPostcode(getPostcode());
        getEntityDescriptor().getRequest().getPatient().getData().setSurname(getSurname());
        getEntityDescriptor().getRequest().getPatient().getData().setTitle(getPatientTitle());
        getEntityDescriptor().getRequest().getPatient().getData().setTown(getTown());
        if (getGuardian() != null){
            getEntityDescriptor().getRequest().setGuardian(getGuardian());
        }
        
            
        
        /**
         * Note: the following GUI field values will have already been initialised 
         * in the EntityDescriptor object, ie are read-only from the user
         * point of view. Even though the user can update the value of the 
         * Guardian displayed in txtGuardian widget, this is done indirectly via 
         * a call to another view (dialog) on return from which the 
         * EntityDescriptor object is initialised 
         */
    }

    private ActionListener getMyController(){
        return myController;
    } 

    private void setMyController(ActionListener value){
        myController = value;
    }
    
    private String getPatientTitle(){
        String value = "";
        if(TitleItem.Dr.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Dr.toString();
        }
        else if(TitleItem.Mr.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Mr.toString();
        }
        else if(TitleItem.Mrs.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Mrs.toString();
        }
        else if(TitleItem.Ms.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Ms.toString();
        }
        return value;
    }
    private void setPatientTitle(String title){
        Integer index = null;
        for(TitleItem ti: TitleItem.values()){
            if (ti.toString().equals(title)){
                index = ti.ordinal();
                break;
            }
        }
        if (index != null){
            cmbTitle.setSelectedIndex(index);
        }
        else {
            cmbTitle.setSelectedIndex(-1);
        }
    }
    private String getForenames(){
        return this.txtForenames.getText();
    }
    private void setForenames(String forenames){
        this.txtForenames.setText(forenames);
    }
    private String getSurname(){
        return this.txtSurname.getText();
    }
    private void setSurname(String surname){
        this.txtSurname.setText(surname);
    }
    private String getLine1(){
        return this.txtAddressLine1.getText();
    }
    private void setLine1(String line1){
        this.txtAddressLine1.setText(line1);
    }
    private String getLine2(){
        return this.txtAddressLine2.getText();
    }
    private void setLine2(String line2){
        this.txtAddressLine2.setText(line2);
    }
    private String getTown(){
        return this.txtAddressTown.getText();
    }
    private void setTown(String town){
        this.txtAddressTown.setText(town);
    }
    private String getCounty(){
        return this.txtAddressCounty.getText();
    }
    private void setCounty(String county){
        this.txtAddressCounty.setText(county);
    }
    private String getPostcode(){
        return this.txtAddressPostcode.getText();
    }
    private void setPostcode(String postcode){
        this.txtAddressPostcode.setText(postcode);
    }
    private String getGender(){
        String result = "";
        if (this.cmbGender.getSelectedIndex()!=-1){
            result = this.cmbGender.getSelectedItem().toString();
        }
        return result;
    }
    private void setGender(String gender){
        Integer index = null;
        for (GenderItem gi: GenderItem.values()){
            if (gi.toString().equals(gender)){
                index = gi.ordinal();
                break;
            }
        }
        if (index != null){
            cmbGender.setSelectedIndex(index);
        }
        else {
            cmbGender.setSelectedIndex(-1);
        }
    }
    private LocalDate getDOB(){
        LocalDate value = null;
        if (!this.txtDOB.getText().equals("")){
            try{
                value = LocalDate.parse(this.txtDOB.getText(),dmyFormat);
            }
            catch (DateTimeParseException e){
                //UnspecifiedErrorAction
            } 
            
        }
        return value;   
    }
    private void setDOB(LocalDate value){
        if (value == null){
            this.txtDOB.setText("");
        }
        else{
            this.txtDOB.setText(value.format(dmyFormat));
        }
    }
    private boolean getIsGuardianAPatient(){
        boolean value = false;
        if(YesNoItem.Yes.ordinal()==this.cmbIsGuardianAPatient.getSelectedIndex()){
            value = true;
        }
        else if(YesNoItem.No.ordinal()==this.cmbIsGuardianAPatient.getSelectedIndex()){
            value = false;
        }
        return value;
    }
    private void setIsGuardianAPatient(boolean isGuardianAPatient){
        if (isGuardianAPatient){
            cmbIsGuardianAPatient.setSelectedItem(YesNoItem.Yes);
        }
        else{
            cmbIsGuardianAPatient.setSelectedItem(YesNoItem.No);
        }
    }
    private EntityDescriptor.Patient getGuardian(){
        if (cmbSelectGuardian.getSelectedIndex() == -1){
            return null;
        }
        else {
            return (EntityDescriptor.Patient)cmbSelectGuardian.getSelectedItem();
        }
    }
    private LocalDate getDentalRecallDate(){
        LocalDate value = null;
        if (!this.txtDentalRecallDate.getText().equals("")){
            try{
                value = LocalDate.parse(this.txtDentalRecallDate.getText(),myFormat);
            }
            catch (DateTimeParseException e){
                //UnspecifiedErrorAction
            } 
        }
        return value;
    }
    private void setDentalRecallDate(LocalDate dentalRecallDate){
        if (dentalRecallDate == null){
            this.txtDentalRecallDate.setText("");
        }
        else{
            this.txtDentalRecallDate.setText(dentalRecallDate.format(myFormat));
        }
    }
    /*
    private LocalDate getHygieneRecallDate(){
        LocalDate value = null;
        if (!this.txtHygieneRecallDate.getText().equals("")){
            try{
                value = LocalDate.parse(this.txtHygieneRecallDate.getText(),myFormat);
            }
            catch (DateTimeParseException e){
                //UnspecifiedErrorAction
            } 
        }
        return value;
    }
    */
    /*
    private void setHygieneRecallDate(LocalDate hygieneRecallDate){
        if (hygieneRecallDate == null){
            this.txtHygieneRecallDate.setText("");
        }
        else{
            this.txtHygieneRecallDate.setText(hygieneRecallDate.format(myFormat));
        }
    }
    */
    private Integer getDentalRecallFrequency(){
        return (Integer)this.spnDentalRecallFrequency.getValue();
    }
    private void setDentalRecallFrequency(Integer value){
        this.spnDentalRecallFrequency.setValue(value);
    }
    /*
    private Integer getHygieneRecallFrequency(){
        return (Integer)this.spnHygieneRecallFrequency.getValue();
    }
    private void setHygieneRecallFrequency(Integer value){
        this.spnHygieneRecallFrequency.setValue(value);
    }
    */
    private String getNotes(){
        return this.txaPatientNotes.getText();
    }
    private void setNotes(String notes){
        this.txaPatientNotes.setText(notes);
    }
    private String getPhone1(){
        return txtPhone1.getText();
    }
    private void setPhone1(String value){
        txtPhone1.setText(value);
    }
    private String getPhone2(){
        return txtPhone2.getText();
    }
    private void setPhone2(String value){
        txtPhone2.setText(value);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        pnlContactDetails = new javax.swing.JPanel();
        lblSurname = new javax.swing.JLabel();
        txtSurname = new javax.swing.JTextField();
        jblForenames = new javax.swing.JLabel();
        txtForenames = new javax.swing.JTextField();
        lblTitle = new javax.swing.JLabel();
        cmbTitle = new javax.swing.JComboBox<TitleItem>();
        lblAddress = new javax.swing.JLabel();
        txtAddressLine1 = new javax.swing.JTextField();
        txtAddressLine2 = new javax.swing.JTextField();
        lblTown = new javax.swing.JLabel();
        txtAddressTown = new javax.swing.JTextField();
        jblCounty = new javax.swing.JLabel();
        txtAddressCounty = new javax.swing.JTextField();
        jblPostcode = new javax.swing.JLabel();
        txtAddressPostcode = new javax.swing.JTextField();
        jblPhoneHome = new javax.swing.JLabel();
        txtPhone1 = new javax.swing.JTextField();
        jblPhone2 = new javax.swing.JLabel();
        txtPhone2 = new javax.swing.JTextField();
        lblGender = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox<GenderItem>();
        lblDOB = new javax.swing.JLabel();
        txtDOB = new javax.swing.JTextField();
        lblDOB1 = new javax.swing.JLabel();
        txtAGE = new javax.swing.JTextField();
        pnlRecallDetails = new javax.swing.JPanel();
        lblDATE = new javax.swing.JLabel();
        lblFREQUENCY = new javax.swing.JLabel();
        txtDentalRecallDate = new javax.swing.JTextField();
        spnDentalRecallFrequency = new javax.swing.JSpinner();
        pnlPatientNotes = new javax.swing.JPanel();
        scpPatientNotes = new javax.swing.JScrollPane();
        txaPatientNotes = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        btnCreateUpdatePatient = new javax.swing.JButton();
        btnCloseView = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        pnlGuardianDetails = new javax.swing.JPanel();
        lblGuardianIsAPatient = new javax.swing.JLabel();
        cmbIsGuardianAPatient = new javax.swing.JComboBox<YesNoItem>();
        lblGuardianPatientName = new javax.swing.JLabel();
        cmbSelectGuardian = new javax.swing.JComboBox<EntityDescriptor.Patient>();
        pnlAppointmentHistory = new javax.swing.JPanel();
        scrAppointmentHistory = new javax.swing.JScrollPane();
        tblAppointmentHistory = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        cmbSelectPatient = new javax.swing.JComboBox<EntityDescriptor.Patient>();
        jButton2 = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Patient guardian details"));
        setTitle("Patient view");

        pnlContactDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Contact Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        lblSurname.setText("Surname");

        jblForenames.setText("Forenames");

        txtForenames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtForenamesActionPerformed(evt);
            }
        });

        lblTitle.setText("Title");

        cmbTitle.setEditable(true);
        cmbTitle.setModel(new javax.swing.DefaultComboBoxModel<>(TitleItem.values()));

        lblAddress.setText("Address");

        txtAddressLine1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAddressLine1ActionPerformed(evt);
            }
        });

        lblTown.setText("Town");

        jblCounty.setText("County");

        jblPostcode.setText("Postcode");

        txtAddressPostcode.setPreferredSize(new java.awt.Dimension(20, 20));

        jblPhoneHome.setText("Phone (1)");

        jblPhone2.setText("Phone (2)");

        lblGender.setText("Gender");

        cmbGender.setEditable(true);
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(GenderItem.values()));

        lblDOB.setText("DOB");

        txtDOB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDOBActionPerformed(evt);
            }
        });

        lblDOB1.setText("Age");

        txtAGE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAGEActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlContactDetailsLayout = new javax.swing.GroupLayout(pnlContactDetails);
        pnlContactDetails.setLayout(pnlContactDetailsLayout);
        pnlContactDetailsLayout.setHorizontalGroup(
            pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jblPhoneHome, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jblPhone2)
                            .addComponent(lblDOB, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPhone1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDOB, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(8, 8, 8)
                                .addComponent(lblDOB1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtAGE, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jblCounty)
                            .addComponent(lblTown))
                        .addGap(42, 42, 42)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtAddressTown, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                            .addComponent(txtAddressCounty)))
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addComponent(lblSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addComponent(jblForenames, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtForenames, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(lblGender)
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addComponent(lblAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAddressLine2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAddressLine1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addComponent(jblPostcode, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(txtAddressPostcode, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15))
        );
        pnlContactDetailsLayout.setVerticalGroup(
            pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtAGE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSurname)
                            .addComponent(txtSurname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(txtForenames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContactDetailsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jblForenames)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTitle)
                            .addComponent(lblGender)
                            .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblAddress)
                            .addComponent(txtAddressLine1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addComponent(txtAddressLine2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAddressTown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTown))
                        .addGap(4, 4, 4)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jblCounty)
                            .addComponent(txtAddressCounty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jblPostcode)
                            .addComponent(txtAddressPostcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPhone1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jblPhoneHome))
                        .addGap(4, 4, 4)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                .addComponent(jblPhone2)
                                .addGap(11, 11, 11)
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblDOB1)
                                    .addComponent(lblDOB)))
                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(txtDOB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(10, 10, 10))
        );

        pnlRecallDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Recall details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        lblDATE.setText("Date");

        lblFREQUENCY.setText("Frequency (months)");

        spnDentalRecallFrequency.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spnDentalRecallFrequency.setToolTipText("recall frequency (months)");

        javax.swing.GroupLayout pnlRecallDetailsLayout = new javax.swing.GroupLayout(pnlRecallDetails);
        pnlRecallDetails.setLayout(pnlRecallDetailsLayout);
        pnlRecallDetailsLayout.setHorizontalGroup(
            pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(txtDentalRecallDate, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRecallDetailsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblDATE, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)))
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(spnDentalRecallFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRecallDetailsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblFREQUENCY)
                        .addGap(25, 25, 25))))
        );
        pnlRecallDetailsLayout.setVerticalGroup(
            pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFREQUENCY)
                    .addComponent(lblDATE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnDentalRecallFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDentalRecallDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );

        pnlPatientNotes.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Patient notes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        txaPatientNotes.setColumns(20);
        txaPatientNotes.setLineWrap(true);
        txaPatientNotes.setRows(5);
        scpPatientNotes.setViewportView(txaPatientNotes);

        javax.swing.GroupLayout pnlPatientNotesLayout = new javax.swing.GroupLayout(pnlPatientNotes);
        pnlPatientNotes.setLayout(pnlPatientNotesLayout);
        pnlPatientNotesLayout.setHorizontalGroup(
            pnlPatientNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPatientNotesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scpPatientNotes)
                .addContainerGap())
        );
        pnlPatientNotesLayout.setVerticalGroup(
            pnlPatientNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPatientNotesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(scpPatientNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        btnCreateUpdatePatient.setText("Update existing patient from entered details");
        btnCreateUpdatePatient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateUpdatePatientActionPerformed(evt);
            }
        });

        btnCloseView.setText("Close view");
        btnCloseView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseViewActionPerformed(evt);
            }
        });

        jButton1.setText("Reset view");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btnCreateUpdatePatient)
                .addGap(41, 41, 41)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCloseView)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCloseView))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnCreateUpdatePatient)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(6, 6, 6))
        );

        pnlGuardianDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Guardian details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        lblGuardianIsAPatient.setText("Parent/guardian also a patient?");

        cmbIsGuardianAPatient.setEditable(true);
        cmbIsGuardianAPatient.setModel(new javax.swing.DefaultComboBoxModel<YesNoItem>(YesNoItem.values()));
        cmbIsGuardianAPatient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbIsGuardianAPatientActionPerformed(evt);
            }
        });

        lblGuardianPatientName.setText("Guardian");

        cmbSelectGuardian.setEditable(false);
        cmbSelectGuardian.setModel(new DefaultComboBoxModel<EntityDescriptor.Patient>());

        javax.swing.GroupLayout pnlGuardianDetailsLayout = new javax.swing.GroupLayout(pnlGuardianDetails);
        pnlGuardianDetails.setLayout(pnlGuardianDetailsLayout);
        pnlGuardianDetailsLayout.setHorizontalGroup(
            pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGuardianDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGuardianDetailsLayout.createSequentialGroup()
                        .addComponent(lblGuardianIsAPatient)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbIsGuardianAPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlGuardianDetailsLayout.createSequentialGroup()
                        .addComponent(lblGuardianPatientName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbSelectGuardian, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlGuardianDetailsLayout.setVerticalGroup(
            pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGuardianDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGuardianIsAPatient)
                    .addComponent(cmbIsGuardianAPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGuardianPatientName)
                    .addComponent(cmbSelectGuardian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pnlAppointmentHistory.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Appointment history", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        scrAppointmentHistory.setRowHeaderView(null);
        scrAppointmentHistory.setViewportView(tblAppointmentHistory);

        javax.swing.GroupLayout pnlAppointmentHistoryLayout = new javax.swing.GroupLayout(pnlAppointmentHistory);
        pnlAppointmentHistory.setLayout(pnlAppointmentHistoryLayout);
        pnlAppointmentHistoryLayout.setHorizontalGroup(
            pnlAppointmentHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrAppointmentHistory)
                .addContainerGap())
        );
        pnlAppointmentHistoryLayout.setVerticalGroup(
            pnlAppointmentHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentHistoryLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(scrAppointmentHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Select patient", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        cmbSelectPatient.setEditable(false);
        cmbSelectPatient.setModel(new DefaultComboBoxModel<EntityDescriptor.Patient>());
        cmbSelectPatient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSelectPatientActionPerformed(evt);
            }
        });

        jButton2.setText("Clear patient selection");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlAppointmentHistory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(pnlContactDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlRecallDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlPatientNotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlGuardianDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlGuardianDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(pnlRecallDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(pnlPatientNotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(pnlContactDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(pnlAppointmentHistory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCreateUpdatePatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateUpdatePatientActionPerformed
        // TODO add your handling code here:
        initialiseEntityFromView();
        if (getViewMode().equals(ViewMode.CREATE)){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_VIEW_CREATE_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        else if (getViewMode().equals(ViewMode.UPDATE)){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_VIEW_UPDATE_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }//GEN-LAST:event_btnCreateUpdatePatientActionPerformed

    private void btnCloseViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseViewActionPerformed
        String[] options = {"Yes", "No"};
        int close = JOptionPane.showOptionDialog(this,
                        "Any changes to patient record will be lost. Cancel anyway?",null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        null);
        if (close == JOptionPane.YES_OPTION){
            try{
                /**
                 * setClosed will fire INTERNAL_FRAME_CLOSED event for the 
                 * listener to send ActionEvent to the view controller
                 */
                this.setClosed(true);
            }
            catch (PropertyVetoException e){
                //UnspecifiedError action
            }
        }
        
    }//GEN-LAST:event_btnCloseViewActionPerformed

    private void cmbSelectPatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSelectPatientActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSelectPatientActionPerformed

    private void txtAGEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAGEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAGEActionPerformed

    private void txtDOBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDOBActionPerformed
        //calculate and display age when textfield contents updated

    }//GEN-LAST:event_txtDOBActionPerformed

    private void txtAddressLine1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAddressLine1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAddressLine1ActionPerformed

    private void txtForenamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtForenamesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtForenamesActionPerformed

    private void cmbIsGuardianAPatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbIsGuardianAPatientActionPerformed
        if (this.cmbIsGuardianAPatient.getSelectedItem()!=null){
            switch ((YesNoItem)this.cmbIsGuardianAPatient.getSelectedItem()){
                case Yes -> this.cmbSelectGuardian.setEnabled(true);
                case No -> this.cmbSelectGuardian.setEnabled(false);
            }
        }
    }//GEN-LAST:event_cmbIsGuardianAPatientActionPerformed

    private void cmbSelectPatientActionPerformed(){
        if (this.cmbSelectPatient.getSelectedItem()!=null){
            EntityDescriptor.Patient patient = (EntityDescriptor.Patient)this.cmbSelectPatient.getSelectedItem();
            getEntityDescriptor().getRequest().setPatient(patient);
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    PatientViewControllerActionEvent.PATIENT_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCloseView;
    private javax.swing.JButton btnCreateUpdatePatient;
    private javax.swing.JComboBox<GenderItem> cmbGender;
    private javax.swing.JComboBox<YesNoItem> cmbIsGuardianAPatient;
    private javax.swing.JComboBox<EntityDescriptor.Patient> cmbSelectGuardian;
    private javax.swing.JComboBox<EntityDescriptor.Patient> cmbSelectPatient;
    private javax.swing.JComboBox<TitleItem> cmbTitle;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jblCounty;
    private javax.swing.JLabel jblForenames;
    private javax.swing.JLabel jblPhone2;
    private javax.swing.JLabel jblPhoneHome;
    private javax.swing.JLabel jblPostcode;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblDATE;
    private javax.swing.JLabel lblDOB;
    private javax.swing.JLabel lblDOB1;
    private javax.swing.JLabel lblFREQUENCY;
    private javax.swing.JLabel lblGender;
    private javax.swing.JLabel lblGuardianIsAPatient;
    private javax.swing.JLabel lblGuardianPatientName;
    private javax.swing.JLabel lblSurname;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTown;
    private javax.swing.JPanel pnlAppointmentHistory;
    private javax.swing.JPanel pnlContactDetails;
    private javax.swing.JPanel pnlGuardianDetails;
    private javax.swing.JPanel pnlPatientNotes;
    private javax.swing.JPanel pnlRecallDetails;
    private javax.swing.JScrollPane scpPatientNotes;
    private javax.swing.JScrollPane scrAppointmentHistory;
    private javax.swing.JSpinner spnDentalRecallFrequency;
    private javax.swing.JTable tblAppointmentHistory;
    private javax.swing.JTextArea txaPatientNotes;
    private javax.swing.JTextField txtAGE;
    private javax.swing.JTextField txtAddressCounty;
    private javax.swing.JTextField txtAddressLine1;
    private javax.swing.JTextField txtAddressLine2;
    private javax.swing.JTextField txtAddressPostcode;
    private javax.swing.JTextField txtAddressTown;
    private javax.swing.JTextField txtDOB;
    private javax.swing.JTextField txtDentalRecallDate;
    private javax.swing.JTextField txtForenames;
    private javax.swing.JTextField txtPhone1;
    private javax.swing.JTextField txtPhone2;
    private javax.swing.JTextField txtSurname;
    // End of variables declaration//GEN-END:variables
    private DatePicker dobPicker;
    private DatePicker dentalRecallPicker;
    private DatePicker hygieneRecallPicker;

    class DOBDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date != null) {
                //DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtDOB.setText(date.format(dmyFormat));
                //LocalDate dob = LocalDate.parse(txtDOB.getText(), dmyFormat);
                txtAGE.setText(String.valueOf(Period.between(date, LocalDate.now()).getYears()));
            }
            else txtDOB.setText("");
        }
    }

    class DentalRecallDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date != null) {
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtDentalRecallDate.setText(date.format(myFormat));
            }
            else txtDentalRecallDate.setText("");
        }
    }
    
    /*
    class HygieneRecallDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date != null) {
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtHygieneRecallDate.setText(date.format(myFormat));
            }
            else txtHygieneRecallDate.setText("");
        }
    }
*/

}
