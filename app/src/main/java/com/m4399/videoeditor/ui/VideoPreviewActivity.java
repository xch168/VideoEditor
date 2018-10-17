package com.m4399.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.m4399.videoeditor.R;
import com.m4399.videoeditor.cover.ControllerCover;

public class VideoPreviewActivity extends AppCompatActivity
{
    private BaseVideoView mVideoView;

    private ReceiverGroup mReceiverGroup;

    private String mVideoPath;

    private OnVideoViewEventHandler mOnEventAssistHandler = new OnVideoViewEventHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        mVideoPath = getIntent().getStringExtra("video_path");

        mReceiverGroup = new ReceiverGroup(null);
        mReceiverGroup.addReceiver("controller_cover", new ControllerCover(this));

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setEventHandler(mOnEventAssistHandler);
        mVideoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
        mVideoView.setReceiverGroup(mReceiverGroup);
        mVideoView.setDataSource(new DataSource(mVideoPath));
        mVideoView.start();
    }

    public static void open(Context context, String videoPath)
    {
        Intent intent = new Intent(context, VideoPreviewActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }
}
