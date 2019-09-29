package com.skipjack.adoi.messaging;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.model.MessageDetail;
import com.skipjack.adoi.messaging.model.MessageSection;

import org.matrix.androidsdk.core.EventDisplay;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import mx.skipjack.service.MatrixService;
import mx.skipjack.service.MatrixUtility;

public class MessageDetailActivity extends BaseAppCompatActivity {

    @BindView(R.id.recyclerViewMessageDetail)
    RecyclerView recyclerView;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_message_detail;
    }

    @Override
    public void onCreate() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        String roomID = getIntent().getStringExtra(Constants.KEY_ROOM_ID);

        sortRoomMessages(roomID);

//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragmentContainer,
//                        MessagingDetailFragment.newInstance(roomID))
//                .commit();
//


    }

    private void sortRoomMessages(String roomID){
        RoomState roomState =  MatrixService.get().mxSession.getDataHandler().getStore().getRoom(roomID).getState();
        ArrayList<Event> roomMessages =new ArrayList<>(MatrixService.get().mxSession.getDataHandler().getStore().getRoomMessages(roomID));

        ArrayList<MessageDetail> messageDetails = new ArrayList<>();
        for (Event event: roomMessages){
            MatrixUtility.LOG("Event : "+event.toString());
            boolean addNew = true;
            int cnt = 0;
            for (MessageDetail messageDetail: messageDetails){
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(new Date(event.originServerTs));
                cal2.setTime(new Date(messageDetail.getTimeStamp()));
                boolean isSameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                if (messageDetail.getSenderId().equals(event.sender) && isSameDay){
                    messageDetail.addEvent(event);
                    messageDetails.set(cnt,messageDetail);
                    addNew = false;
                    break;
                }
                cnt ++;
            }
            if (messageDetails.size() == 0 || addNew){
                MessageDetail detail = new
                        MessageDetail(event.eventId,event.sender,roomState.getMemberName(event.sender),
                        MatrixService.get().mxSession.getDataHandler().getUser(event.sender).avatar_url,event.originServerTs);
                ArrayList<Event> eventList = new ArrayList<>();
                eventList.add(event);
                detail.setEvents(eventList);

                messageDetails.add(detail);

            }
        }

//        recyclerView.setAdapter(new MessagesDetailRecyclerAdapter2(roomMessages,roomState));
        recyclerView.setAdapter(new MessagesDetailRecyclerAdapter(messageDetails,roomState));


    }
}
