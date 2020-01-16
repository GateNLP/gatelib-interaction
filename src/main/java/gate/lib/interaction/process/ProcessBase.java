/*
 * Copyright (c) 2018-2019 The University of Sheffield.
 *
 * This file is part of gatelib-interaction 
 * (see https://github.com/GateNLP/gatelib-interaction).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package gate.lib.interaction.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Common base class for all ProcessXxxx subclasses.
 * 
 * The various subclasses of this implement more specific ways of how 
 * the data actually gets exchanged with the process.
 * <p>
 * The subclasses are divided by communication protocol (pipe, http, websocket)
 * and within that, by the kind of data to exchange.
 * <p>
 * All the implemented classes only provide support for a very basic communication
 * protocol the follows the following steps:
 * <ul>
 * <li>Start the process (optional for http, websocket)
 * <li>As often as needed: send data and receive a response
 * <li>End the process (optional for http, websocket)
 * </ul>
 * The subclasses provide out-of the box implementations for sending data 
 * and receiving responses that are Strings or objects and through 
 * different kinds of serialisation. 
 * <p> 
 * Each implementing subclass provides their own way for how to construct
 * the process instance and start the process. The instance can then
 * be used to exchange data using the process() method. 
 * 
 */
public abstract class ProcessBase 
{

  /**
   * Our logger instance.
   */
  public transient Logger logger = LoggerFactory.getLogger(this.getClass());
  
  // The command to start the process. not required for all process types!
  // TODO: replace with the commandline representation of apache commons exe!
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
   * Preferred way of interaction: process some data and get back some result.
   * This expects that data is one "unit" of information that can get 
   * read by the process on the other side in a single read, e.g. for 
   * String a single line is usually what should get transferred. 
   * Equally, the response is read as one unit, so the process on the 
   * other side is expected to only send a single unit as a response.
   * Using the writeObject and readObject methods directly can be used
   * for more complex protocols if needed, but is discouraged.
   * 
   * NOTE: if we get an EOF condition when reading the result back from the 
   * process, this returns null. This is mainly intended to gracefully deal
   * with the situation that the process was sent a "STOP" command and
   * shuts down without sending back a response first.
   * 
   * @param data the data to process 
   * @return the response 
   */
  public abstract Object process(Object data);
  
    
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
    loggerThread = new StreamCopierByLine(processStream, ourStream);
    loggerThread.setDaemon(true);
    loggerThread.start();
  }

  /**
   * Helper class for copying a process stream.
   * This class copies the input stream to the output stream using a buffer.
   * NOTE: this class was used in older versions, but if several process
   * streams get copied to a local stream using this class, the output 
   * may get mixed up. The byLine class should work better and is used now. 
   */
  private static class StreamCopierByBuffer extends Thread {
      public transient Logger logger = LoggerFactory.getLogger(this.getClass());
      InputStream stream;
      OutputStream outstream;
      public StreamCopierByBuffer(InputStream stream, OutputStream outstream) {
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
            logger.error("Could not copy stream from the process to our own stream", ex);
            break;
          }
          try {
            // if we actually got something in our buffer, write it to our stream
            outstream.write(buffer);
          } catch (IOException ex) {
            logger.error("Could not copy stream from the process to our own stream", ex);
            break;
          }
        }
      }  
  }
  
  /**
   * Helper class for copying a process stream.
   * This class copies the input stream to the output stream line by line.
   */
  private static class StreamCopierByLine extends Thread {
      OutputStream outstream;
      InputStreamReader isr;
      BufferedReader br;
      public StreamCopierByLine(InputStream instream, OutputStream outstream) {
        this.outstream = outstream;
        try {
          isr = new InputStreamReader(instream, "utf-8");
        } catch (UnsupportedEncodingException ex) {
          throw new RuntimeException("Could not create StreamReader", ex);
        }
        br = new BufferedReader(isr);
      }
      @Override
      public void run() {
        String strLine;
        boolean isFirstLine = true;
        try {
          while( (strLine = br.readLine()) != null){
            if(!isFirstLine) {
              outstream.write("\n".getBytes());
            }
            outstream.write(strLine.getBytes("utf-8"));
            isFirstLine = false;
            outstream.flush();
          }        
        } catch(IOException ex) {
          throw new RuntimeException("Error during stream copy", ex);
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
