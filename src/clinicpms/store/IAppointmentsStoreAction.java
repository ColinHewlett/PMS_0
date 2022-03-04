/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment.Category;
import clinicpms.model.Patient;
import clinicpms.model.Appointment;
import clinicpms.model.Appointments;
import java.util.ArrayList;
import java.time.LocalDate;
/**
 *
 * @author colin
 */
public interface IAppointmentsStoreAction {
    public int countRowsIn(Appointments a)throws StoreException;
    public Appointments readAppointments(Patient p, Category c) throws StoreException;
    public Appointments readAppointments() throws StoreException;
    public Appointments readAppointmentsFor(LocalDate day) throws StoreException;
    public Appointments readAppointmentsFrom(LocalDate day) throws StoreException;
    public void insert(Appointments appointments)throws StoreException;
}
