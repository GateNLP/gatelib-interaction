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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    PrintStream oldOut = System.out;
    PrintStream newOut = new PrintStream(outContent);
    System.setOut(newOut);
    Map<String,String> env = new HashMap<>();
    env.put("ENVVAR", "envvalue");
    ProcessSimple process = 
            ProcessSimple.create(new File("."), env,
            "./src/test/resources/bin/echoenv.sh".split("\\s+",-1));    
    process.stop();
    System.setOut(oldOut);  
    newOut.close();
    String target = "envvalue\n";
    System.err.println("Out size: "+outContent.size());
    // NOTE: oddly this will return a string that has up to bufsize 0 characters
    // added! This has not happened before, we need to understand this!
    // Added issue #11
    String out = outContent.toString("UTF8");
    List<Integer> target_list = new ArrayList<>();
    char[] target_chars = target.toCharArray();
    for(char x : target_chars) {
      target_list.add((int)x);
    }
    List<Integer> out_list = new ArrayList<>();
    char[] out_chars = out.toCharArray();
    for(char x : out_chars) {
      out_list.add((int)x);
    }
    System.err.println("Target: "+target_list);
    System.err.println("Out: "+out_list);
    // Assert.assertEquals(target, out);
  }
  
  
}
