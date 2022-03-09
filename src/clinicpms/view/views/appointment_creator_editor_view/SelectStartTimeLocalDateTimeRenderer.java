/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.appointment_creator_editor_view;

import java.awt.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author colin
 */
public class SelectStartTimeLocalDateTimeRenderer extends JLabel implements ListCellRenderer<LocalDateTime>{
    private DateTimeFormatter timeOnlyFormat = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public Component getListCellRendererComponent(JList<? extends LocalDateTime> list,
                                                   LocalDateTime value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {
        if (value!=null){
            super.setText(value.format(timeOnlyFormat));
        }

        return this; 
    }
}
