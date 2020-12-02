package com.group1.bidding_system.transactionhistory;

import android.content.Context;
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
import com.group1.bidding_system.currentbids.BidItemViewHolder;
import com.group1.bidding_system.models.BidItem;
import com.group1.bidding_system.models.Transaction;
import com.group1.bidding_system.models.User;

import java.util.ArrayList;


public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryViewHolder> {
    ArrayList<Transaction> transactions = new ArrayList<>();
    FirebaseFirestore db;
    Context context;

    public TransactionHistoryAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Transaction> transactions
            , FirebaseFirestore db){
        this.context = context;
        this.transactions = transactions;
        this.db = db;
    }

    @NonNull
    @Override
    public TransactionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item_2, parent, false);

        TransactionHistoryViewHolder vh = new TransactionHistoryViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHistoryViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        TextView itemName = holder.itemView.findViewById(R.id.transactionItem_itemName);
        TextView price = holder.itemView.findViewById(R.id.transactionItem_price);
        TextView seller = holder.itemView.findViewById(R.id.transactionItem_seller);
        TextView date = holder.itemView.findViewById(R.id.transactionItem_date);

        itemName.setText(transaction.itemName);
        price.setText("$" + transaction.price);
        date.setText(transaction.date.toDate().toString());

        setSellerName(transaction.sellerId, seller);

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setSellerName(String id, final TextView tv){
        Log.d("Seller ID", id);
        db.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = new User(task.getResult().getData());
                tv.setText(user.firstName + " " + user.lastName);
            }
        });
    }
}
