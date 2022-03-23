/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.StoreException;
/**
 *
 * @author colin
 */
public interface ITable {
    public void create()throws StoreException;
    public void drop()throws StoreException;
    public void exportToPMS() throws StoreException;
    //05/03/2022 20:09 
    //public void populate()throws StoreException;
    //public void importFromCSV()throws StoreException;
    public IEntityStoreType read() throws StoreException;
}
