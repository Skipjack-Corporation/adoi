package com.skipjack.adoi.messaging.room.settings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.media.MediaPreviewActivity;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;
import com.skipjack.adoi.messaging.room.file.FileViewHolder;

import java.util.ArrayList;
import java.util.List;

import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixService;

public class SettingsAdapter extends RecyclerView.Adapter<SettingViewHolder> {

    private List<SettingItem> list = new ArrayList<>();
    private Callback callback;
    public SettingsAdapter(List<SettingItem> list, Callback callback) {
        this.list = list;
        this.callback = callback;
    }

    public void update(List<SettingItem> list) {
//        RoomDiffCallback diffCallback = new RoomDiffCallback(this.list,list);
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.list = list;
//        diffResult.dispatchUpdatesTo(this);
    }



    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingType settingType = SettingType.getType(viewType);
        switch (settingType){
            case RoomPhoto:
                return new SettingViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_settings_photo,parent,false));
            case TitleHeader:
                return new SettingViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_settings_title,parent,false));
            case AddNewAddress:
            case AddNewCommunity:
                return new SettingViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_settings_add,parent,false));
            case RoomDirectory:
            case Encrypt:
                return new SettingViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_settings_switch,parent,false));
            case LineSeparator:
                return new SettingViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_settings_line,parent,false));
            default:
                return new SettingViewHolder(LayoutInflater
                        .from(parent.getContext()).inflate(R.layout.adapter_room_settings_text,parent,false));
        }


    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingItem item = list.get(position);
        holder.populateView(item);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSelectedSettings(item.type);
            }
        });
    }
    @Override
    public int getItemViewType(int position) {
        return list.get(position).type.ordinal();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface Callback{
        void onSelectedSettings(SettingType settingType);
    }


}
