package com.rosteringester.filesystem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by MichaelChrisco on 06/30/2017.
 */
public class DelimitedTextTest {

    DelimitedText delim;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        delim = new DelimitedText();
    }
    @Test
    public void getHeaders() throws Exception {
        File resourcesDirectory = new File("src/main/resources/example.roster.txt");
        File resourcesAstDirectory = new File("src/main/resources/example.astRoster.txt");
        File resourcesCSVDirectory = new File("src/main/resources/example.csvRoster.txt");
        File resourcesTabDirectory = new File("src/main/resources/example.tabRoster.txt");
//        File resourcesDirectory = new File("C:\\DATA\\example1.astRoster.txt");
        DelimitedText testing = new DelimitedText();
        HashMap hash = new HashMap();
        hash.put(0, "A");
        hash.put(1, "B");
        hash.put(2, "C");
        HashMap subject = testing.getHeaders(resourcesDirectory.getAbsolutePath(), "|");
        HashMap subject2 = testing.getHeaders(resourcesCSVDirectory.getAbsolutePath(), ",");
        HashMap subject3 = testing.getHeaders(resourcesTabDirectory.getAbsolutePath(), "\t");
//        HashMap subject2 = testing.getHeaders(resourcesAstDirectory.getAbsolutePath(), "*");
        assertEquals(hash, subject);
        assertEquals(hash, subject2);
        assertEquals(hash, subject3);
    }

//    //Get single record for processing
//    @Test
//    public void getRecord(){
//
//    }
//
//    //Get multiple records from the file and batch insert the records.
//    @Test
//    public void getBatchedRecords(){
//
//    }

    //Get all records from a file.
/*    @Test
    public void getRecords() throws Exception{
        File resourcesDirectory = new File("src/main/resources/example.roster.txt");
        DelimitedText testing = new DelimitedText();
        HashMap hash = new HashMap();
        hash.put(0, "A");
        hash.put(1, "B");
        hash.put(2, "C");
        HashMap result[] = {hash};
        assertEquals(result, testing.getRecords(resourcesDirectory.getAbsolutePath(), "|"));
    }*/

    @Test
    public void detectDelimiterTest() throws Exception {
        File resourcesDirectory = new File("src/main/resources/example.roster.txt");
        File resourcesAstDirectory = new File("src/main/resources/example.astRoster.txt");
        File resourcesCSVDirectory = new File("src/main/resources/example.csvRoster.txt");
        File resourcesYAMLDirectory = new File("src/main/resources/example.env.yaml");
        File resourcesTabDirectory = new File("src/main/resources/example.tabRoster.txt");
        
        DelimitedText subject = new DelimitedText();
        //When delimiter is given
        assertEquals("|", subject.detectDelimiter(resourcesDirectory.getAbsolutePath(), "|"));
        //When delimiter is not given
        assertEquals("|", subject.detectDelimiter(resourcesDirectory.getAbsolutePath()));
        assertEquals("*", subject.detectDelimiter(resourcesAstDirectory.getAbsolutePath()));
        assertEquals(",", subject.detectDelimiter(resourcesCSVDirectory.getAbsolutePath()));
        assertEquals("\t" , subject.detectDelimiter(resourcesTabDirectory.getAbsolutePath()));
        assertEquals("", subject.detectDelimiter(resourcesYAMLDirectory.getAbsolutePath()));
    }

}