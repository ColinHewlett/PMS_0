/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.appontment_schedule_view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @author colin
 * update:
 * -- strategy remove the statically defined data structure in the table model
 * -- client uses addElement method to add each appointment to table model
 * -- extend from AbstractTableModel instead of DefaultTableModel
 * -- replaces -> public static ArrayList<EntityDescriptor.Appointment> appointments = null;
 * -- with -> public ArrayList<EntityDescriptor.Appointment> appointments = new ArrayList<>();
 */
public class Appointments5ColumnTableModel extends AbstractTableModel{
    public ArrayList<Appointment> appointments = new ArrayList<>();
    private enum COLUMN{ThePatient, From,To,Duration,Notes};
    private final Class[] columnClass = new Class[] {
        Patient.class,
        LocalTime.class, 
        LocalTime.class, 
        Duration.class, 
        String.class};
    
    public ArrayList<Appointment> getAppointments(){
        return this.appointments;
    }
    
    public void addElement(Appointment a){
        appointments.add(a);
    }
    
    public void removeAllElements(){
        appointments.clear();
        this.fireTableDataChanged();
    }
    
    public Appointment getElementAt(int row){
        return appointments.get(row);
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
        Appointment appointment = getAppointments().get(row);
        for (COLUMN column: COLUMN.values()){
            if (column.ordinal() == columnIndex){
                if (appointment == null){
                    return null;
                }
                else{
                    //LocalDateTime start = appointment.getData().getStart();
                    //long minutes = appointment.getData().getDuration().toMinutes();
                    //Duration duration = appointment.getData().getDuration();
                    LocalDateTime start = appointment.getStart();
                    long minutes = appointment.getDuration().toMinutes();
                    Duration duration = appointment.getDuration();
                    switch (column){
                        case ThePatient:
                            result = appointment.getPatient();
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
                            result = appointment.getNotes(); 
                            break;
                    }
                    break;
                }
            }
        }
        return result;
    }
    
}
