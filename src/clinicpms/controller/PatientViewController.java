/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.controller.DesktopViewController.DesktopViewControllerActionEvent;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.view.views.DesktopView;
import clinicpms.view.View;
import clinicpms.view.interfaces.IView;
import clinicpms.store.StoreException;
import java.beans.PropertyChangeSupport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
/**
 *
 * @author colin
 */
public class PatientViewController extends ViewController {
    private ActionListener myController = null;
    private PropertyChangeSupport pcSupportForView = null;
    //private PropertyChangeSupport pcSupportForPatientSelector = null;
    private PropertyChangeEvent pcEvent = null;
    private View view = null;
    private EntityDescriptor oldEntityDescriptor = new EntityDescriptor();
    private EntityDescriptor newEntityDescriptor = new EntityDescriptor();
    private EntityDescriptor entityDescriptorFromView = null;
    private JFrame owningFrame = null;
    private String message = null;

    
    private void cancelView(ActionEvent e){
        try{
            getView().setClosed(true);
            myController.actionPerformed(e);
        }
        catch (PropertyVetoException e1) {
            
        }
    }
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
            }  
        }
        return ra;
    }
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
                    //case DENTAL_RECALL_FREQUENCY -> vp.setHygieneRecallFrequency((p.getRecall().getDentalFrequency()));
                    case DENTAL_RECALL_FREQUENCY:
                        if (p.getRecall().getDentalFrequency()==null) p.getRecall().setDentalFrequency(0);
                        else vp.setDentalRecallFrequency((p.getRecall().getDentalFrequency()));
                        break;
                    case DOB:
                        vp.setDOB((p.getDOB()));
                        break;
                    case GENDER:
                        vp.setGender((p.getGender()));
                        break;
                    case PHONE1:
                        vp.setPhone1((p.getPhone1()));
                        break;
                    case PHONE2:
                        vp.setPhone2((p.getPhone2()));
                        break;
                    case IS_GUARDIAN_A_PATIENT:
                        if (p.getIsGuardianAPatient()==null) vp.setIsGuardianAPatient((false));
                        else vp.setIsGuardianAPatient(p.getIsGuardianAPatient());
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
     * A request for a collection of all patient records is processed
     * -- collection of all patients fetched from model
     * -- EntityDescriptor.Collection patients cleared
     * -- each model patient is serialised into EntityDescriptor.Patient and then
     * the EntityDescriptor.Patient added to the EntityDescriptor.Collection of
     * patients
     * -- note, the objects the model Patient encapsulates are not included in 
     * the serialisation
     * @throws StoreException passed up the line to caller to process
     */
    private void serialisePatientsToEDCollection(ArrayList<Patient> patients) throws StoreException{
        //fetch all patients on the system from the model
        
        getNewEntityDescriptor().getPatients().getData().clear();
        Iterator<Patient> patientsIterator = patients.iterator();
        while(patientsIterator.hasNext()){       
            getNewEntityDescriptor().setPatient(new EntityDescriptor().getPatient());
            Patient patient = patientsIterator.next();
            RenderedPatient p = renderPatient(patient);
            getNewEntityDescriptor().getPatient().setData(p);
            getNewEntityDescriptor().getPatients().getData().add(getNewEntityDescriptor().getPatient());
        }
    }
    /**
     * A request for a patient object is processed
     * -- the selected patient's key is fetched from the EntityDescriptor.Selection.Patient object
     * -- the model patient with this key is fetched and serialised into the EntityDescriptor.Patient object
     * -- -- note: the model Patient object is flattened into EntityDescriptor.Patient.Data & EntityDescriptor.Patient.PatientGuardian
     * -- -- reference update dated 30/07/2021 09:05
     * -- -- also commented out updates to EntityDescriptor.Request.Patient.PatientGuardian (formerly EntityDescriptor.Request.PatientGuardian)
     * -- -- note also:code assumes a patient guardian does not also have a patient guardian
     * @throws StoreException 
     */
    private void serialisePatientToEDPatient(Patient patient) throws StoreException{
        RenderedPatient p = renderPatient(patient);
        getNewEntityDescriptor().getPatient().setData(p);
        /**
         * note: its the view controller's responsibility to initialise EntityDescriptor.Request.Patient
         * -- the Patient View offers 2 choices; update current patient or create a new patient
         * -- if an update is selected the view controller needs to know the key value of the currently selected patient to update
         * -- since the view cannot access the key field of the rendered patient object (it is a protected property) it must ensure the key field is already initialised 
         */
        getNewEntityDescriptor().getRequest().getPatient().setData(p);
        if (p.getIsGuardianAPatient()){
            if (patient.getGuardian() != null){
                RenderedPatient g = renderPatient(patient.getGuardian());
                /**
                 * update 30/07/2021 09:05
                 * -- when a new entity descriptor is constructed cannot include in the construction a new Patient to represent a patient guardian because EntityDescriptor.Patient constructor would be recursively invoked
                 * -- hence has to be done here to avoid a NullPointerException in case PatientGuardian is null shich probably be the case 
                 */

                getNewEntityDescriptor().getPatient().setPatientGuardian(new EntityDescriptor().getPatient());
                getNewEntityDescriptor().getPatient().getPatientGuardian().setData(g);
                //getNewEntityDescriptor().getPatientGuardian().setData(g);  
                //getNewEntityDescriptor().getRequest().getPatientGuardian().setData(g);
            }
            else
                //getNewEntityDescriptor().setPatientGuardian(null);
                getNewEntityDescriptor().getPatient().setPatientGuardian(null);
                
        } 
        //else getNewEntityDescriptor().setPatientGuardian(null);
        else getNewEntityDescriptor().getPatient().setPatientGuardian(null);
        
        ArrayList<Appointment> appointments;
        if (patient.getAppointmentHistory().getDentalAppointments()!=null){
            if (patient.getAppointmentHistory().getDentalAppointments().size() > 0){
                appointments = patient.getAppointmentHistory().getDentalAppointments();
                serialisePatientAppointmentHistory(appointments);
            }
        }
    }
    private void serialisePatientAppointmentHistory(ArrayList<Appointment> appointments){
        EntityDescriptor ed = null;
        ArrayList<EntityDescriptor.Appointment> xx = new ArrayList<>();
        Iterator<Appointment> appointmentsIterator = appointments.iterator();
        while (appointmentsIterator.hasNext()){
            Appointment appointment = appointmentsIterator.next();
            RenderedAppointment a = renderAppointment(appointment);
            getNewEntityDescriptor().setAppointment(new EntityDescriptor().getAppointment());
            getNewEntityDescriptor().getAppointment().setData(a);
            getNewEntityDescriptor().getAppointment().setAppointee(getNewEntityDescriptor().getPatient());
            getNewEntityDescriptor().getPatientAppointmentHistory().getDentalAppointments()
                    .add(getNewEntityDescriptor().getAppointment());
        }   
    }
    private Patient makePatientFrom(EntityDescriptor.Patient  eP){
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
    /**
     * On entry view controller's EntityFromView.Selection.Patient contains the 
     * view's currently selected patient. This is deserialised into a model Patient
     * object
     * @return model Patient object
     */
    private Patient deserialisePatientFromEDRequest(){
        Patient patient = makePatientFrom(getEntityDescriptorFromView().getRequest().getPatient());
        if (getEntityDescriptorFromView().getRequest().getPatient().getData().getIsGuardianAPatient()){
            if (getEntityDescriptorFromView().getRequest().getPatientGuardian()!=null){
                patient.setGuardian(makePatientFrom(
                        getEntityDescriptorFromView().getRequest().getPatientGuardian()));
            }
        }
        return patient;
    }
    private void setEntityDescriptor(EntityDescriptor value){
        this.newEntityDescriptor = value; 
    }
    private EntityDescriptor getNewEntityDescriptor(){
        return this.newEntityDescriptor;
    }
    private EntityDescriptor getOldEntityDescriptor(){
        return this.oldEntityDescriptor;
    }
    private void setOldEntityDescriptor(EntityDescriptor e){
        this.oldEntityDescriptor = e;
    }
    private void setNewEntityDescriptor(EntityDescriptor e){
        this.newEntityDescriptor = e;
    }
    public EntityDescriptor getEntityDescriptorFromView(){
        return this.entityDescriptorFromView;
    }
    private void setEntityDescriptorFromView(EntityDescriptor e){
        this.entityDescriptorFromView = e;
    }
    
    private void doPrimaryViewActionRequest(ActionEvent e){
        EntityDescriptor.PatientViewControllerActionEvent actionCommand =
               EntityDescriptor.PatientViewControllerActionEvent.valueOf(e.getActionCommand());
        switch (actionCommand){
            case APPOINTMENT_VIEW_CONTROLLER_REQUEST: //on selection of row in appointment history table
                doAppointmentViewControllerRequest();
                break;
            case PATIENT_VIEW_CLOSED://notification from view uts shutting down
                doPatientViewClosed();
                break;
            case PATIENT_VIEW_CREATE_REQUEST:
                doPatientViewCreateRequest();
                break;
            case PATIENT_VIEW_UPDATE_REQUEST:
                doPatientViewUpdateRequest();
                break;
            case PATIENT_REQUEST:
                doPatientRequest(e);
                break;
            case PATIENTS_REQUEST: //THIS NOT RAISED AS AN ACTION ANYWHERE
                doPatientsRequest(e);
                break;
            case NULL_PATIENT_REQUEST:
                doNullPatientRequest();
                break;     
        }
    }
    
    private void doSecondaryViewActionRequest(ActionEvent e){
        View the_view = (View)e.getSource();
        switch (the_view.getMyViewType()){
            case PATIENT_NOTIFICATION_EDITOR_VIEW:
                //do nothing
                break;
            default:
                JOptionPane.showMessageDialog(getView(), 
                        "Unrecognised view type specified in PatientViewController::doSecondaryViewActionRequest()",
                        "Patient View Controller Error", 
                        JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doDesktopViewControllerActionRequest(ActionEvent e){
        DesktopViewControllerActionEvent actionCommand =
               DesktopViewControllerActionEvent.valueOf(e.getActionCommand());
        switch (actionCommand){
            case APPOINTMENT_HISTORY_CHANGE_NOTIFICATION:
                doAppointmentHistoryChangeNotification();
                break;
            case VIEW_CLOSE_REQUEST://prelude to the Desktop VC closing down the Patient VC
                try{
                    getView().setClosed(true);   
                }catch (PropertyVetoException ex){
                //UnspecifiedError action
            }
                break;
        }
    }
    
    /**
     * The method handles a notification from the DesktopVC that a change has happened to the appointment history for the currently selected patient
     * -- it re-initialisation the display of the current patient in order that the change to its appointment history will be displayed
     */
    private void doAppointmentHistoryChangeNotification(){
        Patient patient = deserialisePatientFromEDRequest();
        if (patient.getKey() != null){
            try{
                Patient p = patient.read();
                this.initialiseNewEntityDescriptor();
                serialisePatientToEDPatient(p);
                getNewEntityDescriptor().getPatient().getData().setIsKeyDefined(true);
                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            }
        }

    }
    
    /**
     * appointment in Patient view's appointment history has been selected to request the appointment schedule for that day 
     * -- Request is forwarded onto the Desktop VC
     * -- the forwarded request references the Patient VC's EntityDescriptorFromView which contains details of the selected appointment for this patient; and thus the appointment schedule requested
     */
    private void doAppointmentViewControllerRequest(){  
        setEntityDescriptorFromView(view.getEntityDescriptor());
        ActionEvent actionEvent = new ActionEvent(
            this,ActionEvent.ACTION_PERFORMED,
            EntityDescriptor.PatientViewControllerActionEvent.APPOINTMENT_VIEW_CONTROLLER_REQUEST.toString());
        getMyController().actionPerformed(actionEvent);
    }
    
    /**
     * notification from view it is closing down
     * -- let DesktopVC know so it can close down the Patient VC
     */
    private void doPatientViewClosed(){
        ActionEvent actionEvent = new ActionEvent(
            this,ActionEvent.ACTION_PERFORMED,
            DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
        getMyController().actionPerformed(actionEvent); 
    }
    
    /**
     * method processes request to create a new patient
     * -- details for new patient fetched from the view and stored in the VC's EntityDescriptorFromView object
     * -- new patient stored in persistent store 
     * -- details of the patient are returned as a PropertyCangeEvent fired to the view, as well as the updated collection of patients on  the system now
     */
    private void doPatientViewCreateRequest(){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        Patient patient = deserialisePatientFromEDRequest();
        if (patient.getKey() == null){
            try{
                patient.insert();//was patient.create()
                int test2 = patient.getKey();
                patient = patient.read();
                //setOldEntityDescriptor(getNewEntityDescriptor());
                initialiseNewEntityDescriptor();
                serialisePatientToEDPatient(patient);

                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                            PATIENT_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);

                Patients patients = new Patients();
                patients.read();
                //setOldEntityDescriptor(getNewEntityDescriptor());
                initialiseNewEntityDescriptor();
                serialisePatientsToEDCollection(patients);
                pcEvent = new PropertyChangeEvent(this,
                            EntityDescriptor.PatientViewControllerPropertyEvent.
                                    PATIENTS_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            }
        }
        else {//throw null patient key expected, non null value received
            //UnspecifiedErrorException
        }
    }
    
    /**
     * method processes a view request to update the details of the currently selected patient
     * -- update patient details are stored on the VC's EntityDescriptorFromView object
     * -- updated patient details inserted in persistent store
     * -- PropertyChangeEvent fired to let view know the updated patient has been processed
     * -- could also send a further PropertyChangeEvent 
     */
    private void doPatientViewUpdateRequest(){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        Patient patient = deserialisePatientFromEDRequest();
        if (patient.getKey() != null){
            try{
                patient.update();
                patient = patient.read();
                //setOldEntityDescriptor(getNewEntityDescriptor());
                initialiseNewEntityDescriptor();
                serialisePatientToEDPatient(patient);

                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);

            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
        }
        else {//display an error message in view that non null key expected
            //UnspecifiedErrorException
        }
    }
    
    /**
     * method handles a view request to display the details of a patient the details of which afre fetched from the view's EntityDescriptor object 
     * @param e 
     */
    private void doPatientRequest(ActionEvent e){
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        Patient patient = deserialisePatientFromEDRequest();
        //PropertyChangeListener[] pcls = pcSupportForView.getPropertyChangeListeners();
        if (patient.getKey() != null){
            try{
                Patient p = patient.read();
                //setOldEntityDescriptor(getNewEntityDescriptor());
                this.initialiseNewEntityDescriptor();
                serialisePatientToEDPatient(p);
                getNewEntityDescriptor().getPatient().getData().setIsKeyDefined(true);
                //EntityDescriptor ed = getNewEntityDescriptor();
                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            }
        }
    }
    
    /**
     * AS FAR AS A CAN TELL THIS REQUEST IS NEVER RAISED
     */
    private void doPatientsRequest(ActionEvent e){
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        Patient patient = deserialisePatientFromEDRequest();
        //PropertyChangeListener[] pcls = pcSupportForView.getPropertyChangeListeners();
        if (patient.getKey() != null){
            try{
                Patient p = patient.read();
                //setOldEntityDescriptor(getNewEntityDescriptor());
                this.initialiseNewEntityDescriptor();
                serialisePatientToEDPatient(p);
                getNewEntityDescriptor().getPatient().getData().setIsKeyDefined(true);
                //EntityDescriptor ed = getNewEntityDescriptor();
                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                JOptionPane.showMessageDialog(getView(),
                                      new ErrorMessagePanel(ex.getMessage()));
            }
        }
    }
    
    private void doNullPatientRequest(){
        initialiseNewEntityDescriptor();
        Patient patient = new Patient();
        try{
            serialisePatientToEDPatient(patient);
            getNewEntityDescriptor().getPatient().getData().setIsKeyDefined(false);
            pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                                NULL_PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupportForView.firePropertyChange(pcEvent);  
        }
        catch (StoreException ex){
            //UnspecifiedError action
        }
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
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    
    public PatientViewController(DesktopViewController controller, DesktopView desktopView)throws StoreException{
        setMyController(controller);
        pcSupportForView = new PropertyChangeSupport(this);
        this.newEntityDescriptor = new EntityDescriptor();
        this.oldEntityDescriptor = new EntityDescriptor();
        Patients patients = new Patients();
        patients.read();
        serialisePatientsToEDCollection(patients);
        View.setViewer(View.Viewer.PATIENT_VIEW);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        super.centreViewOnDesktop(desktopView, view);
        
        this.view.addInternalFrameClosingListener(); 
        pcSupportForView.addPropertyChangeListener(
                EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENTS_RECEIVED.toString(),view);
        pcSupportForView.addPropertyChangeListener(
                EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENT_RECEIVED.toString(),view);
        pcSupportForView.addPropertyChangeListener(
                EntityDescriptor.PatientViewControllerPropertyEvent.
                        NULL_PATIENT_RECEIVED.toString(),view);
        view.initialiseView();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //PropertyChangeListener[] pcls;
        if (e.getSource() instanceof DesktopViewController){
            doDesktopViewControllerActionRequest(e);
        }

        View the_view = (View)e.getSource();
        switch (the_view.getMyViewType()){
            case PATIENT_VIEW:
                doPrimaryViewActionRequest(e);
                break;
            default:
                doSecondaryViewActionRequest(e);
                break;
        }
    }
    
    public View getView( ){
        return view;
    }
}
