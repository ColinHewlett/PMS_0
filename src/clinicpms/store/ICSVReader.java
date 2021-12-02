/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment; 
import java.util.ArrayList;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;

/**
 *
 * @author colin
 */
public interface ICSVReader {
    public ArrayList<Appointment> getAppointments()throws StoreException;
    public ArrayList<Patient> getPatients() throws StoreException;
}
