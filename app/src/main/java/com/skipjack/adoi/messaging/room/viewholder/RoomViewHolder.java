package com.skipjack.adoi.messaging.room.viewholder;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.event.EventActivity;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.local_storage.AppSharedPreference;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixHelper;

public class RoomViewHolder extends AbsRViewHolder {
    public TextView textDisplayName;
    public TextView textMessage;
    public TextView textDate;
    public TextView textUnreadCount;

    public CircleImageView imgPostAvatar;
    public ImageView imgStatus;
    public ImageView imgMessageStatus;
    public RoomViewHolder(@NonNull View itemView) {
        super(itemView);
        textDisplayName = itemView.findViewById(R.id.textDisplayname);
        textMessage = itemView.findViewById(R.id.textMessage);
        textDate = itemView.findViewById(R.id.textDate);
        textUnreadCount = itemView.findViewById(R.id.textUnreadCount);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        imgMessageStatus = itemView.findViewById(R.id.imgMessageStatus);

    }

    @Override
    public void populateView(Room room) {
        textDisplayName.setText(room.getRoomDisplayName(MatrixService.get().getContext()));
        RoomSummary roomSummary = room.getRoomSummary();
        textDate.setText(MatrixHelper.getTimestampToString(roomSummary.getLatestReceivedEvent().originServerTs));
        textMessage.setText(MatrixService.get().getRoomMessageDisplay(room));

        if (room.isDirect()){
            room.getActiveMembersAsync(new MatrixCallback<List<RoomMember>>() {
                @Override
                public void onAPISuccess(List<RoomMember> data) {
                    for (RoomMember member: data){
                        if (!member.getUserId().equals(AppSharedPreference.get().getLoginCredential().getUserId())){
                            User user = MatrixService.get().mxSession.getDataHandler().getUser(data.get(0).getUserId());
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
                }

                @Override
                public void onAPIFailure(String errorMessage) {

                }
            });
        }

        if (room.getNotificationCount() > 0){
            textUnreadCount.setText(room.getNotificationCount()+"");
            textUnreadCount.setVisibility(View.VISIBLE);
        }else
            textUnreadCount.setVisibility(View.GONE);

        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(room.getAvatarUrl(),size))
                .placeholder(R.drawable.ic_placeholder_fill)
                .into(imgPostAvatar);


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(itemView.getContext(),
                        EventActivity.class);
                intent.putExtra(Constants.KEY_ROOM_ID,room.getRoomId());
                itemView.getContext().startActivity(intent);
            }
        });

    }

}
