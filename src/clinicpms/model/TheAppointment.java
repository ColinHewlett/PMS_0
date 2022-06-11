/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.IAppointmentsStoreAction;
import clinicpms.store.IStoreAction;
import clinicpms.store.Store;
import clinicpms.store.StoreException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author colin
 */
public class TheAppointment extends EntityStoreType{
    public static enum Status{BOOKED,UNBOOKED};
    private Integer key = null;
    private LocalDateTime start = null;
    private Duration duration  = null;
    private String notes = null;
    private Patient patient;
    private Category category = null;
    private Status status = TheAppointment.Status.BOOKED;
    
    
    public enum Scope {ALL, FOR_PATIENT, FOR_DAY, FROM_DAY};
    
    private  enum DenAppField {DATE,A_1,A_2,A_3,A_4,A_5,A_6,A_7,A_8,A_9,
                                A_10,A_11,A_12,A_13,A_14,A_15,A_16,A_17,A_18,A_19,
                                A_20,A_21,A_22,A_23,A_24,A_25,A_26,A_27,A_28,A_29,
                                A_30,A_31,A_32,A_33,A_34,A_35,A_36,A_37,A_38,A_39,
                                A_40,A_41,A_42,A_43,A_44,A_45,A_46,A_47,A_48,A_49,
                                A_50,A_51,A_52,A_53,A_54,A_55,A_56,A_57,A_58,A_59,
                                A_60,A_61,A_62,A_63,A_64,A_65,A_66,A_67,A_68,A_69,
                                A_70,A_71,A_72,A_73,A_74,A_75,A_76,A_77,A_78,A_79,
                                A_80,A_81,A_82,A_83,A_84,A_85,A_86,A_87,A_88,A_89,
                                A_90,A_91,A_92,A_93,A_94,A_95,A_96,A_97,A_98,A_99,
                                A_100,A_101,A_102,A_103,A_104,A_105,A_106,A_107,A_108,A_109,
                                A_110,A_111,A_112,A_113,A_114,A_115,A_116,A_117,A_118,A_119,
                                A_120,A_121,A_122,A_123,A_124,A_125,A_126,A_127,A_128,A_129,
                                A_130,A_131,_132,A_133,A_134,A_135,A_136,A_137,A_138,A_139,
                                A_140,A_141,A_142,A_143,A_144}
    private TheAppointment.Collection collection = null;
    private static final DateTimeFormatter ddMMyyyyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static enum Category{DENTAL, HYGIENE, ALL}
    
    public TheAppointment(){
        super.setIsAppointment(true);
        collection = new TheAppointment.Collection();
    } //constructor creates a new Appointment record

    /**
     * 
     * @param key 
     */
    public TheAppointment( int key) {
        this.key = key;
        collection = new TheAppointment.Collection();
        super.setIsAppointment(true);
    }
    
    public TheAppointment.Collection getCollection(){
        return collection;
    }
    
    public void setCollection(TheAppointment.Collection value){
        collection = value;
    }
    
    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    
    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration  duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getKey() {
        return key;
    }
    public void setKey(Integer key) {
        this.key = key;
    }
    public TheAppointment.Status getStatus(){
        return this.status;
    }
    public void setStatus(TheAppointment.Status value){
        this.status = value;
    }       

    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public TheAppointment.Category getCategory() {
        return category;
    }
    public void setCategory(TheAppointment.Category category) {
        this.category = category;
    }
    
    public void create()throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        store.create(this);
    }
    
    public void insert() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        store.insert(this);  
    }
    
    public void delete() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        store.delete(this);
    }
    
    public void drop() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        store.drop(this);        
    }
    
    public TheAppointment read() throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        return store.read(this);
    }
    
    public void update() throws StoreException{ 
        IStoreAction store = Store.FACTORY(this);
        store.update(this);
    }
    
    public class Collection extends EntityStoreType{
        private ArrayList<TheAppointment> collection = null;
        private TheAppointment.Scope readScope = null;
        private Collection(){
            super.setIsAppointments(true);
        }
        public Integer count()throws StoreException{
            IStoreAction store = Store.FACTORY(this); 
            return store.count(this);
        }
        public void read()throws StoreException{
            IStoreAction store = Store.FACTORY(TheAppointment.this); 
            set(store.read(this).get());
        }
        
        public ArrayList<TheAppointment> get(){
            return collection;
        }
        public void set(ArrayList<TheAppointment> value){
            collection = value;
        }
        public TheAppointment.Scope getScope(){
            return readScope;
        }
        public void setScope(TheAppointment.Scope value){
            readScope = value;
        }
    }
    
    public List<String[]> importEntityFromCSV()throws StoreException{
        IStoreAction store = Store.FACTORY(this);
        //setImportedDBFRecords(store.importFromCSV1(this));
        return store.importEntityFromCSV(this);
    }
    
    public ArrayList<TheAppointment> convertDBFRecordToAppointments(String[] dbfAppointmentRow)throws StoreException{
        String date = null;
        String year;
        String month;
        String d;
        
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
        
        ArrayList<TheAppointment> appointmentsForThisDBFRecord = null;
        Patient patient = null;
        LocalDateTime start = null;
        Duration duration = null;
        String notes = null;
        Integer patientKey = null;
        Integer value = null;
        int appointmentStartTimeRowIndex = 0;
        int appointmentEndTimeRowIndex = 0;
        boolean isRowEnd = false;
        TheAppointment appointment = null;
        
        int rowIndex = DenAppField.A_1.ordinal();
        appointmentsForThisDBFRecord = new ArrayList<>();
        for (; rowIndex < DenAppField.A_144.ordinal(); rowIndex++){
            while(dbfAppointmentRow[rowIndex].isEmpty()){
                if (patientKey!=null){//signals end of appointment of current patient
                        appointmentEndTimeRowIndex = rowIndex;
                        appointment = getAppointmentFrom(dbfAppointmentRow,
                                                         date,
                                                         appointmentStartTimeRowIndex,
                                                         appointmentEndTimeRowIndex,
                                                         patientKey);
                        appointment.setKey(appointmentsForThisDBFRecord.size()+1);
                        appointmentsForThisDBFRecord.add(appointment);
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
                    value = getPatientKey(dbfAppointmentRow[rowIndex]); 
                    if (value!=null){
                        if (!value.equals(patientKey)){//signals next appointment start slot
                            appointmentEndTimeRowIndex = rowIndex;
                            appointment = getAppointmentFrom(dbfAppointmentRow,
                                                             date,
                                                             appointmentStartTimeRowIndex,
                                                             appointmentEndTimeRowIndex,
                                                             patientKey);
                            appointment.setKey(appointmentsForThisDBFRecord.size()+1);
                            appointmentsForThisDBFRecord.add(appointment);
                            patientKey = value;
                            appointmentStartTimeRowIndex = rowIndex;
                        }
                    }
                }
                else {//first appointment after a gap
                    value = getPatientKey(dbfAppointmentRow[rowIndex]);
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
        return appointmentsForThisDBFRecord;
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
    private static TheAppointment getAppointmentFrom(String[] row, 
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
        TheAppointment appointment = new TheAppointment();
        
        appointment.setPatient(patient);
        appointment.setStart(start);
        appointment.setDuration(duration);
        appointment.setNotes(notes);
        
        return appointment;
    }
    
    private Integer getPatientKey(String s)throws StoreException{
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
                throw new StoreException(s, StoreException.ExceptionType.IO_EXCEPTION);
            }
        }
        return result;
    }
    
    private static LocalTime getAppointmentStartTime(int startRowIndex){
        int slotCountFromDayStart = startRowIndex - TheAppointment.DenAppField.A_1.ordinal();
        LocalTime firstSlotTimeForDay = LocalTime.of(8, 0); //= 8am
        return firstSlotTimeForDay.plusMinutes(slotCountFromDayStart * 5);
    }
}
