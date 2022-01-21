/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

/**
 *
 * @author colin
 */
public interface  ITargetsStoreAction {
    public String getStoreType();
    public String readAppointmentCSVPath() throws StoreException;
    public String readMigrationTargetStorePath()throws StoreException;
    public String readPatientCSVPath() throws StoreException;
    public String readPMSTargetStorePath()throws StoreException;
    public void updateMigrationTargetStorePath(String path)throws StoreException;
    public void updatePMSTargetStorePath(String path)throws StoreException;
    public void updateAppointmentCSVPath(String path)throws StoreException;
    public void updatePatientCSVPath(String path)throws StoreException;
}
