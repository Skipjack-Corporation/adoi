package com.skipjack.adoi.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;

import com.dropbox.core.v2.team.ActiveWebSession;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.base.BaseApplication;
import com.skipjack.adoi.messaging.MessagingFragment;
import com.skipjack.adoi.connection.ConnectionFragment;
import com.skipjack.adoi.more.MoreFragment;
import com.skipjack.adoi.more.MoreListActivity;
import com.skipjack.adoi.permission.Permission;
import com.skipjack.adoi.permission.PermissionManager;
import com.skipjack.adoi.post.HomeFragment;
import com.skipjack.adoi.notifications.NotificationFragment;
import com.skipjack.adoi.search.SearchFragment;
import com.skipjack.adoi.view.BadgeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import support.skipjack.adoi.local_storage.AppSharedPreference;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.service.MatrixEventService;

public class MainActivity extends BaseAppCompatActivity implements TabHost.OnTabChangeListener {

    @BindView(R.id.toolbar)Toolbar toolbar;
    @BindView(android.R.id.tabhost) FragmentTabHost tabHost;
    @BindView(R.id.layoutSearch)View layoutSearch;

    private List<View> tabViews = new ArrayList<>();
    @Override
    public int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate() {
        setSupportActionBar(toolbar);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        addTabView(R.drawable.selector_main_search,0, new SearchFragment());
        addTabView(R.drawable.selector_main_home,0, new HomeFragment());
        addTabView(R.drawable.selector_main_notif,0, new NotificationFragment());
        addTabView(R.drawable.selector_main_request,0, new ConnectionFragment());
        addTabView(R.drawable.selector_main_messages, AppSharedPreference.get().getMessaingCount(),
                new MessagingFragment());
        addTabView(R.drawable.selector_main_more,0, new MoreFragment());

        tabHost.setOnTabChangedListener(this);


        if (!PermissionManager.checkPermission(this, Permission.PERMISSION_WRITE_STORAGE,
                Permission.PERMISSION_READ_STORAGE)){
            PermissionManager.askPermissions(this, Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_READ_STORAGE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

         if (tabHost.getCurrentTab() == 5) tabHost.setCurrentTab(0);

         updateMessageCount();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionManager.REQUESTCODE_ASK_PERMISSION){
            if (!PermissionManager.checkPermission(this, Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_READ_STORAGE)){

            }
        }
    }
    @Override
    public void onTabChanged(String tabId) {
        if (tabHost.getCurrentTab() == 5)
            startActivity(new Intent(this, MoreListActivity.class));

    }

    private void addTabView(int imgResId, int count, Fragment fragment){
        View view = getLayoutInflater().inflate(R.layout.layout_main_tab,null);
        ImageView imgLogo = view.findViewById(R.id.imgLogo);
        imgLogo.setImageResource(imgResId);

        View viewNotif = view.findViewById(R.id.viewNotif);
        if (count > 0){
            viewNotif.setVisibility(View.VISIBLE);
        }else viewNotif.setVisibility(View.GONE);

        tabHost.addTab(tabHost.newTabSpec(fragment.getClass().getSimpleName())
                .setIndicator(view), fragment.getClass(), null);
        tabViews.add(view);
    }

    public void updateMessageCount(){
        View view =  tabHost.getTabWidget().getChildTabViewAt(4);
        if (AppSharedPreference.get().getMessaingCount() > 0){
            view.findViewById(R.id.viewNotif).setVisibility(View.VISIBLE);
        }else view.findViewById(R.id.viewNotif).setVisibility(View.GONE);

    }



}
