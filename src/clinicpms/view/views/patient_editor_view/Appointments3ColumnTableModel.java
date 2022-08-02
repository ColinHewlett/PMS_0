/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.patient_editor_view;

import clinicpms.model.Appointment;
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
 * update to remove statically defined data structure
 * -- replaces extension  from DefaultTableModel to AbstractTableModel
 */
public class Appointments3ColumnTableModel extends AbstractTableModel{
    public ArrayList<Appointment> appointments = new ArrayList<>();
    private enum COLUMN{From,Duration,Notes};
    private final Class[] columnClass = new Class[] {
        LocalDateTime.class, Duration.class,String.class};
    
    public ArrayList<Appointment> getAppointments(){
        return appointments;
    }
    
    public void addElement(Appointment a){
        appointments.add(a);
    }
    
    public void removeAllElements(){
        appointments.clear();
        this.fireTableDataChanged();
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
                    switch (column){
                        case Duration:
                            result = appointment.getDuration();
                            break;
                        case From:
                            result = appointment.getStart();
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
