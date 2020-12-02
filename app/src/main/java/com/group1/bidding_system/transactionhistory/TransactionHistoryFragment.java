package com.group1.bidding_system.transactionhistory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.group1.bidding_system.R;
import com.group1.bidding_system.currentbids.BidItemAdapter;
import com.group1.bidding_system.models.Transaction;
import com.group1.bidding_system.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class TransactionHistoryFragment extends Fragment {
    String uid = "";
    RecyclerView transactionRecyclerView;
    ArrayList<Transaction> transactions = new ArrayList<>();
    FirebaseFirestore db;
    private FirebaseFunctions mFunctions;



    public TransactionHistoryFragment() {
        // Required empty public constructor
    }


    public static TransactionHistoryFragment newInstance(String param1, String param2) {
        TransactionHistoryFragment fragment = new TransactionHistoryFragment();

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
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        db = FirebaseFirestore.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        transactionRecyclerView = view.findViewById(R.id.transactionHistory_list);

        getHistoryOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    Log.d("Response", task.getResult());

                    try {
                        JSONObject root = new JSONObject(task.getResult());
                        JSONObject history = root.getJSONObject("result");
                        JSONArray transactionArray = history.getJSONArray("history");

                        for(int i = 0; i < transactionArray.length(); i++){
                            JSONObject transactionObj = (JSONObject)transactionArray.get(i);

                            Transaction transaction = new Transaction();

                            transaction.itemName = transactionObj.getString("item");
                            Log.d("Name", transaction.itemName);
                            transaction.price = transactionObj.getDouble("price");
                            transaction.sellerId = transactionObj.getString("sellerId");
                            JSONObject date = transactionObj.getJSONObject("date");

                            long seconds = date.getLong("_seconds");
                            int nanoseconds = date.getInt("_nanoseconds");

                            transaction.date = new Timestamp(seconds, nanoseconds);

                            transactions.add(transaction);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    setTransactionRecyclerView();

                    Toast.makeText(getContext(), "Retrieved transaction history",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "Failed to retrieve transaction history",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public Task<String> getHistoryOnCall(){
        Map<String, String> data = new HashMap<>();

        data.put("uid", uid);

        return mFunctions
                .getHttpsCallable("getHistory")
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

    public void setTransactionRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        transactionRecyclerView.setLayoutManager(layoutManager);

        final TransactionHistoryAdapter ad = new TransactionHistoryAdapter(getContext(),
                android.R.layout.simple_list_item_1, transactions, db);

        transactionRecyclerView.setAdapter(ad);
    }
}