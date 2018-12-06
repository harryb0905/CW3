package com.harrybaines.scc311.auctioningsystem.server;

/**
 * Code: Authentication Challenge   AuthChallenge.java
 * Date: 26/11/18
 *
 * This class represents an authentication challenge used in the
 * challenge-response protocol. The challenge is simply a long integer
 * value randomly generated by the source.
 * @author Harry Baines
*/
public class AuthChallenge implements java.io.Serializable {
  private long challenge;

  /**
   * Constructor to initialise a new authentication challenge object.
   * @param value the value of the challenge.
  */
  public AuthChallenge(long value) {
    this.challenge = value;
  }

  /**
   * Accessor to obtain the challenge value.
   * @return the challenge value.
  */
  public long getValue() {
    return this.challenge;
  }
}