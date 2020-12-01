package com.group1.bidding_system.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BidItem  implements Serializable {
    public Item item;
    public String ownerId;
    public double minFinalbid;
    public Bid winningBid;
    public List<Bid> previousBids;

    public BidItem() {

    }

    public BidItem(Map itemMap) {
        this.item = new Item((HashMap) itemMap.get("item"));
        this.ownerId = (String) itemMap.get("owner");
        this.minFinalbid = Double.parseDouble(String.valueOf(itemMap.get("minfinalbid")));
        this.winningBid = new Bid((HashMap)itemMap.get("winningBid"));
    }

}
