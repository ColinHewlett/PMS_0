/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.patient_notifications_view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class PatientNotificationTableOverdueLocalDateRenderer extends JLabel implements TableCellRenderer{
    private DateTimeFormatter ddmmyy = DateTimeFormatter.ofPattern("dd/MM/yy");
    private LocalDate date = null;
    
    public PatientNotificationTableOverdueLocalDateRenderer(){
        Font font = super.getFont();
        super.setFont(font.deriveFont(font.getStyle()|~Font.BOLD));
        super.setFont(font.deriveFont(font.getStyle()|~Font.ITALIC));
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column){
        
        int test = 0;
        Font font = super.getFont();
        super.setFont(font.deriveFont(font.getStyle()|~Font.BOLD));
        font = super.getFont();
        super.setFont(font.deriveFont(font.getStyle()|~Font.ITALIC));
        font = super.getFont();
        
        if (font.isItalic()){// how can it be after the preceding statement?
            test = test;
            super.setFont(new Font("Tahoma",Font.PLAIN,11 ));
        }

        //super.setFont(new Font("Tahoma",Font.PLAIN,11 ));
        //Font font = super.getFont();
        if (value != null){
            date = (LocalDate)value;
            super.setText(date.format(ddmmyy));
            
            if (date.compareTo(LocalDate.now()) < 0) {
                super.setForeground(Color.RED);
                font = super.getFont();
                super.setFont(font.deriveFont(font.getStyle()|Font.BOLD));
            }
            else{
                super.setForeground(Color.BLACK);
                font = super.getFont();
                super.setFont(font.deriveFont(font.getStyle()|~Font.BOLD));
            }

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

