package gate.lib.interaction.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.commons.io.IOUtils;

/**
 * Minimalist class for exchanging objects through obejct streams  with a command line process.
 * 
 * NOTE: at the moment, reading from the process can block forever, there is
 * no timeout! 
 */
public class Process4ObjectStream extends ProcessBase
{

  private static final Logger LOGGER = Logger.getLogger(Process4ObjectStream.class.getName());
  
  ObjectInputStream ois;
  ObjectOutputStream oos;
  
  public Process4ObjectStream(File workingDirectory,  List<String> command) {
    this.workingDir = workingDirectory;
    this.command.addAll(command);
    ensureProcess();
  }
  public Process4ObjectStream(File workingDirectory,  String... command) {
    this.workingDir = workingDirectory;
    this.command.addAll(Arrays.asList(command));    
    ensureProcess(); 
  }
  public Process4ObjectStream(File workingDirectory, String command) {
    this.workingDir = workingDirectory;
    this.command.addAll(Arrays.asList(command.split("\\s+")));
    ensureProcess(); 
  }
  
  
  
  public Object readObject() {
    try {
      return ois.readObject();
    } catch (Exception ex) {
      throw new RuntimeException("Problem when reading from object stream",ex);
    }
  }
  
  
  /**
   * Send a message to the process.
   * @param object 
   */
  public void writeObject(Object object) {
    try {
      oos.writeObject(object);
      oos.flush();
    } catch (IOException ex) {
      throw new RuntimeException("Problem when writing to object stream",ex);
    }
  }
  
  /**
   * Check if the external process is running.
   * @return 
   */
  public boolean isAlive() {
    return !need2start();
  }
  
  /**
   * Attempt to stop the external process.
   */
  public void stop() {
    try {
      oos.close();
    } catch (IOException ex) {
      // ignore
    }
    try {
      ois.close();
    } catch (IOException ex) {
      // ignore
    }
    process.destroy();    
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
        ois.readObject();
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException("Could not receive the other side's hello object");
      }
    } catch (IOException ex) {
      throw new RuntimeException("Could not create object input stream",ex);      
    }
  }

  @Override
  protected void stopInteraction() {
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
  
  public static void main(String[] args) {
    System.err.println("Running the Process4ObjectStream class");
    Process4ObjectStream pr = new Process4ObjectStream(new File("."),"java -cp target/interaction-1.0-SNAPSHOT.jar gate.lib.interaction.process.EchoObjectStream");
    String someString = "this is some string";
    System.err.println("Right before writing to process");
    pr.writeObject(someString);
    System.err.println("Right before reading from process");
    Object obj = pr.readObject();
    System.err.println("Got the object back: "+obj);
    System.err.println("Writing another one (1234)");
    pr.writeObject("1234");
    System.err.println("Right before reading again");
    obj = pr.readObject();
    System.err.println("Got "+obj);
    System.err.println("Shutting down");
    pr.stop();
  }
  
}
