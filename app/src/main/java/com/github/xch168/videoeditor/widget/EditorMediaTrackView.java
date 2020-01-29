package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.OverScroller;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.FrameExtractor;
import com.github.xch168.videoeditor.util.SizeUtil;

public class EditorMediaTrackView extends View {
    private static final int THUMB_INTERVAL = 5000;

    private Context mContext;

    private OverScroller mScroller;

    private VelocityTracker mVelocityTracker;
    private int mMinVelocity;
    private int mMaxVelocity;

    private int mMinPosition;
    private int mMaxPosition;

    private int mMinScale = 0;
    private int mMaxScale = 1000;
    private int mCurrentScale = 0;
    private int mLength;

    private int mThumbSize;
    private int mDrawOffset;

    private Paint mTextPaint;
    private Paint mThumbPaint;
    private Bitmap mDefaultThumb;

    private SparseArray<Bitmap> mThumbMap;
    private FrameExtractor mFrameExtractor;

    private float mLastX;

    private boolean mIsTrackingByUser = false;
    private OnTrackViewChangeListener mOnTrackViewChangeListener;
    private OnScrollChangeListener mOnScrollChangeListener;

    public EditorMediaTrackView(Context context) {
        super(context);
        mContext = context;
        mThumbMap = new SparseArray<>();

        mThumbSize = calcThumbSize();
        mDrawOffset = mThumbSize;

        initView();
    }

    private int calcThumbSize() {
        int thumbSize = SizeUtil.dp2px(mContext, 38);
        if (thumbSize % 2 != 0) {
            thumbSize--;
        }
        return thumbSize;
    }

    private void initView() {
        mScroller = new OverScroller(mContext);

        mVelocityTracker = VelocityTracker.obtain();
        mMinVelocity = ViewConfiguration.get(mContext).getScaledMinimumFlingVelocity();
        mMaxVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTextPaint.setTextSize(28);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDefaultThumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_default);
        mDefaultThumb = Bitmap.createScaledBitmap(mDefaultThumb, mThumbSize, mThumbSize, true);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                goToScale(mCurrentScale);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mThumbSize, MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshSize();
    }

    private void refreshSize() {
        if (mFrameExtractor != null) {
            int count = Math.round((float) mFrameExtractor.getVideoDuration() / THUMB_INTERVAL);
            mLength = count * mThumbSize;
        }
        int halfWidth = getWidth() / 2;
        mMinPosition = -halfWidth;
        mMaxPosition = mLength - halfWidth;
        mMaxScale = mLength;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawThumbnail(canvas);
    }

    private void drawThumbnail(Canvas canvas) {
        int start = (getScrollX() - mDrawOffset) + mMinScale;
        int end = (getScrollX() + mDrawOffset + getWidth()) + mMinScale;
        Log.i("asdf", "scrollX:" + getScrollX() + " start:" + start + " end:" + end + " ==> itemSize:" + mThumbSize + " length:" + mLength + " || minP:" + mMinPosition + " maxP:" + mMaxPosition);
        for (int scale = start; scale <= end; scale++) {
            if (isADrawScale(scale)) {
                int locationX = scale - mMinScale;
                Log.i("asdf", "draw:" + scale + " pos:" + locationX);
                int index = positionToThumbIndex(locationX);
                Bitmap drawThumb = mDefaultThumb;
                if (mThumbMap != null && mThumbMap.get(index) != null) {
                    drawThumb = mThumbMap.get(index);
                }
                canvas.drawBitmap(drawThumb, locationX, 0, mThumbPaint);
                canvas.drawText("" + (index + 1), locationX + 28, 38, mTextPaint);
            }
        }
    }

    private boolean isADrawScale(int scale) {
        return scale >= mMinScale && scale <= mMaxScale && scale % mThumbSize == 0 && scale != mMaxScale;
    }

    private int positionToThumbIndex(int pos) {
        return (pos - mMinScale) / mThumbSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        ViewGroup parent = (ViewGroup) getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastX = currentX;
                if (mOnTrackViewChangeListener != null) {
                    mOnTrackViewChangeListener.onStartTrackingTouch();
                }
                parent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                mIsTrackingByUser = true;
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) moveX, 0);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(velocityX) > mMinVelocity) {
                    fling(-velocityX);
                }
                recycleVelocityTracker();
                parent.requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                recycleVelocityTracker();
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < mMinPosition) {
            x = mMinPosition;
        }
        if (x > mMaxPosition) {
            x = mMaxPosition;
        }
        if (x != getScrollX()) {
            super.scrollTo(x, y);
        }
        mCurrentScale = scrollXToScale(x);
        if (mOnTrackViewChangeListener != null && mIsTrackingByUser) {
            mOnTrackViewChangeListener.onScaleChanged(mCurrentScale);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangeListener != null) {
            mOnScrollChangeListener.onScrollChange(l);
        }
    }

    private void fling(int vX) {
        mScroller.fling(getScrollX(), 0, vX, 0, mMinPosition, mMaxPosition, 0, 0);
        invalidate();
    }

    private void goToScale(int scale) {
        mCurrentScale = scale;
        scrollTo(scaleToScrollX(mCurrentScale), 0);
    }

    private int scaleToScrollX(int scale) {
        return (scale - mMinScale) + mMinPosition;
    }

    private int scrollXToScale(int scrollX) {
        return (scrollX - mMinPosition) + mMinScale;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setCurrentScale(int scale) {
        mIsTrackingByUser = false;
        goToScale(scale);
    }

    public int getCurrentScale() {
        return mCurrentScale;
    }

    public int getMaxScale() {
        return mMaxScale;
    }

    public void setVideoPath(String path) {
        loadVideoThumbnails(path);
    }

    private void loadVideoThumbnails(String path) {
        if (mFrameExtractor == null) {
            mFrameExtractor = new FrameExtractor();
        }
        mFrameExtractor.setVideoPath(path);
        mFrameExtractor.setDstSize(mThumbSize, mThumbSize);
        mFrameExtractor.getFrameByInterval(THUMB_INTERVAL, new FrameExtractor.Callback() {
            @Override
            public void onFrameExtracted(Bitmap bitmap, long timestamp) {
                int index = (int) (timestamp / THUMB_INTERVAL);
                mThumbMap.put(index, bitmap);
                invalidate();
            }
        });
        refreshSize();
    }

    public void setOnTrackViewChangeListener(OnTrackViewChangeListener listener) {
        mOnTrackViewChangeListener = listener;
    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        mOnScrollChangeListener = listener;
    }

    public interface OnTrackViewChangeListener {
        void onStartTrackingTouch();
        void onScaleChanged(int scale);
    }

    public interface OnScrollChangeListener {
        void onScrollChange(int scrollX);
    }

}
