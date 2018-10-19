package com.m4399.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.m4399.videoeditor.R;
import com.m4399.videoeditor.adapter.VideoThumbnailAdapter;
import com.m4399.videoeditor.media.FrameExtractor;

public class VideoThumbSeekBar extends FrameLayout
{
    private RecyclerView mVideoThumbnailGallery;
    private ImageSeekBar mSeekBar;

    private VideoThumbnailAdapter mThumbnailAdapter;

    private FrameExtractor mFrameExtractor;

    private long mTotalTime;

    public VideoThumbSeekBar(Context context)
    {
        super(context);

        initView();
    }

    public VideoThumbSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initView();
    }

    public VideoThumbSeekBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.video_thumb_seek_bar, this);

        mFrameExtractor = new FrameExtractor();

        mVideoThumbnailGallery = findViewById(R.id.video_thumbnails);
        mVideoThumbnailGallery.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mThumbnailAdapter = new VideoThumbnailAdapter(getContext(), mFrameExtractor);
        mVideoThumbnailGallery.setAdapter(mThumbnailAdapter);

        mSeekBar = findViewById(R.id.seek_bar);
    }

    public void setDataSource(String path)
    {
        mFrameExtractor.setDataSource(path);

        mTotalTime = mThumbnailAdapter.fetchDuration();
    }

    public long getTotalTime()
    {
        return mTotalTime;
    }

    public void setThumbBitmap(Bitmap bitmap)
    {
        mSeekBar.setThumbBitmap(bitmap);
    }


    public void setOnSeekBarChangeListener(ImageSeekBar.OnSeekBarChangeListener listener)
    {
        mSeekBar.setOnSeekBarChangeListener(listener);
    }
}
