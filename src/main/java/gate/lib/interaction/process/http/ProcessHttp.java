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
package gate.lib.interaction.process.http;

import java.net.ServerSocket;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import gate.lib.interaction.process.ProcessSimple;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import org.springframework.util.SocketUtils;


/**
 *
 * @author Johann Petrak
 */
public class ProcessHttp {
  /**
   * Possible process states.
   */
  public enum ProcessState {
    /**
     * Before started.
     */
    NOT_STARTED,
    /**
     * Started, expected to be running.
     */
    STARTED,
    /**
     * Ended or after some error.
     */
    ENDED_OR_ABORTED
  }
  // (uses default constructor)
  private String path = "/";
  private int port = 57117;
  private String host = "127.0.0.1";
  private ServerSocket socket = null;
  private String uri = "${host}:${port}${path}";
  private List<String> command;
  private ProcessState state = ProcessState.NOT_STARTED;
  private Map<String,String> env;
  private File workingDir = new File(".");
  private ProcessSimple process;
  
  /**
   * Set the host address.
   * @param host host address
   * @return modified ProcessHttp instance
   */
  public ProcessHttp setHost(String host) {
    this.host = host;
    return this;
  }
  
  private void ensure(ProcessState state) {
    if(this.state != state) {
      throw new RuntimeException("Method cannot be used unless in state "+state);
    }
  }
  
  /**
   * Set the port number to use. 
   * This sets the port to use. Note: the port is not immediately used,
   * so the user should make sure that this port number is not used by
   * any other process on the same system until the server is started. 
   * @param port the port to use
   * @return modified ProcessHttp instance
   */
  public ProcessHttp setPort(int port) {
    ensure(ProcessState.NOT_STARTED);
    try {
      this.port = SocketUtils.findAvailableTcpPort(port, port);
    } catch (Exception ex) {
      state = ProcessState.ENDED_OR_ABORTED;
      throw new RuntimeException("Port not available:"+port);
    }
    return this;
  }
  
  /**
   * Set the working directory of the process to run the command.
   * This is only used if the command is set as well.
   * @param dir the working directory to use
   * @return  modified ProcessHttp instance
   */
  public ProcessHttp setWorkingDir(File dir) {
    ensure(ProcessState.NOT_STARTED);
    workingDir = dir;
    return this;
  }

  /**
   * Set the root path of the server. 
   * @param path the path to use, this should start with a slash
   * @return modified ProcessHttp instance
   */
  public ProcessHttp setPath(String path) {
    ensure(ProcessState.NOT_STARTED);
    this.path = path;
    return this;
  }
  
  
  /**
   * Set the port number to use from a free port in a range.
   * This sets the port to use to some available TCP port from the given range. 
   * Note: the port is not immediately used,
   * so the user should make sure that this port number is not used by
   * any other process on the same system until the server is started. 
   * @param fromport lower range port number
   * @param toport  upper range port number
   * @return modified ProcessHttp instance
   */
  public ProcessHttp setPort(int fromport, int toport) {
    ensure(ProcessState.NOT_STARTED);
    try {
      this.port = SocketUtils.findAvailableTcpPort(fromport, toport);
    } catch (Exception ex) {
      state = ProcessState.ENDED_OR_ABORTED;
      throw new RuntimeException("Port not available from range:"+fromport+"-"+toport, ex);
    }
    return this;
  }
  
  /**
   * Get the port used for communication.
   * @return the port number
   */
  public int getPort() {
    return port;
  }
  
  /**
   * Set the command to run to start the server.
   * This should be a string that can be used to start the server process.
   * The string may contain the placeholder ${port} which will get replaced
   * with the port number.
   * If this is not used prior to the start() method, then the server is
   * expected to have been started by other means already. 
   * <p>
   * IMPORTANT!  The string will split up into the actual command and arguments
   * on whitespace. This means that at the moment, no arguments that contain
   * whitespace can be used in the command!
   * 
   * @param command the command to run to start the server.
   * @return modified ProcessHttp instance
   */
  public ProcessHttp setRunServerCmd(String command) {
    ensure(ProcessState.NOT_STARTED);    
    this.command = Arrays.asList(command.split("\\s+", 0));
    return this;
  }
  
  /**
   * Set the command to run to start the server.
   * This expects a list that contains the actual program to run followed
   * by arguments.
   * @param command a list representing the command 
   * @return modified ProcessHttp instance
   */
  public ProcessHttp setRunServerCmd(List<String> command) {
    ensure(ProcessState.NOT_STARTED);
    this.command = command;
    return this;
  }
  
  /**
   * Start communication with the server. 
   * If a command was set this actually starts the process for running 
   * the server. Otherwise it is expected that server is running.
   * This may also initialise and configure the communication with the server
   * and must always be called once before the actual communication starts.
   * 
   * @return modified ProcessHttp instance
   */
  public ProcessHttp start() {
    ensure(ProcessState.NOT_STARTED);
    if(uri == null || command == null || command.isEmpty() || workingDir == null
            || env == null) {
      state = ProcessState.ENDED_OR_ABORTED;
      throw new RuntimeException("One of uri, command, workingDir or env is null or empty");
    }
    // create a usable URI
    uri = uri.replaceAll("$\\{path\\}", path);
    uri = uri.replaceAll("$\\{port\\}", ""+port);
    uri = uri.replaceAll("$\\{host\\}", host);
    // if we have a command, replace any vars and actually run the command    
    if(command != null) {
      for(int i=0; i<command.size();i++) {
        command.set(i, command.get(i).replaceAll("$\\{path\\}", path));
        command.set(i, command.get(i).replaceAll("$\\{port\\}", ""+port));
        command.set(i, command.get(i).replaceAll("$\\{host\\}", host));    
      }
      // TODO set environment variables before running the command!
      // Start a process to run the server - for now we could use our own
      // ProcessSimple, but eventually maybe use
      // https://github.com/fleipold/jproc or learn from that?
      // NOTE: we run the process and keep control, so we can try to 
      // terminate it
      
      process = ProcessSimple.create(workingDir, env, command);      
    }
    
    state = ProcessState.STARTED;
    return this;
  }
  
  /**
   * Stop the process.
   * @return ProcessHttp instance
   */
  public ProcessHttp stop() {
    ensure(ProcessState.STARTED);
    // !!! ONLY actuall send the stop command to the server if we 
    // started the server ourselves, i.e. if we have a command
    if(command != null) {
      process.stop();
      // TODO: send the stop command over to the server. Who should do this?
    }
    state = ProcessState.ENDED_OR_ABORTED;
    Unirest.shutDown();
    return this;
  }
  
  
  /**
   * Send a string to process, get back a string.
   * This sends the string over to the server and expects back a string 
   * in the body of the response. 
   * 
   * @param toProcess the data to run the command on
   * @param command the server command to invoke, may be null
   * @param parms any additional parameters, may be null
   * @return the processed data, as String
   */
  public String processString(String toProcess, String command, Map<String,String> parms) {
    try {
      HttpResponse<String> response = Unirest.post(uri)
              .header("accept","text/plain")
              .header("content-type","text/plain")
              .body(command)
              .asString();
      return response.getBody();
    } catch (UnirestException ex) {
      throw new RuntimeException("Exception when connecting to the server",ex);
    }
  }

  
}
