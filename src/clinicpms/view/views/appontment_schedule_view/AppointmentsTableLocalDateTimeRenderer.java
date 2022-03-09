/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.appontment_schedule_view;

import java.awt.Component;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
/**
 *
 * @author colin
 */
public class AppointmentsTableLocalDateTimeRenderer extends JLabel implements TableCellRenderer{
    private DateTimeFormatter ddMMyyhhmm12Format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm ");
    
    public AppointmentsTableLocalDateTimeRenderer()
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
            super.setText(startTime.format(ddMMyyhhmm12Format));
        }
        else super.setText("");
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        
        setOpaque(true);
        return this;
    }
}
