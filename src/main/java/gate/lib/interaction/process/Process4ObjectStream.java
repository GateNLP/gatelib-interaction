package gate.lib.interaction.process;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 * Minimalist class for exchanging objects through object streams  with a command line process.
 * <p>
 * NOTE: at the moment, reading from the process can block forever, there is
 * no timeout! 
 */
public class Process4ObjectStream extends ProcessBase
{

  private static final Logger LOGGER = Logger.getLogger(Process4ObjectStream.class.getName());
  
  private final Object synchronizer = new Object();

  private ObjectInputStream ois;
  private ObjectOutputStream oos;
  
  private Process4ObjectStream() {}
  
  /**
   * Factory method to create a process that exchanges ObjectStream data.
   * @param workingDirectory directory to use as a working directory by the process
   * @param env environment variable settings
   * @param command the command to run, as a list of String to the method
   * @return the initialised process instance
   */
  public static Process4ObjectStream create(File workingDirectory, Map<String,String> env,  List<String> command) {
    Process4ObjectStream ret = new Process4ObjectStream();
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
   * Factory method to create a process that exchanges ObjectStream data.
   * @param workingDirectory directory to use as a working directory by the process
   * @param env environment variable settings
   * @param command the command to run, as a list of arguments to the method
   * @return the initialised process instance
   */
  public static Process4ObjectStream create(File workingDirectory, Map<String,String> env,  String... command) {
    Process4ObjectStream ret = new Process4ObjectStream();
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
        oos.writeObject(data);
        oos.flush();
        // if we get an end of file, we return null 
        // This is mainly for the case where the other side got a STOP command
        // and terminated without sending any response back first.
        try {
          return ois.readObject();
        } catch (EOFException eofex) {
          return null;
        }
      }
    } catch (IOException|ClassNotFoundException ex) {
      throw new RuntimeException("Problem when writing to object stream",ex);
    }
  }

  
  /**
   * Check if the external process is running.
   * @return the indicator if running or not
   */
  @Override
  public boolean isAlive() {
    return !need2start();
  }
  
  ///////////////////////////////////////////////////////////////////
  
  

  @Override
  protected void setupInteraction() {
    // NOTE: creating an object input stream will block until the header of 
    // the first object is received so we have to have the convention that
    // both sides need to first send some Hello object.
    // This should be a string that indicates the sending component and version.
    try {
      oos = new ObjectOutputStream(process.getOutputStream());
      oos.writeObject("Hello from Process4ObjectStream v1.0");
    } catch (IOException ex) {
      throw new RuntimeException("Could not create object output stream",ex);
    }
    try {
      InputStream pis = process.getInputStream();
      ois = new ObjectInputStream(pis);
      try {
        Object ret = ois.readObject();
        System.err.println("Got hello from process: "+ret);
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException("Could not receive the other side's hello object");
      }
    } catch (IOException ex) {
      throw new RuntimeException("Could not create object input stream",ex);      
    }
    copyStream(process.getErrorStream(), System.out);
  }

  @Override
  protected void stopInteraction() {
    stopRequested = true;
    try {
      ois.close();
    } catch (IOException ex) {
      //ignore
    }
    try {
      oos.close();
    } catch (IOException ex) {
      //ignore
    }
  }
  
  ////////////////////////////////////////////////

  /**
   * Main method for testing.
   * @param args not used
   */
  
  public static void main(String[] args) {
    System.err.println("Running the Process4ObjectStream class");
    Process4ObjectStream pr = 
            Process4ObjectStream.create(new File("."),null,
                    "java -cp target/interaction-1.0-SNAPSHOT.jar gate.lib.interaction.process.EchoObjectStream");
    String someString = "this is some string";
    System.err.println("Right before writing to process");
    Object obj = pr.process(someString);
    System.err.println("Got the object back: "+obj);
    System.err.println("Writing another one (1234)");
    obj = pr.process("1234");
    System.err.println("Got "+obj);
    System.err.println("Shutting down");
    pr.stop();
  }
  
}
