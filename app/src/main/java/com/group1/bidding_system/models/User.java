package com.group1.bidding_system.models;

import java.io.Serializable;
import java.util.Map;

public class User implements Serializable {
    public String userId, firstName, lastName, email;
    public double balance = 0.0;

    public User() {

    }

    public User(Map userMap) {
        this.userId = (String) userMap.get("userId");
        this.firstName = (String) userMap.get("fname");
        this.lastName = (String) userMap.get("lname");
        this.email = (String) userMap.get("email");
        this.balance = Double.parseDouble(String.valueOf(userMap.get("balance")));
    }

    public User(String userId, String firstName, String lastName, String emailAddress, double balance) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = emailAddress;
        this.balance = balance;
    }
}
