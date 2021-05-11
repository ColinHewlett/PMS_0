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
public class ScheduleContactListFactoryMethod extends ViewFactoryMethod{
    
    public ScheduleContactListFactoryMethod(ActionListener viewController,
            EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(viewController);
    }

    public View makeView(View.Viewer myViewType){
        return new PatientAppointmentContactView(myViewType, getViewController(), getEntityDescriptor()); 
    }
    
}
