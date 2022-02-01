/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment.Category;
import clinicpms.model.Patient;
import clinicpms.model.Appointment;
import java.util.ArrayList;
import java.time.LocalDate;
/**
 *
 * @author colin
 */
public interface IAppointmentsStoreAction {
    public ArrayList<Appointment> readAppointments(Patient p, Category c) throws StoreException;
    public ArrayList<Appointment> readAppointments() throws StoreException;
    public ArrayList<Appointment> readAppointmentsFor(LocalDate day) throws StoreException;
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException;
}
