package com.skipjack.adoi.messaging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.skipjack.adoi.R;
import com.skipjack.adoi.database.AppSharedPreference;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.db.MXMediaCache;
import org.matrix.androidsdk.fragments.MatrixMessageListFragment;

import mx.skipjack.service.MatrixService;


public class MessagingDetailFragment extends MatrixMessageListFragment {

    public static MessagingDetailFragment newInstance(String roomId) {
        MessagingDetailFragment f = new MessagingDetailFragment();
        Bundle args = getArguments(AppSharedPreference.get().getLoginCredential().userId, roomId,
                R.layout.fragment_messaging_detail);

        args.putString(ARG_EVENT_ID, MatrixService.get().mxSession.getDataHandler().getStore().getSummary(roomId).getLatestReceivedEvent().eventId);
        args.putString(ARG_PREVIEW_MODE_ID, PREVIEW_MODE_READ_ONLY);

        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public MXMediaCache getMXMediaCache() {
        return MatrixService.get().mxSession.getMediaCache();
    }

    @Override
    public MXSession getSession(String matrixId) {
        return MatrixService.get().mxSession;
    }

    @Override
    public AbstractMessagesAdapter createMessagesAdapter() {
        return new MessagingDetailAdapter(getContext(),R.layout.adapter_message_sender);
    }
}
