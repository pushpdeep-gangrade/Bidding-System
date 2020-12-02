package com.group1.bidding_system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private NavController navController;
    public FirebaseAuth mAuth;
    private TextView userFullName, userEmail;
    String uid;
    private FirebaseFunctions mFunctions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();



        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);


        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(drawerLayout).build();

        NavigationUI.setupActionBarWithNavController(this,navController, appBarConfiguration);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setNavigationItemSelectedListener(this);

        uid = getIntent().getStringExtra("userId");

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("demo", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d("demo", token.toString());
                        Toast.makeText(MainActivity.this, token.toString(), Toast.LENGTH_SHORT).show();

                        DocumentReference userRef = db.collection("Users").document(uid);

                        userRef
                                .update("deviceToken", token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("demo", "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("demo", "Error updating document", e);
                                    }
                                });

                    }
                });

        Bundle authBundle = new Bundle();
        authBundle.putString("userId", uid);
        navController.setGraph(navController.getGraph(), authBundle);

        Log.d("User ID", uid);

        userFullName = navView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        userEmail = navView.getHeaderView(0).findViewById(R.id.nav_header_nav_user_email);

        //getProfileOnRequest(uid);

        getProfileOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    try {
                        JSONObject root = new JSONObject(task.getResult());
                        JSONObject userObject = root.getJSONObject("result");

                        User user = new User();

                        user.firstName = userObject.getString("fname");
                        user.lastName = userObject.getString("lname");
                        user.email = userObject.getString("email");
                        user.balance = userObject.getDouble("balance");

                        userFullName.setText(user.firstName + " " + user.lastName);
                        userEmail.setText(user.email);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this, "Profile retrieved",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Failed to retrieve profile",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });;



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

    public Task<String> getProfileOnCall(){
        Map<String, String> data = new HashMap<>();

        data.put("uid", uid);

        return mFunctions
                .getHttpsCallable("getProfile")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Object result = task.getResult().getData();
                        Log.d("Result", result.toString());
                        return task.getResult().getData().toString();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        Bundle authBundle = new Bundle();
        authBundle.putString("userId", uid);

        if (item.getItemId() == R.id.nav_logout) {
            mAuth.signOut();

            finish();
        }
        else if(item.getItemId() == R.id.nav_current_bids){
            navController.navigate(R.id.currentBidsFragment, authBundle);

            drawer.closeDrawer(GravityCompat.START);
        }
        else if(item.getItemId() == R.id.nav_post_item){
            navController.navigate(R.id.postItemFragment, authBundle);

            drawer.closeDrawer(GravityCompat.START);
        }
        else if(item.getItemId() == R.id.nav_view_profile){
            navController.navigate(R.id.viewProfileFragment, authBundle);

            drawer.closeDrawer(GravityCompat.START);
        }
        else if(item.getItemId() == R.id.nav_transaction_history){
            navController.navigate(R.id.transactionHistoryFragment, authBundle);

            drawer.closeDrawer(GravityCompat.START);
        }

        return true;

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();

            finish();
        }



        return super.onOptionsItemSelected(item);
    }


    /*public void getProfileOnRequest(String uid){
        String getProfileUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/getProfile";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("uid", uid);

        client.post(getProfileUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);

                            try {
                                JSONObject root = new JSONObject(str);
                                JSONObject userObject = root.getJSONObject("result");


                                User user = new User();

                                user.firstName = userObject.getString("fname");
                                user.lastName = userObject.getString("lname");
                                user.email = userObject.getString("email");
                                user.balance = userObject.getDouble("balance");


                                userFullName.setText(user.firstName + " " + user.lastName);
                                userEmail.setText(user.email);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(MainActivity.this, "Profile data retrieved successfully", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(MainActivity.this, "Unable to load actors", Toast.LENGTH_SHORT).show();
                        String str = "";

                        try {
                            str = new String(responseBody, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        Log.d("demo", str);
                    }
                }
        );

    }*/
}
