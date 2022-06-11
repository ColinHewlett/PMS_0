/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.views.DesktopView;
import clinicpms.view.views.appontment_schedule_view.AppointmentScheduleView;
import clinicpms.view.View;
import java.awt.event.ActionListener;

/**
 *
 * @author colin
 */
public class AppointmentScheduleFactoryMethod extends ViewFactoryMethod{

    public AppointmentScheduleFactoryMethod(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        initialiseView(controller, ed, dtView);
    }
    
    @Override
    public View makeView(View.Viewer myViewType){
        return new AppointmentScheduleView(myViewType, this.getViewController(), this.getEntityDescriptor());
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
    
    
}
