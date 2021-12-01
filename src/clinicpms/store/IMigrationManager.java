/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import java.time.Duration;
import java.util.ArrayList;


/**
 *
 * @author colin
 */
public interface IMigrationManager {
    public void action(Store.MigrationMethod mm)throws StoreException;
    public ArrayList<Appointment> getAppointments();
    public ArrayList<Patient> getPatients();
    public void setAppointments(ArrayList<Appointment> appointments);
    public void setPatients(ArrayList<Patient> patients);
    public int getAppointmentCount();
    public int getPatientCount();
    public int getNonExistingPatientsReferencedByAppointmentsCount();
    public int getUnfilteredAppointmentCount();
    public int getFilteredAppointmentCount();
    public Duration getMigrationActionDuration();
    public void setAppointmentCount(int value);
    public void setPatientCount(int count);
    public void setNonExistingPatientsReferencedByAppointmentsCount(int count);
    public void setUnfilteredAppointmentCount(int count);
    public void setFilteredAppointmentCount(int count);
    public void setMigrationActionDuration(Duration value);
    

            //int filteredAppointmentCount = 0;

}

