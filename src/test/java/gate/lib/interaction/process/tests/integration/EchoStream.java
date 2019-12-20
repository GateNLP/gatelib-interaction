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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;

/**
 * A simple process that echoes back the string data it receives. 
 * 
 * This is for testing only.
 * Note: this should probably move into the test packages.
 * 
 * @author Johann Petrak
 */
public class EchoStream {

  /**
   * Simple echo process for testing.
   * Simple process which will run forever and echo whatever it receives on
   * standard input as String to standard output as String
   * @param args not used
   * @throws IOException passes on IOException
   * @throws ClassNotFoundException  passes on ClassNotFoundException
   */
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    ObjectMapper mapper = new ObjectMapper();
    PrintStream oos = new PrintStream(System.out, false, Charset.forName("UTF-8"));
    // this will wait for the hello object header of the other side
    BufferedReader ois = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
    String line;
    while(true) {
      System.err.println("ES: before reading");
      line = ois.readLine();
      if(line==null) {
        System.err.println("Received a null line, terminating");
        break;
      }
      System.err.println("ES: got a line >"+StringEscapeUtils.escapeJson(line)+"<");
      line = line.trim();
      // e terminate if we get the string STOP instead of a JSON object or a json map
      // that contains the key/value cmd/"STOP"
      if(line.equals("STOP") || line.equals("\"STOP\"")) {
        System.err.println("Received STOP signal from string");
        break;
      } else if(line.startsWith("{")) {
        try {
          Map<?,?> m = (Map<?,?>)mapper.readValue(line,Map.class);
          // @SuppressWarnings("unchecked")
          // Map<String,Object> m = mapper.readValue(line,Map.class);
          String val = (String)m.get("cmd");
          if(val != null && val.equals("STOP")) {
            System.err.println("Received the stop signal from JSON");
            break;        
          }
        } catch (IOException ex) {
          // ignore... could have been something that is not actually a JSON map
        }
      }
      System.err.println("Sending back line ..");
      oos.println(line);
      oos.flush();
    }
    System.err.println("Terminating echo");
  }
}
