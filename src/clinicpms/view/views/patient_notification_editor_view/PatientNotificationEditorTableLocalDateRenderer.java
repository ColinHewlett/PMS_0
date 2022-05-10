/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.patient_notification_editor_view;

import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class PatientNotificationEditorTableLocalDateRenderer extends JLabel implements TableCellRenderer{
    private DateTimeFormatter ddmmyy = DateTimeFormatter.ofPattern("dd/MM/yy");
    private LocalDate date = null;
    
    public PatientNotificationEditorTableLocalDateRenderer(){
        Font font = super.getFont();
        super.setFont(font.deriveFont(font.getStyle()|~Font.BOLD));
        super.setFont(font.deriveFont(font.getStyle()|~Font.ITALIC));
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column){
        
        super.setFont(new Font("Tahoma",Font.PLAIN,11 ));
        //Font font = super.getFont();
        if (value != null){
            date = (LocalDate)value;
            super.setText(date.format(ddmmyy));
        }
        //else super.setText("");
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        return this;
    }
}
