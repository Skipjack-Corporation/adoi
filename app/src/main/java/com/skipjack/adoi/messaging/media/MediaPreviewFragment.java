package com.skipjack.adoi.messaging.media;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonElement;
import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseFragment;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.core.callback.SimpleApiCallback;
import org.matrix.androidsdk.db.MXMediaCache;
import org.matrix.androidsdk.listeners.MXMediaDownloadListener;
import org.matrix.androidsdk.rest.model.message.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.OnClick;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.matrix.MatrixUtility;

import static support.skipjack.adoi.matrix.MatrixUtility.LOG_TAG;

public class MediaPreviewFragment extends BaseFragment {
    @BindView(R.id.imgImageMedia)
    ImageView imgImageMedia;

    @BindView(R.id.layoutVideoMedia)
    View layoutVideoMedia;

    @BindView(R.id.videoMedia)
    VideoView videoMedia;

    @BindView(R.id.imgThumbnailVideoMedia)
    ImageView imgThumbnailVideoMedia;

    @BindView(R.id.imgVideoPlay)
    ImageButton imgVideoPlay;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private SlidableMediaInfo mediaInfo;
    private MXMediaCache mMediasCache;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_media_preview;
    }

    @Override
    public void onCreateView() {
        if (getArguments() != null){
            mediaInfo = (SlidableMediaInfo) getArguments().getSerializable(Constants.ARG_MEDIA_DATA);
        }

        if (mediaInfo == null){
            return;
        }
        mMediasCache  = MatrixService.get().mxSession.getMediaCache();
        if (mediaInfo.mMessageType.equals(Message.MSGTYPE_IMAGE)){
            setImageView();
        }else if(mediaInfo.mMessageType.equals(Message.MSGTYPE_VIDEO)){
            setVideoView();
        }

    }
    private void setImageView(){
        layoutVideoMedia.setVisibility(View.GONE);
        imgImageMedia.setVisibility(View.VISIBLE);

        CircularProgressDrawable drawable = new CircularProgressDrawable(getContext());
        drawable.setStrokeWidth(5f);
        drawable.setCenterRadius(30f);
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
        drawable.start();
        Glide.with(this)
                .load(MatrixService.get().getDownloadableThumbnailUrl(mediaInfo.mMediaUrl,
                        AppUtility.getScreenSize(getActivity())[0]))
                .placeholder(drawable)
                .into(imgImageMedia);

    }
    private void setVideoView(){
        layoutVideoMedia.setVisibility(View.VISIBLE);
        imgImageMedia.setVisibility(View.GONE);
        CircularProgressDrawable drawable = new CircularProgressDrawable(getContext());
        drawable.setStrokeWidth(5f);
        drawable.setCenterRadius(30f);
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
        drawable.start();

        Glide.with(this)
                .load(MatrixService.get().getDownloadableThumbnailUrl(mediaInfo.mThumbnailUrl,
                        AppUtility.getScreenSize(getActivity())[0]))
                .placeholder(drawable)
                .into(imgThumbnailVideoMedia);


        imgVideoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMediasCache.isMediaCached(mediaInfo.mMediaUrl, mediaInfo.mMimeType)) {
                    mMediasCache.createTmpDecryptedMediaFile(mediaInfo.mMediaUrl, mediaInfo.mMimeType, mediaInfo.mEncryptedFileInfo, new SimpleApiCallback<File>() {
                        @Override
                        public void onSuccess(File file) {
                            if (null != file) {
                                playVideo(  file );
                            }
                        }
                    });
                }else {
                    downloadVideo();
                }
            }
        });

    }



    /**
     * Play a video.
     *
     * @param videoFile     the video file
     */
    private void playVideo(File videoFile) {
        if ((null != videoFile) && videoFile.exists()) {
            try {
                stopPlayingVideo();
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mediaInfo.mMimeType);

                if (null != extension) {
                    extension += "." + extension;
                }

                // copy the media to ensure that it is deleted while playing
                File dstFile = new File(getActivity().getCacheDir(), "sliderMedia" + extension);
                if (dstFile.exists()) {
                    dstFile.delete();
                }

                // Copy source file to destination
                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    // create only the
                    if (!dstFile.exists()) {
                        dstFile.createNewFile();

                        inputStream = new FileInputStream(videoFile);
                        outputStream = new FileOutputStream(dstFile);

                        byte[] buffer = new byte[1024 * 10];
                        int len;
                        while ((len = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "## playVideo() : failed " + e.getMessage(), e);
                    dstFile = null;
                } finally {
                    // Close resources
                    try {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "## playVideo() : failed " + e.getMessage(), e);
                    }
                }

                // update the source
                videoMedia.setVideoPath(dstFile.getAbsolutePath());
                // hide the thumbnail
                imgThumbnailVideoMedia.setVisibility(View.GONE);

                // let's playing
                videoMedia.start();

            } catch (Exception e) {
                Log.e(LOG_TAG, "## playVideo() : videoView.start(); failed " + e.getMessage(), e);
            }
        }
    }
    /**
     * Stop any playing video
     */
    public void stopPlayingVideo() {
        if (null != videoMedia && videoMedia.isPlaying()) {
            videoMedia.stopPlayback();
            imgThumbnailVideoMedia.setVisibility(View.VISIBLE);
        }
    }

    private void downloadVideo(){
        imgVideoPlay.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        String downloadId = mMediasCache.downloadMedia(getContext(),
                MatrixService.get().homeServerConfig, mediaInfo.mMediaUrl, mediaInfo.mMimeType, mediaInfo.mEncryptedFileInfo);

        mMediasCache.addDownloadListener(downloadId, new MXMediaDownloadListener(){
            @Override
            public void onDownloadError(String downloadId, JsonElement jsonElement) {
                super.onDownloadError(downloadId, jsonElement);
                progressBar.setVisibility(View.GONE);
                AppUtility.toast(getActivity(),"Failed to load video.");
            }

            @Override
            public void onDownloadProgress(String downloadId, DownloadStats stats) {
                super.onDownloadProgress(downloadId, stats);
                progressBar.setProgress(stats.mProgress);

            }

            @Override
            public void onDownloadComplete(String downloadId) {
                super.onDownloadComplete(downloadId);
                progressBar.setVisibility(View.GONE);
                if (mMediasCache.isMediaCached(mediaInfo.mMediaUrl, mediaInfo.mMimeType)) {

                    mMediasCache.createTmpDecryptedMediaFile(mediaInfo.mMediaUrl, mediaInfo.mMimeType, mediaInfo.mEncryptedFileInfo, new SimpleApiCallback<File>() {
                        @Override
                        public void onSuccess(File file) {
                            if (null != file) {
                                imgVideoPlay.setVisibility(View.GONE);
                                playVideo(  file );
                            }
                        }
                    });
                }else {
                    imgVideoPlay.setVisibility(View.VISIBLE);
                    AppUtility.toast(getActivity(),"Failed to load video.");
                }
            }
        });

    }










}
