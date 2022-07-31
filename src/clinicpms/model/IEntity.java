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
public interface IEntity {
    public void create()throws StoreException;
    public void drop() throws StoreException;
    public void delete() throws StoreException;
    public void insert() throws StoreException;
    public EntityStoreType read() throws StoreException;
    public void update() throws StoreException;
}
