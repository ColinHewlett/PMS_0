/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.view.views.surgery_day_editor_view.SurgeryDaysEditorModalViewer;
import clinicpms.view.views.DesktopView;
import clinicpms.controller.EntityDescriptor;
import clinicpms.view.*;
import java.awt.event.ActionListener;
/**
 *
 * @author colin
 */
public class SurgeryDaysEditorFactoryMethod extends ViewFactoryMethod{
    
    public SurgeryDaysEditorFactoryMethod(ActionListener viewController, 
            EntityDescriptor ed, DesktopView dtView){
        initialiseView(viewController, ed, dtView);
    }
    
    @Override
    public View makeView(View.Viewer myViewType){
        return new SurgeryDaysEditorModalViewer(myViewType, getViewController(), 
                getEntityDescriptor(), getDesktopView().getContentPane());
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
}
