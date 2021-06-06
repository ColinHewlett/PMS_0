/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author colin
 */
public class TableHeaderCellBorderRenderer extends JLabel implements TableCellRenderer {
    
    public TableHeaderCellBorderRenderer(Color color){
        this.setBorder(BorderFactory.createLineBorder(color));
        this.setHorizontalAlignment(JLabel.CENTER);
    }
    
    public Component getTableCellRendererComponent(JTable table,
            Object value, 
            boolean isSelected, 
            boolean hasFocus, 
            int row,int column){
        
        this.setText(value.toString());
        return this;    
    }
    
}
