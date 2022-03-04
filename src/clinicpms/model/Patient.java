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
    

}
