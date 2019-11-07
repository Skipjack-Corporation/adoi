package com.skipjack.adoi.messaging.room.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.messaging.room.viewholder.AbsRViewHolder;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomSummary;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixUtility;

public class InvitationViewHolder extends AbsRViewHolder implements View.OnClickListener {
    public TextView textDisplayName;
    public TextView textMessage;
    public TextView textDate;

    public CircleImageView imgPostAvatar;
    public ImageView imgStatus;
    public Button btnJoin;
    public Button btnReject;
    private Room room;
    public InvitationViewHolder(@NonNull View itemView) {
        super(itemView);
        textDisplayName = itemView.findViewById(R.id.textDisplayname);
        textMessage = itemView.findViewById(R.id.textMessage);
        textDate = itemView.findViewById(R.id.textDate);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        btnJoin = itemView.findViewById(R.id.btnJoin);
        btnReject = itemView.findViewById(R.id.btnReject);

        btnJoin.setOnClickListener(this);
        btnReject.setOnClickListener(this);
    }

    @Override
    public void populateView(Room room) {
        this.room = room;
        textDisplayName.setText(room.getRoomDisplayName(MatrixService.get().getContext()));
        RoomSummary roomSummary = room.getRoomSummary();
        textDate.setText(MatrixUtility.getRoomTimestamp(roomSummary.getLatestReceivedEvent().originServerTs));
        textMessage.setText(MatrixService.get().getRoomMessageDisplay(room));

        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(room.getAvatarUrl(),size))
                .placeholder(R.drawable.ic_placeholder_fill)
                .into(imgPostAvatar);
    }


    @Override
    public void onClick(View v) {
        ((BaseAppCompatActivity)itemView.getContext()).showProgressDialog();
        switch (v.getId()){
            case R.id.btnJoin:{
             room.join(new MatrixCallback<Void>() {
                 @Override
                 public void onAPISuccess(Void data) {
                     ((BaseAppCompatActivity)itemView.getContext()).hideProgressDialog();
                     AppUtility.toast(itemView.getContext(),"Success!");
                 }

                 @Override
                 public void onAPIFailure(String errorMessage) {
                     ((BaseAppCompatActivity)itemView.getContext()).hideProgressDialog();
                     AppUtility.toast(itemView.getContext(),errorMessage);

                 }
             });
            }break;
            case R.id.btnReject:{
                room.leave(new MatrixCallback<Void>() {
                    @Override
                    public void onAPISuccess(Void data) {
                        ((BaseAppCompatActivity)itemView.getContext()).hideProgressDialog();
                        AppUtility.toast(itemView.getContext(),"Success!");

                    }

                    @Override
                    public void onAPIFailure(String errorMessage) {
                        ((BaseAppCompatActivity)itemView.getContext()).hideProgressDialog();
                        AppUtility.toast(itemView.getContext(),errorMessage);

                    }
                });
            }break;
        }
    }
}
