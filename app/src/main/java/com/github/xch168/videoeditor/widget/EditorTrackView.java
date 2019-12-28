package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.xch168.videoeditor.util.SizeUtil;

public class EditorTrackView extends FrameLayout {
    private Context mContext;

    private EditorMediaTrackView mMediaTrackView;

    public EditorTrackView(@NonNull Context context) {
        this(context, null);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initView();
    }

    private void initView() {
        initUIComponent();
        addUIComponent();
    }

    private void initUIComponent() {
        mMediaTrackView = new EditorMediaTrackView(mContext);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtil.dp2px(mContext, ViewGroup.LayoutParams.WRAP_CONTENT));
        mMediaTrackView.setLayoutParams(layoutParams);
    }

    private void addUIComponent() {
        addView(mMediaTrackView);
    }

    public void setVideoPath(String videoPath) {
        mMediaTrackView.setVideoPath(videoPath);
    }

}
