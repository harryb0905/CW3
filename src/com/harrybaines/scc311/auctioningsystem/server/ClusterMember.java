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


    public static int generateRandom(int min, int max) {
        System.out.println("[CM] Generating Random");
        return ((new Random()).nextInt(max - min) + min);
    }

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
        synchronized (auctions) {
            Util.objectToStream(auctions, new DataOutputStream(output));
        }
//    super.getState(output);
    }

    @Override
    public void setState(InputStream input) throws Exception {
        System.out.println("setting state");
        Map<String, AuctionItem> newState = (Map<String, AuctionItem>) Util.objectFromStream(new DataInputStream(input));
        synchronized (auctions) {
            auctions.clear();
            auctions.putAll(newState);
        }
        super.setState(input);
    }

    public static void main(String args[]) throws Exception {
        new ClusterMember().start();
    }
}
