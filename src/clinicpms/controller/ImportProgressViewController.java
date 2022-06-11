/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.view.views.DesktopView;
import clinicpms.view.View;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

/**
 *
 * @author colin
 */
public class ImportProgressViewController extends ViewController{
    public static enum Entity{APPOINTMENT, PATIENT, NONE}
    private static enum Operation {EXPORT, IMPORT};
    private EntityDescriptor entityDescriptorFromView = null;
    private EntityDescriptor newEntityDescriptor = null;
    private View view = null;
    private Entity newEntity = Entity.NONE;
    private Entity oldEntity = Entity.NONE;
    private PropertyChangeSupport pcSupportForView = null;
    private PropertyChangeEvent pcEvent = null;
    private ActionListener myController = null;
    private Operation operation = Operation.IMPORT;
    
    private Operation getOperation(){ 
        return operation;
    }
    
    private void setOperation(Operation value){
        operation = value;
    }
    
    private Entity getNewEntity(){ 
        return this.newEntity;
    }
    
    private void setNewEntity(Entity entity){
        newEntity = entity;
    }
    
    private Entity getOldEntity(){ 
        return oldEntity;
    }
    
    private void setOldEntity(Entity entity){
        oldEntity = entity;
    }
    
    private EntityDescriptor getNewEntityDescriptor(){
        return newEntityDescriptor;
    }
    
    private void setNewEntityDescriptor(EntityDescriptor value){
        this.newEntityDescriptor = value;
    }
    
    private void setEntityDescriptorFromView(EntityDescriptor value){
        this.entityDescriptorFromView = value;
    }
    
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    public ImportProgressViewController(DesktopViewController controller, DesktopView desktopView, EntityDescriptor entityDescriptor){
        View.setViewer(View.Viewer.EXPORT_PROGRESS_VIEW);
        this.setMyController(controller);
        this.setNewEntityDescriptor(entityDescriptor);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);
        this.view.addInternalFrameClosingListener();
        super.centreViewOnDesktop(desktopView, view);
        pcSupportForView = new PropertyChangeSupport(this);
        pcSupportForView.addPropertyChangeListener(getView());
    }
    
    @Override
    public EntityDescriptor getEntityDescriptorFromView(){
        return entityDescriptorFromView;
    }
    
    private void doDesktopViewControllerAction(ActionEvent e){
        ActionEvent actionEvent = null;
        DesktopViewController.DesktopViewControllerActionEvent actionCommand =
               DesktopViewController.DesktopViewControllerActionEvent.valueOf(e.getActionCommand());
        
        switch (actionCommand){
            case IMPORT_EXPORT_PATIENT_DATA_COMPLETED:
                setOldEntity(getNewEntity());
                setNewEntity(Entity.APPOINTMENT);
                pcEvent = new PropertyChangeEvent(this,
                            EntityDescriptor.ImportExportProgressViewControllerPropertyChangeEvent.
                                    PREPARE_FOR_RECEIPT_OF_APPOINTMENT_PROGRESS.toString(),
                            getOldEntity(),getNewEntity());
                pcSupportForView.firePropertyChange(pcEvent);
                break;
                
            case IMPORT_EXPORT_APPOINTMENT_DATA_COMPLETED:
                actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewController.DesktopViewControllerActionEvent.IMPORT_EXPORT_MIGRATED_SURGERY_DAYS_ASSIGNMENT.toString());
                getMyController().actionPerformed(actionEvent);
                
                setOldEntity(getNewEntity());
                setNewEntity(Entity.NONE);
                pcEvent = new PropertyChangeEvent(this,
                            EntityDescriptor.ImportExportProgressViewControllerPropertyChangeEvent.
                                    OPERATION_COMPLETED.toString(),
                            getOldEntity(),getNewEntity());
                pcSupportForView.firePropertyChange(pcEvent);
                break;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        ActionEvent actionEvent = null;
        
        if (e.getSource() instanceof DesktopViewController){
            doDesktopViewControllerAction(e);
        }else{
            EntityDescriptor.ImportExportProgressViewControllerActionEvent actionCommand =
               EntityDescriptor.ImportExportProgressViewControllerActionEvent.valueOf(e.getActionCommand());
            
            switch (actionCommand){
                case IMPORT_EXPORT_START_REQUEST:
                    setOldEntity(getNewEntity());
                    setNewEntity(Entity.PATIENT);
                    pcEvent = new PropertyChangeEvent(this,
                                EntityDescriptor.ImportExportProgressViewControllerPropertyChangeEvent.
                                        PREPARE_FOR_RECEIPT_OF_PATIENT_PROGRESS.toString(),
                                getOldEntity(),getNewEntity());
                    pcSupportForView.firePropertyChange(pcEvent);
                    break;

                case READY_FOR_RECEIPT_OF_PATIENT_PROGRESS:
                    actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewController.DesktopViewControllerActionEvent.IMPORT_EXPORT_PATIENT_DATA.toString());
                    getMyController().actionPerformed(actionEvent);
                    break;

                case READY_FOR_RECEIPT_OF_APPOINTMENT_PROGRESS:                 
                    actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewController.DesktopViewControllerActionEvent.IMPORT_EXPORT_APPOINTMENT_DATA.toString());
                    getMyController().actionPerformed(actionEvent);
                    break;

                case IMPORT_EXPORT_PROGRESS_CLOSE_NOTIFICATION:
                    actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        DesktopViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
                    getMyController().actionPerformed(actionEvent);
                    break;
            }
        }
        
        
    }
    
    public View getView( ){
        return view;
    }
}
