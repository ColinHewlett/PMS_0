/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.store.AccessStore;
import clinicpms.store.Store;
import clinicpms.store.DbLocationStorex;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.interfaces.IStore;
import clinicpms.view.base.DatabaseLocatorView;
import clinicpms.view.base.DesktopView;
import clinicpms.view.View;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import javax.swing.JOptionPane;
/**
 *
 * @author colin
 */
public class DatabaseLocatorViewController extends ViewController {
    private ActionListener myController = null;
    private DesktopView desktopView;
    private EntityDescriptor edFromView = null;
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    private View view = null;
    private PropertyChangeSupport pcSupport = null;
    private PropertyChangeEvent pcEvent = null;
    
    public DatabaseLocatorViewController(ActionListener myController, DesktopView desktopView){
        this.myController = myController;
        this.desktopView = desktopView;
        this.pcSupport = new PropertyChangeSupport(this);
        setNewEntityDescriptor(new EntityDescriptor());
        initialiseNewEntityDescriptor();
        String s = getDatabaseLocation();
        getNewEntityDescriptor().getRequest().setDatabaseLocation(s);
        this.view = new DatabaseLocatorView(this, getNewEntityDescriptor());
        super.centreViewOnDesktop(desktopView, view);
        this.view.addInternalFrameClosingListener(); 
        this.view.initialiseView();
        this.pcSupport.addPropertyChangeListener(this.view);
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        if (e.getActionCommand().equals(   
            ViewController.DatabaseLocatorViewControllerActionEvent.DATABASE_LOCATION_REQUEST.toString())){
            setEntityDescriptorFromView(this.view.getEntityDescriptor());
            try{
                setDatabaseLocation(getEntityDescriptorFromView().getRequest().getDatabaseLocation());
                getNewEntityDescriptor().getRequest().setDatabaseLocation(getDatabaseLocation());
                /*
                IStore store = Store.factory();
                store.closeConnection();  
                */
                pcEvent = new PropertyChangeEvent(this,
                    ViewController.DatabaseLocatorViewPropertyEvent.DATABASE_LOCATION_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupport.firePropertyChange(pcEvent);
            }
            catch (IOException ex){
                
            }        
        }
        else if (e.getActionCommand().equals(
                    DatabaseLocatorViewControllerActionEvent.
                            DATABASE_LOCATOR_VIEW_CLOSED.toString())){
                ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
                getMyController().actionPerformed(actionEvent);
        }

    }
    
    public View getView(){
        return view;
    }
    
    @Override
    public EntityDescriptor getEntityDescriptorFromView(){
        return edFromView;
    }
    
     private void setEntityDescriptorFromView(EntityDescriptor e){
        this.edFromView = e;
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
    
    private String getDatabaseLocation(){
        /*

        String result = null;
        File file = null;
        BufferedReader br;
        URL s = this.getClass().getResource("../database/databasePath.txt");
        String path = s.getPath();
        file = new File (path);
        try{
            if (file.exists()){
                FileReader fr = new FileReader(file);
                br = new BufferedReader(fr);
                String location = br.readLine();
                result = location;
            }
        }
        catch (FileNotFoundException ex){
            JOptionPane.showInternalMessageDialog(desktopView.getContentPane(), ex.getMessage());
        }
        catch (IOException ex){
            JOptionPane.showInternalMessageDialog(desktopView.getContentPane(), ex.getMessage());
        }
        */
        DbLocationStorex store = null;
        String result = null;
        try{
            store = Store.getDbLocationStore();
            result = store.read();
            return result;
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"DatabaseLocationViewController error",JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }
    
    private String setDatabaseLocation(String location)throws IOException{
        DbLocationStorex store = null;
        String result = null;
        try{
            store = Store.getDbLocationStore();
            result = store.update(location);
            return result;
        }
        catch (StoreException ex){
            String message = ex.getMessage();
            displayErrorMessage(message,"DatabaseLocationViewController error",JOptionPane.WARNING_MESSAGE);
        }
        /*
        File file;
        BufferedWriter bw;
        URL s = this.getClass().getResource("../database/databasePath.txt");
        String path = s.getPath();
        file = new File (path);
        if (file.exists()){
            boolean flag = file.delete();
            flag = flag;
        }
        if (file.createNewFile()){
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(location);
            bw.close();
        }
     */ 
        return result;
    }
}
