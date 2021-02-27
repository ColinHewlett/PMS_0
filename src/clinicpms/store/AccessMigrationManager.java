/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.exceptions.StoreException;

/**
 *
 * @author colin
 */
public class AccessMigrationManager {
    public static void action(Store.MigrationMethod mm)throws StoreException{
        switch (mm){
            case ACCESS_PATIENT_PREPROCESS -> AccessStore.getInstance(); 
        }
    }
}
