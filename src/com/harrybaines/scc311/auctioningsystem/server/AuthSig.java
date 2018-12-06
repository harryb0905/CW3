package com.harrybaines.scc311.auctioningsystem.server;

import java.security.PublicKey;

/**
 * Code: Authentication Signature   AuthSig.java
 * Date: 26/11/18
 *
 * This class represents an authentication signature used in the
 * challenge-response protocol. The signature can be verified and signed
 * with the result serialised to a byte array for communication.
 * @author Harry Baines
*/
public class AuthSig implements java.io.Serializable {

  private byte[] sigBytes;
  private AuthChallenge challenge;
  private PublicKey pubKey;

  /**
   * Constructor to initialise a new authentication signature object with the signature bytes,
   * the authentication challenge used and the public key need to verify the signature.
   * @param sigBytes the serialised signature object.
   * @param challenge the authentication challenge object.
   * @param pubKey the public key used in the signing process.
  */
  public AuthSig(byte[] sigBytes, AuthChallenge challenge, PublicKey pubKey) {
    this.sigBytes = sigBytes;
    this.challenge = challenge;
    this.pubKey = pubKey;
  }

  /** 
   * Accessor to obtain the serialised signature.
   * @return the signature as an array of bytes.
  */
  public byte[] getSigBytes() {
    return this.sigBytes;
  }

  /** 
   * Accessor to obtain the authentication challenge used for this signature.
   * @return the authentication challenge object used.
  */
  public AuthChallenge getChallenge() {
    return this.challenge;
  }

  /** 
   * Accessor to obtain the public key used to verify this signature.
   * @return the public key need to verify this signature.
  */
  public PublicKey getPubKey() {
    return this.pubKey;
  }
}
