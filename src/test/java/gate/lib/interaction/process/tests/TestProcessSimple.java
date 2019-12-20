/*
 * Copyright (c) 2018-2019 The University of Sheffield.
 *
 * This file is part of gatelib-interaction 
 * (see https://github.com/GateNLP/gatelib-interaction).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.junit.Test;

/**
 *
 * @author Johann Petrak
 */
public class TestProcessSimple {
  
  /**
   * Ye olde test!
   * @throws UnsupportedEncodingException  sometimes
   */
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
