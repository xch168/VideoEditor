package com.m4399.videoeditor.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.m4399.videoeditor.R;


public class VideoRangeSlider extends FrameLayout implements RangeSeekBar.OnRangeSeekBarChangeListener
{
    private static final String TAG = "VideoRangeSlider";

    private TextView mStartTimeView;
    private TextView mEndTimeView;
    private TextView mDurationView;

    private LinearLayout mVideoThumbnailGallery;

    private RangeSeekBar mRangeSeekBar;

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

        mStartTimeView = findViewById(R.id.tv_start_time);
        mEndTimeView = findViewById(R.id.tv_end_time);
        mDurationView = findViewById(R.id.tv_duration);

        mVideoThumbnailGallery = findViewById(R.id.video_thumbnails);

        mRangeSeekBar = findViewById(R.id.range_seek_bar);
        mRangeSeekBar.setOnRangeSeekBarChangeListener(this);
    }

    private void loadVideoThumbnails()
    {

    }

    public void setFrameProgress(float percent)
    {
        mRangeSeekBar.showFrameProgress(true);
        mRangeSeekBar.setFrameProgress(percent);
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
