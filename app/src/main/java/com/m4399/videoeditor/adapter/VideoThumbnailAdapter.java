package com.m4399.videoeditor.adapter;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.m4399.videoeditor.R;
import com.m4399.videoeditor.media.FrameExtractor;
import com.m4399.videoeditor.media.ShareableBitmap;

import java.util.concurrent.TimeUnit;

public class VideoThumbnailAdapter extends RecyclerView.Adapter<VideoThumbnailAdapter.ThumbViewHolder>
{
    private Context mContext;

    private static final int DEFAULT_FRAME_COUNT = 10;

    private FrameExtractor mFrameExtractor;

    private long mDuration;
    private long mDurationLimit = Integer.MAX_VALUE;

    private int screenWidth;
    private int right;
    private int maxRight;
    private float perSecond;

    public VideoThumbnailAdapter(Context context, FrameExtractor frameExtractor)
    {
        mContext = context;
        mFrameExtractor = frameExtractor;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    @Override
    public ThumbViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        return new ThumbViewHolder(LayoutInflater.from(mContext).inflate(R.layout.video_thumb_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ThumbViewHolder holder, int position)
    {
        perSecond = (float)mDuration / getItemCount()/1000;

        if (holder.mBitmap != null)
        {
            holder.mBitmap.release();
            holder.mBitmap = null;
        }

        ViewGroup.LayoutParams params = holder.thumbLayout.getLayoutParams();
        params.width = screenWidth / getItemCount();
        holder.thumbLayout.setLayoutParams(params);

        holder.task = mFrameExtractor.newTask(holder, TimeUnit.SECONDS.toNanos((long) (position * perSecond)));
    }

    @Override
    public int getItemCount()
    {
        if (mDuration == 0)
        {
            return 0;
        }
        if ((int) (mDuration / 1000) > mDurationLimit)
        {
            return Math.round((mDuration / 1000 / mDurationLimit) * DEFAULT_FRAME_COUNT);
        }
        else
        {
            return DEFAULT_FRAME_COUNT;
        }
    }

    public long fetchDuration()
    {
        mDuration = mFrameExtractor.getVideoDuration();
        notifyDataSetChanged();
        return mDuration;
    }

    static class ThumbViewHolder extends RecyclerView.ViewHolder implements FrameExtractor.Callback
    {
        FrameLayout thumbLayout;
        ImageView thumbImage;

        ShareableBitmap mBitmap;
        AsyncTask<?, ?, ?> task;

        ThumbViewHolder(@NonNull View itemView)
        {
            super(itemView);

            thumbLayout = itemView.findViewById(R.id.thumb_layout);
            thumbImage = itemView.findViewById(R.id.video_thumb);
        }

        @Override
        public void onFrameExtracted(ShareableBitmap bitmap, long timestamp)
        {
            if (bitmap != null)
            {
                mBitmap = bitmap;
                thumbImage.setImageBitmap(bitmap.getData());
            }
        }
    }
}
