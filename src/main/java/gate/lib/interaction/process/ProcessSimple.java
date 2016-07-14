package gate.lib.interaction.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Minimalist class for running an external command. This simple passes on
 * standard output and error to our own process and closes standard input
 * as soon as possible.
 * The read and write methods for this class do nothing.
 */
public class ProcessSimple extends ProcessBase
{

  private static final Logger LOGGER = Logger.getLogger(ProcessSimple.class.getName());
  
  private final Object synchronizer = new Object();

  public ProcessSimple(File workingDirectory,  List<String> command) {
    this.workingDir = workingDirectory;
    this.command.addAll(command);
    updateCommand4OS(this.command);
    ensureProcess();
  }
  public ProcessSimple(File workingDirectory,  String... command) {
    this.workingDir = workingDirectory;
    this.command.addAll(Arrays.asList(command));    
    updateCommand4OS(this.command);
    ensureProcess(); 
  }
  
  

  /**
   * This always returns null for this class.
   * @return 
   */
  public Object readObject() {
    return null;
  }
  
  
  /**
   * Does nothing.
   * @param object 
   */
  public void writeObject(Object object) {
  }
  
  /**
   * Check if the external process is running.
   * @return 
   */
  public boolean isAlive() {
    return !need2start();
  }
  
  ///////////////////////////////////////////////////////////////////
  
  protected void copyStream(final InputStream processStream, final OutputStream ourStream) {
    Thread copyThread = new Thread() {
      public void run() {
        try {
          IOUtils.copy(processStream, ourStream);
        } catch (IOException ex) {
          LOGGER.error("Could not copy stream", ex);
        }
      }
    };
    copyThread.setDaemon(true);
    copyThread.start();
  }
  
  

  @Override
  protected void setupInteraction() {
    copyStream(process.getInputStream(),System.out);
    try {
      process.getOutputStream().close();
    } catch (IOException ex) {
      //
    }
  }

  @Override
  protected void stopInteraction() {
  }
  
  ////////////////////////////////////////////////
  
  public static void main(String[] args) {
    // TODO
  }
  
}