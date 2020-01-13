package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.OverScroller;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.Map;

public class EditorMediaTrackView extends View {
    private Context mContext;

    private EditorTrackView mParent;

    private OverScroller mScroller;

    private VelocityTracker mVelocityTracker;
    private int mMinVelocity;
    private int mMaxVelocity;

    private int mMinPosition;
    private int mMaxPosition;

    private int mMinScale = 0;
    private int mMaxScale = 1000;
    private int mScaleLength;
    private float mCurrentScale = 0;

    private int mHalfWidth;

    private int mLength;
    private int mItemSize;
    private int mItemCount;

    private int mInterval;

    private Paint mThumbPaint;
    private Bitmap mDefaultThumb;

    private int mDrawOffset;

    private Paint mTextPaint;

    private Map<Integer, Bitmap> mThumbMap;

    private float mLastX;

    private boolean mIsTrackingByUser = false;
    private OnTrackViewChangeListener mOnTrackViewChangeListener;
    private OnScrollChangeListener mOnScrollChangeListener;

    public EditorMediaTrackView(Context context, EditorTrackView parent) {
        super(context);
        mContext = context;
        mParent = parent;

        initView();
    }

    private void initView() {
        mItemSize = SizeUtil.dp2px(mContext, 38);
        if (mItemSize % 2 != 0) {
            mItemSize--;
        }

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
        mDefaultThumb = Bitmap.createScaledBitmap(mDefaultThumb, mItemSize, mItemSize, true);

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
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize, mode);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshSize();
    }

    private void refreshSize() {
        mLength = mItemSize * mItemCount;
        mScaleLength = mLength;
        mHalfWidth = getWidth() / 2;
        mMinPosition = -mHalfWidth;
        mMaxPosition = mLength - mHalfWidth;
        mInterval = mLength / mScaleLength;
        mMaxScale = mLength;
        mDrawOffset = mItemSize / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawThumbnail(canvas);
    }

    private void drawThumbnail(Canvas canvas) {
        float start = (getScrollX() - mDrawOffset) + mMinScale;
        float end = (getScrollX() + mDrawOffset + getWidth()) + mMinScale;
        Log.i("asdf", "scrollX:" + getScrollX() + " start:" + start + " end:" + end + " ==> itemSize:" + mItemSize + " itemCount:" + mItemCount  + " length:" + mLength + " || minP:" + mMinPosition + " maxP:" + mMaxPosition);
        for (float scale = start; scale <= end; scale++) {
            if (isADrawScale(scale)) {
                float locationX = (scale - mMinScale) * mInterval;
                Log.i("asdf", "draw:" + scale + " pos:" + locationX);
                int index = positionToThumbIndex(locationX);
                Bitmap drawThumb = mDefaultThumb;
                if (mThumbMap != null && mThumbMap.get(index) != null) {
                    drawThumb = mThumbMap.get(index);
                }
                canvas.drawBitmap(drawThumb, locationX, 0, mThumbPaint);
//                canvas.drawText("" + (index + 1), locationX + 28, 38, mTextPaint);
            }
        }
    }

    private boolean isADrawScale(float scale) {
        return scale >= mMinScale && scale <= mMaxScale && scale % mItemSize == 0 && scale != mMaxScale;
    }

    private int positionToThumbIndex(float pos) {
        return (int) ((pos - mMinScale) / mItemSize);
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
            mOnTrackViewChangeListener.onScaleChanged((int) mCurrentScale);
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

    private void goToScale(float scale) {
        mCurrentScale = scale;
        scrollTo(scaleToScrollX(mCurrentScale), 0);
    }

    private int scaleToScrollX(float scale) {
        return (int) ((scale - mMinScale) / mScaleLength * mLength + mMinPosition);
    }

    private float scrollXToScale(int scrollX) {
        return ((float)(scrollX - mMinPosition) / mLength) * mScaleLength + mMinScale;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setThumbMap(Map<Integer, Bitmap> map) {
        mThumbMap = map;
    }

    public void setCurrentScale(int scale) {
        mIsTrackingByUser = false;
        goToScale(scale);
    }

    public int getCurrentScale() {
        return (int) mCurrentScale;
    }

    public int getItemSize() {
        return mItemSize;
    }

    public void setItemCount(int count) {
        mItemCount = count;
        mMaxScale = mItemCount * mItemSize;
    }

    public int getMaxScale() {
        return mMaxScale;
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
