/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Patient;
import clinicpms.model.Patients;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public interface IPatientsStoreAction {
    public int countRowsIn(Patients p) throws StoreException;
    public Patients readPatients() throws StoreException;
    public void insert(Patients patients) throws StoreException;
}
