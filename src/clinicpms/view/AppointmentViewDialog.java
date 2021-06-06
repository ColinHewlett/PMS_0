/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.view.interfaces.IView;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.Window;
import java.beans.PropertyChangeListener;


/**
 *
 * @author colin
 */
public abstract class AppointmentViewDialog extends javax.swing.JDialog
                                           implements IView, PropertyChangeListener{
    
    public AppointmentViewDialog(Window parent, boolean isModal){
        super(parent, ModalityType.APPLICATION_MODAL);
    }

    
}
