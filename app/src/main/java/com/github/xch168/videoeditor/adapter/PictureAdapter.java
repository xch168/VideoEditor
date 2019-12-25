package com.github.xch168.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.entity.Picture;

import java.util.List;

/**
 * Created by XuCanHui on 2019/12/25.
 */
public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureItemViewHolder> {

    private List<Picture> mPictureList;

    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public PictureItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PictureItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PictureItemViewHolder holder, final int position) {
        final Picture picture = mPictureList.get(position);
        Glide.with(holder.itemView).load(picture.getFilePath()).into(holder.iv);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position, picture);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPictureList == null ? 0 : mPictureList.size();
    }

    public void setPictureList(List<Picture> pictureList) {
        mPictureList = pictureList;
        notifyDataSetChanged();
    }

    static class PictureItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv;

        PictureItemViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener  {
        void onItemClick(int position, Picture picture);
    }
}
