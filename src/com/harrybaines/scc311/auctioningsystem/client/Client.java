package com.harrybaines.scc311.auctioningsystem.client;

import com.harrybaines.scc311.auctioningsystem.utils.Constants;
import com.harrybaines.scc311.auctioningsystem.utils.SecurityManager;
import com.harrybaines.scc311.auctioningsystem.utils.StateManager;
import com.harrybaines.scc311.auctioningsystem.server.*;
import java.rmi.*;
import java.io.*;
import java.util.Map;
import java.security.*;
import java.security.spec.*;

/**
 * Code: Client Bidder/Seller   Client.java
 * Date: 26/11/18
 *
 * A Java RMI Client program to allow a client to place bids on
 * auctions provided in an auctioning server, open auctions,
 * close auctions and browse the list of currently active auctions.
 * The client authenticates with the auctioning server using
 * a 5-stage challenge response protocol.
 *
 * Generate a new key pair:
 * SecurityManager.generateKeyPair(Constants.USERS_DIR_CLIENT + email, String.format(Constants.CLIENT_PUBLIC_KEY_PATH, email), String.format(Constants.CLIENT_PRIVATE_KEY_PATH, email));
 * @author Harry Baines
 */
public class Client {

  private IAuctionServer auctionServer;  /* Server stub */
  private User clientUser;
  private BufferedReader reader;

  /**
   * Constructor to connect to the service on the rmiregistry.
   * The user can enter their email and using their private key they can authenticate with the auctioning server.
   * A menu of options is then displayed to the user.
   */
  public Client() {
    try {
      // Get reference to remote object via the service in the rmiregistry
      this.auctionServer = (IAuctionServer) Naming.lookup(Constants.REGISTRY_URL);
      System.out.println(this.auctionServer.getRandom(10,20));
      System.out.format(Constants.SERVER_CONNECTED_STR, Constants.REGISTRY_URL);
      // Create new input reader and get user details
      this.reader = new BufferedReader(new InputStreamReader(System.in));
      String email = null;
      while (true) {
        email = this.getStrInput("Email");
        if (this.authenticate(email)) {
          System.out.println(Constants.AUTH_SUCCESS);
          break;
        } else {
          System.out.println(Constants.AUTH_FAILED);
        }
      }

      // Load user state
      this.clientUser = (User) StateManager.loadObject(Constants.USERS_DIR_CLIENT + email + "/" + email);
      if (this.clientUser != null) {
        while (true) {
          this.displayMenu();
        }
      }
    } catch (RemoteException e) {
      System.out.println("RemoteException: " + e);
    } catch (java.rmi.NotBoundException e) {
      System.out.println("ClientBuyer unable to bind to server: " + e);
    } catch (java.net.MalformedURLException e) {
      System.out.println("Malformed URL for server: " + e);
    }
  }

  // ======================================================================= //
  //     AUTHENTICATION METHODS (ASYMMETRIC CHALLENGE RESPONSE PROTOCOL)     //
  // ======================================================================= //

  /**
   * Attempts to authenticate with the server (once verified)
   * by solving the server's challenge on the client.
   * @param email the email of the user to authenticate.
   * @return true if authenticating was successful, false otherwise.
   * @throws RemoteException if an error occurs on the server.
   */
  private boolean authenticate(String email) throws RemoteException {
    // First verify server
    if (this.verifyServer()) {
      AuthChallenge receivedChallenge = this.auctionServer.attemptAuth();
      // Sign received challenge with user's private key
      PrivateKey privKey = this.getPrivateKey(String.format(Constants.CLIENT_PRIVATE_KEY_PATH, email));
      if (privKey != null) {
        byte[] userSigBytes = SecurityManager.signChallenge(receivedChallenge, privKey);
        // Ensure server can verify this signature using user's public key + verify server response
        PublicKey userPubKey = SecurityManager.getPublicKey(String.format(Constants.CLIENT_PUBLIC_KEY_PATH, email));
        if (userPubKey != null) {
          AuthSig clientSig = new AuthSig(userSigBytes, receivedChallenge, userPubKey);
          ServerAuthResponse verifiedRes = this.auctionServer.verifySignature(clientSig);
          return verifiedRes.isSigVerified();
        }
      }
      System.out.println("Authentication unsuccessful - error authenticating with the server");
    }
    return false;
  }

  /**
   * Verifies the server is genuine by challenging the server with a challenge
   * and verifies the returned signature using the server's public key.
   * @return true if the server can be verified, false otherwise.
   * @throws RemoteException if an error occurs on the server.
   */
  private boolean verifyServer() throws RemoteException {
    AuthChallenge clientChallenge = SecurityManager.getChallenge();
    ServerAuthResponse res = this.auctionServer.signChallenge(clientChallenge);
    byte[] servSigBytes = res.getSigBytes();

    // Verify signature using the server's public key
    PublicKey serverPubKey = SecurityManager.getPublicKey(Constants.CLIENT_DIR + Constants.SERVER_PUBLIC_KEY_STR);
    if (serverPubKey != null) {
      boolean verifies = SecurityManager.verifySignature(new AuthSig(servSigBytes, clientChallenge, serverPubKey));
      if (verifies) {
        return true;
      }
    }
    System.out.println("Authentication unsuccessful - error verifying the server");
    return false;
  }

  /**
   * Method to obtain a particular user's private key based on a given file path.
   * @param filepath the path to the private key file.
   * @return the private key object.
   */
  private PrivateKey getPrivateKey(String filepath) {
    try (FileInputStream keyfis = new FileInputStream(filepath)) {
      byte[] encKey = new byte[keyfis.available()];
      keyfis.read(encKey);
      PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
      return KeyFactory.getInstance("DSA", "SUN").generatePrivate(privKeySpec);
    } catch (Exception e) {
      System.out.println("Couldn't get user's private key");
    }
    return null;
  }

  // ================================================== //
  //                AUCTION METHODS                     //
  // ================================================== //

  /**
   * Allows the user to browse the current list of autions
   * once the relevant server method has been invoked.
   */
  private void browseAuctions() {
    try {
      Map<String, AuctionItem> activeAuctions = auctionServer.getActiveAuctions();
      // Browse auctions validation
      if (activeAuctions.size() == 0) {
        System.out.println("\n  No Active Auctions");
      } else {
        // Display each auction in table format
        System.out.print(Constants.AUCTION_MENU_HEAD);
        for (Map.Entry<String, AuctionItem> auction : activeAuctions.entrySet()) {
          AuctionItem auctionItem = auction.getValue();
          String auctionId = auction.getKey();
          Bid highestBid = auctionItem.getHighestBid();
          // Format bid output
          String sellerEmail = auctionItem.getSeller().getEmail();
          String highestBidStr = highestBid != null ? "£" + String.format("%.2f", highestBid.getBidValue()) : "No Bids";
          System.out.format(" | %-36s | %-15s | £%-14.2f | %-15s | %-20s |\n", auctionId, auctionItem.getDesc(), auctionItem.getStartPrice(), highestBidStr, sellerEmail);
        }
        System.out.println(Constants.LINE);
      }
    } catch (RemoteException e) {
      System.out.println("RemoteException browsing auctions in Client: " + e);
      System.exit(0);
    }
  }

  /**
   * Allow a user to create a new auction for an item offered for sale.
   */
  private void createAuction() {
    try {
      // Get user input values
      String description = this.getStrInput("Description");
      double startPrice = this.getDoubleInput("Start Price (£)");
      double reservePrice = this.getDoubleInput("Reserve Price (£)");
      // Validation
      if (reservePrice < startPrice) {
        System.out.format(Constants.RESERVE_LOWER_THAN_START, reservePrice, startPrice);
        return;
      }
      // Create the auction for this user
      ServerResponse res = auctionServer.createAuction(new AuctionItem(startPrice, reservePrice, description, this.clientUser));
      System.out.format(Constants.AUCTION_CREATED, res.getAuctionItem().getId());
    } catch (RemoteException e) {
      System.out.println("RemoteException creating auction in Client: " + e);
      System.exit(0);
    }
  }

  /**
   * Allows a user to close an auction by id.
   */
  private void closeAuction() {
    try {
      String auctionToClose = this.getStrInput("Auction ID");
      ServerResponse res = auctionServer.closeAuction(auctionToClose, this.clientUser);
      AuctionItem itemClosed = res.getAuctionItem();
      int statusCode = res.getStatusCode();
      // Couldn't close the auction
      if (itemClosed == null) {
        System.out.println(String.format(this.getResultString(statusCode), auctionToClose));
      } else {
        // Check if their was a highest bid (higher than reserve) - if so, output that bidders details
        Bid highestBid = itemClosed.getHighestBid();
        if (highestBid == null) {
          System.out.println(String.format(this.getResultString(statusCode), auctionToClose));
        } else {
          User bidder = highestBid.getBidder();
          System.out.println(String.format(this.getResultString(statusCode), bidder.getName(), bidder.getEmail(), itemClosed.getDesc(), itemClosed.getHighestBid().getBidValue()));
        }
      }
    } catch (RemoteException e) {
      System.out.println("RemoteException closing auction in Client: " + e);
      System.exit(0);
    }
  }

  /**
   * Method to allow a user to input a new bid for a given auction item.
   */
  private void makeBid() {
    try {
      String auctionId = this.getStrInput("Auction ID");
      double bidAmount = this.getDoubleInput("Bid");
      ServerResponse res = auctionServer.bid(new Bid(auctionId, this.clientUser, bidAmount));
      System.out.println(String.format(this.getResultString(res.getStatusCode()), auctionId));
    } catch (RemoteException e) {
      System.out.println("RemoteException making bid in Client: " + e);
      System.exit(0);
    }
  }

  /**
   * Displays a list of options to the user.
   */
  private void displayMenu() {
    try {
      System.out.print(Constants.CLIENT_MENU);
      int input = Integer.parseInt(this.reader.readLine());
      switch (input) {
        case 1:
          this.browseAuctions();
          break;
        case 2:
          this.createAuction();
          break;
        case 3:
          this.makeBid();
          break;
        case 4:
          this.closeAuction();
          break;
        case 5:
          System.exit(0);
        default:
          System.out.println("\nPlease enter a valid option");
          break;
      }
    } catch (NumberFormatException e) {
      System.out.println("\nPlease enter an option (1-5)");
    } catch (IOException e) {
      System.out.println("IOException in Client: " + e);
      System.exit(0);
    }
  }

  /**
   * Obtains a String input from a BufferedReader reading from the command line.
   * @param field the name of the field to input.
   * @return a String input variable.
   */
  public String getStrInput(String field) {
    String input = null;
    try {
      while (true) {
        System.out.print("Enter " + field + ": ");
        input = this.reader.readLine().trim();
        if (!input.isEmpty()) {
          break;
        }
      }
    } catch (IOException e) {
      System.out.println("IOException in Client: " + e);
      System.exit(0);
    }
    return input;
  }

  /**
   * Obtains a double input from a BufferedReader reading from the command line.
   * @param field the name of the field to input.
   * @return a double input variable.
   */
  public double getDoubleInput(String field) {
    double input = 0;
    while (true) {
      try {
        System.out.print("Enter " + field + ": ");
        input = Double.parseDouble(this.reader.readLine());
        if (input <= 0) {
          System.out.println("Please enter a value greater than 0");
        } else {
          break;
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid input - please enter a number");
      } catch (IOException e) {
        System.out.println("IOException in Client: " + e);
        System.exit(0);
      }
    }
    return input;
  }

  /**
   * Returns a result string corresponding to the provided status code parameter.
   * @param resultCode the integer code received from the server.
   * @return a human-readable string based on the provided status code.
   */
  public String getResultString(int resultCode) {
    switch (resultCode) {
      case IAuctionServer.CANT_CLOSE_OWN:
        return Constants.CANT_CLOSE_OWN;
      case IAuctionServer.CANT_BID_OWN:
        return Constants.CANT_BID_OWN;
      case IAuctionServer.NO_AUCTION:
        return Constants.NO_AUCTION;
      case IAuctionServer.BID_SMALLER_THAN_START:
        return Constants.BID_SMALLER_THAN_START;
      case IAuctionServer.BID_SMALLER_THAN_HIGH:
        return Constants.BID_SMALLER_THAN_HIGH;
      case IAuctionServer.RESERVE_NOT_MET:
        return Constants.RESERVE_NOT_MET;
      case IAuctionServer.AUCTION_WON:
        return Constants.AUCTION_WON;
      case IAuctionServer.BID_SUCCESSFUL:
        return Constants.BID_SUCCESSFUL;
      case IAuctionServer.AUCTION_CLOSED:
        return Constants.AUCTION_CLOSED;
      default:
        return Constants.INVALID_CODE;
    }
  }

  public static void main(String[] args) {
    new Client();
  }
}