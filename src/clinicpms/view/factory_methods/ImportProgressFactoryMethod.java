/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.views.import_export_progress_view.ImportProgressView;
import clinicpms.view.View;
import clinicpms.view.views.DesktopView;
import java.awt.event.ActionListener;

/**
 *
 * @author colin
 */
public class ImportProgressFactoryMethod extends ViewFactoryMethod{
    
    public ImportProgressFactoryMethod(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        initialiseView(controller, ed, dtView);
    }
    
    @Override
    public View makeView(View.Viewer myViewType){
        return new ImportProgressView(myViewType, this.getViewController(), 
                this.getEntityDescriptor());
        
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
    
}
