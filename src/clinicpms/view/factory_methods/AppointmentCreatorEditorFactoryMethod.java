/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.view.base.DesktopView;
import clinicpms.controller.EntityDescriptor;
import clinicpms.view.*;
import java.awt.event.ActionListener;

/**
 *
 * @author colin
 */
public class AppointmentCreatorEditorFactoryMethod extends ViewFactoryMethod{
    public AppointmentCreatorEditorFactoryMethod(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
    public View makeView(View.Viewer myViewType){
        return new AppointmentCreatorEditorModalViewer(myViewType, this.getViewController(), 
                this.getEntityDescriptor(), getDesktopView().getContentPane());
        
    }
    
    
}

