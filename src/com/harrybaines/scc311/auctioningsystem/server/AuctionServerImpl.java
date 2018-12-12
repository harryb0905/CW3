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
 * This is the front-end server for the Java RMI-JGroups communication system.
 * The implementation of all the methods is provided
 * by the methods in the IAcutionServer interface.
 * @author Harry Baines
*/

public class AuctionServerImpl extends UnicastRemoteObject implements IAuctionServer {

  private JChannel channel;
  private RpcDispatcher dispatcher;
  private RequestOptions requestOptions;

  /**
   * Constructor to perform RMI linking and remote object initialisation.
   * @throws RemoteException if an error occurs on the server.
  */
  public AuctionServerImpl() throws RemoteException {
    super();
    setupCluster();
  }

  /**
   * Sets up a new JGroups cluster and connects.
   */
  private void setupCluster() {
    try {
      this.channel = new JChannel();
      this.requestOptions = new RequestOptions(ResponseMode.GET_ALL, Constants.TIMEOUT);
      this.dispatcher = new RpcDispatcher(this.channel, new ClusterMember());
      this.channel.connect(Constants.CLUSTER_NAME);
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
  public ServerResponse createAuction(AuctionItem auctionItem) throws RemoteException {
    try {
      System.out.println("[SERVER] CREATING AUCTION");
      RspList<ServerResponse> responses = this.dispatcher.callRemoteMethods(  null,
              "createAuction",
              new Object[]{auctionItem},
              new Class[]{auctionItem.getClass()},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [CREATE AUCTION] Failed to get responses");
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
    try {
      System.out.println("[SERVER] CLOSING AUCTION");
      RspList<ServerResponse> responses = this.dispatcher.callRemoteMethods(  null,
              "closeAuction",
              new Object[]{auctionId, user},
              new Class[]{String.class, User.class},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [CLOSE AUCTION] Failed to get responses");
    }
    return null;
  }

  /**
   * Allows a user to close an auction for a given item for sale.
   * @param bid the Bid object.
   * @return a server response containing the result of the bid method.
   * @throws RemoteException if an error occurs on the server.
   */
  @Override
  public synchronized ServerResponse bid(Bid bid) throws RemoteException {
    try {
      System.out.println("[SERVER] BIDDING");
      RspList<ServerResponse> responses = this.dispatcher.callRemoteMethods(  null,
              "bid",
              new Object[]{bid},
              new Class[]{Bid.class},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [BIDDING] Failed to get responses");
    }
    return null;
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
    try {
      System.out.println("[SERVER] ATTEMPTING AUTH");
      RspList<AuthChallenge> responses = this.dispatcher.callRemoteMethods(  null,
              "attemptAuth",
              new Object[]{},
              new Class[]{},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [ATTEMPTING AUTH] Failed to get responses");
    }
    return null;
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
    try {
      System.out.println("[SERVER] SIGNING AUTH");
      RspList<ServerAuthResponse> responses = this.dispatcher.callRemoteMethods(  null,
              "signChallenge",
              new Object[]{challenge},
              new Class[]{AuthChallenge.class},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [SIGNING AUTH] Failed to get responses");
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
    try {
      System.out.println("[SERVER] VERIFYING SIGNATURE");
      RspList<ServerAuthResponse> responses = this.dispatcher.callRemoteMethods(  null,
              "verifySignature",
              new Object[]{authSig},
              new Class[]{AuthSig.class},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [VERIFYING SIGNATURE] Failed to get responses");
    }
    return null;
  }

  /**
   * Method to obtain the server's private key based on a given file path.
   * @return the private key object.
   */
  private PrivateKey getPrivateKey() {
    try {
      System.out.println("[SERVER] GET PRIVATE KEY");
      RspList<PrivateKey> responses = this.dispatcher.callRemoteMethods(  null,
              "getPrivateKey",
              new Object[]{},
              new Class[]{},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [GET PRIVATE KEY] Failed to get responses");
    }
    return null;
  }

  /**
   * Accessor to obtain the list of currently active auctions.
   * @return the list of currently active auctions.
   */
  public ConcurrentHashMap<String, AuctionItem> getActiveAuctions() throws RemoteException {
    try {
      System.out.println("[SERVER] GET ACTIVE AUCTIONS");
      RspList<ConcurrentHashMap<String, AuctionItem>> responses = this.dispatcher.callRemoteMethods(  null,
              "getActiveAuctions",
              new Object[]{},
              new Class[]{},
              this.requestOptions );
      return responses.getFirst();
    } catch(Exception e) {
      System.out.println("[SERVER] [GET ACTIVE AUCTIONS] Failed to get responses");
    }
    return null;
  }
}