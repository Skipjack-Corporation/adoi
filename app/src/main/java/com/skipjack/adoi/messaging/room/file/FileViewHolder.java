package com.skipjack.adoi.messaging.room.file;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;
import com.skipjack.adoi.utility.AppUtility;
import com.skipjack.adoi.view.RoundCornerImageVIew;

import org.matrix.androidsdk.core.callback.SimpleApiCallback;
import org.matrix.androidsdk.db.MXMediaCache;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.message.Message;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixService;

public class FileViewHolder extends RecyclerView.ViewHolder {
    public TextView textDisplayName;
    public TextView textMessage;
    public TextView textDate;

    public RoundCornerImageVIew imgPhoto;
    public ImageView imgStatus;
    private MXMediaCache mMediasCache;
    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
        textDisplayName = itemView.findViewById(R.id.textDisplayname);
        textMessage = itemView.findViewById(R.id.textMessage);
        textDate = itemView.findViewById(R.id.textDate);
        imgPhoto = itemView.findViewById(R.id.imgPhoto);
        imgStatus = itemView.findViewById(R.id.imgStatus);
    }

    public void populateView(SlidableMediaInfo mediaInfo){
        textDisplayName.setText(mediaInfo.mFileName);
        textMessage.setText(Formatter.formatFileSize(itemView.getContext(),mediaInfo.fileSize));
        textDate.setText(MatrixHelper.getTimestampToString(mediaInfo.date));
        mMediasCache  = MatrixService.get().mxSession.getMediaCache();
        if (mediaInfo.mMessageType.equals(Message.MSGTYPE_IMAGE)){
            setImageView(mediaInfo);
        }else if(mediaInfo.mMessageType.equals(Message.MSGTYPE_VIDEO)){
            setVideoView(mediaInfo);
        }


    }

    private void setImageView(SlidableMediaInfo mediaInfo){
        CircularProgressDrawable drawable = new CircularProgressDrawable(itemView.getContext());
        drawable.setStrokeWidth(5f);
        drawable.setCenterRadius(30f);
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
        drawable.start();
        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);

        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(mediaInfo.mMediaUrl,
                       size))
                .placeholder(drawable)
                .into(imgPhoto);

    }
    private void setVideoView(SlidableMediaInfo mediaInfo){
        CircularProgressDrawable drawable = new CircularProgressDrawable(itemView.getContext());
        drawable.setStrokeWidth(5f);
        drawable.setCenterRadius(30f);
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
        drawable.start();
        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);

        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(mediaInfo.mThumbnailUrl,
                       size))
                .placeholder(drawable)
                .into(imgPhoto);

    }
}
