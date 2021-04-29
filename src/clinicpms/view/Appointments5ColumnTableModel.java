/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author colin
 */
public class Appointments5ColumnTableModel extends DefaultTableModel{
    public static ArrayList<EntityDescriptor.Appointment> appointments = null;
    private enum COLUMN{Patient, From,To,Duration,Notes};
    private final Class[] columnClass = new Class[] {
        EntityDescriptor.Patient.class, 
        LocalTime.class, 
        LocalTime.class, 
        Duration.class, 
        String.class};
    
    public ArrayList<EntityDescriptor.Appointment> getAppointments(){
        return this.appointments;
    }

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
                if (appointment == null){
                    return null;
                }
                else{
                    LocalDateTime start = appointment.getData().getStart();
                    long minutes = appointment.getData().getDuration().toMinutes();
                    Duration duration = appointment.getData().getDuration();
                    switch (column){
                        case Patient:
                            result = appointment.getAppointee();
                            break;
                        case From:
                            result = start.toLocalTime();
                            break;
                        case To:
                            result = start.plusMinutes(duration.toMinutes()).toLocalTime();
                            break;
                        case Duration:
                            result = duration;
                            break;
                        case Notes:
                            appointment.getData().getNotes(); 
                            break;
                    }
                    break;
                }
            }
        }
        return result;
    }
    
}
