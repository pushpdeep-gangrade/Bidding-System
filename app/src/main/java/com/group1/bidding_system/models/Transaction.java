package com.group1.bidding_system.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Map;

public class Transaction implements Serializable {
    public Timestamp date;
    public String itemName, sellerId;
    public double price;

    public Transaction(){

    }

    public Transaction(Map transactionMap) {
        this.itemName = (String) transactionMap.get("item");
        this.date = (Timestamp) transactionMap.get("date");
        this.sellerId = (String) transactionMap.get("sellerId");
        this.price = Double.parseDouble(String.valueOf(transactionMap.get("balance")));
    }

    public Transaction(String itemName, Timestamp date, String sellerId,  double balance) {
        this.itemName = itemName;
        this.date = date;
        this.sellerId = sellerId;
        this.price = balance;
    }
}
