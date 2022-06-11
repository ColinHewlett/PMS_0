/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.view.views.DesktopView;
import clinicpms.view.factory_methods.*;
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
    private Boolean viewChangedSinceLastSaved = false;
    
    public View(){
        super("Appointments view",true,true,true,true);
        
    }
    
    public static enum Viewer { APPOINTMENT_SCHEDULE_VIEW,
                                APPOINTMENT_CREATOR_VIEW,
                                APPOINTMENT_CREATOR_EDITOR_VIEW,
                                APPOINTMENT_EDITOR_VIEW,
                                EMPTY_SLOT_SCANNER_VIEW,
                                EXPORT_PROGRESS_VIEW,
                                MIGRATION_MANAGER_VIEW,
                                NON_SURGERY_DAY_EDITOR_VIEW,
                                PATIENT_VIEW,
                                PATIENT_NOTIFICATION_VIEW,
                                PATIENT_NOTIFICATION_EDITOR_VIEW,
                                UNACTIONED_PATIENT_NOTIFICATION_VIEW,
                                SCHEDULE_CONTACT_DETAILS_VIEW,
                                SURGERY_DAY_EDITOR_VIEW}
    
    protected Boolean getViewStatus(){
        return viewChangedSinceLastSaved;
    }
    
    protected void setViewStatus(Boolean value){
        viewChangedSinceLastSaved = value;
    }
    
    public static void setViewer(Viewer value){
        viewer = value;
    }
    
    public abstract Viewer getMyViewType();
    
    /*
    public static Viewer getViewer(){
        return viewer;
    }
     */
    
    public static View factory(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        View result = null;
        switch(viewer){
            case APPOINTMENT_SCHEDULE_VIEW:
                result = new AppointmentScheduleFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case APPOINTMENT_CREATOR_VIEW:
                result = null;
                break;
            case APPOINTMENT_CREATOR_EDITOR_VIEW:
                result = new AppointmentCreatorEditorFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case APPOINTMENT_EDITOR_VIEW:
                result = null;
                break;
            case EMPTY_SLOT_SCANNER_VIEW:
                result = new EmptySlotScannerFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case EXPORT_PROGRESS_VIEW:
                result = new ImportProgressFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case MIGRATION_MANAGER_VIEW:
                result = new ImportProgressFactoryMethod(controller, ed, dtView).makeView(viewer);
            case PATIENT_VIEW:
                result = new PatientFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case PATIENT_NOTIFICATION_VIEW:
                result = new PatientNotificationFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case PATIENT_NOTIFICATION_EDITOR_VIEW:
                result = new PatientNotificationEditorFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case NON_SURGERY_DAY_EDITOR_VIEW:
                result = new NonSurgeryDayEditorFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case SCHEDULE_CONTACT_DETAILS_VIEW:
                result = new ScheduleContactDetailsFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
            case SURGERY_DAY_EDITOR_VIEW:
                result = new SurgeryDaysEditorFactoryMethod(controller, ed, dtView).makeView(viewer);
                break;
                
        }
        return result;
    }
}
