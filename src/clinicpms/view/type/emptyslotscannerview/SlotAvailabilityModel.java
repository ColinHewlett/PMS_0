/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.type.emptyslotscannerview;

import clinicpms.controller.EntityDescriptor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.event.ListDataListener;
import javax.swing.ListModel;

/**
 *
 * @author colin
 */
public class SlotAvailabilityModel implements ListModel{
    private LinkedList<EntityDescriptor.Appointment> data;
    private LinkedList<ListDataListener> listeners;

    public SlotAvailabilityModel() {
        data = new LinkedList<>();
        listeners = new LinkedList<>();
    }
    
    public void addElement(EntityDescriptor.Appointment element){
        data.add(element);
    }
    
    public void removeAllElements(){
        data.clear();
    }
    
    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public Object getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    } 
}
