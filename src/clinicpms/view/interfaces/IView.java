/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.interfaces;

import clinicpms.controller.EntityDescriptor;
//import java.awt.event.ActionListener;

/**
 *
 * @author colin
 */
public interface IView {  
    /**
     * Enables communication of property change and action listener events
     * between view and its controller
     * @return EntityDescriptor object contained in the PropertyChangeEvent received by the view
     */
    public EntityDescriptor getEntityDescriptor(); 
    public void initialiseView();
}
