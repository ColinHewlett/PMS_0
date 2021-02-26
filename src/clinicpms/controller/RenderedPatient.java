/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import java.time.LocalDate;

/**
 *
 * @author colin
 */
public class RenderedPatient {
    private Integer key = null;
    private String title = null;
    private String forenames = null;
    private String surname = null;
    private String line1 = null;
    private String line2 = null;
    private String town = null;
    private String county = null;
    private String postcode = null;
    private String phone1 = null;
    private String phone2 = null;
    private String gender = null;
    private LocalDate dob = null;
    private boolean isGuardianAPatient = false;
    private RenderedPatient guardian = null;
    private String notes = null; 
    private LocalDate dentalRecallDate = null;
    private LocalDate hygieneRecallDate = null;
    private int dentalRecallFrequency;
    private int hygieneRecallFrequency;
    private RenderedAppointment lastDentalAppointment = null;
    private RenderedAppointment lastHygieneAppointment = null;
    private RenderedAppointment nextDentalAppointment = null;
    private RenderedAppointment nextHygieneAppointment = null;
    private boolean isKeyDefined = false;
    
    protected Integer getKey(){
        return key;
    }
    protected void setKey(Integer value){
        key = value;
    }
    
    public boolean getIsKeyDefined(){
        return this.isKeyDefined;
    }
    protected void setIsKeyDefined(boolean value){
        this.isKeyDefined = value;
    }
    
    public String getTitle(){
        return title;
    }
    public void setTitle(String value){
        title = value;
    }
    
    public String getForenames(){
        return forenames;
    }
    public void setForenames(String value){
        forenames = value;
    }
    
    public String getSurname(){
        return surname;
    }
    public void setSurname(String value){
        surname = value;
    }
    
    public String getLine1(){
        return line1;
    }
    public void setLine1(String value){
        line1 = value;
    }
    
    public String getLine2(){
        return line2;
    }
    public void setLine2(String value){
        line2 = value;
    }
    
    public String getTown(){
        return town;
    }
    public void setTown(String value){
        town = value;
    }
    
    public String getCounty(){
        return county;
    }
    public void setCounty(String value){
        county = value;
    }
    
    public String getPostcode(){
        return postcode;
    }
    public void setPostcode(String value){
        postcode = value;
    }
    
    public String getPhone1(){
        return phone1;
    }
    public void setPhone1(String value){
        phone1 = value;
    }
    
    public String getPhone2(){
        return phone2;
    }
    public void setPhone2(String value){
        phone2 = value;
    }
    
    public String getGender(){
        return gender;
    }
    public void setGender(String value){
        gender = value;
    }
    
    public LocalDate getDOB(){
        return dob;
    }
    public void setDOB(LocalDate value){
        dob = value;
    }
    
    public boolean getIsGuardianAPatient(){
        return isGuardianAPatient;
    }
    public void setIsGuardianAPatient(boolean value){
        isGuardianAPatient = value;
    }
    
    public String getNotes(){
        return notes;
    }
    public void setNotes(String value){
        notes = value;
    }
    
    public LocalDate getDentalRecallDate(){
        return dentalRecallDate;
    }
    public void setDentalRecallDate(LocalDate value){
        dentalRecallDate = value;
    }
    
    public LocalDate getHygieneRecallDate(){
        return hygieneRecallDate;
    }
    public void setHygieneRecallDate(LocalDate value){
        hygieneRecallDate = value;
    }
    
    public int getDentalRecallFrequency(){
        return dentalRecallFrequency;
    }
    public void setDentalRecallFrequency(int value){
        dentalRecallFrequency = value;
    }
    
    public int getHygieneRecallFrequency(){
        return hygieneRecallFrequency;
    }
    public void setHygieneRecallFrequency(int value){
        hygieneRecallFrequency = value;
    }


    /*
    @Override
    public String toString(){
        String name = null;
        if (title != null){
            name = name + title;
        }
        if (forenames != null){
            if (name!=null){
                name = name + " " + forenames;
            }
            else{
                name = forenames;
            }
        }
        if (surname!=null){
            if (name!=null){
                name = name + " " + surname;
            }
            else {
                name = surname;
            }
        }
        return name;
    }
*/
}
