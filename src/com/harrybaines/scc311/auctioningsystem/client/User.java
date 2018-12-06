package com.harrybaines.scc311.auctioningsystem.client;

/**
 * Code: User Implementation   User.java
 * Date: 03/11/18
 *
 * A Class to represent a user who bids in an auction.
 * A user has a name and an email, and represents someone
 * who can create, bid and browse on auction items.
 * @author Harry Baines
*/

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User implements java.io.Serializable {
  
  private String name;
  private String email;
  private String id;

  /**
   * Constructor to initialise a new user object.
   * @param name the name of the user.
   * @param email the email of the user.
  */
  public User(String name, String email) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.email = email;
  }

  /**
   * Accessor to obtain the id of this user.
   * @return the id of this user.
  */
  public String getId() {
    return this.id;
  }

  /**
   * Accessor to obtain the name of this user.
   * @return the name of this user.
  */
  public String getName() {
    return this.name;
  }

  /**
   * Accessor to obtain the email of this user.
   * @return the email of this user.
  */
  public String getEmail() {
    return this.email;
  }
}
