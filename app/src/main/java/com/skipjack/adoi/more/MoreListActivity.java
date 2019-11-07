package com.skipjack.adoi.more;

import android.graphics.Bitmap;

import com.skipjack.adoi.R;

import support.skipjack.adoi.matrix.MatrixService;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.utility.AppUtility;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MoreListActivity extends BaseAppCompatActivity {

    @BindView(R.id.imgProfPic)
    CircleImageView imgProfPic;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_more_list;
    }

    @Override
    public void onCreate() {
//        imgProfPic.setImageCircularDrawable(getDrawable(R.drawable.img_example_2_small));

        Bitmap icon = AppUtility.getBitmapFromVectorDrawable(
                this,R.drawable.ic_placeholder_fill);

        MatrixService.get().mxSession.getMediaCache().loadAvatarThumbnail(MatrixService.get().homeServerConfig,
                imgProfPic,
                MatrixService.get().mxSession.getMyUser().avatar_url,
                getResources().getDimensionPixelSize(R.dimen.more_profpic_size),icon);

    }
}
