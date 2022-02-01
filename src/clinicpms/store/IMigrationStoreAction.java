/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointments;
import clinicpms.model.AppointmentTable;
import clinicpms.model.AppointmentTableRowValue;
import clinicpms.model.Patients;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysAssignmentTable;
import clinicpms.model.SurgeryDaysAssignment;
import java.sql.SQLException;

/**
 *
 * @author colin
 */
public interface IMigrationStoreAction {
    public void checkIntegrity()throws StoreException;
    public int countRowsIn(AppointmentTable table)throws StoreException;
    public int countRowsIn(PatientTable table)throws StoreException;
    //public int countRowsInTable(SurgeryDaysAssignmentTable table) throws StoreException;
    public void create(AppointmentTable table)throws StoreException;
    public void create(PatientTable table)throws StoreException;
    public void create(SurgeryDaysAssignmentTable table)throws StoreException;
    public void drop(AppointmentTable table)throws StoreException;
    public void drop(PatientTable table)throws StoreException;
    public void drop(SurgeryDaysAssignmentTable table)throws StoreException;
    public void populate(AppointmentTable table)throws StoreException;
    public void populate(PatientTable table)throws StoreException;
    public void populate(SurgeryDaysAssignment data)throws StoreException;
    public Appointments read(AppointmentTable table)throws StoreException;
    public Patients read(PatientTable table)throws StoreException;
    public SurgeryDaysAssignment read(SurgeryDaysAssignmentTable table)throws StoreException;


}
