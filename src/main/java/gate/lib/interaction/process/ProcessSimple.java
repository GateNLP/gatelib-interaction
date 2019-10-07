package gate.lib.interaction.process;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Minimalist class for running an external command. 
 * 
 * This simply passes on standard output and error to our own process and 
 * closes standard input as soon as possible.
 * The read and write methods for this class do nothing.
 */
public class ProcessSimple extends ProcessBase
{

  private static final Logger LOGGER = Logger.getLogger(ProcessSimple.class.getName());
  
  private ProcessSimple() {} 
  
  /**
   * Factory class to create a process instance.
   * @param workingDirectory directory to use as a working directory by the process.
   * If null, use the current directory.
   * @param env environment variable settings, if null, empty.
   * @param command the command to run, as a list of strings
   * @return the initialised process instance
   */
  public static ProcessSimple create(File workingDirectory, Map<String,String> env,  List<String> command) {
    ProcessSimple ret = new ProcessSimple();
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
   * Factory class to create a process instance.
   * @param workingDirectory directory to use as a working directory by the process
   * @param env environment variable settings
   * @param command the command to run, as additional parameters
   * @return the initialised process instance
   */
  public static ProcessSimple create(File workingDirectory, Map<String,String> env,  String... command) {
    ProcessSimple ret = new ProcessSimple();
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
  
  

  /**
   * This always returns null for this class.
   * @return null
   */
  @Override
  public Object readObject() {
    return null;
  }
  
  
  /**
   * Does nothing.
   * @param object to send
   */
  @Override
  public void writeObject(Object object) {
  }
  
  
  ///////////////////////////////////////////////////////////////////

  

  @Override
  protected void setupInteraction() {
    copyStream(process.getInputStream(),System.out);
    try {
      process.getOutputStream().close();
    } catch (IOException ex) {
      //
    }
    copyStream(process.getErrorStream(), System.err);
  }

  @Override
  protected void stopInteraction() {
  }
  
  
}
