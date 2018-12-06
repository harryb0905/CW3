package com.harrybaines.scc311.auctioningsystem.server;

import com.harrybaines.scc311.auctioningsystem.client.User;

/**
 * Code: Auction Item   AuctionItem.java
 * Date: 26/11/18
 *
 * A Class to represent an item offered for sale in an auction.
 * @author Harry Baines
*/
public class AuctionItem implements java.io.Serializable {
  
  private String id;
  private double startPrice;
  private double reservePrice;
  private String desc;
  private User seller;
  private Bid highestBid;

  /**
   * Constructor to initialise a new auction item offered for sale.
   * @param startPrice the starting price for this item.
   * @param reservePrice the minimum acceptable price for this item.
   * @param desc the description for this item.
   * @param seller the user who is selling this auction item.
  */
  public AuctionItem(double startPrice, double reservePrice, String desc, User seller) {
    this.desc = desc;
    this.startPrice = startPrice;
    this.reservePrice = reservePrice;
    this.seller = seller;
    this.highestBid = null;
  }

  /**
   * Accessor to obtain the id of this auction item.
   * @return the id of this auction item.
  */ 
  public String getId() {
    return this.id;
  }

  /**
   * Mutator to set the id of this auction item.
   * @param id the id of the auction item to set.
  */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Obtains the description of this auction item for sale.
   * @return the textual description of the auction item.
  */
  public String getDesc() {
    return this.desc;
  }

  /**
   * Obtains the starting price of an auction item offered for sale.
   * @return the start price of the auction item.
  */
  public double getStartPrice() {
    return this.startPrice;
  } 

  /**
   * Obtains the reserve price of an auction item offered for sale.
   * Reserve price is the minimum acceptable price.
   * @return the start price of the auction item.
  */
  public double getReservePrice() {
    return this.reservePrice;
  }

  /** 
   * Accessor to obtain the current highest bid for this auction item.
   * @return the bid containing the highest value along with the bidder.
  */
  public Bid getHighestBid() {
    return this.highestBid;
  }

  /**
   * Mutator to set the new highest bid for this auction item.
   * @param newHighestBid the new highest bid.
  */
  public void setHighestBid(Bid newHighestBid) {
    this.highestBid = newHighestBid;
  }

  /** 
   * Accessor to obtain the owner/seller of this auction item.
   * @return the seller of this auction item.
  */
  public User getSeller() {
    return this.seller;
  }

  /**
   * Accessor to obtain a succint summary of this auction item.
   * @return a string representation of this auction item.
  */
  public String toSummaryString() {
    if (this.highestBid == null) {
      return "No Bids";
    } else {
      double highestBid = this.highestBid.getBidValue();
      return "Start Price: £" + String.format("%.2f", this.startPrice) + ", Description: " + this.desc + ", Highest Bid: £" +
          String.format("%.2f", highestBid) + ", Seller: " + this.seller.getEmail();
    }
  }
}
