package com.m4399.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.m4399.videoeditor.R;
import com.m4399.videoeditor.widget.VideoThumbProgressBar;

public class EditThumbActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
                                                                    OnPlayerEventListener
{
    private static final String TAG = "EditThumbActivity";

    private BaseVideoView mVideoView;
    private VideoThumbProgressBar mVideoThumbProgressBar;
    private ImageView mThumbView;

    private long mTotalTime;

    private String mVideoPath;

    private long mCurrentPosition;

    private Bitmap mThumbBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_thumb);

        setTitle("编辑封面");

        mVideoPath = getIntent().getStringExtra("video_path");

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setOnPlayerEventListener(this);
        mVideoView.setDataSource(new DataSource(mVideoPath));
        mVideoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
        mVideoView.start();

        mVideoThumbProgressBar = findViewById(R.id.video_range_slider);
        mVideoThumbProgressBar.setOnSeekBarChangeListener(this);
        mVideoThumbProgressBar.setDataSource(mVideoPath);

        mThumbView = findViewById(R.id.iv_thumb);

        mTotalTime = mVideoThumbProgressBar.getTotalTime();
    }

    public static void open(Context context, String videoPath)
    {
        Intent intent = new Intent(context, EditThumbActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle)
    {
        switch (eventCode)
        {
            case PLAYER_EVENT_ON_START:
                mVideoView.pause();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        float percent = (float)progress / 1000;
        Log.i(TAG, "progress:" + progress + " percent:" + percent);

        progress = (int) (mTotalTime * percent);

        if (mVideoView != null)
        {
            mVideoView.seekTo(progress);
        }

        mCurrentPosition = progress;

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        loadThumb();
    }

    private void loadThumb()
    {
        Log.i(TAG, "currentPos:" + mCurrentPosition);
        if (mThumbBitmap != null)
        {
            mThumbView.setImageBitmap(null);
            mThumbBitmap.recycle();
        }
        mThumbBitmap = mVideoThumbProgressBar.getFrameExtractor().getFrameAt(mVideoView.getCurrentPosition() * 1000L);
        mThumbView.setImageBitmap(mThumbBitmap);
    }
}
