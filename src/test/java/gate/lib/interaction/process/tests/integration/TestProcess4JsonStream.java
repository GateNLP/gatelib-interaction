/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction.process.tests.integration;
import gate.lib.interaction.process.pipes.Process4JsonStream;
import gate.lib.interaction.process.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author Johann Petrak
 */
public class TestProcess4JsonStream {
  
  @Test
  public void testAll() {
    System.err.println("Test Process4JsonStream: before creation");
    Process4JsonStream process = 
            Process4JsonStream.create(new File("."), null,
            "java -cp target/*:target/dependency/*:target/test-classes  gate.lib.interaction.process.tests.integration.EchoStream".split("\\s+",-1));
    System.err.println("Test Process4JsonStream: after creation");
    assertTrue(process.isAlive());
    System.err.println("Test Process4JsonStream: is alive");
    // send something to the echo process
    Map m = new HashMap();
    m.put("x","y");
    m.put("cmd","do");
    System.err.println("Test Process4JsonStream: before write");
    Map ret = (Map)process.process(m);
    System.err.println("Test Process4JsonStream: after read");
    // make sure we got it back properly
    assertNotNull(ret);
    assertEquals("y", ret.get("x"));
    
    // test sending an array of doubles
    List<Double> origs = Arrays.asList(1.1,2.2,3.3,4.4);
    m = new HashMap();
    m.put("vals",origs);
    ret = (Map)process.process(m);
    assertNotNull(ret);
    List<Double> retvals = (List)ret.get("vals");
    System.err.println("Array back: "+retvals);
    
    // send the stop signal, can do this as a JSON object or just raw String for the EchoStream class  
    // Note the raw string will get transmitted as JSON as well, i.e. it will be sent over quoted.
    //m = new HashMap();
    //m.put("cmd", "STOP");
    //process.writeObject(m);
    // stop process
    process.process("STOP");
    
    process.stop();
    assertFalse(process.isAlive());
            
  }
  
  
}
