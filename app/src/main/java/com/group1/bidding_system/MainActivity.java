package com.group1.bidding_system;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseDatabase.getInstance().getReference();

        /*
        //Testing Firebase Connection
        Map userMap = new HashMap<>();
        userMap.put("firstname","Ronald");
        userMap.put("lastname","McDonald");
        userMap.put("email","ronmcdon@email.com");
        userMap.put("password","password");
        userMap.put("wallet_balance",200);
        db.child("Users").child("02319553").setValue(userMap);
        */
    }
}
