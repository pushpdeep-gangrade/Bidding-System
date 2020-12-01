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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group1.bidding_system.models.BidItem;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class BidItemFragment extends Fragment {
    String uid, itemId = "";
    Button makeBid, cancelBid, acceptBid;
    TextView titleText, currentWinnerText;
    EditText bidAmount;
    LinearLayout makeBidLayout;
    FirebaseFirestore db;


    public BidItemFragment() {
        // Required empty public constructor
    }


    public static BidItemFragment newInstance(String param1, String param2) {
        BidItemFragment fragment = new BidItemFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString("userId");
            itemId = getArguments().getString("itemId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bid_item, container, false);

        titleText = view.findViewById(R.id.bidItemF_titleText);
        currentWinnerText = view.findViewById(R.id.bidItemF_currentWinner);
        makeBid = view.findViewById(R.id.bidItemF_makeBidButton);
        cancelBid = view.findViewById(R.id.bidItemF_cancelBidButton);
        acceptBid = view.findViewById(R.id.bidItemF_acceptBidButton);
        makeBidLayout = view.findViewById(R.id.bidItemF_makeBidLayout);
        bidAmount = view.findViewById(R.id.bidItemF_yourBidEdit);

        db = FirebaseFirestore.getInstance();

        makeBidLayout.setVisibility(View.GONE);
        acceptBid.setVisibility(View.GONE);

        setItem();

        makeBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bidOnItemOnRequest();
            }
        });

        acceptBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBidOnRequest();
            }
        });

        cancelBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBidOnRequest();
            }
        });

        return view;
    }

    public void bidOnItemOnRequest(){
        String bidOnItemUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/bidOnItem";
        String amount = bidAmount.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("bidAmount", amount);
        params.put("uid", uid);
        params.put("itemId", itemId);

        client.post(bidOnItemUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);

                            Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), "Unable to post item", Toast.LENGTH_SHORT).show();
                        String str = "";

                        try {
                            str = new String(responseBody, "UTF-8");
                            Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        Log.d("demo", str);
                    }
                }
        );
    }

    public void cancelBidOnRequest(){
        String cancelBidUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/cancelBid";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("uid", uid);
        params.put("itemId", itemId);

        client.post(cancelBidUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);

                            Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), "Unable to post item", Toast.LENGTH_SHORT).show();
                        String str = "";

                        try {
                            str = new String(responseBody, "UTF-8");
                            Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        Log.d("demo", str);
                    }
                }
        );
    }

    public void acceptBidOnRequest(){
        String cancelBidUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/acceptBid";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("uid", uid);
        params.put("itemId", itemId);

        client.post(cancelBidUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);

                            Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), "Unable to post item", Toast.LENGTH_SHORT).show();
                        String str = "";

                        try {
                            str = new String(responseBody, "UTF-8");
                            Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        Log.d("demo", str);
                    }
                }
        );
    }

    public void setItem(){
        db.collection("Items").document(itemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                BidItem bidItem = new BidItem(task.getResult().getData());
                if(uid.equals(bidItem.ownerId)){
                    makeBidLayout.setVisibility(View.GONE);
                    acceptBid.setVisibility(View.VISIBLE);
                }
                else{
                    acceptBid.setVisibility(View.GONE);
                    makeBidLayout.setVisibility(View.VISIBLE);
                }
                titleText.setText(bidItem.item.itemName + " Bid");
                setWinnerName(bidItem.winningBid.bidderId, bidItem);
            }
        });
    }

    public void setWinnerName(String id, final BidItem bidItem){
        Log.d("Owner ID", id);
        db.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = new User(task.getResult().getData());
                currentWinnerText.setText(user.firstName + " " + user.lastName + ": $" + bidItem.winningBid.amount);
            }
        });
    }
}