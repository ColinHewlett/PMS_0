/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.model.Appointment;
import clinicpms.model.Appointments;
import clinicpms.model.Patient;
import clinicpms.store.AccessStore;
import clinicpms.store.CSVMigrationManager;
import clinicpms.store.interfaces.IStore;
import clinicpms.store.Store;
import clinicpms.store.Store.Storage;
import clinicpms.store.exceptions.StoreException;
import clinicpms.view.DesktopView;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class DesktopViewController extends ViewController{
    private boolean isDesktopPendingClosure = false;
    private DesktopView view = null;
    private ArrayList<AppointmentViewController> appointmentViewControllers = null;
    private ArrayList<PatientViewController> patientViewControllers = null;
    //private HashMap<ViewControllers,ArrayList<ViewController>> viewControllers = null;
     
    enum ViewControllers {
                            PATIENT_VIEW_CONTROLLER,
                            APPOINTMENT_VIEW_CONTROLLER,
                         }
   
    
    
    private DesktopViewController(){
        
        //setAppointmentsViewController(new AppointmentViewController(this));
        //setPatientViewController(new PatientViewController(this));
        view = new DesktopView(this);
        view.setSize(850, 700);
        view.setVisible(true);
        setView(view);
        //view.setContentPane(view);
        
        appointmentViewControllers = new ArrayList<>();
        patientViewControllers = new ArrayList<>();
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        String s;
        s = e.getSource().getClass().getSimpleName();
        switch(s){
            case "DesktopView" -> doDesktopViewAction(e);
            case "AppointmentViewController" -> doAppointmentViewControllerAction(e);
            case "PatientViewController" -> doPatientViewControllerAction(e);
        }
    }
    
    private void doAppointmentViewControllerAction(ActionEvent e){
        AppointmentViewController avc = null;
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            Iterator<AppointmentViewController> viewControllerIterator = 
                    this.appointmentViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                avc = viewControllerIterator.next();
                if (avc.equals(e.getSource())){
                    break;
                }
            }
            if (!this.appointmentViewControllers.remove(avc)){
                String message = "Could not find AppointmentViewController in "
                                        + "DesktopViewController collection.";
                displayErrorMessage(message,"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
                */
            }
            else{
                if (this.isDesktopPendingClosure){
                    this.requestViewControllersToCloseViews();
                }
            }
        }
    }
    private void doPatientViewControllerAction(ActionEvent e){
        PatientViewController pvc = null;
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            Iterator<PatientViewController> viewControllerIterator = 
                    this.patientViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                pvc = viewControllerIterator.next();
                if (pvc.equals(e.getSource())){
                    break;
                }
            }
            if (!this.patientViewControllers.remove(pvc)){
                String message = "Could not find PatientViewController in "
                                        + "DesktopViewController collection.";
                displayErrorMessage(message,"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
                */
            }
            else{
                if (this.isDesktopPendingClosure){
                    this.requestViewControllersToCloseViews();
                }
            }
            
            /*
            else{//view controller successfully removed from collection
                isAppointmentViewControllerActive = (appointmentViewControllers.size() > 0);
                isPatientViewControllerActive = (patientViewControllers.size() > 0);
                if ((!(isAppointmentViewControllerActive||isPatientViewControllerActive)) 
                        && isDesktopPendingClosure){
                    getView().dispose();
                }
            }
            */
        }
    }
    /**
     * 
     * @param e source of event is DesktopView object
     */
    private void doDesktopViewAction(ActionEvent e){  
        if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString())){
            String[] options = {"Yes", "No"};
            String message;
            if (!appointmentViewControllers.isEmpty()||!patientViewControllers.isEmpty()){
                message = "At least one patient or appointment view is active. Close application anyway?";
            }
            else {message = "Close The Clinic PMS?";}
            int close = JOptionPane.showOptionDialog(getView(),
                            message,null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            null);
            if (close == JOptionPane.YES_OPTION){
                this.isDesktopPendingClosure = true;
                if (this.appointmentViewControllers.isEmpty()||this.patientViewControllers.isEmpty()){
                    requestViewControllersToCloseViews();
                }
                else {
                    getView().dispose();
                    System.exit(0);
                }    
            }
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    DESKTOP_VIEW_APPOINTMENTS_REQUEST.toString())){
            try{
                appointmentViewControllers.add(
                                            new AppointmentViewController(this, getView()));
                AppointmentViewController avc = 
                        appointmentViewControllers.get(appointmentViewControllers.size()-1);
                
                this.getView().getDeskTop().add(avc.getView());
                avc.getView().setVisible(true);
                avc.getView().setTitle("Appointments");
                avc.getView().setClosable(false);
                avc.getView().setMaximizable(false);
                avc.getView().setIconifiable(true);
                avc.getView().setResizable(false);
                avc.getView().setSelected(true);
                avc.getView().setSize(760,550);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
            catch (PropertyVetoException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    DESKTOP_VIEW_PATIENTS_REQUEST.toString())){
            try{
                patientViewControllers.add(
                                        new PatientViewController(this, getView()));
                PatientViewController pvc = patientViewControllers.get(patientViewControllers.size()-1);

                this.getView().getDeskTop().add(pvc.getView());
                pvc.getView().setVisible(true);
                pvc.getView().setClosable(false);
                pvc.getView().setMaximizable(false);
                pvc.getView().setIconifiable(true);
                pvc.getView().setResizable(false);
                pvc.getView().setSelected(true);
                pvc.getView().setSize(525,585);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
            catch (PropertyVetoException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
        } 
        /**
         * user has attempted to close the desktop view
         */
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            System.exit(0);
        }
        
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.MIGRATE_APPOINTMENT_DBF_TO_CSV.toString())){
            try{
                /**
                 * CSV_APPOINTMENT_FILE_CONVERTER action produces csv appointments file
                 * from the csv version of denApp.dbf
                 * -- refer to AppointmentField enum to see which columns are transferred
                 * -- trailing "\" in a cell removed manually from dbf file before conversion
                 * because the character escapes the following comma delimiter and
                 * lessens the column count by one as a result; which breaks the logic
                 * involved in the conversion
                 * -- headers row removed and any obvious duff lines (blank/rubbish) that might exist
                 */
                CSVMigrationManager.action(Store.MigrationMethod.CSV_APPOINTMENT_FILE_CONVERTER);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
        } 
        
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.MIGRATE_PATIENT_DBF_TO_CSV.toString())){
            try{
                /**
                 * CSV_PATIENT_FILE_CONVERTER action produces a csv file comprising
                 * appointment records, derived from the csv file imported from the
                 * denPat.dbf file.
                 * -- age and shortened name columns removed and much else (refer to 
                 * PatientField enum for reordering & deletion of columns so same enum 
                 * is used for input and output files in the cnversion
                 * -- headers row removed and any obvious duff lines (blank/rubbish) that might exist
                 * -- no attempt to copy over patient-guardian connections
                 * -- 1904, 8926, 12402 and 13164 = no contact info, removed
                 * -- 20773 "missing name" replaces the blanks in name fields
                 * -- duplicate keys 14155 and 17755 found and duplicate key removed
                 */
                CSVMigrationManager.action(Store.MigrationMethod.CSV_PATIENT_FILE_CONVERTER);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
        }
        
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.MIGRATE_INTEGRITY_CHECK.toString())){
            try{
                /**
                 * CSV_MIGRATION_INTEGRITY_PROCESS action produces a list of orphaned 
                 * appointments which refer to patient keys which no longer exist; and
                 * uses this to delete the orphaned records from the system.
                 * -- nonExistingPatients.csv lists the patient keys uniquely referred to by
                 * orphaned appointment records. Note this is derived from a set in which
                 * duplicate references (to the same patient key) do not appear in the list (1668 keys)
                 * -- the orphaned appointment.csv lists all references to patient keys that 
                 * do not exist (i.e. non unique list of keys, total 10081
                 * -- the referential integrity between appointments and patients is achieved 
                 * with the deletion of each of the orphaned appointment records in the list
                 * (37794 - 10081 = 27713 remaining appointment records)
                 * 
                 */
                CSVMigrationManager.action(Store.MigrationMethod.CSV_MIGRATION_INTEGRITY_PROCESS);
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
        }
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.MIGRATE_PATIENT_DATE_CLEANED_IN_ACCESS.toString())){
            try{
                AccessStore store = AccessStore.getInstance();
                store.tidyPatientImportedDate();
            }
            catch (StoreException ex){
                displayErrorMessage(ex.getMessage(),"DesktopViewController error",JOptionPane.WARNING_MESSAGE);
                /*
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(ex.getMessage()));
                */
            }
        }
    }

    private DesktopView getView(){
        return this.view;
    }       
    private void setView(DesktopView view){
        this.view = view;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {   
        Border border = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            /**
             * Selection of persistent storage type
             */
            if (args.length > 0){
                switch (args[0]){
                    case "ACCESS" -> {
                        Store.setStorageType(Storage.ACCESS);
                    }
                    case "CSV" -> Store.setStorageType(Storage.CSV);
                    case "POSTGRES" -> Store.setStorageType(Storage.POSTGRES);
                    case "SQL_EXPRESS" -> Store.setStorageType(Storage.SQL_EXPRESS);
                }
            }
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            /*
            javax.swing.UIManager.getDefaults().put("TableHeader.cellBorder",new LineBorder(Color.RED,2));
            border = javax.swing.UIManager.getBorder("TableHeader.cellBorder");
            */
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DesktopViewController();
            }
        });
    }
    
    private void requestViewControllersToCloseViews(){
        if (this.patientViewControllers.size() > 0){
            Iterator<PatientViewController> pvcIterator = patientViewControllers.iterator();
            while(pvcIterator.hasNext()){
                PatientViewController pvc = pvcIterator.next();
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
                pvc.actionPerformed(actionEvent);    
            }
        }
        
        if (this.appointmentViewControllers.size() > 0){
            Iterator<AppointmentViewController> avcIterator = appointmentViewControllers.iterator();
            while(avcIterator.hasNext()){
                AppointmentViewController avc = avcIterator.next();
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
                avc.actionPerformed(actionEvent);    
            }
        }
        if ((appointmentViewControllers.isEmpty()) && (patientViewControllers.isEmpty())){
            if (this.isDesktopPendingClosure){
                getView().dispose();
                System.exit(0);
            }
        }
        else{
            //this.requestViewControllersToCloseViews();
        }
         
    }
    
    
}
