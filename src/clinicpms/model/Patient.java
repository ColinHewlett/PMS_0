/**
 * Current version includes an inner class which defines references to
 * Appointment objects, i.e the lastDentalAppointment, the nextDentalAppointment 
 * and the nextHygieneAppointment. The query leg work to fetch the Appointment 
 * objects can be done automatically by the Store object or initiated by the 
 * Patient class. Currently the latter option is adopted.   
 */
package clinicpms.model;

import clinicpms.store.StoreException;
import clinicpms.store.Store;
import java.time.LocalDate;
import java.util.ArrayList;
import clinicpms.store.IPMSStoreAction;

/**
 *
 * @author colin
 */
public class Patient implements IEntity, IEntityStoreType{
    
    private LocalDate dob = null;
    private Patient guardian = null;
    private String gender = null;
    private Boolean isGuardianAPatent = null;
    private Integer key  = null;
    private String notes = null;
    private String phone1 = null;
    private String phone2 = null;

    private Patient.AppointmentHistory appointmentHistory = null;
    private Patient.Address address = null;
    private Patient.Name name = null;
    private Patient.Recall recall = null;

    public enum PatientField    {       ID,
                                        KEY,
                                        PHONE1,
                                        PHONE2,
                                        GENDER,
                                        DOB,
                                        IS_GUARDIAN_A_PATIENT,
                                        PATIENT_GUARDIAN,
                                        NOTES;
                    public enum Name    {   TITLE,
                                            FORENAMES,
                                            SURNAME
                                        }
                    public enum Address {   LINE1,
                                            LINE2,
                                            TOWN,
                                            COUNTY,
                                            POSTCODE
                                        }
                    public enum Recall  {   DENTAL_DATE,
                                            HYGIENE_DATE,
                                            DENTAL_FREQUENCY,
                                            HYGIENE_FREQUENCY
                                        }
                    public enum Activity    {   LAST_DENTAL_APPOINTMENT,
                                                NEXT_DENTAL_APPOINTMENT,
                                                NEXT_HYGIENE_APPOINTMENT
                                            }
                                }
    @Override
    public boolean isAppointment(){
        return false;
    }
    
    @Override
    public boolean isAppointments(){
        return false;
    }
    
    @Override
    public boolean isAppointmentTable(){
        return false;
    }
    
    @Override
    public boolean isAppointmentDate(){
        return false;
    }
    
    @Override
    public final boolean isAppointmentTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isPatientTable(){
        return false;
    }
    
    @Override
    public boolean isPatient(){
        return true;
    }
    
    @Override
    public boolean isPatients(){
        return false;
    }
    
    @Override
    public final boolean isPatientTableRowValue(){
        return false;
    }
    
    @Override
    public boolean isSurgeryDaysAssignment(){
        return false;
    }
    
    /**
     * Constructs a new Patient object with none of its fields initialised
     */
    public Patient(){
        name = new Name();
        address = new Address();
        recall = new Recall();
        appointmentHistory = new AppointmentHistory();
    } 
    
    public Patient(Integer key) {
            name = new Name();
            address = new Address();
            recall = new Recall();
            appointmentHistory = new AppointmentHistory();
            this.key = key;
    } 
    
    @Override
    public void create()throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.create(this);
    }
    
    @Override
    public void insert() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.insert(this);    
    }
    
    @Override
    public void delete() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.delete(this);
    }
    
    @Override
    public void drop() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        store.drop(this);        
    }
    
    @Override
    public Patient read() throws StoreException{
        IPMSStoreAction store = Store.FACTORY(this);
        return store.read(this); 
    }
    
    @Override
    public void update() throws StoreException{ 
        IPMSStoreAction store = Store.FACTORY(this);
        store.update(this);
    }

    public class Name {

        private String forenames = null;
        private String surname = null;
        private String title = null;

        public String getForenames() {
            return forenames;
        }

        public void setForenames(String forenames) {
            this.forenames = forenames;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

    }
    
    public class Address {

        private String line1 = null;
        private String line2 = null;
        private String town = null;
        private String county = null;
        private String postcode = null;

        public String getLine1() {
            return line1;
        }

        public void setLine1(String line1) {
            this.line1 = line1;
        }

        public String getLine2() {
            return line2;
        }

        public void setLine2(String line2) {
            this.line2 = line2;
        }

        public String getTown() {
            return town;
        }

        public void setTown(String town) {
            this.town = town;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public String getPostcode() {
            return postcode;
        }

        public void setPostcode(String postcode) {
            this.postcode = postcode;
        }

    }

    public class Recall {

        private LocalDate dentalDate = null;
        private LocalDate hygieneDate = null;
        private Integer dentalFrequency = null;
        private Integer hygieneFrequency = null;

        public LocalDate getDentalDate() {
            return dentalDate;
        }

        public void setDentalDate(LocalDate dentalDate) {
            this.dentalDate = dentalDate;
        }

        public Integer getDentalFrequency() {
            return dentalFrequency;
        }

        public void setDentalFrequency(Integer dentalFrequency) {
            this.dentalFrequency = dentalFrequency;
        }

        public LocalDate getHygieneDate() {
            return hygieneDate;
        }

        public void setHygieneDate(LocalDate hygieneDate) {
            this.hygieneDate = hygieneDate;
        }

        public Integer getHygieneFrequency() {
            return hygieneFrequency;
        }

        public void setHygieneFrequency(Integer hygieneFrequency) {
            this.hygieneFrequency = hygieneFrequency;
        }
    }
    
    public class AppointmentHistory{

        public ArrayList<Appointment> getDentalAppointments()throws StoreException{
            if (Patient.this.getKey()!=null) {
                Appointments appointments = new Appointments();
                appointments.readForPatient(
                    Patient.this,Appointment.Category.DENTAL);
                return appointments;
            }
            else return null;
        }
        
        public ArrayList<Appointment> getHygieneAppointments()throws StoreException{
            if (Patient.this.getKey()!=null){ 
                Appointments appointments = new Appointments();
                appointments.readForPatient(Patient.this, Appointment.Category.HYGIENE);
                return appointments;
            }
            else return null;
        }
    }
    
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDOB() {
        return dob;
    }
    public void setDOB(LocalDate dob) {
        this.dob = dob;
    }
    
    public Integer getKey(){
        return key;
    }
    public void setKey(Integer key){
        this.key = key;
    }
    
    public String getNotes(){
        return notes;
    }
    public void setNotes(String notes){
        this.notes = notes;
    }
    
    public String getPhone1(){
        return phone1;
    }
    public void setPhone1(String phone1){
        this.phone1 = phone1;
    }
    
    public String getPhone2(){
        return phone2;
    }
    public void setPhone2(String phone2){
        this.phone2 = phone2;
    }
    
    public Boolean getIsGuardianAPatient(){
        return isGuardianAPatent;
    }
    public void setIsGuardianAPatient(Boolean isGuardianAPatient){
        this.isGuardianAPatent = isGuardianAPatient;
    }
    
    public Patient getGuardian(){
        return guardian;
    }
    public void setGuardian(Patient guardian){
        this.guardian = guardian;
    }
    
    public Patient.Name getName(){
        return name;
    }
    public void setName(Patient.Name name){
        this.name = name;
    }
    
    public Patient.Address getAddress(){
        return address;
    }
    public void setAddress(Patient.Address address){
        this.address = address;
    }
    
    public Patient.Recall getRecall(){
        return recall;
    }
    public void setRecall(Patient.Recall recall){
        this.recall = recall;
    }
    
    public Patient.AppointmentHistory getAppointmentHistory(){
        return appointmentHistory;
    }
    public void setAppointmentHistory(Patient.AppointmentHistory value){
        this.appointmentHistory = value;
    }
    
    public void reformat(){
        String cappedForenames = "";
        String cappedSurname = "";
        String cappedTitle = "";
        String cappedLine1 = "";
        String cappedLine2 = "";
        String cappedTown = "";
        String cappedCounty = "";
        
        if (getAddress().getLine1() == null) getAddress().setLine1("");
        if (getAddress().getLine1().length()>0){
            //cappedLine1 = getAddress().getLine1().strip();
            cappedLine1 = getAddress().getLine1();
            if (cappedLine1.contains("-")){ 
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "-");
                if (cappedLine1.contains(" ")){
                   cappedLine1 = capitaliseFirstLetter(cappedLine1, "\\s+"); 
                }
            }
            else if (cappedLine1.contains(" ")) 
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "\\s+");
            else
                cappedLine1 = capitaliseFirstLetter(cappedLine1, "");
        }
        
        if (getAddress().getLine2() == null) getAddress().setLine2("");
        if (getAddress().getLine2().length()>0){
            //cappedLine2 = getAddress().getLine2().strip();
            cappedLine2 = getAddress().getLine2();
            if (cappedLine2.contains("-")){ 
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "-");
                if (cappedLine2.contains(" ")){
                   cappedLine2 = capitaliseFirstLetter(cappedLine2, "\\s+"); 
                }
            }
            else if (cappedLine2.contains(" ")) 
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "\\s+");
            else
                cappedLine2 = capitaliseFirstLetter(cappedLine2, "");
        }
        
        if (getAddress().getTown() == null) getAddress().setTown("");
        if (getAddress().getTown().length()>0){
            //cappedTown = getAddress().getTown().strip();
            cappedTown = getAddress().getTown();
            if (cappedTown.contains("-")){ 
                cappedTown = capitaliseFirstLetter(cappedTown, "-");
                if (cappedTown.contains(" ")){
                   cappedTown = capitaliseFirstLetter(cappedTown, "\\s+"); 
                }
            }
            else if (cappedTown.contains(" ")) 
                cappedTown = capitaliseFirstLetter(cappedTown, "\\s+");
            else
                cappedTown = capitaliseFirstLetter(cappedTown, "");
        }
        
        if (getAddress().getCounty() == null) getAddress().setCounty("");
        if (getAddress().getCounty().length()>0){
            //cappedCounty = getAddress().getCounty().strip();
            cappedCounty = getAddress().getCounty();
            if (cappedCounty.contains("-")){ 
                cappedCounty = capitaliseFirstLetter(cappedCounty, "-");
                if (cappedCounty.contains(" ")){
                   cappedCounty = capitaliseFirstLetter(cappedCounty, "\\s+"); 
                }
            }
            else if (cappedCounty.contains(" ")) 
                cappedCounty = capitaliseFirstLetter(cappedCounty, "\\s+");
            else
                cappedCounty = capitaliseFirstLetter(cappedCounty, "");
        }
        
        if (getName().getSurname() == null) getName().setSurname("");
        if (getName().getSurname().length()>0){
            //cappedSurname = getName().getSurname().strip();
            cappedSurname = getName().getSurname();
            if (cappedSurname.contains("-")){ 
                cappedSurname = capitaliseFirstLetter(cappedSurname, "-");
                if (cappedSurname.contains(" ")){
                   cappedSurname = capitaliseFirstLetter(cappedSurname, "\\s+"); 
                }
            }
            else if (cappedSurname.contains(" ")) 
                cappedSurname = capitaliseFirstLetter(cappedSurname, "\\s+");
            else
                cappedSurname = capitaliseFirstLetter(cappedSurname, "");
        }
        if (getName().getForenames() == null) getName().setForenames("");
        if (getName().getForenames().length()>0){
            //cappedForenames = getName().getForenames().strip();
            cappedForenames = getName().getForenames();
            if (cappedForenames.contains("-")){ 
                cappedForenames = capitaliseFirstLetter(cappedForenames, "-");
                if (cappedForenames.contains(" ")){
                   cappedForenames = capitaliseFirstLetter(cappedForenames, "\\s+"); 
                }
            }
            else if (cappedForenames.contains(" ")) 
                cappedForenames = capitaliseFirstLetter(cappedForenames, "\\s+");
            else
                cappedForenames = capitaliseFirstLetter(cappedForenames, "");
        }
        if (getName().getTitle() == null) getName().setTitle("");
        if (getName().getTitle().length()>0){
            //cappedTitle = getName().getTitle().strip();
            cappedTitle = getName().getTitle();
            cappedTitle = capitaliseFirstLetter(cappedTitle, "");
        }
        getName().setSurname(cappedSurname);
        getName().setForenames(cappedForenames);
        getName().setTitle(cappedTitle);
        getAddress().setLine1(cappedLine1);
        getAddress().setLine2(cappedLine2);
        getAddress().setTown(cappedTown);
        getAddress().setCounty(cappedCounty);
    }
    
    /**
     * Utility method involved in the "tidy up" of the imported patient's contact details
     * @param value:String
     * @param delimiter:String representing the character used to delimit in the context of the patient's name
     * @return Sting; the processed patient's contact details
     */
    private String capitaliseFirstLetter(String value, String delimiter){
        ArrayList<String> parts = new ArrayList<>();
        String result = null;
        //value = value.strip();
        if (!delimiter.equals("")){
            String[] values = value.split(delimiter);
            for (int index = 0; index < values.length; index++){
                parts.add(capitalisePart(values[index]));
            }
            for (int index = 0;index < parts.size();index++){
                if (index == 0){
                    result = parts.get(index);
                }
                else if (delimiter.equals("\\s+")){
                    result = result + " " + parts.get(index);
                }
                else{
                    result = result + delimiter + parts.get(index);
                }
            }
        }
        else{
            result = capitalisePart(value);
        }
        return result;
    }
    
    /**
     * Part of the convenience process used for tidying up the imported patient's contact details
     * @param part:String; part of the string required to be processed
     * @return String; processed part
     */
    private String capitalisePart(String part){
        String firstLetter = part.substring(0,1).toUpperCase();
        String otherLetters = part.substring(1).toLowerCase();
        String result =  firstLetter + otherLetters;
        return result;
    }
    
    private Patient updateGender(Patient patient){
        switch (patient.getGender()){
            case "M":
                patient.setGender("Male");
                break;
            case "F":
                patient.setGender("Female");
                break;
        }
        return patient;
    }
}
