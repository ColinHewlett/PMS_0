/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.view.views.schedule_contact_details_view.ScheduleContactDetailsView;
import clinicpms.view.views.DesktopView;
import clinicpms.controller.EntityDescriptor;
import clinicpms.view.*;
import java.awt.event.ActionListener;
/**
 *
 * @author colin
 */
public class ScheduleContactDetailsFactoryMethod extends ViewFactoryMethod{
    
    public ScheduleContactDetailsFactoryMethod(ActionListener viewController,
            EntityDescriptor ed, DesktopView dtView){
        initialiseView(viewController, ed, dtView);
    }

    @Override
    public View makeView(View.Viewer myViewType){
        return new ScheduleContactDetailsView(myViewType, getViewController(), getEntityDescriptor()); 
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
    
}
