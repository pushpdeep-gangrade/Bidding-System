package com.group1.bidding_system.currentbids;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BidItemViewHolder extends RecyclerView.ViewHolder {
    public View itemView;

    public BidItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }
}