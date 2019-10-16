package gate.lib.interaction.process.pipes;

import gate.lib.interaction.process.ProcessBase;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import java.util.Map;
/**
 * Minimalist class for exchanging String lines
 * 
 * This just sends or retrieves arbitrary lines of String which could be 
 * JSON we already have or want to parse ourself, or anything else. 
 * <p>
 * Note: the string being sent over this connection in either direction should
 * NOT contain any newline character!
 * <p>
 * NOTE: at the moment, reading from the process can block forever, there is
 * no timeout! 
 */
public class Process4StringStream extends ProcessBase
{

  private static final Logger LOGGER = Logger.getLogger(Process4StringStream.class.getName());
  
  private final Object synchronizer = new Object();
  

  private Process4StringStream() {
  }
  
  private BufferedReader ir;
  private PrintStream ps;
  
  /**
   * Factory method to create a process that exchanges strings over stdin/stdout.
   * @param workingDirectory directory to use as a working directory by the process
   * @param env environment variable settings
   * @param command the command to run, as a list of strings
   * @return the initialised process instance
   */
  public static Process4StringStream create(File workingDirectory, Map<String,String> env,  List<String> command) {
    Process4StringStream ret = new Process4StringStream();    
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
   * Factory method to create a process that exchanges Strings over stdin/stdout.
   * @param workingDirectory directory to use as a working directory by the process
   * @param env environment variable settings
   * @param command the command to run, as a list of arguments to the method
   * @return the initialised process instance
   */
  public static Process4StringStream create(File workingDirectory, Map<String,String> env,  String... command) {
    Process4StringStream ret = new Process4StringStream();    
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
  
  
  @Override
  public Object process(Object data) {
    try {
      synchronized(synchronizer) {
        String str = (String)data;
        ps.println(str);
        ps.flush();
        // if we get an end of file, we return null 
        // This is mainly for the case where the other side got a STOP command
        // and terminated without sending any response back first.
        try {
          str = ir.readLine();
          return str;
        } catch (EOFException eofex) {
          return null;
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException("Problem when writing to output connection",ex);
    }
  }
  
  
  /**
   * Check if the external process is running.
   * @return flag 
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
   * Simple main method for testing.
   * @param args not used
   */
  
  public static void main(String[] args) {
    System.err.println("Running the Process4StringStream class");
    Process4StringStream pr = Process4StringStream.create(new File("."),null,"java -cp target/interaction-1.0-SNAPSHOT.jar:target/dependency/* gate.lib.interaction.process.EchoStream");
    //String someString = "this is some string";
    System.err.println("Right before writing to process");
    String ret = (String)pr.process("First line sent over");
    System.err.println("Got the line back: "+ret);
    System.err.println("Writing another one (1234)");
    ret = (String)pr.process("1234");
    System.err.println("Got "+ret);
    System.err.println("Shutting down");
    pr.stop();
  }
  
}
