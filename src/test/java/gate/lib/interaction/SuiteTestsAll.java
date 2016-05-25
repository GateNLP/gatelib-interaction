/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author johann
 */
public class SuiteTestsAll extends TestCase {
  
  public SuiteTestsAll(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite("SuiteTestsAll");
    return suite;
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
}
