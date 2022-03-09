/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.empty_slot_scanner_view;

import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class EmptySlotAvailabilityLocalDateTimeTableRenderer extends JLabel implements TableCellRenderer{
    private DateTimeFormatter emptySlotFormat = DateTimeFormatter.ofPattern("(EEE) dd/MM/yy HH:mm ");
    
    public EmptySlotAvailabilityLocalDateTimeTableRenderer()
    {
        //Font f = super.getFont();
        // bold
        //this.setFont(f.deriveFont(f.getStyle() | ~Font.BOLD));;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column)
    {
        if (value != null){
            LocalDateTime startTime = (LocalDateTime)value;
            setText(startTime.format(emptySlotFormat));
        }
        else super.setText("");
        return this;
    }
}
