/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.surgery_day_editor_view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.view.View;
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
import java.beans.PropertyVetoException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.time.DayOfWeek;

/**
 *
 * @author colin
 */
public class SurgeryDaysEditorModalViewer extends View {
    private View.Viewer myViewType = null;
    private ActionListener myController = null;
    private EntityDescriptor entityDescriptor = null;

    /**
     * Creates new form CalendarVetoPolicyEditorModalViewer
     */
    public SurgeryDaysEditorModalViewer(View.Viewer myViewType, ActionListener myController,
            EntityDescriptor entityDescriptor, 
            Component parent) {//ViewMode arg
        //initialiseDialogClosing();
        setMyViewType(myViewType);
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        
        initComponents();
        // Try to find a JDesktopPane.
        JLayeredPane toUse = JOptionPane.getDesktopPaneForComponent(parent);
        // If we don't have a JDesktopPane, we try to find a JLayeredPane.
        if (toUse == null)  toUse = JLayeredPane.getLayeredPaneAbove(parent);
        // If this still fails, we throw a RuntimeException.
        if (toUse == null) throw new RuntimeException   ("parentComponent does not have a valid parent");
        
        JDesktopPane x = (JDesktopPane)toUse;
        toUse.add(this);
        this.setLayer(JLayeredPane.MODAL_LAYER);
        centreViewOnDesktop(x.getParent(),this);
        //this.initialiseView();
        this.setVisible(true);
        this.setClosable(true);
        
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
    
    private void centreViewOnDesktop(Container desktopView, JInternalFrame view){
        Insets insets = desktopView.getInsets();
        Dimension deskTopViewDimension = desktopView.getSize();
        Dimension myViewDimension = view.getSize();
        view.setLocation(new Point(
                (int)(deskTopViewDimension.getWidth() - (myViewDimension.getWidth()))/2,
                (int)((deskTopViewDimension.getHeight()-insets.top) - myViewDimension.getHeight())/2));
    }
    
    @Override
    public void initialiseView(){
        setTitle("Surgery days editor");
        HashMap<DayOfWeek, Boolean> surgeryDays = getEntityDescriptor().getSurgeryDaysAssignment();
        for(Entry<DayOfWeek,Boolean> entry : surgeryDays.entrySet()){
            switch(entry.getKey()){
                case MONDAY:
                    this.chkMonday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
                case TUESDAY:
                    this.chkTuesday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
                case WEDNESDAY:
                    this.chkWednesday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
                case THURSDAY:
                    this.chkThursday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
                case FRIDAY:
                    this.chkFriday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
                case SATURDAY:
                    this.chkSaturday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
                case SUNDAY:
                    this.chkSunday.setSelected(surgeryDays.get(entry.getKey()));
                    break;
            }
            
        }
    }
    
    @Override
    public void addInternalFrameClosingListener(){
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
 
        if (e.getPropertyName().equals(
            EntityDescriptor.AppointmentViewControllerPropertyEvent.APPOINTMENT_SCHEDULE_ERROR_RECEIVED.toString())){
            ViewController.displayErrorMessage(getEntityDescriptor().getError(),
                                               "Appointment editor dialog error",
                                               JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener value){
        this.myController = value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chkTuesday = new javax.swing.JCheckBox();
        chkMonday = new javax.swing.JCheckBox();
        chkWednesday = new javax.swing.JCheckBox();
        chkThursday = new javax.swing.JCheckBox();
        chkFriday = new javax.swing.JCheckBox();
        chkSaturday = new javax.swing.JCheckBox();
        chkSunday = new javax.swing.JCheckBox();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        chkTuesday.setText("Tuesday");

        chkMonday.setText("Monday");

        chkWednesday.setText("Wednesday");

        chkThursday.setText("Thursday");

        chkFriday.setText("Friday");

        chkSaturday.setText("Saturday");

        chkSunday.setText("Sunday");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkSunday)
                    .addComponent(chkSaturday)
                    .addComponent(chkFriday)
                    .addComponent(chkThursday)
                    .addComponent(chkWednesday)
                    .addComponent(chkMonday)
                    .addComponent(chkTuesday))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkMonday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkTuesday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkWednesday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkThursday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkFriday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSaturday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSunday)
                .addGap(20, 20, 20))
        );

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText("Close");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancel)
                .addGap(25, 25, 25))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        HashMap<DayOfWeek,Boolean> surgeryDaysAssignmentValue = new HashMap<DayOfWeek,Boolean>();
        surgeryDaysAssignmentValue.put(DayOfWeek.MONDAY, this.chkMonday.isSelected());
        surgeryDaysAssignmentValue.put(DayOfWeek.TUESDAY, this.chkTuesday.isSelected());
        surgeryDaysAssignmentValue.put(DayOfWeek.WEDNESDAY, this.chkWednesday.isSelected());
        surgeryDaysAssignmentValue.put(DayOfWeek.THURSDAY, this.chkThursday.isSelected());
        surgeryDaysAssignmentValue.put(DayOfWeek.FRIDAY, this.chkFriday.isSelected());
        surgeryDaysAssignmentValue.put(DayOfWeek.SATURDAY, this.chkSaturday.isSelected()); 
        surgeryDaysAssignmentValue.put(DayOfWeek.SUNDAY, this.chkSunday.isSelected());
        getEntityDescriptor().getRequest().setSurgeryDaysAssignmentValue(surgeryDaysAssignmentValue);
        
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.AppointmentViewControllerActionEvent.SURGERY_DAYS_EDIT_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        try{
            this.setClosed(true);
        }
        catch (PropertyVetoException ex){
            
        }
    }//GEN-LAST:event_btnCancelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox chkFriday;
    private javax.swing.JCheckBox chkMonday;
    private javax.swing.JCheckBox chkSaturday;
    private javax.swing.JCheckBox chkSunday;
    private javax.swing.JCheckBox chkThursday;
    private javax.swing.JCheckBox chkTuesday;
    private javax.swing.JCheckBox chkWednesday;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }

    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
}
