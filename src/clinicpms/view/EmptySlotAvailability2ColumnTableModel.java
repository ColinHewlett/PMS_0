/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.constants.ClinicPMS;
import clinicpms.controller.EntityDescriptor;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.AbstractTableModel;

/**
 * A custom model for display of empty slot availability is required to handle the 
 * processing requirements. The model returns String values thus avoiding the need
 * for custom renderers.
 * @author colin
 */
public class EmptySlotAvailability2ColumnTableModel extends AbstractTableModel{
    public static ArrayList<EntityDescriptor.Appointment> emptySlots = new ArrayList<>();
    private DateTimeFormatter emptySlotFormat = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm (EEE)");
    private enum COLUMN{EmptySlot, Duration};
    private final Class[] columnClass = new Class[] { 
        String.class,
        String.class};
    
    
    public ArrayList<EntityDescriptor.Appointment> getEmptySlots(){
        return this.emptySlots;
    }
    
    public void addElement(EntityDescriptor.Appointment a){
        emptySlots.add(a);
    }
    
    public void removeAllElements(){
        emptySlots.clear();
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount(){
        return getEmptySlots().size();
    }

    @Override
    public int getColumnCount(){
        return COLUMN.values().length;
    }
    
    @Override
    public String getColumnName(int columnIndex){
        String result = null;
        switch (columnIndex){
            case 0:
                result = "Empty slots";
                break;
            case 1:
                result = "Duration";
                break;
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
        EntityDescriptor.Appointment slot = getEmptySlots().get(row);
        switch (columnIndex){
            case 0:
                result = slot.getData().getStart().format(emptySlotFormat);  
                break;   
            case 1:
                result = convertSlotDurationToString(
                    slot.getData().getDuration(), slot.getData().getStart().toLocalDate());   
                break; 
        }
        return (String)result;
    }
    
    /*
    @Override
    public void setValueAt(Object value, int row, int columnIndex){
        
    }
    */
    
    private String convertSlotDurationToString(Duration duration, LocalDate start){
        String result = null;
        if (duration.toHours() < 8) result = renderDurationLessThanSingleDay(duration);
        else result = renderDurationMoreThanOrEqualToSingleDay(duration, start); 
        
        return result;
    }
    
    private String renderDurationLessThanSingleDay(Duration duration){
        String result = null;
        if (!duration.isZero()){
            int hours = getHoursFromDuration(duration.toMinutes());
            int minutes = getMinutesFromDuration(duration.toMinutes());
            switch (hours){
                case 0:
                    result = String.valueOf(minutes) + " minutes";
                    break;
                case 1:
                    result = (minutes == 0) ? 
                        String.valueOf(hours) + " hour" : 
                        String.valueOf(hours) + " hour " + String.valueOf(minutes) + " minutes";
                    break;
                default:
                    result = (minutes == 0) ?
                        String.valueOf(hours) + " hours" :
                        String.valueOf(hours) + " hours " + String.valueOf(minutes) + " minutes";
                    break;
            }
        }
        return result;
    }
    
    /**
     * Slightly tricky calculation involved because method includes in its counting
     * whether the interim days fall on a practice day or not. The actual duration 
     * can therefor exceed the specified duration.
     * @param duration Duration representing one or more whole days
     * @param start LocalDate represents the start date of the empty slot being processed,
     * required to calculate the "until" date
     * @return String representing the closing date and time of the empty slot when duration
     * is more than one day or "all day" if duration is a single day only
     */
    private String renderDurationMoreThanOrEqualToSingleDay(Duration duration, LocalDate start){
        String result = null;
        int index;
        LocalDate currentDate = start;
        int practiceDays = (int)duration.toHours() / 8;
        if (practiceDays == 1) result = "all day";
        else{
            int dayCount = 0;//at this point duration must be at least 2 days from start
            for (index = 0; index < practiceDays ; index ++){
                while(!isValidDay(currentDate.plusDays(dayCount++))){

                } 
            }
            currentDate = currentDate.plusDays(--dayCount);
            result = "until " + 
                    currentDate.atTime(ClinicPMS.LAST_APPOINTMENT_SLOT).format(emptySlotFormat);
        }
        return result;
    }
    
    private Integer getHoursFromDuration(long duration){
        return (int)duration / 60;
    }
    private Integer getMinutesFromDuration(long duration){
        return (int)duration % 60;
    }
    
    private boolean isValidDay(LocalDate day){
        return(day.getDayOfWeek().equals(DayOfWeek.TUESDAY) 
                            || day.getDayOfWeek().equals(DayOfWeek.THURSDAY)
                            || day.getDayOfWeek().equals(DayOfWeek.FRIDAY));
    }
}
