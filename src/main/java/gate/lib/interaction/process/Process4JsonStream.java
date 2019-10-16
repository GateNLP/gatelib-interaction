package gate.lib.interaction.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.*;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;
/**
 * Minimalist class for exchanging objects through JSON
 * 
 * NOTE: at the moment, reading from the process can block forever, there is
 * no timeout! 
 */
public class Process4JsonStream extends ProcessBase
{

  private static final Logger LOGGER = Logger.getLogger(Process4JsonStream.class.getName());
  
  private final Object synchronizer = new Object();
  
  private final ObjectMapper mapper = new ObjectMapper();

  private Process4JsonStream() { }
  
  /**
   * Factory method to create a process that exchanges JSON over stdin/stdout.
   * 
   * @param workingDirectory the directory to use as a working directory
   * @param env environment variable settings
   * @param command the command to run with each part of the command as a separate list element
   * @return the initialised process instance
   */
  public static Process4JsonStream create(File workingDirectory, Map<String,String> env,  List<String> command) {
    Process4JsonStream ret = new Process4JsonStream();    
    if(workingDirectory != null) {
      ret.workingDir = workingDirectory;
    }
    if(env != null) {
      ret.envvars.putAll(env);
    }
    ret.command.addAll(command);
    ret.updateCommand4OS(ret.command);
    ret.ensureProcess();
    return ret;
  }
  
  /**
   * Factory method to create a process that exchanges JSON over stdin/stdout. 
   * 
   * @param workingDirectory the directory to use as a working directory
   * @param env environment variable settings
   * @param command the command to run with each part of the command as a separate argument
   * @return the initialised process instance
   */
  public static Process4JsonStream create(File workingDirectory, Map<String,String> env,  String... command) {
    Process4JsonStream ret = new Process4JsonStream();    
    if(workingDirectory != null) {
      ret.workingDir = workingDirectory;
    }
    if(env != null) {
      ret.envvars.putAll(env);
    }
    ret.command.addAll(Arrays.asList(command));    
    ret.updateCommand4OS(ret.command);
    ret.ensureProcess(); 
    return ret;
    
  }
  
  private BufferedReader ir;
  private PrintStream ps;
  
  @Override
  public Object process(Object data) {
    try {
      synchronized(synchronizer) {
        String json = mapper.writeValueAsString(data);
        ps.println(json);
        ps.flush();
        json = ir.readLine();
        //System.err.println("DEBUG: got json line: "+json);
        while(json != null && !json.trim().startsWith("{")) {
          // System.err.println("DEBUG: Ignoring non-map response: "+json);
          try {
            json = ir.readLine();
          } catch (EOFException eofex) {
            return null;
          }
          // System.err.println("DEBUG: got another line: "+json);
        }
        if(json == null) {
          return null;
        } else {
          Object obj = mapper.readValue(json,Map.class);
          return obj;
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException("Problem when writing to output connection",ex);
    }
  }

  
  
  
  /**
   * Check if the external process is running.
   * @return flag flag
   */
  @Override
  public boolean isAlive() {
    return !need2start();
  }
  
  ///////////////////////////////////////////////////////////////////
  
  

  @Override
  protected void setupInteraction() {
    try {
      //System.err.println("Setting up the Print Stream");
      ps = new PrintStream(process.getOutputStream());
      //ps.println("Hello from Process4ObjectStream v1.0");
    } catch (Exception ex) {
      throw new RuntimeException("Could not create output connection",ex);
    }
    try {
      //System.err.println("Setting up the input stream");
      InputStream pis = process.getInputStream();
      ir = new BufferedReader(new InputStreamReader(pis,"UTF-8"));
      try {
        //String ret = ir.readLine();
        //System.err.println("Got hello from process: "+ret);
      } catch (Exception ex) {
        throw new RuntimeException("Could not receive the other side's hello message");
      }
    } catch (IOException ex) {
      throw new RuntimeException("Could not create input connection",ex);      
    }
    copyStream(process.getErrorStream(), System.out);
    //System.err.println("DONE setting up the interaction");
  }

  @Override
  protected void stopInteraction() {
    stopRequested = true;
    try {
      ir.close();
    } catch (IOException ex) {
      //ignore
    }
    try {
      ps.close();
    } catch (Exception ex) {
      //ignore
    }
  }
  
  ////////////////////////////////////////////////

  /**
   * Simple main for testing.
   * @param args unused
   */
  
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    System.err.println("Running the Process4JsonStream class");
    Process4JsonStream pr = Process4JsonStream.create(new File("."),null,
            "java -cp target/interaction-1.0-SNAPSHOT.jar:target/dependency/* gate.lib.interaction.process.EchoStream");
    //String someString = "this is some string";
    System.err.println("Right before writing to process");
    Map<String,Object> m = new HashMap<>();
    m.put("field1",12);
    m.put("field2","asasa");
    Map<?,?> ret = (Map<?,?>)pr.process(m);
    System.err.println("Got the object back: "+ret);
    System.err.println("Shutting down");
    pr.stop();
  }
  
}
