package com.skipjack.adoi.messaging.room.settings;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;
import com.skipjack.adoi.view.RoundCornerImageVIew;

import org.matrix.androidsdk.db.MXMediaCache;
import org.matrix.androidsdk.rest.model.message.Message;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixService;

public class SettingViewHolder extends RecyclerView.ViewHolder {
    public TextView textTitle;
    public TextView textSubTitle;
    public CircleImageView imgPhoto;
    public Switch aSwitch;
    public SettingViewHolder(@NonNull View itemView) {
        super(itemView);
       if (itemView.findViewById(R.id.textTitle) != null)
            textTitle = itemView.findViewById(R.id.textTitle);
       if (itemView.findViewById(R.id.textSubTitle) != null)
            textSubTitle = itemView.findViewById(R.id.textSubTitle);
       if (itemView.findViewById(R.id.imgPhoto) != null)
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
       if (itemView.findViewById(R.id.switchSetting) != null)
            aSwitch = itemView.findViewById(R.id.switchSetting);
    }

    public void populateView(SettingItem settingItem){
        switch (settingItem.type){
            case RoomPhoto:{
                textTitle.setText(settingItem.title);
                int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_post_size);
                Glide.with(itemView.getContext())
                        .load(MatrixService.get().getDownloadableThumbnailUrl(settingItem.url,size))
                        .placeholder(R.drawable.ic_placeholder_fill)
                        .into(imgPhoto);

            }break;
            case LineSeparator:break;
            case RoomDirectory:
            case TitleHeader:
            case AddNewAddress:
            case AddNewCommunity:
            case NoLocalAddress:
            case NoFlairCommunity:
            case Leave:
            default: {
                textTitle.setText(settingItem.title);
                if (textSubTitle != null){
                    if (settingItem.subTitle != null) {
                        textSubTitle.setVisibility(View.VISIBLE);
                        textSubTitle.setText(settingItem.subTitle);
                    } else textSubTitle.setVisibility(View.GONE);
                }

            }break;

        }

    }

}
