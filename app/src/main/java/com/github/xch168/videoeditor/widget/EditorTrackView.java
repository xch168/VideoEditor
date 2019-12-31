package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Size;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.util.SizeUtil;

public class EditorTrackView extends FrameLayout {
    private Context mContext;

    private Drawable mCursorDrawable;

    private EditorMediaTrackView mMediaTrackView;

    private int mMinScale = 0;
    private int mMaxScale = 1000;

    private int mInterval = 18;

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

        setWillNotDraw(false);
    }

    private void initUIComponent() {
        mCursorDrawable = getResources().getDrawable(R.drawable.shape_cursor);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMediaTrackView = new EditorMediaTrackView(mContext, this);
        mMediaTrackView.setLayoutParams(layoutParams);
    }

    private void initCursor() {
        int cursorWidth = SizeUtil.dp2px(mContext, 2);
        int cursorHeight = SizeUtil.dp2px(mContext, 54);
        mCursorDrawable.setBounds((getWidth() - cursorWidth) / 2, getHeight() - cursorHeight, (getWidth() + cursorWidth) / 2, getHeight());
    }

    private void addUIComponent() {
        addView(mMediaTrackView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mMediaTrackView.layout(0, 0, right - left, bottom - top);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        mCursorDrawable.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initCursor();
    }

    public void setVideoPath(String videoPath) {
        mMediaTrackView.setVideoPath(videoPath);
    }

    public int getMinScale() {
        return mMinScale;
    }

    public void setMinScale(int minScale) {
        this.mMinScale = minScale;
    }

    public int getMaxScale() {
        return mMaxScale;
    }

    public void setMaxScale(int maxScale) {
        this.mMaxScale = maxScale;
    }

    public int getInterval()
    {
        return mInterval;
    }

    public void setInterval(int interval)
    {
        this.mInterval = interval;
    }
}
