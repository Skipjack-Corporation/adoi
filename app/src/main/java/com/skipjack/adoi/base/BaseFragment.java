package com.skipjack.adoi.base;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skipjack.adoi.R;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {
    public abstract int getLayoutResource();
    public abstract void onCreateView();

    public View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutResource(),container,false);
        ButterKnife.bind(this,rootView);
        onCreateView();

        return rootView;
    }

    /**
     * Initialize and show progressdialog
     * */
    private Dialog progressdialog;

    public void showProgressDialog() {
        if (progressdialog == null) {
            progressdialog = new Dialog(getActivity());
            progressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressdialog.setContentView(R.layout.layout_progressdialog);
            progressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        progressdialog.setCancelable(false);
        progressdialog.show();
    }
    /**
     * hide progressdialog
     * */
    public void hideProgressDialog() {
        try {
            if (progressdialog.isShowing()) {
                progressdialog.dismiss();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
