package com.skipjack.adoi.messaging.room.people;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.messaging.room.viewholder.AbsRViewHolder;
import com.skipjack.adoi.messaging.room.viewholder.InvitationViewHolder;
import com.skipjack.adoi.messaging.room.viewholder.RoomViewHolder;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.RoomMember;

import java.util.ArrayList;
import java.util.List;

import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.model.RoomItemType;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleViewHolder> {

    private List<RoomMember> list = new ArrayList<>();

    public PeopleAdapter(List<RoomMember> list) {
        this.list = list;
    }

    public void update(List<RoomMember> list) {
//        RoomDiffCallback diffCallback = new RoomDiffCallback(this.list,list);
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
//        diffResult.dispatchUpdatesTo(this);
    }



    @NonNull
    @Override
    public PeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeopleViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.adapter_people,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull PeopleViewHolder holder, int position) {
        RoomMember roomMember = list.get(position);
        holder.populateView(roomMember);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
