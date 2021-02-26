/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store.exceptions;

/**
 *
 * @author colin
 */
public class NullKeyExpectedException extends Exception {
    public NullKeyExpectedException(String s){
        super(s);
    }
}
