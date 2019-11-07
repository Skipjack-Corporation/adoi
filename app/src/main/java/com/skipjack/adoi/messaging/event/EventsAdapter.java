package com.skipjack.adoi.messaging.event;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.event.EventViewHolder;
import com.skipjack.adoi.messaging.media.MediaPreviewActivity;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;

import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.message.ImageMessage;
import org.matrix.androidsdk.rest.model.message.Message;
import org.matrix.androidsdk.rest.model.message.VideoMessage;

import java.util.ArrayList;
import java.util.List;

import support.skipjack.adoi.local_storage.AppSharedPreference;

public class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> implements EventViewHolder.Callback {

    private List<Event> list = new ArrayList<>();
    private Callback callback;
    public EventsAdapter(List<Event> list, RoomState roomState, Callback callback) {
        this.list = list;
        this.callback = callback;
    }
    public void update(List<Event> list, RoomState roomState) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<Event> getList() {
        return list;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0){
            return new EventViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_message_content_you,parent,false),parent,true,this);
        }
        return new EventViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_message_content,parent,false),parent,false,this);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        Event event = list.get(position);
        if (event.sender.equals(AppSharedPreference.get().getLoginCredential().getUserId())){
            //you
            return 0;
        }
        //end user
        return 1;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onMediaClick(Event currentEvent) {
       callback.onMediaContentClick(currentEvent);
    }

    public interface Callback{
        void onMediaContentClick(Event event);
    }
}
