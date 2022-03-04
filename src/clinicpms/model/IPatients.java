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
public interface IPatients {
    public Integer count()throws StoreException;
    public void read() throws StoreException;
    public void insert() throws StoreException;
            
}
