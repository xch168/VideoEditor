package com.m4399.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.m4399.videoeditor.R;

public class VideoPreviewActivity extends AppCompatActivity
{
    private BaseVideoView mVideoView;

    private String mVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        mVideoPath = getIntent().getStringExtra("video_path");

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setDataSource(new DataSource(mVideoPath));
        mVideoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
        mVideoView.start();
    }

    public static void open(Context context, String videoPath)
    {
        Intent intent = new Intent(context, VideoPreviewActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }
}
