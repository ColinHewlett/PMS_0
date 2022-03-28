/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.patient_notification_editor_view;

import clinicpms.constants.ClinicPMS;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.view.View;
import clinicpms.view.views.appointment_creator_editor_view.SelectStartTimeLocalDateTimeRenderer;
import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author colin
 */
public class PatientNotificationEditorModalViewerx extends View{
    private View.Viewer myViewType = null;
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    private ViewController.ViewMode viewMode = null;
    
    public PatientNotificationEditorModalViewerx(View.Viewer myViewType, ActionListener myController,
            EntityDescriptor entityDescriptor, 
            Component parent) {//ViewMode arg
        //initialiseDialogClosing();
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        setMyViewType(myViewType);
        //initComponents();
        initialiseViewMode();
        // Try to find a JDesktopPane.
        JLayeredPane toUse = JOptionPane.getDesktopPaneForComponent(parent);
        // If we don't have a JDesktopPane, we try to find a JLayeredPane.
        if (toUse == null)  toUse = JLayeredPane.getLayeredPaneAbove(parent);
        // If this still fails, we throw a RuntimeException.
        if (toUse == null) throw new RuntimeException   ("parentComponent does not have a valid parent");
        this.setClosable(true);
        JDesktopPane x = (JDesktopPane)toUse;
        toUse.add(this);
        this.setLayer(JLayeredPane.MODAL_LAYER);
        centreViewOnDesktop(x.getParent(),this);
        this.initialiseView();
        this.setVisible(true);
        
        
        ActionEvent actionEvent = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            EntityDescriptor.AppointmentViewControllerActionEvent.MODAL_VIEWER_ACTIVATED.toString());
        this.getMyController().actionPerformed(actionEvent);
        
        startModal(this);
    }
    
    private void startModal(JInternalFrame f) {
        // We need to add an additional glasspane-like component directly
        // below the frame, which intercepts all mouse events that are not
        // directed at the frame itself.
        JPanel modalInterceptor = new JPanel();
        modalInterceptor.setOpaque(false);
        JLayeredPane lp = JLayeredPane.getLayeredPaneAbove(f);
        lp.setLayer(modalInterceptor, JLayeredPane.MODAL_LAYER.intValue());
        modalInterceptor.setBounds(0, 0, lp.getWidth(), lp.getHeight());
        modalInterceptor.addMouseListener(new MouseAdapter(){});
        modalInterceptor.addMouseMotionListener(new MouseMotionAdapter(){});
        lp.add(modalInterceptor);
        f.toFront();

        // We need to explicitly dispatch events when we are blocking the event
        // dispatch thread.
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        try {
            while (! f.isClosed())       {
                if (EventQueue.isDispatchThread())    {
                    // The getNextEventMethod() issues wait() when no
                    // event is available, so we don't need do explicitly wait().
                    AWTEvent ev = queue.getNextEvent();
                    // This mimics EventQueue.dispatchEvent(). We can't use
                    // EventQueue.dispatchEvent() directly, because it is
                    // protected, unfortunately.
                    if (ev instanceof ActiveEvent)  ((ActiveEvent) ev).dispatch();
                    else if (ev.getSource() instanceof Component)  ((Component) ev.getSource()).dispatchEvent(ev);
                    else if (ev.getSource() instanceof MenuComponent)  ((MenuComponent) ev.getSource()).dispatchEvent(ev);
                    // Other events are ignored as per spec in
                    // EventQueue.dispatchEvent
                } else  {
                    // Give other threads a chance to become active.
                    Thread.yield();
                }
            }
        }
        catch (InterruptedException ex) {
            // If we get interrupted, then leave the modal state.
        }
        finally {
            // Clean up the modal interceptor.
            lp.remove(modalInterceptor);

            // Remove the internal frame from its parent, so it is no longer
            // lurking around and clogging memory.
            Container parent = f.getParent();
            if (parent != null) parent.remove(f);
        }
    }
    
     /**
     * On entry the local EntityDescriptor.Appointment is initialised 
     */
    private void initialiseViewFromED(){
        
    }
    
    private void centreViewOnDesktop(Container desktopView, JInternalFrame view){
        Insets insets = desktopView.getInsets();
        Dimension deskTopViewDimension = desktopView.getSize();
        Dimension myViewDimension = view.getSize();
        view.setLocation(new Point(
                (int)(deskTopViewDimension.getWidth() - (myViewDimension.getWidth()))/2,
                (int)((deskTopViewDimension.getHeight()-insets.top) - myViewDimension.getHeight())/2));
    }

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }
    
    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
    
    @Override
    public void addInternalFrameClosingListener(){
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        if (e.getPropertyName().equals(
                EntityDescriptor.AppointmentViewDialogPropertyEvent.APPOINTMENT_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            initialiseViewFromED();
        }
        else if (e.getPropertyName().equals(
            EntityDescriptor.AppointmentViewDialogPropertyEvent.APPOINTMENT_VIEW_ERROR.toString())){
            EntityDescriptor ed = (EntityDescriptor)e.getNewValue();
            ViewController.displayErrorMessage(ed.getError(),
                                               "Appointment editor dialog error",
                                               JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void initialiseView(){
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener value){
        this.myController = value;
    }
    
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    private ViewController.ViewMode getViewMode(){
        return this.viewMode;
    }
    private void setViewMode(ViewController.ViewMode value){
        this.viewMode = value;
    }
    private void initialiseViewMode(){
        
    }
}
