/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.CSVStore;
import clinicpms.store.exceptions.StoreException;
import java.util.ArrayList;

/**
 *
 * @author colin
 */
public class Patients {
    public ArrayList<Patient> getPatients() throws StoreException{
        return CSVStore.getInstance().readPatients();
    }
}
