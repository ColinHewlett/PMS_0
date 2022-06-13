/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Appointments;
import clinicpms.model.AppointmentTable;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.model.PatientTable;
import clinicpms.model.SurgeryDaysAssignmentTable;
//import clinicpms.model.SurgeryDaysAssignmentx;
import clinicpms.model.IEntityStoreType;
import java.util.List;

/**
 *
 * @author colin
 */
public interface IMigrationStoreAction {
    public void checkIntegrity()throws StoreException;
    //public void closeMigrationConnection() throws StoreException;
    public int countRowsIn(AppointmentTable table)throws StoreException;
    public int countRowsIn(PatientTable table)throws StoreException;
    public int countRowsIn(SurgeryDaysAssignmentTable table) throws StoreException;
    public void create(AppointmentTable table)throws StoreException;
    public void create(PatientTable table)throws StoreException;
    public void create(SurgeryDaysAssignmentTable table)throws StoreException;
    public void drop(AppointmentTable table)throws StoreException;
    public void drop(PatientTable table)throws StoreException;
    public void drop(SurgeryDaysAssignmentTable table)throws StoreException;
    public void exportToPMS(Appointments table)throws StoreException;
    public void exportToPMS(Patients table)throws StoreException;
    //public void exportToPMS(SurgeryDaysAssignmentx table)throws StoreException;
    //public IEntityStoreType importFromCSV(IEntityStoreType entity) throws StoreException;
    public List<String[]> importFromCSV1(IEntityStoreType entity) throws StoreException;
    public void insert(AppointmentTable table, Appointment appointment)throws StoreException;
    public void insert(PatientTable table, Patient patient)throws StoreException;
    //public void populate(AppointmentTable table)throws StoreException;
    //public void populate(PatientTable table)throws StoreException;
    //public void populate(SurgeryDaysAssignmentx data)throws StoreException;
    public Appointments read(AppointmentTable table)throws StoreException;
    public Patients read(PatientTable table)throws StoreException;
    //public SurgeryDaysAssignmentx read(SurgeryDaysAssignmentTable table)throws StoreException;

}
