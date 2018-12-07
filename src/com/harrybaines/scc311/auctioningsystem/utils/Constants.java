package com.harrybaines.scc311.auctioningsystem.utils;

import java.util.Collections;

/**
 * Code: Constants file   Constants.java
 * Date: 26/11/18
 *
 * Define relevant code constants for the Java RMI application.
 * @author Harry Baines
*/
public final class Constants {

  // RMI Registry, JGroups and service constants
  public static final String REGISTRY = "rmi://localhost/";
  public static final String SERVICE = "AuctionServer";
  public static final String REGISTRY_URL = REGISTRY + SERVICE;
  public static final int REGISTRY_PORT = 1099;
  public static final String CLUSTER_NAME = "RAND_CLUSTER";
  public static final int TIMEOUT = 5000;

  public static final String USERS_DIR_CLIENT = "src/com/harrybaines/scc311/auctioningsystem/client/users/";
  public static final String USERS_DIR_SERVER = "src/com/harrybaines/scc311/auctioningsystem/server/users/";
  public static final String SERVER_DIR = "src/com/harrybaines/scc311/auctioningsystem/server/";
  public static final String CLIENT_DIR = "src/com/harrybaines/scc311/auctioningsystem/client/";
  public static final String SERVER_PUBLIC_KEY_STR = "serverPublic.key";
  public static final String SERVER_PRIVATE_KEY_STR = "serverPrivate.key";
  public static final String CLIENT_PUBLIC_KEY_PATH = Constants.CLIENT_DIR + "users/%s/public.key";
  public static final String CLIENT_PRIVATE_KEY_PATH = Constants.CLIENT_DIR + "users/%s/private.key";

  // Symbols
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String CHECKMARK = ANSI_GREEN + "\u2713" + ANSI_RESET;
  public static final String CROSS = ANSI_RED + "\u2718" + ANSI_RESET;

  // Output constants
  public static final String CLIENT_MENU = "\nMenu:\n----------\n1: Browse Auctions  2: Create an Auction  3: Bid on Auction  4: Close an Auction  5: Quit\n----------\n> ";
  public static final String SERVER_CONNECTED_STR = "\nConnected to %s " + CHECKMARK + "\n\n";
  public static final String SERVER_RUNNING_STR = "Server running on port " + REGISTRY_PORT + " with name " + SERVICE + " " + CHECKMARK;
  public static final String LINE = "  " + String.join("", Collections.nCopies(115, "-"));
  public static final String AUCTION_MENU_HEAD = "\n" + LINE + String.format("\n | %-36s | %-15s | %-15s | %-15s | %-20s |\n%s\n", "Auction ID", "Description", "Start Price", "Highest Bid", "Seller", LINE);
  public static final String AUCTION_SUMMARY = "  <%s>";
  public static final String INVALID_CODE = "\n  Error - not a valid status code";
  public static final String AUTH_SUCCESS = "\nAuthentication successful " + CHECKMARK;
  public static final String AUTH_FAILED = "\nAuthentication failed " + CROSS + "\n";

  // Auction result code constants
  public static final String CANT_CLOSE_OWN = "\n  Can't close auction " + CROSS + " (you don't own this auction)";
  public static final String CANT_BID_OWN = "\n  Can't bid on auction " + CROSS + " (you own this auction)";
  public static final String NO_AUCTION = "\n  Rejected " + CROSS + " (id: %s doesn't exist)";
  public static final String RESERVE_LOWER_THAN_START = "\n  Couldn't create auction " + CROSS + " (reserve price is lower than the start price)\n";
  public static final String BID_SMALLER_THAN_START = "\n  Bid Rejected " + CROSS + " (bid is smaller than the start price)";
  public static final String BID_SMALLER_THAN_HIGH = "\n  Bid Rejected " + CROSS + " (bid is smaller than or equal to the highest current bid)";
  public static final String RESERVE_NOT_MET = "\n  Auction Closed " + CHECKMARK + "\n  Reserve price hasn't been reached.";
  public static final String AUCTION_WON = "\n  Auction Won: %s (%s) wins %s for £%.2f " + CHECKMARK;
  public static final String BID_SUCCESSFUL = "\n  Bid Successful (id: %s) " + CHECKMARK;
  public static final String AUCTION_CREATED = "\n  Auction Created Successfully (id: %s) " + CHECKMARK + "\n";
  public static final String AUCTION_CLOSED = "\n  Auction Closed Successfully (id: %s) " + CHECKMARK + "\n";
}

