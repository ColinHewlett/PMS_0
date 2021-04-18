/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JInternalFrame;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

/**
 *
 * @author colin
 * V02_VCSuppliesDataOnDemandToView
 */
public abstract class ViewController implements ActionListener{

    public static enum AppointmentField {ID,
                                KEY,
                                APPOINTMENT_PATIENT,
                                START,
                                DURATION,
                                NOTES}
    public static enum Status{BOOKED,UNBOOKED};
    public enum AppointmentViewControllerActionEvent {
                                            APPOINTMENT_CANCEL_REQUEST,/*of selected appt*/
                                            APPOINTMENT_CREATE_VIEW_REQUEST,
                                            APPOINTMENT_UPDATE_VIEW_REQUEST,/*of selected appt*/
                                            APPOINTMENTS_VIEW_CLOSED,
                                            APPOINTMENTS_FOR_DAY_REQUEST,/*triggered by day selection*/
                                            APPOINTMENT_SLOTS_FROM_DATE_REQUEST,
                                            EMPTY_SLOT_SCANNER_DIALOG_REQUEST
                                            }
    public enum AppointmentViewDialogActionEvent {
                                            APPOINTMENT_VIEW_CLOSE_REQUEST,
                                            APPOINTMENT_VIEW_CREATE_REQUEST,
                                            APPOINTMENT_VIEW_UPDATE_REQUEST,
                                            }
    public enum AppointmentViewDialogPropertyEvent {
                                            APPOINTMENT_RECEIVED,
                                            APPOINTMENT_VIEW_ERROR
                                            }
    public enum AppointmentViewControllerPropertyEvent {
                                            APPOINTMENTS_FOR_DAY_RECEIVED,
                                            APPOINTMENT_SLOTS_FROM_DAY_RECEIVED,
                                            APPOINTMENT_FOR_DAY_ERROR
                                            }
    
    public enum DesktopViewControllerActionEvent {
                                            VIEW_CLOSE_REQUEST,//raised by Desktop view
                                            VIEW_CLOSED_NOTIFICATION,//raised by internal frame views
                                            APPOINTMENT_VIEW_CONTROLLER_REQUEST,
                                            PATIENT_VIEW_CONTROLLER_REQUEST
    }
    
    public enum DesktopViewControllerPropertyEvent{
                                            
    }
    
    public enum EmptySlotSearchCriteriaDialogActionEvent {EMPTY_SLOT_SCANNER_CLOSE_REQUEST,
                                                          EMPTY_SLOT_SCANNER_SETTINGS_REQUEST}
    
    public enum EmptySlotSearchCriteriaDialogPropertyEvent {EMPTY_SLOT_SCANNER_SETTINGS_RECEIVED}

    public enum PatientField {
                              KEY,
                              TITLE,
                              FORENAMES,
                              SURNAME,
                              LINE1,
                              LINE2,
                              TOWN,
                              COUNTY,
                              POSTCODE,
                              PHONE1,
                              PHONE2,
                              GENDER,
                              DOB,
                              IS_GUARDIAN_A_PATIENT,
                              GUARDIAN,
                              NOTES,
                              DENTAL_RECALL_DATE,
                              HYGIENE_RECALL_DATE,
                              DENTAL_RECALL_FREQUENCY,
                              HYGIENE_RECALL_FREQUENCY,
                              DENTAL_APPOINTMENT_HISTORY,
                              HYGIENE_APPOINTMENT_HISTORY}
    
    public static enum PatientViewControllerActionEvent {
                                            NULL_PATIENT_REQUEST,
                                            PATIENT_REQUEST,
                                            PATIENTS_REQUEST,
                                            PATIENT_VIEW_CLOSED,
                                            PATIENT_VIEW_CREATE_REQUEST,
                                            PATIENT_VIEW_UPDATE_REQUEST,
                                            PATIENT_GUARDIAN_REQUEST,
                                            PATIENT_GUARDIANS_REQUEST,
                                            APPOINTMENT_VIEW_CONTROLLER_REQUEST
                                            }
    public static enum PatientViewControllerPropertyEvent {
                                            NULL_PATIENT_RECEIVED,
                                            PATIENT_RECEIVED,
                                            PATIENTS_RECEIVED,
                                            PATIENT_GUARDIANS_RECEIVED}

    public enum ViewMode {CREATE,
                          UPDATE} 
    
    public DateTimeFormatter dmyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public DateTimeFormatter dmyhhmmFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");
    public DateTimeFormatter recallFormat = DateTimeFormatter.ofPattern("MMMM/yyyy");
    public DateTimeFormatter startTime24Hour = DateTimeFormatter.ofPattern("HH:mm");
    
    protected void centreViewOnDesktop(Frame desktopView, JInternalFrame view){
        Insets insets = desktopView.getInsets();
        Dimension deskTopViewDimension = desktopView.getSize();
        Dimension myViewDimension = view.getSize();
        view.setLocation(new Point(
                (int)(deskTopViewDimension.getWidth() - (myViewDimension.getWidth()))/2,
                (int)((deskTopViewDimension.getHeight()-insets.top) - myViewDimension.getHeight())/2));
    }
    
    public static void displayErrorMessage(String message, String title, int messageType){
        JOptionPane.showMessageDialog(null,new ErrorMessagePanel(message),title,messageType);
    }
    
    public abstract EntityDescriptor getEntityDescriptorFromView();
}
