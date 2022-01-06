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
public interface IEntityCounter {
    public int count() throws StoreException;
}
