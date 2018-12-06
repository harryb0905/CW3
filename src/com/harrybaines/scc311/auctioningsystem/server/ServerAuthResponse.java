package com.harrybaines.scc311.auctioningsystem.server;

import java.security.*;
import java.io.Serializable;

/**
 * Code: Server Authentication Response   ServerAuthResponse.java
 * Date: 26/11/18
 *
 * A server response during the authentication process. The response consists
 * of the signed signature, the challenge used in the signing process, and whether
 * the signature has been verified yet.
 * @author Harry Baines
*/
public class ServerAuthResponse implements Serializable {

  private byte[] sigBytes;
  private AuthChallenge challenge;
  private boolean sigVerified;

  /** 
   * Constructor to initialise a new server response object during the authentication process.
   * @param sigBytes the serialised signature as an array of bytes.
   * @param challenge the authentication challenge object used.
   * @param sigVerified whether the signature has been verified yet.
   */
  public ServerAuthResponse(byte[] sigBytes, AuthChallenge challenge, boolean sigVerified) {
    this.sigBytes = sigBytes;
    this.challenge = challenge;
    this.sigVerified = sigVerified;
  }

  /**
   * Accessor to verify if the signature has been verified.
   * @return true if verified, false otherwise.
  */
  public boolean isSigVerified() {
    return this.sigVerified;
  }

  /** 
   * Accessor to obtain the serialised signature object.
   * @return the signature as an array of bytes.
  */
  public byte[] getSigBytes() {
    return this.sigBytes;
  }

  /**
   * Accessor to obtain the challenge used during the authentication process.
   * @return the authentication challenge object.
  */
  public AuthChallenge getChallenge() {
    return this.challenge;
  }
}
