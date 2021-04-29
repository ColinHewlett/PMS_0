/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import java.awt.Component;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author colin
 */
public class SlotAvailabilityListRenderer extends JLabel implements ListCellRenderer<EntityDescriptor.Appointment>{
    public DateTimeFormatter dmyhhmmFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public SlotAvailabilityListRenderer(){
         setOpaque(true);   
    }
    
    
    @Override
    public Component getListCellRendererComponent(JList<? extends EntityDescriptor.Appointment> list,
                                                   EntityDescriptor.Appointment value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {
        
        
        EntityDescriptor.Appointment appointment = value;
        
        setText(appointment.getData().getStart().format(dmyhhmmFormat));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this; 
    }
    
    private String renderDuration(Duration duration){
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
                case 8:
                    result = "all day";
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
    private Integer getHoursFromDuration(long duration){
        return (int)duration / 60;
    }
    private Integer getMinutesFromDuration(long duration){
        return (int)duration % 60;
    }
    
}
