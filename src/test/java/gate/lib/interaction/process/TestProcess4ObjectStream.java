/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction.process;

import java.io.File;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author Johann Petrak
 */
public class TestProcess4ObjectStream {
  
  static Process4ObjectStream process;
  
  @BeforeClass
  public static void init() throws Exception {
    //process = new Process4ObjectStream(new File("."), "java -cp target/interaction-1.0-SNAPSHOT.jar  gate.lib.interaction.process.EchoObjectStream");
  }
  
  @AfterClass
  public static void cleanup() throws Exception {
    //process.stop();
  }

  /**
   * Test of readObject method, of class Process4ObjectStream.
   */
  @Test
  public void testReadObject() {
    /*
    System.out.println("readObject");
    Process4ObjectStream instance = null;
    Object expResult = null;
    Object result = instance.readObject();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }

  /**
   * Test of writeObject method, of class Process4ObjectStream.
   */
  public void testWriteObject() {
    /*
    System.out.println("writeObject");
    Object object = null;
    Process4ObjectStream instance = null;
    instance.writeObject(object);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }

  /**
   * Test of isAlive method, of class Process4ObjectStream.
   */
  @Test
  public void testIsAlive() {
    System.err.println("isAlive");
    boolean expResult = true;
   // boolean result = process.isAlive();
    //assertEquals(expResult, result);
  }

  /**
   * Test of stop method, of class Process4ObjectStream.
   */
  public void testStop() {
    /*
    System.out.println("stop");
    Process4ObjectStream instance = null;
    instance.stop();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }

  /**
   * Test of logStream method, of class Process4ObjectStream.
   */
  public void testLogStream() {
    /*
    System.out.println("logStream");
    InputStream stream = null;
    Level level = null;
    Process4ObjectStream instance = null;
    instance.logStream(stream, level);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }

  /**
   * Test of need2start method, of class Process4ObjectStream.
   */
  public void testNeed2start() {
    /*
    System.out.println("need2start");
    Process4ObjectStream instance = null;
    boolean expResult = false;
    boolean result = instance.need2start();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }

  /**
   * Test of setupInteraction method, of class Process4ObjectStream.
   */
  public void testSetupInteraction() {
    /*
    System.out.println("setupInteraction");
    Process4ObjectStream instance = null;
    instance.setupInteraction();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }

  /**
   * Test of stopInteraction method, of class Process4ObjectStream.
   */
  public void testStopInteraction() {
    /*
    System.out.println("stopInteraction");
    Process4ObjectStream instance = null;
    instance.stopInteraction();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
    */
  }
  
}
