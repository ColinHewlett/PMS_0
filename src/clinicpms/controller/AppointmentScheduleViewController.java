/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import static clinicpms.controller.ViewController.displayErrorMessage;
import clinicpms.model.Appointments;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.model.SurgeryDaysAssignment;
import clinicpms.store.StoreException;
import clinicpms.view.views.DesktopView;
import clinicpms.view.View;
import clinicpms.view.interfaces.IView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class AppointmentScheduleViewController extends ViewController{

    private enum RequestedAppointmentState{ STARTS_AFTER_PREVIOUS_SLOT,
                                            ENDS_AFTER_PREVIOUS_SLOT,
                                            APPOINTMENT_ADDED_TO_SCHEDULE,
                                            ERROR_ADDING_APPOINTMENT_TO_SCHEDULE}

    private ActionListener myController = null;
    private PropertyChangeSupport pcSupport = null;
    private View view = null;
    private View view2 = null;
    private ArrayList<Appointment> appointments = null;  
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    //private LocalDate day = null;
    private PropertyChangeEvent pcEvent = null;
    //private JFrame owningFrame = null;
    //private ViewController.ViewMode viewMode = null;
    //private AppointmentEditorDialog dialog = null;
    //private AppointmentViewDialog dialog = null;
    //private InternalFrameAdapter internalFrameAdapter = null;
    private View pacView = null;
    private DesktopView desktopView = null;
    
    /**
     * 
     * @param controller
     * @param desktopView
     * @param ed
     * @throws StoreException 
     */
    public AppointmentScheduleViewController(ActionListener controller, DesktopView desktopView, Optional<EntityDescriptor> ed)throws StoreException{
        setMyController(controller);
        this.desktopView = desktopView;
        //this.owningFrame = desktopView;
        pcSupport = new PropertyChangeSupport(this);
        //setNewEntityDescriptor(new EntityDescriptor());
        //getNewEntityDescriptor().getRequest().setDay(LocalDate.now());
        EntityDescriptor e = ed.orElse(new EntityDescriptor());
        setNewEntityDescriptor(e);
        setOldEntityDescriptor(getNewEntityDescriptor());
        try{
            SurgeryDaysAssignment surgeryDaysAssignment = new SurgeryDaysAssignment();
            //surgeryDaysAssignment.read();
            surgeryDaysAssignment = surgeryDaysAssignment.readTheSurgeryDaysAssignment();
            getNewEntityDescriptor().setSurgeryDaysAssignment(surgeryDaysAssignment.get());
            View.setViewer(View.Viewer.APPOINTMENT_SCHEDULE_VIEW);
            this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
            super.centreViewOnDesktop(desktopView, view);
            this.view.addInternalFrameClosingListener(); 
            this.view.initialiseView();
            pcSupport.removePropertyChangeListener(this.view);
            //this.day = getNewEntityDescriptor().getRequest().getDay();
        }
        catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
        }   
    }
    @Override
    public void actionPerformed(ActionEvent e){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        /**
         * On each case listeners on both views are removed (no action if listener not registered)
         * and the listener for that case added. This ensures only a single listener, and the correct
         * one, is active when property change events are fired
         */
        
        //View test = (View)e.getSource();
        if (e.getSource() instanceof DesktopViewController){
            doDesktopViewControllerAction(e);
        }
        else {
            /**
             * The following code is required because JInternalFrame modality is not directly supported in Java which has the following consequences
             * -- usage of modality-based JDialogs (1) JDialogs can be moved outside the JDesktop view and (2) are visually different to JInternalFrames
             * -- for this reason third party code has been used to add modality to JInternlFrames (https://stackoverflow.com/questions/16590399/modal-jinternalframe-that-returns-data-to-caller)
             * -- the consequences of adoption of this approach explain the logic of following code
             * 
             * First action: reference to source of action event fetched
             * If source is a modal JinternalFrame 2 views active for this view controller
             * -- the main non-modal JInternalFrame and
             * -- the modal JInternalFrame launched from the main JInternalFrame
             * ---- on construction the modal JInternalFrame does not return (!) to the view controller and hence the controller does not have a reference to the non-modal view, which it needs
             * ---- only when modal JInternalFrame sends an action event can view controller fetch the reference to the modal view 
             * ---- modal form reference is needed to close down form when required, but is also used for any other reason the modal form might want to communicate with the view controller
             * ------ in particular: because the modality of JInternalFrame does not restrict access to the desktop controls, at appropriate times the modal JInternalFrame requests the view controller to do this immediately after the construction of the modal JInternalFrame
             * ---- the view controller maintains a global reference to the non modal JInternalForm
             * ------ this is used to fire a property change event to the main form on the closure of the modal form
             * 
             */
            View the_view = (View)e.getSource();
            switch(the_view.getMyViewType()){
                case APPOINTMENT_SCHEDULE_VIEW:
                    doPrimaryViewActionRequest(e);
                    break;
                default:
                    doSecondaryViewActionRequest(e);
                    break;
                /*
                case APPOINTMENT_SCHEDULE_VIEW:
                    doAppointmentScheduleViewAction(e);
                    break;
                case APPOINTMENT_CREATOR_EDITOR_VIEW:
                    this.view2 = (View)e.getSource();
                    doAppointmentCreatorEditorViewAction(e);
                    break;
                case EMPTY_SLOT_SCANNER_VIEW:
                    this.view2 = (View)e.getSource();
                    doEmptySlotScannerViewAction(e);
                    break;
                case NON_SURGERY_DAY_EDITOR_VIEW:
                    this.view2 = (View)e.getSource();
                    doNonSurgeryDayScheduleEditorViewAction(e);
                    break;
                case SCHEDULE_CONTACT_DETAILS_VIEW:
                    doScheduleContactListView(e);
                    break;
                case SURGERY_DAY_EDITOR_VIEW:
                    this.view2 = (View)e.getSource();
                    doSurgeryDaysEditorViewAction(e);
                    break;
                */
            }
        }
        
    }
    
    private void doAppointmentCancelRequest(){
        /**
         * on receipt of APPOINTMENT_CANCEL_REQUEST
         * -- assumes on entry EntityDescriptorFromView has been initialised (by view)
         * -- hence: getRequest.getPatient() returns the patient whose appointment has been cancelled
         */
        if (getEntityDescriptorFromView().getRequest().getAppointment().getAppointee().getData().getKey()!=null){

            try{
               /**
                * The requested appointment is read into memory from the store using the specified appointment key
                * -- this means the appointee (patient) object is encapsulated in the appointment object
                * -- knowledge of the patient object is required to maintain consistency between views, which is a responsibility of the desktop view controller
                * 
                */
                Appointment appointment = new Appointment(
                   getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey()).read();
                appointment.delete();
               /**
                * two updates to a new Entity Descriptor required
                * -- serialise appointments derived from appointments and empty slots for the day
                * -- serialise the appointment which has just been delete (required for post processing by DesktopViewController)
                */
               /*
                Appointments appointments = null;
                LocalDate day = getEntityDescriptorFromView().
                       getRequest().getAppointment().getData().getStart().toLocalDate();
                initialiseNewEntityDescriptor();
                appointments = new Appointments();
                appointments.readForDay(day);
                ArrayList<Appointment> appointmentSlotsForDay = 
                        getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day);
                appointments.addAll(appointmentSlotsForDay);
                serialiseAppointmentsToEDCollection(appointments);
               */
               LocalDate day = getEntityDescriptorFromView().
                       getRequest().getAppointment().getData().getStart().toLocalDate();
               getUpdatedAppointmentSlotsForDay(day);
               /**
                * appointment serialisation to ED ensures appointee key is accessible by the desktop view controller
                */
                serialiseAppointmentToEDAppointment(appointment);
               /**
                * fire event over to APPOINTMENT_SCHEDULE
                */ 
                pcSupport.addPropertyChangeListener(this.view);
                pcEvent = new PropertyChangeEvent(this,
                   EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                   getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
                pcSupport.removePropertyChangeListener(this.view);
               /**
                * send APPOINTMENT_HISTORY_CHANGE_NOTIFICATION to Desktop view controller
                * -- prime the appointment view controller's entity description with the patient that has just been deleted
                * -- i.e so view controller can reliably let the desktop view controller know which patient has had an appointment deleted
                */
                setEntityDescriptorFromView(getNewEntityDescriptor());
                ActionEvent actionEvent = new ActionEvent(
                   this,ActionEvent.ACTION_PERFORMED,
                   DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString());
                this.myController.actionPerformed(actionEvent);
            }
            catch (StoreException ex){
               String message = ex.getMessage();
               displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
       }
    }
    
    private void doAppointmentCreateViewRequest(){
        /**
         * on receipt of APPOINTMENT_CREATE_VIEW_REQUEST
         * -- initialises NewEntityDescriptor with the collection of all patients on the system
         * -- launches the APPOINTMENT_CREATOR_EDITOR_VIEW for the selected appointment for update
         */
        Patients patients = null;
        initialiseNewEntityDescriptor();
        try{
            patients = new Patients();
            patients.read();
            serialisePatientsToEDCollection(patients);
            //Window window = SwingUtilities.windowForComponent(this.desktopView.getContentPane());
            View.setViewer(View.Viewer.APPOINTMENT_CREATOR_EDITOR_VIEW);
            this.view2 = View.factory(this, getNewEntityDescriptor(), this.desktopView);
            /**
             * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
             * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
             * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
             */
            ActionEvent actionEvent = new ActionEvent(
                   this,ActionEvent.ACTION_PERFORMED,
                   DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
            this.myController.actionPerformed(actionEvent);
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doAppointmentUpdateViewRequest(){
        /**
         * on receipt of APPOINTMENT_UPDATE_VIEW_REQUEST
         * -- on entry assumes EntityDescriptorFromView has already been initialised from the view's entity descriptor
         * -- launches the APPOINTMENT_CREATOR_EDITOR_VIEW for the selected appointment for update
         */
        Patients patients = null;
        if (getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey() != null){
            try{

                Appointment appointment = new Appointment(
                        getEntityDescriptorFromView().getRequest().
                                getAppointment().getData().getKey()).read();

                patients = new Patients();
                patients.read();
                initialiseNewEntityDescriptor();
                serialiseAppointmentToEDAppointment(appointment);
                serialisePatientsToEDCollection(patients);
                View.setViewer(View.Viewer.APPOINTMENT_CREATOR_EDITOR_VIEW);
                this.view2 = View.factory(this, getNewEntityDescriptor(), this.desktopView);
                /**
                 * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
                 * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
                 * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
                 */
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
                this.myController.actionPerformed(actionEvent);
            }
            catch (StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            } 
        }
    }
    
    private void doAppointmentsViewClosedRequest(){
        /**
         * APPOINTMENTS_VIEW_CLOSED
         */
        ActionEvent actionEvent = new ActionEvent(
               this,ActionEvent.ACTION_PERFORMED,
               DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
        this.myController.actionPerformed(actionEvent); 
    }
    
    private void doAppointmentsForDayRequest(ActionEvent e){
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        initialiseNewEntityDescriptor();
        LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
        Appointments appointments = null;
        try{
            getUpdatedAppointmentSlotsForDay(day);
            /*
            appointments = new Appointments();
            appointments.readForDay(day);

            //appointments.addAll(getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day));
            ArrayList<Appointment> appointmentSlotsForDay = 
                    getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day);
            appointments.clear();
            appointments.addAll(appointmentSlotsForDay);
            serialiseAppointmentsToEDCollection(appointments);
            */
            /**
             * fire event over to APPOINTMENT_SCHEDULE
             */
            pcSupport.addPropertyChangeListener(this.view);
            pcEvent = new PropertyChangeEvent(this,
                EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(this.view);
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            /*
            if (ex.getErrorType().toString().equals(Store.ExceptionType.UNDEFINED_DATABASE))
                JOptionPane.showInternalMessageDialog(desktopView.getContentPane(), message);
            else 
                displayErrorMessage(message,"AppointmentScheduleViewController error",JOptionPane.WARNING_MESSAGE);
            */
        }
    }
    
    private void doModalViewerActivated(){
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
        this.myController.actionPerformed(actionEvent);
    }
    
    private void doNonSurgeryDayScheduleViewRequest(){
        try{
            setNewEntityDescriptor(new EntityDescriptor());
            initialiseNewEntityDescriptor();
            getNewEntityDescriptor().setSurgeryDaysAssignment(new SurgeryDaysAssignment().readTheSurgeryDaysAssignment().get());
            View.setViewer(View.Viewer.NON_SURGERY_DAY_EDITOR_VIEW);
            this.view2 = View.factory(this, getNewEntityDescriptor(), desktopView); 
            /**
             * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
             * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
             * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
             */
            ActionEvent actionEvent = new ActionEvent(
                   this,ActionEvent.ACTION_PERFORMED,
                   DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
            this.myController.actionPerformed(actionEvent);
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doSurgeryDayScheduleViewRequest(){
        try{
            setNewEntityDescriptor(new EntityDescriptor());
            initialiseNewEntityDescriptor();
            getNewEntityDescriptor().setSurgeryDaysAssignment(new SurgeryDaysAssignment().readTheSurgeryDaysAssignment().get());
            View.setViewer(View.Viewer.SURGERY_DAY_EDITOR_VIEW);
            this.view2 = View.factory(this, getNewEntityDescriptor(), desktopView); 
            /**
             * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
             * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
             * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
             */
            ActionEvent actionEvent = new ActionEvent(
                   this,ActionEvent.ACTION_PERFORMED,
                   DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
            this.myController.actionPerformed(actionEvent);
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doPatientAppointmentContactViewRequest(ActionEvent e){
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        initialiseNewEntityDescriptor();
        LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
        Appointments appointments = null;
        try{
            appointments = new Appointments();
            appointments.readForDay(day);
            serialiseAppointmentsToEDCollection(appointments);
            View.setViewer(View.Viewer.SCHEDULE_CONTACT_DETAILS_VIEW);
            this.pacView = View.factory(this, getNewEntityDescriptor(), desktopView);
            this.desktopView.add(pacView);
            this.pacView.initialiseView();
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doEmptySlotScannerDialogRequest(ActionEvent e){
        /**
         * EMPTY_SLOT_SCANNER_DIALOG_REQUEST constructs an EmptySlotScanEditorModalViewer
         */
        View.setViewer(View.Viewer.EMPTY_SLOT_SCANNER_VIEW);
        this.view2 = View.factory(this, getNewEntityDescriptor(), desktopView);

        /**
         * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
         * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
         * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
         */
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
        this.myController.actionPerformed(actionEvent);
    }

    private void doPrimaryViewActionRequest(ActionEvent e){
        EntityDescriptor.AppointmentViewControllerActionEvent actionCommand =
               EntityDescriptor.AppointmentViewControllerActionEvent.valueOf(e.getActionCommand());
        switch (actionCommand){
            case APPOINTMENT_CANCEL_REQUEST:
                doAppointmentCancelRequest();
                break;
            case APPOINTMENT_CREATE_VIEW_REQUEST:
                doAppointmentCreateViewRequest();
                break;
            case APPOINTMENT_UPDATE_VIEW_REQUEST:
                doAppointmentUpdateViewRequest();
                break;
            case APPOINTMENTS_VIEW_CLOSED: 
                doAppointmentsViewClosedRequest();
                break;
            case APPOINTMENTS_FOR_DAY_REQUEST:
                doAppointmentsForDayRequest(e);
                break;
            case MODAL_VIEWER_ACTIVATED://notification from view uts shutting down
                doModalViewerActivated();
                break;
            case NON_SURGERY_DAY_SCHEDULE_VIEW_REQUEST:
                doNonSurgeryDayScheduleViewRequest();
                break;
            case SURGERY_DAYS_EDITOR_VIEW_REQUEST:
                doSurgeryDayScheduleViewRequest();
                break;
            case PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST:
                doPatientAppointmentContactViewRequest(e);
                break;
            case EMPTY_SLOT_SCANNER_DIALOG_REQUEST: 
                doEmptySlotScannerDialogRequest(e);
                break;     
        }
    }
    
    private void doSecondaryViewActionRequest(ActionEvent e){
        this.view2 = (View)e.getSource();
        setEntityDescriptorFromView(this.view2.getEntityDescriptor());
        View the_view = (View)e.getSource();
        switch(the_view.getMyViewType()){
            case APPOINTMENT_CREATOR_EDITOR_VIEW:
                doAppointmentCreatorEditorViewAction(e);
                break;
            case EMPTY_SLOT_SCANNER_VIEW:
                doEmptySlotScannerViewAction(e);
                break;
            case NON_SURGERY_DAY_EDITOR_VIEW:
                doNonSurgeryDayScheduleEditorViewAction(e);
                break;
            case SCHEDULE_CONTACT_DETAILS_VIEW:
                break;
            case SURGERY_DAY_EDITOR_VIEW:
                doSurgeryDaysEditorViewAction(e);
                break;
        }
    }
    
    private void doScheduleContactListView(ActionEvent e){
        //SCHEDULE_CONTACT_LIST_VIEW performs no actions currently
    }

    
    private void doNonSurgeryDayScheduleEditorViewAction(ActionEvent e){
        /**
         * Following is execution strategy
         * -- fetch the calling view's entity descriptor
         * -- close down the calling view
         * -- construct a new ActionEvent
         * ---- source = this.view (AppointmentsForDayView
         * ---- property = APPOINTMENTS_FOR_DAY_REQUEST
         * -- recursively call AppointmentViewController::actionPerformed() method
         * The latter call simulates the event raised when the date is updated on the AppointmentsForDayView object
         */
        if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.NON_SURGERY_DAY_SCHEDULE_EDIT_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            try{
                this.view2.setClosed(true);
            }
            catch (PropertyVetoException ex){
                String message = ex.getMessage() + "\n";
                message = message + "Error when closing down the NON_SURGERY_DAY_SCHEDULE_EDITOR view in AppointmentViewController::doSurgeryDaysEditorModalViewer()";
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
            initialiseNewEntityDescriptor();
            getNewEntityDescriptor().getRequest().setDay(
                    getEntityDescriptorFromView().getRequest().getDay());
            getNewEntityDescriptor().getRequest().setSurgeryDaysAssignmentValue(
                    getEntityDescriptorFromView().getRequest().getSurgeryDaysAssignmentValue());
            
            pcSupport.addPropertyChangeListener(view);
            pcEvent = new PropertyChangeEvent(this,
                EntityDescriptor.AppointmentViewControllerPropertyEvent.NON_SURGERY_DAY_EDIT_RECEIVED.toString(),
                getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            pcSupport.removePropertyChangeListener(view);
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){
            /**
             * DISABLE_CONTROLS_REQUEST requests DesktopViewController to disable menu options in its view
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
            this.myController.actionPerformed(actionEvent);     
        }
    }
    private void doSurgeryDaysEditorViewAction(ActionEvent e){
        if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.SURGERY_DAYS_EDIT_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            try{
                this.view2.setClosed(true);
            }
            catch (PropertyVetoException ex){
                String message = ex.getMessage() + "\n";
                message = message + "Error when closing down the SURGERY_DAYS_EDITOR view in AppointmentViewController::doSurgeryDaysEditorModalViewer()";
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }

            //SurgeryDaysValues surgeryDays = getEntityDescriptorFromView().getRequest().getSurgeryDays();
            HashMap<DayOfWeek,Boolean> surgeryDaysAssignmentValue = getEntityDescriptorFromView().getRequest().getSurgeryDaysAssignmentValue();
            try{
                SurgeryDaysAssignment surgeryDaysAssignment = new SurgeryDaysAssignment(surgeryDaysAssignmentValue);
                surgeryDaysAssignment.update();
                getEntityDescriptorFromView().setSurgeryDaysAssignment(new SurgeryDaysAssignment().readTheSurgeryDaysAssignment().get());
                /**
                 * fire event over to APPOINTMENT_SCHEDULE
                 */
                pcSupport.addPropertyChangeListener(view);
                pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.AppointmentViewControllerPropertyEvent.SURGERY_DAYS_UPDATE_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
                pcSupport.removePropertyChangeListener(view);
            }
            catch(StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){
            this.view2.initialiseView();
            /**
             * passes message to DesktopView Controller to disable the VIEW control
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
            this.myController.actionPerformed(actionEvent); 
            
        }
    }
    
    private void doEmptySlotScannerViewAction(ActionEvent e){
        if (e.getActionCommand().equals(EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_SLOTS_FROM_DATE_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            try{
                this.view2.setClosed(true);
                /**
                 * the modal JinternalFrame has closed
                 */
                
            }
            catch (PropertyVetoException ex){
                
            }
            initialiseNewEntityDescriptor();
            LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
            Duration duration = getEntityDescriptorFromView().getRequest().getDuration();
            Appointments appointments = null;
            try{
                appointments = new Appointments();
                appointments.readFromDay(day);
                if (appointments.isEmpty()){
                    JOptionPane.showMessageDialog(null, "No scheduled appointments from selected scan date (" + day.format(dmyFormat) + ")");
                }
                else{
                    ArrayList<Appointment> availableSlotsOfDuration =  
                            getAvailableSlotsOfDuration(
                                    appointments,duration,day);
                    serialiseAppointmentsToEDCollection(availableSlotsOfDuration);
                    /**
                     * fire event over to APPOINTMENT_SCHEDULE view
                     */
                    pcSupport.addPropertyChangeListener(view);
                    pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENT_SLOTS_FROM_DAY_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupport.firePropertyChange(pcEvent);
                    pcSupport.removePropertyChangeListener(view);
                    
                    /**
                     * re-enabling of desktop view menu is handled when the View factory returns from the view constructor
                     * -- note: a modal JInternalFrame constructor only returns on the closure of the JInternalFrame
                     * -- which includes also when the modal view is closed locally via a Cancel button selection by the user
                     * 
                     * Post processing of data received from the modal view must be done at a time signalled by an action request sent by the modal view to the controller, whilst the view's entity descriptor is still accessible.. 
                     */
                }
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){ 
            /**
             * DISABLE_CONTROLS_REQUEST requests DesktopViewController to disable menu options in its view
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
            this.myController.actionPerformed(actionEvent); 
            
        }
    }
    
    private Appointment doAppointmentCreateRequest(ActionEvent e, LocalDate day){
        Appointment result = null;
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        day = getEntityDescriptorFromView().getRequest().
                getAppointment().getData().getStart().toLocalDate();
        initialiseNewEntityDescriptor();
        try{
            result = requestToChangeAppointmentSchedule(ViewMode.CREATE); 
        }catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error on attempt to create a new appointment",JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private Appointment doAppointmentUpdateRequest(ActionEvent e, LocalDate day){
        Appointment result = null;
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        day = getEntityDescriptorFromView().getRequest().
                getAppointment().getData().getStart().toLocalDate();
        initialiseNewEntityDescriptor();
        try{
            result = requestToChangeAppointmentSchedule(ViewMode.UPDATE);
        }catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error on attempt to update an appointment",JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private void initialiseAppointmentSchedule(LocalDate day, Appointment result){
        Appointments appointments = null;
        try{
            this.view2.setClosed(true);
        }
        catch (PropertyVetoException ex){

        }
        /**
         * either an update or create appointment event has occurred, so clear empty slot list
         * fire event over to APPOINTMENT_SCHEDULE
         * -- note: this action will overwrite the APPOINTMENT_CREATE_EDIT_VIEW entity descriptor with that of APPOINTMENT_SCHEDULE_VIEW entity descriptor
         */
        initialiseNewEntityDescriptor();
        //
        pcSupport.addPropertyChangeListener(view);
        pcEvent = new PropertyChangeEvent(this,
            EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENT_SLOTS_FROM_DAY_RECEIVED.toString(),
            null,getNewEntityDescriptor());
        pcSupport.firePropertyChange(pcEvent);
        pcSupport.removePropertyChangeListener(this.view);
        /**
         * initialise entity descriptor
         * -- with new list of appointments and available slots for the day
         * -- as well as the appointment instance that's been created or updated
         */
        try{
            getUpdatedAppointmentSlotsForDay(day);
            /*
            appointments = new Appointments();
            appointments.readForDay(day); //throws StoreException

            ArrayList<Appointment> appointmentSlotsForDay = 
                    getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day);
            appointments.clear();
            appointments.addAll(appointmentSlotsForDay);
            serialiseAppointmentsToEDCollection(appointments);
            */
            /**
             * Added request to update ED with serialised new or updated appointment
             */
            serialiseAppointmentToEDAppointment(result);
            /**
             * fire event over to APPOINTMENT_SCHEDULE
             * -- note: this action will overwrite the APPOINTMENT_CREATE_EDIT_VIEW entity descriptor with that of APPOINTMENT_SCHEDULE_VIEW entity descriptor
             */
            pcSupport.addPropertyChangeListener(this.view);
            pcEvent = new PropertyChangeEvent(this,
                EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
            /**
             * send desktop view controller the APPOINTMENT_HISTORY_CHANGE_NOTIFICATION
             * -- note: important to restore view controller's EntityDescriptorFromView with entity descriptor fetched from APPOINTMENT_CREATE_EDIT_VIEW
             */
            setEntityDescriptorFromView(getNewEntityDescriptor());
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);
        }catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error on "
                    + "attempt to fetch from store appointments for a specific day",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void sendErrorToAppointmentCreatorEditorView(){
        /**
         * fire event over to APPOINTMENT_CREATOR_EDITOR_VIEW
         */
        pcSupport.addPropertyChangeListener(this.view2);
        pcEvent = new PropertyChangeEvent(this,
           EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENT_SCHEDULE_ERROR_RECEIVED.toString(),
           getOldEntityDescriptor(),getNewEntityDescriptor());
        pcSupport.firePropertyChange(pcEvent);
        pcSupport.removePropertyChangeListener(this.view2);
    }
    
    private void doAppointmentCreatorEditorViewAction(ActionEvent e){
        Appointment result = null;
        LocalDate day = null;
        EntityDescriptor.AppointmentViewControllerActionEvent actionCommand =
               EntityDescriptor.AppointmentViewControllerActionEvent.valueOf(e.getActionCommand());        
        switch (actionCommand){
            case APPOINTMENT_CREATE_REQUEST:
                day = getEntityDescriptorFromView().getRequest().getAppointment().
                        getData().getStart().toLocalDate();
                result = doAppointmentCreateRequest(e, day);
                if (result!=null) initialiseAppointmentSchedule(day, result);
                else sendErrorToAppointmentCreatorEditorView();
                break;
            case APPOINTMENT_UPDATE_REQUEST:
                day = getEntityDescriptorFromView().getRequest().getAppointment().
                        getData().getStart().toLocalDate();
                result = doAppointmentUpdateRequest(e, day);
                if (result!=null) initialiseAppointmentSchedule(day,result);
                else sendErrorToAppointmentCreatorEditorView();
                break;
            case MODAL_VIEWER_ACTIVATED:
                doModalViewerActivated();
                break;     
        } 
    }
    
    /**
     * on APPOINTMENT_VIEW_CREATE_REQUEST & APPOINTMENT_VIEW_UPDATE_REQUEST
     * -- the view controller expects appointee data in EntityDescriptor.Request.Patient and start, duration and notes in EntityDescriptor.Request.Appointment
     * @param e ActionEvent
     */
    private void doAppointmentCreatorEditorViewActionx(ActionEvent e){
        Appointment result = null;
        LocalDate day = null;
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        EntityDescriptor edFromAppointmentCreateEditView = getEntityDescriptorFromView();
        try{
            if (e.getActionCommand().equals(EntityDescriptor.AppointmentViewDialogActionEvent.
                    APPOINTMENT_VIEW_CREATE_REQUEST.toString())){
                setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
                day = getEntityDescriptorFromView().getRequest().
                        getAppointment().getData().getStart().toLocalDate();
                initialiseNewEntityDescriptor();
                result = requestToChangeAppointmentSchedule(ViewMode.CREATE); 
            }
            else if (e.getActionCommand().equals(EntityDescriptor.AppointmentViewDialogActionEvent.
                    APPOINTMENT_VIEW_UPDATE_REQUEST.toString())){
                setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
                day = getEntityDescriptorFromView().getRequest().
                        getAppointment().getData().getStart().toLocalDate();
                initialiseNewEntityDescriptor();
                result = requestToChangeAppointmentSchedule(ViewMode.UPDATE);
            }
            else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){ 
            /**
             * DISABLE_CONTROLS_REQUEST requests DesktopViewController to disable menu options in its view
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
            this.myController.actionPerformed(actionEvent); 
            
            }
            /**
             * if current view request is not a DISABLE_DESKTOP_CONTROLS_REQUEST
             * -- process "result" variable
             * ---- if "result" not null
             * ------ close APPOINTMENT_CREATOR_EDITOR_VIEW (this.view2)
             * ------ fire updated appointment list to APPOINTMENT_SCHEDULE view (this.view) as a property change APPOINTMENTS_FOR_DAY_RECEIVED event
             * ------ fire a blank list of appointment slots to APPOINTMENT_SCHEDULE view as a property change APPOINTMENTS_FOR_DAY_RECEIVED event
             * ---- else fire error message to APPOINTMENT_CREATOR_EDITOR_VIEW to report APPOINTMENT_VIEW_ERROR as a property change event
             */
            Appointments appointments = null;
            if (!e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){
                if (result!=null){
                    try{
                        this.view2.setClosed(true);
                    }
                    catch (PropertyVetoException ex){

                    }
                    
                    /**
                     * either an update or create appointment event has occurred, so clear empty slot list
                     * fire event over to APPOINTMENT_SCHEDULE
                     * -- note: this action will overwrite the APPOINTMENT_CREATE_EDIT_VIEW entity descriptor with that of APPOINTMENT_SCHEDULE_VIEW entity descriptor
                     */
                    initialiseNewEntityDescriptor();
                    pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENT_SLOTS_FROM_DAY_RECEIVED.toString(),
                        null,getNewEntityDescriptor());
                    pcSupport.firePropertyChange(pcEvent);
                    pcSupport.removePropertyChangeListener(this.view);
                    
                    /**
                     * initialise entity descriptor
                     * -- with new list of appointments and available slots for the day
                     * -- as well as the fully instanciated appointment that's been created or updated
                     */
                    appointments = new Appointments();
                    appointments.readForDay(day);
                    appointments.addAll(getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day));
                    serialiseAppointmentsToEDCollection(appointments);
                    /**
                     * Added request to update ED with serialised new or updated appointment
                     */
                    serialiseAppointmentToEDAppointment(result);
                    
                    /**
                     * fire event over to APPOINTMENT_SCHEDULE
                     * -- note: this action will overwrite the APPOINTMENT_CREATE_EDIT_VIEW entity descriptor with that of APPOINTMENT_SCHEDULE_VIEW entity descriptor
                     */
                    pcSupport.addPropertyChangeListener(this.view);
                    pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupport.firePropertyChange(pcEvent);

                    
                    /**
                     * send desktop view controller the APPOINTMENT_HISTORY_CHANGE_NOTIFICATION
                     * -- note: important to restore view controller's EntityDescriptorFromView with entity descriptor fetched from APPOINTMENT_CREATE_EDIT_VIEW
                     */
                    setEntityDescriptorFromView(getNewEntityDescriptor());
                    ActionEvent actionEvent = new ActionEvent(
                            this,ActionEvent.ACTION_PERFORMED,
                            DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString());
                    this.myController.actionPerformed(actionEvent);

                }
                else{
                    /**
                     * fire event over to APPOINTMENT_CREATOR_EDITOR_VIEW
                     */
                    pcSupport.addPropertyChangeListener(this.view2);
                    pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.AppointmentViewDialogPropertyEvent.APPOINTMENT_VIEW_ERROR.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupport.firePropertyChange(pcEvent);
                    pcSupport.removePropertyChangeListener(this.view2);
                }
            }
            
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doAppointmentScheduleViewAction(ActionEvent e){
        if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.
                        APPOINTMENTS_VIEW_CLOSED.toString())){
            /**
             * APPOINTMENTS_VIEW_CLOSED
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);   
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString())){ 
           //this.view2.initialiseView();
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.NON_SURGERY_DAY_SCHEDULE_VIEW_REQUEST.toString())){
            try{
                setNewEntityDescriptor(new EntityDescriptor());
                initialiseNewEntityDescriptor();
                getNewEntityDescriptor().setSurgeryDaysAssignment(new SurgeryDaysAssignment().readTheSurgeryDaysAssignment().get());
                View.setViewer(View.Viewer.NON_SURGERY_DAY_EDITOR_VIEW);
                this.view2 = View.factory(this, getNewEntityDescriptor(), desktopView); 
                /**
                 * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
                 * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
                 * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
                 */
                ActionEvent actionEvent = new ActionEvent(
                       this,ActionEvent.ACTION_PERFORMED,
                       DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
                this.myController.actionPerformed(actionEvent);
            }
            catch (StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        else if (e.getActionCommand().equals(
                EntityDescriptor.AppointmentViewControllerActionEvent.SURGERY_DAYS_EDITOR_VIEW_REQUEST.toString())){
            try{
                setNewEntityDescriptor(new EntityDescriptor());
                initialiseNewEntityDescriptor();
                getNewEntityDescriptor().setSurgeryDaysAssignment(new SurgeryDaysAssignment().readTheSurgeryDaysAssignment().get());
                View.setViewer(View.Viewer.SURGERY_DAY_EDITOR_VIEW);
                this.view2 = View.factory(this, getNewEntityDescriptor(), desktopView); 
                /**
                 * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
                 * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
                 * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
                 */
                ActionEvent actionEvent = new ActionEvent(
                       this,ActionEvent.ACTION_PERFORMED,
                       DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
                this.myController.actionPerformed(actionEvent);
            }
            catch (StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        else if (e.getActionCommand().equals(   
            ViewController.PatientAppointmentContactListViewControllerActionEvent.PATIENT_APPOINTMENT_CONTACT_VIEW_CLOSED.toString())){

        }
        
        else if (e.getActionCommand().equals(   
            EntityDescriptor.AppointmentViewControllerActionEvent.PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            initialiseNewEntityDescriptor();
            LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
            Appointments appointments = null;
            try{
                appointments = new Appointments();
                appointments.readForDay(day);
                serialiseAppointmentsToEDCollection(appointments);
                View.setViewer(View.Viewer.SCHEDULE_CONTACT_DETAILS_VIEW);
                this.pacView = View.factory(this, getNewEntityDescriptor(), desktopView);
                this.desktopView.add(pacView);
                this.pacView.initialiseView();
            }
            catch (StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        
        else if (e.getActionCommand().equals(   
                EntityDescriptor.AppointmentViewControllerActionEvent.EMPTY_SLOT_SCANNER_DIALOG_REQUEST.toString())){
            /**
             * EMPTY_SLOT_SCANNER_DIALOG_REQUEST constructs an EmptySlotScanEditorModalViewer
             */
            View.setViewer(View.Viewer.EMPTY_SLOT_SCANNER_VIEW);
            this.view2 = View.factory(this, getNewEntityDescriptor(), desktopView);
            
            /**
             * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
             * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
             * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
            this.myController.actionPerformed(actionEvent);
            
        }
        else if (e.getActionCommand().equals(
            /**
             * APPOINTMENTS_REQUEST would be sent by view on a change of the selected day
             */
            EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            initialiseNewEntityDescriptor();
            LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
            Appointments appointments = null;
            try{
                appointments = new Appointments();
                appointments.readForDay(day);
                appointments.addAll(getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day));
                serialiseAppointmentsToEDCollection(appointments);
                /**
                 * fire event over to APPOINTMENT_SCHEDULE
                 */
                pcSupport.addPropertyChangeListener(this.view);
                pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
                pcSupport.removePropertyChangeListener(this.view);
            }
            catch (StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                if (ex.getErrorType().toString().equals(Store.ExceptionType.UNDEFINED_DATABASE))
                    JOptionPane.showInternalMessageDialog(desktopView.getContentPane(), message);
                else 
                    displayErrorMessage(message,"AppointmentScheduleViewController error",JOptionPane.WARNING_MESSAGE);
                */
            }
        }
        else if (e.getActionCommand().equals(EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_UPDATE_VIEW_REQUEST.toString())){
            /**
             * on receipt of APPOINTMENT_UPDATE_VIEW_REQUEST
             * -- on entry assumes EntityDescriptorFromView has already been initialised from the view's entity descriptor
             * -- launches the APPOINTMENT_CREATOR_EDITOR_VIEW for the selected appointment for update
             */
            Patients patients = null;
            if (getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey() != null){
                try{
                    
                    Appointment appointment = new Appointment(
                            getEntityDescriptorFromView().getRequest().
                                    getAppointment().getData().getKey()).read();
                    
                    patients = new Patients();
                    patients.read();
                    initialiseNewEntityDescriptor();
                    serialiseAppointmentToEDAppointment(appointment);
                    serialisePatientsToEDCollection(patients);
                    View.setViewer(View.Viewer.APPOINTMENT_CREATOR_EDITOR_VIEW);
                    this.view2 = View.factory(this, getNewEntityDescriptor(), this.desktopView);
                    /**
                     * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
                     * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
                     * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
                     */
                    ActionEvent actionEvent = new ActionEvent(
                            this,ActionEvent.ACTION_PERFORMED,
                            DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
                    this.myController.actionPerformed(actionEvent);
                }
                catch (StoreException ex){
                    String message = ex.getMessage();
                    displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
                } 
            }
        }
        else if (e.getActionCommand().equals(EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_CREATE_VIEW_REQUEST.toString())){
            /**
             * on receipt of APPOINTMENT_CREATE_VIEW_REQUEST
             * -- initialises NewEntityDescriptor with the collection of all patients on the system
             * -- launches the APPOINTMENT_CREATOR_EDITOR_VIEW for the selected appointment for update
             */
            Patients patients = null;
            initialiseNewEntityDescriptor();
            try{
                patients = new Patients();
                patients.read();
                serialisePatientsToEDCollection(patients);
                //Window window = SwingUtilities.windowForComponent(this.desktopView.getContentPane());
                View.setViewer(View.Viewer.APPOINTMENT_CREATOR_EDITOR_VIEW);
                this.view2 = View.factory(this, getNewEntityDescriptor(), this.desktopView);
                /**
                 * ENABLE_CONTROLS_REQUEST requests DesktopViewController to enable menu options in its view
                 * -- note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
                 * -- at which stage its appropriate to re-enable the View menu on the Desktop View Controller's view
                 */
                ActionEvent actionEvent = new ActionEvent(
                       this,ActionEvent.ACTION_PERFORMED,
                       DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
                this.myController.actionPerformed(actionEvent);
            }
            catch (StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }
        
        else if (e.getActionCommand().equals(EntityDescriptor.AppointmentViewControllerActionEvent.APPOINTMENT_CANCEL_REQUEST.toString())){
            /**
             * on receipt of APPOINTMENT_CANCEL_REQUEST
             * -- assumes on entry EntityDescriptorFromView has been initialised (by view)
             * -- hence: getRequest.getPatient() returns the patient whose appointment has been cancelled
             */
            if (getEntityDescriptorFromView().getRequest().getAppointment().getAppointee().getData().getKey()!=null){
                
                try{
                    /**
                     * The requested appointment is read into memory from the store using the specified appointment key
                     * -- this means the appointee (patient) object is encapsulated in the appointment object
                     * -- knowledge of the patient object is required to maintain consistency between views, which is a responsibility of the desktop view controller
                     * 
                     */
                    Appointment appointment = new Appointment(
                        getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey()).read();
                    appointment.delete();
                    /**
                     * two updates to a new Entity Descriptor required
                     * -- serialise appointments derived from appointments and empty slots for the day
                     * -- serialise the appointment which has just been delete (required for post processing by DesktopViewController)
                     */
                    Appointments appointments = null;
                    LocalDate day = getEntityDescriptorFromView().
                            getRequest().getAppointment().getData().getStart().toLocalDate();
                    initialiseNewEntityDescriptor();
                    appointments = new Appointments();
                    appointments.readForDay(day);
                    appointments.addAll(getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day));
                    serialiseAppointmentsToEDCollection(appointments);
                    /**
                     * appointment serialisation to ED ensures appointee key is accessible by the desktop view controller
                     */
                    serialiseAppointmentToEDAppointment(appointment);
                    /**
                     * fire event over to APPOINTMENT_SCHEDULE
                     */ 
                    pcSupport.addPropertyChangeListener(this.view);
                    pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupport.firePropertyChange(pcEvent);
                    pcSupport.removePropertyChangeListener(this.view);
                    /**
                     * send APPOINTMENT_HISTORY_CHANGE_NOTIFICATION to Desktop view controller
                     * -- prime the appointment view controller's entity description with the patient that has just been deleted
                     * -- i.e so view controller can reliably let the desktop view controller know which patient has had an appointment deleted
                     */
                    setEntityDescriptorFromView(getNewEntityDescriptor());
                    ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewController.DesktopViewControllerActionEvent.APPOINTMENT_HISTORY_CHANGE_NOTIFICATION.toString());
                    this.myController.actionPerformed(actionEvent);
                }
                catch (StoreException ex){
                    String message = ex.getMessage();
                    displayErrorMessage(message,"AppointmentViewController error",JOptionPane.WARNING_MESSAGE);
                }
            }
        }   
    }
    private void doDesktopViewControllerAction(ActionEvent e){
        if (e.getActionCommand().equals(DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString())){
            try{
                /**
                 * view will message view controller when view is closed 
                 */
                getView().setClosed(true);
            }
            catch (PropertyVetoException ex){
                //UnspecifiedError action
            }
        }
    }
    
    private String getNameOfSlotOwnerPlusSlotStart(Appointment slot){
        String result = getNameOfSlotOwner(slot);
        LocalTime start = slot.getStart().toLocalTime();
        result = result + " which starts at " + start.format(DateTimeFormatter.ofPattern("HH:mm"));
        return result;
    }
  
    private String getNameOfSlotOwner(Appointment slot){
        String title;
        String forenames;
        String surname;
        
        title = slot.getPatient().getName().getTitle();
        forenames = slot.getPatient().getName().getForenames();
        surname = slot.getPatient().getName().getSurname();
        //result = slot.getPatient().getName().getTitle();
        /*
        if (title.strip().length()==0) title = "?";
        if (forenames.strip().length() == 0) forenames = "<...>";
        if (surname.strip().length() == 0) surname = "<...>";
        */
        if (title.length()==0) title = "?";
        if (forenames.length() == 0) forenames = "<...>";
        if (surname.length() == 0) surname = "<...>";
       
        return title + " " + forenames + " " + surname;
    }
    
    /**
     * method identifies if requested slot can be added to schedule or not
     * @param rSlot Appointment, the requested appointment
     * @param appointments ArrayList of scheduled appointments for the day in question
     * @param mode ViewMode, determines if request is CREATE a new appointment or UPDATE a scheduled appointment
     * @return String, contains error message if a collision occurs, else null
     */
    private String appointmentCollisionChangingSchedule(
            Appointment rSlot, 
            ArrayList<Appointment> appointments, ViewMode mode){
        String result = null;
        LocalDateTime sSlotEnd;
        LocalDateTime rSlotEnd = rSlot.getStart().plusMinutes(rSlot.getDuration().toMinutes());
        Iterator<Appointment> it = appointments.iterator();
        RequestedAppointmentState state = RequestedAppointmentState.STARTS_AFTER_PREVIOUS_SLOT;
        while(it.hasNext()){
            Appointment sSlot = it.next();
            sSlotEnd = sSlot.getStart().plusMinutes(sSlot.getDuration().toMinutes());
            switch (state){
                case STARTS_AFTER_PREVIOUS_SLOT:
                    if(!rSlotEnd.isAfter(sSlot.getStart())){
                        /**
                         * requested slot is prior to scheduled slot so can add to schedule
                         */
                        result = null;
                        state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                        break;
                    }
                    else if (!rSlot.getStart().isBefore(sSlotEnd)){
                        /**
                         * --requested slot starts after end of scheduled slot
                         * --do nothing, state remains the same
                         */
                    }
                    else {//must mean rSlot overlaps sSlot
                        switch (mode){
                            case CREATE:
                                /**
                                 * --rSlot overlaps scheduled appointment so cannot be created 
                                 * --abort attempt to create a new appointment and post error
                                 */
                                state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                                result = 
                                        "The new appointment for " + getNameOfSlotOwner(rSlot)
                                        + " overwrites existing appointment for " + getNameOfSlotOwnerPlusSlotStart(sSlot);
                                break;
                            case UPDATE:
                                /**
                                 * requested slot starts before schedule lot ends
                                 */
                                if (!rSlot.getKey().equals(sSlot.getKey())){
                                    /**
                                     * --requested slot and scheduled slot are different appointment records
                                     * --abort attempt to update requested slot and register error
                                     */
                                    state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                                    result = 
                                            "The new appointment for " + getNameOfSlotOwner(rSlot)
                                            + " overwrites existing appointment for " + getNameOfSlotOwnerPlusSlotStart(sSlot);
                                    break;
                                }
                                else if (rSlotEnd.isAfter(sSlotEnd)){
                                    /**
                                     * --requested slot is an updated version of scheduled slot 
                                     * --starts before the end of the scheduled slot
                                     * --and ends after end scheduled slot
                                     * --change state accordingly
                                     */
                                    state = RequestedAppointmentState.ENDS_AFTER_PREVIOUS_SLOT;
                                }
                                else{
                                    /**
                                     * --requested slot is an updated version of scheduled slot
                                     * --and starts and ends before or by the end of the scheduled slot
                                     * --permit update
                                     */
                                    result = null;
                                    break;
                                }
                                break;
                        }
                    }
                    break;
                case ENDS_AFTER_PREVIOUS_SLOT:
                    /**
                     * --requested slot is an updated version of the previous slot to scheduled appointment
                     */
                   if (!rSlotEnd.isAfter(sSlot.getStart())){
                       /**
                        * --end of requested slot is before the start of the scheduled slot
                        * --means requested slot update is allowed since it doesn't overlap next scheduled slot
                        */
                       result = null;
                       state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                       break;
                   }
                   else{
                        /**
                         * --requested slot ends after the start of the scheduled slot 
                         * --but requested slot cannot be an updated version of 2 separate appointment slots
                         * --abort update process
                         */
                        state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                        result = 
                                            "The new appointment for " + getNameOfSlotOwner(rSlot)
                                            + " overwrites existing appointment for " + getNameOfSlotOwnerPlusSlotStart(sSlot);
                        //result = 
                        //        "Attempt to overwrite two separate appointments disallowed";
                        break;
                   }

            }
        }
        return result;   
    }
    
    /**
     * Attempts to CREATE/UPDATE an appointment according to ED request
     * @param mode:ViewMode; either an appointment CREATE or UPDATE 
     * @return Appointment if the CREATE or UPSATE is successful; else null if not
     * @throws StoreException 
     */
    private Appointment requestToChangeAppointmentSchedule(ViewMode mode) throws StoreException{
        String error;
        Appointment result = null;
        Appointment rSlot = makeAppointmentFromEDRequest();
        LocalDate day = rSlot.getStart().toLocalDate();
        
        //NOTE: changed "appointments" to "appts" because former hid a previous definition of "appointments"
        Appointments appts = new Appointments();
        appts.readForDay(day);
        if (appts.isEmpty()){
            /**
             * no appointments for selected day so go head and CREATE appointment (no appointment to UPGRADE)
             */
            switch (mode){
                case CREATE:
                    rSlot.insert();
                    result = rSlot.read();
                    break;
                case UPDATE://this shouldn't be ever the case
                    rSlot.update();
                    result = rSlot.read();
                    break;
            }
        }
        else{
            switch (mode){//one or more appointmwent already exist so check the CREATE or UPGRADE make sense
                case CREATE:
                    error = appointmentCollisionChangingSchedule(rSlot, appts, mode);
                    getNewEntityDescriptor().setError(error);
                    if (error==null){
                        //no collision results
                        rSlot.insert(); //was rslot.create()
                        result = rSlot.read();
                    }
                    break;
                case UPDATE:
                    error = appointmentCollisionChangingSchedule(rSlot, appts, mode);
                    getNewEntityDescriptor().setError(error);
                    if (error==null){
                        //no collision results
                        rSlot.update();
                        result = rSlot.read();
                    }
                    break;
            }
        }
        return result;
    }

    /**
     * 
     * @param appointments all recorded appointments from a selected date 
     * @param duration minimum duration of an appointment slot to be included
     * @return ArrayList<Appointment> slots which meet specified minimum duration 
     */
    private ArrayList<Appointment> getAvailableSlotsOfDuration(
            ArrayList<Appointment> appointments,Duration duration, LocalDate searchStartDay){
        
        ArrayList<Appointment> result = new ArrayList<>();
        ArrayList<Appointment> appointmentsForSingleDay = new ArrayList<>();
        ArrayList<ArrayList<Appointment>> appointmentsGroupedByDay = new ArrayList<>();
        LocalDate currentDate = null;
        Iterator<Appointment> it = appointments.iterator();
        while(it.hasNext()){
            Appointment appointment = it.next();
            if (currentDate==null) currentDate = appointment.getStart().toLocalDate();
            if (appointment.getStart().toLocalDate().equals(currentDate)) appointmentsForSingleDay.add(appointment);
            else {
                appointmentsGroupedByDay.add(appointmentsForSingleDay);
                currentDate = appointment.getStart().toLocalDate();
                appointmentsForSingleDay = new ArrayList<>();
                appointmentsForSingleDay.add(appointment);
            }
        }
        appointmentsGroupedByDay.add(appointmentsForSingleDay);
        Iterator<ArrayList<Appointment>> it1 = appointmentsGroupedByDay.iterator();
        //appointmentsForSingleDay.clear();
        
        /**
         * -- current search date initialised to start day of search
         * -- for each collection of appointments for a given day (appointmentsForSingleDay)
         * ----- if current search date is prior to this appointmentsForSingleDay and search date is a practice day
         * ------- create an empty slot for this day and add to collection of empty slots (search result)
         * ------- increment current search date
         * ----- else 
         * ------- get slots for this day (empty and non empty) 
         * ------- iterate through these 
         * ---------- for empty slots that >= specified duration and add to result of scan
         * ------- increment current search date
         * -- 
         * ------ and current day is a practice day 
         * ------ create a new appointmentsForSingleDay with single empty slot for whole day and add to result (collection of empty slots)
         * ------ increment current day
         * ----process this day group of appts (adding any unbooked slots) adding to search result if duration permits
         */
        currentDate = searchStartDay;
        while(it1.hasNext()){
            appointmentsForSingleDay = it1.next();
            LocalDate appointmentsForSingleDayDate = appointmentsForSingleDay.get(0).getStart().toLocalDate();
            while(currentDate.isBefore(appointmentsForSingleDayDate)){
                if(currentDate.getDayOfWeek().equals(DayOfWeek.TUESDAY) 
                            || currentDate.getDayOfWeek().equals(DayOfWeek.THURSDAY)
                            || currentDate.getDayOfWeek().equals(DayOfWeek.FRIDAY))
                        result.add(this.createEmptyAppointmentSlot(
                              currentDate.atTime(ViewController.FIRST_APPOINTMENT_SLOT))); 
                currentDate = currentDate.plusDays(1); 
            }
            ArrayList<Appointment> slotsForDay = 
                    getAppointmentsForSelectedDayIncludingEmptySlots(
                            appointmentsForSingleDay, appointmentsForSingleDayDate); 
            Iterator<Appointment> it2 = slotsForDay.iterator();
            //currentDate = null;
            while(it2.hasNext()){
                Appointment slot = it2.next();
                if (slot.getStatus().equals(Appointment.Status.UNBOOKED)){
                    long slotDuration = slot.getDuration().toMinutes();
                    if (slotDuration >= duration.toMinutes()){
                        result.add(slot);
                    }
                }
            } 
            currentDate = currentDate.plusDays(1);
        } 
        /**
         * if scan duration == all day (8 hours)
         * 
         * else
         * -- check and process days which have no appointments on, as follows
         *   -- consecutive appointment-less days are merged into a single slot 
         *   -- the single slot duration represents in hours the number of consecutive days
         */
        
        boolean multiDayIntervalHasStarted = false;
        Appointment multiDayIntervalWithNoAppointments = null;
        ArrayList<Appointment> finalisedResult = new ArrayList<>();
        //LocalDate thisDate = result.get(0).getStart().toLocalDate();
        it = result.iterator();
        int count = 0;
        
        if (duration.toHours()==8){//empty slot scan duration is all day
            while (it.hasNext()){
                count = count + 1;
                if (count == 23){
                    count = 19;                    
                }
                Appointment appointment = it.next();
                if (finalisedResult.isEmpty()&&multiDayIntervalWithNoAppointments==null){//start of procedure on entry
                    //multiDayIntervalHasStarted = true;
                    multiDayIntervalWithNoAppointments = new Appointment();
                    multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                    multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                    multiDayIntervalWithNoAppointments.setStatus(Appointment.Status.UNBOOKED);
                }
                else{
                    //LocalDate appointmentDate = appointment.getStart().toLocalDate();
                    if (areTheseSlotsOnConsecutivePracticeDays(multiDayIntervalWithNoAppointments,appointment)){
                        if (multiDayIntervalWithNoAppointments!=null){
                            Duration d = multiDayIntervalWithNoAppointments.getDuration();
                            multiDayIntervalWithNoAppointments.setDuration(d.plusHours(8));
                        }
                    }
                    else{
                        if (multiDayIntervalWithNoAppointments!=null){
                            Duration d = multiDayIntervalWithNoAppointments.getDuration();
                            multiDayIntervalWithNoAppointments.setDuration(d.plusHours(8));
                            finalisedResult.add(multiDayIntervalWithNoAppointments);
                            multiDayIntervalWithNoAppointments = new Appointment();
                            multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                            multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                            multiDayIntervalWithNoAppointments.setStatus(Appointment.Status.UNBOOKED);
                        }
                    }
                }
            }
            if (multiDayIntervalWithNoAppointments!=null){
                Duration d = multiDayIntervalWithNoAppointments.getDuration();
                multiDayIntervalWithNoAppointments.setDuration(d.plusHours(8));
                finalisedResult.add(multiDayIntervalWithNoAppointments);
            }
        }
        else{// this is not a scan of all day slots
            while(it.hasNext()){
                Appointment appointment = it.next();
                /*
                if (appointment.getStart().toLocalDate().isEqual(LocalDate.of(2021,7, 16))){
                    LocalDate test = appointment.getStart().toLocalDate();
                }
                */
                if (appointment.getDuration().toHours() == 8){
                    //WHAT HAPPENS WHEN APPOINTMENT CHANGES MULTIDAYINTERVALHASSTARTED SLOT
                    if (!multiDayIntervalHasStarted) {
                        multiDayIntervalHasStarted = true;
                        multiDayIntervalWithNoAppointments = new Appointment();
                        multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                        multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                        multiDayIntervalWithNoAppointments.setStatus(Appointment.Status.UNBOOKED);
                    }
                    else if (areTheseSlotsOnConsecutivePracticeDays(
                            multiDayIntervalWithNoAppointments,appointment)){
                            if (multiDayIntervalWithNoAppointments!=null){
                                duration = multiDayIntervalWithNoAppointments.getDuration();
                                multiDayIntervalWithNoAppointments.setDuration(duration.plusHours(8));
                            }
                    }
                    else{
                        if (multiDayIntervalWithNoAppointments!=null){
                            Duration d = multiDayIntervalWithNoAppointments.getDuration();
                            multiDayIntervalWithNoAppointments.setDuration(d.plusHours(8));
                            finalisedResult.add(multiDayIntervalWithNoAppointments);
                            multiDayIntervalWithNoAppointments = new Appointment();
                            multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                            multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                            multiDayIntervalWithNoAppointments.setStatus(Appointment.Status.UNBOOKED);
                        }
                    }
                }
                else if (multiDayIntervalHasStarted){
                    if (multiDayIntervalWithNoAppointments!=null){
                        Duration d = multiDayIntervalWithNoAppointments.getDuration();
                        multiDayIntervalWithNoAppointments.setDuration(d.plusHours(8));
                        finalisedResult.add(multiDayIntervalWithNoAppointments);
                        multiDayIntervalHasStarted = false;
                        finalisedResult.add(appointment);
                    }
                }
                else finalisedResult.add(appointment);  
            } 
            if (multiDayIntervalHasStarted){
                if (multiDayIntervalWithNoAppointments!=null){
                    Duration d = multiDayIntervalWithNoAppointments.getDuration();
                    multiDayIntervalWithNoAppointments.setDuration(d.plusHours(8));
                    finalisedResult.add(multiDayIntervalWithNoAppointments);
                }
            }
        }
        
        return finalisedResult;
    }
    private LocalDate getPracticeDayOnWhichSlotEnds(Appointment slot){
        
        long intervalHours = slot.getDuration().toHours();
        long intervalDays = intervalHours/8;
        LocalDate currentDate = slot.getStart().toLocalDate();
        for (int index = 0; index < intervalDays ; index ++){
            do{
                currentDate = currentDate.plusDays(1);
            }
            while(!isValidDay(currentDate));
        }
        return currentDate;
    }
    private boolean isValidDay(LocalDate day){
        return(day.getDayOfWeek().equals(DayOfWeek.TUESDAY) 
                            || day.getDayOfWeek().equals(DayOfWeek.THURSDAY)
                            || day.getDayOfWeek().equals(DayOfWeek.FRIDAY));
    }
    private boolean areTheseSlotsOnConsecutivePracticeDays(Appointment slot1, Appointment slot2){
        boolean result = false;
        LocalDate d1 = getPracticeDayOnWhichSlotEnds(slot1);
        LocalDate d2 = slot2.getStart().toLocalDate();
        LocalDate nextPracticeDay = d1;
        do{
            nextPracticeDay = nextPracticeDay.plusDays(1);
            
        }while (nextPracticeDay.getDayOfWeek()==DayOfWeek.SATURDAY||
                nextPracticeDay.getDayOfWeek()==DayOfWeek.SUNDAY||
                nextPracticeDay.getDayOfWeek()==DayOfWeek.MONDAY||
                nextPracticeDay.getDayOfWeek()==DayOfWeek.WEDNESDAY);
        if (nextPracticeDay.isEqual(d2)){
            result = true;
        }
        return result;
    }
    
    /*
    private boolean areTheseDatesConsecutivePracticeDays(LocalDate d1, LocalDate d2){
        boolean result = false;
        LocalDate nextDay = d1;
        do{
            nextDay = nextDay.plusDays(1);
            
        }while (nextDay.getDayOfWeek()==DayOfWeek.SATURDAY||
                nextDay.getDayOfWeek()==DayOfWeek.SUNDAY||
                nextDay.getDayOfWeek()==DayOfWeek.MONDAY||
                nextDay.getDayOfWeek()==DayOfWeek.WEDNESDAY);
        if (nextDay.isEqual(d2)){
            result = true;
        }
        return result;
    }
    */
    
    private ArrayList<Appointment> getAppointmentsForSelectedDayIncludingEmptySlots(
            ArrayList<Appointment> appointments, LocalDate day) {
        LocalDateTime nextEmptySlotStartTime;
        nextEmptySlotStartTime = LocalDateTime.of(day, 
                                                  ViewController.FIRST_APPOINTMENT_SLOT);
        ArrayList<Appointment> apptsForDayIncludingEmptySlots = new ArrayList<>();      
        Iterator<Appointment> it = appointments.iterator();
        /**
         * check for no appointments on this day if no appointment create a
         * single empty slot for whole day
         */
        if (appointments.isEmpty()) {
            apptsForDayIncludingEmptySlots.add(createEmptyAppointmentSlot(
                                                nextEmptySlotStartTime));
        } 
        /**
         * At least one appointment scheduled, calculate empty slot intervals
         * interleaved appropriately (time ordered) with scheduled
         * appointment(s)
         */
        else { 
            while (it.hasNext()) {
                Appointment appointment = it.next();
                Duration durationToNextSlot = Duration.between(
                        nextEmptySlotStartTime,appointment.getStart() );
                /**
                 * check if no time exists between next scheduled appointment
                 * If so update nextEmptySlotStartTime to immediately follow
                 * the current scheduled appointment
                 */
                if (durationToNextSlot.isZero()) {
                    nextEmptySlotStartTime = 
                            appointment.getStart().plusMinutes(appointment.getDuration().toMinutes());
                    apptsForDayIncludingEmptySlots.add(appointment);
                } 
                /**
                 * If time exists between nextEmptySlotTime and the current 
                 * appointment,
                 * -- create an empty appointment slot to fill the gap
                 * -- re-initialise nextEmptySlotTime to immediately follow the
                 *    the current appointment
                 */
                else {
                    Appointment emptySlot = createEmptyAppointmentSlot(nextEmptySlotStartTime,
                            Duration.between(nextEmptySlotStartTime, appointment.getStart()).abs());
                    apptsForDayIncludingEmptySlots.add(emptySlot);
                    apptsForDayIncludingEmptySlots.add(appointment);
                    nextEmptySlotStartTime =
                            appointment.getStart().plusMinutes(appointment.getDuration().toMinutes());
                }
            }
        }
        Appointment lastAppointment = 
                apptsForDayIncludingEmptySlots.get(apptsForDayIncludingEmptySlots.size()-1);
        if (lastAppointment.getStatus().equals(Appointment.Status.BOOKED)){
            //check if bookable time after last appointment
            Duration durationToDayEnd = 
                    Duration.between(nextEmptySlotStartTime.toLocalTime(), ViewController.LAST_APPOINTMENT_SLOT).abs();
            if (!durationToDayEnd.isZero()) {
                Appointment emptySlot = createEmptyAppointmentSlot(nextEmptySlotStartTime);
                apptsForDayIncludingEmptySlots.add(emptySlot);
            }
        }
        return apptsForDayIncludingEmptySlots;
    }
    private Appointment createEmptyAppointmentSlot(LocalDateTime start){
        Appointment appointment = new Appointment();
        appointment.setPatient(null);
        appointment.setStart(start);
        appointment.setDuration(Duration.between(start.toLocalTime(), 
                                                ViewController.LAST_APPOINTMENT_SLOT));
        appointment.setStatus(Appointment.Status.UNBOOKED);
        return appointment;
    }

    private Appointment createEmptyAppointmentSlot(LocalDateTime start, Duration duration){
        Appointment appointment = new Appointment();
        appointment.setPatient(null);
        appointment.setStart(start);
        appointment.setDuration(duration);
        appointment.setStatus(Appointment.Status.UNBOOKED);
        //appointment.setEnd(appointment.getStart().plusMinutes(duration.toMinutes()));
        return appointment;
    }
    private Patient makePatientFrom(EntityDescriptor.Patient eP){
        Patient p = new Patient();
        for (EntityDescriptor.PatientField pf: EntityDescriptor.PatientField.values()){
            switch (pf){
                case KEY:
                    p.setKey(eP.getData().getKey());
                    break;
                case TITLE:
                    p.getName().setTitle(eP.getData().getTitle());
                    break;
                case FORENAMES:
                    p.getName().setForenames(eP.getData().getForenames());
                    break;
                case SURNAME:
                    p.getName().setSurname(eP.getData().getSurname());
                    break;
                case LINE1:
                    p.getAddress().setLine1(eP.getData().getLine1());
                    break;
                case LINE2:
                    p.getAddress().setLine2(eP.getData().getLine2());
                    break;
                case TOWN:
                    p.getAddress().setTown(eP.getData().getTown());
                    break;
                case COUNTY:
                    p.getAddress().setCounty(eP.getData().getCounty());
                    break;
                case POSTCODE:
                    p.getAddress().setPostcode(eP.getData().getPostcode());
                    break;
                case DENTAL_RECALL_DATE:
                    p.getRecall().setDentalDate(eP.getData().getDentalRecallDate());
                    break;
                case HYGIENE_RECALL_DATE:
                    p.getRecall().setHygieneDate(eP.getData().getHygieneRecallDate());
                    break;
                case HYGIENE_RECALL_FREQUENCY:
                    p.getRecall().setHygieneFrequency(eP.getData().getHygieneRecallFrequency());
                    break;
                case DENTAL_RECALL_FREQUENCY:
                    p.getRecall().setDentalFrequency(eP.getData().getDentalRecallFrequency());
                    break;
                case GENDER:
                    p.setGender(eP.getData().getGender());
                    break;
                case PHONE1:
                    p.setPhone1(eP.getData().getPhone1());
                    break;
                case PHONE2:
                    p.setPhone2(eP.getData().getPhone2());
                    break;
                case DOB:
                    p.setDOB(eP.getData().getDOB());
                    break;
                case NOTES:
                    p.setNotes(eP.getData().getNotes());
                    break;
                case IS_GUARDIAN_A_PATIENT:
                    p.setIsGuardianAPatient(eP.getData().getIsGuardianAPatient());
                    break;
            }
        }
        return p;
    }
    private Appointment makeAppointmentFromEDRequest(){
        Appointment appointment;
        if (getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey()!=null){
            appointment = new Appointment(getEntityDescriptorFromView().getAppointment().getData().getKey());
        }
        else appointment = new Appointment();
        
        appointment.setDuration(getEntityDescriptorFromView().getRequest().getAppointment().getData().getDuration());
        appointment.setStart(getEntityDescriptorFromView().getRequest().getAppointment().getData().getStart());
        appointment.setNotes(getEntityDescriptorFromView().getRequest().getAppointment().getData().getNotes());
        appointment.setPatient(makePatientFrom(getEntityDescriptorFromView().getRequest().getPatient()));
        return appointment;
    }

    private EntityDescriptor getNewEntityDescriptor(){
        return this.newEntityDescriptor;
    }
    private void setNewEntityDescriptor(EntityDescriptor value){
        this.newEntityDescriptor = value;
    }
    private EntityDescriptor getOldEntityDescriptor(){
        return this.oldEntityDescriptor;
    }
    private void setOldEntityDescriptor(EntityDescriptor e){
        this.oldEntityDescriptor = e;
    }
    
    @Override
    public EntityDescriptor getEntityDescriptorFromView(){
        return this.entityDescriptorFromView;
    }
    private void setEntityDescriptorFromView(EntityDescriptor e){
        this.entityDescriptorFromView = e;
    }

    
    /**
     * update old entity descriptor with previous new entity descriptor 
     * re-initialise the new entity descriptor, but copy over the old selected day
     */
    private void initialiseNewEntityDescriptor(){
        setOldEntityDescriptor(getNewEntityDescriptor());
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().getRequest().setDay(getOldEntityDescriptor().getRequest().getDay());
    }
    
    /**
     * serialiseAppointmentToEDAppointment method flattens the model appointment object
     * -- EntityDescriptor.Appointment.Data = renderAppointment(appointment) -> initialises Data with the key, start, duration and note fields from the model appointment object
     * -- -- EntityDescriptor.Appointment.Appointee = renderPatient(appointment.getPatient()) -> initialises Data with the model patient field values; else null of this is an empty appointment slot
     * -- -- EntityDescriptor.Patient = Appointee
     * -- -- for convenience an additional Appointment.Data.EmptySlot is initialised true or false as appropriate
     * -- -- this is a duplication of the significance of the EntityDescriptor.Appointment.Appointee and EntityDescriptor.Patient values; a null value of which indicates EmptySlot = true
     * @param model appointment object
     */
    private void serialiseAppointmentToEDAppointment(Appointment appointment){
        RenderedAppointment renderedAppointment = renderAppointment(appointment);
        RenderedPatient renderedPatient;
        if (appointment.getPatient() != null){
            renderedAppointment.IsEmptySlot(false);
            renderedPatient = renderPatient(appointment.getPatient());
            getNewEntityDescriptor().setAppointment(new EntityDescriptor().getAppointment());
            getNewEntityDescriptor().getAppointment().setData(renderedAppointment);
            getNewEntityDescriptor().getAppointment().getAppointee().setData(renderedPatient);
            getNewEntityDescriptor().setPatient(getNewEntityDescriptor().getAppointment().getAppointee());
        }
        else {
            renderedAppointment.IsEmptySlot(true);
            getNewEntityDescriptor().getAppointment().setData(renderedAppointment);
            getNewEntityDescriptor().getAppointment().setAppointee(null);
            getNewEntityDescriptor().setPatient(null);
        }
    }
    /**
     * Produces a flattened version of the model.Patient object which does not include a reference to a patient object if the legal guardian of this patient is also a patient of the practice.
     * -- fields in the EntityDescriptor.Patient object include both a Data object (the rendered patient) as well as an Appointee object (a rendered patient object)
     * @param p
     * @return 
     */
    private RenderedPatient renderPatient(Patient p){
        RenderedPatient result = null;
        if (p!=null){
            RenderedPatient vp = new RenderedPatient();
            for (EntityDescriptor.PatientField pf: EntityDescriptor.PatientField.values()){
                switch(pf){
                    case KEY:
                        vp.setKey(p.getKey());
                        break;
                    case TITLE:
                        vp.setTitle((p.getName().getTitle()));
                        break;
                    case FORENAMES:
                        vp.setForenames((p.getName().getForenames()));
                        break;
                    case SURNAME:
                        vp.setSurname((p.getName().getSurname()));
                        break;
                    case LINE1:
                        vp.setLine1((p.getAddress().getLine1()));
                        break;
                    case LINE2:
                        vp.setLine2((p.getAddress().getLine2()));
                        break;
                    case TOWN:
                        vp.setTown((p.getAddress().getTown()));
                        break;
                    case COUNTY:
                        vp.setCounty((p.getAddress().getCounty()));
                        break;
                    case POSTCODE:
                        vp.setPostcode((p.getAddress().getPostcode()));
                        break;
                    case DENTAL_RECALL_DATE:
                        vp.setDentalRecallDate((p.getRecall().getDentalDate()));
                        break;
                    case HYGIENE_RECALL_DATE:
                        vp.setHygieneRecallDate((p.getRecall().getHygieneDate()));
                        break;
                    case DENTAL_RECALL_FREQUENCY:
                        vp.setHygieneRecallFrequency((p.getRecall().getDentalFrequency()));
                        break;
                    case HYGIENE_RECALL_FREQUENCY:
                        vp.setDentalRecallFrequency((p.getRecall().getDentalFrequency()));
                        break;
                    case DOB:
                        vp.setDOB((p.getDOB()));
                        break;
                    case GENDER:
                        p.setGender((p.getGender()));
                        break;
                    case PHONE1:
                        vp.setPhone1((p.getPhone1()));
                        break;
                    case PHONE2:
                        vp.setPhone2((p.getPhone2()));
                        break;
                    case IS_GUARDIAN_A_PATIENT:
                        vp.setIsGuardianAPatient((p.getIsGuardianAPatient()));
                        break;
                    case NOTES:
                        vp.setNotes((p.getNotes()));
                        break;
                }
            }
            result = vp;
        }
        return result;
    }
    /**
     * Produces a flattened version of the model.Appointment object which does not include a reference to a patient object if an appointee exists.
     * -- fields in the EntityDescriptor.Appointment object include both a Data object (the rendered appointment) as well as an Appointee object (a rendered patient object)
     * @param a model.Appointment object
     * @return 
     */
    private RenderedAppointment renderAppointment(Appointment a){
        RenderedAppointment ra = new RenderedAppointment();
        for (EntityDescriptor.AppointmentField af: EntityDescriptor.AppointmentField.values()){
            switch(af){
                case KEY:
                    ra.setKey(a.getKey());
                    break;
                case DURATION:
                    ra.setDuration(a.getDuration());
                    break;
                case NOTES:
                    ra.setNotes(a.getNotes());
                    break;
                case START:
                    ra.setStart(a.getStart()); 
                    break;
            }  
        }
        return ra;
    }
    /**
     * Method serialises the specified collection of Appointment objects into
     * EntityDescriptor.Collection of serialised Appointment objects.This collection
     * is emptied of entries on entry to the method. The booked/unbooked status 
     * of the appointment object being serialised is checked to see if an  
     * encapsulated patient object,the appointee, exists. If it does the patient
     * object is serialised
     * @param appointments, collection of model Appointment objects 
     */
    private void serialiseAppointmentsToEDCollection(ArrayList<Appointment> appointments){
        getNewEntityDescriptor().getAppointments().getData().clear();
        Iterator<Appointment> appointmentsIterator = appointments.iterator();
        while(appointmentsIterator.hasNext()){
            Appointment appointment = appointmentsIterator.next();
            RenderedAppointment renderedAppointment = renderAppointment(appointment);
            EntityDescriptor.Appointment edAppointment = new EntityDescriptor().getAppointment();
            edAppointment.setData(renderedAppointment);
            if (appointment.getStatus() == Appointment.Status.BOOKED){
                edAppointment.getData().IsEmptySlot(false);
                if (appointment.getPatient()!=null){
                    RenderedPatient appointee = renderPatient(appointment.getPatient());
                    EntityDescriptor.Patient edPatient = new EntityDescriptor().getPatient();
                    edPatient.setData(appointee);
                    edAppointment.setAppointee(edPatient);
                }
            }
            else{
                edAppointment.setAppointee(null);
                edAppointment.getData().IsEmptySlot(true);
            }
            getNewEntityDescriptor().getAppointments().getData().add(edAppointment);
        }    
    }
   
    private void serialisePatientsToEDCollection(ArrayList<Patient> patients) throws StoreException{
        /**
         * initialises NewEntityDescriptor with the collection of all the patients on the system
         */
        getNewEntityDescriptor().getPatients().getData().clear();
        Iterator<Patient> patientsIterator = patients.iterator();
        while(patientsIterator.hasNext()){
            Patient patient = patientsIterator.next();
            RenderedPatient renderedPatient = renderPatient(patient);
            getNewEntityDescriptor().setPatient(new EntityDescriptor().getPatient());
            getNewEntityDescriptor().getPatient().setData(renderedPatient);
            //getNewEntityDescriptor().setPatientGuardian(new EntityDescriptor().getPatientGuardian());
            //update 30/07/2021 09:05
            getNewEntityDescriptor().getPatient().setPatientGuardian(new EntityDescriptor().getPatient().getPatientGuardian());
            getNewEntityDescriptor().setPatientAppointmentHistory(null);
            getNewEntityDescriptor().getPatients().getData().add(getNewEntityDescriptor().getPatient());
        }
    }

    private void setMyController(ActionListener myController ){
        this.myController = myController;
    }
    
    /*public JInternalFrame getView( ){
        return view;
    }
    */

    public JInternalFrame getView( ){
        return view;
    }

    private void getUpdatedAppointmentSlotsForDay(LocalDate day)throws StoreException{
        Appointments appointments = null;
        
        initialiseNewEntityDescriptor();
        appointments = new Appointments();
        appointments.readForDay(day);
        ArrayList<Appointment> appointmentSlotsForDay = 
                getAppointmentsForSelectedDayIncludingEmptySlots(appointments,day);
        appointments.clear();
        appointments.addAll(appointmentSlotsForDay);
        serialiseAppointmentsToEDCollection(appointments);
    }
}
