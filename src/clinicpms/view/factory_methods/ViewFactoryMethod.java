/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.factory_methods;

import clinicpms.controller.EntityDescriptor;
import clinicpms.view.views.DesktopView;
import clinicpms.view.View;
import java.awt.event.ActionListener;

/**
 *
 * @author colin
 */
public abstract class ViewFactoryMethod {
    private ActionListener viewController = null;
    private EntityDescriptor entityDescriptor = null;
    private DesktopView desktopView = null;
    protected abstract View makeView(View.Viewer viewType); 

    public ActionListener getViewController(){
        return viewController;
    }
    
    public void setViewController(ActionListener value){
        this.viewController = value;
    }
    
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    
    public void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    
    public DesktopView getDesktopView(){
        return this.desktopView;
    }
    
    public void setDesktopView(DesktopView value){
        this.desktopView = value;
    }
}


