package com.group1.bidding_system.currentbids;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.group1.bidding_system.R;
import com.group1.bidding_system.models.BidItem;

import java.util.ArrayList;


public class CurrentBidsFragment extends Fragment {
    String uid = "";
    RecyclerView bidRecyclerView;
    ArrayList<BidItem> bidItemArrayList = new ArrayList<>();
    FirebaseFirestore db;
    private NavController navController;

    public CurrentBidsFragment() {
        // Required empty public constructor
    }


    public static CurrentBidsFragment newInstance(String param1, String param2) {
        CurrentBidsFragment fragment = new CurrentBidsFragment();

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

        View view = inflater.inflate(R.layout.fragment_current_bids, container, false);

        db = FirebaseFirestore.getInstance();

        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        bidRecyclerView = view.findViewById(R.id.currentBids_list);

        getBids();

        return view;
    }

    public void getBids(){
        bidItemArrayList.clear();
        db.collection("Items").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        BidItem bid = new BidItem(document.getData());
                        Log.d("Bid", bid.toString());
                        bidItemArrayList.add(bid);
                    }

                    setBidRecyclerView();
                }
            }
        });
    }

    public void setBidRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        bidRecyclerView.setLayoutManager(layoutManager);

        final BidItemAdapter ad = new BidItemAdapter(getContext(),
                android.R.layout.simple_list_item_1, bidItemArrayList, db, uid, navController);

        bidRecyclerView.setAdapter(ad);
    }


}