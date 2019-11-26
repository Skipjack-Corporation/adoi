package com.skipjack.adoi.messaging.room.file;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.media.MediaPreviewActivity;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;
import com.skipjack.adoi.messaging.room.people.PeopleViewHolder;

import org.matrix.androidsdk.rest.model.RoomMember;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private List<SlidableMediaInfo> list = new ArrayList<>();

    public FilesAdapter(List<SlidableMediaInfo> list) {
        this.list = list;
    }

    public void update(List<SlidableMediaInfo> list) {
//        RoomDiffCallback diffCallback = new RoomDiffCallback(this.list,list);
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
//        diffResult.dispatchUpdatesTo(this);
    }



    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.adapter_files,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        SlidableMediaInfo mediaInfo = list.get(position);
        holder.populateView(mediaInfo);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MediaPreviewActivity.class);
                intent.putExtra(Constants.KEY_MEDIALIST_DATA, new ArrayList<>(list));
                intent.putExtra(Constants.KEY_CURRENT_MEDIA_ID,mediaInfo.id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
