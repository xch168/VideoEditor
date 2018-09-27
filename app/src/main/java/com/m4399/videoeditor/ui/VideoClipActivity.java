package com.m4399.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.m4399.videoeditor.R;

public class VideoClipActivity extends AppCompatActivity implements OnPlayerEventListener,
                                                                    View.OnClickListener
{
    private BaseVideoView mVideoView;
    private ImageView mPlayBtn;
    private View mTouchView;

    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clip);

        setTitle("剪辑视频");

        videoPath = getIntent().getStringExtra("video_path");

        mPlayBtn = findViewById(R.id.iv);

        mTouchView = findViewById(R.id.touch_view);
        mTouchView.setOnClickListener(this);

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setDataSource(new DataSource(videoPath));
        mVideoView.setOnPlayerEventListener(this);
        mVideoView.start();
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle)
    {
        switch (eventCode)
        {
            case PLAYER_EVENT_ON_START:
            case PLAYER_EVENT_ON_RESUME:
                mPlayBtn.setVisibility(View.GONE);
                break;
            case PLAYER_EVENT_ON_PAUSE:
                mPlayBtn.setVisibility(View.VISIBLE);
                break;
            case PLAYER_EVENT_ON_PLAY_COMPLETE:
                mPlayBtn.setVisibility(View.VISIBLE);
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
                mVideoView.resume();
            }
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
}
