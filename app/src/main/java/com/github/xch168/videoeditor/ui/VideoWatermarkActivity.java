package com.github.xch168.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.cover.ControllerCover;
import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;

public class VideoWatermarkActivity extends BaseActivity {

    private BaseVideoView mVideoView;

    private ReceiverGroup mReceiverGroup;

    private String mVideoPath;

    private OnVideoViewEventHandler mOnEventAssistHandler = new OnVideoViewEventHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_watermark);

        setTitle("视频水印");
        setBackBtnVisible(true);

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

    public void pictureWatermark(View view) {
        PictureListActivity.open(this);
    }

    public void textWatermark(View view) {

    }

    public void composeVideo(View view) {

    }

    public static void open(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoWatermarkActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }
}
