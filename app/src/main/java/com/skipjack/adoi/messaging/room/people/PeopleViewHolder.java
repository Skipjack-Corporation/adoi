package com.skipjack.adoi.messaging.room.people;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;

import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixService;

public class PeopleViewHolder extends RecyclerView.ViewHolder {
    public TextView textDisplayName;
    public TextView textMessage;

    public CircleImageView imgPostAvatar;
    public ImageView imgStatus;

    public PeopleViewHolder(@NonNull View itemView) {
        super(itemView);
        textDisplayName = itemView.findViewById(R.id.textDisplayname);
        textMessage = itemView.findViewById(R.id.textMessage);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        imgStatus = itemView.findViewById(R.id.imgStatus);
    }

    public void populateView(RoomMember roomMember){
        User user = MatrixService.get().getUser(roomMember.getUserId());

        textDisplayName.setText(roomMember.displayname);

        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(roomMember.getAvatarUrl(),size))
                .placeholder(R.drawable.ic_placeholder_fill)
                .into(imgPostAvatar);

        String presence =  user.presence;
        imgStatus.setVisibility(View.VISIBLE);
        if (presence == null)
            imgStatus.setImageResource(R.drawable.ic_status_offline);
        else if (presence.equals(User.PRESENCE_ONLINE))
            imgStatus.setImageResource(R.drawable.ic_status_online);
        else if (presence.equals(User.PRESENCE_UNAVAILABLE))
            imgStatus.setImageResource(R.drawable.ic_status_idle);
        else if (presence.equals(User.PRESENCE_OFFLINE))
            imgStatus.setImageResource(R.drawable.ic_status_offline);
        else if (presence.equals(User.PRESENCE_HIDDEN))
            imgStatus.setImageResource(R.drawable.ic_status_donotdisturb);
        else
            imgStatus.setImageResource(R.drawable.ic_status_offline);
    }
}
