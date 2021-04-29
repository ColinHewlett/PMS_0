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
public class CSVMigrationManager {
    public static void action(Store.MigrationMethod mm)throws StoreException{
        switch (mm){
            case CSV_APPOINTMENT_FILE_CONVERTER:
                CSVStore.getInstance().appointmentfileConverter();
                break;
            case CSV_MIGRATION_INTEGRITY_PROCESS:
                CSVStore.getInstance().checkAppointeeExists1();
                break;
            case CSV_PATIENT_FILE_CONVERTER:
                CSVStore.getInstance().patientfileConverter();
                break;
        }
    }
}
