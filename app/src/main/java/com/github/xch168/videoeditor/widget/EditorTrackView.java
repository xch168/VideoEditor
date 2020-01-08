package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.FrameExtractor;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditorTrackView extends FrameLayout {
    private Context mContext;

    private Drawable mCursorDrawable;

    private ImageView mLeftThumb;
    private ImageView mRightThumb;
    private EditorMediaTrackView mMediaTrackView;

    private Paint mBorderPaint;
    private Paint mMaskPaint;

    private int mMinScale = 0;
    private int mMaxScale = 3000;

    private int mScreenWidth;
    private int mPadding;

    private HashMap<Integer, Bitmap> mThumbMap = new HashMap<>();

    private FrameExtractor mFrameExtractor;

    private Rect mBounds = new Rect();

    public EditorTrackView(@NonNull Context context) {
        this(context, null);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mPadding = SizeUtil.dp2px(context, 7);

        mFrameExtractor = new FrameExtractor();

        initView();
    }

    private void initView() {
        setPadding(0, mPadding, 0, mPadding);

        initPaint();
        initUIComponent();
        addUIComponent();

        setWillNotDraw(false);
    }

    private void initPaint() {
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(getResources().getColor(R.color.colorAccent));

        mMaskPaint = new Paint();
        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(getResources().getColor(R.color.colorMask));
    }

    private void initUIComponent() {
        mCursorDrawable = getResources().getDrawable(R.drawable.shape_cursor);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, SizeUtil.dp2px(mContext, 40));
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLeftThumb = new ImageView(mContext);
        mLeftThumb.setImageResource(R.drawable.ic_progress_left);
        mLeftThumb.setScaleType(ImageView.ScaleType.FIT_END);
        mLeftThumb.setLayoutParams(layoutParams);
        mLeftThumb.setX(200);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, SizeUtil.dp2px(mContext, 40));
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mRightThumb = new ImageView(mContext);
        mRightThumb.setImageResource(R.drawable.ic_progress_right);
        mRightThumb.setScaleType(ImageView.ScaleType.FIT_START);
        mRightThumb.setLayoutParams(layoutParams);
        mRightThumb.setX(400);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mMediaTrackView = new EditorMediaTrackView(mContext, this);
        mMediaTrackView.setThumbMap(mThumbMap);
        mMediaTrackView.setLayoutParams(layoutParams);
    }

    private void initCursor() {
        int cursorWidth = SizeUtil.dp2px(mContext, 2);
        mCursorDrawable.setBounds((getWidth() - cursorWidth) / 2, 0, (getWidth() + cursorWidth) / 2, getHeight());
    }

    private void addUIComponent() {
        removeAllViews();

        addView(mMediaTrackView);
        addView(mLeftThumb);
        addView(mRightThumb);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initCursor();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mMediaTrackView.layout(0, mPadding, right - left, bottom - top);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

        }
        mMediaTrackView.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        drawBorder(canvas);
//        drawMask(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        mCursorDrawable.draw(canvas);
    }

    private void drawBorder(Canvas canvas) {
        Rect leftThumbBounds = new Rect();
        Rect rightThumbBounds = new Rect();
        mLeftThumb.getHitRect(leftThumbBounds);
        mRightThumb.getHitRect(rightThumbBounds);
        // top
        canvas.drawRect(leftThumbBounds.right, leftThumbBounds.top + 1, rightThumbBounds.left,  leftThumbBounds.top + 2, mBorderPaint);
        // bottom
        canvas.drawRect(leftThumbBounds.right, leftThumbBounds.bottom - 1, rightThumbBounds.left, leftThumbBounds.bottom, mBorderPaint);
    }

    private void drawMask(Canvas canvas) {
        Rect bounds = new Rect();
        // left
        mLeftThumb.getHitRect(bounds);
        canvas.drawRect(0, bounds.top, bounds.right, bounds.bottom, mMaskPaint);
        // right
        mRightThumb.getHitRect(bounds);
        canvas.drawRect(bounds.left, bounds.top, 1000, bounds.bottom, mMaskPaint);
    }

    private void setBounds(Rect bounds, int left, int top, int right, int bottom) {
        bounds.left = left;
        bounds.top = top;
        bounds.right = right;
        bounds.bottom = bottom;
    }

    private void moveLeftThumb(int x) {

    }

    private void moveRightThumb(int x) {

    }

    private int getLeftThumbPosition() {
        Object tag = mLeftThumb.getTag();
        if (tag instanceof Integer) {
            return (Integer) tag;
        }
        return 0;
    }

    private int getRightThumbPosition() {
        Object tag = mRightThumb.getTag();
        if (tag instanceof Integer) {
            return (Integer) tag;
        }
        return 0;
    }

    public void setVideoPath(String videoPath) {
        mFrameExtractor.setDataSource(videoPath);
        mFrameExtractor.setDstSize(mMediaTrackView.getItemSize(), mMediaTrackView.getItemSize());
        mFrameExtractor.getFrameByInterval(5000, new FrameExtractor.Callback() {
            @Override
            public void onFrameExtracted(Bitmap bitmap, long timestamp) {
                int index = (int) (timestamp / 5000);
                mThumbMap.put(index, bitmap);
            }
        });
        mMediaTrackView.setItemCount((int) (mFrameExtractor.getVideoDuration() / 5000));
    }

    public int getMinScale() {
        return mMinScale;
    }

    public void setMinScale(int minScale) {
        this.mMinScale = minScale;
    }

    public int getMaxScale() {
        return mMediaTrackView.getMaxScale();
    }

    public void setCurrentScale(float currentPos) {
        mMediaTrackView.setCurrentScale((int) currentPos);
    }

    public HashMap<Integer, Bitmap> getThumbMap() {
        return mThumbMap;
    }

    public void setOnTrackViewChangeListener(EditorMediaTrackView.OnTrackViewChangeListener listener) {
        mMediaTrackView.setOnTrackViewChangeListener(listener);
    }
}
