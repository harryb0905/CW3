package com.harrybaines.scc311.auctioningsystem.server;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.Naming;

import com.harrybaines.scc311.auctioningsystem.utils.Constants;
import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.*;

// RMI server can call other servers via JGroups.
// Client connects to a single server via Java RMI to request a random number.
// However, the server does not generate this random number itself, but gets a random 
// number from all cluster members and divides by the total number of responses, finding 
// an average random number.
public class RmiServer extends UnicastRemoteObject implements ServerInterface {

  // JGroups
  private static final String CLUSTER_NAME = "RAND_CLUSTER";
  private static final int TIMEOUT = 1000;
  private JChannel channel;
  private RpcDispatcher dispatcher;
  private RequestOptions requestOptions;

  public RmiServer() throws RemoteException {
    super();
    setupRMI();
    setupCluster();
  }

  private void setupRMI() {
    // Bind to the RMI Registry
    try {
      Naming.rebind(Constants.REGISTRY_URL, this);
    } catch(Exception e) {
      System.out.println("[SERVER] Failed to bind");
    }
  }

  private void setupCluster() {
    // Join / Create the JGroups cluster
    try {
      this.channel = new JChannel();
      this.requestOptions = new RequestOptions(ResponseMode.GET_ALL, TIMEOUT);
      this.dispatcher = new RpcDispatcher(this.channel, new ClusterMember());
      this.channel.connect(CLUSTER_NAME);
    } catch(Exception e) {
      System.out.println("[SERVER] Failed to connect to cluster");
    }
  }

  public String getRandom(int min, int max) throws RemoteException {
    try {
      RspList responses = this.dispatcher.callRemoteMethods(  null,
                                                              "generateRandom",
                                                              new Object[]{min, max},
                                                              new Class[]{int.class, int.class},
                                                              this.requestOptions );
      int value = 0;
      for (Object response : responses.getResults()) {
        value += (int)response;
      }

      return Float.toString(value/responses.size());
    } catch(Exception e) {
      System.out.println("[SERVER] Failed to get responses");
    }
    return null;
  }

  public static void main(String args[]) {
    try {
      new RmiServer();
    } catch(Exception e) {
      System.out.println("[SERVER] Failed to init server");
    }
  }
}