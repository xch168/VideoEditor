package com.github.xch168.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.VideoEditor;
import com.github.xch168.videoeditor.cover.ControllerCover;
import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;

import androidx.annotation.Nullable;

public class VideoWatermarkActivity extends BaseActivity {

    private BaseVideoView mVideoView;
    private ImageView mWatermarkView;
    private EditText mWatermarkEditor;

    private ReceiverGroup mReceiverGroup;

    private String mVideoPath;
    private long mVideoDuration;

    private String mWatermarkPath;

    private OnVideoViewEventHandler mOnEventAssistHandler = new OnVideoViewEventHandler();

    private VideoEditor.OnEditListener mOnEditListener = new VideoEditor.OnEditListener() {
        @Override
        public void onSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    Toast.makeText(VideoWatermarkActivity.this, "视频合成成功", Toast.LENGTH_SHORT).show();
                    VideoPreviewActivity.open(VideoWatermarkActivity.this, VideoEditor.getSavePath());
                }
            });
        }

        @Override
        public void onFailure() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoWatermarkActivity.this, "视频合成失败，请重试！", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onProgress(float progress) {
            updateProgress(progress);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_watermark);

        setTitle("视频水印");
        setBackBtnVisible(true);

        mVideoPath = getIntent().getStringExtra("video_path");
        mVideoDuration = getIntent().getLongExtra("video_duration", 0);

        mWatermarkView = findViewById(R.id.iv_watermark);
        mWatermarkEditor = findViewById(R.id.et_watermark);

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
        mWatermarkView.setVisibility(View.VISIBLE);
        mWatermarkEditor.setVisibility(View.GONE);
        mWatermarkEditor.setText("");

        PictureListActivity.openForResult(this, 123);
    }

    public void textWatermark(View view) {
        mWatermarkEditor.setVisibility(View.VISIBLE);
        mWatermarkView.setVisibility(View.GONE);
        mWatermarkEditor.setText("辉天神龙");
        mWatermarkPath = null;
    }

    public void composeVideo(View view) {
        if (TextUtils.isEmpty(mWatermarkPath) && TextUtils.isEmpty(mWatermarkEditor.getText())) {
            Toast.makeText(this, "请先添加水印", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();
        if (!TextUtils.isEmpty(mWatermarkPath)) {
            VideoEditor.addPictureWatermark(mVideoPath, mVideoDuration, mWatermarkPath, mOnEditListener);
        } else if (!TextUtils.isEmpty(mWatermarkEditor.getText())) {
            VideoEditor.addTextWatermark(mVideoPath, mVideoDuration, mWatermarkEditor.getText().toString(), mOnEditListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK) {
            mWatermarkPath = data.getStringExtra("picture_path");
            Glide.with(this).load(mWatermarkPath).into(mWatermarkView);
        }
    }

    public static void open(Context context, String videoPath, long videoDuration) {
        Intent intent = new Intent(context, VideoWatermarkActivity.class);
        intent.putExtra("video_path", videoPath);
        intent.putExtra("video_duration", videoDuration);
        context.startActivity(intent);
    }
}
