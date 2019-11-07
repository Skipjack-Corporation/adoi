package com.skipjack.adoi.messaging.event;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.media.MediaPreviewActivity;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;

import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.core.EventDisplay;
import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.crypto.model.crypto.EncryptedFileInfo;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.message.FileMessage;
import org.matrix.androidsdk.rest.model.message.ImageInfo;
import org.matrix.androidsdk.rest.model.message.ImageMessage;
import org.matrix.androidsdk.rest.model.message.Message;
import org.matrix.androidsdk.rest.model.message.StickerMessage;
import org.matrix.androidsdk.rest.model.message.VideoInfo;
import org.matrix.androidsdk.rest.model.message.VideoMessage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import support.skipjack.adoi.matrix.MatrixUtility;
import support.skipjack.adoi.matrix.MatrixService;

public class EventViewHolder extends RecyclerView.ViewHolder {

    public TextView textName;
    public TextView textDate;
    public CircleImageView imgPostAvatar;
    public ImageView imgStatus;
    public LinearLayout layoutContent;
    private ViewGroup parent;
    private boolean isYou;
    private Callback callback;
    public EventViewHolder(@NonNull View itemView, ViewGroup parent, boolean isYou, Callback callback) {
        super(itemView);
        this.parent = parent;
        this.isYou = isYou;
        this.callback = callback;
        textName = itemView.findViewById(R.id.textName);
        textDate = itemView.findViewById(R.id.textDate);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        layoutContent = itemView.findViewById(R.id.layoutContent);
    }

    public void bind(Event event){
        IMXStore roomStore = MatrixService.get().mxSession.getDataHandler().getStore();
        RoomState roomState = roomStore.getRoom(event.roomId).getState();
        textName.setText(roomState.getMemberName(event.sender));
        textDate.setText(MatrixService.getTimestampToString(event.getOriginServerTs()));


        int size = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.img_msg_avatar_size);
        int drawableId = R.drawable.ic_placeholder_fill;
        if (isYou){
            drawableId = R.drawable.ic_myplaceholder_fill;
        }
        try{
            Glide.with(itemView.getContext())
                    .load(MatrixService.get().getDownloadableThumbnailUrl(roomState.getMember(event.sender).avatarUrl,size))
                    .placeholder(drawableId)
                    .into(imgPostAvatar);
        }catch (Exception e){
            imgPostAvatar.setImageResource(drawableId);
        }

        //set content view
        EventMsgType eventMsgType = EventMsgType.getType(JsonUtils.toMessage(event.getContent()).msgtype);
        View contentView = null;
        switch (eventMsgType){
            case MSGTYPE_TEXT:{
                contentView = LayoutInflater.from(itemView.getContext())
                        .inflate(isYou?R.layout.adapter_message_text_you:R.layout.adapter_message_text,parent,false);
            }break;
            case MSGTYPE_IMAGE:
            case MSGTYPE_VIDEO:{
                contentView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.adapter_message_image_video,parent,false);
            }break;
            case MSGTYPE_FILE:{
                contentView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.adapter_message_file,parent,false);
            }break;
            default:{
                contentView = LayoutInflater.from(itemView.getContext())
                        .inflate(isYou?R.layout.adapter_message_text_you:R.layout.adapter_message_text,parent,false);
            }break;
        }

        MatrixUtility.LOG("eventType:"+event.getType());
        MatrixUtility.LOG("msgType:"+ eventMsgType);
        switch (eventMsgType){
            case MSGTYPE_TEXT:{
                setTextView(contentView,event, roomState);
            }break;
            case MSGTYPE_IMAGE:
            case MSGTYPE_VIDEO:{
                configImageVideoContentView(contentView,event, eventMsgType);
            }break;
            case MSGTYPE_FILE:{
                setFileView(contentView,event);
            }break;
            default:{

                setTextView(contentView,event,roomState);
            }break;
        }

        layoutContent.removeAllViews();
        layoutContent.addView(contentView);

    }

    private void setTextView(View view, Event event, RoomState roomState){
        TextView textMessage = view.findViewById(R.id.textChildText);
        //Message
//        Message message = JsonUtils.toMessage(event.getContent());
//        if (message != null){
//            if (message.formatted_body != null)
//            textMessage.setText(Html.fromHtml(message.body));
//            else
//              textMessage.setText("");
//        }

        if (event.getType().equals(Event.EVENT_TYPE_CALL_HANGUP)){
            textMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_media_call_end_click,0,0,0);

        }else if (event.getType().contains("m.call")){
            textMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_media_call,0,0,0);

        }else
            textMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);


        if (event != null && event.sender != null && roomState != null){
            MessageRow messageRow = new MessageRow(event,roomState);

            Spannable spannable = messageRow.getText(new AlignmentSpan() {
                @Override
                public Layout.Alignment getAlignment() {
                    return Layout.Alignment.ALIGN_LEFT;
                }
            }, new EventDisplay(MatrixService.get().getContext()));
            if (spannable != null)
                textMessage.setText(spannable);

        }
    }

    private void configImageVideoContentView(View view,Event event, EventMsgType msType){
        ImageView imgPhoto = view.findViewById(R.id.imgPhoto);
        ImageView imgBtnPlay = view.findViewById(R.id.imgBtnPlay);
        TextView textFailed = view.findViewById(R.id.textFailed);
        try {
            Message message = null;
            boolean videoContent = false;
            if (msType == EventMsgType.MSGTYPE_IMAGE) {
                ImageMessage imageMessage = JsonUtils.toImageMessage(event.getContent());
                if ("image/gif".equals(imageMessage.getMimeType())) {
                    videoContent = true;
                }
                message = imageMessage;
            } else if (msType == EventMsgType.MSGTYPE_VIDEO) {
                videoContent = true;
                message = JsonUtils.toVideoMessage(event.getContent());
            } else if (event.getType().equals(Event.EVENT_TYPE_STICKER)) {
                StickerMessage stickerMessage = JsonUtils.toStickerMessage(event.getContent());
                message = stickerMessage;
            }

            if (message == null){
                textFailed.setVisibility(View.VISIBLE);
                return;
            }
            imgPhoto.setVisibility(View.VISIBLE);
            textFailed.setVisibility(View.GONE);



            //config
            if (videoContent){
                imgBtnPlay.setVisibility(View.VISIBLE);
                setVideoView((VideoMessage) message,imgPhoto);
            }else {
                setImageView((ImageMessage) message,imgPhoto);
            }

        } catch (Exception e) {
            MatrixUtility.LOG("## getImageVideoView() failed : " + e.getMessage());
        }

        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onMediaClick(event);
            }
        });
    }


    private void setImageView(ImageMessage imageMessage, ImageView imgPhoto){
        int rotationAngle = 0;
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        String thumbUrl = null;
        int thumbWidth = -1;
        int thumbHeight = -1;

        EncryptedFileInfo encryptedFileInfo = null;

        imageMessage.checkMediaUrls();

        // Backwards compatibility with events from before Synapse 0.6.0
        if (imageMessage.getThumbnailUrl() != null) {
            thumbUrl = imageMessage.getThumbnailUrl();

            if (null != imageMessage.info) {
                encryptedFileInfo = imageMessage.info.thumbnail_file;
            }

        } else if (imageMessage.getUrl() != null) {
            thumbUrl = imageMessage.getUrl();
            encryptedFileInfo = imageMessage.file;
        }

        rotationAngle = imageMessage.getRotation();

        ImageInfo imageInfo = imageMessage.info;

        if (null != imageInfo) {
            if ((null != imageInfo.w) && (null != imageInfo.h)) {
                thumbWidth = imageInfo.w;
                thumbHeight = imageInfo.h;
            }

            if (null != imageInfo.orientation) {
                orientation = imageInfo.orientation;
            }
        }

        CircularProgressDrawable drawable = new CircularProgressDrawable(itemView.getContext());
        drawable.setStrokeWidth(5f);
        drawable.setCenterRadius(30f);
        drawable.start();
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(thumbUrl,thumbWidth))
                .placeholder(drawable)
                .into(imgPhoto);
    }
    private void setVideoView(VideoMessage videoMessage, ImageView imgPhoto){
        int rotationAngle = 0;
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        String thumbUrl = null;
        int thumbWidth = -1;
        int thumbHeight = -1;

        EncryptedFileInfo encryptedFileInfo = null;

        videoMessage.checkMediaUrls();

        thumbUrl = videoMessage.getThumbnailUrl();
        if (null != videoMessage.info) {
            encryptedFileInfo = videoMessage.info.thumbnail_file;
        }

        VideoInfo videoinfo = videoMessage.info;

        if (null != videoinfo) {
            if (null != videoMessage.info.thumbnail_info
                    && null != videoMessage.info.thumbnail_info.w
                    && null != videoMessage.info.thumbnail_info.h) {
                thumbWidth = videoMessage.info.thumbnail_info.w;
                thumbHeight = videoMessage.info.thumbnail_info.h;
            }
        }
        CircularProgressDrawable drawable = new CircularProgressDrawable(itemView.getContext());
        drawable.setStrokeWidth(5f);
        drawable.setCenterRadius(30f);
        drawable.start();
        Glide.with(itemView.getContext())
                .load(MatrixService.get().getDownloadableThumbnailUrl(thumbUrl,thumbWidth))
                .placeholder(drawable)
                .into(imgPhoto);

    }

    private void setFileView(View view,Event event){
        try {
            final FileMessage fileMessage = JsonUtils.toFileMessage(event.getContent());
            final TextView fileTextView = view.findViewById(R.id.textFileName);

            fileTextView.setPaintFlags(fileTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            fileTextView.setText(fileMessage.body);

            // display the right message tabType icon.
            // Audio and File messages are managed by the same method
            final ImageView imageTypeView = view.findViewById(R.id.imgType);

            if (null != imageTypeView) {
                imageTypeView.setImageResource(Message.MSGTYPE_AUDIO.equals(fileMessage.msgtype) ? R.drawable.filetype_audio : R.drawable.filetype_attachment);
            }
            imageTypeView.setBackgroundColor(Color.TRANSPARENT);


        } catch (Exception e) {
            MatrixUtility.LOG( "## getFileView() failed " + e.getMessage());
        }

    }

    public interface Callback{
        void onMediaClick(Event event);
    }
}
