/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.interfaces.IView;
import clinicpms.view.interfaces.IViewInternalFrameListener;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.time.format.DateTimeFormatter;
import javax.swing.JInternalFrame;

/**
 *
 * @author colin
 */
public abstract class View extends JInternalFrame
                           implements PropertyChangeListener,IView, IViewInternalFrameListener{
    private static Viewer viewer = null;
    
    public View(){
        super("Appointments view",true,true,true,true);
        
    }
    
    public static enum Viewer { APPOINTMENT_SCHEDULE_VIEW,
                                APPOINTMENT_CREATOR_VIEW,
                                APPOINTMENT_CREATOR_EDITOR_VIEW,
                                APPOINTMENT_EDITOR_VIEW,
                                PATIENT_VIEW}
    
    public static Viewer getViewer(){
        return viewer;
    }
    
    public static void setViewer(Viewer value){
        viewer = value;
    }
    
    public static View factory(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        View result = null;
        switch(getViewer()){
            case APPOINTMENT_SCHEDULE_VIEW:
                result = new AppointmentsForDayView(controller, ed);
                break;
            case APPOINTMENT_CREATOR_VIEW:
                result = null;
                break;
            case APPOINTMENT_CREATOR_EDITOR_VIEW:
                result = new AppointmentCreatorEditorModalViewer(controller, ed, dtView.getContentPane());
                break;
            case APPOINTMENT_EDITOR_VIEW:
                result = null;
                break;
            case PATIENT_VIEW:
                result = new PatientView(controller, ed);
                break;
                
        }
        return result;
    }
}
