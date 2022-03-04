/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

/**
 *
 * @author colin
 */
public interface IEntityStoreType {
    public boolean isAppointment();
    public boolean isAppointments();
    public boolean isAppointmentDate();
    public boolean isAppointmentTable();
    public boolean isAppointmentTableRowValue();
    public boolean isPatient();
    public boolean isPatients();
    public boolean isPatientTable();
    public boolean isPatientTableRowValue();
    public boolean isSurgeryDaysAssignment();

}
