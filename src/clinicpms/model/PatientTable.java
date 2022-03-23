/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.Store;
import clinicpms.store.StoreException;
import clinicpms.store.IMigrationStoreAction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author colin
 */
public class PatientTable extends ArrayList<Patient> implements ITable,
                                                                IEntityStoreType{
    private enum DenPatField {KEY,
                              TITLE,
                              FORENAMES,
                              SURNAME,
                              LINE1,
                              LINE2,
                              TOWN,
                              COUNTY,
                              POSTCODE,
                              PHONE1,
                              PHONE2,
                              GENDER,
                              DOB,
                              IS_GUARDIAN_A_PATIENT,
                              DENTAL_RECALL_FREQUENCY,
                              DENTAL_RECALL_DATE,
                              NOTES,
                              GUARDIAN}
    private static final DateTimeFormatter ddMMyyyyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int count;
    private List<String[]> importedDBFRecords = null;
    
    private List<String[]> getImportedDBFRecords(){
        return importedDBFRecords;
    }
    
    private void setImportedDBFRecords(List<String[]>value){
        importedDBFRecords = value;
    }
    
    public boolean isAppointment(){
        return false;
    }
    
    public boolean isAppointmentDate(){
        return false;
    }
    
    public boolean isAppointments(){
        return false;
    }
    
    public boolean isAppointmentTable(){
        return false;
    }
    
    public boolean isAppointmentTableRowValue(){
        return false;
    }
    
    public boolean isPatient(){
        return false;
    }
    
    public boolean isPatients(){
        return false;
    }
    
    public boolean isPatientTable(){
        return true;
    }
    
    public boolean isPatientTableRowValue(){
        return false;
    }
     
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    @Override
    public void create() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);   
        store.create(this);
    }
    
    @Override
    public void drop()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.drop(this);
    }
    
    @Override
    public void exportToPMS() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.exportToPMS(new Patients());
    }
    
    public List<String[]> importFromCSV1()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        //setImportedDBFRecords(store.importFromCSV1(this));
        return store.importFromCSV1(this);
    }
    
    /*
    public void insert()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.insert(this);  
    }
    */
    /**
     * 14/03/2022 17:23 update
     * @param patient:Patient, the patient object to be inserted into the PatientTable 
     * @throws StoreException 
     */
    public void insert(Patient patient)throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        store.insert(this,patient);  
    }
    
    public Patient convertDBFToPatient(String[] dbfPatientRow){
        Patient patient = new Patient();
        for (DenPatField pf: DenPatField.values()){
            switch (pf){
                case KEY:
                    patient.setKey(Integer.parseInt(dbfPatientRow[pf.ordinal()]));
                    //if (patient.getKey() == 10791) isSelectedKey = true;
                    break;
                case TITLE:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.getName().setTitle(dbfPatientRow[pf.ordinal()]);   
                    }
                    else patient.getName().setTitle("");
                    break;
                case FORENAMES:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.getName().setForenames(dbfPatientRow[pf.ordinal()]);
                    }
                    else patient.getName().setForenames("");
                    break;
                case SURNAME: 
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.getName().setSurname(dbfPatientRow[pf.ordinal()]);
                    }
                    else patient.getName().setSurname("");
                    break;
                case LINE1:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.getAddress().setLine1(dbfPatientRow[pf.ordinal()]);
                    }
                    else patient.getAddress().setLine1("");
                    break;
                case LINE2:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.getAddress().setLine2(dbfPatientRow[pf.ordinal()]);
                    }
                    else patient.getAddress().setLine2("");
                    break;
                case TOWN:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.getAddress().setTown(dbfPatientRow[pf.ordinal()]);
                    }
                    else patient.getAddress().setTown("");
                    break;
                case COUNTY:
                    patient.getAddress().setCounty(dbfPatientRow[pf.ordinal()]);
                    break;
                case POSTCODE:
                    patient.getAddress().setPostcode(dbfPatientRow[pf.ordinal()]);
                    break;
                case PHONE1:
                    patient.setPhone1(dbfPatientRow[pf.ordinal()]);
                    break;
                case PHONE2:
                    patient.setPhone2(dbfPatientRow[pf.ordinal()]);
                    break;
                case GENDER:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.setGender(dbfPatientRow[pf.ordinal()]);
                    }
                    else patient.setGender("");
                    break;
                case DOB:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.setDOB(LocalDate.parse(dbfPatientRow[pf.ordinal()],ddMMyyyyFormat));
                    }
                    else patient.setDOB(null);
                    break;
                case IS_GUARDIAN_A_PATIENT:
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        patient.setIsGuardianAPatient(Boolean.valueOf(dbfPatientRow[pf.ordinal()]));
                    }
                    else patient.setIsGuardianAPatient(Boolean.valueOf(false));
                    break;
                case DENTAL_RECALL_FREQUENCY:
                    boolean isDigit = true;
                    Integer value = 0;
                    String s = dbfPatientRow[pf.ordinal()];
                    if (!s.isEmpty()){
                        s = s.strip();
                        char[] c = s.toCharArray(); 
                        for (int index = 0; index < c.length; index++){
                            if (!Character.isDigit(c[index])){
                                isDigit = false;
                                break;
                            }
                        }
                        if (isDigit){
                            value = Integer.parseInt(s);
                        }
                        else value = 0;
                    }
                    else value = 0;
                    patient.getRecall().setDentalFrequency(value);
                    break;
                /**
                 * huge issue here
                 * -- Excel file saved as CSV renders recall date as (for example) "May-07"
                 * -- in fact actual data is "MAY/02"!!
                 * -- also isolated occurrence of "APR/ 9" which logic couldn't handle
                 * -- eventual logic if 1st char after "/" is blank, remove blank with call to String strip()
                 * -- also logic overlooked checking for a date in the 80's, now corrected
                 */
                case DENTAL_RECALL_DATE: 
                    boolean isInvalidMonth = false;
                    String $value = "";
                    String[] values;
                    int mm = 0;
                    int yyyy = 0;
                    LocalDate recallDate = null;
                    $value = dbfPatientRow[pf.ordinal()];
                    /*
                    if (isSelectedKey){
                        int test = 0;
                        test++;
                    }
                    */
                    isInvalidMonth = false;
                    if ($value.length()>1){
                        $value = $value.strip();
                        values = $value.split("-");
                        if (values.length > 0){
                            switch (values[0]){
                                case "Jan": 
                                    mm = 1;
                                    break;
                                case "Feb":
                                    mm = 2;
                                    break;
                                case "Mar":
                                    mm = 3;
                                    break;
                                case "Apr":
                                    mm = 4;
                                    break;
                                case "May":
                                    mm = 5;
                                    break;
                                case "Jun":
                                    mm = 6;
                                    break;
                                case "Jul":
                                    mm = 7;
                                    break;
                                case "Aug":
                                    mm = 8;
                                    break;
                                case "Sep":
                                    mm = 9;
                                    break;
                                case "Oct":
                                    mm = 10;
                                    break;
                                case "Nov":
                                    mm = 11;
                                    break;
                                case "Dec":
                                    mm = 12;
                                    break;
                                default:
                                    isInvalidMonth = true;
                                    break;
                            }
                        }
                        if (!isInvalidMonth){
                            if ((values[1].substring(0,1).equals("9")) || (values[1].substring(0,1).equals("8"))){
                                yyyy = 1900 + Integer.parseInt(values[1]); 
                                //break;
                            }
                            else if(values[1].substring(0,1).equals(" ")){
                                String v = values[1].strip();
                                yyyy = 2000 + Integer.parseInt(v);
                            }
                            else{
                                yyyy = 2000 + Integer.parseInt(values[1]);
                            }
                            recallDate = LocalDate.of(yyyy, mm, 1);
                            patient.getRecall().setDentalDate(recallDate);
                        }
                        else patient.getRecall().setDentalDate(null);
                    }
                    break;
                case NOTES:   
                    String notes = "";
                    if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                        notes = notes + dbfPatientRow[pf.ordinal()];
                    }

                    if (!dbfPatientRow[pf.ordinal()+1].isEmpty()){
                        if (!notes.isEmpty()){
                            notes = notes + "; ";
                        }
                        notes = notes + dbfPatientRow[pf.ordinal()+1];
                    }
                    patient.setNotes(notes);

                //case GUARDIAN -> patient.setGuardian(null); 
            }
        }
        patient.setIsGuardianAPatient(Boolean.FALSE);
        patient.setGuardian(null);
        return patient;
    }
    /*
    05/03/2022 20:09
    @Override
    public void populate() throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this); 
        store.populate(this);
    } 
    */
    
    @Override
    public IEntityStoreType read()throws StoreException{
        IMigrationStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    public Integer count() {
        Integer result = null;
        IEntityStoreType entity = null;
        Integer rowcount = null;
        try{
            IMigrationStoreAction store = Store.FACTORY(this);
            result = store.countRowsIn(this);
        }catch (StoreException ex){
            /**
             * if MigrationSQL.PATIENT_TABLE_ROW_COUNT is source of exception
             * -- assumed this is cause because the AppointmentTable is currently missing from the database schema 
             */
            if (!ex.getErrorType().equals(StoreException.ExceptionType.PATIENT_TABLE_MISSING_IN_MIGRATION_DATABASE)){
                //throw new StoreException(ex.getMessage(), ex.getErrorType());
                result = null;
            }
        }
        return result;
    }
}
