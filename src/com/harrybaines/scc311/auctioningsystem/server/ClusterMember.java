package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;
import com.harrybaines.scc311.auctioningsystem.utils.Constants;
import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.*;

import java.io.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusterMember extends ReceiverAdapter {

    private ConcurrentHashMap<String, AuctionItem> auctions = new ConcurrentHashMap<String, AuctionItem>();  /* ConcurrentHashMap of all active auctions */

    private AtomicInteger nextID = new AtomicInteger(1);

    private static final String CLUSTER_NAME = "RAND_CLUSTER";
    private static final int TIMEOUT = 5000;
    private JChannel channel;
    private RpcDispatcher dispatcher;

    public ServerResponse createAuction(AuctionItem auctionItem) {
        System.out.println("[CM] Creating Auction");
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
        AuctionItem auction = null; //auctions.get(auctionId);
        // Check if auction exists and only allow seller to close
        if (auction == null) {
            return (new ServerResponse(IAuctionServer.NO_AUCTION, auction));
        } else if (!this.ownsAuction(auction, user.getId())) {
            return (new ServerResponse(IAuctionServer.CANT_CLOSE_OWN, auction));
        }
        AuctionItem auctionItem = null;//auctions.remove(auctionId);
        Bid highestBid = auctionItem.getHighestBid();
        System.out.println(String.format(Constants.AUCTION_CLOSED, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));

        // Indicate if reserve price has not been reached
        double bidAmount = highestBid != null ? highestBid.getBidValue() : -1;
        if (bidAmount == -1 || bidAmount < auctionItem.getReservePrice()) {
            return (new ServerResponse(IAuctionServer.RESERVE_NOT_MET, auctionItem));
        }
        return (new ServerResponse(IAuctionServer.AUCTION_WON, auctionItem));
    }


    public void start() throws Exception {
        //this.auctions = new ConcurrentHashMap<String, AuctionItem>();

        // Setup and connect to the channel
        this.channel = new JChannel();
        this.dispatcher = new RpcDispatcher(this.channel, this, this, this);

        this.channel.connect(CLUSTER_NAME);

        this.dispatcher.start();
        this.channel.getState(null,TIMEOUT);
    }

    @Override
    public void receive(Message msg) {
        super.receive(msg);
        System.out.println("New message: " + msg.src() + msg.dest() + msg.getObject());
    }

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
     * Method to determine if a given auction id is owned by a particular user by id.
     * @param auctionItem the item of the auction to check.
     * @param userId the id of the user to check.
     * @return true if this user owns the provided auction, false otherwise.
     */
    private boolean ownsAuction(AuctionItem auctionItem, String userId) {
        return auctionItem.getSeller().getId().equals(userId) ? true : false;
    }

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
        super.setState(input);
    }

    public static void main(String args[]) throws Exception {
        new ClusterMember().start();
    }
}
