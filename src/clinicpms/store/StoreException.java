/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;


/**
 * Wrapper for all exceptions thrown by the store, the cause of which is
 * defined by the message and an error number
 * @author colin
 */
public class StoreException extends Exception{
    
    
    private ExceptionType  exceptionType = null;
    
    public static enum ExceptionType {  
                                 DROP_APPOINTMENT_TABLE_EXCEPTION,
                                 DROP_PATIENT_TABLE_EXCEPTION,
                                 APPOINTMENT_TABLE_MISSING_IN_MIGRATION_DATABASE,
                                 APPOINTMENT_TABLE_MISSING_IN_PMS_DATABASE,
                                 INTEGRITY_CONSTRAINT_VIOLATION,
                                 MIGRATION_CONNECTION_FAILURE,
                                 PATIENT_TABLE_MISSING_IN_MIGRATION_DATABASE,
                                 PATIENT_TABLE_MISSING_IN_PMS_DATABASE,
                                 SURGERY_DAYS_TABLE_MISSING_IN_MIGRATION_DATABASE,
                                 SURGERY_DAYS_TABLE_MISSING_IN_PMS_DATABASE,
                                 APPOINTEE_NOT_FOUND_EXCEPTION,
                                 IO_EXCEPTION,
                                 CSV_EXCEPTION,
                                 NULL_KEY_EXPECTED_EXCEPTION,
                                 NULL_KEY_EXCEPTION,
                                 INVALID_KEY_VALUE_EXCEPTION,
                                 KEY_FOUND_EXCEPTION,
                                 KEY_NOT_FOUND_EXCEPTION,
                                 SQL_EXCEPTION,
                                 STORE_EXCEPTION,
                                 UNEXPECTED_DATA_TYPE_ENCOUNTERED,
                                 UNDEFINED_DATABASE}
    
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
