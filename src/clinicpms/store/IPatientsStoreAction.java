/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Patient;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public interface IPatientsStoreAction {
    public ArrayList<Patient> readPatients() throws StoreException;
}
