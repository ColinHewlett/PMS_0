/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author colin
 */
public class AppointmentsSingleColumnTableModel extends DefaultTableModel{
    public static ArrayList<EntityDescriptor.Appointment> appointments = null;
    private enum COLUMN{From,Duration,Notes};
    private final Class[] columnClass = new Class[] {
        LocalDateTime.class, Duration.class,String.class};
    
    public ArrayList<EntityDescriptor.Appointment> getAppointments(){
        return this.appointments;
    }
   
    /*
    public AppointmentsSingleColumnTableModel(ArrayList<EntityDescriptor.Appointment> appointments, String header){
        //this.appointments = appointments;
        //this.header = header;
    }
*/

    @Override
    public int getRowCount(){
        return getAppointments().size();
    }

    @Override
    public int getColumnCount(){
        return COLUMN.values().length;
    }
    @Override
    public String getColumnName(int columnIndex){
        String result = null;
        for (COLUMN column: COLUMN.values()){
            if (column.ordinal() == columnIndex){
                result = column.toString();
                break;
            }
        }
        return result;
    }
    @Override
    public Class<?> getColumnClass(int columnIndex){
        return columnClass[columnIndex];
    }

    @Override
    public Object getValueAt(int row, int columnIndex){
        Object result = null;
        EntityDescriptor.Appointment appointment = getAppointments().get(row);
        for (COLUMN column: COLUMN.values()){
            if (column.ordinal() == columnIndex){
                result =
                        switch (column){
                            case Duration -> appointment.getData().getDuration();
                            case From -> appointment.getData().getStart();
                            case Notes -> appointment.getData().getNotes();   
                        };
                break;
            }
        }
        return result;
    }
}
