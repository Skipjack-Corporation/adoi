package com.skipjack.adoi.messaging.room;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.messaging.room.viewholder.AbsRViewHolder;
import com.skipjack.adoi.messaging.room.viewholder.InvitationViewHolder;
import com.skipjack.adoi.messaging.room.viewholder.RoomViewHolder;

import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;
import java.util.List;

import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.model.RoomItemType;

public class RoomAdapter extends RecyclerView.Adapter<AbsRViewHolder> {

    private List<Room> list = new ArrayList<>();

    public RoomAdapter(ArrayList<Room> list) {
        this.list = list;
    }

    public void update(List<Room> list) {
//        RoomDiffCallback diffCallback = new RoomDiffCallback(this.list,list);
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
//        diffResult.dispatchUpdatesTo(this);
    }



    @NonNull
    @Override
    public AbsRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (RoomItemType.getRoomItemType(viewType)){
            case INVITE_ROOM:
                return new InvitationViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_invite,parent,false));
            case INVITE_DIRECT:
                return new InvitationViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_invite,parent,false));
            default:{
               return new RoomViewHolder(LayoutInflater
                    .from(parent.getContext()).inflate(R.layout.adapter_room,parent,false));

            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbsRViewHolder holder, int position) {
        Room room = list.get(position);

        holder.populateView(room);


    }

    @Override
    public int getItemViewType(int position) {
        return MatrixUtility.getRoomItemType(list.get(position)).getType();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
