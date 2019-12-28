package com.github.xch168.videoeditor.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.media.FrameExtractor;
import com.github.xch168.videoeditor.media.ShareableBitmap;

import java.util.concurrent.TimeUnit;

import static com.bumptech.glide.load.resource.bitmap.VideoDecoder.FRAME_OPTION;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbViewHolder> {

    // 帧间隔时长 5s
    private final int FRAME_INTERVAL_TIME_S = 5;
    private final int FRAME_INTERVAL_TIME_MS = FRAME_INTERVAL_TIME_S * 1000;

    private final Context mContext;

    private FrameExtractor mFrameExtractor;

    private long mDuration;
    private String mVideoPath;

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
        holder.thumbImage.setImageBitmap(null);
        if (holder.mBitmap != null) {
            holder.mBitmap.release();
            holder.mBitmap = null;
        }

//        holder.task = mFrameExtractor.newTask(holder, TimeUnit.SECONDS.toNanos(position * FRAME_INTERVAL_TIME_S));
        loadVideoScreenshot(holder.itemView.getContext(), mVideoPath, holder.thumbImage, TimeUnit.SECONDS.toMicros(position * FRAME_INTERVAL_TIME_S));
    }



    @SuppressLint("CheckResult")
    private void loadVideoScreenshot(final Context context, String uri, ImageView imageView, long frameTimeMicros) {
        RequestOptions requestOptions = RequestOptions.frameOf(frameTimeMicros);
        requestOptions.set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        requestOptions.dontTransform();
        Glide.with(mContext).load(uri).apply(requestOptions).into(imageView);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mDuration > 0) {
            count = (int) (mDuration / FRAME_INTERVAL_TIME_MS);
        }
        return count;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        mFrameExtractor.setDataSource(videoPath);
        mDuration = mFrameExtractor.getVideoDuration();
        notifyDataSetChanged();
    }

    static class ThumbViewHolder extends RecyclerView.ViewHolder implements FrameExtractor.Callback {
        ImageView thumbImage;

        ShareableBitmap mBitmap;
        AsyncTask<?, ?, ?> task;

        ThumbViewHolder(@NonNull View itemView) {
            super(itemView);

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
