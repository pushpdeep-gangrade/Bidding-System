package com.group1.bidding_system.currentbids;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group1.bidding_system.R;
import com.group1.bidding_system.models.BidItem;
import com.group1.bidding_system.models.User;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BidItemAdapter extends RecyclerView.Adapter<BidItemViewHolder>  {
    Context context;
    List<BidItem> bidItemArrayList = new ArrayList<>();
    FirebaseFirestore db;
    String uid;
    NavController navController;

    public BidItemAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BidItem> bidItemList
            , FirebaseFirestore db, String uid, NavController navController) {
        this.context = context;
        this.bidItemArrayList = bidItemList;
        this.db = db;
        this.navController = navController;
        this.uid = uid;

        Log.d("Bid List", bidItemArrayList.toString());
    }

    @NonNull
    @Override
    public BidItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bid_item, parent, false);

        BidItemViewHolder vh = new BidItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull BidItemViewHolder holder, int position) {
        final BidItem bidItem = bidItemArrayList.get(position);

        Locale locale  = new Locale("en", "UK");
        String pattern = "###.00";

        DecimalFormat decimalFormat = (DecimalFormat)
                NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);

        TextView itemName = holder.itemView.findViewById(R.id.bidItem_itemName);
        TextView ownerName = holder.itemView.findViewById(R.id.bidItem_ownerName);
        TextView startingBid = holder.itemView.findViewById(R.id.bidItem_startingBid);
        TextView minFinalBid = holder.itemView.findViewById(R.id.bidItem_minFinalBid);

        itemName.setText(bidItem.item.itemName);
        setOwnerName(bidItem.ownerId, ownerName);
        startingBid.setText("$" + decimalFormat.format(bidItem.winningBid.amount));
        minFinalBid.setText("$" + decimalFormat.format(bidItem.minFinalbid));

        Log.d("Bid List", String.valueOf(getItemCount()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", uid);
                bundle.putString("itemId", bidItem.item.itemId);
                navController.navigate(R.id.action_currentBidsFragment_to_bidItemFragment, bundle);
            }
        });


    }

    @Override
    public int getItemCount() {
        return bidItemArrayList.size();
    }

    public void setOwnerName(String id, final TextView tv){
        Log.d("Owner ID", id);
        db.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = new User(task.getResult().getData());
                tv.setText(user.firstName + " " + user.lastName);
            }
        });
    }
}
