/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import static clinicpms.controller.ViewController.displayErrorMessage;
import clinicpms.model.TheAppointment;
import clinicpms.model.ThePatient;
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

    private enum RequestedAppointmentState{ REQUESTED_SLOT_STARTS_AFTER_PREVIOUS_SCHEDULED_SLOT_AND_BEFORE_NEXT_SCHEDULED_SLOT,
                                            REQUESTED_SLOT_END_TIME_UPDATED_TO_LATER_TIME,
                                            APPOINTMENT_ADDED_TO_SCHEDULE,
                                            ERROR_ADDING_APPOINTMENT_TO_SCHEDULE}

    private ActionListener myController = null;
    private PropertyChangeSupport pcSupport = null;
    private View view = null;
    private View view2 = null;
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private PropertyChangeEvent pcEvent = null;
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
        pcSupport = new PropertyChangeSupport(this);
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
            }
        }
        
    }
    
    private void doAppointmentCancelRequest(){
        /**
         * on receipt of APPOINTMENT_CANCEL_REQUEST
         * -- assumes on entry EntityDescriptorFromView has been initialised (by view)
         */
        if (getEntityDescriptorFromView().getRequest().getTheAppointment().getPatient().getIsKeyDefined()){
            try{
               /**
                * The requested appointment is read into memory from the store using the specified appointment key
                * -- this means the appointee (patient) object is encapsulated in the appointment object
                * -- knowledge of the patient object is required to maintain consistency between views, which is a responsibility of the desktop view controller
                * 
                */
               TheAppointment appointment = getEntityDescriptorFromView().getRequest().getTheAppointment();
               appointment.delete();
               LocalDate day = getEntityDescriptorFromView().
                       getRequest().getTheAppointment().getStart().toLocalDate();
               getUpdatedAppointmentSlotsForDay(day);
               
               getNewEntityDescriptor().setTheAppointment(appointment);

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
        ThePatient.Collection patients = null;
        initialiseNewEntityDescriptor();
        try{
            patients = new ThePatient().getCollection();
            patients.read();
            getNewEntityDescriptor().setThePatients(patients.get());
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
        ThePatient.Collection patients = null;
        if (getEntityDescriptorFromView().getRequest().getTheAppointment().getIsKeyDefined()){
            try{

                TheAppointment appointment = getEntityDescriptorFromView().
                        getRequest().getTheAppointment();

                patients = new ThePatient().getCollection();
                patients.read();
                initialiseNewEntityDescriptor();
                getNewEntityDescriptor().setTheAppointment(appointment);
                getNewEntityDescriptor().setThePatients(patients.get());
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
        TheAppointment.Collection appointments = null;
        try{
            getUpdatedAppointmentSlotsForDay(day);
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
        TheAppointment.Collection appointments = null;
        try{
            appointments = new TheAppointment().getCollection();
            appointments.setScope(TheAppointment.Scope.FOR_DAY);
            appointments.getAppointment().setStart(day.atStartOfDay());
            appointments.read();
            getNewEntityDescriptor().setTheAppointments(appointments.get());
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
                    EntityDescriptor.AppointmentViewControllerPropertyEvent.SURGERY_DAYS_ASSIGNMENT_RECEIVED.toString(),
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
    
    private void doAppointmentSlotsFromDayRequest(){
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
        TheAppointment.Collection appointments = null;
        try{
            appointments = new TheAppointment().getCollection();
            appointments.setScope(TheAppointment.Scope.FROM_DAY);
            appointments.getAppointment().setStart(day.atStartOfDay());
            appointments.read();
            if (appointments.get().isEmpty()){
                JOptionPane.showMessageDialog(null, "No scheduled appointments from selected scan date (" + day.format(dmyFormat) + ")");
            }
            else{
                ArrayList<TheAppointment> availableSlotsOfDuration =  
                        getAvailableSlotsOfDuration(
                                appointments.get(),duration,day);
                getNewEntityDescriptor().setTheAppointments(availableSlotsOfDuration);
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
            displayErrorMessage("StoreException raised in controller doAppointmentSlotsFromDay()",
                    "Appointment view controller error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doEmptySlotScannerViewAction(ActionEvent e){
        EntityDescriptor.AppointmentViewControllerActionEvent actionCommand =
               EntityDescriptor.AppointmentViewControllerActionEvent.valueOf(e.getActionCommand());        
        switch (actionCommand){
            case APPOINTMENT_SLOTS_FROM_DATE_REQUEST:
                doAppointmentSlotsFromDayRequest();
                break;
            case MODAL_VIEWER_ACTIVATED:
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
                this.myController.actionPerformed(actionEvent);
                break;        
        }
    }
    
    private TheAppointment doAppointmentCreateRequest(ActionEvent e, LocalDate day){
        TheAppointment result = null;
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        TheAppointment rSlot = getEntityDescriptorFromView().getRequest().getTheAppointment();
        day = getEntityDescriptorFromView().getRequest().
                getTheAppointment().getStart().toLocalDate();
        initialiseNewEntityDescriptor();
        try{
            result = requestToChangeAppointmentSchedule(ViewMode.CREATE); 
        }catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error on attempt to create a new appointment",JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private TheAppointment doAppointmentUpdateRequest(ActionEvent e, LocalDate day){
        TheAppointment result = null;
        //setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        day = getEntityDescriptorFromView().getRequest().
                getTheAppointment().getStart().toLocalDate();
        initialiseNewEntityDescriptor();
        try{
            result = requestToChangeAppointmentSchedule(ViewMode.UPDATE);
        }catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"AppointmentViewController error on attempt to update an appointment",JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private void initialiseAppointmentSchedule(LocalDate day, TheAppointment result){
        
        //22/07/2022 08:56 save copy of EntityDescriptorFromView; could be overwritten
        EntityDescriptor ed = getEntityDescriptorFromView();
        //TheAppointment a = ed.getRequest().getTheAppointment();
        TheAppointment.Collection appointments = null;
        try{
            this.view2.setClosed(true);
        }
        catch (PropertyVetoException ex){

        }

        initialiseNewEntityDescriptor();
        //09/07/2022 07:02
        pcSupport.addPropertyChangeListener(view);
        pcEvent = new PropertyChangeEvent(this,
            EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENT_SLOTS_FROM_DAY_RECEIVED.toString(),
            null,getNewEntityDescriptor());
        pcSupport.firePropertyChange(pcEvent);
        pcSupport.removePropertyChangeListener(this.view);

        try{
            getUpdatedAppointmentSlotsForDay(day);
           
           
            pcSupport.addPropertyChangeListener(this.view);
            pcEvent = new PropertyChangeEvent(this,
                EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);

            //22/07/2022 08:56
            setEntityDescriptorFromView(ed);
            //getNewEntityDescriptor().setTheAppointment(result);
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
        TheAppointment result = null;
        LocalDate day = null;
        EntityDescriptor.AppointmentViewControllerActionEvent actionCommand =
               EntityDescriptor.AppointmentViewControllerActionEvent.valueOf(e.getActionCommand());        
        switch (actionCommand){
            case APPOINTMENT_CREATE_REQUEST:
                day = getEntityDescriptorFromView().getRequest().getTheAppointment().
                        getStart().toLocalDate();
                result = doAppointmentCreateRequest(e, day);
                if (result!=null) initialiseAppointmentSchedule(day, result);
                else sendErrorToAppointmentCreatorEditorView();
                break;
            case APPOINTMENT_UPDATE_REQUEST:
                day = getEntityDescriptorFromView().getRequest().getTheAppointment().
                        getStart().toLocalDate();
                result = doAppointmentUpdateRequest(e, day);
                if (result!=null) initialiseAppointmentSchedule(day,result);
                else sendErrorToAppointmentCreatorEditorView();
                break;
            case MODAL_VIEWER_ACTIVATED:
                doModalViewerActivated();
                break;     
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
    
    private String getNameOfSlotOwnerPlusSlotStart(TheAppointment slot){
        String result = getNameOfSlotOwner(slot);
        LocalTime start = slot.getStart().toLocalTime();
        result = result + " which starts at " + start.format(DateTimeFormatter.ofPattern("HH:mm"));
        return result;
    }
  
    private String getNameOfSlotOwner(TheAppointment slot){
        String title;
        String forenames;
        String surname;
        
        title = slot.getPatient().getName().getTitle();
        forenames = slot.getPatient().getName().getForenames();
        surname = slot.getPatient().getName().getSurname();
        if (title.length()==0) title = "?";
        if (forenames.length() == 0) forenames = "<...>";
        if (surname.length() == 0) surname = "<...>";
       
        return title + " " + forenames + " " + surname;
    }
    
    private String appointmentCollisionCheckOnScheduleChangeRequest(
            TheAppointment requestedSlot,
            ArrayList<TheAppointment> appointments, ViewMode mode){
        String result = null;
        RequestedAppointmentState state = RequestedAppointmentState.REQUESTED_SLOT_STARTS_AFTER_PREVIOUS_SCHEDULED_SLOT_AND_BEFORE_NEXT_SCHEDULED_SLOT;
        Iterator<TheAppointment> appointmentsForDay = appointments.iterator();
        while (appointmentsForDay.hasNext()){
            TheAppointment nextScheduledSlot = appointmentsForDay.next();
            switch(state){
                case REQUESTED_SLOT_END_TIME_UPDATED_TO_LATER_TIME:
                    /**
                     * requested slot updated end time is later than the previous scheduled slot
                     * -- check if its end time is not after next scheduled slot start time
                     * ---- its not; so state = APPOINTMENT_ADDED_TO_SCHEDULE
                     * ---- else check if requested slot and overlapped next scheduled slot refer to same appointment (currently being updated) 
                     * ------ not the same appointment; state = ERROR_ADDING_APPOINTMENT_TO_SCHEDULE and create error content
                     *
                     */
                    if (!requestedSlot.getSlotEndTime().isAfter(nextScheduledSlot.getSlotEndTime())){
                        /**
                         * -- if requested slot end time is not after next scheduled end time 
                         * ---- check requested and next scheduled slot refer to the same appointment
                         * ------ yes; means appointment is being correctly updated
                         * ------ change state to APPOINTMENT_ADDED_TO_SCHEDULE 
                         */
                        if (requestedSlot.equals(nextScheduledSlot)){
                            state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                        }
                        else{
                            state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                            result = 
                                    "The new appointment for " + requestedSlot.getAppointeeName()
                                    + " overwrites existing appointment for " 
                                    + nextScheduledSlot.getAppointeeNamePlusSlotStartTime();
                        }      
                    }else //do nothing and retain state = REQUESTED_SLOT_END_TIME_UPDATED_TO_LATER_TIME
                    break;
                case REQUESTED_SLOT_STARTS_AFTER_PREVIOUS_SCHEDULED_SLOT_AND_BEFORE_NEXT_SCHEDULED_SLOT:
                    if(!requestedSlot.getSlotEndTime().isAfter(nextScheduledSlot.getSlotStartTime())){
                        /**
                         * requested slot fits into empty slot space prior to scheduled slot so can add to schedule
                         */
                        result = null;
                        state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                        break;
                    }
                    else if (!requestedSlot.getSlotStartTime().isBefore(nextScheduledSlot.getSlotEndTime())){
                        /**
                         * --requested slot starts after end of scheduled slot
                         * --do nothing, state (STARTS_AFTER_PREVIOUS_SLOT) remains the same
                         */
                    }
                    else {//means requested slot overlaps next scheduled slot
                        switch (mode){
                            case CREATE:
                                /**
                                 * --requested Slot overlaps next scheduled appointment so cannot be created 
                                 * --abort attempt to create a new appointment and post error
                                 * -- check if overlapped next scheduled appointment is for the same patient
                                 * -- let user know if this is the case and suggest an update request is required
                                 */
                                state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                                if (requestedSlot.getPatient().equals(nextScheduledSlot.getPatient())){
                                    result = "The requested new appointment for "
                                            + requestedSlot.getAppointeeName()  
                                            + " overlaps a scheduled appointment for the same patient.\n"
                                            + "Rather than creating a new appointment for "
                                            + requestedSlot.getAppointeeName() 
                                            + " update the scheduled appointment." ;
                                }else {
                                result = 
                                        "The new appointment for " + requestedSlot.getAppointeeName()
                                        + " overwrites existing appointment for " + nextScheduledSlot.getAppointeeNamePlusSlotStartTime();
                                }
                                break;
                            case UPDATE:
                                //-- requested slot overlaps next scheduled slot
                                if (!requestedSlot.getSlotEndTime().isAfter(nextScheduledSlot.getSlotEndTime())){
                                    /**
                                     * -- if requested slot end time is not after next scheduled end time 
                                     * ---- check requested and next scheduled slot refer to the same appointment
                                     * ------ yes; means appointment is being correctly updated
                                     * ------ change state to APPOINTMENT_ADDED_TO_SCHEDULE 
                                     */
                                    if (requestedSlot.equals(nextScheduledSlot)){
                                        state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                                    }
                                    else{
                                        state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                                        result = 
                                                "The new appointment for " + requestedSlot.getAppointeeName()
                                                + " overwrites existing appointment for " 
                                                + nextScheduledSlot.getAppointeeNamePlusSlotStartTime();
                                    }      
                                }else  state = RequestedAppointmentState.REQUESTED_SLOT_END_TIME_UPDATED_TO_LATER_TIME;
                                break;
                        }
                    }        
            }
        }
        return result;
        
    }
    
    /*
    private String appointmentCollisionChangingSchedule(
            TheAppointment rSlot, 
            ArrayList<TheAppointment> appointments, ViewMode mode){
        String result = null;
        LocalDateTime sSlotEnd;
        LocalDateTime rSlotEnd = rSlot.getStart().plusMinutes(rSlot.getDuration().toMinutes());
        Iterator<TheAppointment> it = appointments.iterator();
        RequestedAppointmentState state = RequestedAppointmentState.REQUESTED_SLOT_STARTS_AFTER_PREVIOUS_SCHEDULED_SLOT_AND_BEFORE_NEXT_SCHEDULED_SLOT;
        while(it.hasNext()){
            TheAppointment sSlot = it.next();
            sSlotEnd = sSlot.getStart().plusMinutes(sSlot.getDuration().toMinutes());
            switch (state){
                case REQUESTED_SLOT_STARTS_AFTER_PREVIOUS_SCHEDULED_SLOT_AND_BEFORE_NEXT_SCHEDULED_SLOT:
                    if(!rSlotEnd.isAfter(sSlot.getStart())){
       
                        result = null;
                        state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                        break;
                    }
                    else if (!rSlot.getStart().isBefore(sSlotEnd)){

                    }
                    else {//must mean rSlot overlaps sSlot
                        switch (mode){
                            case CREATE:
           
                                state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                                result = 
                                        "The new appointment for " + getNameOfSlotOwner(rSlot)
                                        + " overwrites existing appointment for " + getNameOfSlotOwnerPlusSlotStart(sSlot);
                                break;
                            case UPDATE:
                                //24/07/2022 13:42
                                //if (!rSlot.getIsKeyDefined().equals(sSlot.getIsKeyDefined())){
                                //26/07/2022 11:37
                                if (!rSlot.equals(sSlot)){
     
                                    state = RequestedAppointmentState.ERROR_ADDING_APPOINTMENT_TO_SCHEDULE;
                                    result = 
                                            "The new appointment for " + getNameOfSlotOwner(rSlot)
                                            + " overwrites existing appointment for " + getNameOfSlotOwnerPlusSlotStart(sSlot);
                                    break;
                                }
                                else if (rSlotEnd.isAfter(sSlotEnd)){
      
                                    state = RequestedAppointmentState.ENDS_AFTER_PREVIOUS_SLOT;
                                }
                                else{

                                    result = null;
                                    break;
                                }
                                break;
                        }
                    }
                    break;
                case ENDS_AFTER_PREVIOUS_SLOT:

                   if (!rSlotEnd.isAfter(sSlot.getStart())){

                       result = null;
                       state = RequestedAppointmentState.APPOINTMENT_ADDED_TO_SCHEDULE;
                       break;
                   }
                   else{

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
    */
    private TheAppointment requestToChangeAppointmentSchedule(ViewMode mode) throws StoreException{
        String error;
        TheAppointment result = null;
        //TheAppointment rSlot = makeAppointmentFromEDRequest();
        TheAppointment rSlot = getEntityDescriptorFromView().getRequest().getTheAppointment();
        LocalDate day = rSlot.getStart().toLocalDate();
        
        //NOTE: changed "appointments" to "appts" because former hid a previous definition of "appointments"
        TheAppointment.Collection appts = new TheAppointment().getCollection();
        TheAppointment appointment = appts.getAppointment();
        appointment.setStart(day.atStartOfDay());
        appts.setScope(TheAppointment.Scope.FOR_DAY);
        appts.read();
        if (appts.get().isEmpty()){
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
            switch (mode){//one or more appointments already exist so check the CREATE or UPGRADE make sense
                case CREATE:
                    error = appointmentCollisionCheckOnScheduleChangeRequest(rSlot, appts.get(), mode);
                    getNewEntityDescriptor().setError(error);
                    if (error==null){
                        //no collision results
                        rSlot.insert(); //was rslot.create()
                        result = rSlot.read();
                    }
                    break;
                case UPDATE:
                    error = appointmentCollisionCheckOnScheduleChangeRequest(rSlot, appts.get(), mode);
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
    
    private ArrayList<TheAppointment> getAvailableSlotsOfDuration(
            ArrayList<TheAppointment> appointments, Duration duration, LocalDate searchStartDay){
        ArrayList<TheAppointment> result = new ArrayList<>();
        ArrayList<TheAppointment> appointmentsForSingleDay = new ArrayList<>();
        ArrayList<ArrayList<TheAppointment>> appointmentsGroupedByDay = new ArrayList<>();
        LocalDate currentDate = null;
        Iterator<TheAppointment> it = appointments.iterator();
        while(it.hasNext()){
            TheAppointment appointment = it.next();
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
        Iterator<ArrayList<TheAppointment>> it1 = appointmentsGroupedByDay.iterator();
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
            ArrayList<TheAppointment> slotsForDay = 
                    getAppointmentsForSelectedDayIncludingEmptySlots(
                            appointmentsForSingleDay, appointmentsForSingleDayDate); 
            Iterator<TheAppointment> it2 = slotsForDay.iterator();
            //currentDate = null;
            while(it2.hasNext()){
                TheAppointment slot = it2.next();
                if (slot.getStatus().equals(TheAppointment.Status.UNBOOKED)){
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
        TheAppointment multiDayIntervalWithNoAppointments = null;
        ArrayList<TheAppointment> finalisedResult = new ArrayList<>();
        //LocalDate thisDate = result.get(0).getStart().toLocalDate();
        it = result.iterator();
        int count = 0;
        
        if (duration.toHours()==8){//empty slot scan duration is all day
            while (it.hasNext()){
                count = count + 1;
                if (count == 23){
                    count = 19;                    
                }
                TheAppointment appointment = it.next();
                if (finalisedResult.isEmpty()&&multiDayIntervalWithNoAppointments==null){//start of procedure on entry
                    //multiDayIntervalHasStarted = true;
                    multiDayIntervalWithNoAppointments = new TheAppointment();
                    multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                    multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                    multiDayIntervalWithNoAppointments.setStatus(TheAppointment.Status.UNBOOKED);
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
                            multiDayIntervalWithNoAppointments = new TheAppointment();
                            multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                            multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                            multiDayIntervalWithNoAppointments.setStatus(TheAppointment.Status.UNBOOKED);
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
                TheAppointment appointment = it.next();
                /*
                if (appointment.getStart().toLocalDate().isEqual(LocalDate.of(2021,7, 16))){
                    LocalDate test = appointment.getStart().toLocalDate();
                }
                */
                if (appointment.getDuration().toHours() == 8){
                    //WHAT HAPPENS WHEN APPOINTMENT CHANGES MULTIDAYINTERVALHASSTARTED SLOT
                    if (!multiDayIntervalHasStarted) {
                        multiDayIntervalHasStarted = true;
                        multiDayIntervalWithNoAppointments = new TheAppointment();
                        multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                        multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                        multiDayIntervalWithNoAppointments.setStatus(TheAppointment.Status.UNBOOKED);
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
                            multiDayIntervalWithNoAppointments = new TheAppointment();
                            multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                            multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(0));
                            multiDayIntervalWithNoAppointments.setStatus(TheAppointment.Status.UNBOOKED);
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
    
    private LocalDate getPracticeDayOnWhichSlotEnds(TheAppointment slot){
        
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
    private boolean areTheseSlotsOnConsecutivePracticeDays(TheAppointment slot1, TheAppointment slot2){
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
    
    private ArrayList<TheAppointment> getAppointmentsForSelectedDayIncludingEmptySlots(
            ArrayList<TheAppointment> appointments, LocalDate day) {
        LocalDateTime nextEmptySlotStartTime;
        nextEmptySlotStartTime = LocalDateTime.of(day, 
                                                  ViewController.FIRST_APPOINTMENT_SLOT);
        ArrayList<TheAppointment> apptsForDayIncludingEmptySlots = new ArrayList<>();      
        Iterator<TheAppointment> it = appointments.iterator();
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
                TheAppointment appointment = it.next();
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
                    TheAppointment emptySlot = createEmptyAppointmentSlot(nextEmptySlotStartTime,
                            Duration.between(nextEmptySlotStartTime, appointment.getStart()).abs());
                    apptsForDayIncludingEmptySlots.add(emptySlot);
                    apptsForDayIncludingEmptySlots.add(appointment);
                    nextEmptySlotStartTime =
                            appointment.getStart().plusMinutes(appointment.getDuration().toMinutes());
                }
            }
        }
        TheAppointment lastAppointment = 
                apptsForDayIncludingEmptySlots.get(apptsForDayIncludingEmptySlots.size()-1);
        if (lastAppointment.getStatus().equals(TheAppointment.Status.BOOKED)){
            //check if bookable time after last appointment
            Duration durationToDayEnd = 
                    Duration.between(nextEmptySlotStartTime.toLocalTime(), ViewController.LAST_APPOINTMENT_SLOT).abs();
            if (!durationToDayEnd.isZero()) {
                TheAppointment emptySlot = createEmptyAppointmentSlot(nextEmptySlotStartTime);
                apptsForDayIncludingEmptySlots.add(emptySlot);
            }
        }
        return apptsForDayIncludingEmptySlots;
    }
    private TheAppointment createEmptyAppointmentSlot(LocalDateTime start){
        TheAppointment appointment = new TheAppointment();
        appointment.setPatient(null);
        appointment.setStart(start);
        appointment.setDuration(Duration.between(start.toLocalTime(), 
                                                ViewController.LAST_APPOINTMENT_SLOT));
        appointment.setStatus(TheAppointment.Status.UNBOOKED);
        return appointment;
    }

    private TheAppointment createEmptyAppointmentSlot(LocalDateTime start, Duration duration){
        TheAppointment appointment = new TheAppointment();
        appointment.setPatient(null);
        appointment.setStart(start);
        appointment.setDuration(duration);
        appointment.setStatus(TheAppointment.Status.UNBOOKED);
        //appointment.setEnd(appointment.getStart().plusMinutes(duration.toMinutes()));
        return appointment;
    }

    public EntityDescriptor getNewEntityDescriptor(){
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

    private void setMyController(ActionListener myController ){
        this.myController = myController;
    }

    public JInternalFrame getView( ){
        return view;
    }

    private void getUpdatedAppointmentSlotsForDay(LocalDate day)throws StoreException{
        TheAppointment appointment = new TheAppointment();
        TheAppointment.Collection appts = appointment.getCollection();
        initialiseNewEntityDescriptor();
        appointment.setStart(day.atStartOfDay());
        appts.setScope(TheAppointment.Scope.FOR_DAY);
        appts.read();
        ArrayList<TheAppointment> appointmentSlotsForDay =
                getAppointmentsForSelectedDayIncludingEmptySlots(appts.get(),appointment.getStart().toLocalDate());
        appts.get().clear();
        appts.get().addAll(appointmentSlotsForDay);
        getNewEntityDescriptor().setTheAppointments(appts.get());
    }
}
