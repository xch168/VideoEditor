package com.github.xch168.videoeditor.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.adapter.VideoAdapter;
import com.github.xch168.videoeditor.entity.Video;
import com.github.xch168.videoeditor.util.SpacingDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VideoListActivity extends BaseActivity implements VideoAdapter.OnItemClickListener {
    private static final int MSG_NOTIFY_DATA_CHANGED = 100;

    private RecyclerView mRecyclerView;

    private VideoAdapter mVideoAdapter;

    private List<Video> mVideoList = new ArrayList<>();

    private int mIntentType;

    @SuppressLint("HandlerLeak")
    private Handler mUiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_NOTIFY_DATA_CHANGED) {
                mVideoAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        setTitle("视频列表");
        setBackBtnVisible(true);

        mIntentType = getIntent().getIntExtra("type", MainActivity.TYPE_VIDEO_CLIP);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addItemDecoration(new SpacingDecoration(this, 10, 10, true));
        mVideoAdapter = new VideoAdapter(this);
        mVideoAdapter.setOnItemClickListener(this);
        mVideoAdapter.setVideoList(mVideoList);
        mRecyclerView.setAdapter(mVideoAdapter);

        if (Build.VERSION.SDK_INT < 23) {
            loadData();
        } else {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == 0) {
            loadData();
        }
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadVideoList();
            }
        }).start();
    }

    private void loadVideoList() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DATE_ADDED + " desc");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        } else {
            return;
        }
        int fileNum = cursor.getCount();
        for(int count = 0; count < fileNum; count++) {
            String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            File file = new File(videoPath);

            if (!file.exists()) {
                cursor.moveToNext();
                continue;
            }
            Video video = new Video();
            video.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
            video.setVideoName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
            video.setVideoPath(videoPath);

            String videoPathLowerCase = videoPath.toLowerCase();
            if (!videoPathLowerCase.endsWith("mp4") && !videoPathLowerCase.endsWith("3gp") && !videoPathLowerCase.endsWith("mkv") && !videoPathLowerCase.endsWith("avi")) {
                cursor.moveToNext();
                continue;
            }
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            if (duration == 0) {
                duration = getVideoDurationFromMediaMetadata(video.getVideoPath());
            }
            if (duration > 0) {
                video.setDuration(duration);
                video.setVideoSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
                mVideoList.add(video);
            }
            cursor.moveToNext();
        }
        cursor.close();

        mUiHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
    }

    private long getVideoDurationFromMediaMetadata(String path) {
        long duration = 0;

        if (!TextUtils.isEmpty(path)) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(path);
                duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return duration;
    }

    @Override
    public void onItemClick(int position, Video video) {
        switch (mIntentType) {
            case MainActivity.TYPE_VIDEO_CLIP:
                VideoClipActivity.open(this, video.getVideoPath());
                break;
            case MainActivity.TYPE_VIDEO_COVER:
                EditThumbActivity.open(this, video.getVideoPath());
                break;
            case MainActivity.TYPE_VIDEO_CLIP_COMPOSE:
                VideoClipComposeActivity.open(this, video.getVideoPath());
                break;
            case MainActivity.TYPE_VIDEO_WATERMARK:
                VideoWatermarkActivity.open(this, video.getVideoPath());
        }
    }

    public static void open(Context context, int type) {
        Intent intent = new Intent(context, VideoListActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }
}
