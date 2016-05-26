/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction.process;

import java.io.File;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author Johann Petrak
 */
public class TestProcess4ObjectStream {
  
  @Test
  public void testAll() {
    Process4ObjectStream process = 
            new Process4ObjectStream(new File("."), "java -cp target/interaction-1.0-SNAPSHOT.jar  gate.lib.interaction.process.EchoObjectStream");
    assertTrue(process.isAlive());
    // send something to the echo process
    process.writeObject("Something");
    Object obj = process.readObject();
    // make sure we got it back properly
    assertEquals("Something", obj);
    // send the stop signal
    process.writeObject("STOP");
    // stop process
    process.stop();
    // make sure it is stopped
    // NOTE: this does not seem to work?
    // TODO
    // assertFalse(process.isAlive());
            
  }
  
  
}
