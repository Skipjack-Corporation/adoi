package com.skipjack.adoi.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.view.AppImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder>{
    public PostRecyclerAdapter() {

    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.adapter_post,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        switch (position){
            case 0:{
                holder.imgPostAvatar.setImageCircularDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_1_small));
                holder.imgPostPic.setImageDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_1));
                holder.imgStatus.setImageResource(R.drawable.ic_status_online);
            }break;
            case 1:{
                holder.imgPostAvatar.setImageCircularDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_2_small));
                holder.imgPostPic.setImageDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_2));
                holder.imgStatus.setImageResource(R.drawable.ic_status_idle);
            }break;
            case 2:{
                holder.imgPostAvatar.setImageCircularDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_3_small));
                holder.imgPostPic.setImageDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_3));
                holder.imgStatus.setImageResource(R.drawable.ic_status_donotdisturb);
            }break;
            case 3:{
                holder.imgPostAvatar.setImageCircularDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_4_small));
                holder.imgPostPic.setImageDrawable(holder.itemView.getContext().getDrawable(R.drawable.img_example_4));
                holder.imgStatus.setImageResource(R.drawable.ic_status_offline);
            }break;
        }

    }



    @Override
    public int getItemCount() {
        return 4;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        AppImageView imgPostAvatar;
        AppImageView imgPostPic;
        ImageView imgStatus;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
            imgPostPic = itemView.findViewById(R.id.imgPostPic);
            imgStatus = itemView.findViewById(R.id.imgStatus);

            imgStatus.setVisibility(View.VISIBLE);

        }
    }
}
