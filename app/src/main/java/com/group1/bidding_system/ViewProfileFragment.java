package com.group1.bidding_system;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;


public class ViewProfileFragment extends Fragment {
    //private FirebaseFirestore db;
    String uid = "";
    TextView first;
    TextView last;
    TextView email;
    TextView balance;


    public ViewProfileFragment() {
        // Required empty public constructor
    }


    public static ViewProfileFragment newInstance(String param1, String param2) {
        ViewProfileFragment fragment = new ViewProfileFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

        /*db = FirebaseFirestore.getInstance();

        db.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = new User(task.getResult().getData());
                Log.d("User", user.email);

                TextView first = (TextView) view.findViewById(R.id.viewProfile_firstNameText);
                TextView last = view.findViewById(R.id.viewProfile_lastNameText);
                TextView email = view.findViewById(R.id.viewProfile_emailText);
                TextView balance = view.findViewById(R.id.viewProfile_currentBalance);

                first.setText(user.firstName);
                last.setText(user.lastName);
                email.setText(user.email);
                balance.setText("$" + String.valueOf(user.balance));
            }
        });*/

        first = (TextView) view.findViewById(R.id.viewProfile_firstNameText);
        last = view.findViewById(R.id.viewProfile_lastNameText);
        email = view.findViewById(R.id.viewProfile_emailText);
        balance = view.findViewById(R.id.viewProfile_currentBalance);

        getProfileOnRequest();

        Button addMoney = view.findViewById(R.id.viewProfile_addMoneyButton);
        final EditText moneyAmount = view.findViewById(R.id.viewProfile_addMoneyEdit);

        addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = moneyAmount.getText().toString();

                addBalanceOnRequest(amount, view);
            }
        });


        return view;
    }

    public void getProfileOnRequest(){
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


                                first.setText(user.firstName);
                                last.setText(user.lastName);
                                email.setText(user.email);
                                balance.setText("$" + String.valueOf(user.balance));


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(getContext(), "Profile data retrieved successfully", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), "Unable to load actors", Toast.LENGTH_SHORT).show();
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

    }

    public void addBalanceOnRequest(String amount, final View view){
        String addBalanceUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/addBalance";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("uid", uid);
        params.put("balance", amount);

        client.post(addBalanceUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);
                            getProfileOnRequest();
                            Toast.makeText(getContext(), "Balance updated successfully", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), "Unable to load data", Toast.LENGTH_SHORT).show();
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
    }
}