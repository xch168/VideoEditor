package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.OverScroller;

import com.github.xch168.videoeditor.R;

import androidx.annotation.NonNull;

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

    private float mLastX = 0;

    private int mLength;
    private int mMaxLength;
    private int mHalfWidth;
    private int mMinPosition;
    private int mMaxPosition;

    private int mDrawOffset;

    private float mCurrentScale = 0;

    private Paint mScalePaint;
    private Paint mBigScalePaint;
    private Paint mTextPaint;

    private Paint mBitmapPaint;
    private Bitmap mDefaultBitmap;

    private int mItemSize;

    private int mCount;

    public EditorMediaTrackView(@NonNull Context context, EditorTrackView parent) {
        super(context);
        mContext = context;
        mParent = parent;

        mCount = mParent.getCount();

        mItemSize = mCount * mParent.getInterval();
        mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_default);
        mDefaultBitmap = Bitmap.createScaledBitmap(mDefaultBitmap, mItemSize, mItemSize, true);

        initView();
    }

    private void initView() {
        mMaxLength = mParent.getMaxScale() - mParent.getMinScale();

        mDrawOffset = mCount * mParent.getInterval() / 2;

        minScrollPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_SCROLL_DP, mContext.getResources().getDisplayMetrics());

        mScroller = new OverScroller(mContext);
        mVelocityTracker = VelocityTracker.obtain();
        mMaxVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();
        mMinVelocity = ViewConfiguration.get(mContext).getScaledMinimumFlingVelocity();

        mScalePaint = new Paint();
        mScalePaint.setColor(getResources().getColor(R.color.colorAccent));
        mScalePaint.setStrokeWidth(1);
        mScalePaint.setStrokeCap(Paint.Cap.ROUND);

        mBigScalePaint = new Paint();
        mBigScalePaint.setColor(getResources().getColor(R.color.colorAccent));
        mBigScalePaint.setStrokeWidth(1);
        mBigScalePaint.setStrokeCap(Paint.Cap.ROUND);

        mBitmapPaint = new Paint(1);
        mBitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint();
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTextPaint.setTextSize(14);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                goToScale(mCurrentScale);
            }
        });
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            if (!mScroller.computeScrollOffset()) {
                int currentScale = Math.round(mCurrentScale);
                if ((Math.abs(mCurrentScale - currentScale) > 0.001f)) {
                    scrollBackToCurrentScale(currentScale);
                }
            }
            postInvalidate();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int mode = MeasureSpec.getMode(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize, mode);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawScale(canvas);
    }

    private void drawScale(Canvas canvas) {
        float start = (getScrollX() - mDrawOffset) / mParent.getInterval() + mParent.getMinScale();
        float end = (getScrollX() + canvas.getWidth() + mDrawOffset) / mParent.getInterval() + mParent.getMinScale();
        for (float i = start; i <= end; i++) {
            //将要刻画的刻度转化为位置信息
            float locationX = (i - mParent.getMinScale()) * mParent.getInterval();

            if (i >= mParent.getMinScale() && i <= mParent.getMaxScale()) {
                if (i % mCount == 0) {
                    if ((int) i != mParent.getMaxScale()) {
                        canvas.drawBitmap(mDefaultBitmap, locationX, 0f, mBitmapPaint);
                    }
                    canvas.drawLine(locationX, 0, locationX, 30, mBigScalePaint);
                    canvas.drawText(valueOfScale(i, mParent.getFactor()), locationX, 40, mTextPaint);
                } else {
                    canvas.drawLine(locationX, 0, locationX, 15, mScalePaint);
                }
            }
        }
    }

    private float factorCache = 0;
    private String valueOfScale(float scale, float factor) {
        float dividerCache = 1;
        if (factor >= 1) {
            return String.valueOf((int)(scale * factor));
        } else if (factor > 0) {
            if (factorCache != factor) {
                factorCache = factor;
                dividerCache = 1 / factor;
            }
            return String.valueOf(scale / dividerCache);
        }
        return "";
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
                    scrollBackToCurrentScale();
                }
                recycleVelocityTracker();
                parent.requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                scrollBackToCurrentScale();
                recycleVelocityTracker();
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    private void fling(int vX) {
        mScroller.fling(getScrollX(), 0 , vX, 0, mMinPosition, mMaxPosition, 0, 0);
        invalidate();
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
    }

    private void goToScale(float scale) {
        mCurrentScale = Math.round(scale);
        scrollTo(scaleToScrollX(mCurrentScale), 0);
    }

    private int scaleToScrollX(float scale) {
        return (int) ((scale - mParent.getMinScale()) / mMaxLength * mLength + mMinPosition);
    }

    private float scrollXToScale(int scrollX) {
        return ((float) (scrollX - mMinPosition) / mLength) * mMaxLength + mParent.getMinScale();
    }

    private void scrollBackToCurrentScale() {
        scrollBackToCurrentScale(Math.round(mCurrentScale));
    }

    private void scrollBackToCurrentScale(int scale) {
        float scrollX = scaleToScrollFloatX(scale);
        int dx = Math.round((scrollX - SCALE_TO_PX_FACTOR * getScrollX()) / SCALE_TO_PX_FACTOR);
        if (dx > minScrollPx) {
            mScroller.startScroll(getScrollX(), getScrollY(), dx, 0, 500);
            invalidate();
        } else {
            scrollBy(dx, 0);
        }

    }

    private float scaleToScrollFloatX(float scale) {
        return (((scale - mParent.getMinScale()) / mMaxLength * mLength * SCALE_TO_PX_FACTOR) + mMinPosition * SCALE_TO_PX_FACTOR);
    }

    private void refreshSize() {
        mLength = (mParent.getMaxScale() - mParent.getMinScale()) * mParent.getInterval();
        mHalfWidth = getWidth() / 2;
        mMinPosition = -mHalfWidth;
        mMaxPosition = mLength - mHalfWidth;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setCurrentScale(float currentScale) {
        mCurrentScale = currentScale;
        goToScale(mCurrentScale);
    }

    public float getCurrentScale() {
        return mCurrentScale;
    }

    public void setVideoPath(String path) {

    }
}
