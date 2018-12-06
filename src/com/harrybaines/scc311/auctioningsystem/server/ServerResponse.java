package com.harrybaines.scc311.auctioningsystem.server;

import java.security.*;

/**
 * Code: Server Response   ServerResponse.java
 * Date: 26/11/18
 *
 * A server response provided by the server once a method has finished computation.
 * The result is a status code along with the auction item involved in that particular method.
 * @author Harry Baines
*/
public class ServerResponse implements java.io.Serializable {

  private int statusCode;
  private AuctionItem auctionItem;

  /** 
   * Constructor to initialise a new server response object.
   * @param statusCode the result code from the calling methods computation.
   * @param auctionItem the auction item involved in the calling methods computation. 
  */
  public ServerResponse(int statusCode, AuctionItem auctionItem) {
    this.statusCode = statusCode;
    this.auctionItem = auctionItem;
  }

  /**
   * Accessor to obtain the status code in this response.
   * @return the status code.
  */
  public int getStatusCode() {
    return this.statusCode;
  }

  /** 
   * Accessor to obtain the auction item in this response.
   * @return the auction item object.
  */
  public AuctionItem getAuctionItem() {
    return this.auctionItem;
  }
}
