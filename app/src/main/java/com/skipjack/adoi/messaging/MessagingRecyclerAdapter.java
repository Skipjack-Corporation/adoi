package com.skipjack.adoi.messaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.model.MessageGroup;
import com.skipjack.adoi.view.AppImageView;

import org.matrix.androidsdk.db.MXMediaCache;

import java.util.ArrayList;
import java.util.List;

import mx.skipjack.service.MatrixService;

public class MessagingRecyclerAdapter extends RecyclerView.Adapter<MessagingRecyclerAdapter.PostViewHolder>{

    private List<MessageGroup> list = new ArrayList<>();
    public MessagingRecyclerAdapter(ArrayList<MessageGroup> list) {
        this.list = list;
    }

    public void setList(List<MessageGroup> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.adapter_message,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        MessageGroup item = list.get(position);

        holder.textDisplayName.setText(item.getDisplayName());
        holder.textDate.setText(item.getDate());
        holder.textMessage.setText(item.getLatestMessage());

        holder.imgStatus.setImageResource(R.drawable.ic_status_online);

        if (item.getUnreadCount() > 0){
            holder.textUnreadCount.setText(item.getUnreadCount()+"");
            holder.textUnreadCount.setVisibility(View.VISIBLE);
        }else
            holder.textUnreadCount.setVisibility(View.GONE);

//        if (item.getAvatarUrl() != null) {
//            holder.imgPostAvatar.setImageCircularDrawable(item.getAvatarUrl());
//        }else {
//
//            }
        Bitmap icon = BitmapFactory.decodeResource(holder.itemView.getContext().getResources(),
                R.drawable.ic_logo_small);

        MatrixService.get().mxSession.getMediaCache().loadAvatarThumbnail(MatrixService.get().homeServerConfig,
                holder.imgPostAvatar,item.getAvatarUrl(), holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size),icon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),
                        MessageDetailActivity.class);
                intent.putExtra(Constants.KEY_ROOM_ID,item.getRoomId());
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        TextView textDisplayName;
        TextView textMessage;
        TextView textDate;
        TextView textUnreadCount;

        AppImageView imgPostAvatar;
        ImageView imgStatus;
        ImageView imgMessageStatus;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textDisplayName = itemView.findViewById(R.id.textDisplayname);
            textMessage = itemView.findViewById(R.id.textMessage);
            textDate = itemView.findViewById(R.id.textDate);
            textUnreadCount = itemView.findViewById(R.id.textUnreadCount);
            imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgMessageStatus = itemView.findViewById(R.id.imgMessageStatus);

        }
    }
}
