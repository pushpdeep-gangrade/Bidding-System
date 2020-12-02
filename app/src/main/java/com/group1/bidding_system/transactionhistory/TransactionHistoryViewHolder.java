package com.group1.bidding_system.transactionhistory;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionHistoryViewHolder extends RecyclerView.ViewHolder {
    public View itemView;

    public TransactionHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }
}
