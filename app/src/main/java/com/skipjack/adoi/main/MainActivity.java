package com.skipjack.adoi.main;

import android.content.Intent;
import android.view.View;
import android.widget.RadioGroup;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.messaging.MessagingFragment;
import com.skipjack.adoi.connection.ConnectionFragment;
import com.skipjack.adoi.more.MoreListActivity;
import com.skipjack.adoi.permission.Permission;
import com.skipjack.adoi.permission.PermissionManager;
import com.skipjack.adoi.post.HomeFragment;
import com.skipjack.adoi.notifications.NotificationFragment;
import com.skipjack.adoi.search.SearchFragment;

import butterknife.BindView;

public class MainActivity extends BaseAppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.toolbar)Toolbar toolbar;
    @BindView(R.id.radioGroupMain) RadioGroup radioGroupMain;
    @BindView(R.id.layoutSearch)
    View layoutSearch;




    @Override
    public int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate() {
        setSupportActionBar(toolbar);
        radioGroupMain.setOnCheckedChangeListener(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutContainer, new SearchFragment())
                .commit();

        if (!PermissionManager.checkPermission(this, Permission.PERMISSION_WRITE_STORAGE,
                Permission.PERMISSION_READ_STORAGE)){
            PermissionManager.askPermissions(this, Permission.PERMISSION_WRITE_STORAGE,
                    Permission.PERMISSION_READ_STORAGE);
        }
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
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        layoutSearch.setVisibility(View.VISIBLE);
        if (checkedId == R.id.rbtnSearchMain){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutContainer, new SearchFragment())
                    .commit();
        }else if(checkedId == R.id.rbtnHome){
            layoutSearch.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutContainer, new HomeFragment())
                    .commit();
        }else if(checkedId == R.id.rbtnRequests){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutContainer, new ConnectionFragment())
                    .commit();
        }else if(checkedId == R.id.rbtnNotifications){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutContainer, new NotificationFragment())
                    .commit();
        }else if(checkedId == R.id.rbtnMessages){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutContainer, new MessagingFragment())
                    .commit();
        }else if(checkedId == R.id.rbtnMore){
            startActivity(new Intent(this, MoreListActivity.class));
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.layoutContainer, new MoreFragment())
//                    .commit();
        }
    }
}
