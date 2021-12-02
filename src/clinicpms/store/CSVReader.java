/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment; 
import java.util.ArrayList;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import clinicpms.store.migration_import_store.CSVStore;
import static clinicpms.store.migration_import_store.CSVStore.ddMMyyyyFormat;
import static clinicpms.store.migration_import_store.CSVStore.getAppointmentCSVPath;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author colin
 */
public class CSVReader implements ICSVReader{
    
    private static enum DenAppField {DATE,
                                A_1,
                                A_2,
                                A_3,
                                A_4,
                                A_5,
                                A_6,
                                A_7,
                                A_8,
                                A_9,
                                A_10,
                                A_11,
                                A_12,
                                A_13,
                                A_14,
                                A_15,
                                A_16,
                                A_17,
                                A_18,
                                A_19,
                                A_20,
                                A_21,
                                A_22,
                                A_23,
                                A_24,
                                A_25,
                                A_26,
                                A_27,
                                A_28,
                                A_29,
                                A_30,
                                A_31,
                                A_32,
                                A_33,
                                A_34,
                                A_35,
                                A_36,
                                A_37,
                                A_38,
                                A_39,
                                A_40,
                                A_41,
                                A_42,
                                A_43,
                                A_44,
                                A_45,
                                A_46,
                                A_47,
                                A_48,
                                A_49,
                                A_50,
                                A_51,
                                A_52,
                                A_53,
                                A_54,
                                A_55,
                                A_56,
                                A_57,
                                A_58,
                                A_59,
                                A_60,
                                A_61,
                                A_62,
                                A_63,
                                A_64,
                                A_65,
                                A_66,
                                A_67,
                                A_68,
                                A_69,
                                A_70,
                                A_71,
                                A_72,
                                A_73,
                                A_74,
                                A_75,
                                A_76,
                                A_77,
                                A_78,
                                A_79,
                                A_80,
                                A_81,
                                A_82,
                                A_83,
                                A_84,
                                A_85,
                                A_86,
                                A_87,
                                A_88,
                                A_89,
                                A_90,
                                A_91,
                                A_92,
                                A_93,
                                A_94,
                                A_95,
                                A_96,
                                A_97,
                                A_98,
                                A_99,
                                A_100,
                                A_101,
                                A_102,
                                A_103,
                                A_104,
                                A_105,
                                A_106,
                                A_107,
                                A_108,
                                A_109,
                                A_110,
                                A_111,
                                A_112,
                                A_113,
                                A_114,
                                A_115,
                                A_116,
                                A_117,
                                A_118,
                                A_119,
                                A_120,
                                A_121,
                                A_122,
                                A_123,
                                A_124,
                                A_125,
                                A_126,
                                A_127,
                                A_128,
                                A_129,
                                A_130,
                                A_131,
                                A_132,
                                A_133,
                                A_134,
                                A_135,
                                A_136,
                                A_137,
                                A_138,
                                A_139,
                                A_140,
                                A_141,
                                A_142,
                                A_143,
                                A_144
    }
    
    private enum PatientField {KEY,
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
    
    private ArrayList<Appointment> appointments = null;
    private ArrayList<Patient> patients = null;

    
    private void setAppointments(ArrayList<Appointment> value){
        appointments = value;
    }
    
    private void setPatients(ArrayList<Patient> value){
        patients = value;
    }
    
    @Override
    public ArrayList<Patient> getPatients()throws StoreException{
        Path patientsPath = Path.of(Store.getPatientCSVPath());
        String message = null;
        try{
            BufferedReader patientReader = Files.newBufferedReader(patientsPath,StandardCharsets.ISO_8859_1);
            //BufferedReader patientReader = Files.newBufferedReader(patientsPath,StandardCharsets.UTF_8);
            com.opencsv.CSVReader csvDBFPatientsReader = new com.opencsv.CSVReader(patientReader);
            List<String[]> dbfPatients = csvDBFPatientsReader.readAll();
            convertToPatientsFromDBFFile(dbfPatients);
            //create csv file from patient collection
            //convertPatientsToCSV(patients);
            return patients;
        }
        catch (java.nio.charset.MalformedInputException e){
            message = "MalformedInputException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of appointmentReader or patientReader File object";
            throw new StoreException(message, Store.ExceptionType.IO_EXCEPTION);
        }
        catch (IOException e){
            message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of appointmentReader or patientReader File object";
            throw new StoreException(message, Store.ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            message = "CSVException " + e.getMessage();
        }
        catch (Exception e){
            message = "CSVException "  + e.getMessage() + "\n" +
            "StoreException message -> Error encountered in CSVStore::migratePatients " +
                    "on call to CSVStore::csvDBFPatientsReader";
            throw new StoreException(message, Store.ExceptionType.CSV_EXCEPTION);
        }    
        return patients;
    }
    
    public ArrayList<Appointment> getAppointments()throws StoreException{
        Path sourcePath = Path.of(getAppointmentCSVPath());
        try{
            BufferedReader appointmentReader = Files.newBufferedReader(sourcePath,StandardCharsets.ISO_8859_1);
            com.opencsv.CSVReader csvDBFAppointments = new com.opencsv.CSVReader(appointmentReader);
            List<String[]> dbfAppointments = csvDBFAppointments.readAll();
            convertToAppointmentsFromDBFFile(dbfAppointments);
            //convertAppointmentsToCSV(appointments);
            return appointments;
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in appointmentfileconverter()";
            throw new StoreException(message, Store.ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            String message = "CsvException message -> + e.getMessage()" + "\n" +
                    "StoreException message -> Error encountered in appointmentfileconverter()";
            throw new StoreException(message, Store.ExceptionType.CSV_EXCEPTION);
        }
        catch (Exception e){
            String message = "Exception message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in migrateAppointments()()";
            throw new StoreException(message, Store.ExceptionType.IO_EXCEPTION);
        }

    }
    
    public void convertToAppointmentsFromDBFFile(List<String[]> dbfAppointments)throws StoreException{
        String date = null;
        String year;
        String month;
        String d;
        Iterator<String[]> dbfAppointmentsIt = dbfAppointments.iterator();
        while(dbfAppointmentsIt.hasNext()){
            String[] dbfAppointmentRow = dbfAppointmentsIt.next();
            date = dbfAppointmentRow[DenAppField.DATE.ordinal()];
            switch (date.length()){
                case 3: 
                    date = "000" + date;
                    break;
                case 4: 
                    date = "00" + date;
                    break;
                case 5: 
                    date = "0" + date;
                    break;
            }
            if (date.substring(0,1).equals("9")){
                year = "19" + date.substring(0,2);
            }
            else if (date.substring(0,2).equals("00")){
                year = "2000";
            }
            else if (date.substring(0,1).equals("0")){
                year = "200" + date.substring(1,2);
            }
            else {
                year = "20" + date.substring(0,2);
            }
            month = date.substring(2,4);
            d = date.substring(4);
            date = d + "/" + month + "/" + year;
            makeAppointmentsFromDBFRow(dbfAppointmentRow, date, DenAppField.A_1.ordinal());           
        }   
    }
    
    /**
     * for each csv row received
     * --> make an Appointment record and collect in global ArrayList<Appointment>
     * Uses following methods
     * -- getAppointmentFrom
     * @param row
     * @param date
     * @param rowIndex
     * @throws StoreException 
     */
    private void makeAppointmentsFromDBFRow(String[] row, String date, int rowIndex) throws StoreException{
        Patient patient = null;
        LocalDateTime start = null;
        Duration duration = null;
        String notes = null;
        Integer patientKey = null;
        Integer value = null;
        int appointmentStartTimeRowIndex = 0;
        int appointmentEndTimeRowIndex = 0;
        boolean isRowEnd = false;
        Appointment appointment = null;
        //ArrayList<Appointment> appointmentsInRow= new ArrayList<>();
        for (; rowIndex < DenAppField.A_144.ordinal(); rowIndex++){
            while(row[rowIndex].isEmpty()){
                if (patientKey!=null){//signals end of appointment of current patient
                        appointmentEndTimeRowIndex = rowIndex;
                        appointment = getAppointmentFrom(row,
                                                         date,
                                                         appointmentStartTimeRowIndex,
                                                         appointmentEndTimeRowIndex,
                                                         patientKey);
                        appointment.setKey(appointments.size()+1);
                        appointments.add(appointment);
                        patientKey = null;
                }
                rowIndex++;
                if (rowIndex > DenAppField.A_143.ordinal()){
                    isRowEnd = true;
                    break;
                }
                
            }
            if (!isRowEnd){
                if (patientKey!=null){
                    value = getPatientKey(row[rowIndex]); 
                    if (value!=null){
                        if (!value.equals(patientKey)){//signals next appointment start slot
                            appointmentEndTimeRowIndex = rowIndex;
                            appointment = getAppointmentFrom(row,
                                                             date,
                                                             appointmentStartTimeRowIndex,
                                                             appointmentEndTimeRowIndex,
                                                             patientKey);
                            appointment.setKey(appointments.size()+1);
                            appointments.add(appointment);
                            patientKey = value;
                            appointmentStartTimeRowIndex = rowIndex;
                        }
                    }
                }
                else {//first appointment after a gap
                    value = getPatientKey(row[rowIndex]);
                    if (value!=null){
                        if (value.equals(20134) && date.equals("21/01/2021")){
                            value = 20134;
                        }
                        patientKey = value;//first appointment of the day
                        appointmentStartTimeRowIndex = rowIndex;
                    }
                }
            }
            else {
                break;
            }
        }
    }
    
    private static Integer getPatientKey(String s)throws StoreException{
        int index;
        Integer result = null;
        Integer c;
        boolean includesInt16Char = false;
        s = s.strip();
        if (!(s.equals("PRIVATE TIME")||
              s.equals("EMERGENCIES")||
              s.equals("emergencies")||
              s.equals("DO NOT BOOK")||
              s.equals("LUNCH TIME")||
              s.equals("LUNCHTIME")||
              s.equals("PROV. BLOCK"))){
            for (index = 0; index < s.length(); index++){
                if (!Character.isDigit(s.charAt(index))) break;
            }
            try{
                result = Integer.parseInt(s.substring(0,index));
            }
            catch (NumberFormatException e){
                throw new StoreException(s, Store.ExceptionType.IO_EXCEPTION);
            }
        }
        return result;
    }
    
    /**
     * converts row received (String[]) to an Appointment
     * -- from string to Appointment data types
     * -- some processing of notes field
     * Uses following methods
     * -- getAppointmentStartTime
     * @param row
     * @param date
     * @param startSlot
     * @param endSlot
     * @param patientKey
     * @return 
     */
    private static Appointment getAppointmentFrom(String[] row, 
                                                  String date, 
                                                  int startSlot, 
                                                  int endSlot, 
                                                  Integer patientKey){
        Patient patient;
        LocalDateTime start; 
        Duration duration;
        String notes = "";
        LocalTime startTime = getAppointmentStartTime(startSlot);
        LocalDate day = LocalDate.parse(date,ddMMyyyyFormat);
        start = LocalDateTime.of(day, startTime);
        patient = new Patient(patientKey);
        duration = Duration.ofMinutes((endSlot-startSlot)*5);
        int index = startSlot + 1;
        String keyString = String.valueOf(patient.getKey());
        for (; index < endSlot; index++){
            if (row[index].length() > keyString.length()){
                int code = (int)row[index].charAt(keyString.length());
                if (code==16){
                    row[index] = "----- \" -----";
                }
                if (!row[index].contains("----- \" -----")){
                    notes = notes + row[index].substring(keyString.length());
                }
            }
        }
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStart(start);
        appointment.setDuration(duration);
        appointment.setNotes(notes);
        
        return appointment;
    }
    
    private static LocalTime getAppointmentStartTime(int startRowIndex){
        int slotCountFromDayStart = startRowIndex - DenAppField.A_1.ordinal();
        LocalTime firstSlotTimeForDay = LocalTime.of(8, 0); //= 8am
        return firstSlotTimeForDay.plusMinutes(slotCountFromDayStart * 5);
    }
    
    public ArrayList<Patient> convertToPatientsFromDBFFile(List<String[]> dbfPatients){
        ArrayList<Patient> result = new ArrayList<>();
        int message = 0;
        int count = 0;
        int size = dbfPatients.size();
        String date = null;
        String year;
        String month;
        String d;
        Iterator<String[]> dbfPatientsIt = dbfPatients.iterator();
        boolean isSelectedKey = false;
        while(dbfPatientsIt.hasNext()){
            count ++;
            String[] dbfPatientRow = dbfPatientsIt.next();
            if (count>0){
                Patient patient = new Patient();
                for (PatientField pf: PatientField.values()){
                    switch (pf){
                        case KEY:
                            patient.setKey(Integer.parseInt(dbfPatientRow[pf.ordinal()]));
                            if (patient.getKey() == 10791) isSelectedKey = true;
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
                            if (isSelectedKey){
                                int test = 0;
                                test++;
                            }
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
                result.add(patient);  
            }
        } 
        //count = patients.size();
        return result;
    }
    
}
