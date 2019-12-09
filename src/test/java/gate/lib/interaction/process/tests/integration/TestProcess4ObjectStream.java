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
import gate.lib.interaction.process.pipes.Process4ObjectStream;
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
            "java -cp target/*:target/test-classes  gate.lib.interaction.process.tests.integration.EchoObjectStream".split("\\s+",-1));
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
