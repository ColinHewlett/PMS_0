/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import java.awt.Component;
import java.time.Duration;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
/**
 *
 * @author colin
 */
public class SelectSlotDurationRenderer extends JLabel implements ListCellRenderer<Duration>{
    
    @Override
    public Component getListCellRendererComponent(JList<? extends Duration> list,
                                                   Duration value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {
        
        if ((value).isZero())super.setText("slot duration undefined");
        else super.setText(renderDuration((Duration)value));
        return this; 
    }
    
    private String renderDuration(Duration duration){
        String result = null;
        if (!duration.isZero()){
            int hours = getHoursFromDuration(duration.toMinutes());
            int minutes = getMinutesFromDuration(duration.toMinutes());
            switch (hours){
                case 0 -> result = String.valueOf(minutes) + " minutes";
                case 1 -> {result = (minutes == 0) ? 
                        String.valueOf(hours) + " hour" : 
                        String.valueOf(hours) + " hour " + String.valueOf(minutes) + " minutes";}
                case 8 -> result = "all day";
                default -> {result = (minutes == 0) ?
                        String.valueOf(hours) + " hours" :
                        String.valueOf(hours) + " hours " + String.valueOf(minutes) + " minutes";}
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
