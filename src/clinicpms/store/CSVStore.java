/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException; 
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Comparator;

/**
 *
 * @author colin
 */
public class CSVStore extends Store {
    public static final Path DATABASE_FOLDER = Paths.get(
     "C:\\Users\\colin\\OneDrive\\Documents\\Databases\\csv\\");
    public static final String APPOINTMENTS_FILE = "appointment.csv";
    public static final String PATIENTS_FILE = "patient.csv";
    public static final String NON_EXISTING_PATIENT_FILE = "nonExistingPatient.csv";
    public static final String ORPHANED_APPOINTMENTS_FILE = "orphanedAppointments.csv";
    //public static final String DBF_APPOINTMENTS_FILE = "dbfAppointmentCSV.csv";
    public static final String DBF_APPOINTMENTS_FILE = "$1Appointment.csv";
    public static final String DBF_PATIENTS_FILE = "dbfPatientCSV.csv";
    private static CSVReader csvDbfAppointmentReader = null;
    private static CSVReader csvDbfPatientReader = null;
    private static CSVStore instance;
    private static CSVReader csvAppointmentReader = null;
    private static CSVReader csvPatientReader = null;
    private static CSVWriter csvAppointmentWriter = null;
    private static CSVWriter csvPatientWriter = null;
    private static Path appointmentsPath = DATABASE_FOLDER.resolve(APPOINTMENTS_FILE);
    private static Path patientsPath = DATABASE_FOLDER.resolve(PATIENTS_FILE);
    private static Path nonExistingPatientPath = DATABASE_FOLDER.resolve(NON_EXISTING_PATIENT_FILE);
    private static Path orphanedAppointmentsPath = DATABASE_FOLDER.resolve(ORPHANED_APPOINTMENTS_FILE);
    private static ArrayList<Integer> nonExistingPatients = null;
    
    
    
    private enum AppointmentField {KEY,
                                   PATIENT,
                                   START,
                                   DURATION,
                                   NOTES}
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
                                A_144}
    private enum Months {   Jan,
                            Feb,
                            Mar,
                            Apr,
                            May,
                            Jun,
                            Jul,
                            Aug,
                            Sep,
                            Oct,
                            Nov,
                            Dec
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
    
    
    public static final DateTimeFormatter ddMMyyyy24Format = DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm");
    public static final DateTimeFormatter ddMMyyyy12Format = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    public static final DateTimeFormatter ddMMyyyyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter MMMyyFormat = DateTimeFormatter.ofPattern("MMM-yy");
    private LocalDate day = null;
    private static ArrayList<Appointment> appointments = new ArrayList<>();
    private static ArrayList<Patient> patients = new ArrayList<>();

    public static CSVStore getInstance() throws StoreException{
        CSVStore result = null;
        if (instance == null) result = new CSVStore();
        else result = instance;
        return result;
    }
    
    public static void patientfileConverter()throws StoreException{
        Path patientsPath = DATABASE_FOLDER.resolve(DBF_PATIENTS_FILE);
        
        try{
            BufferedReader patientReader = Files.newBufferedReader(patientsPath,StandardCharsets.ISO_8859_1);
            CSVReader csvDBFPatientsReader = new CSVReader(patientReader);
            List<String[]> dbfPatients = csvDBFPatientsReader.readAll();
            convertToPatientsFromDBFFile(dbfPatients);
            //create csv file from patient collection
            convertPatientsToCSV(patients);
        }
        catch (java.nio.charset.MalformedInputException e){
            String message = "MalformedInputException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of appointmentReader or patientReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of appointmentReader or patientReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            
        }
    }
    
    public static void convertToPatientsFromDBFFile(List<String[]> dbfPatients){
        int message = 0;
        int count = 0;
        int size = dbfPatients.size();
        String date = null;
        String year;
        String month;
        String d;
        patients = new ArrayList<>();
        Iterator<String[]> dbfPatientsIt = dbfPatients.iterator();
        while(dbfPatientsIt.hasNext()){
            count ++;
            String[] dbfPatientRow = dbfPatientsIt.next();
            Patient patient = new Patient();
            for (PatientField pf: PatientField.values()){
                switch (pf){
                    case KEY -> patient.setKey(Integer.parseInt(dbfPatientRow[pf.ordinal()]));
                    case TITLE ->   {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.getName().setTitle(dbfPatientRow[pf.ordinal()]);   
                        }
                        else patient.getName().setTitle("");
                    }
                    case FORENAMES ->   {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.getName().setForenames(dbfPatientRow[pf.ordinal()]);
                        }
                        else patient.getName().setForenames("");
                    }
                    case SURNAME -> { 
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.getName().setSurname(dbfPatientRow[pf.ordinal()]);
                        }
                        else patient.getName().setSurname("");
                    }
                    case LINE1 ->   {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.getAddress().setLine1(dbfPatientRow[pf.ordinal()]);
                        }
                        else patient.getAddress().setLine1("");
                    }
                    case LINE2 ->   {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.getAddress().setLine2(dbfPatientRow[pf.ordinal()]);
                        }
                        else patient.getAddress().setLine2("");
                    }
                    case TOWN ->    {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.getAddress().setTown(dbfPatientRow[pf.ordinal()]);
                        }
                        else patient.getAddress().setTown("");
                    }
                    case COUNTY ->  {
                        patient.getAddress().setCounty(dbfPatientRow[pf.ordinal()]);
                    }
                    case POSTCODE -> {
                        patient.getAddress().setPostcode(dbfPatientRow[pf.ordinal()]);
                    }
                    case PHONE1 -> {
                        patient.setPhone1(dbfPatientRow[pf.ordinal()]);
                    }
                    case PHONE2 -> {
                        patient.setPhone2(dbfPatientRow[pf.ordinal()]);
                    }
                    case GENDER ->  {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.setGender(dbfPatientRow[pf.ordinal()]);
                        }
                        else patient.setGender("");
                    }
                    case DOB ->     {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.setDOB(LocalDate.parse(dbfPatientRow[pf.ordinal()],ddMMyyyyFormat));
                        }
                        else patient.setDOB(null);
                    }
                    case IS_GUARDIAN_A_PATIENT ->   {
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            patient.setIsGuardianAPatient(Boolean.valueOf(dbfPatientRow[pf.ordinal()]));
                        }
                        else patient.setIsGuardianAPatient(Boolean.valueOf(false));
                    }
                    case DENTAL_RECALL_FREQUENCY ->     {
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
                    }                    
                    case DENTAL_RECALL_DATE -> { 
                        boolean isInvalidMonth = false;
                        String value = "";
                        String[] values;
                        int mm = 0;
                        int yyyy = 0;
                        LocalDate recallDate = null;
                        value = dbfPatientRow[pf.ordinal()];
                        if (!value.isEmpty()){
                            value = value.strip();
                            values = value.split("-");
                            if (values.length > 0){
                                switch (values[0]){
                                    case "Jan" -> mm = 1;
                                    case "Feb" -> mm = 2;
                                    case "Mar" -> mm = 3;
                                    case "Apr" -> mm = 4;
                                    case "May" -> mm = 5;
                                    case "Jun" -> mm = 6;
                                    case "Jul" -> mm = 7;
                                    case "Aug" -> mm = 8;
                                    case "Sep" -> mm = 9;
                                    case "Oct" -> mm = 10;
                                    case "Nov" -> mm = 11;
                                    case "Dec" -> mm = 12;
                                    default -> isInvalidMonth = true;
                                }
                            }
                            if (!isInvalidMonth){
                                if (values[1].substring(0,1).equals("9")){
                                    yyyy = 1900 + Integer.parseInt(values[1]); 
                                    break;
                                }
                                else{
                                    yyyy = 2000 + Integer.parseInt(values[1]);
                                }
                                recallDate = LocalDate.of(yyyy, mm, 1);
                                patient.getRecall().setDentalDate(recallDate);
                            }
                            else patient.getRecall().setDentalDate(null);
                        }
                    }
                    case NOTES ->   {   
                        String notes = "";
                        if (!dbfPatientRow[pf.ordinal()].isEmpty()){
                            notes = notes + dbfPatientRow[pf.ordinal()];
                        }
    /**
     * Following code necessary because of occurrence of "\" characters (escape character)
     * could escape an end of field comma, which would reduce field count by one.
     * This then caused an array index bound exception to occur because logic relied
     * on a fixed number of fields per row
     */
                        /*
                        if (count == 4000){
                            message = pf.ordinal()+2;
                            count = dbfPatientRow.length;
                            if (dbfPatientRow[pf.ordinal()+2].isEmpty()){  
                                message = pf.ordinal()+2;
                            } 
                            if (!dbfPatientRow[pf.ordinal()+2].isEmpty()){  
                                message = count;
                            }
                        }
                        */
                        if (!dbfPatientRow[pf.ordinal()+1].isEmpty()){
                            if (!notes.isEmpty()){
                                notes = notes + "\n";
                            }
                            notes = notes + dbfPatientRow[pf.ordinal()+1];
                        }
                        if (!dbfPatientRow[pf.ordinal()+2].isEmpty()){ 
                            if (!notes.isEmpty()){
                                notes = notes + "\n";
                            }
                            notes = notes + dbfPatientRow[pf.ordinal()+2]; 
                        }
                        patient.setNotes(notes);
                    }
                    case GUARDIAN -> patient.setGuardian(null); 
                }
            }
            patients.add(patient);           
        }   
    }
    
    private static void convertPatientsToCSV(ArrayList<Patient> patients)throws StoreException{
        String key = "";
        String title = "";
        String forenames = "";
        String surname = "";
        String line1 = "";
        String line2 = "";
        String town = "";
        String county = "";
        String postcode = "";
        String phone1 = "";
        String phone2 = "";
        String gender = "";
        String dob = "";
        String isGuardianAPatient = "";
        String recallFrequency = "";
        String recallDate = "";
        String notes = "";
        String guardian = "";
        
        List<String[]> csvPatients = new ArrayList<String[]>();
        String[] csvPatientRow;
        Iterator<Patient> patientInterator = patients.iterator();
        while(patientInterator.hasNext()){
            Patient patient = patientInterator.next();
            for(PatientField pf: PatientField.values()){
                switch (pf){
                    case KEY -> key = String.valueOf(patient.getKey());
                    case TITLE ->   {   
                        if(!patient.getName().getTitle().isEmpty()){
                           title = patient.getName().getTitle(); 
                        }
                        else title = "";
                    }
                    case FORENAMES ->   {   
                        if(!patient.getName().getForenames().isEmpty()){
                           forenames = patient.getName().getForenames(); 
                        }
                        else forenames = "";
                    }
                    case SURNAME ->   {   
                        if(!patient.getName().getSurname().isEmpty()){
                           surname = patient.getName().getSurname(); 
                        }
                        else surname = "";
                    }
                    case LINE1 ->   {   
                        if(!patient.getAddress().getLine1().isEmpty()){
                           line1 = patient.getAddress().getLine1(); 
                        }
                        else line1 = "";
                    }
                    case LINE2 ->   {   
                        if(!patient.getAddress().getLine2().isEmpty()){
                           line2 = patient.getAddress().getLine2(); 
                        }
                        else line2 = "";
                    }
                    case TOWN ->   {   
                        if(!patient.getAddress().getTown().isEmpty()){
                           town = patient.getAddress().getTown(); 
                        }
                        else town = "";
                    }
                    case COUNTY ->   {   
                        if(!patient.getAddress().getCounty().isEmpty()){
                           county = patient.getAddress().getCounty(); 
                        }
                        else county = "";
                    }
                    case POSTCODE ->   {   
                        if(!patient.getAddress().getPostcode().isEmpty()){
                           postcode = patient.getAddress().getPostcode(); 
                        }
                        else postcode = "";
                    }
                    case PHONE1 ->   {   
                        if(!patient.getPhone1().isEmpty()){
                           phone1 = patient.getPhone1(); 
                        }
                        else phone1 = "";
                    }
                    case PHONE2 ->   {   
                        if(!patient.getPhone2().isEmpty()){
                           phone2 = patient.getPhone2(); 
                        }
                        else phone2 = "";
                    }
                    case GENDER ->   {   
                        if(!patient.getGender().isEmpty()){
                           gender = patient.getGender(); 
                        }
                        else gender = "";
                    }
                    case DOB ->   {   
                        if(patient.getDOB()!=null){
                           dob = patient.getDOB().format(ddMMyyyyFormat); 
                        }
                        else dob = "";
                    }
                    case IS_GUARDIAN_A_PATIENT ->   {
                        isGuardianAPatient = String.valueOf(patient.getIsGuardianAPatient()); 
                    }
                    case DENTAL_RECALL_FREQUENCY ->    {
                        recallFrequency = String.valueOf(patient.getRecall().getDentalFrequency());
                    }
                    case DENTAL_RECALL_DATE ->  {
                        if (patient.getRecall().getDentalDate()!=null){
                            recallDate = patient.getRecall().getDentalDate().format(ddMMyyyyFormat);  
                        }
                        else recallDate = "";
                    }
                    case NOTES -> {
                        if(patient.getNotes() != null){
                            notes = patient.getNotes();
                        }
                        else notes = "";
                    }
                    case GUARDIAN ->    {
                        if (patient.getGuardian() != null){
                            guardian = String.valueOf(patient.getGuardian());
                        }
                        else guardian = "";
                    }  
                }
            }
            csvPatientRow = new String[] {
                key, 
                title, 
                forenames, 
                surname,
                line1, 
                line2, 
                town, 
                county, 
                postcode,
                phone1,
                phone2,
                gender,
                dob,
                isGuardianAPatient,
                recallFrequency,
                recallDate,
                notes,
                guardian    
            };
            csvPatients.add(csvPatientRow);
        } 
        CSVWriter csvWriter = getCSVPatientWriter();
        csvWriter.writeAll(csvPatients);
        csvWriter.flushQuietly();
    }
    
    
    
    private boolean isDigit(String s){
        boolean isDigit = true;
        s = s.strip();
        char[] c = s.toCharArray(); 
        for (int index = 0; index < c.length; index++){
            if (!Character.isDigit(c[index])){
                isDigit = false;
                break;
            }
        }
        return isDigit;
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
                throw new StoreException(s, ExceptionType.IO_EXCEPTION);
            }
        }
        return result;
    }
    
    
    /**
     * 
     * @param appointments
     * @throws StoreException 
     */
    public static void convertAppointmentsToCSV(ArrayList<Appointment> appointments)throws StoreException{
        int index = 0;
        String key;
        String patient;
        String start;
        String duration;
        String notes;
        ArrayList<String[]> csvAppointments = new ArrayList<String[]>();
        String[] csvAppointmentRow;
        Iterator<Appointment> appointmentInterator = appointments.iterator();
        while(appointmentInterator.hasNext()){
            Appointment appointment = appointmentInterator.next();
            appointment.setKey(++index);
            if (appointment.getKey().equals(37702)){
                key = String.valueOf(appointment.getKey());
            }
                
            key = String.valueOf(appointment.getKey());
            patient = String.valueOf(appointment.getPatient().getKey());
            start = appointment.getStart().format(ddMMyyyy24Format);
            duration = String.valueOf(appointment.getDuration().toMinutes());
            notes = appointment.getNotes();
            csvAppointmentRow = new String[]{key, patient,start,duration,notes};
            csvAppointments.add(csvAppointmentRow);
            //getCSVAppointmentWriter().writeNext(csvAppointmentRow);

        }
        //List<String[]> csvA = csvAppointments;
        CSVWriter csvWriter = getCSVAppointmentWriter();
        csvWriter.writeAll(csvAppointments);
        csvWriter.flushQuietly();
        
    }
    
    /**
     * -- render dbf date format -> dd/mm/yyyy
     * -- make appointment records from dbf rows and collect in static global ArrayList<Appointment>
     * ----> key, start (LocalDateTime), duration (minutes), notes
     * Uses following methods
     * -- makeAppointmentsFromDBFRow
     * 
     * @param dbfAppointments
     * @throws StoreException 
     */
    public static void convertToAppointmentsFromDBFFile(List<String[]> dbfAppointments)throws StoreException{
        String date = null;
        String year;
        String month;
        String d;
        Iterator<String[]> dbfAppointmentsIt = dbfAppointments.iterator();
        while(dbfAppointmentsIt.hasNext()){
            String[] dbfAppointmentRow = dbfAppointmentsIt.next();
            date = dbfAppointmentRow[DenAppField.DATE.ordinal()];
            date = switch (date.length()){
                case 3 -> "000" + date;
                case 4 -> "00" + date;
                case 5 -> "0" + date;
                default -> date;
            };
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
    private static void makeAppointmentsFromDBFRow(String[] row, String date, int rowIndex) throws StoreException{
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

    /**
     * Constructs two new CSVReader objects, one to read from appointments.csv 
     * file and one to read from patients.csv file.
     * @throws StoreException
     */
    public CSVStore() throws StoreException{ 
        
    }
    
    /**
     * Creates a unique key for the received Patient object and adds the 
     * serialised Patient record to the patients csv file
     * @param p Patient with a null key value
     * @throws StoreException if the received \Patient object does not have a 
     * null key
     */
    @Override
    public Patient create(Patient p)   throws StoreException{
        Patient result = null;
        List<String[]> readPatientsStringArrayList;
        if (p.getKey() == null){
            readPatientsStringArrayList = readPatientsAsStringArrayList();
            Integer nextPatientKey = getNextHighestKeyFromRecords(readPatientsStringArrayList);
            p.setKey(nextPatientKey);
            String[] serialisedPatient = serialise(p);
            this.csvPatientWriter.writeNext(serialisedPatient);
            return read(p);
        }
        else{
            throw new StoreException(
                    "Received patient key not null although"
                       + "expected null by CSVStore.create(Patient p) method",
                    ExceptionType.NULL_KEY_EXPECTED_EXCEPTION);
        }
    }
    
    /**
     * Creates a unique key for the received Appointment object and adds the 
     * serialised Appointment to the patients csv file
     * @param a Appointment with a null key value
     * @throws StoreException if the received Appointment object does not have a 
     * null key
     * @return Appointment, reads back from store the newly created appointment
     */
    @Override
    public Appointment create(Appointment a)   throws StoreException{
        List<String[]> readAppointmentsStringArrayList;
        if (a.getKey() == null){
            readAppointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Integer nextAppointmentKey = getNextHighestKeyFromRecords(readAppointmentsStringArrayList);
            a.setKey(nextAppointmentKey);
            String[] serialisedAppointment = serialise(a);
            this.csvAppointmentWriter.writeNext(serialisedAppointment);
            return read(a);
        }
        else{
            throw new StoreException(
                    "Received appointment key not null although"
                       + "expected null by CSVStore.create(Appointment a) method",
                    ExceptionType.NULL_KEY_EXPECTED_EXCEPTION);
        }
    }

    /**
     * Calculates the next highest (new) key from the records received
     * @param records List<String[]>
     * @return Integer representing the next available key for the record
     * collection
     */
    public Integer getNextHighestKeyFromRecords(List<String[]> records){
        Iterator<String[]> it = records.iterator();
        ArrayList<Integer> keys = new ArrayList<>();
        while (it.hasNext()){
            String[] s = it.next();
            keys.add(Integer.parseInt(s[PatientField.KEY.ordinal()]));
        }
        Collections.sort(keys);
        return keys.get(keys.size()-1) + 1;
    }
    
    /**
     * 
     * @param a Appointment object to serialise
     * @return String[] result of the serialisation
     */
    private String[] serialise(Appointment a){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String[] sA = new String[AppointmentField.values().length];
        for (AppointmentField af: AppointmentField.values()){
            switch(af){
                case KEY -> 
                    sA[AppointmentField.KEY.ordinal()] = a.getKey().toString();
                case PATIENT -> 
                    sA[AppointmentField.PATIENT.ordinal()] = a.getKey().toString();
                case START ->
                    sA[AppointmentField.START.ordinal()] = 
                            a.getStart().format(formatter);
                case DURATION ->
                    sA[AppointmentField.DURATION.ordinal()] = 
                            Long.toString(a.getDuration().toMinutes());
                case NOTES ->
                    sA[AppointmentField.NOTES.ordinal()] = a.getNotes();       
            }
        }
        return sA;
    }
    
    private String[] serialise(Patient p){
        DateTimeFormatter dobFormat = DateTimeFormatter.ofPattern("dd/mm/yyyy");
        DateTimeFormatter recallFormat = DateTimeFormatter.ofPattern("MM/yy");
        String[] sP = new String[PatientField.values().length];
        
        for (PatientField pf: PatientField.values()){
            switch(pf){
                case KEY -> sP[PatientField.KEY.ordinal()] = 
                        p.getKey().toString();
                case TITLE -> sP[PatientField.TITLE.ordinal()] = 
                        p.getName().getTitle();
                case FORENAMES -> sP[PatientField.FORENAMES.ordinal()] = 
                        p.getName().getForenames();
                case SURNAME -> sP[PatientField.SURNAME.ordinal()] = 
                        p.getName().getSurname();
                case LINE1 -> sP[PatientField.LINE1.ordinal()] = 
                        p.getAddress().getLine1();
                case LINE2 -> sP[PatientField.LINE2.ordinal()] = 
                        p.getAddress().getLine2();
                case TOWN -> sP[PatientField.TOWN.ordinal()] = 
                        p.getAddress().getTown();
                case COUNTY -> sP[PatientField.COUNTY.ordinal()] = 
                        p.getAddress().getCounty();
                case POSTCODE -> sP[PatientField.POSTCODE.ordinal()] = 
                        p.getAddress().getPostcode();
                case DENTAL_RECALL_DATE -> sP[PatientField.DENTAL_RECALL_DATE.ordinal()] = 
                        p.getRecall().getDentalDate().format(recallFormat);
                case DENTAL_RECALL_FREQUENCY -> sP[PatientField.DENTAL_RECALL_FREQUENCY.ordinal()] = 
                        p.getRecall().getDentalFrequency().toString();
                case DOB -> sP[PatientField.DOB.ordinal()] = 
                        p.getDOB().format(dobFormat);
                case GENDER -> sP[PatientField.GENDER.ordinal()] = 
                        p.getGender();
                case IS_GUARDIAN_A_PATIENT -> sP[PatientField.IS_GUARDIAN_A_PATIENT.ordinal()] = 
                        Boolean.toString(p.getIsGuardianAPatient());
                case GUARDIAN -> sP[PatientField.GUARDIAN.ordinal()] = 
                        String.valueOf(p.getGuardian());
                case NOTES -> sP[PatientField.NOTES.ordinal()] = 
                        p.getGuardian().toString();
            }
        }
        return sP;
    }
    
    private List<String[]> readPatientsAsStringArrayList() throws StoreException{
        try{
            CSVReader csvReader = getCSVPatientReader();
            List<String[]> data = csvReader.readAll();
            csvReader.close();
            return data;
        }
        catch (IOException e){
            throw new StoreException(e.getMessage(), ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            throw new StoreException(e.getMessage(), ExceptionType.CSV_EXCEPTION);
        }  
    }
    public ArrayList<Appointment> readAppointmentsFrom(LocalDate day) throws StoreException{
        return null;
    }
    
    private List<String[]> readAppointmentsAsStringArrayList() throws StoreException{
        try{
            CSVReader csvReader = getCSVAppointmentReader();
            List<String[]> data = csvReader.readAll();
            csvReader.close();
            return data;
        }
        catch (IOException e){
            throw new StoreException(e.getMessage(), ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            throw new StoreException(e.getMessage(), ExceptionType.CSV_EXCEPTION);
        }  
    }
    
    public ArrayList<Patient> readPatients() throws StoreException{
        ArrayList<Patient> patients = new ArrayList<>();
        List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
        Iterator<String[]> it = patientsStringArrayList.iterator();
        while (it.hasNext()){
            String[] patientStringArray = it.next();
            Patient p = makePatientFromStringArray(patientStringArray);
            patients.add(p);
        }
        sortPatientsByName(patients);
        return patients;
    }
    
    /**
     * checkAppointeeExists1 creates a set of unique appointee IDs and then
     * uses these keys to check a patient record exists for each one
     */
    public void checkAppointeeExists1()throws StoreException{
        int patientCount = 0;
        int orphanCount = 0;
        Integer key = null;
        this.nonExistingPatients = new ArrayList<Integer>();
        HashSet<Integer> patientSet = new HashSet<>();
        List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
        Iterator<String[]> it = appointmentsStringArrayList.iterator();
        while (it.hasNext()){
            String[] appointmentStringArray = it.next();
            key = Integer.parseInt(appointmentStringArray[AppointmentField.PATIENT.ordinal()]); 
            patientSet.add(key);
        }
        Iterator<Integer> patientSetIt = patientSet.iterator();
        while (patientSetIt.hasNext()){
            key = patientSetIt.next();
            try{
                read(new Patient(key));
                patientCount++;
            }
            catch (StoreException ex){
                if (ex.getErrorType().equals(ExceptionType.KEY_NOT_FOUND_EXCEPTION)){
                    this.nonExistingPatients.add(key);
                }
            }
        }
        
        deleteOrphanedAppointmentsFromAppointments(nonExistingPatients);
    }
    private void deleteOrphanedAppointmentsFromAppointments(ArrayList<Integer> nonPatientKeys)throws StoreException{
        ArrayList<Integer> orphanedAppointmentKeys = new ArrayList<Integer>();
        ArrayList<Integer> patientlessAppointmentKeys = new ArrayList<>();
        ArrayList<Appointment> appointments = null;
        sortNonExistingPatientsByKey(nonPatientKeys);
        try{
            appointments = readAppointments1();
        }
        catch(StoreException ex){
            
        }
        Iterator<Integer> nonExistingPatientKeysIt = nonPatientKeys.iterator();
        List<String[]> orphanedAppointmentPatientString = new ArrayList<>();
        while(nonExistingPatientKeysIt.hasNext()){
            Integer nonExistingPatientKey = nonExistingPatientKeysIt.next();
            Iterator<Appointment> appointmentsIt = appointments.iterator();
            while(appointmentsIt.hasNext()){
                Appointment appointment = appointmentsIt.next();
                if (appointment.getPatient().getKey().equals(nonExistingPatientKey)){
                    orphanedAppointmentKeys.add(appointment.getKey());
                    String s1 = String.valueOf(appointment.getPatient().getKey());
                    String s2 = String.valueOf(appointment.getKey());
                    orphanedAppointmentPatientString.add(
                            new String[]{s1,s2});
                }
            }
        }
        
        CSVWriter csvWriter = getCSVOrphanedAppointmentWriter();
        csvWriter.writeAll(orphanedAppointmentPatientString);
        csvWriter.flushQuietly();
        
        List<String[]> nonExistingPatientsString = new ArrayList<>();
        for (int index = 0; index < nonPatientKeys.size(); index++){
            String[]  s = {String.valueOf(nonPatientKeys.get(index))};
            nonExistingPatientsString.add(new String[]{String.valueOf(nonPatientKeys.get(index))});
        }
        csvWriter = getCSVNonExistingPatientWriter();
        csvWriter.writeAll((List<String[]>)nonExistingPatientsString);
        csvWriter.flushQuietly();
        deleteOrphanedAppointment(orphanedAppointmentKeys);
    }
    private void deleteOrphanedAppointment(ArrayList<Integer> orphanedAppointmentKeys)throws StoreException{
       
        boolean isAppointmentRecordFound = false;
        String[] row = null;
        int counter = 0;
        String message = null;
        List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
        Iterator<Integer> orphanedAppointmentKeysIt = orphanedAppointmentKeys.iterator();
        while (orphanedAppointmentKeysIt.hasNext()){
            Integer orphanedAppointmentKey = orphanedAppointmentKeysIt.next();
            Appointment orphanedAppointment = new Appointment(orphanedAppointmentKey);
            
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                row = it.next();
                if (row[AppointmentField.KEY.ordinal()].
                            equals(String.valueOf(orphanedAppointment.getKey()))){
                    isAppointmentRecordFound = true;
                    break;
                }
            }
            appointmentsStringArrayList.remove(index);
        }
            
            //File f =  new File(appointmentsPath.toString());
            //f.delete();
            
        CSVWriter csvWriter = getCSVAppointmentWriter();
        Iterator<String[]> appointmentsStringArrayListIt = appointmentsStringArrayList.iterator();
        while(appointmentsStringArrayListIt.hasNext()){
            String[] s = appointmentsStringArrayListIt.next();
            csvWriter.writeNext(s);  
            counter++;
        }
        csvWriter.flushQuietly();
    }
    public void checkAppointeeExists()throws StoreException{
        Integer key = null;
        this.nonExistingPatients = new ArrayList<Integer>();
        List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
        Iterator<String[]> it = appointmentsStringArrayList.iterator();
        while (it.hasNext()){
            String[] appointmentStringArray = it.next();
            key = Integer.parseInt(appointmentStringArray[AppointmentField.PATIENT.ordinal()]); 
            Patient patient = new Patient(key);
            try{
                read(patient);
            }
            catch (StoreException ex){
                if (ex.getErrorType().equals(ExceptionType.KEY_NOT_FOUND_EXCEPTION)){
                    this.nonExistingPatients.add(key);
                }
            }
        }
    }
    /**
     * 
     * @return ArrayList<Appointment> time ordered collection of appointments
     * @throws StoreException
     */
    public ArrayList<Appointment> readAppointments() throws StoreException{
        ArrayList<Appointment> appointments = new ArrayList<>();
        List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
        Iterator<String[]> it = appointmentsStringArrayList.iterator();
        while (it.hasNext()){
            String[] appointmentStringArray = it.next();
            Appointment a = makeAppointmentFromStringArray(appointmentStringArray);
            appointments.add(a);
        }
        sortAppointmentsByDay(appointments);
        return appointments;
    }
    
    /**
     * 
     * object
     * @param appointmentRecord
     * @return Appointment, which represents a String[] 
     * @throws StoreException 
     */
    private Appointment makeAppointmentFromStringArray1(String[] appointmentRecord) throws StoreException{
        DateTimeFormatter startFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy H:m");
        Appointment a = new Appointment();
        for (AppointmentField af: AppointmentField.values()){
            if (af.equals(AppointmentField.PATIENT)){
                Patient patient = new Patient(Integer.parseInt(
                        appointmentRecord[AppointmentField.PATIENT.ordinal()]));
                a.setPatient(patient);
            }
            else{
                switch (af){
                    case KEY -> a.setKey(Integer.parseInt(
                                        appointmentRecord[AppointmentField.KEY.ordinal()]));
                    /**
                     * assumes data in original dbf file has been converted appropriately
                     * to be consistent with two fields in the converted csv result
                     * namely -> original date, start time and end time (3 fields from many fields)
                     * converted to 2 fields -> correctly formatted (dd/mm/yyyy hh:mm) date and 
                     * number of minutes for the duration
                     */
                    case START -> a.setStart(LocalDateTime.parse(
                            appointmentRecord[AppointmentField.START.ordinal()],startFormat));
                    case DURATION -> a.setDuration(Duration.ofMinutes(Long.parseLong(
                                appointmentRecord[AppointmentField.DURATION.ordinal()])));
                    case NOTES -> a.setNotes(
                            appointmentRecord[AppointmentField.NOTES.ordinal()]);
                }
            }
        }
        return a;
    }
    
    /**
     * readAppointments1() special version of readAppointments which invokes a special version
     * of makeAppointmentFromStringArray(), namely makeAppointmentFromStringArray1()
     * for purposes of the "key integrity check"
     * @return ArrayList<Appointment> time ordered collection of appointments
     * @throws StoreException
     */
    public ArrayList<Appointment> readAppointments1() throws StoreException{
        ArrayList<Appointment> appointments = new ArrayList<>();
        List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
        Iterator<String[]> it = appointmentsStringArrayList.iterator();
        while (it.hasNext()){
            String[] appointmentStringArray = it.next();
            Appointment a = makeAppointmentFromStringArray1(appointmentStringArray);
            appointments.add(a);
        }
        sortAppointmentsByPatientKey(appointments);
        return appointments;
    }
    
    /**
     * 
     * @param day
     * @return ArrayList of Appointment objects time ordered list of appointments for the
     * specified day
     * @throws StoreException
     */
    @Override
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException{
        ArrayList<Appointment> dayAppointments = new ArrayList<>();
        ArrayList<Appointment> appointments = readAppointments();
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()){
            Appointment a = it.next();
            if (a.getStart().toLocalDate().equals(day)){
                dayAppointments.add(a);
            } 
        }
        return dayAppointments;
    }
    
    /**
     * 
     * @param p Patient object
     * @param t Category enumerator constant  
     * @return ArrayList of Appointment objects time ordered for this patient
     * @throws StoreException
     */
    @Override
    public ArrayList<Appointment> readAppointments(Patient p, Appointment.Category t) throws StoreException{
        ArrayList<Appointment> patientAppointments = new ArrayList<>();
        ArrayList<Appointment> appointments = readAppointments();
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()){
            Appointment a = it.next();
            
            if (a.getPatient().getKey().equals(p.getKey())){
                switch (a.getCategory()){
                    case DENTAL -> {
                        if (t.equals(Appointment.Category.DENTAL)){
                            patientAppointments.add(a);
                        }
                    }
                    case HYGIENE -> {
                        if (t.equals(Appointment.Category.HYGIENE)){
                            patientAppointments.add(a);
                        }
                    }
                    case ALL -> patientAppointments.add(a);
                }
            }
        }
        return patientAppointments;
    }
    
    /**
     * 
     * object
     * @param appointmentRecord
     * @return Appointment, which represents a String[] 
     * @throws StoreException 
     */
    private Appointment makeAppointmentFromStringArray(String[] appointmentRecord) throws StoreException{
        Appointment a = new Appointment();
        for (AppointmentField af: AppointmentField.values()){
            if (af.equals(AppointmentField.PATIENT)){
                Patient patient = read(new Patient(Integer.parseInt(
                        appointmentRecord[AppointmentField.PATIENT.ordinal()])));
                a.setPatient(patient);
            }
            else{
                switch (af){
                    case KEY -> a.setKey(Integer.parseInt(
                                        appointmentRecord[AppointmentField.KEY.ordinal()]));
                    /**
                     * assumes data in original dbf file has been converted appropriately
                     * to be consistent with two fields in the converted csv result
                     * namely -> original date, start time and end time (3 fields from many fields)
                     * converted to 2 fields -> correctly formatted (dd/mm/yyyy hh:mm) date and 
                     * number of minutes for the duration
                     */
                    case START -> a.setStart(LocalDateTime.parse(
                            appointmentRecord[AppointmentField.START.ordinal()],ddMMyyyy24Format));
                    case DURATION -> a.setDuration(Duration.ofMinutes(Long.parseLong(
                                appointmentRecord[AppointmentField.DURATION.ordinal()])));
                    case NOTES -> a.setNotes(
                            appointmentRecord[AppointmentField.NOTES.ordinal()]);
                }
            }
        }
        return a;
    }
    
    /**
     * Converts a row read from the patient csv file to a Patient. The method 
     * can be called recursively if this patient has a guardian who is also a 
     * patient. The recursion halts when  the patient field 
     * Is_Guardian_A_Patient is false. Successful processing relies therefor
     * that the Is_Guardian_A_Patient patient field is processed before the 
     * Guardian field. Scope for a business rule here -> this field can only be 
     * true if patient age is less than 18; which would make it extremely 
     * unlikely (impossible?)for there to be than a single recursive call on the 
     * method
     * @param patientRecord, which represents a String[]
     * @return Patient
     * @throws StoreException 
     */
    private Patient makePatientFromStringArray(String[] patientRecord) throws StoreException{
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Patient p = new Patient();
        for (PatientField pf: PatientField.values()){
            switch (pf){
                case KEY -> p.setKey(Integer.parseInt(
                                    patientRecord[PatientField.KEY.ordinal()]));
                case TITLE -> p.getName().setTitle(
                                    patientRecord[PatientField.TITLE.ordinal()]);
                case FORENAMES -> p.getName().setForenames(
                            patientRecord[PatientField.FORENAMES.ordinal()]);
                case SURNAME -> p.getName().setSurname(
                            patientRecord[PatientField.SURNAME.ordinal()]);
                case LINE1 -> p.getAddress().setLine1(
                            patientRecord[PatientField.LINE1.ordinal()]);
                case LINE2 -> p.getAddress().setLine2(
                            patientRecord[PatientField.LINE2.ordinal()]);
                case TOWN -> p.getAddress().setTown(
                            patientRecord[PatientField.TOWN.ordinal()]);
                case COUNTY -> p.getAddress().setCounty(
                            patientRecord[PatientField.COUNTY.ordinal()]);
                case POSTCODE -> p.getAddress().setPostcode(
                            patientRecord[PatientField.POSTCODE.ordinal()]);
                case DENTAL_RECALL_DATE -> {
                    String value = patientRecord[PatientField.DENTAL_RECALL_DATE.ordinal()];
                    int mm = 0;
                    int yyyy = 0;
                    boolean isInvalidMonth = false;
                    if (!value.isEmpty()){
                        String values[] = value.split("-");
                        if (values.length > 0){
                            switch (values[0]){
                                case "Jan" -> mm = 1;
                                case "Feb" -> mm = 2;
                                case "Mar" -> mm = 3;
                                case "Apr" -> mm = 4;
                                case "May" -> mm = 5;
                                case "Jun" -> mm = 6;
                                case "Jul" -> mm = 7;
                                case "Aug" -> mm = 8;
                                case "Sep" -> mm = 9;
                                case "Oct" -> mm = 10;
                                case "Nov" -> mm = 11;
                                case "Dec" -> mm = 12;
                                default -> isInvalidMonth = true;
                            }
                        }
                        if (!isInvalidMonth){
                            if (values[1].substring(0,1).equals("9")){
                                yyyy = 1900 + Integer.parseInt(values[1]); 
                                break;
                            }
                            else{
                                yyyy = 2000 + Integer.parseInt(values[1]);
                            }
                            p.getRecall().setDentalDate(LocalDate.of(yyyy,mm,1));
                        }
                    }      
                }
                case DENTAL_RECALL_FREQUENCY -> p.getRecall().setDentalFrequency(Integer.parseInt(
                    patientRecord[PatientField.DENTAL_RECALL_FREQUENCY.ordinal()]));
                case GENDER -> p.setGender(
                        patientRecord[PatientField.GENDER.ordinal()]);
                case PHONE1 -> p.setPhone1(
                        patientRecord[PatientField.PHONE1.ordinal()]);
                case PHONE2 -> p.setPhone2(
                        patientRecord[PatientField.PHONE2.ordinal()]);
                case DOB -> {
                    if (!patientRecord[PatientField.DOB.ordinal()].isEmpty()){
                        p.setDOB(LocalDate.parse(patientRecord[PatientField.DOB.ordinal()],ddMMyyyyFormat));
                    }
                    else p.setDOB(null);
                }
                case NOTES -> p.setNotes(
                        patientRecord[PatientField.NOTES.ordinal()]);
                case IS_GUARDIAN_A_PATIENT -> p.setIsGuardianAPatient(Boolean.valueOf(
                        patientRecord[PatientField.IS_GUARDIAN_A_PATIENT.ordinal()]));

            }
            if (pf.equals(PatientField.GUARDIAN)){
                if (p.getIsGuardianAPatient()){
                    Integer key = (
                            patientRecord[PatientField.KEY.ordinal()] == null) ? 
                            null:Integer.parseInt(
                                    patientRecord[PatientField.KEY.ordinal()]);
                    if (key!=null){
                        Patient guardian = new Patient(key);
                        p.setGuardian(read(guardian));
                    } 
                }
            }
        }
        return p;
    }

    /**
     * The key field is the only field of the Appointment parameter considered, 
     * and is used to read the corresponding appointment record from the database.
     * @param a represents an Appointment object
     * @return Appointment
     * @throws StoreException 
     */
    @Override
    public Appointment read(Appointment a) throws StoreException{ 
        boolean isRecordFound = false;
        Appointment appointment = null;
        Integer key = a.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.read(Appointment) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key <= 0){
            throw new StoreException("Zero or negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Appointment) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            while (it.hasNext()){
                String[] appointmentStringArray = it.next();
                if (appointmentStringArray[AppointmentField.KEY.ordinal()].
                                            equals(String.valueOf(a.getKey()))){
                    appointment = makeAppointmentFromStringArray(appointmentStringArray);
                    isRecordFound = true;
                    break;
                }
            } 
            if (!isRecordFound){
                String message = String.format("Appointment record with key = %d not found", a.getKey());
                throw new StoreException(message,ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
        }
        return appointment;
    }
    
    /**
     * The key field is the only field of the Patient parameter considered, 
     * and is used to read the corresponding appointment record from the database.
     * @param p represents a Patient object
     * @return Patient
     * @throws StoreException 
     */
    @Override
    public Patient read(Patient p)throws StoreException{
        boolean isRecordFound = false;
        Patient patient = null;
        Integer key = p.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.read(Patient) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key <= 0){
            throw new StoreException("Zero or egative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Patient) "
                                                + "method", 
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
            Iterator<String[]> it = patientsStringArrayList.iterator();
            while (it.hasNext()){
                String[] patientStringArray = it.next();
                if (patientStringArray[PatientField.KEY.ordinal()].
                        equals(String.valueOf(p.getKey()))){
                    patient = makePatientFromStringArray(patientStringArray);
                    isRecordFound = true;
                    break;
                }
            } 
            if (!isRecordFound){
                String message = String.format("Patient record with key = %d not found", p.getKey());
                throw new StoreException(message,ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
        }
        return patient;
    }
     
    @Override
    public Appointment update(Appointment a) throws StoreException{
        boolean isAppointmentRecordFound = false;
        Integer key = a.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.update(Appointment) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Appointment) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] appointmentStringArray = it.next();
                if (appointmentStringArray[AppointmentField.KEY.ordinal()].
                        equals(String.valueOf(a.getKey()))){
                    isAppointmentRecordFound = true;
                    break;
                }
            }
            if (!isAppointmentRecordFound){
                throw new StoreException("Specified appointment key in "
                            + "CSVStore.Update(Appointment) not found in darabase.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                appointmentsStringArrayList.remove(index);
                CSVWriter csvWriter = getCSVAppointmentWriter();
                csvWriter.writeAll(appointmentsStringArrayList);
                csvWriter.flushQuietly();
                String[] serialisedAppointment = serialise(a);
                csvWriter = getCSVAppointmentWriter();
                csvWriter.writeNext(serialisedAppointment);
                csvWriter.flushQuietly();
                return read(a);
            }
        } 
    }
    
    private static CSVReader getCSVAppointmentReader() throws StoreException{
        try{
            BufferedReader appointmentReader = Files.newBufferedReader(appointmentsPath,StandardCharsets.ISO_8859_1);
            csvAppointmentReader = new CSVReader(appointmentReader);
        }
        catch (java.nio.charset.MalformedInputException e){
            String message = "MalformedInputException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of appointmentReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered on initialisation "
                    + "CSVAppointmentReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        return csvAppointmentReader;
    }
    private static CSVReader getCSVPatientReader() throws StoreException{
        try{
            BufferedReader patientReader = Files.newBufferedReader(patientsPath,StandardCharsets.ISO_8859_1);
            csvPatientReader = new CSVReader(patientReader);
        }
        catch (java.nio.charset.MalformedInputException e){
            String message = "MalformedInputException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of patientReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered on initialisation "
                    + "CSVPatientReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }

        return csvPatientReader;
    }
    private static CSVWriter getCSVNonExistingPatientWriter()throws StoreException{
        try{
            File appointmentWriter = new File(nonExistingPatientPath.toString());
            FileWriter outputAppointmentsFile = new FileWriter(appointmentWriter);
            csvAppointmentWriter = new CSVWriter(outputAppointmentsFile);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered on initialisation "
                    + "CSVNonExistingPatientWriter File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }

        return csvAppointmentWriter;
    }
    private static CSVWriter getCSVOrphanedAppointmentWriter()throws StoreException{
        try{
            File appointmentWriter = new File(orphanedAppointmentsPath.toString());
            FileWriter outputAppointmentsFile = new FileWriter(appointmentWriter);
            csvAppointmentWriter = new CSVWriter(outputAppointmentsFile);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered on initialisation "
                    + "CSVOrphanedAppointmentWriter File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }

        return csvAppointmentWriter;
    }
    private static CSVWriter getCSVAppointmentWriter()throws StoreException{
        try{
            File appointmentWriter = new File(appointmentsPath.toString());
            FileWriter outputAppointmentsFile = new FileWriter(appointmentWriter);
            csvAppointmentWriter = new CSVWriter(outputAppointmentsFile);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered on initialisation "
                    + "CSVOrphanedAppointmentWriter File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }

        return csvAppointmentWriter;
    }
    private static CSVWriter getCSVPatientWriter()throws StoreException{
        if (csvPatientWriter == null){
            try{
                File patientWriter = new File(patientsPath.toString());
                FileWriter outputPatientsFile = new FileWriter(patientWriter);
                csvPatientWriter = new CSVWriter(outputPatientsFile);
            }
            catch (IOException e){
                String message = "IOException message -> " + e.getMessage() + "\n" +
                        "StoreException message -> Error encountered on initialisation "
                        + "CSVPatientWriter File object";
                throw new StoreException(message, ExceptionType.IO_EXCEPTION);
            }
        }
        return csvPatientWriter;
    }
    
    @Override
    public Patient update(Patient p) throws StoreException{
        boolean isPatientRecordFound = false;
        Integer key = p.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.update(Patient) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Patient) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
            Iterator<String[]> it = patientsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] patientStringArray = it.next();
                if (patientStringArray[PatientField.KEY.ordinal()].
                        equals(String.valueOf(p.getKey()))){
                    isPatientRecordFound = true;
                    break;
                }
            }
            if (!isPatientRecordFound){
                throw new StoreException("Specified patient key in "
                            + "CSVStore.Update(Patient) not found in darabase.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                patientsStringArrayList.remove(index);
                CSVWriter csvWriter = getCSVPatientWriter();
                csvWriter.writeAll(patientsStringArrayList);
                csvWriter.flushQuietly();
                String[] serialisedPatient = serialise(p);
                csvWriter = getCSVPatientWriter();
                csvWriter.writeNext(serialisedPatient);
                csvWriter.flushQuietly();
                return read(p);
            }
        }
    }

    @Override
    public void delete(Appointment a) throws StoreException{
        String[] appointmentStringArray = null;
        boolean isAppointmentRecordFound = false;
        Integer key = a.getKey();
        if (key == null){
            throw new StoreException("Key value null in call to "
                                        + "CSVStore.delete(Appointment) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Specified key value not found in "
                                      + "call to CSVStore.delete(Appointment) "
                                      + "method",
                                      ExceptionType.KEY_NOT_FOUND_EXCEPTION);
        }
        else {
            List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                appointmentStringArray = it.next();
                if (appointmentStringArray[AppointmentField.KEY.ordinal()].
                        equals(String.valueOf(a.getKey()))){
                    isAppointmentRecordFound = true;
                    break;
                }
            }
            if (!isAppointmentRecordFound){
                throw new StoreException("Specified appointment key in "
                            + "CSVStore.delete(Appointment) not found in database.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                appointmentStringArray = appointmentsStringArrayList.remove(index);
                File f =  new File(appointmentsPath.toString());
                f.delete();
                CSVWriter csvWriter = getCSVAppointmentWriter();
                csvWriter.writeAll(appointmentsStringArrayList);
                csvWriter.flushQuietly(); 
            }
        }
    }
    
    @Override
    public void delete(Patient p) throws StoreException{
        boolean isPatientRecordFound = false;
        Integer key = p.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.delete(Patient) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.delete(Patient) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
            Iterator<String[]> it = patientsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] patientStringArray = it.next();
                if (patientStringArray[PatientField.KEY.ordinal()].
                        equals(String.valueOf(p.getKey()))){
                    isPatientRecordFound = true;
                    break;
                }
            }
            if (!isPatientRecordFound){
                throw new StoreException("Specified patient key in "
                            + "CSVStore.delete(Patient) not found in database.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                patientsStringArrayList.remove(index);
                CSVWriter csvWriter = getCSVAppointmentWriter();
                csvWriter.writeAll(patientsStringArrayList);
                csvWriter.flushQuietly();
            }
        }
    }
    
    private void sortNonExistingPatientsByKey(ArrayList<Integer> unSortedList){
        Comparator<Integer> compareByValue
                = (Integer o1, Integer o2)
                -> o1.compareTo(o2);
        Collections.sort(unSortedList, compareByValue);
    }
    
    private void sortAppointmentsByPatientKey(ArrayList<Appointment> unSortedA){
        Comparator<Appointment> compareByPatientKey
                = (Appointment o1, Appointment o2)
                -> o1.getPatient().getKey().compareTo(o2.getPatient().getKey());
        Collections.sort(unSortedA, compareByPatientKey);
    }
    
    private void sortAppointmentsByDay(ArrayList<Appointment> unSortedA){
        Comparator<Appointment> compareByStart
                = (Appointment o1, Appointment o2)
                -> o1.getStart().compareTo(o2.getStart());
        Collections.sort(unSortedA, compareByStart);
    }
    
    private void sortPatientsByName(ArrayList<Patient> patients){
        Comparator<Patient> compareByName
                = (Patient o1, Patient o2)
                -> (o1.getName().getSurname() + o1.getName().getForenames()).
                        compareTo(o2.getName().getSurname() + o2.getName().getForenames());
        Collections.sort(patients, compareByName);
    }
    
    public static void appointmentfileConverter()throws StoreException{ 
        Path appointmentsPath = DATABASE_FOLDER.resolve(DBF_APPOINTMENTS_FILE);
        Path patientsPath = DATABASE_FOLDER.resolve(DBF_PATIENTS_FILE);

        try{
            BufferedReader appointmentReader = Files.newBufferedReader(appointmentsPath,StandardCharsets.ISO_8859_1);
            CSVReader csvDBFAppointments = new CSVReader(appointmentReader);
            List<String[]> dbfAppointments = csvDBFAppointments.readAll();
            convertToAppointmentsFromDBFFile(dbfAppointments);
            convertAppointmentsToCSV(appointments);

        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in appointmentfileconverter()";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            String message = "CsvException message -> + e.getMessage()" + "\n" +
                    "StoreException message -> Error encountered in appointmentfileconverter()";
        }
    }

}
