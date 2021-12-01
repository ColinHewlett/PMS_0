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
import java.sql.Connection;
import clinicpms.store.Store.TargetDatabase;
import clinicpms.store.exceptions.StoreException;

public interface ITargetsDatabaseManager {
    public Connection getConnection() throws StoreException;
    public void closeConnection() throws StoreException;
    public String read(TargetDatabase db) throws StoreException;
    public String update(String updatedLocation, TargetDatabase db) throws StoreException;
}