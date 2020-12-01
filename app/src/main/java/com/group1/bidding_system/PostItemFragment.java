package com.group1.bidding_system;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.group1.bidding_system.models.Item;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;


public class PostItemFragment extends Fragment {
    String uid = "";
    EditText itemName, startingBid, minFinalBid;
    Button postItem;


    public PostItemFragment() {
        // Required empty public constructor
    }


    public static PostItemFragment newInstance(String param1, String param2) {
        PostItemFragment fragment = new PostItemFragment();

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
        View view = inflater.inflate(R.layout.fragment_post_item, container, false);

        itemName = view.findViewById(R.id.postItem_itemName);
        startingBid = view.findViewById(R.id.postItem_startingBid);
        minFinalBid = view.findViewById(R.id.postItem_minFinalBid);
        postItem = view.findViewById(R.id.postItem_postItemButton);

        postItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postItemOnRequest();
            }
        });

        return view;
    }

    public void postItemOnRequest(){
        String postItemUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/postItem";
        String itemNameInput, startingBidInput, minFinalBidInput;

        Locale locale  = new Locale("en", "UK");
        String pattern = "###.##";

        DecimalFormat decimalFormat = (DecimalFormat)
                NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);

        itemNameInput = itemName.getText().toString();
        startingBidInput = decimalFormat.format(Double.parseDouble(startingBid.getText().toString()));
        minFinalBidInput = decimalFormat.format(Double.parseDouble(minFinalBid.getText().toString()));

        String itemId = UUID.randomUUID().toString();

        String item = "{\n" +
                "        \"name\":\"" + itemNameInput + "\",\n" +
                "        \"id\":\"" + itemId + "\"\n" +
                "    }";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        Log.d("Item", item);

        params.put("uid", uid);
        params.put("item", item);
        params.put("startbid", startingBidInput);
        params.put("minfinalbid", minFinalBidInput);


        client.post(postItemUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);

                            Toast.makeText(getContext(), "Item posted successfully", Toast.LENGTH_SHORT).show();
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
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        Log.d("demo", str);
                    }
                }
        );

    }
}