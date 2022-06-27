/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;


import clinicpms.model.ThePatient;

/**
 *
 * @author colin
 */
final class PatientDelegate extends ThePatient {
    private ThePatient patient;
    
    protected PatientDelegate(){
        super();
    }
    
    protected PatientDelegate(Integer key){
        super();
        setPatientKey(key);
    }

    protected PatientDelegate(ThePatient patient){
        super();
        copyPatientState(patient);
    }
    
    private void copyPatientState(ThePatient patient){
        super.getAddress().setLine1(patient.getAddress().getLine1());
        super.getAddress().setLine2(patient.getAddress().getLine1());
        super.getAddress().setTown(patient.getAddress().getTown());
        super.getAddress().setCounty(patient.getAddress().getCounty());
        super.getAddress().setPostcode(patient.getAddress().getPostcode());
        super.setDOB(patient.getDOB());
        super.setGender(patient.getGender());
        super.setGuardian(patient.getGuardian());
        super.setAppointmentHistory(patient.getAppointmentHistory());
        super.setIsGuardianAPatient(patient.getIsGuardianAPatient());
        super.getName().setForenames(patient.getName().getForenames());
        super.getName().setSurname(patient.getName().getSurname());
        super.getName().setTitle(patient.getName().getTitle());
        super.setNotes(patient.getNotes());
        super.setPhone1(patient.getPhone1());
        super.getRecall().setDentalDate(patient.getRecall().getDentalDate());
        super.getRecall().setDentalFrequency(patient.getRecall().getDentalFrequency());
    }
    
    protected Integer getPatientKey(){
        return super.getKey();
    }
    protected void setPatientKey(Integer key){
        super.setKey(key);
    }  
}