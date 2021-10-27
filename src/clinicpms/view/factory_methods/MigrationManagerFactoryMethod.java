/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.type.migrationmanagerview.MigrationManagerModalViewer;
import clinicpms.view.View;
import clinicpms.view.DesktopView;
import java.awt.event.ActionListener;

/**
 *
 * @author colin
 */
public class MigrationManagerFactoryMethod extends ViewFactoryMethod{
    
    public MigrationManagerFactoryMethod(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        initialiseView(controller, ed, dtView);
    }
    
    @Override
    public View makeView(View.Viewer myViewType){
        return new MigrationManagerModalViewer(myViewType, this.getViewController(), 
                this.getEntityDescriptor(), getDesktopView().getContentPane());
        
    }
    
    private void initialiseView(ActionListener controller, EntityDescriptor ed, DesktopView dtView){
        this.setDesktopView(dtView);
        this.setEntityDescriptor(ed);
        this.setViewController(controller);
    }
    
}
