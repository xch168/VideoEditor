package com.github.xch168.videoeditor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.VideoEditor;
import com.github.xch168.videoeditor.widget.RangeSeekBar;
import com.github.xch168.videoeditor.widget.VideoRangeSlider;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;

public class VideoClipActivity extends BaseActivity implements OnPlayerEventListener,
        View.OnClickListener,
        RangeSeekBar.OnRangeChangeListener {
    private static final String TAG = "VideoClipActivity";

    private static final int MSG_UPDATE_PROGRESS = 1;

    private BaseVideoView mVideoView;
    private ImageView mPlayBtn;
    private View mTouchView;
    private VideoRangeSlider mVideoRangeSlider;

    private String videoPath;

    private long mStartTime;
    private long mEndTime;
    private long mTotalTime;

    private boolean needPlayStart;

    @SuppressLint("HandlerLeak")
    private Handler mUiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    updateProgress();

                    mUiHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clip);

        setTitle("剪辑视频");
        setBackBtnVisible(true);

        videoPath = getIntent().getStringExtra("video_path");

        mPlayBtn = findViewById(R.id.iv);

        mTouchView = findViewById(R.id.touch_view);
        mTouchView.setOnClickListener(this);

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setDataSource(new DataSource(videoPath));
        mVideoView.setOnPlayerEventListener(this);
        mVideoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
        mVideoView.start();

        mVideoRangeSlider = findViewById(R.id.video_range_slider);
        mVideoRangeSlider.setVideoView(mVideoView);
        mVideoRangeSlider.setOnRangeSeekBarChangeListener(this);
        mVideoRangeSlider.setDataSource(videoPath);

        mTotalTime = mVideoRangeSlider.getTotalTime();
        mEndTime = mTotalTime;

    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case PLAYER_EVENT_ON_START:
            case PLAYER_EVENT_ON_RESUME:
                mPlayBtn.setVisibility(View.GONE);
                mUiHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                break;
            case PLAYER_EVENT_ON_PAUSE:
            case PLAYER_EVENT_ON_PLAY_COMPLETE:
                mPlayBtn.setVisibility(View.VISIBLE);
                mUiHandler.removeMessages(MSG_UPDATE_PROGRESS);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (!mVideoView.isInPlaybackState()) {
            mVideoView.start();
            return;
        }
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        } else {
            if (mVideoView.getState() == IPlayer.STATE_PLAYBACK_COMPLETE) {
                mVideoView.start(0);
            } else {
                if (!needPlayStart) {
                    mVideoView.resume();
                } else {
                    mVideoView.seekTo((int) mStartTime);
                    mVideoView.start();
                }

            }
        }

    }

    private void updateProgress() {
        if (mVideoRangeSlider != null) {
            mVideoRangeSlider.setFrameProgress(mVideoView.getCurrentPosition() / (float) mVideoView.getDuration());
        }
    }

    public void startClipVideo(View view) {
        showProgressDialog();
        VideoEditor.cropVideo(videoPath, mStartTime, mEndTime, new VideoEditor.OnEditListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Toast.makeText(VideoClipActivity.this, "视频处理成功", Toast.LENGTH_SHORT).show();
                        VideoPreviewActivity.open(VideoClipActivity.this, getSavePath());
                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                });
            }

            @Override
            public void onProgress(float progress) {
                updateProgress(progress);
            }
        });
    }

    public static void open(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoClipActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }

    private static String getSavePath() {
        return Environment.getExternalStorageDirectory().getPath() + "/VideoEditor/out.mp4";
    }

    @Override
    public void onKeyDown(int type) {
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    public void onKeyUp(int type, int lThumbIndex, int rThumbIndex) {
        needPlayStart = true;
    }

    @Override
    public void onRangeChange(RangeSeekBar rangeSeekBar, int type, int lThumbIndex, int rThumbIndex) {
        mStartTime = (long) ((float)lThumbIndex / 100 * mTotalTime);
        mEndTime = (long) ((float)rThumbIndex / 100 * mTotalTime);
        long duration = mEndTime - mStartTime;

        mVideoRangeSlider.setStartTime(mStartTime);
        mVideoRangeSlider.setEndTime(mEndTime);
        mVideoRangeSlider.setDuration(duration);

        if (mVideoView != null) {
            switch (type) {
                case RangeSeekBar.TYPE_LEFT:
                    mVideoView.seekTo((int) mStartTime);
                    break;
                case RangeSeekBar.TYPE_RIGHT:
                    mVideoView.seekTo((int) mEndTime);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }
}
