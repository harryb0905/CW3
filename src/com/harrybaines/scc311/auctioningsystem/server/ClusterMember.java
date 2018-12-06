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

public class ClusterMember extends ReceiverAdapter {

    private ConcurrentHashMap<String, AuctionItem> auctions = new ConcurrentHashMap<String, AuctionItem>();  /* ConcurrentHashMap of all active auctions */

    private static final String CLUSTER_NAME = "RAND_CLUSTER";
    private static final int TIMEOUT = 5000;
    private JChannel channel;
    private RpcDispatcher dispatcher;


    public static int generateRandom(int min, int max) {
        System.out.println("[CM] Generating Random");
        return ((new Random()).nextInt(max - min) + min);
    }

    public ServerResponse createAuction(AuctionItem auctionItem) throws RemoteException {
        System.out.println("[CM] Creating Auction");
        String auctionId = UUID.randomUUID().toString();
        auctionItem.setId(auctionId);
        this.auctions.put(auctionId, auctionItem);
        System.out.println(String.format(Constants.AUCTION_CREATED, auctionId) + String.format(Constants.AUCTION_SUMMARY, auctionItem.toSummaryString()));
        return (new ServerResponse(IAuctionServer.AUCTION_CREATED, auctionItem));
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
