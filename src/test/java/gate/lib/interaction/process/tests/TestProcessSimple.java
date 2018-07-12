/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction.process.tests;

import gate.lib.interaction.process.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Johann Petrak
 */
public class TestProcessSimple {
  
  @Test
  public void testAll() throws UnsupportedEncodingException {
    // only run if it looks as if this would be a linux-like OS
    if(!System.getProperty("file.separator").equals("/")) return;
    
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Map<String,String> env = new HashMap<String,String>();
    env.put("ENVVAR", "envvalue");
    ProcessSimple process = 
            ProcessSimple.create(new File("."), env,
            "./src/test/resources/bin/echoenv.sh".split("\\s+",-1));    
    process.stop();
    String out = outContent.toString("UTF8");
    Assert.assertEquals("envvalue\n",out);
  }
  
  
}
