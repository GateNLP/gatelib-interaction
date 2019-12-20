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
import gate.lib.interaction.process.pipes.Process4JsonStream;
import gate.lib.interaction.process.*;
import gate.lib.interaction.process.tests.Utils;

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
            ("java -cp "+Utils.getClassPath()+
             " gate.lib.interaction.process.tests.integration.EchoStream")
                    .split("\\s+",-1));
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
