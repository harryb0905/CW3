package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Code: Remote Object Interface   IAuctionServer.java
 * Date: 26/11/18
 *
 * Interface for the remote RMI object.
 * @author Harry Baines
 */
public interface IAuctionServer extends Remote {

  // Result code constants
  public static final int CANT_CLOSE_OWN = 0;
  public static final int CANT_BID_OWN = 1;
  public static final int NO_AUCTION = 2;
  public static final int BID_SMALLER_THAN_START = 3;
  public static final int BID_SMALLER_THAN_HIGH = 4;
  public static final int RESERVE_NOT_MET = 5;
  public static final int AUCTION_WON = 6;
  public static final int BID_SUCCESSFUL = 7;
  public static final int AUCTION_CLOSED = 8;
  public static final int AUCTION_CREATED = 9;

  public String getRandom(int min, int max) throws RemoteException;

  /**
   * Method called by the client once this server has been verified.
   * This method will return a challenge for the client to solve.
   * @return an authentication challenge object.
   * @throws RemoteException if an error occurs on the server.
   */
  public AuthChallenge attemptAuth() throws RemoteException;

  /**
   * Method called by the client or the server to sign a particular challenge object.
   * This method will return a server auth response containing the result of signing.
   * @param challenge the authentication challenge to sign.
   * @return a server auth response object.
   * @throws RemoteException if an error occurs on the server.
   */
  public ServerAuthResponse signChallenge(AuthChallenge challenge) throws RemoteException;

  /**
   * Method called by the client or the server to verify a particular signature object.
   * This method will return a server auth response containing the result of verification.
   * @param authSig the authentication signature to verify.
   * @return a server auth response object.
   * @throws RemoteException if an error occurs on the server.
   */
  public ServerAuthResponse verifySignature(AuthSig authSig) throws RemoteException;

  /**
   * Allows a user to create an auction for a given item for sale.
   * @param auctionItem the auction item offered for sale.
   * @return a server response containing the result of the create auction method.
   * @throws RemoteException if an error occurs on the server.
   */
  public ServerResponse createAuction(AuctionItem auctionItem) throws RemoteException;

  /**
   * Allows a user to close an auction for a given item for sale.
   * @param auctionId the ID of the auction to close.
   * @param user the user who wishes to close this particular auction.
   * @return a server response containing the result of the close auction method.
   * @throws RemoteException if an error occurs on the server.
   */
  public ServerResponse closeAuction(String auctionId, User user) throws RemoteException;

  /**
   * Allows a user to close an auction for a given item for sale.
   * @param bid the Bid object.
   * @return a server response containing the result of the bid method.
   * @throws RemoteException if an error occurs on the server.
   */
  public ServerResponse bid(Bid bid) throws RemoteException;

  /**
   * Allows a user to retrieve all active auctions on offer.
   * @return a Map of auctionId's to auction items in the active auction.
   * @throws RemoteException if an error occurs on the server.
   */
  public Map<String, AuctionItem> getActiveAuctions() throws RemoteException;
}
