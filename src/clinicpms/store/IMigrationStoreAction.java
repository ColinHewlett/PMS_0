/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.AppointmentTable;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysTable;
import clinicpms.model.SurgeryDaysAssignment;

/**
 *
 * @author colin
 */
public interface IMigrationStoreAction {
    public void checkIntegrity()throws StoreException;
    public int countRowsInTable(AppointmentTable table)throws StoreException;
    public int countRowsInTable(PatientTable table)throws StoreException;
    public int countRowsInTable(SurgeryDaysTable table) throws StoreException;
    public void create(AppointmentTable table)throws StoreException;
    public void create(PatientTable table)throws StoreException;
    public void create(SurgeryDaysTable table)throws StoreException;
    public void drop(AppointmentTable table)throws StoreException;
    public void drop(PatientTable table)throws StoreException;
    public void drop(SurgeryDaysTable table)throws StoreException;
    public void populate(AppointmentTable table)throws StoreException;
    public void populate(PatientTable table)throws StoreException;
    public void populate(SurgeryDaysAssignment data)throws StoreException;
}
