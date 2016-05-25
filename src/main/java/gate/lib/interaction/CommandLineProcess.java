package gate.lib.interaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple class for interacting with a process via pipes.
 * The protocol for using this is as follows:
 * <ul>
 * <li>create a new instance of this class, and provide the
 * command to run. Also specify if the reading from the process should
 * be blocking or non-blocking.
 * <li>optionally indicate if non-blocking reading from the process should 
 * be enabled. By default reading will block.
 * <li>Start the process using the ensureProcess() method. This does nothing
 * if the process is already running.
 * <li>Send data to the process using one of the send methods, 
 * <li>retrieve output from process using one of the read methods
 * <li>Query the status of the process (is it still running)
 * <li>If necessary, terminate the process.
 * </ul>
 */
public class CommandLineProcess 
{

  private static final Logger LOGGER = Logger.getLogger(CommandLineProcess.class.getName());
  
  List<String> command = new ArrayList<String>();
  ProcessBuilder builder = null;
  Process process = null;
  File workingDir = new File(".");
  ConcurrentLinkedQueue<String> collectedLines;
  boolean blocking = true;
  BufferedReader reader;
  
  public CommandLineProcess(boolean blocking, List<String> command) {
    this.blocking = blocking;
    this.command.addAll(command);
  }
  public CommandLineProcess(boolean blocking, String... command) {
    this.blocking = blocking;
    this.command.addAll(Arrays.asList(command));    
  }
  public CommandLineProcess(boolean blocking, String command) {
    this.blocking = blocking;
    this.command.addAll(Arrays.asList(command.split("\\s+")));
  }
  
  
  public void setWorkingDirectory(File wDir) {
    workingDir = wDir;
  }
  
  /**
   * Make sure the process is running.
   * Returns true if the process was started freshly, false if it was 
   * already running.
   * 
   * @return
   * @throws IOException 
   */
  public boolean ensureProcess() throws IOException {
    if(need2start()) {
      builder = new ProcessBuilder(command);
      builder.directory(workingDir);
      process = builder.start();
      // copy the standard output of the process to the logger
      logStream(process.getErrorStream(),Level.ERROR);
      if(blocking) {
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      } else {
        collectProcessOutput(process.getInputStream()); 
      }
      return true;
    } else {
      return false;
    }
  }
  
  public String readLine() throws IOException {
    if(!isAlive()) throw new RuntimeException("Attempt to read a line but process is not alive");
    if(blocking) {
      return reader.readLine();
    } else {
      return collectedLines.poll();
    }
  }
  
  
  public boolean isAlive() {
    return !need2start();
  }
  
  public void stop() {
    process.destroy();    
  }
  
  /**
   * Copy the stream output to the logger using the given logging level.
   * @param stream 
   */
  protected void logStream(InputStream stream, Level level) {
    // Not sure how to do this yet, probably a thread that copies the 
    // stream to another stream which is our own implementation that
    // actually writes to the logger
  }
  
  protected void collectProcessOutput(InputStream stream) {
    // TODO
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
