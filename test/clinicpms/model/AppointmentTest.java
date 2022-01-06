/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author colin
 */
public class AppointmentTest {
    
    public AppointmentTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of create method, of class Appointment.
     */
    @Test
    public void testCreate() throws Exception {
        System.out.println("create");
        Appointment instance = new Appointment();
        Appointment expResult = null;
        //Appointment result = instance.create();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class Appointment.
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("delete");
        Appointment instance = new Appointment();
        instance.delete();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class Appointment.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        Appointment instance = new Appointment();
        Appointment expResult = null;
        Appointment result = instance.read();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class Appointment.
     */
    @Test
    public void testUpdate() throws Exception {
        System.out.println("update");
        Appointment instance = new Appointment();
        Appointment expResult = null;
        //Appointment result = instance.update();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStart method, of class Appointment.
     */
    @Test
    public void testGetStart() {
        System.out.println("getStart");
        Appointment instance = new Appointment();
        LocalDateTime expResult = null;
        LocalDateTime result = instance.getStart();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStart method, of class Appointment.
     */
    @Test
    public void testSetStart() {
        System.out.println("setStart");
        LocalDateTime start = null;
        Appointment instance = new Appointment();
        instance.setStart(start);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDuration method, of class Appointment.
     */
    @Test
    public void testGetDuration() {
        System.out.println("getDuration");
        Appointment instance = new Appointment();
        Duration expResult = null;
        Duration result = instance.getDuration();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDuration method, of class Appointment.
     */
    @Test
    public void testSetDuration() {
        System.out.println("setDuration");
        Duration duration = null;
        Appointment instance = new Appointment();
        instance.setDuration(duration);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNotes method, of class Appointment.
     */
    @Test
    public void testGetNotes() {
        System.out.println("getNotes");
        Appointment instance = new Appointment();
        String expResult = "";
        String result = instance.getNotes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNotes method, of class Appointment.
     */
    @Test
    public void testSetNotes() {
        System.out.println("setNotes");
        String notes = "";
        Appointment instance = new Appointment();
        instance.setNotes(notes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKey method, of class Appointment.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        Appointment instance = new Appointment();
        Integer expResult = null;
        Integer result = instance.getKey();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setKey method, of class Appointment.
     */
    @Test
    public void testSetKey() {
        System.out.println("setKey");
        Integer key = null;
        Appointment instance = new Appointment();
        instance.setKey(key);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatus method, of class Appointment.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        Appointment instance = new Appointment();
        Appointment.Status expResult = null;
        Appointment.Status result = instance.getStatus();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStatus method, of class Appointment.
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");
        Appointment.Status value = null;
        Appointment instance = new Appointment();
        instance.setStatus(value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPatient method, of class Appointment.
     */
    @Test
    public void testGetPatient() {
        System.out.println("getPatient");
        Appointment instance = new Appointment();
        Patient expResult = null;
        Patient result = instance.getPatient();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPatient method, of class Appointment.
     */
    @Test
    public void testSetPatient() {
        System.out.println("setPatient");
        Patient patient = null;
        Appointment instance = new Appointment();
        instance.setPatient(patient);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCategory method, of class Appointment.
     */
    @Test
    public void testGetCategory() {
        System.out.println("getCategory");
        Appointment instance = new Appointment();
        Appointment.Category expResult = null;
        Appointment.Category result = instance.getCategory();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCategory method, of class Appointment.
     */
    @Test
    public void testSetCategory() {
        System.out.println("setCategory");
        Appointment.Category category = null;
        Appointment instance = new Appointment();
        instance.setCategory(category);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
