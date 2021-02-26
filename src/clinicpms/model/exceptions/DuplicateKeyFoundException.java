/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model.exceptions;

/**
 *
 * @author colin
 */
public class DuplicateKeyFoundException extends Exception{
    public DuplicateKeyFoundException(String s) 
    { 
        // Call constructor of parent Exception 
        super(s); 
    }
}
