/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.lib.interaction.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.util.SocketUtils;

/**
 *
 * @author johann
 */
public class ProcessHttp {
  public enum ProcessState {
    NOT_STARTED,
    STARTED,
    ENDED_OR_ABORTED
  }
  // (uses default constructor)
  private String path = "/";
  private int port = 57117;
  private String host = "127.0.0.1";
  private ServerSocket socket = null;
  private String uri = "${host}:${port}${path}";
  private String command;
  private ProcessState state = ProcessState.NOT_STARTED;
  private HttpClient httpclient;
  
  
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
   * @param port
   * @return 
   */
  public ProcessHttp setPort(int port) {
    ensure(ProcessState.NOT_STARTED);
    try {
      this.port = SocketUtils.findAvailableTcpPort(port, port);
    } catch (Exception ex) {
      throw new RuntimeException("Port not available:"+port);
    }
    return this;
  }

  /**
   * Set the root path of the server. 
   * @param path the path to use, this should start with a slash
   * @return 
   */
  public ProcessHttp setPath(String path) {
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
   * @return 
   */
  public ProcessHttp setPort(int fromport, int toport) {
    ensure(ProcessState.NOT_STARTED);
    try {
      this.port = SocketUtils.findAvailableTcpPort(fromport, toport);
    } catch (Exception ex) {
      throw new RuntimeException("Port not available from range:"+fromport+"-"+toport);
    }
    return this;
  }
  
  public int getPort() {
    return port;
  }
  
  /**
   * Set the command to run to start the server.
   * This should be a string that can be used to start the server process.
   * The string may contain the placeholder ${port} which will get replaced
   * with the port number.
   * If this is not used prior tp the start() method, then the server is
   * expected to have been started by other means already. 
   * 
   * @param command the command to run to start the server.
   * @return 
   */
  public ProcessHttp setRunServerCmd(String command) {
    ensure(ProcessState.NOT_STARTED);
    this.command = command;
    return this;
  }
  
  public ProcessHttp start() {
    ensure(ProcessState.NOT_STARTED);
    // if we have a command, replace any vars
    if(command != null) {
      command = command.replaceAll("$\\{path\\}", path);
      command = command.replaceAll("$\\{port\\}", ""+port);
      command = command.replaceAll("$\\{host\\}", host);
    
      // TODO set environment variables before running the command!
      // Start a process to run the server - for now we could use our own
      // ProcessSimple, but eventually maybe use
      // https://github.com/fleipold/jproc or learn from that?
      // NOTE: we run the process and keep control, so we can try to 
      // terminate it
    
    }
    
    // try connecting to it with a ping request for some time
    // - create a httpclient for this
    // TODO: consider allowing to add a proxy and other things if we have set that
    uri = uri.replaceAll("$\\{path\\}", path);
    uri = uri.replaceAll("$\\{port\\}", ""+port);
    uri = uri.replaceAll("$\\{host\\}", host);
    
    httpclient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .build();
    try {
      HttpResponse<String> response = httpclient.send(request, BodyHandlers.ofString(java.nio.charset.Charset.forName("UTF-8")));
    } catch (IOException|InterruptedException ex) {
      Logger.getLogger(ProcessHttp.class.getName()).log(Level.SEVERE, null, ex);
      // if we could not do the pinging, something is wrong:
      // try to terminate the server
      // set the state to ended/aborted
      // throw an exception
    }
    // either we are successful or we change the state and abort
    // if everything went well this far, we have state STARTED
    state = ProcessState.STARTED;
    return this;
  }
  
  public ProcessHttp stop() {
    ensure(ProcessState.STARTED);
    // !!! ONLY actuall send the stop command to the server if we 
    // started the server ourselves, i.e. if we have a command
    if(command != null) {
      
    }
    // TODO: send the stop command over to the server
    // may try to ensure that the process stopped
    state = ProcessState.ENDED_OR_ABORTED;
    return this;
  }
  
  
  /**
   * Send a string to process, get back a string, mainly for JSON
   * @param toProcess the data to run the command on
   * @param command the server command to invoke, may be null
   * @param parms any additional parameters, may be null
   * @return the processed data, as String
   */
  public String processString(String toProcess, String command, Map<String,String> parms) {
    // TODO
    // TODO: make sure the string to send does not have new lines - either replace
    // with spaces or escape (could add a set option method for that)
    return "";
  }

  
}
