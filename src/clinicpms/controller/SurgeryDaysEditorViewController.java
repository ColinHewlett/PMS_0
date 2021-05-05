/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import static clinicpms.controller.ViewController.displayErrorMessage;
import clinicpms.store.Store;
import clinicpms.store.interfaces.IStore;
import clinicpms.store.exceptions.StoreException;
import clinicpms.view.DesktopView;
import clinicpms.view.View;
import clinicpms.view.interfaces.IView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Dictionary;
import javax.swing.JOptionPane;
/**
 *
 * @author colin
 */
public class SurgeryDaysEditorViewController extends ViewController{
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private ActionListener myController = null;
    private View view = null;
    private DesktopView desktopView = null;
    
    public SurgeryDaysEditorViewController(ActionListener controller, DesktopView desktopView)throws StoreException{
        this.myController = controller;
        IStore store = Store.factory();
        Dictionary<String,Boolean> d  = store.readSurgeryDays();
        setNewEntityDescriptor(new EntityDescriptor());
        initialiseNewEntityDescriptor();
        getNewEntityDescriptor().getRequest().setSurgeryDays(d);
        View.setViewer(View.Viewer.SURGERY_DAYS_EDITOR_VIEW);
        this.view = View.factory(this, getNewEntityDescriptor(), desktopView);    
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        if (e.getActionCommand().equals(SurgeryDaysEditorViewControllerActionEvents.SURGERY_DATES_EDITOR_REQUEST.toString())){
            this.view = (View)e.getSource();
            try{
                this.view.setClosed(true);
            }
            catch (PropertyVetoException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"SurgeryDaysEditorViewController error",JOptionPane.WARNING_MESSAGE);
            }
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            Dictionary<String,Boolean> surgeryDays = getEntityDescriptorFromView().getRequest().getSurgeryDays();
            try{
                IStore store = Store.factory();
                surgeryDays = store.updateSurgeryDays(surgeryDays);
            }
            catch(StoreException ex){
                String message = ex.getMessage();
                displayErrorMessage(message,"SurgeryDaysEditorViewController error",JOptionPane.WARNING_MESSAGE);
            }
        }   
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
    public EntityDescriptor getEntityDescriptorFromView(){
        return this.entityDescriptorFromView;
    }
    private void setEntityDescriptorFromView(EntityDescriptor e){
        this.entityDescriptorFromView = e;
    }
    
    private ActionListener getMyController(){
        return myController;
    }
    private void setMyController(ActionListener myController ){
        this.myController = myController;
    }
    
    public View getView(){
        return view;
    }
    
    private void initialiseNewEntityDescriptor(){
        setOldEntityDescriptor(getNewEntityDescriptor());
        setNewEntityDescriptor(new EntityDescriptor());
        getNewEntityDescriptor().getRequest().setDay(getOldEntityDescriptor().getRequest().getDay());
    }
}
