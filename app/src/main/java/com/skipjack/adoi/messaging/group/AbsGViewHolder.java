package com.skipjack.adoi.messaging.group;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.group.Group;

public abstract class AbsGViewHolder extends RecyclerView.ViewHolder {

    public AbsGViewHolder(@NonNull View itemView) {
        super(itemView);
    }
    public abstract void populateView(Group group);
}
