package com.skipjack.adoi.messaging;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.database.AppSharedPreference;
import com.skipjack.adoi.messaging.model.MessageDetail;
import com.skipjack.adoi.view.AppImageView;

import org.matrix.androidsdk.core.EventDisplay;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;
import java.util.List;

import mx.skipjack.service.MatrixService;
import mx.skipjack.service.MatrixUtility;
import mx.skipjack.service.MxRoomManager;

public class MessagesDetailRecyclerAdapter extends RecyclerView.Adapter<MessagesDetailRecyclerAdapter.PostViewHolder>{

    List<MessageDetail> list = new ArrayList<>();
    RoomState roomState;
    public MessagesDetailRecyclerAdapter(ArrayList<MessageDetail> arrayList, RoomState roomState) {
        this.list = arrayList;
        this.roomState = roomState;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
        return new PostViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.adapter_message_receiver,parent,false));

        return new PostViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.adapter_message_sender,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        MessageDetail detail = list.get(position);


        holder.textName.setText(roomState.getMemberName(detail.getSenderName()));
        holder.textDate.setText(MxRoomManager.getRoomTimestamp(detail.getTimeStamp()));


        EventDisplay eventDisplay = new EventDisplay(MatrixService.get().getContext());
        eventDisplay.setPrependMessagesWithAuthor(false);
        String strMessages = "";
        for (Event event: detail.getEvents()){
            strMessages = strMessages + String.valueOf(eventDisplay.getTextualDisplay(event,roomState))+"\n\n";
        }
        holder.textMessage.setText(strMessages);

        Bitmap icon = BitmapFactory.decodeResource(holder.itemView.getContext().getResources(),
                R.drawable.ic_logo_small);

       MatrixService.get().mxSession.getMediaCache().loadAvatarThumbnail(MatrixService.get().homeServerConfig,
                holder.imgPostAvatar,detail.getAvatarUrl(), holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_msg_avatar_size),icon);

//        if (getItemViewType(position) == 0){
            holder.imgPostAvatar.setImageCircularDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_1_small));
            holder.imgStatus.setImageResource(R.drawable.ic_status_online);
//        }else {
//            holder.imgPostAvatar.setImageCircularDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_2_small));
//        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(),
                        MessageDetailActivity.class));
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        MessageDetail detail = list.get(position);
        if (detail.getSenderId().equals(AppSharedPreference.get().getLoginCredential().getUserId()))
            return 1;

        return 0 ;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        TextView textName;
        TextView textDate;
        TextView textMessage;
        AppImageView imgPostAvatar;
        ImageView imgStatus;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textDate = itemView.findViewById(R.id.textDate);
            textMessage = itemView.findViewById(R.id.textMessage);
            imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
            imgStatus = itemView.findViewById(R.id.imgStatus);


        }
    }
}
