package com.group1.bidding_system.models;

import java.io.Serializable;
import java.util.Map;

public class Item implements Serializable {
    public String itemId, itemName;

    public Item() {

    }

    public Item(Map itemMap) {
        this.itemId = (String) itemMap.get("id");
        this.itemName = (String) itemMap.get("name");
    }

    public Item(String itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }
}
