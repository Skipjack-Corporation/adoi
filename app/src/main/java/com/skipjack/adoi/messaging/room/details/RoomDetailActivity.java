package com.skipjack.adoi.messaging.room.details;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTabHost;

import android.os.Bundle;
import android.widget.TextView;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.connection.ConnectionFragment;
import com.skipjack.adoi.messaging.room.file.FilesFragment;
import com.skipjack.adoi.messaging.room.people.PeopleFragment;
import com.skipjack.adoi.messaging.room.settings.SettingsFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class RoomDetailActivity extends BaseAppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.textActionbarTitle)
    TextView textActionbarTitle;
    @BindView(android.R.id.tabhost) FragmentTabHost tabHost;
    @Override
    public int getLayoutResource() {
        return R.layout.activity_room_detail;
    }

    @Override
    public void onCreate() {
        setSupportActionBar(toolbar);
        textActionbarTitle.setText("Room Details");
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ROOM_ID,getIntent().getStringExtra(Constants.KEY_ROOM_ID));
        tabHost.addTab(tabHost.newTabSpec("People")
                .setIndicator("People"), new PeopleFragment().getClass(), bundle);

        tabHost.addTab(tabHost.newTabSpec("Files")
                .setIndicator("Files"), new FilesFragment().getClass(), bundle);

        tabHost.addTab(tabHost.newTabSpec("Settings")
                .setIndicator("Settings"), new SettingsFragment().getClass(), bundle);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
    }

    @OnClick(R.id.textActionbarTitle)
    public void onBack(){
        finish();
    }
}
