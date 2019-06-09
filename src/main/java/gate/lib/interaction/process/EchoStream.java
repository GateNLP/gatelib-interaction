
package gate.lib.interaction.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;

/**
 * A simple process that echoes back the string data it receives. 
 * 
 * This is for testing only.
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
    PrintStream oos = new PrintStream(System.out);
    // this will wait for the hello object header of the other side
    BufferedReader ois = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
    String line;
    while(true) {
      System.err.println("ES: before reading");
      line = ois.readLine();
      System.err.println("ES: got a line >"+line+"<");
      if(line==null) {
        System.err.println("Received a null line, terminating");
        break;
      }
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
