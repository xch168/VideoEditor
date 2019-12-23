package com.github.xch168.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.entity.Video;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SelectVideoAdapter extends RecyclerView.Adapter<SelectVideoAdapter.VideoItemViewHolder> {

    private List<Video> mVideoList = new ArrayList<>();

    private OnItemRemoveListener mOnItemRemoveListener;

    public SelectVideoAdapter() {

    }

    @NonNull
    @Override
    public VideoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.select_video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoItemViewHolder holder, final int position) {
        final Video video = mVideoList.get(position);
        Glide.with(holder.picView.getContext()).load(video.getVideoPath()).into(holder.picView);
        holder.closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoList.remove(video);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mVideoList.size() - position);
                if (mOnItemRemoveListener != null) {
                    mOnItemRemoveListener.onItemRemove(position, video);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    public void add(Video video) {
        mVideoList.add(video);
        notifyDataSetChanged();
    }

    public boolean contains(Video video) {
        return mVideoList.contains(video);
    }

    static class VideoItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView picView;
        private ImageView closeView;

        VideoItemViewHolder(@NonNull View itemView) {
            super(itemView);

            picView = itemView.findViewById(R.id.iv);
            closeView = itemView.findViewById(R.id.ic_close);
        }
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        mOnItemRemoveListener = listener;
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int position, Video video);
    }
}
