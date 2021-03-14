/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;
import clinicpms.constants.ClinicPMS;
import clinicpms.model.Appointments;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.store.exceptions.StoreException;
import clinicpms.view.AppointmentsForDayView;
import clinicpms.view.AppointmentViewDialog;
import clinicpms.view.AppointmentEditorDialog;
import clinicpms.view.EmptySlotScannerSettingsDialog;
import clinicpms.view.interfaces.IView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author colin
 */


public class AppointmentViewController extends ViewController{

    private ActionListener myController = null;
    private PropertyChangeSupport pcSupport = null;
    private AppointmentsForDayView view = null;
    private ArrayList<Appointment> appointments = null;  
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private LocalDate day = null;
    private PropertyChangeEvent pcEvent = null;
    private JFrame owningFrame = null;
    private ViewController.ViewMode viewMode = null;
    //private AppointmentEditorDialog dialog = null;
    private AppointmentViewDialog dialog = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    
    /**
     * 
     * @param controller ActionLister to send ActionEvent objects to
     * @param owner JFrame the owning frame the view controller needs to reference 
     * if managing a customised JDialog view
     */
    public AppointmentViewController(ActionListener controller, JFrame owner)throws StoreException{
        setMyController(controller);
        this.owningFrame = owner;
        pcSupport = new PropertyChangeSupport(this);
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().getRequest().setDay(LocalDate.now());
        setEntityDescriptorFromView(getNewEntityDescriptor());
        //getAppointmentsForSelectedDay();
        this.view = new AppointmentsForDayView(this, getNewEntityDescriptor());
        this.view.addInternalFrameClosingListener(); 
        this.view.initialiseView();
    }
    @Override
    public void actionPerformed(ActionEvent e){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        /**
         * On each case listeners on both views are removed (no action if listener not registered)
         * and the listener for that case added. This ensures only a single listener, and the correct
         * one, is active when property change events are fired
         */
        String s;
        s = e.getSource().getClass().getSimpleName();
        switch(s){
            case "AppointmentsForDayView" -> {pcSupport.removePropertyChangeListener(this.dialog);
                                                pcSupport.removePropertyChangeListener(this.view);
                                                pcSupport.addPropertyChangeListener(view);
                                                doAppointmentsForDayViewActions(e);}
            case "AppointmentEditorDialog" -> {pcSupport.removePropertyChangeListener(view);
                                                        pcSupport.removePropertyChangeListener(this.dialog);
                                                        pcSupport.addPropertyChangeListener(this.dialog);
                                                        doAppointmentViewDialogActions(e);}
            case "DesktopViewController" -> doDesktopViewControllerAction(e);
            case "EmptySlotScannerSettingsDialog" -> {doEmptySlotScannerSettingsDialogActions(e);}
        }
    }
    
    private void doDesktopViewControllerAction(ActionEvent e){
        if (e.getActionCommand().equals(DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString())){
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
    private void doEmptySlotScannerSettingsDialogActions(ActionEvent e){
        if (e.getActionCommand().equals(EmptySlotSearchCriteriaDialogActionEvent.
                EMPTY_SLOT_SCANNER_CLOSE_REQUEST.toString())){
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));   
        }
        else if (e.getActionCommand().equals(AppointmentViewControllerActionEvent.APPOINTMENT_SLOTS_FROM_DATE_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            initialiseNewEntityDescriptor();
            LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
            Duration duration = getEntityDescriptorFromView().getRequest().getDuration();
            try{
                this.appointments =
                    new Appointments().getAppointmentsFrom(day);
                ArrayList<Appointment> availableSlotsOfDuration = 
                        getAvailableSlotsOfDuration(
                                this.appointments,duration,day);
                serialiseAppointmentsToEDCollection(availableSlotsOfDuration);
                pcEvent = new PropertyChangeEvent(this,
                    AppointmentViewControllerPropertyEvent.APPOINTMENT_SLOTS_FROM_DAY_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
        }
    }
    private void doAppointmentViewDialogActions(ActionEvent e){
        if (e.getActionCommand().equals(AppointmentViewDialogActionEvent.
                APPOINTMENT_VIEW_CREATE_REQUEST.toString())){
            Appointment appointment = makeAppointmentFromEDSelection();
            try{
                appointment = appointment.create();
                initialiseNewEntityDescriptor();
                serialiseAppointmentToEDAppointment(appointment);
                pcEvent = new PropertyChangeEvent(this,
                        AppointmentViewDialogPropertyEvent.APPOINTMENT_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
        }
        else if (e.getActionCommand().equals(AppointmentViewDialogActionEvent.
                APPOINTMENT_VIEW_UPDATE_REQUEST.toString())){
            Appointment appointment = makeAppointmentFromEDSelection();
            try{
                appointment = appointment.update();
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
            initialiseNewEntityDescriptor();
            serialiseAppointmentToEDAppointment(appointment);
            pcEvent = new PropertyChangeEvent(this,
                    AppointmentViewDialogPropertyEvent.APPOINTMENT_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupport.firePropertyChange(pcEvent);
        }
        else if (e.getActionCommand().equals(
                AppointmentViewDialogActionEvent.APPOINTMENT_VIEW_CLOSE_REQUEST.toString())){
            if (e.getSource() instanceof JFrame){
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            }
        }
    }
    private void doAppointmentsForDayViewActions(ActionEvent e){
        if (e.getActionCommand().equals(
                AppointmentViewControllerActionEvent.
                        APPOINTMENTS_VIEW_CLOSED.toString())){
            /**
             * APPOINTMENTS_VIEW_CLOSED
             */
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);   
        }
        
        else if (e.getActionCommand().equals(   
                AppointmentViewControllerActionEvent.EMPTY_SLOT_SCANNER_DIALOG_REQUEST.toString())){
            /**
             * EMPTY_SLOT_SCANNER_DIALOG_REQUEST empty slot scanner requested by view
             * -- construct a new dialog with a newly initialised EntityDescriptor, owning Frame
             */
            this.dialog = new EmptySlotScannerSettingsDialog(
                    this,getNewEntityDescriptor(),this.owningFrame);
            this.dialog.setVisible(true);
        }
        else if (e.getActionCommand().equals(
            /**
             * APPOINTMENTS_REQUEST would be sent by view on a change of the selected day
             */
            AppointmentViewControllerActionEvent.APPOINTMENTS_FOR_DAY_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            initialiseNewEntityDescriptor();
            LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
            try{
                this.appointments =
                    new Appointments().getAppointmentsFor(day);
                this.appointments = getAppointmentsForSelectedDayIncludingEmptySlots(this.appointments,day);
                serialiseAppointmentsToEDCollection(this.appointments);
                pcEvent = new PropertyChangeEvent(this,
                    AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
        }
        else if (e.getActionCommand().equals(AppointmentViewControllerActionEvent.APPOINTMENT_UPDATE_VIEW_REQUEST.toString())){
            if (getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey() != null){
                try{
                    Appointment appointment = new Appointment(
                            getEntityDescriptorFromView().getRequest().
                                    getAppointment().getData().getKey()).read();
                    ArrayList<Patient> patients = new Patients().getPatients();
                    initialiseNewEntityDescriptor();
                    serialiseAppointmentToEDAppointment(appointment);
                    serialisePatientsToEDCollection(patients);
                    
                    this.dialog = new AppointmentEditorDialog(this,getNewEntityDescriptor(),
                            this.owningFrame, ViewController.ViewMode.UPDATE);
                    this.dialog.setVisible(true);
                }
                catch (StoreException ex){
                    //UnspecifiedError action
                } 
            }
        }
        else if (e.getActionCommand().equals(AppointmentViewControllerActionEvent.APPOINTMENT_CREATE_VIEW_REQUEST.toString())){
            initialiseNewEntityDescriptor();
            this.dialog = new AppointmentEditorDialog(this,getNewEntityDescriptor(),
                    this.owningFrame, ViewController.ViewMode.CREATE);
            this.dialog.setVisible(true);
        }
        
        else if (e.getActionCommand().equals(AppointmentViewControllerActionEvent.APPOINTMENT_CANCEL_REQUEST.toString())){
            if (getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey()!=null){
                Appointment appointment = new Appointment(
                        getEntityDescriptorFromView().getRequest().getAppointment().getData().getKey());
                try{
                    appointment.delete();
                    LocalDate day = getEntityDescriptorFromView().getRequest().getDay();
                    this.appointments = new Appointments().getAppointmentsFor(day);
                    this.appointments = getAppointmentsForSelectedDayIncludingEmptySlots(
                            this.appointments,day);
                    serialiseAppointmentsToEDCollection(this.appointments);
                    pcEvent = new PropertyChangeEvent(this,
                        AppointmentViewControllerPropertyEvent.APPOINTMENTS_FOR_DAY_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupport.firePropertyChange(pcEvent);
                }
                catch (StoreException ex){
                    //UnspecifiedError action
                }
            }
        }   
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
                appointmentsForSingleDay = new ArrayList<Appointment>();
                appointmentsForSingleDay.add(appointment);
            }
        }
        Iterator<ArrayList<Appointment>> it1 = appointmentsGroupedByDay.iterator();
        //appointmentsForSingleDay.clear();
        
        /**
         * -- current day initialised to start day of search
         * -- for each day group of appts
         * ----while current day before date of day group of appts and practice day 
         * ------ create another day group containing single all day slot and add these to search result
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
                              currentDate.atTime(ClinicPMS.FIRST_APPOINTMENT_SLOT))); 
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
         * check and process days which have no appointments on, as follows
         * -- consecutive appointment-less days are merged into a single slot 
         * -- the single slot duration represents in hours the number of consecutive days
         */
        
        boolean multiDayIntervalHasStarted = false;
        Appointment multiDayIntervalWithNoAppointments = null;
        ArrayList<Appointment> finalisedResult = new ArrayList<>();
        it = result.iterator();
        while(it.hasNext()){
            Appointment appointment = it.next();

            if (appointment.getDuration().toHours() == 8){
                if (!multiDayIntervalHasStarted) {
                    multiDayIntervalHasStarted = true;
                    multiDayIntervalWithNoAppointments = new Appointment();
                    multiDayIntervalWithNoAppointments.setStart(appointment.getStart());
                    multiDayIntervalWithNoAppointments.setDuration(Duration.ofHours(8));
                    multiDayIntervalWithNoAppointments.setStatus(Appointment.Status.UNBOOKED);
                }
                else{
                    duration = multiDayIntervalWithNoAppointments.getDuration();
                    multiDayIntervalWithNoAppointments.setDuration(duration.plusHours(8));
                } 
            }
            else if (multiDayIntervalHasStarted){
                finalisedResult.add(multiDayIntervalWithNoAppointments);
                multiDayIntervalHasStarted = false;
                finalisedResult.add(appointment);
            }
            else finalisedResult.add(appointment);  
        }
        return finalisedResult;
    }
    private ArrayList<Appointment> getAppointmentsForSelectedDayIncludingEmptySlots(
            ArrayList<Appointment> appointments, LocalDate day) {
        LocalDateTime nextEmptySlotStartTime;
        nextEmptySlotStartTime = LocalDateTime.of(day, 
                                                  ClinicPMS.FIRST_APPOINTMENT_SLOT);
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
                    Duration.between(nextEmptySlotStartTime.toLocalTime(), ClinicPMS.LAST_APPOINTMENT_SLOT).abs();
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
                                                ClinicPMS.LAST_APPOINTMENT_SLOT));
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
        for (PatientField pf: PatientField.values()){
            switch (pf){
                case KEY -> p.setKey(eP.getData().getKey());
                case TITLE -> p.getName().setTitle(eP.getData().getTitle());
                case FORENAMES -> p.getName().setForenames(eP.getData().getForenames());
                case SURNAME -> p.getName().setSurname(eP.getData().getSurname());
                case LINE1 -> p.getAddress().setLine1(eP.getData().getLine1());
                case LINE2 -> p.getAddress().setLine2(eP.getData().getLine2());
                case TOWN -> p.getAddress().setTown(eP.getData().getTown());
                case COUNTY -> p.getAddress().setCounty(eP.getData().getCounty());
                case POSTCODE -> p.getAddress().setPostcode(eP.getData().getPostcode());
                case DENTAL_RECALL_DATE -> p.getRecall().setDentalDate(eP.getData().getDentalRecallDate());
                case HYGIENE_RECALL_DATE -> p.getRecall().setHygieneDate(eP.getData().getHygieneRecallDate());
                case HYGIENE_RECALL_FREQUENCY -> p.getRecall().setHygieneFrequency(eP.getData().getHygieneRecallFrequency());
                case DENTAL_RECALL_FREQUENCY -> p.getRecall().setDentalFrequency(eP.getData().getDentalRecallFrequency());
                case GENDER -> p.setGender(eP.getData().getGender());
                case PHONE1 -> p.setPhone1(eP.getData().getPhone1());
                case PHONE2 -> p.setPhone2(eP.getData().getPhone2());
                case DOB -> p.setDOB(eP.getData().getDOB());
                case NOTES -> p.setNotes(eP.getData().getNotes());
                case IS_GUARDIAN_A_PATIENT -> p.setIsGuardianAPatient(eP.getData().getIsGuardianAPatient());
            }
        }
        return p;
    }
    private Appointment makeAppointmentFromEDSelection(){
        Appointment appointment;
        if (getEntityDescriptorFromView().getAppointment().getData().getKey()!=null){
            appointment = new Appointment(getEntityDescriptorFromView().getAppointment().getData().getKey());
        }
        else appointment = new Appointment();
        
        appointment.setDuration(getEntityDescriptorFromView().getAppointment().getData().getDuration());
        appointment.setStart(getEntityDescriptorFromView().getAppointment().getData().getStart());
        appointment.setNotes(getEntityDescriptorFromView().getAppointment().getData().getNotes());
        appointment.setPatient(makePatientFrom(getEntityDescriptorFromView().getPatient()));
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
    private EntityDescriptor getEntityDescriptorFromView(){
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
    
    private void serialiseAppointmentToEDAppointment(Appointment appointment){
        RenderedAppointment renderedAppointment = renderAppointment(appointment);
        RenderedPatient renderedPatient;
        if (appointment.getPatient() != null){
            renderedAppointment.IsEmptySlot(false);
            renderedPatient = renderPatient(appointment.getPatient());
            getNewEntityDescriptor().setAppointment(new EntityDescriptor().getAppointment());
            getNewEntityDescriptor().getAppointment().setData(renderedAppointment);
            getNewEntityDescriptor().getAppointment().getAppointee().setData(renderedPatient);
        }
        else {
            renderedAppointment.IsEmptySlot(true);
            getNewEntityDescriptor().getAppointment().setData(renderedAppointment);
            getNewEntityDescriptor().getAppointment().setAppointee(null);
        }
    }
    private RenderedPatient renderPatient(Patient p){
        RenderedPatient result = null;
        if (p!=null){
            RenderedPatient vp = new RenderedPatient();
            for (PatientField pf: PatientField.values()){
                switch(pf){
                    case KEY -> vp.setKey(p.getKey());
                    case TITLE -> vp.setTitle((p.getName().getTitle()));
                    case FORENAMES -> vp.setForenames((p.getName().getForenames()));
                    case SURNAME -> vp.setSurname((p.getName().getSurname()));
                    case LINE1 -> vp.setLine1((p.getAddress().getLine1()));
                    case LINE2 -> vp.setLine2((p.getAddress().getLine2()));
                    case TOWN -> vp.setTown((p.getAddress().getTown()));
                    case COUNTY -> vp.setCounty((p.getAddress().getCounty()));
                    case POSTCODE -> vp.setPostcode((p.getAddress().getPostcode()));
                    case DENTAL_RECALL_DATE -> vp.setDentalRecallDate((p.getRecall().getDentalDate()));
                    case HYGIENE_RECALL_DATE -> vp.setHygieneRecallDate((p.getRecall().getHygieneDate()));
                    case DENTAL_RECALL_FREQUENCY -> vp.setHygieneRecallFrequency((p.getRecall().getDentalFrequency()));
                    case HYGIENE_RECALL_FREQUENCY -> vp.setDentalRecallFrequency((p.getRecall().getDentalFrequency()));
                    case DOB -> vp.setDOB((p.getDOB()));
                    case GENDER -> vp.setGender((p.getGender()));
                    case PHONE1 -> vp.setPhone1((p.getPhone1()));
                    case PHONE2 -> vp.setPhone2((p.getPhone2()));
                    case IS_GUARDIAN_A_PATIENT -> vp.setIsGuardianAPatient((p.getIsGuardianAPatient()));
                    case NOTES -> vp.setNotes((p.getNotes()));
                }
            }
            result = vp;
        }
        return result;
    }
    private RenderedAppointment renderAppointment(Appointment a){
        RenderedAppointment ra = new RenderedAppointment();
        for (AppointmentField af: AppointmentField.values()){
            switch(af){
                case KEY -> ra.setKey(a.getKey());
                case DURATION -> ra.setDuration(a.getDuration());
                case NOTES -> ra.setNotes(a.getNotes());
                case START -> ra.setStart(a.getStart());   
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
        //fetch all patients on the system from the model
        
        getNewEntityDescriptor().getPatients().getData().clear();
        Iterator<Patient> patientsIterator = patients.iterator();
        while(patientsIterator.hasNext()){
            Patient patient = patientsIterator.next();
            RenderedPatient renderedPatient = renderPatient(patient);
            getNewEntityDescriptor().setPatient(new EntityDescriptor().getPatient());
            getNewEntityDescriptor().getPatient().setData(renderedPatient);
            getNewEntityDescriptor().setPatientGuardian(new EntityDescriptor().getPatientGuardian());
            getNewEntityDescriptor().setPatientAppointmentHistory(null);
            getNewEntityDescriptor().getPatients().getData().add(getNewEntityDescriptor().getPatient());
        }
    }

    private ActionListener getMyController(){
        return myController;
    }
    private void setMyController(ActionListener myController ){
        this.myController = myController;
    }
    
    public JInternalFrame getView( ){
        return view;
    }
    private void setView(AppointmentsForDayView view ){
        this.view = view;
    }
    
}