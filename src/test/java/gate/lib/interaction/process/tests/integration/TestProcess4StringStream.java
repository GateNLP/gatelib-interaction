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
package gate.lib.interaction.process.tests.integration;
import gate.lib.interaction.process.pipes.Process4StringStream;
import gate.lib.interaction.process.*;
import gate.lib.interaction.process.tests.Utils;

import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 *
 * @author Johann Petrak
 */
public class TestProcess4StringStream {
  
  @Test
  public void testAll() {
    System.err.println("Test Process4StringStream: before creation");
    
    Process4StringStream process = 
            Process4StringStream.create(new File("."), null,
            ("java -cp "+Utils.getClassPath()+                    
             " gate.lib.interaction.process.tests.integration.EchoStream")
                    .split("\\s+",-1));
    System.err.println("Test Process4StringStream: after creation");
    assertTrue(process.isAlive());
    System.err.println("Test Process4StringStream: is alive");
    // send something to the echo process
    System.err.println("Test Process4StringStream: before write");
    String ret = (String)process.process("This is the first line");
    System.err.println("Test Process4StringStream: after read, got "+ret);
    // make sure we got it back properly
    assertEquals("This is the first line", ret);
    
    // test another one
    System.err.println("Before sending the second line");
    ret = (String)process.process("1234");
    System.err.println("Got this: "+ret);
    assertEquals("1234",ret);
    // stop process
    System.err.println("Sending the STOP command");
    process.process("STOP");
    process.stop();
    assertFalse(process.isAlive());
            
  }
  
  
}
