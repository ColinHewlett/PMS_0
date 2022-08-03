/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;
import clinicpms.store.StoreException;
import java.io.File;

/**
 *
 * @author colin
 */
public interface IStoreManager {
    public File createStore(File file)throws StoreException;    
    public String getAppointmentCSVPath()throws StoreException;
    public String getPatientCSVPath()throws StoreException;
    public String getPMSStorePath()throws StoreException;
    public void setAppointmentCSVPath(String path)throws StoreException;
    public void setPatientCSVPath(String path)throws StoreException;
    public void setPMSStorePath(String path)throws StoreException;
    
}
