/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author colin
 */
public class CSVReader {
    public List<String[]> getAppointmentDBFRecords(String path) throws StoreException{
        List<String[]> result = null;
        Path sourcePath = Path.of(path);
        try{
            BufferedReader appointmentReader = Files.newBufferedReader(sourcePath,StandardCharsets.ISO_8859_1);
            com.opencsv.CSVReader csvDBFAppointments = new com.opencsv.CSVReader(appointmentReader);
            result = csvDBFAppointments.readAll();
            csvDBFAppointments.close();
            appointmentReader.close();
        }catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in appointmentfileconverter()";
            throw new StoreException(message, StoreException.ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            String message = "CsvException message -> + e.getMessage()" + "\n" +
                    "StoreException message -> Error encountered in appointmentfileconverter()";
            throw new StoreException(message, StoreException.ExceptionType.CSV_EXCEPTION);
        }
        catch (Exception e){
            String message = "Exception message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in migrateAppointments()";
            throw new StoreException(message, StoreException.ExceptionType.IO_EXCEPTION);
        }
        return result;
    }
    
    public List<String[]> getPatientDBFRecords(String path) throws StoreException{
        List<String[]> result = null;
        Path patientsPath = Path.of(path);
        String message = null;
        try{
            BufferedReader patientReader = Files.newBufferedReader(patientsPath,StandardCharsets.ISO_8859_1);
            //BufferedReader patientReader = Files.newBufferedReader(patientsPath,StandardCharsets.UTF_8);
            com.opencsv.CSVReader csvDBFPatientsReader = new com.opencsv.CSVReader(patientReader);
            
            result = csvDBFPatientsReader.readAll();
            csvDBFPatientsReader.close();
            patientReader.close();
        }catch (java.nio.charset.MalformedInputException e){
            message = "MalformedInputException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVReader constructor " +
                    "on initialisation of appointmentReader or patientReader File object";
            throw new StoreException(message, StoreException.ExceptionType.IO_EXCEPTION);
        }
        catch (IOException e){
            message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVReader constructor " +
                    "on initialisation of appointmentReader or patientReader File object";
            throw new StoreException(message, StoreException.ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            message = "CSVException " + e.getMessage();
        }
        catch (Exception e){
            message = "CSVException "  + e.getMessage() + "\n" +
            "StoreException message -> Error encountered in CSVReader::migratePatients " +
                    "on call to CSVReader::csvDBFPatientsReader";
            throw new StoreException(message, StoreException.ExceptionType.CSV_EXCEPTION);
        }
        return result;
    }
}
