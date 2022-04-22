/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

//import clinicpms.controller.EntityDescriptor;
//import clinicpms.model.Patient;
import clinicpms.model.PatientNotification;
/**
 * Patients was used to replace the use of EntityDescriptor.Patient
 * -- EntityDescriptor.thePatient getter/setters a partial approach to this end
 */
//import clinicpms.model.Patients;
/**
 * ThePatient is used temporarily to start a refactored and restructured Patient process
 * -- primary difference between Patient & ThePatient is ThePatient.Collection inner class
 * -- this removes the need for a separate Patients class
 * -- thus EntityDescriptor.thePatient is also being updated to an ArrayList<ThePatient>
 */
import clinicpms.model.ThePatient;

import clinicpms.store.StoreException;
import clinicpms.view.DesktopView;
import clinicpms.view.View;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.JOptionPane;
import java.util.ArrayList;
//import java.util.Iterator;

/**
 *
 * @author colin
 */
public class PatientNotificationViewController extends ViewController{
    private ActionListener myController = null;
    private PropertyChangeSupport pcSupportForView = null;
    private PropertyChangeEvent pcEvent = null;
    private View view = null;
    private EntityDescriptor oldEntityDescriptor = new EntityDescriptor();
    private EntityDescriptor newEntityDescriptor = new EntityDescriptor();
    private EntityDescriptor entityDescriptorFromView = null;
    private DesktopView desktopView = null;
    private View secondaryView = null;
    
    private View getSecondaryView(){
        return secondaryView;
    }
    
    private void setSecondaryView(View value){
        secondaryView = value;
    }
    
    private EntityDescriptor getNewEntityDescriptor(){
        return newEntityDescriptor;
    }
    
    private void setNewEntityDescriptor(EntityDescriptor value){
        newEntityDescriptor = value;
    }
    
    private EntityDescriptor getOldEntityDescriptor(){
        return oldEntityDescriptor;
    
    }
    
    private void setOldEntityDescriptor(EntityDescriptor value){
        oldEntityDescriptor = value;
    }
    
    private void setMyController(ActionListener controller){
        myController = controller;
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        if (e.getSource() instanceof DesktopViewController){
            doDesktopViewControllerActionRequest(e);
        }
                                            
        View the_view = (View)e.getSource();
        setEntityDescriptorFromView(the_view.getEntityDescriptor());
        switch (the_view.getMyViewType()){
            case PATIENT_NOTIFICATION_VIEW:
                doPrimaryViewActionRequest(e);
                break;
            default:
                try{
                    doSecondaryViewActionRequest(e);
                }catch (StoreException ex){
                    
                }
        }
    }
    
    private void doDesktopViewControllerActionRequest(ActionEvent e){
        
    }
    
    private void doPrimaryViewActionRequest(ActionEvent e){
        EntityDescriptor.PatientNotificationViewControllerActionEvent actionCommand =
               EntityDescriptor.PatientNotificationViewControllerActionEvent.valueOf(e.getActionCommand());
        switch (actionCommand){
            case ACTION_PATIENT_NOTIFICATION_REQUEST:
                doActionPatientNotificationRequest();
                break;
            case CREATE_PATIENT_NOTIFICATION_REQUEST:
                doCreatePatientNotificationRequest();
                break;
            case UPDATE_PATIENT_NOTIFICATION_REQUEST:
                doUpdatePatientNotificationRequest();
                break;
        }
    }
    
    private void doActionPatientNotificationRequest(){
        
    }
    
    /**
     * method launches the patient notification editor view
     * -- and initialises the accompanying EntityDescriptor::patientNotifications as an empty ArrayList
     * -- this on basis that the patient notification editor view will then know view is used for creation of a new notification
     * -- 
     */
    private void doCreatePatientNotificationRequest(){
        clinicpms.model.Patients patients = null;
        setOldEntityDescriptor(getNewEntityDescriptor());
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().setPatientNotifications(new ArrayList<>());

        try{
            /*
            refer to notes added to import list
            patients = new Patients();
            patients.read();
            */
            ThePatient patient = new ThePatient();
            ThePatient.Collection  patientCollection = patient.getCollection();
            patientCollection.read();
            getNewEntityDescriptor().setThePatients(patientCollection.get());
            View.setViewer(View.Viewer.PATIENT_NOTIFICATION_EDITOR_VIEW);
            setSecondaryView(View.factory(this, getNewEntityDescriptor(), desktopView));
            //note: View.factory when opening a modal JInternalFrame does not return until the JInternalFrame has been closed
            ActionEvent actionEvent = new ActionEvent(
                   this,ActionEvent.ACTION_PERFORMED,
                   DesktopViewController.DesktopViewControllerActionEvent.MODAL_VIEWER_CLOSED.toString());
            this.myController.actionPerformed(actionEvent);
        }catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"PatientNotificaionViewController error",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doUpdatePatientNotificationRequest(){
        
    }

    private void doSecondaryViewActionRequest(ActionEvent e)throws StoreException{
        View the_view = (View)e.getSource();
        switch (the_view.getMyViewType()){
            case PATIENT_NOTIFICATION_EDITOR_VIEW:
                EntityDescriptor.PatientNotificationViewControllerActionEvent actionCommand =
               EntityDescriptor.PatientNotificationViewControllerActionEvent.valueOf(e.getActionCommand());
                switch (actionCommand){
                    case PATIENT_NOTIFICATION_EDITOR_CREATE_NOTIFICATION_REQUEST:
                        doPatientNotificationEditorCreateNotificationRequest();
                        break;
                    case PATIENT_NOTIFICATION_EDITOR_UPDATE_NOTIFICATION_REQUEST:
                        doPatientNotificationEditorUpdateNotificationRequest();
                        break;
                    case PATIENT_NOTIFICATION_EDITOR_CLOSE_VIEW_REQUEST:
                        doPatientNotificationEditorCloseViewRequest();
                        break;
                    case MODAL_VIEWER_ACTIVATED:
                        break;
                }
                break;
                
            default:
                JOptionPane.showMessageDialog(getView(), 
                        "Unrecognised view type specified in PatientNotificationViewController::doSecondaryViewActionRequest()",
                        "Patient Notification View Controller Error", 
                        JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doPatientNotificationEditorCreateNotificationRequest()throws StoreException{
        PatientNotification patientNotification = 
                getEntityDescriptorFromView().getRequest().getPatientNotification();
        if (patientNotification!=null){
            patientNotification.insert();
            PatientNotification newPatientNotification = patientNotification.read();
            setNewEntityDescriptor(new EntityDescriptor());
            getNewEntityDescriptor().setPatientNotification(newPatientNotification);
            this.pcSupportForView.addPropertyChangeListener(getSecondaryView());
            pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent.
                        RECEIVED_PATIENT_NOTIFICATION.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupportForView.firePropertyChange(pcEvent);
            this.pcSupportForView.removePropertyChangeListener(getSecondaryView());
            
            
            PatientNotification.Collection patientNotificationCollection = patientNotification.getCollection();
            patientNotificationCollection.setScope(PatientNotification.Scope.ALL_BY_KEY);
            ArrayList<PatientNotification> patientNotifications = patientNotificationCollection.get();
            setNewEntityDescriptor(new EntityDescriptor());
            getNewEntityDescriptor().setPatientNotifications(patientNotifications);
            this.pcSupportForView.addPropertyChangeListener(getSecondaryView());
            pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent.
                        RECEIVED_PATIENT_NOTIFICATIONS.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupportForView.firePropertyChange(pcEvent);
            this.pcSupportForView.removePropertyChangeListener(getSecondaryView());
            
        }
    }
    
    private void doPatientNotificationEditorUpdateNotificationRequest(){
        
    }
    
    private void doPatientNotificationEditorCloseViewRequest(){
        
    }
    
    private DesktopView getDeskTopView(){
        return desktopView;
    }
    
    private void setDesktopView(DesktopView value){
        desktopView = value;
    }
    
    private void setEntityDescriptorFromView(EntityDescriptor value){
        entityDescriptorFromView = value;
    }
    
    @Override
    public EntityDescriptor getEntityDescriptorFromView(){
        return entityDescriptorFromView;
    }
    public PatientNotificationViewController(DesktopViewController controller, 
                                                DesktopView desktopView)
                                                throws StoreException{
        setMyController(controller);
        setDesktopView(desktopView);
        pcSupportForView = new PropertyChangeSupport(this);
        this.newEntityDescriptor = new EntityDescriptor();
        this.oldEntityDescriptor = new EntityDescriptor();
        /**
         * -- construct a PatientNotification object
         * -- initialise its Collection with the stored unactioned notifications on the system
         * -- store the patient notification object in an EntityDescriptor object
         */
        PatientNotification patientNotification = new PatientNotification();
        patientNotification.getCollection().setScope(PatientNotification.Scope.UNACTIONED);
        patientNotification.getCollection().read();
        getNewEntityDescriptor().setPatientNotifications(
                patientNotification.getCollection().get());
        
        View.setViewer(View.Viewer.PATIENT_NOTIFICATION_VIEW);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        super.centreViewOnDesktop(desktopView, view);
    }
    
    public View getView(){
        return view;
    }
    
}
