package com.m4399.videoeditor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.m4399.videoeditor.R;
import com.m4399.videoeditor.widget.RangeSeekBar;
import com.m4399.videoeditor.widget.VideoRangeSlider;

public class VideoClipActivity extends AppCompatActivity implements OnPlayerEventListener,
                                                                    View.OnClickListener,
                                                                    RangeSeekBar.OnRangeSeekBarChangeListener
{
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
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_UPDATE_PROGRESS:
                    updateProgress();

                    mUiHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clip);

        setTitle("剪辑视频");

        videoPath = getIntent().getStringExtra("video_path");

        Log.i("asdf", "video:" + videoPath);

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
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle)
    {
        switch (eventCode)
        {
            case PLAYER_EVENT_ON_START:
            case PLAYER_EVENT_ON_RESUME:
                mPlayBtn.setVisibility(View.GONE);
                mUiHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                break;
            case PLAYER_EVENT_ON_PAUSE:
                mPlayBtn.setVisibility(View.VISIBLE);
                mUiHandler.removeMessages(MSG_UPDATE_PROGRESS);
                break;
            case PLAYER_EVENT_ON_PLAY_COMPLETE:
                mPlayBtn.setVisibility(View.VISIBLE);
                mUiHandler.removeMessages(MSG_UPDATE_PROGRESS);
                break;
        }
    }

    @Override
    public void onClick(View v)
    {
        if (!mVideoView.isInPlaybackState())
        {
            mVideoView.start();
            return;
        }
        if (mVideoView.isPlaying())
        {
            mVideoView.pause();
        }
        else
        {
            if (mVideoView.getState() == IPlayer.STATE_PLAYBACK_COMPLETE)
            {
                mVideoView.start(0);
            }
            else
            {
                if (!needPlayStart)
                {
                    mVideoView.resume();
                }
                else
                {
                    mVideoView.seekTo((int) mStartTime);
                    mVideoView.start();
                }

            }
        }

    }

    private void updateProgress()
    {
        if (mVideoRangeSlider != null)
        {
            mVideoRangeSlider.setFrameProgress(mVideoView.getCurrentPosition() / (float)mVideoView.getDuration());
        }
    }

    public void startClipVideo(View view)
    {

    }

    public static void open(Context context, String videoPath)
    {
        Intent intent = new Intent(context, VideoClipActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }

    @Override
    public void onRangeChange(int witchSide, float leftValue, float rightValue)
    {
        Log.i(TAG, "left:" + leftValue + " right:" + rightValue);
        mStartTime = (long) (leftValue / 100 * mTotalTime);
        mEndTime = (long) (rightValue / 100 * mTotalTime);
        long duration = mEndTime - mStartTime;

        mVideoRangeSlider.setStartTime(mStartTime);
        mVideoRangeSlider.setEndTime(mEndTime);
        mVideoRangeSlider.setDuration(duration);

        if (mVideoView != null)
        {
            switch (witchSide)
            {
                case 0:
                    mVideoView.seekTo((int) mStartTime);
                    break;
                case 1:
                    mVideoView.seekTo((int) mEndTime);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch()
    {
        Log.i(TAG, "onStartTrackingTouch");
        if (mVideoView != null)
        {
            mVideoView.pause();
        }
    }

    @Override
    public void onStopTrackingTouch()
    {
        Log.i(TAG, "onStopTrackingTouch");
        needPlayStart = true;
    }
}
