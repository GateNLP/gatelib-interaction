/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction;

import java.io.File;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.log4j.Level;

/**
 *
 * @author johann
 */
public class CommandLineProcessTest extends TestCase {
  
  public CommandLineProcessTest(String testName) {
    super(testName);
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test of setWorkingDirectory method, of class CommandLineProcess.
   */
  public void testSetWorkingDirectory() {
    System.out.println("setWorkingDirectory");
    File wDir = null;
    CommandLineProcess instance = null;
    instance.setWorkingDirectory(wDir);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of ensureProcess method, of class CommandLineProcess.
   */
  public void testEnsureProcess() throws Exception {
    System.out.println("ensureProcess");
    CommandLineProcess instance = null;
    boolean expResult = false;
    boolean result = instance.ensureProcess();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of readLine method, of class CommandLineProcess.
   */
  public void testReadLine() throws Exception {
    System.out.println("readLine");
    CommandLineProcess instance = null;
    String expResult = "";
    String result = instance.readLine();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isAlive method, of class CommandLineProcess.
   */
  public void testIsAlive() {
    System.out.println("isAlive");
    CommandLineProcess instance = null;
    boolean expResult = false;
    boolean result = instance.isAlive();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of stop method, of class CommandLineProcess.
   */
  public void testStop() {
    System.out.println("stop");
    CommandLineProcess instance = null;
    instance.stop();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of logStream method, of class CommandLineProcess.
   */
  public void testLogStream() {
    System.out.println("logStream");
    InputStream stream = null;
    Level level = null;
    CommandLineProcess instance = null;
    instance.logStream(stream, level);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of collectProcessOutput method, of class CommandLineProcess.
   */
  public void testCollectProcessOutput() {
    System.out.println("collectProcessOutput");
    InputStream stream = null;
    CommandLineProcess instance = null;
    instance.collectProcessOutput(stream);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of need2start method, of class CommandLineProcess.
   */
  public void testNeed2start() {
    System.out.println("need2start");
    CommandLineProcess instance = null;
    boolean expResult = false;
    boolean result = instance.need2start();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
}
