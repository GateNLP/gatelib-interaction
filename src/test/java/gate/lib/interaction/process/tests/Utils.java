package gate.lib.interaction.process.tests;

import java.util.Locale;

/**
 * Various utility methods needed in the tests.
 * @author Johann Petrak
 */
public class Utils {
  
  /**
   * Check if we are running on windows.
   * 
   * @return true if on Windows, false otherwise
   */
  public static boolean isOsWindows() {
    return System.getProperty("os.name").toLowerCase(Locale.UK).contains("win");
  }
  
  /**
   * Get the class path for running integration tests.
   * @return class path as string
   */
  public static String getClassPath() {
    if(isOsWindows()) {
      return "target/*;target/dependency/*;target/test-classes";
    } else {
      return "target/*:target/dependency/*:target/test-classes";
    }            
  }
  
}
