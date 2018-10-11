package com.m4399.videoeditor.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.m4399.videoeditor.R;
import com.m4399.videoeditor.adapter.VideoThumbnailAdapter;
import com.m4399.videoeditor.media.FrameExtractor;


public class VideoRangeSlider extends FrameLayout implements RangeSeekBar.OnRangeSeekBarChangeListener
{
    private static final String TAG = "VideoRangeSlider";

    private TextView mStartTimeView;
    private TextView mEndTimeView;
    private TextView mDurationView;

    private RecyclerView mVideoThumbnailGallery;
    private RangeSeekBar mRangeSeekBar;

    private VideoThumbnailAdapter mThumbnailAdapter;

    private FrameExtractor mFrameExtractor;

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
        mRangeSeekBar.setOnRangeSeekBarChangeListener(this);
    }

    public void setFrameProgress(float percent)
    {
        mRangeSeekBar.showFrameProgress(true);
        mRangeSeekBar.setFrameProgress(percent);
    }

    public void setDataSource(String path)
    {
        mFrameExtractor.setDataSource(path);

        mThumbnailAdapter.fetchDuration();
    }

    @Override
    public void onRangeChange(int witchSide, float leftValue, float rightValue)
    {
        Log.i(TAG, "left:" + leftValue + " right:" + rightValue);
    }

    @Override
    public void onStartTrackingTouch()
    {
        Log.i(TAG, "onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch()
    {
        Log.i(TAG, "onStopTrackingTouch");
    }
}
