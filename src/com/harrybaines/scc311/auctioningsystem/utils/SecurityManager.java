package com.harrybaines.scc311.auctioningsystem.utils;

import com.harrybaines.scc311.auctioningsystem.server.*;
import java.util.Random;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Code: Security Manager   SecurityManager.java
 * Date: 26/11/18
 *
 * The security manager is responsible for providing new challenges
 * for both the client and the server to solve. The manager also provides
 * functionality for signing and verifying digital signatures, generating
 * public and private key pairs and retrieving a public key from disk.
 * @author Harry Baines
*/

public final class SecurityManager {

  public static final Random RAND = new Random();  /* Random number generator */

  /**
   * Method to receive a new authentication challenge object based on a random number.
   * @return a new challenge object.
  */
  public static AuthChallenge getChallenge() {
    return new AuthChallenge(RAND.nextLong());
  }

  /**
   * Signs a given authentication challenge based on the provided private key.
   * The private key is used to initialise the signature object before signing.
   * @param challenge the authentication challenge instance.
   * @param privKey the private key instance.
   * @return the serialised signature object.
  */
  public static byte[] signChallenge(AuthChallenge challenge, PrivateKey privKey) {
    byte[] res = null;
    try {
      Signature sig = Signature.getInstance("SHA1withDSA", "SUN"); 
      sig.initSign(privKey);

      // Supply the Signature object with the challenge + generate the signature (byte array)
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.putLong(challenge.getValue());
      sig.update(buffer.array());
      res = sig.sign();
    } catch (Exception e) {
      System.out.println("Couldn't sign challenge");
    }
    return res;
  }

  /**
   * Method to verify a Signature object (as byte array) and initialize it with the provided public key.
   * The challenge is provided to ensure the signature is valid.
   * @param authSig the authentication signaure object containing the public key, challenge and serialised signature.
   * @return true if the signature is verified, false otherwise.
  */
  public static boolean verifySignature(AuthSig authSig) {
    try {
      Signature sigVer = Signature.getInstance("SHA1withDSA", "SUN");
      sigVer.initVerify(authSig.getPubKey());

      // Verify the data equals the sent challenge
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.putLong(authSig.getChallenge().getValue());
      sigVer.update(buffer.array());
      return sigVer.verify(authSig.getSigBytes());
    } catch (Exception e) {
      System.out.println("Couldn't verify signature");
    }
    return false;
  }

  /**
   * Method to obtain the public key based on the given filepath.
   * @param filepath the path to the public key file as a string.
   * @return the public key object.
  */
  public static PublicKey getPublicKey(String filepath) {
    try (FileInputStream keyfis = new FileInputStream(filepath)) {
      byte[] encKey = new byte[keyfis.available()];  
      keyfis.read(encKey);
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
      KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
      return keyFactory.generatePublic(pubKeySpec);
    } catch (Exception e) {
      System.out.println("Couldn't get public key: " + filepath);
    }
    return null;
  }

  /**
   * Generates a new public/private key pair to a provided filepath.
   * @param filepath the filepath to the location where the public/private keys will be stored (new folder).
   * @param publicKeyPath the path to where the public key will be stored.
   * @param privateKeyPath the path to where the private key will be stored
   * @return true if successful, false otherwise.
  */
  public static boolean generateKeyPair(String filepath, String publicKeyPath, String privateKeyPath) {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
      keyGen.initialize(1024, random);

      KeyPair pair = keyGen.generateKeyPair();
      PublicKey pubKey = pair.getPublic();
      PrivateKey privKey = pair.getPrivate();

      // Make new directory for user
      (new File(filepath)).mkdir();

      // Save the public key in a file
      byte[] pubKeyBytes = pubKey.getEncoded();
      FileOutputStream pubKeyOut = new FileOutputStream(new File(publicKeyPath));
      pubKeyOut.write(pubKeyBytes);
      pubKeyOut.close();

      byte[] privKeyBytes = privKey.getEncoded();
      FileOutputStream privKeyOut = new FileOutputStream(new File(privateKeyPath));
      privKeyOut.write(privKeyBytes);
      privKeyOut.close();
      return true;
    } catch (Exception e) {
      System.err.println("Couldn't generate a key pair");
      return false;
    }
  }
}
