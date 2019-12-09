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

package gate.lib.interaction.process.tests.integration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A simple process that will echo back the object stream data it gets.
 * 
 * This reads object stream data from standard input and echoes the data
 * back to standard output. This is for testing only.
 * 
 * @author Johann Petrak
 */
public class EchoObjectStream {

  /**
   * Simple main method for testing
   * @param args not used
   * @throws IOException passes on IOException
   * @throws ClassNotFoundException passes on ClassNotFoundException
   */
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    ObjectOutputStream oos = new ObjectOutputStream(System.out);
    // Send the hello object
    oos.writeObject("Hello");
    oos.flush();
    // this will wait for the hello object header of the other side
    ObjectInputStream ois = new ObjectInputStream(System.in);
    ois.readObject();
    System.err.println("EOS: BEfore loop");
    while(true) {
      System.err.println("EchoObjectStream: before reading");
      Object obj = ois.readObject();
      System.err.println("EchoObjectStream: got an object, class is "+obj.getClass());
      // we terminate if we get the string STOP
      if(obj.equals("STOP")) {
        System.err.println("Received the stop signal");
        break;
      }
      oos.writeObject(obj);
      oos.flush();
    }
    System.err.println("Terminating echo");
  }
}
