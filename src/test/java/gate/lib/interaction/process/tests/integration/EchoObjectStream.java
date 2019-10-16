
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
