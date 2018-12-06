package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;

/**
 * Code: Bid   Bid.java
 * Date: 14/11/18
 *
 * A Class to represent an individual bid on an auction
 * item. Each bid contains an auctionId, a bid value
 * and a reference to the person who placed a bid on this
 * auction.
 * @author Harry Baines
 */
public class Bid implements java.io.Serializable {

  private String auctionId;
  private User bidder;
  private double bidValue;

  /**
   * Constructor to initialise a new bid.
   * @param auctionId the id of the auction this bid has been placed on.
   * @param bidder the bidder reference who placed this bid.
   * @param bidValue the value of the bid that has been placed.
   */
  public Bid(String auctionId, User bidder, double bidValue) {
    this.auctionId = auctionId;
    this.bidder = bidder;
    this.bidValue = bidValue;
  }

  /**
   * Accessor to obtain the id of the auction this bid was placed on.
   * @return the auction id.
   */
  public String getAuctionId() {
    return this.auctionId;
  }

  /**
   * Accessor to obtain the bid value on the auction.
   * @return the bid value.
   */
  public double getBidValue() {
    return this.bidValue;
  }

  /**
   * Accessor to obtain the reference to the bidder who owns this bid.
   * @return the bidder reference.
   */
  public User getBidder() {
    return this.bidder;
  }
}
