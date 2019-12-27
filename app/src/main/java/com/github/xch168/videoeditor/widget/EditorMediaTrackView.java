package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.adapter.ThumbnailAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EditorMediaTrackView extends FrameLayout {

    private Context mContext;
    private RecyclerView mRecyclerView;

    private ThumbnailAdapter mThumbnailAdapter;

    public EditorMediaTrackView(@NonNull Context context) {
        this(context, null);
    }

    public EditorMediaTrackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorMediaTrackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initView();
    }

    private void initView() {
        mThumbnailAdapter = new ThumbnailAdapter(mContext);

        mRecyclerView = new RecyclerView(mContext);
        mRecyclerView.setBackgroundColor(Color.WHITE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRecyclerView.setLayoutParams(layoutParams);
        mRecyclerView.setAdapter(mThumbnailAdapter);
    }

    public void setVideoPath(String path) {
        mThumbnailAdapter.setVideoPath(path);
    }
}
