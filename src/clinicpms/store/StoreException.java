/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.Store.ExceptionType;

/**
 * Wrapper for all exceptions thrown by the store, the cause of which is
 * defined by the message and an error number
 * @author colin
 */
public class StoreException extends Exception{
    private ExceptionType  exceptionType = null;
    
    public StoreException(String s, ExceptionType e){
        super(s);
        exceptionType = e;
    }
    public void setErrorType(ExceptionType exceptionType){
        this.exceptionType = exceptionType;
    }
    public ExceptionType getErrorType(){
        return this.exceptionType;
    }
}
