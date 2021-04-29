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
import java.util.List;
//import javax.swing.table.AbstractTableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author colin
 */
public class AppointmentsTableModel extends AbstractTableModel {
    private List<EntityDescriptor.Appointment> appointments = null;
    private enum COLUMN{From, Duration, Patient, Notes};
    private final Class[] columnClass = new Class[] {
        LocalDateTime.class, 
        Duration.class, 
        EntityDescriptor.Patient.class,
        String.class};
    
    public List<EntityDescriptor.Appointment> getAppointments(){
        return this.appointments;
    }
    
    public AppointmentsTableModel(List<EntityDescriptor.Appointment> appointments){
        this.appointments = appointments;
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
        for (COLUMN column : COLUMN.values()){
            if (column.ordinal() == columnIndex ){
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
    public Object getValueAt(int row, int column){
        Object result = null;
        EntityDescriptor.Appointment appointment = getAppointments().get(row);
        switch (column){
            case 0:
                result = appointment.getData().getStart();
                break;
            case 1:
                result = appointment.getData().getDuration();
                break;
            case 2:
                result = appointment.getAppointee();
                break;
            case 3:
                result = appointment.getData().getNotes();
                break;
        }
        return result;
    }   
}
