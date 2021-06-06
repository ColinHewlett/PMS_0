/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import java.awt.Component;
import java.time.Duration;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class EmptySlotAvailabilityDurationTableRenderer extends JLabel implements TableCellRenderer{
    
    public EmptySlotAvailabilityDurationTableRenderer()
    {
        //Font f = super.getFont();
        // bold
        //this.setFont(f.deriveFont(f.getStyle() | ~Font.BOLD));;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column){
        if (value!=null){
            Duration duration = (Duration)value;
            if (duration.toHours() < 8) setText(renderDuration(duration));
            else setText(renderDayDuration(duration));
        }
        else super.setText("");
        return this;
    }
    
    private String renderDayDuration(Duration duration){
        
        return null;
    }
    
    private String renderDuration(Duration duration){
        long practiceDays = duration.toHours() / 8;
        
        
        String result;
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
        return result;
    }
    private Integer getHoursFromDuration(long duration){
        return (int)duration / 60;
    }
    private Integer getMinutesFromDuration(long duration){
        return (int)duration % 60;
    }
}
