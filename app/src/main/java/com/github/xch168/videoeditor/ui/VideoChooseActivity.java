package com.github.xch168.videoeditor.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.adapter.SelectVideoAdapter;
import com.github.xch168.videoeditor.adapter.VideoAdapter;
import com.github.xch168.videoeditor.entity.Video;
import com.github.xch168.videoeditor.helper.VideoManger;
import com.github.xch168.videoeditor.util.SpacingDecoration;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VideoChooseActivity extends BaseActivity implements VideoAdapter.OnItemClickListener {

    private RecyclerView mVideoListView;
    private RecyclerView mSelectVideoListView;

    private VideoAdapter mVideoAdapter;
    private SelectVideoAdapter mSelectVideoAdapter;

    @SuppressLint("HandlerLeak")
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mVideoAdapter.setVideoList((List<Video>) msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_choose);

        setTitle("选择视频");
        setBackBtnVisible(true);

        initVideoListView();
        initSelectVideoListView();

        loadVideoList();
    }

    private void initVideoListView() {
        mVideoAdapter = new VideoAdapter(this);
        mVideoAdapter.setOnItemClickListener(this);

        mVideoListView = findViewById(R.id.video_list_view);
        mVideoListView.setLayoutManager(new GridLayoutManager(this, 3));
        mVideoListView.addItemDecoration(new SpacingDecoration(this, 10, 10, true));
        mVideoListView.setAdapter(mVideoAdapter);
    }

    private void initSelectVideoListView() {
        mSelectVideoAdapter = new SelectVideoAdapter();

        mSelectVideoListView = findViewById(R.id.select_video_list);
        mSelectVideoListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSelectVideoListView.addItemDecoration(new SpacingDecoration(this, 5, 0, true));
        mSelectVideoListView.setAdapter(mSelectVideoAdapter);
    }

    private void loadVideoList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            VideoManger.getInstance().getAllVideo(new VideoManger.OnLoadCompletionListener() {
                @Override
                public void onLoadCompletion(List<Video> videoList) {
                    Message msg = new Message();
                    msg.obj = videoList;
                    mUiHandler.sendMessage(msg);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadVideoList();
        }
    }

    @Override
    public void onItemClick(int position, Video video) {
        mSelectVideoAdapter.add(video);
    }

    public static void open(Context context) {
        Intent intent = new Intent(context, VideoChooseActivity.class);
        context.startActivity(intent);
    }


}
