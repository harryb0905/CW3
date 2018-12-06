package com.harrybaines.scc311.auctioningsystem.Client;

import com.harrybaines.scc311.auctioningsystem.server.IAuctionServer;
import com.harrybaines.scc311.auctioningsystem.utils.Constants;

import java.rmi.Naming;

public class RpcRmiClient {

  public static void main(String args[]) throws Exception {
    IAuctionServer server = (IAuctionServer) Naming.lookup(Constants.REGISTRY_URL);
    System.out.println("[CLIENT] Response: " + server.getRandom(10, 100));
    System.out.println("[CLIENT] Response: " + server.getRandom(10, 100));
    System.out.println("[CLIENT] Response: " + server.getRandom(10, 100));
  }
}