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
public class NullKeyException extends Exception{
    public NullKeyException(String s) 
    { 
        // Call constructor of parent Exception 
        super(s); 
    }
}
