/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.appontment_schedule_view;

import clinicpms.model.Patient;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class AppointmentsTablePatientRenderer  extends JLabel implements TableCellRenderer{
    
    public AppointmentsTablePatientRenderer()
    {
        //Font f = super.getFont();
        // bold
        //this.setFont(f.deriveFont(f.getStyle() | ~Font.BOLD));;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column)
    {
        
        Patient patient = (Patient)value;
        if (patient == null) {
            super.setText("NOT BOOKED");
            super.setHorizontalAlignment(JLabel.CENTER);


        }
        else {
            super.setText(patient.toString());
            super.setHorizontalAlignment(JLabel.LEFT);
        }
        
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
