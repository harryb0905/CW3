package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;
import com.harrybaines.scc311.auctioningsystem.utils.Constants;
import com.harrybaines.scc311.auctioningsystem.utils.SecurityManager;

import com.sun.security.ntlm.Server;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

/**
 * Code: Auctioning Server Implementation   AuctionServerImpl.java
 * Date: 03/11/18
 *
 * A Class to represent an auctioning server.
 * The implementation of all the methods is provided
 * by the methods in the IAcutionServer interface.
 * @author Harry Baines
*/

public class AuctionServerImpl extends UnicastRemoteObject implements IAuctionServer {

  // JGroups
  private static final String CLUSTER_NAME = "RAND_CLUSTER";
  private static final int TIMEOUT = 5000;
  private JChannel channel;
  private RpcDispatcher dispatcher;
  private RequestOptions requestOptions;

  /**
   * Constructor to perform RMI linking and remote object initialisation.
   * @throws RemoteException if an error occurs on the server.
  */
  public AuctionServerImpl() throws RemoteException {
    super();
    //this.auctions = new ConcurrentHashMap<String, AuctionItem>();
    setupCluster();
  }

  private void setupCluster() {
    // Join / Create the JGroups cluster
    try {
      this.channel = new JChannel();
      this.channel.setDiscardOwnMessages(true);
      this.requestOptions = new RequestOptions(ResponseMode.GET_ALL, TIMEOUT);
      this.dispatcher = new RpcDispatcher(this.channel, null);
      this.channel.connect(CLUSTER_NAME);
    } catch(Exception e) {
      System.out.println("[SERVER] Failed to connect to cluster");
    }
  }

  /**
   * Allows a user to create an auction for a given item for sale.
   * @param auctionItem the auction item offered for sale.
   * @return a server response containing the result of the create auction method.
   * @throws RemoteException if an error occurs on the server.
   */
  public ServerResponse createAuction(AuctionItem auctionItem) {
    try {
      System.out.println("[SERVER] CREATING AUCTION");
      RspList<ServerResponse> responses = this.dispatcher.callRemoteMethods(  null,
              "createAuction",
              new Object[]{auctionItem},
              new Class[]{auctionItem.getClass()},
              this.requestOptions );



      return  responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] Failed to get responses");
    }
    return null;
  }

  /**
   * Allows a user to close an auction for a given item for sale.
   * @param auctionId the ID of the auction to close.
   * @param user the user who wishes to close this particular auction.
   * @return a server response containing the result of the close auction method.
   * @throws RemoteException if an error occurs on the server.
   */
  @Override
  public synchronized ServerResponse closeAuction(String auctionId, User user) throws RemoteException {
    AuctionItem auction = null; //auctions.get(auctionId);
    // Check if auction exists and only allow seller to close
    if (auction == null) {
      return (new ServerResponse(NO_AUCTION, auction));
    } else if (!this.ownsAuction(auction, user.getId())) {
      return (new ServerResponse(CANT_CLOSE_OWN, auction));
    }
    AuctionItem auctionItem = null;//auctions.remove(auctionId);
    Bid highestBid = auctionItem.getHighestBid();
    System.out.println(String.format(Constants.AUCTION_CLOSED, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));

    // Indicate if reserve price has not been reached
    double bidAmount = highestBid != null ? highestBid.getBidValue() : -1;
    if (bidAmount == -1 || bidAmount < auctionItem.getReservePrice()) {
      return (new ServerResponse(RESERVE_NOT_MET, auctionItem));
    }
    return (new ServerResponse(AUCTION_WON, auctionItem));
  }

  /**
   * Allows a user to close an auction for a given item for sale.
   * @param bid the Bid object.
   * @return a server response containing the result of the bid method.
   * @throws RemoteException if an error occurs on the server.
   */
  @Override
  public synchronized ServerResponse bid(Bid bid) throws RemoteException {
    double bidAmount = bid.getBidValue();
    User bidder = bid.getBidder();
    String auctionId = bid.getAuctionId();

    // Check if auction exists and bidder isnt the seller
    AuctionItem auctionItem = null;//auctions.get(auctionId);
    if (auctionItem == null) {
      return (new ServerResponse(NO_AUCTION, null));
    } else if (this.ownsAuction(auctionItem, bidder.getId())) {
      return (new ServerResponse(CANT_BID_OWN, null));
    }

    // Get highest bid
    Bid highestBid = auctionItem.getHighestBid();
    double startPrice = auctionItem.getStartPrice();
    // Bid validation
    if (bidAmount < startPrice) {
      return (new ServerResponse(BID_SMALLER_THAN_START, null));
    } else if (highestBid != null && bidAmount <= highestBid.getBidValue()) {
      return (new ServerResponse(BID_SMALLER_THAN_HIGH, null));
    }

    // Create new bid
    auctionItem.setHighestBid(bid);
    System.out.println(String.format(Constants.BID_SUCCESSFUL, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));
    return (new ServerResponse(BID_SUCCESSFUL, auctionItem));
  }
//
  /**
   * Accessor to obtain the list of currently active auctions.
   * @return the list of currently active auctions.
   */
  @Override
  public ConcurrentHashMap<String, AuctionItem> getActiveAuctions() throws RemoteException {
    return null;//auctions;
  }

  /**
   * Method to determine if a given auction id is owned by a particular user by id.
   * @param auctionItem the item of the auction to check.
   * @param userId the id of the user to check.
   * @return true if this user owns the provided auction, false otherwise.
   */
  private boolean ownsAuction(AuctionItem auctionItem, String userId) {
    return auctionItem.getSeller().getId().equals(userId) ? true : false;
  }

  // ================================================================== //
  //  AUTHENTICATION METHODS (ASYMMETRIC CHALLENGE RESPONSE PROTOCOL)   //
  // ================================================================== //

  /**
   * Method called by the client once this server has been verified.
   * This method will return a challenge for the client to solve.
   * @return an authentication challenge object.
   * @throws RemoteException if an error occurs on the server.
   */
  @Override
  public AuthChallenge attemptAuth() throws RemoteException {
    return SecurityManager.getChallenge();
  }

  /**
   * Method called by the client or the server to sign a particular challenge object.
   * This method will return a server auth response containing the result of signing.
   * @param challenge the authentication challenge to sign.
   * @return a server auth response object.
   * @throws RemoteException if an error occurs on the server.
   */
  @Override
  public ServerAuthResponse signChallenge(AuthChallenge challenge) throws RemoteException {
    PrivateKey privKey = this.getPrivateKey();
    try {
      byte[] sigBytes = SecurityManager.signChallenge(challenge, privKey);
      return (new ServerAuthResponse(sigBytes, challenge, false));
    } catch (Exception e) {
      System.out.println("Couldn't sign the authentication challenge");
    }
    return null;
  }

  /**
   * Method called by the client or the server to verify a particular signature object.
   * This method will return a server auth response containing the result of verification.
   * @param authSig the authentication signature to verify.
   * @return a server auth response object.
   * @throws RemoteException if an error occurs on the server.
   */
  @Override
  public ServerAuthResponse verifySignature(AuthSig authSig) throws RemoteException {
    boolean verifies = SecurityManager.verifySignature(authSig);
    return (new ServerAuthResponse(authSig.getSigBytes(), authSig.getChallenge(), verifies));
  }

  /**
   * Method to obtain the server's private key based on a given file path.
   * @return the private key object.
   */
  private PrivateKey getPrivateKey() {
    try (FileInputStream keyfis = new FileInputStream(Constants.SERVER_DIR + Constants.SERVER_PRIVATE_KEY_STR)) {
      byte[] encKey = new byte[keyfis.available()];
      keyfis.read(encKey);
      PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
      return KeyFactory.getInstance("DSA", "SUN").generatePrivate(privKeySpec);
    } catch (Exception e) {
      System.out.println("Couldn't get server private key");
    }
    return null;
  }

  /**
   * Method to register a new user to the system based on the new user id.
   * @param userId the id of the new user.
   * @return true if the user was registered successfully, false otherwise.
   */
  public boolean registerUser(String userId) {
    File f = new File(Constants.USERS_DIR_SERVER + userId);
    return f.exists() && f.isDirectory() ? true : false;
  }
}