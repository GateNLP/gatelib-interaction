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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimalist class for running an external command. 
 * 
 * This simply passes on standard output and error to our own process and 
 * closes standard input as soon as possible.
 * The read and write methods for this class do nothing.
 */
public class ProcessSimple extends ProcessBase
{

  /**
   * Our logger instance.
   */
  public transient Logger logger = LoggerFactory.getLogger(this.getClass());
  
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
   * This does nothing and returns null.
   * 
   * @return always returns null
   */
  @Override
  public Object process(Object data) {
    return null;
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
