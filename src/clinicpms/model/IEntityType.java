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
public interface IEntityType {
    public boolean isAppointment();
    public boolean isPatient();
    public boolean isSurgeryDaysAssignment();
    public boolean isAppointmentDate();
}
