package com.m4399.videoeditor.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.m4399.videoeditor.R;
import com.m4399.videoeditor.adapter.VideoThumbnailAdapter;
import com.m4399.videoeditor.media.FrameExtractor;
import com.m4399.videoeditor.util.TimeUtil;


public class VideoRangeSlider extends FrameLayout
{
    private static final String TAG = "VideoRangeSlider";

    private TextView mStartTimeView;
    private TextView mEndTimeView;
    private TextView mDurationView;

    private BaseVideoView mVideoView;

    private RecyclerView mVideoThumbnailGallery;
    private RangeSeekBar mRangeSeekBar;

    private VideoThumbnailAdapter mThumbnailAdapter;

    private FrameExtractor mFrameExtractor;

    private long mTotalTime;

    public VideoRangeSlider(@NonNull Context context)
    {
        super(context);

        initView();
    }

    public VideoRangeSlider(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        initView();
    }

    public VideoRangeSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.video_range_slider, this);

        mFrameExtractor = new FrameExtractor();

        mStartTimeView = findViewById(R.id.tv_start_time);
        mEndTimeView = findViewById(R.id.tv_end_time);
        mDurationView = findViewById(R.id.tv_duration);

        mVideoThumbnailGallery = findViewById(R.id.video_thumbnails);
        mVideoThumbnailGallery.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mThumbnailAdapter = new VideoThumbnailAdapter(getContext(), mFrameExtractor);
        mVideoThumbnailGallery.setAdapter(mThumbnailAdapter);

        mRangeSeekBar = findViewById(R.id.range_seek_bar);
    }

    public void setFrameProgress(float percent)
    {
        mRangeSeekBar.showFrameProgress(true);
        mRangeSeekBar.setFrameProgress(percent);
    }

    public void setDataSource(String path)
    {
        mFrameExtractor.setDataSource(path);

        mTotalTime = mThumbnailAdapter.fetchDuration();
        setEndTime(mTotalTime);
        setDuration(mTotalTime);
    }

    public void setVideoView(BaseVideoView videoView)
    {
        mVideoView = videoView;
    }

    public void setStartTime(long startTime)
    {
        mStartTimeView.setText(TimeUtil.format(startTime));
    }

    public void setEndTime(long endTime)
    {
        mEndTimeView.setText(TimeUtil.format(endTime));
    }

    public void setDuration(long duration)
    {
        mDurationView.setText(TimeUtil.format(duration));
    }

    public long getTotalTime()
    {
        return mTotalTime;
    }

    public void setOnRangeSeekBarChangeListener(RangeSeekBar.OnRangeSeekBarChangeListener listener)
    {
        mRangeSeekBar.setOnRangeSeekBarChangeListener(listener);
    }
}
