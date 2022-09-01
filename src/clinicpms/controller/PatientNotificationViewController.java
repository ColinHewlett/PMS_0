/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.model.PatientNotification;
import clinicpms.model.Entity.Scope;
import java.beans.PropertyVetoException;
/**
 * ThePatient is used temporarily to start a refactored and restructured Patient process
 * -- primary difference between Patient & ThePatient is ThePatient.Collection inner class
 * -- this removes the need for a separate Patients class
 * -- thus EntityDescriptor.thePatient is also being updated to an ArrayList<ThePatient>
 */
import clinicpms.model.Patient;

import clinicpms.store.StoreException;
import clinicpms.view.views.DesktopView;
import clinicpms.view.View;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.JOptionPane;
import java.util.ArrayList;

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
        else{                                    
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
    }
    
    private void doDesktopViewControllerActionRequest(ActionEvent e){
        
    }
    
    private void doPrimaryViewActionRequest(ActionEvent e){
        EntityDescriptor.PatientNotificationViewControllerActionEvent actionCommand =
               EntityDescriptor.PatientNotificationViewControllerActionEvent.valueOf(e.getActionCommand());
        try{
            switch (actionCommand){
                case UNACTIONED_PATIENT_NOTIFICATIONS_REQUEST:
                    doUnactionedPatientNotificationsRequest();
                    break;
                case PATIENT_NOTIFICATIONS_REQUEST:
                    doPatientNotificationsRequest();
                    break;
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
        }catch(StoreException ex){
            displayErrorMessage(ex.getMessage(),
                    "Patient notification view controller error", 
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void doUnactionedPatientNotificationsRequest()throws StoreException{
        setNewEntityDescriptor(new EntityDescriptor());
        //PatientNotification patientNotification = new PatientNotification();
        //07/08/2022
        //patientNotification.getCollection().setScope(PatientNotification.Scope.UNACTIONED);
        //patientNotification.setScope(Scope.UNACTIONED);
        //getPatientNotificationsFor(patientNotification);
        sendPrimaryViewPatientNotifications(Scope.UNACTIONED);
    }
    
    private void doPatientNotificationsRequest()throws StoreException{
        setNewEntityDescriptor(new EntityDescriptor());
        //PatientNotification patientNotification = new PatientNotification();
        //patientNotification.setScope(PatientNotification.Scope.ALL);
        //getPatientNotificationsFor(patientNotification);
        sendPrimaryViewPatientNotifications(PatientNotification.Scope.ALL);
    }
    
    private void getPatientNotificationsFor(PatientNotification notification){
        try{
            notification.read();
            getNewEntityDescriptor().setPatientNotifications(
                    notification.get());
            pcSupportForView.addPropertyChangeListener(this.view);
            pcEvent = new PropertyChangeEvent(this,
               EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent.RECEIVED_PATIENT_NOTIFICATIONS.toString(),
               getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupportForView.firePropertyChange(pcEvent);
            pcSupportForView.removePropertyChangeListener(this.view);
        }catch (StoreException ex){
            displayErrorMessage(ex.getMessage(),
                    "Patient notification controller error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doActionPatientNotificationRequest(){
        ArrayList<PatientNotification> notifications = 
                getEntityDescriptorFromView().getRequest().getPatientNotifications();
        try{
            for (PatientNotification patientNotification : notifications){
                patientNotification.action();
                doUnactionedPatientNotificationsRequest();
            }
        }catch (StoreException ex){
            displayErrorMessage(
                    ex.getMessage(),
                    "Patient notification controller error", 
                    JOptionPane.WARNING_MESSAGE);
        } 
    }
    
    /**
     * method launches the patient notification editor view
     * -- and initialises the accompanying EntityDescriptor::patientNotifications as an empty ArrayList
     * -- this on basis that the patient notification editor view will then know view is used for creation of a new notification
     * -- 
     */
    private void doCreatePatientNotificationRequest(){
        setOldEntityDescriptor(getNewEntityDescriptor());
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().setPatientNotifications(new ArrayList<>());

        try{
            Patient patient = new Patient();
            patient.setScope(Scope.ALL);
            patient.read();
            getNewEntityDescriptor().setPatients(patient.get());
            //07/08/2022
            //Patient.Collection  patientCollection = patient.getCollection();
            //patientCollection.read();
            //getNewEntityDescriptor().setThePatients(patientCollection.get());
            View.setViewer(View.Viewer.PATIENT_NOTIFICATION_EDITOR_VIEW);
            secondaryView = View.factory(this, getNewEntityDescriptor(), desktopView);
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
        PatientNotification notification = getEntityDescriptorFromView().getRequest().getPatientNotification();
        setOldEntityDescriptor(getNewEntityDescriptor());
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().setPatientNotification(notification);
        try{
            /**
             * send view collection of all patients on system
             * -- in case user wants to select another patient to update(?)
             */
            Patient patient = new Patient();
            //07/08/2022
            patient.setScope(Scope.ALL);
            patient.read();
            getNewEntityDescriptor().setPatients(patient.get());
            //Patient.Collection  patientCollection = patient.getCollection();           
            //patientCollection.read();
            //getNewEntityDescriptor().setThePatients(patientCollection.get());
            /**
             * send view collection of previous notifications for this patient
             */
            PatientNotification patientNotification = new PatientNotification();
            patientNotification.setPatient(notification.getPatient());
            patientNotification.setScope(Scope.FOR_PATIENT);
            patientNotification.read();
            getNewEntityDescriptor().setPatientNotifications(patientNotification.get());
            //PatientNotification.Collection patientNotificationCollection = patientNotification.getCollection();
            //patientNotificationCollection.setScope(Scope.FOR_PATIENT);
            //patientNotificationCollection.read();
            //getNewEntityDescriptor().setPatientNotifications(patientNotificationCollection.get());
            View.setViewer(View.Viewer.PATIENT_NOTIFICATION_EDITOR_VIEW);
            secondaryView = View.factory(this, getNewEntityDescriptor(), desktopView);
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

    private void doSecondaryViewActionRequest(ActionEvent e)throws StoreException{
        View the_view = (View)e.getSource();
        setSecondaryView(the_view);
        switch (the_view.getMyViewType()){
            case PATIENT_NOTIFICATION_EDITOR_VIEW:
                EntityDescriptor.PatientNotificationViewControllerActionEvent actionCommand =
               EntityDescriptor.PatientNotificationViewControllerActionEvent.valueOf(e.getActionCommand());
                switch (actionCommand){
                    case MODAL_VIEWER_ACTIVATED:
                        getSecondaryView().initialiseView();
                        break;
                    case PATIENT_NOTIFICATION_EDITOR_CREATE_NOTIFICATION_REQUEST:
                        doPatientNotificationEditorCreateNotificationRequest();
                        break;
                    case PATIENT_NOTIFICATION_EDITOR_UPDATE_NOTIFICATION_REQUEST:
                        doPatientNotificationEditorUpdateNotificationRequest();
                        break;
                    case PATIENT_NOTIFICATION_EDITOR_CLOSE_VIEW_REQUEST:
                        doPatientNotificationEditorCloseViewRequest();
                        break;
                    case MODAL_VIEWER_DEACTIVATED:
                        closeSecondaryView();
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
            closeSecondaryView();
            sendPrimaryViewPatientNotifications(PatientNotification.Scope.UNACTIONED);
            
        }
    }
    
    private void doPatientNotificationEditorUpdateNotificationRequest()throws StoreException{
        PatientNotification patientNotification = 
                getEntityDescriptorFromView().getRequest().getPatientNotification();
        if (patientNotification!=null){
            patientNotification.update();
            closeSecondaryView();
            sendPrimaryViewPatientNotifications(PatientNotification.Scope.UNACTIONED);
        }
            
    }
    
    private void sendPrimaryViewPatientNotifications(PatientNotification.Scope scope)throws StoreException{
        PatientNotification patientNotification = new PatientNotification();
        //07/08/2022
        patientNotification.setScope(scope);
        //PatientNotification.Collection patientNotificationCollection = maPatientNotification.getCollection();
        //patientNotificationCollection.setScope(scope);
        patientNotification.read();
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().setPatientNotifications(patientNotification.get());
        pcSupportForView.addPropertyChangeListener(this.view);
        if (scope.equals(PatientNotification.Scope.UNACTIONED)){
            pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent.RECEIVED_UNACTIONED_NOTIFICATIONS.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
        }
        else{
            pcEvent = new PropertyChangeEvent(this,
                    EntityDescriptor.PatientNotificationViewControllerPropertyChangeEvent.RECEIVED_PATIENT_NOTIFICATIONS.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
        }
        
        pcSupportForView.firePropertyChange(pcEvent);
        pcSupportForView.removePropertyChangeListener(this.view);  
    }
    
    private void closeSecondaryView(){
        try{
            getSecondaryView().setClosed(true);
        }catch (PropertyVetoException ex){

        }
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
        View.setViewer(View.Viewer.PATIENT_NOTIFICATION_VIEW);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        //this.view.initialiseView();
        super.centreViewOnDesktop(desktopView, view);
    }
    
    public View getView(){
        return view;
    }
    
}
