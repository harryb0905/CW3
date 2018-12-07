package com.harrybaines.scc311.auctioningsystem.utils;

import com.harrybaines.scc311.auctioningsystem.client.User;
import java.io.*;

/**
 * Code: State Manager   StateManager.java
 * Date: 01/12/18
 *
 * A State Manager class to manage the state of Objects and their data.
 * User objects can be loaded from a file based on a provided filepath.
 * User objects can be saved to a file on disk.
 * @author Harry Baines
*/
public final class StateManager {

  /**
   * Saves a particular Object to the state.
   * @param obj the Object to save.
   * @param filepath the path to the file where the Object should be stored.
   * @return true if Object was saved to the state, false otherwise.
  */
  public static boolean saveObject(Object obj, String filepath) {
    try {
      (new ObjectOutputStream(new FileOutputStream(filepath))).writeObject(obj);
      return true;
    } catch (Exception e) {
      System.out.println("Error - unable to save object to file.");
    }
    return false;
  }

  /**
   * Loads a particular Object based on the provided filepath from the state.
   * @param filepath the path to the file where the Object is located.
   * @return the Object from the state.
  */
  public static Object loadObject(String filepath) {
    Object obj = null;
    try {
      obj = new ObjectInputStream(new FileInputStream(filepath)).readObject();
    } catch (Exception e) {
      System.out.println("Error loading object from: " + filepath + " - object may not exist.");
    }
    return obj;
  }
}