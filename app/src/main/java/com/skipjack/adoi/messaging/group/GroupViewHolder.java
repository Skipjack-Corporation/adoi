package com.skipjack.adoi.messaging.group;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;

import org.matrix.androidsdk.rest.model.group.Group;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixService;

public class GroupViewHolder extends AbsGViewHolder {
    public TextView textDisplayName;
    public TextView textMessage;
    public TextView textMemberCount;

    public CircleImageView imgPostAvatar;
    public ImageView imgStatus;
    public ImageView imgMessageStatus;
    public GroupViewHolder(@NonNull View itemView) {
        super(itemView);
        textDisplayName = itemView.findViewById(R.id.textDisplayname);
        textMessage = itemView.findViewById(R.id.textMessage);
        textMemberCount = itemView.findViewById(R.id.textMemberCount);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        imgMessageStatus = itemView.findViewById(R.id.imgMessageStatus);

    }

    @Override
    public void populateView(Group group) {
        textDisplayName.setText(group.getDisplayName());

        textMessage.setText(group.getShortDescription());
        textMemberCount.setText(group.getGroupUsers().totalUserCountEstimate+"");
        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(group.getAvatarUrl(),size))
                .placeholder(R.drawable.ic_placeholder_fill)
                .into(imgPostAvatar);

        imgStatus.setVisibility(View.GONE);
//
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(itemView.getContext(),
//                        EventActivity.class);
//                intent.putExtra(Constants.KEY_ROOM_ID,room.getRoomId());
//                intent.putExtra(Constants.KEY_ROOM_NAME,room.getRoomDisplayName(MatrixService.get().getContext()));
//                itemView.getContext().startActivity(intent);
//            }
//        });
    }

}
