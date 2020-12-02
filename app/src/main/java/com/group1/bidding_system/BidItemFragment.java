package com.group1.bidding_system;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.group1.bidding_system.models.BidItem;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class BidItemFragment extends Fragment {
    String uid, itemId = "";
    Button makeBid, cancelBid, acceptBid;
    TextView titleText, currentWinnerText;
    EditText bidAmount;
    LinearLayout makeBidLayout;
    FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    private NavController navController;


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

        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        db = FirebaseFirestore.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        makeBidLayout.setVisibility(View.GONE);
        acceptBid.setVisibility(View.GONE);

        updateBid();

        setItem();

        makeBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bidOnItemOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            Log.d("Response", task.getResult());

                            setItem();

                            Toast.makeText(getContext(), "Bid on item successful",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getContext(), "Failed to bid on item",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });;
            }
        });

        acceptBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //acceptBidOnRequest();
                acceptBidOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            Log.d("Response", task.getResult());

                            Toast.makeText(getContext(), "Bid accepted",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getContext(), "Failed to accept bid",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        cancelBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBidOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            Log.d("Response", task.getResult());

                            Toast.makeText(getContext(), "Bid has been canceled", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getContext(), "Failed to cancel bid", Toast.LENGTH_LONG).show();
                        }
                    }
                });;
            }
        });

        return view;
    }


    public Task<String> bidOnItemOnCall(){
        String amount = bidAmount.getText().toString();

        Map<String, String> data = new HashMap<>();


        data.put("bidAmount", amount);
        data.put("uid", uid);
        data.put("itemId", itemId);

        return mFunctions
                .getHttpsCallable("bidOnItem")
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


    public Task<String> cancelBidOnCall(){
        Map<String, String> data = new HashMap<>();

        data.put("uid", uid);
        data.put("itemId", itemId);

        return mFunctions
                .getHttpsCallable("cancelBid")
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

    public Task<String> acceptBidOnCall(){
        Map<String, String> data = new HashMap<>();

        data.put("uid", uid);
        data.put("itemId", itemId);

        return mFunctions
                .getHttpsCallable("acceptBid")
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

    public Task<String> cancelItemOnCall(){
        Map<String, String> data = new HashMap<>();

        data.put("uid", uid);
        data.put("itemId", itemId);

        return mFunctions
                .getHttpsCallable("cancelItem")
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

    public void setItem(){
        db.collection("Items").document(itemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                BidItem bidItem = new BidItem(task.getResult().getData());
                if(uid.equals(bidItem.ownerId)){
                    makeBidLayout.setVisibility(View.GONE);
                    acceptBid.setVisibility(View.VISIBLE);

                    cancelBid.setText("Cancel Item");
                    cancelBid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cancelItemOnCall().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if(task.isSuccessful()){
                                        Log.d("Response", task.getResult());

                                        Toast.makeText(getContext(), "Item has been canceled", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getContext(), "Failed to cancel item", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });

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

    public void updateBid(){
        db.collection("Items").document(itemId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.exists()){
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", uid);
                    navController.navigate(R.id.action_bidItemFragment_to_currentBidsFragment, bundle);

                    showCancelDialog();
                }
            }
        });
    }

    public void showCancelDialog(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        alertDialog.setTitle("Item canceled");
        alertDialog.setMessage("Item bid has been canceled");

        alertDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialog.create();
        alertDialog.show();
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

    /*public void bidOnItemOnRequest(){
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
*/

    /*public void cancelBidOnRequest(){
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
    }*/
    /*public void acceptBidOnRequest(){
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
    }*/

}