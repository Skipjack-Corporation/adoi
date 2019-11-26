package com.skipjack.adoi.messaging.room.file;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.media.SlidableMediaInfo;

import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.TokensChunkEvents;
import org.matrix.androidsdk.rest.model.message.ImageMessage;
import org.matrix.androidsdk.rest.model.message.Message;
import org.matrix.androidsdk.rest.model.message.VideoMessage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixService;

public class FilesFragment extends BaseFragment {
    private static final int MESSAGES_PAGINATION_LIMIT = 50;

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.layoutNoResult) View layoutNoResult;

    private Room mRoom;
    private String currentToken = null;
    private FilesAdapter adapter;
    private ArrayList<SlidableMediaInfo> mediaInfoList = new ArrayList<>();
    @Override
    public int getLayoutResource() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onCreateView() {
        String roomId = getArguments().getString(Constants.KEY_ROOM_ID);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);

        if (roomId != null)
        {
            showProgressDialog();
            mRoom = MatrixService.get().getRoom(roomId);
            requestMediaListHistory();
        }
        else layoutNoResult.setVisibility(View.VISIBLE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    requestMediaListHistory();
                }
            }
        });

    }

    public void requestMediaListHistory(){
        List<Event> events = new ArrayList<>();
        mRoom.requestServerRoomHistory(currentToken, MESSAGES_PAGINATION_LIMIT, new MatrixCallback<TokensChunkEvents>() {
            @Override
            public void onAPISuccess(TokensChunkEvents data) {
                currentToken = data.end;
//                if (data.start.equals(data.end)){
//                    //end of list
//                }else {
//                    events.addAll(data.chunk);
//                    requestMediaListHistory();
//                }
                hideProgressDialog();
                if (data.chunk.size() == 0) {
                    return;
                }
                if (data.chunk.size() > 0){
                    events.addAll(data.chunk);

                }
                if (mediaInfoList.size() < 10) {
                    showProgressDialog();
                    requestMediaListHistory();
                }
                prepareMediaInfoListing(events);
            }

            @Override
            public void onAPIFailure(String errorMessage) {
                hideProgressDialog();
            }
        });
    }

    public void prepareMediaInfoListing(List<Event> eventList){
        for (Event event: eventList){
            if (checkDuplicate(event.eventId)) return;
            Message message = JsonUtils.toMessage(event.getContent());
            if (Message.MSGTYPE_IMAGE.equals(message.msgtype)) {
                ImageMessage imageMessage = (ImageMessage) message;
                SlidableMediaInfo info = new SlidableMediaInfo();
                info.id = event.eventId;
                info.mMessageType = Message.MSGTYPE_IMAGE;
                info.mFileName = imageMessage.body;
                info.mMediaUrl = imageMessage.getUrl();
                info.mRotationAngle = imageMessage.getRotation();
                info.mOrientation = imageMessage.getOrientation();
                info.mMimeType = imageMessage.getMimeType();
                info.mEncryptedFileInfo = imageMessage.file;
                info.fileSize = imageMessage.info.size;
                info.date = event.getOriginServerTs();
                mediaInfoList.add(info);
            } else if (Message.MSGTYPE_VIDEO.equals(message.msgtype)) {
                VideoMessage videoMessage = (VideoMessage) message;
                SlidableMediaInfo info = new SlidableMediaInfo();
                info.id = event.eventId;
                info.mMessageType = Message.MSGTYPE_VIDEO;
                info.mFileName = videoMessage.body;
                info.mMediaUrl = videoMessage.getUrl();
                info.mThumbnailUrl = (null != videoMessage.info) ? videoMessage.info.thumbnail_url : null;
                info.mMimeType = videoMessage.getMimeType();
                info.mEncryptedFileInfo = videoMessage.file;
                info.fileSize = videoMessage.info.size;
                info.date = event.getOriginServerTs();
                mediaInfoList.add(info);
            }
        }
        populateMediaInfoListing();
    }
    public boolean checkDuplicate(String id){
        for (SlidableMediaInfo info: mediaInfoList){
            if (id.equals(info.id)) return true;
        }
        return false;
    }
    public void populateMediaInfoListing(){
        if (mediaInfoList.size() == 0) return;
        else {
            layoutNoResult.setVisibility(View.GONE);
            if (adapter == null)
                recyclerView.setAdapter(new FilesAdapter(mediaInfoList));
            else {
                adapter.update(mediaInfoList);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
