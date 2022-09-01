/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.controller.DesktopViewController.DesktopViewControllerActionEvent;
import clinicpms.model.Entity;
import clinicpms.model.Patient;
import clinicpms.model.Entity.Scope;
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
import javax.swing.JFrame;
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
                doThePatientViewCreateRequest();
                break;
            case PATIENT_VIEW_UPDATE_REQUEST:
                doThePatientViewUpdateRequest();
                break;
            case PATIENT_REQUEST:
                doThePatientRequest(e);
                break;
            case NULL_PATIENT_REQUEST:
                doNullThePatientRequest();
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
    
    private void doAppointmentHistoryChangeNotification(){
        //22/07/2022 08:56
        //ThePatient patient = getEntityDescriptorFromView().getRequest().getThePatient();
        Patient patient = getEntityDescriptorFromView().getPatient();
        if (patient.getIsKeyDefined()){
            try{
                patient.setScope(Scope.SINGLE);
                Patient p = patient.read();
            
                this.initialiseNewEntityDescriptor();
                //serialisePatientToEDPatient(p);
                getNewEntityDescriptor().setPatient(p);
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
    
    private void doThePatientViewCreateRequest(){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        Patient patient = getEntityDescriptorFromView().getRequest().getPatient();
        if (!patient.getIsKeyDefined()){
            try{
                patient.insert();
                patient.setScope(Scope.SINGLE);
                patient = patient.read();
                initialiseNewEntityDescriptor();
                getNewEntityDescriptor().setPatient(patient);
                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                            PATIENT_RECEIVED.toString(),
                        getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
                //07/08/2022 08:53
                //Patient.Collection patients = patient.getCollection();
                ArrayList<Patient> patients = new ArrayList<>();
                initialiseNewEntityDescriptor();
                patient.setScope(Entity.Scope.ALL);
                patient.read();
                getNewEntityDescriptor().setPatients(patient.get());
                
                //patients.read();
                //getNewEntityDescriptor().setThePatients(patients.get());
                pcEvent = new PropertyChangeEvent(this,
                            EntityDescriptor.PatientViewControllerPropertyEvent.
                                    PATIENTS_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
                
            }catch (StoreException ex){
                displayErrorMessage(ex.getMessage() + "\n"
                        + "Exception raised in PatientViewController.doThePatientViewCreateRequest()",
                        "Patient view controller error",JOptionPane.WARNING_MESSAGE);
            }
        }else{
            displayErrorMessage("StoreException -> Key defined in new patient to be created; "
                    + "new patient create operation aborted", "Patient view controller", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doThePatientViewUpdateRequest(){
        setEntityDescriptorFromView(view.getEntityDescriptor()); 
        Patient patient = getEntityDescriptorFromView().getRequest().getPatient();
        if (patient.getIsKeyDefined()){
            try{
                patient.update();
                //patient = patient.read();//could be "patient.read()";
                patient.setScope(Scope.SINGLE);
                patient.read();
                initialiseNewEntityDescriptor();
                getNewEntityDescriptor().setPatient(patient);
                pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.PatientViewControllerPropertyEvent.
                    PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }catch (StoreException ex){
                displayErrorMessage(ex.getMessage() +"\n"
                        + "Exception raised in PatientViewController::doPatientViewUpdateRequest()",
                        "Patient view controller error", JOptionPane.WARNING_MESSAGE);           
            }
        }else{
            displayErrorMessage("Requested patient for update has no key defined, update action aborted",
                    "Patient view controller error", JOptionPane.WARNING_MESSAGE);
        }
        
    }
    
    private void doThePatientRequest(ActionEvent e){
        setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
        Patient patient = getEntityDescriptorFromView().getRequest().getPatient();
        if (patient.getIsKeyDefined()){
            try{
                patient.setScope(Scope.SINGLE);
                Patient p = patient.read();
                this.initialiseNewEntityDescriptor();
                getNewEntityDescriptor().setPatient(p);
                PropertyChangeListener[] pcls = pcSupportForView.getPropertyChangeListeners();
                pcEvent = new PropertyChangeEvent(this,
                        EntityDescriptor.PatientViewControllerPropertyEvent.
                        PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }catch (StoreException ex){
                displayErrorMessage(ex.getMessage() + "\n"
                        + "Exception raised in PatientViewController::doPatientRequest()",
                        "Patient view controller error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }else{
            displayErrorMessage("No key defined for requested patient; fetch operation aborted",
                    "Patient view controller error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doNullThePatientRequest(){
        initialiseNewEntityDescriptor();
        Patient patient = new Patient();
        getNewEntityDescriptor().setPatient(patient);
        pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.PatientViewControllerPropertyEvent.
                            NULL_PATIENT_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
        pcSupportForView.firePropertyChange(pcEvent);
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
        Patient patient = new Patient();
        patient.setScope(Scope.ALL);
        patient.read();
        //07/08/2022
        //patient.getCollection().read();
        getNewEntityDescriptor().setPatients(patient.get());
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
        else{
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
        
    }
    
    public View getView( ){
        return view;
    }
}
