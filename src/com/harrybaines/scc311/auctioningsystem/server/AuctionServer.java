package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;
import com.harrybaines.scc311.auctioningsystem.utils.Constants;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Code: Auctioning Server   AuctionServer.java
 * Date: 03/11/18
 * <p>
 * A Class to represent an auctioning server.
 * This class instantiates the auction server implementation class
 * and binds the object to the RMI registry.
 *
 * @author Harry Baines
 */

public class AuctionServer{

    private ConcurrentHashMap<String, AuctionItem> auctions;  /* ConcurrentHashMap of all active auctions */

    /**
     * Constructor to initialise a new auctioning server.
     * Binds to naming service (URL of remote service + object reference)
     */
    public AuctionServer() throws RemoteException {
        super();
        this.auctions = new ConcurrentHashMap<String, AuctionItem>();
        setupRMI();
    }

    // Bind to the RMI Registry
    private void setupRMI() {
//    try {
//      Naming.rebind("rmi://localhost/"+SERVICE_NAME, this);
//    } catch(Exception e) {
//      System.out.println("[SERVER] Failed to bind");
//    }
        try {
            // AuctionServerImpl implements IAuctionServer interface (therefore is valid)
            Registry registry = LocateRegistry.createRegistry(Constants.REGISTRY_PORT);
            IAuctionServer auctionServer = new AuctionServerImpl();
            registry.rebind(Constants.SERVICE, auctionServer);
            System.out.println(Constants.SERVER_RUNNING_STR);
        } catch (Exception e) {
            System.out.println("[SERVER] Failed to bind" + e.getMessage());
        }
    }

    /**
     * Main method to instantiate a new auctioning server.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        try {
            new AuctionServer();
        } catch (Exception e) {
            System.out.println("[SERVER] Failed to init server");
        }
    }
}