package com.skipjack.adoi.messaging.group;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.rest.model.group.Group;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;

public class GroupInviteViewHolder extends AbsGViewHolder implements View.OnClickListener {
    public TextView textDisplayName;
    public TextView textMessage;
    public TextView textDate;

    public CircleImageView imgPostAvatar;
    public ImageView imgStatus;
    public Button btnJoin;
    public Button btnReject;
    private Group group;
    public GroupInviteViewHolder(@NonNull View itemView) {
        super(itemView);
        textDisplayName = itemView.findViewById(R.id.textDisplayname);
        textMessage = itemView.findViewById(R.id.textMessage);
        textDate = itemView.findViewById(R.id.textDate);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        btnJoin = itemView.findViewById(R.id.btnJoin);
        btnReject = itemView.findViewById(R.id.btnReject);

        btnJoin.setOnClickListener(this);
    }

    @Override
    public void populateView(Group group) {
        this.group = group;
        textDisplayName.setText(group.getDisplayName());
        textDate.setVisibility(View.GONE);
        textMessage.setText(group.getShortDescription());

        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(group.getAvatarUrl(),size))
                .placeholder(R.drawable.ic_placeholder_fill)
                .into(imgPostAvatar);
    }


    @Override
    public void onClick(View v) {
        ((BaseAppCompatActivity)itemView.getContext()).showProgressDialog();
        switch (v.getId()){
            case R.id.btnJoin:{
                MatrixService.get().mxSession.getGroupsManager().joinGroup(group.getGroupId(), new MatrixCallback<Void>() {
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
                MatrixService.get().mxSession.getGroupsManager().leaveGroup(group.getGroupId(), new MatrixCallback<Void>() {
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
