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
public class PatientAppointmentContactView6ColumnTableModel extends DefaultTableModel{
    private ArrayList<PatientAppointmentContactView6ColumnTableModel.AppointmentPlus> appointments = null;
    private enum COLUMN{Patient, From,To,Duration,Contacts,Contacted};
    private final Class[] columnClass = new Class[] {
        EntityDescriptor.Patient.class, 
        LocalTime.class, 
        LocalTime.class, 
        Duration.class, 
        String.class,
        Boolean.class};
    
    public PatientAppointmentContactView6ColumnTableModel(){
        appointments = new ArrayList<PatientAppointmentContactView6ColumnTableModel.AppointmentPlus>();
    }
        
    public class AppointmentPlus{
        private EntityDescriptor.Patient patient = null;
        private LocalDateTime start = null;
        private Duration duration = null;
        private String notes = null;
        private Boolean hasBeenContacted = false;
        
        public AppointmentPlus(EntityDescriptor.Appointment a){
            patient = a.getAppointee();
            start = a.getData().getStart();
            duration = a.getData().getDuration();
            notes = a.getData().getNotes();   
        }
        public EntityDescriptor.Patient getPatient(){
            return patient;
        }
        public LocalDateTime getStart(){
            return start;
        }
        public Duration getDuration(){
            return duration;
        }
        public Boolean getHasBeenContacted(){
            return hasBeenContacted;
        }
        public void setHasBeenSelected(Boolean value){
            hasBeenContacted = value;
        }
    }
    public ArrayList<PatientAppointmentContactView6ColumnTableModel.AppointmentPlus> getAppointments(){
        return appointments;
    }
    
    public void addElement(EntityDescriptor.Appointment a){
        AppointmentPlus aPlus = new AppointmentPlus(a);
        appointments.add(aPlus);
    }
    
    public void removeAllElements(){
        appointments.clear();
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount(){
        int result;
        if (appointments!=null) result = appointments.size();
        else result = 0;
        return result;
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
    public void setValueAt(Object value, int row, int col) {
        if (col==5){
            if ((Boolean)value)appointments.get(row).setHasBeenSelected(Boolean.TRUE);
            else appointments.get(row).setHasBeenSelected(Boolean.FALSE);
            fireTableCellUpdated(row, col);
        }
        
        
    }

    @Override
    public Object getValueAt(int row, int columnIndex){
        Object result = null;
        AppointmentPlus appointment = (AppointmentPlus)getAppointments().get(row);
        for (COLUMN column: COLUMN.values()){
            if (column.ordinal() == columnIndex){
                if (appointment == null){
                    return null;
                }
                else{
                    LocalDateTime start = appointment.getStart();
                    long minutes = appointment.getDuration().toMinutes();
                    Duration duration = appointment.getDuration();
                    String phone1 = appointment.getPatient().getData().getPhone1();
                    String phone2 = appointment.getPatient().getData().getPhone2();
                    String contactDetails = null;
                    contactDetails = "{Phone 1} " + phone1 + "; ";
                    contactDetails = contactDetails + "{Phone 2} " + phone2;
                    Boolean contactStatus = appointment.getHasBeenContacted();
                    
                    switch (column){
                        case Patient:
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
                        case Contacts:
                            result = contactDetails; 
                            break;
                        case Contacted:
                            result = contactStatus;
                            break;
                    }
                    break;
                }
            }
        }
        return result;
    }
    
}
