/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.views.DesktopView;
import clinicpms.view.views.empty_slot_scanner_view.EmptySlotScannerModalViewer;
import clinicpms.view.View;
import java.awt.event.ActionListener;
/**
 *
 * @author colin
 */
public class EmptySlotScannerFactoryMethod extends ViewFactoryMethod{
    public EmptySlotScannerFactoryMethod(ActionListener viewController, 
            EntityDescriptor ed, DesktopView dtView ){
        initialiseView(viewController, ed,dtView);  
    }
    
    @Override
    public View makeView(View.Viewer myViewType){
        return new EmptySlotScannerModalViewer(myViewType, getViewController(), 
        getEntityDescriptor(), getDesktopView().getContentPane()); 
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
}
