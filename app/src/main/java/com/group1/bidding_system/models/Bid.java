package com.group1.bidding_system.models;

import java.io.Serializable;
import java.util.Map;

public class Bid implements Serializable {
    public double amount;
    public String bidderId;

    public Bid(Map bidMap) {
        this.bidderId = (String) bidMap.get("userId");
        this.amount = Double.parseDouble(String.valueOf(bidMap.get("bidAmount")));
    }

    public Bid(String bidderId, double amount){
        this.amount = amount;
        this.bidderId = bidderId;
    }
}
