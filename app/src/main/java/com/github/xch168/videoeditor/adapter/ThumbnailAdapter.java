package com.github.xch168.videoeditor.adapter;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.media.FrameExtractor;
import com.github.xch168.videoeditor.media.ShareableBitmap;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbViewHolder> {

    // 帧间隔时长 5s
    private final int FRAME_INTERVAL_TIME = 5_000;

    private final Context mContext;

    private FrameExtractor mFrameExtractor;

    private long mDuration;

    public ThumbnailAdapter(Context context) {
        mContext = context;
        mFrameExtractor = new FrameExtractor();
    }

    @NonNull
    @Override
    public ThumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThumbViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_thumb_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbViewHolder holder, int position) {
        if (holder.mBitmap != null) {
            holder.mBitmap.release();
            holder.mBitmap = null;
        }

        ViewGroup.LayoutParams params = holder.thumbLayout.getLayoutParams();
        params.width = SizeUtil.dp2px(mContext, 65);
        holder.thumbLayout.setLayoutParams(params);

        holder.task = mFrameExtractor.newTask(holder, TimeUnit.SECONDS.toNanos(position * FRAME_INTERVAL_TIME));
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mDuration > 0) {
            count = (int) (mDuration / FRAME_INTERVAL_TIME);
        }
        Log.i("asdf", "count:" + count);
        return count;
    }

    public void setVideoPath(String videoPath) {
        mFrameExtractor.setDataSource(videoPath);
        mDuration = mFrameExtractor.getVideoDuration();
        Log.i("asdf", "duration:" + mDuration);
        notifyDataSetChanged();
    }

    static class ThumbViewHolder extends RecyclerView.ViewHolder implements FrameExtractor.Callback {
        FrameLayout thumbLayout;
        ImageView thumbImage;

        ShareableBitmap mBitmap;
        AsyncTask<?, ?, ?> task;

        ThumbViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbLayout = itemView.findViewById(R.id.thumb_layout);
            thumbImage = itemView.findViewById(R.id.video_thumb);
        }

        @Override
        public void onFrameExtracted(ShareableBitmap bitmap, long timestamp) {
            if (bitmap != null) {
                mBitmap = bitmap;
                thumbImage.setImageBitmap(bitmap.getData());
            }
        }
    }
}
