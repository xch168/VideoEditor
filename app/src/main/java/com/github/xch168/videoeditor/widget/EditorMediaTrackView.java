package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.github.xch168.videoeditor.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditorMediaTrackView extends View {
    private Context mContext;

    private EditorTrackView mParent;

    //加入放大倍数来防止精度丢失而导致无限绘制
    protected static final int SCALE_TO_PX_FACTOR = 100;
    private static final int MIN_SCROLL_DP = 1;
    private float minScrollPx = MIN_SCROLL_DP;

    private OverScroller mScroller;

    private VelocityTracker mVelocityTracker;
    // 最大惯性速度
    private int mMaxVelocity;
    // 最小惯性速度
    private int mMinVelocity;

    private EdgeEffect mStartEdgeEffect;
    private EdgeEffect mEndEdgeEffect;
    private int mEdgeLength;

    private float mLastX = 0;

    private int mLength;
    private int mMaxLength;
    private int mHalfWidth;
    private int mMinPosition;
    private int mMaxPosition;

    private int mDrawOffset;

    private float mCurrentPosition = 0;

    private Paint mScalePaint;

    public EditorMediaTrackView(@NonNull Context context, EditorTrackView parent) {
        super(context);
        mContext = context;
        mParent = parent;

        initView();
    }

    private void initView() {
        mMaxLength = mParent.getMaxScale() - mParent.getMinScale();

        mDrawOffset = 10 * mParent.getInterval() / 2;

        mScroller = new OverScroller(mContext);
        mVelocityTracker = VelocityTracker.obtain();
        mMaxVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();
        mMinVelocity = ViewConfiguration.get(mContext).getScaledMinimumFlingVelocity();

        mScalePaint = new Paint();
        mScalePaint.setColor(getResources().getColor(R.color.colorAccent));
        mScalePaint.setStrokeWidth(5);
        mScalePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawScale(canvas);
    }

    private void drawScale(Canvas canvas) {
        float start = (getScaleX() - mDrawOffset) / mParent.getInterval() + mParent.getMinScale();
        float end = (getScrollX() + canvas.getWidth() + mDrawOffset) / mParent.getInterval() + mParent.getMinScale();
        for (float i = start; i <= end; i++) {
            //将要刻画的刻度转化为位置信息
            float locationX = (i - mParent.getMinScale()) * mParent.getInterval();

            if (i >= mParent.getMinScale() && i <= mParent.getMaxScale()) {
                canvas.drawLine(locationX, 0, locationX, 5, mScalePaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshSize();
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
                parent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) moveX, 0);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(velocityX) > mMinVelocity) {
                    fling(-velocityX);
                } else {
                    scrollBackToCurrentPos();
                }
                recycleVelocityTracker();
                parent.requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                scrollBackToCurrentPos();
                recycleVelocityTracker();
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void fling(int vX) {
        mScroller.fling(getScrollX(), 0 , vX, 0, 0, 0, 0, 0);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x != getScrollX()) {
            super.scrollTo(x, y);
        }
        mCurrentPosition = scrollXToScale(x);
    }

    private float scrollXToScale(int scrollX) {
        return ((float) (scrollX - mMinPosition) / mLength) * mMaxLength + mParent.getMinScale();
    }

    private void scrollBackToCurrentPos() {
        scrollBackToCurrentPos(Math.round(mCurrentPosition));
    }

    private void scrollBackToCurrentPos(int pos) {
        float scrollX = scaleToScrollFloatX(pos);
        int dx = Math.round((scrollX - SCALE_TO_PX_FACTOR * getScrollX()) / SCALE_TO_PX_FACTOR);
        if (dx > minScrollPx) {
            mScroller.startScroll(getScrollX(), getScrollY(), dx, 0, 500);
            invalidate();
        } else {
            scrollBy(dx, 0);
        }

    }

    private float scaleToScrollFloatX(int pos) {
        return (((pos - mParent.getMinScale()) / mMaxLength * mLength * SCALE_TO_PX_FACTOR) + mMinPosition * SCALE_TO_PX_FACTOR);
    }

    private void refreshSize() {
        mLength = 0;
        mHalfWidth = getWidth() / 2;
        mMinPosition = -mHalfWidth;
        mMinPosition = mLength - mHalfWidth;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setVideoPath(String path) {

    }
}
