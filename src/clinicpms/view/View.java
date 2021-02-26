/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.view.interfaces.IView;
import clinicpms.view.interfaces.IViewInternalFrameListener;
import java.beans.PropertyChangeListener;
import javax.swing.JInternalFrame;

/**
 *
 * @author colin
 */
public abstract class View extends JInternalFrame
                           implements PropertyChangeListener,IView, IViewInternalFrameListener{
    public View(){
        super("Appointments view",true,true,true,true);
        
    }
}
