package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.FrameExtractor;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditorTrackView extends FrameLayout {
    private Context mContext;

    private Drawable mCursorDrawable;

    private EditorMediaTrackView mMediaTrackView;

    private int mMinScale = 0;
    private int mMaxScale = 3000;

    private int mInterval;
    private int mCount = 50;

    private int mPadding;

    private float mFactor = 0.1f;

    private HashMap<Integer, Bitmap> mThumbMap;

    private FrameExtractor mFrameExtractor;

    public EditorTrackView(@NonNull Context context) {
        this(context, null);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mInterval = SizeUtil.dp2px(context, 1);
        mPadding = SizeUtil.dp2px(context, 8);

        mThumbMap = new HashMap<>();

        mFrameExtractor = new FrameExtractor();

        initView();
    }

    private void initView() {
        setPadding(0, mPadding, 0, mPadding);
        initUIComponent();
        addUIComponent();

        setWillNotDraw(false);
    }

    private void initUIComponent() {
        mCursorDrawable = getResources().getDrawable(R.drawable.shape_cursor);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mMediaTrackView = new EditorMediaTrackView(mContext, this);
        mMediaTrackView.setLayoutParams(layoutParams);
    }

    private void initCursor() {
        int cursorWidth = SizeUtil.dp2px(mContext, 2);
        mCursorDrawable.setBounds((getWidth() - cursorWidth) / 2, 0, (getWidth() + cursorWidth) / 2, getHeight());
    }

    private void addUIComponent() {
        addView(mMediaTrackView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mMediaTrackView.layout(0, mPadding, right - left, bottom - top);
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
        mFrameExtractor.setDataSource(videoPath);
        mFrameExtractor.setDstSize(mMediaTrackView.getItemSize(), mMediaTrackView.getItemSize());
        mFrameExtractor.getFrameByInterval(5000, new FrameExtractor.Callback() {
            @Override
            public void onFrameExtracted(Bitmap bitmap, long timestamp) {
                int index = (int) (timestamp / 5000);
                mThumbMap.put(index, bitmap);
            }
        });
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

    public int getInterval() {
        return mInterval;
    }

    public void setInterval(int interval) {
        this.mInterval = interval;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public void setCurrentScale(float currentPos) {
        mMediaTrackView.setCurrentScale(currentPos);
    }

    public float getFactor() {
        return mFactor;
    }

    public void setFactor(float factor) {
        this.mFactor = factor;
    }

    public HashMap<Integer, Bitmap> getThumbMap() {
        return mThumbMap;
    }
}
