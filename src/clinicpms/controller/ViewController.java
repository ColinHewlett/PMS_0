/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;
import clinicpms.view.TableHeaderCellBorderRenderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JInternalFrame;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author colin
 * V02_VCSuppliesDataOnDemandToView
 */
public abstract class ViewController implements ActionListener{

    public static enum PatientAppointmentContactListViewControllerActionEvent {
                                            PATIENT_APPOINTMENT_CONTACT_VIEW_CLOSED,
                                            PATIENT_APPOINTMENT_CONTACT_VIEW_REQUEST
                                            }

    public enum ViewMode {CREATE,
                          Create,
                          UPDATE,
                          Update} 
    
    public static final LocalTime FIRST_APPOINTMENT_SLOT = LocalTime.of(9,0);
    public static final LocalTime LAST_APPOINTMENT_SLOT = LocalTime.of(17,0);
    
    public DateTimeFormatter dmyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public DateTimeFormatter dmyhhmmFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");
    public DateTimeFormatter recallFormat = DateTimeFormatter.ofPattern("MMMM/yyyy");
    public DateTimeFormatter startTime24Hour = DateTimeFormatter.ofPattern("HH:mm");
    public DateTimeFormatter format24Hour = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * utility helper method which sets the column header colour and each of the widths of the specified table's columns as per the specified WIDTH PERCENTAGES 
     * @param table; JTable whose properties are updated
     * @param tablePreferredWidth; int which defines the total width of the JTable
     * @param percentages; double[] which defines the percentage of the total width of each of the table's columns 
     */
    public static void setJTableColumnProperties(JTable table, int tablePreferredWidth,
        double... percentages) {
        double total = 0;
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            total += percentages[i];
        }

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth((int)
                    (tablePreferredWidth * (percentages[i] / total)));
            column.setHeaderRenderer(new TableHeaderCellBorderRenderer(Color.LIGHT_GRAY));
        }
    }
    
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
