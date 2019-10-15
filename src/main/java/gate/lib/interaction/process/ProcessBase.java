package gate.lib.interaction.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Minimalist base class for implementing subclasses for
 * exchanging objects with a command line process.
 * 
 * The various subclasses of this implement more specific ways of how 
 * the data actually gets exchanged with the process.
 * 
 * The following methods must be implemented: readObject, writeObject,
 * setupInteraction, stopInteraction.
 * 
 * The standard way to use this class then is to use the factory create
 * method to get an instance, which will start the process. 
 * If the instance is one for interacting with the process, use the read/write
 * methods to send data and receive results according to the agreed protocol.
 * When done with the interaction and when the process is not longer needed,
 * call the "stop" method to end it. 
 * 
 * The stop method should NOT get called
 * when there is interaction with the process and a pending result has not
 * yet been retrieved as this may block and cause a deadlock! 
 * Reading from the process when there is nothing available may also cause
 * a deadlock!
 * 
 */
public abstract class ProcessBase 
{

  private static final Logger LOGGER = Logger.getLogger(ProcessBase.class.getName());
  
  protected List<String> command = new ArrayList<>();
  protected ProcessBuilder builder = null;
  protected Process process = null;
  protected File workingDir = new File(".");
  protected Thread loggerThread;
  protected Map<String,String> envvars = new HashMap<>();
  // a flag that we set as soon as termination of the process was requested. 
  // Implementations can use the stream interaction with the process to
  // nicely ask for termination through the stopInteraction() method. 
  protected boolean stopRequested = false;   
    
  /**
   * Make sure the process is running.
   * 
   * Returns true if the process was started freshly, false if it was 
   * already running / we think it is already running.
   * 
   * Throws a runtime exception if anything goes wrong.
   * 
   * @return flag indicating if the process was started (true) or already 
   * running (false)
   */
  public boolean ensureProcess() {
    if(need2start()) {
      // System.err.println("ProcessBase: running command:");
      // for(int i=0; i<command.size();i++) { System.err.println(i+": "+command.get(i)); }
      builder = new ProcessBuilder(command);
      builder.directory(workingDir);
      Map<String,String> env = builder.environment();
      env.putAll(envvars);
      try {
        process = builder.start();
      } catch (IOException ex) {
        throw new RuntimeException("Could not start the process "+command,ex);
      }
      setupInteraction();
      return true;
    } else {
      return false;
    }
  }
  
  
  /**
   * Read an object from the process.
   * 
   * Depending on the implementation, this may block forever!
   * 
   * @return  object read
   */
  public abstract Object readObject();
  
  
  /**
   * Send an object to the process. 
   * 
   * 
   * @param message object to send
   */
  public abstract void writeObject(Object message);
  
  /**
   * Check if the external process is running.
   * 
   * This does not actually check if the process is running but rather
   * returns if we think it is running.
   * 
   * @return  flag
   */
  public boolean isAlive() {
    return !need2start();
  }

  /**
   * Attempt to stop the external process.This tries to end the process in several stages.First, it will 
 call stopInteraction to signal to the process that we want it to end.
   * If the process does not end after some timeout, we try to end it using
 the destroy and destroyForcibly methods, ultimately. 
 There is no guarantee that the process gets removed. 
   * @param timeoutPerStage the timeout in milliseconds per attempt to shut down the process
   * @return the exit value of the process 
   */
  public int stop(int timeoutPerStage) {
    stopRequested = true;
    stopInteraction();
    // wait a little so any pending standard error can still be processed
    try {
      process.waitFor(timeoutPerStage, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      //
    }    
    if(process.isAlive()) {
      process.destroy(); 
    } else {
      return process.exitValue();
    }
    try {
      process.waitFor(timeoutPerStage, TimeUnit.MILLISECONDS);
    } catch(InterruptedException ex) {
      // log this at some point
    }
    if(process.isAlive()) {
      process.destroyForcibly(); 
    } else {
      return process.exitValue();
    }    
    try {
      process.waitFor(timeoutPerStage, TimeUnit.MILLISECONDS);
    } catch(InterruptedException ex) {
      // log this at some point
    }
    if(process.isAlive()) {
      throw new RuntimeException("Could not terminate process");
    } else {
      return process.exitValue();
    }    
  }
  
  /**
   * Try to stop the process and return the exit value.
   * This uses a default timeout of 1000 milliseconds for each phase.
   * @return the exit value
   */
  public int stop() {
    return stop(1000);
  }
  
    /**
     * Copy stream.
     * A utility function for copying a process stream to one of our streams
     * in a separate thread. 
     * 
     * @param processStream to copy
     * @param ourStream where to copy
     */
  
  protected void copyStream(final InputStream processStream, final OutputStream ourStream) {
    loggerThread = new StreamCopier(processStream, ourStream);
    loggerThread.setDaemon(true);
    loggerThread.start();
  }

  /**
   * Helper class for copying a process stream.
   * This class copies the input stream to the output stream in a thread.
   */
  private class StreamCopier extends Thread {
      InputStream stream;
      OutputStream outstream;
      public StreamCopier(InputStream stream, OutputStream outstream) {
        this.stream = stream;
        this.outstream = outstream;
      }
      @Override
      public void run() {
        byte[] buffer = new byte[1024];
        int n;
        while(true) { 
          try {
            n = this.stream.read(buffer);
            // if we have reached EOF, exit the copy loop
            if(n == -1) {
              break;
            }
          } catch (IOException ex) {
            LOGGER.error("Could not copy stream from the process to our own stream", ex);
            break;
          }
          try {
            // if we actually got something in our buffer, write it to our stream
            outstream.write(buffer);
          } catch (IOException ex) {
            LOGGER.error("Could not copy stream from the process to our own stream", ex);
            break;
          }
        }
      }  
  }
  
  
  
  ///////////////////////////////////////////////////////////////////
  
  /**
   * This handles the exact way of how the communication is set up.
   * Needs to get implemented by the implementing subclass.
   */
  protected abstract void setupInteraction();
  
  /**
   * How to end the interaction.
   * Needs to get implemented by the implementing subclass.
   */
  protected abstract void stopInteraction();
  
  
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
  
  /**
   * Does an in-place update of the command to conform to what the OS expects.
   * This is mainly about dealing with commands and arguments that contain spaces for now.
   * On a Windows-like system, everything that contains spaces is surrounded with double quotes.
   * On a Linux-like system, spaces are escaped with a backslash.
   * @param command command
   */
  protected void updateCommand4OS(List<String> command) {
    boolean linuxLike = System.getProperty("file.separator").equals("/");
    boolean windowsLike = System.getProperty("file.separator").equals("\\");
    for(int i=0; i<command.size(); i++) {
      String arg = command.get(i);
      if(arg.contains(" ")) {
        if(linuxLike) {
          // NOTE: although it looked as if we can only get it to work with escaping, it
          // turns out it actually works without the escaping. Not sure why it did not
          // work when I originally tested this. 
          //command.set(i, arg.replaceAll(" ", "\\ "));
        } else if(windowsLike) {
          command.set(i, "\""+arg+"\"");          
        }
      }
    }
    
  }
  
  
}
