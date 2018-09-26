package com.m4399.videoeditor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.m4399.videoeditor.R;
import com.m4399.videoeditor.entity.Video;
import com.m4399.videoeditor.ui.VideoClipActivity;
import com.m4399.videoeditor.util.TimeUtil;

import java.util.List;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoItemViewHolder>
{
    private List<Video> mVideoList;

    private Context mContext;

    public VideoAdapter(Context context)
    {
        mContext = context;
    }

    @NonNull
    @Override
    public VideoItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        return new VideoItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.video_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoItemViewHolder holder, int i)
    {
        final Video video = mVideoList.get(i);
        holder.timeView.setText(TimeUtil.format(video.getDuration()));
        Glide.with(mContext).load(video.getVideoPath()).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                VideoClipActivity.open(mContext, video.getVideoPath());
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mVideoList == null ? 0 : mVideoList.size();
    }

    public void setVideoList(List<Video> videoList)
    {
        mVideoList = videoList;
    }

    static class VideoItemViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView timeView;

        VideoItemViewHolder(@NonNull View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.iv);
            timeView = itemView.findViewById(R.id.tv_duration);
        }
    }

}
