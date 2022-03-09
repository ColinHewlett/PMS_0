/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.views.empty_slot_scanner_view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.view.View;
import clinicpms.view.View;
import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.LocalDate;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author colin
 */
public class EmptySlotScannerModalViewer extends View {
    private View.Viewer myViewType = null;
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    private DateTimeFormatter appointmentScheduleFormat = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy ");


    /**
     * Creates new form AppointmentEditorInternalFrame
     */
    public EmptySlotScannerModalViewer(View.Viewer myViewType,ActionListener myController,
            EntityDescriptor entityDescriptor, 
            Component parent) {//ViewMode arg
        //initialiseDialogClosing();
        setMyViewType(myViewType);
        setEntityDescriptor(entityDescriptor);
        setMyController(myController);
        setTitle("Empty slot scanner" );
        initComponents();
        this.cmbSelectSlotDuration.setRenderer(new SelectSlotDurationRenderer());
        this.buttonGroup1.add(this.rdbSelectMonths);
        this.buttonGroup1.add(this.rdbSelectWeeks);
        this.rdbSelectWeeks.setSelected(true);
        
        cmbSelectSlotDurationActionPerformed(null);
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
        this.initialiseView();
        
        
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
    
    public void addInternalFrameClosingListener(){
        
    }
    
    public void initialiseView(){
        this.setVisible(true);
        this.setClosable(true);
        //disallow any resizing including minimising to tak bar
        this.setMaximizable(false);
        this.setIconifiable(false);
        this.setResizable(false);
    }
    
    public EntityDescriptor getEntityDescriptor(){
        return entityDescriptor;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e){
        
        setEntityDescriptor((EntityDescriptor)e.getNewValue());
        /*
        if (e.getPropertyName().equals(
                ViewController.AppointmentViewDialogPropertyEvent.APPOINTMENT_RECEIVED.toString())){
            initialiseViewFromED();
        }
        */
    }
    private void setEntityDescriptor(EntityDescriptor value){
        entityDescriptor = value;
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cmbSelectSlotDuration = new javax.swing.JComboBox<Duration>();
        jPanel4 = new javax.swing.JPanel();
        spnSlotSearchOffset = new javax.swing.JSpinner();
        rdbSelectMonths = new javax.swing.JRadioButton();
        rdbSelectWeeks = new javax.swing.JRadioButton();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setPreferredSize(new java.awt.Dimension(266, 144));

        jLabel2.setText("  Slot duration");

        cmbSelectSlotDuration.setModel(new javax.swing.DefaultComboBoxModel<>(new Duration[] {
            Duration.ofMinutes(0),
            Duration.ofMinutes(5),
            Duration.ofMinutes(10),
            Duration.ofMinutes(15),
            Duration.ofMinutes(20),
            Duration.ofMinutes(25),
            Duration.ofMinutes(30),
            Duration.ofMinutes(35),
            Duration.ofMinutes(40),
            Duration.ofMinutes(45),
            Duration.ofMinutes(50),
            Duration.ofMinutes(55),
            Duration.ofMinutes(60),
            Duration.ofMinutes(75),
            Duration.ofMinutes(90),
            Duration.ofMinutes(105),
            Duration.ofMinutes(120),
            Duration.ofMinutes(180),
            Duration.ofMinutes(240),
            Duration.ofMinutes(300),
            Duration.ofMinutes(360),
            Duration.ofMinutes(420),
            Duration.ofMinutes(480)}));
cmbSelectSlotDuration.setBorder(javax.swing.BorderFactory.createEtchedBorder());
cmbSelectSlotDuration.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        cmbSelectSlotDurationActionPerformed(evt);
    }
    });

    jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Search start date offset by ..."));

    rdbSelectMonths.setText("month(s)");

    rdbSelectWeeks.setText("week(s)");

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addGap(51, 51, 51)
            .addComponent(spnSlotSearchOffset, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rdbSelectWeeks)
            .addGap(12, 12, 12)
            .addComponent(rdbSelectMonths)
            .addGap(7, 7, 7))
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(spnSlotSearchOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(rdbSelectMonths)
                .addComponent(rdbSelectWeeks))
            .addGap(10, 10, 10))
    );

    spnSlotSearchOffset.setModel(new SpinnerNumberModel(0,0,12,1));

    btnSave.setText("Start scan for empty slots");
    btnSave.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnSaveActionPerformed(evt);
        }
    });

    btnCancel.setText("Cancel");
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnCancelActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGap(20, 20, 20)
            .addComponent(btnSave)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(25, 25, 25))
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(39, 39, 39)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cmbSelectSlotDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGap(9, 9, 9)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel2)
                .addComponent(cmbSelectSlotDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnSave)
                .addComponent(btnCancel))
            .addGap(18, 18, 18))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(15, 15, 15)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(20, 20, 20))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbSelectSlotDurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSelectSlotDurationActionPerformed
        if ((this.cmbSelectSlotDuration.getSelectedIndex()==0) ||
            (this.cmbSelectSlotDuration.getSelectedIndex()==-1)){
            this.rdbSelectMonths.setEnabled(false);
            this.rdbSelectWeeks.setEnabled(false);
            this.spnSlotSearchOffset.setEnabled(false);
            this.cmbSelectSlotDuration.setForeground(Color.red);
        }
        else{

            this.rdbSelectMonths.setEnabled(true);
            this.rdbSelectWeeks.setEnabled(true);
            this.spnSlotSearchOffset.setEnabled(true);
            this.cmbSelectSlotDuration.setForeground(Color.black);
        }
    }//GEN-LAST:event_cmbSelectSlotDurationActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        Duration duration = (Duration)this.cmbSelectSlotDuration.getSelectedItem();
        LocalDate startScanDate = getEntityDescriptor().getRequest().getDay();
        if (!duration.isZero()){
            if(this.rdbSelectWeeks.isSelected()){
                startScanDate = startScanDate.plusWeeks((Integer)this.spnSlotSearchOffset.getValue());
            }
            else startScanDate = startScanDate.plusMonths((Integer)this.spnSlotSearchOffset.getValue());

            getEntityDescriptor().getRequest().setDay(startScanDate);
            getEntityDescriptor().getRequest().setDuration(duration);
            ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                EntityDescriptor.AppointmentViewControllerActionEvent.
                APPOINTMENT_SLOTS_FROM_DATE_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        //EmptySlotScanEditorModalViewer.this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<Duration> cmbSelectSlotDuration;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton rdbSelectMonths;
    private javax.swing.JRadioButton rdbSelectWeeks;
    private javax.swing.JSpinner spnSlotSearchOffset;
    // End of variables declaration//GEN-END:variables

    private void setMyViewType(View.Viewer value){
        this.myViewType = value;
    }

    @Override
    public View.Viewer getMyViewType(){
        return this.myViewType;
    }
}


