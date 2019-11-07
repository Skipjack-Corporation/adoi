package com.skipjack.adoi.messaging.room.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.matrix.androidsdk.data.Room;

public abstract class AbsRViewHolder extends RecyclerView.ViewHolder {

    public AbsRViewHolder(@NonNull View itemView) {
        super(itemView);
    }
    public abstract void populateView(Room room);
}
