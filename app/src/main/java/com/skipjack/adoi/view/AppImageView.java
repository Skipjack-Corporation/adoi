package com.skipjack.adoi.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class AppImageView extends AppCompatImageView {
    public AppImageView(Context context) {
        super(context);
        init();
    }

    public AppImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public AppImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
    }
    public void setImageDrawable(String urlString){
        Glide.with(getContext())
                .load(urlString)
                .centerCrop()
                .into(this);

    }

    public void setImageCircularDrawable(Drawable drawable){
        Glide.with(getContext())
                .load(drawable)
                .apply(RequestOptions.circleCropTransform())
                .into(this);

    }

    public void setImageCircularDrawable(String urlString){
        Glide.with(getContext())
                .load(urlString)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(this);

    }
}
