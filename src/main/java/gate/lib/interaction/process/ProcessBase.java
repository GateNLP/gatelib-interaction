package gate.lib.interaction.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * Minimalist base class for exchanging objects with a command line process.
 * The various subclasses of this implement more specific ways of how 
 * the data actually gets exchanged with the process.
 */
public abstract class ProcessBase 
{

  private static final Logger LOGGER = Logger.getLogger(ProcessBase.class.getName());
  
  List<String> command = new ArrayList<String>();
  ProcessBuilder builder = null;
  Process process = null;
  File workingDir = new File(".");
  
  /**
   * Make sure the process is running.
   * Returns true if the process was started freshly, false if it was 
   * already running.
   * 
   * @return
   * @throws IOException 
   */
  public boolean ensureProcess() {
    if(need2start()) {
      builder = new ProcessBuilder(command);
      builder.directory(workingDir);
      try {
        process = builder.start();
      } catch (IOException ex) {
        throw new RuntimeException("Could not start the process "+command,ex);
      }
      // copy the standard output of the process to the logger
      logStream(process.getErrorStream(),Level.ERROR);
      // do the class-specific setup of how to interact
      setupInteraction();
      return true;
    } else {
      return false;
    }
  }
  
  
  /**
   * Read an object from the process.
   * This will block until the message is available and may currently 
   * block forever!
   * @return
   * @throws IOException 
   */
  public abstract Object readObject();
  
  
  /**
   * Send an object to the process.
   * @param object 
   */
  public abstract void writeObject(Object message);
  
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
    stopInteraction();
    process.destroy();    
  }
  
  ///////////////////////////////////////////////////////////////////
  
  /**
   * This handles the exact way of how the communication is set up.
   */
  protected abstract void setupInteraction();
  
  protected abstract void stopInteraction();
  
  /**
   * Copy the stream output to the logger using the given logging level.
   * @param stream 
   */
  protected void logStream(final InputStream stream, Level level) {
    // Not sure how to do this yet, probably a thread that copies the 
    // stream to another stream which is our own implementation that
    // actually writes to the logger
    // For now we do nothing at all
    /*
    Thread t = new Thread() {
      public void run() {
        try {
          IOUtils.copy(stream, System.err);
        } catch (IOException ex) {
          LOGGER.error("Could not copy standard error from the process to our own standard error", ex);
        }
      }
    };
    t.setDaemon(true);
    t.start();
    */
  }
  
  protected boolean need2start() {
    boolean ret = false;
    if(builder==null) {
      ret = true;
    } else if(process==null) {
      ret = true;
    } else if(!process.isAlive()) {
      ret = true;
    } else {
      boolean stillRunning = false;
      try {
        int code = process.exitValue();
      } catch(IllegalThreadStateException ex) {
        stillRunning = true;
      }
      if(!stillRunning) ret = true;
    }
    return ret;
  }
  
}
