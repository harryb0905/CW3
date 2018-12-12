package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;
import com.harrybaines.scc311.auctioningsystem.utils.Constants;
import com.harrybaines.scc311.auctioningsystem.utils.SecurityManager;
import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.*;

import java.io.*;
import java.rmi.RemoteException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code: Cluster Member   ClusterMember.java
 * Date: 07/12/18
 *
 * A Class to represent a cluster member.
 * This member can join an existing cluster.
 * @author Harry Baines
 */

public class ClusterMember extends ReceiverAdapter {

    private final ConcurrentHashMap<String, AuctionItem> auctions = new ConcurrentHashMap<String, AuctionItem>();  /* ConcurrentHashMap of all active auctions */
    private AtomicInteger nextID = new AtomicInteger(1); /* Provides unique identification of auctions */

    private JChannel channel;
    private RpcDispatcher dispatcher;

    /**
     * Allows a user to create an auction for a given item for sale.
     * @param auctionItem the auction item offered for sale.
     * @return a server response containing the result of the create auction method.
     * @throws RemoteException if an error occurs on the server.
     */
    public synchronized ServerResponse createAuction(AuctionItem auctionItem) throws RemoteException {
        String auctionId = nextID.getAndIncrement() + "";
        auctionItem.setId(auctionId);
        this.auctions.put(auctionId, auctionItem);
        System.out.println(String.format(Constants.AUCTION_CREATED, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));
        return (new ServerResponse(IAuctionServer.AUCTION_CREATED, auctionItem));
    }

    /**
     * Allows a user to close an auction for a given item for sale.
     * @param auctionId the ID of the auction to close.
     * @param user the user who wishes to close this particular auction.
     * @return a server response containing the result of the close auction method.
     * @throws RemoteException if an error occurs on the server.
     */
    public synchronized ServerResponse closeAuction(String auctionId, User user) throws RemoteException {
        AuctionItem auction = auctions.get(auctionId);
        // Check if auction exists and only allow seller to close
        if (auction == null) {
            return (new ServerResponse(IAuctionServer.NO_AUCTION, auction));
        } else if (!this.ownsAuction(auction, user.getId())) {
            return (new ServerResponse(IAuctionServer.CANT_CLOSE_OWN, auction));
        }
        AuctionItem auctionItem = auctions.remove(auctionId);
        Bid highestBid = auctionItem.getHighestBid();
        System.out.println(String.format(Constants.AUCTION_CLOSED, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));

        // Indicate if reserve price has not been reached
        double bidAmount = highestBid != null ? highestBid.getBidValue() : -1;
        if (bidAmount == -1 || bidAmount < auctionItem.getReservePrice()) {
            return (new ServerResponse(IAuctionServer.RESERVE_NOT_MET, auctionItem));
        }
        return (new ServerResponse(IAuctionServer.AUCTION_WON, auctionItem));
    }

    /**
     * Allows a user to bid on an auction for a given item for sale.
     * @param bid the Bid object.
     * @return a server response containing the result of the bid method.
     * @throws RemoteException if an error occurs on the server.
     */
    public synchronized ServerResponse bid(Bid bid) throws RemoteException {
        double bidAmount = bid.getBidValue();
        User bidder = bid.getBidder();
        String auctionId = bid.getAuctionId();

        // Check if auction exists and bidder isnt the seller
        AuctionItem auctionItem = auctions.get(auctionId);
        if (auctionItem == null) {
            return (new ServerResponse(IAuctionServer.NO_AUCTION, null));
        } else if (this.ownsAuction(auctionItem, bidder.getId())) {
            return (new ServerResponse(IAuctionServer.CANT_BID_OWN, null));
        }

        // Get highest bid
        Bid highestBid = auctionItem.getHighestBid();
        double startPrice = auctionItem.getStartPrice();
        // Bid validation
        if (bidAmount < startPrice) {
            return (new ServerResponse(IAuctionServer.BID_SMALLER_THAN_START, null));
        } else if (highestBid != null && bidAmount <= highestBid.getBidValue()) {
            return (new ServerResponse(IAuctionServer.BID_SMALLER_THAN_HIGH, null));
        }

        // Create new bid
        auctionItem.setHighestBid(bid);
        System.out.println(String.format(Constants.BID_SUCCESSFUL, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));
        return (new ServerResponse(IAuctionServer.BID_SUCCESSFUL, auctionItem));
    }

    /**
     * Accessor to obtain the list of currently active auctions.
     * @return the list of currently active auctions.
     */
    public ConcurrentHashMap<String, AuctionItem> getActiveAuctions() throws RemoteException {
        return auctions;
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
    public ServerAuthResponse signChallenge(AuthChallenge challenge) throws RemoteException {
        System.out.println("SIGNING ON CLUSTER");
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
     * Method called to connect to the channel on a JGroups cluster.
     * @throws Exception if an error occurs connecting to the channel.
     */
    public void start() throws Exception {
        // Setup and connect to the channel
        this.channel = new JChannel();
        this.dispatcher = new RpcDispatcher(this.channel, this, this, this);
        this.channel.connect(Constants.CLUSTER_NAME);
        System.out.println("conneted to channel");

        this.dispatcher.start();
        this.channel.getState(null, Constants.TIMEOUT);
    }

    /**
     * Obtains the state from an output stream.
     * @param output the output stream containing the object to get.
     * @throws Exception if an error occurs getting the state.
     */
    @Override
    public void getState(OutputStream output) throws Exception {
        System.out.println("getting state");
        ObjectOutputStream out = new ObjectOutputStream(output);
        synchronized (auctions) {
            out.writeObject(auctions);
            out.writeObject(nextID);
        }
    }

    /**
     * Sets the state from a given input stream.
     * @param input the input stream containing the object to set.
     * @throws Exception if an error occurs setting the state.
     */
    @Override
    public void setState(InputStream input) throws Exception {
        System.out.println("setting state");
        ObjectInputStream in = new ObjectInputStream(input);

        Map<String, AuctionItem> newState = (Map<String, AuctionItem>) in.readObject();
        AtomicInteger newIDstart = (AtomicInteger) in.readObject();
        synchronized (auctions) {
            nextID.set(newIDstart.get());
            auctions.clear();
            auctions.putAll(newState);
        }
    }

    /**
     * Receives a message from a channel.
     * @param msg the message object from the channel.
     */
    @Override
    public void receive(Message msg) {
        super.receive(msg);
        System.out.println("New message: " + msg.src() + msg.dest() + msg.getObject());
    }

    /**
     * Main method to start a new cluster member instance.
     * @param args unused.
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        new ClusterMember().start();
    }
}
