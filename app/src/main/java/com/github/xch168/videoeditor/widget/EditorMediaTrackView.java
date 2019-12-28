package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.xch168.videoeditor.adapter.ThumbnailAdapter;

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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRecyclerView.setLayoutParams(layoutParams);
        mRecyclerView.setAdapter(mThumbnailAdapter);

        addView(mRecyclerView);
    }

    public void setVideoPath(String path) {
        mThumbnailAdapter.setVideoPath(path);
    }
}
