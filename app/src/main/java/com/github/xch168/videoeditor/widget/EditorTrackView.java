package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.xch168.videoeditor.util.SizeUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, SizeUtil.dp2px(mContext, 44));
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.bottomMargin = SizeUtil.dp2px(mContext, 5);
        mMediaTrackView.setLayoutParams(layoutParams);
    }

    private void addUIComponent() {
        addView(mMediaTrackView);
    }

    public void setVideoPath(String videoPath) {
        mMediaTrackView.setVideoPath(videoPath);
    }

}
