package com.github.xch168.videoeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.entity.Video;
import com.github.xch168.videoeditor.util.TimeUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoItemViewHolder> {
    private List<Video> mVideoList;

    private Context mContext;

    private OnItemClickListener mOnItemClickListener;

    public VideoAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public VideoItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VideoItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.video_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoItemViewHolder holder, final int i) {
        final Video video = mVideoList.get(i);
        holder.timeView.setText(TimeUtil.format(video.getDuration()));
        Glide.with(mContext).load(video.getVideoPath()).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(i, video);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideoList == null ? 0 : mVideoList.size();
    }

    public void setVideoList(List<Video> videoList) {
        mVideoList = videoList;
        notifyDataSetChanged();
    }

    static class VideoItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView timeView;

        VideoItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.iv);
            timeView = itemView.findViewById(R.id.tv_duration);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener  {
        void onItemClick(int position, Video video);
    }

}
