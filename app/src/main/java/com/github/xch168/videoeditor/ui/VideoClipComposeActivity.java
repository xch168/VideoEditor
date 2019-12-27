package com.github.xch168.videoeditor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.widget.EditorTrackView;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;

public class VideoClipComposeActivity extends BaseActivity implements OnPlayerEventListener, View.OnClickListener {

    private static final int MSG_UPDATE_PROGRESS = 1;

    private BaseVideoView mVideoView;
    private ImageView mPlayBtn;
    private View mTouchView;
    private EditorTrackView mEditorTrackView;

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
        setContentView(R.layout.activity_video_clip_compose);
        setTitle("视频剪辑和合成");
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

        mEditorTrackView = findViewById(R.id.editor_track_view);
        mEditorTrackView.setVideoPath(videoPath);
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

    }

    public static void open(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoClipComposeActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }
}
