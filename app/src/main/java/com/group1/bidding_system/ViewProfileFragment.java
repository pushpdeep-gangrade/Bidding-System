package com.group1.bidding_system;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class ViewProfileFragment extends Fragment {
    private FirebaseFirestore db;
    String uid = "";
    TextView first, last, email, balance;
    private FirebaseFunctions mFunctions;
    Context context = getContext();



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


        first = (TextView) view.findViewById(R.id.viewProfile_firstNameText);
        last = view.findViewById(R.id.viewProfile_lastNameText);
        email = view.findViewById(R.id.viewProfile_emailText);
        balance = view.findViewById(R.id.viewProfile_currentBalance);

        mFunctions = FirebaseFunctions.getInstance();
        db = FirebaseFirestore.getInstance();

        updateProfile();

        setProfile();

        Button addMoney = view.findViewById(R.id.viewProfile_addMoneyButton);
        final EditText moneyAmount = view.findViewById(R.id.viewProfile_addMoneyEdit);

        addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = moneyAmount.getText().toString();

                addBalanceOnCall(amount).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            Log.d("Response", task.getResult());

                            getProfileOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if(task.isSuccessful()){
                                        try {
                                            JSONObject root = new JSONObject(task.getResult());
                                            JSONObject userObject = root.getJSONObject("result");

                                            User user = new User();

                                            user.balance = userObject.getDouble("balance");

                                            Locale locale  = new Locale("en", "UK");
                                            String pattern = "###.00";

                                            DecimalFormat decimalFormat = (DecimalFormat)
                                                    NumberFormat.getNumberInstance(locale);
                                            decimalFormat.applyPattern(pattern);

                                            balance.setText("$" + decimalFormat.format(user.balance));


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(getActivity(), "Profile retrieved", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "Failed to retrieve profile", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });;

                            Toast.makeText(getActivity(), "Money added successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getActivity(), "Failed to add money", Toast.LENGTH_SHORT).show();
                        }
                    }
                });;
            }
        });


        return view;
    }

    public void setProfile(){
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


                        first.setText(user.firstName);
                        last.setText(user.lastName);
                        email.setText(user.email);

                        Locale locale  = new Locale("en", "UK");
                        String pattern = "###.00";

                        DecimalFormat decimalFormat = (DecimalFormat)
                                NumberFormat.getNumberInstance(locale);
                        decimalFormat.applyPattern(pattern);

                        balance.setText("$" + decimalFormat.format(user.balance));

                        Toast.makeText(getActivity(), "Profile retrieved",
                                Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                else{
                    Toast.makeText(getActivity(), "Failed to retrieve profile",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });;
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

    public Task<String> addBalanceOnCall(String amount){
        Map<String, String> data = new HashMap<>();

        data.put("uid", uid);
        data.put("balance", amount);

        return mFunctions
                .getHttpsCallable("addBalance")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = task.getResult().getData().toString();
                        Log.d("Result", result.toString());
                        return task.getResult().getData().toString();
                    }
                });
    }

    public void updateProfile(){
        db.collection("Users").document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                setProfile();
            }
        });
    }

   /* public void getProfileOnRequest(){
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

                                Locale locale  = new Locale("en", "UK");
                                String pattern = "###.00";

                                DecimalFormat decimalFormat = (DecimalFormat)
                                        NumberFormat.getNumberInstance(locale);
                                decimalFormat.applyPattern(pattern);

                                balance.setText("$" + decimalFormat.format(user.balance));


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

        Locale locale  = new Locale("en", "UK");
        String pattern = "###.##";

        DecimalFormat decimalFormat = (DecimalFormat)
                NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);

        params.put("uid", uid);
        params.put("balance", decimalFormat.format(Double.parseDouble(amount)));

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
    }*/
}