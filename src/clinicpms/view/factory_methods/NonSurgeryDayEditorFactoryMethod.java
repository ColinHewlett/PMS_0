/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.view.views.non_surgery_day_editor_view.NonSurgeryDayEditorModalViewer;
import clinicpms.view.views.DesktopView;
import clinicpms.controller.EntityDescriptor;
import clinicpms.view.*;
import java.awt.event.ActionListener;
/**
 *
 * @author colin
 */
public class NonSurgeryDayEditorFactoryMethod extends ViewFactoryMethod{
    
    public NonSurgeryDayEditorFactoryMethod(ActionListener viewController, 
            EntityDescriptor ed, DesktopView dtView){
        initialiseView(viewController,ed, dtView);
    }
    
    @Override
    public View makeView(View.Viewer myViewType){
        return new NonSurgeryDayEditorModalViewer(myViewType, getViewController(),
                getEntityDescriptor(), getDesktopView().getContentPane());
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
}
