package com.skipjack.adoi.messaging.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;

import org.matrix.androidsdk.rest.model.group.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<AbsGViewHolder> {

    private List<Group> list = new ArrayList<>();

    public GroupAdapter(ArrayList<Group> list) {
        this.list = list;
    }

    public void update(List<Group> list) {
//        RoomDiffCallback diffCallback = new RoomDiffCallback(this.list,list);
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
//        diffResult.dispatchUpdatesTo(this);
    }



    @NonNull
    @Override
    public AbsGViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new GroupInviteViewHolder(LayoutInflater
                    .from(parent.getContext()).inflate(R.layout.adapter_group_invite,parent,false));
        }

        return new GroupViewHolder(LayoutInflater
                    .from(parent.getContext()).inflate(R.layout.adapter_group,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull AbsGViewHolder holder, int position) {
        holder.populateView(list.get(position));

    }

    @Override
    public int getItemViewType(int position) {
        Group group = list.get(position);
        if (group.isInvited()) return 0;
        return 1;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
