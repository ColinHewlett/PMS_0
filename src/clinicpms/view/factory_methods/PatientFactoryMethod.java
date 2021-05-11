/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.base.DesktopView;
import clinicpms.view.PatientView;
import clinicpms.view.View;
import java.awt.event.ActionListener;
/**
 *
 * @author colin
 */
public class PatientFactoryMethod extends ViewFactoryMethod{
    
    public PatientFactoryMethod(ActionListener viewController,
            EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(viewController);
    }
    
    public View makeView(View.Viewer myViewType){
        return new PatientView(myViewType, getViewController(), getEntityDescriptor());
    }
    
}
