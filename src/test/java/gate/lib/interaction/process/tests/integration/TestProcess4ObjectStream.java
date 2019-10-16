/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction.process.tests.integration;
import gate.lib.interaction.process.*;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 *
 * @author Johann Petrak
 */
public class TestProcess4ObjectStream {
  
  @Test
  public void testAll() {
    Process4ObjectStream process = 
            Process4ObjectStream.create(new File("."), null,
            "java -cp target/*  gate.lib.interaction.process.EchoObjectStream".split("\\s+",-1));
    assertTrue(process.isAlive());
    // send something to the echo process
    Object obj = process.process("Something");
    // make sure we got it back properly
    assertEquals("Something", obj);
    // send the stop signal
    process.process("STOP");
    // stop process
    process.stop();
    assertFalse(process.isAlive());
            
  }
  
  
}
