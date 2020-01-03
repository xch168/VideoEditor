package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.Map;

public class MediaTrackView extends View {
    private Context mContext;

    private EditorTrackView mParent;

    private int mMinPosition;
    private int mMaxPosition;
    private int mCurrentPosition;
    private int mLength;

    private int mMinProgress = 0;
    private int mMaxProgress = 1000;
    private int mCurrentProgress = 0;

    private int mHalfWidth;
    private int mItemSize;

    private int mItemCount;

    private Paint mThumbPaint;
    private Bitmap mDefaultThumb;

    private Map<Integer, Bitmap> mThumbMap;

    private float mLastX;

    public MediaTrackView(Context context, EditorTrackView parent) {
        super(context);
        mContext = context;
        mParent = parent;

        initView();
    }

    private void initView() {
        mItemSize = SizeUtil.dp2px(mContext, 38);

        mThumbPaint = new Paint(1);
        mThumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDefaultThumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_default);
        mDefaultThumb = Bitmap.createScaledBitmap(mDefaultThumb, mItemSize, mItemSize, true);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollTo(mMinPosition, 0);
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
        Log.i("asdf", "w:" + w + " old:" + oldw + " sw:" + mContext.getResources().getDisplayMetrics().widthPixels);
        refreshSize();
    }

    private void refreshSize() {
        mHalfWidth = getWidth() / 2;
        mMinPosition = -mHalfWidth;
        mMaxPosition = mItemSize * mItemCount - mHalfWidth;
        mLength = mMaxPosition - mMinPosition;
        Log.i("asdf", "xxxxx:" + mItemCount);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawThumbnail(canvas);
    }

    private void drawThumbnail(Canvas canvas) {
        Log.i("asdf", "scrollX:" + getScrollX() + " cw:" + canvas.getWidth() + " min:" + mMinPosition + " max:" + mMaxPosition);
        int start = mMinProgress + getScrollX();
        int end = mMinProgress + getScrollX() + getWidth();
        Log.i("asdf", "start:" + start + " end;" + end);
        for (int pos = start; pos <= end; pos++) {
            if (isADrawPosition(pos)) {
                Log.i("asdf", "draw:" + pos);
                int locationX = pos;
                int index = positionToThumbIndex(locationX);
                Bitmap drawThumb = mDefaultThumb;
                if (mThumbMap != null && mThumbMap.get(index) != null) {
                    drawThumb = mThumbMap.get(index);
                }
                canvas.drawBitmap(drawThumb, locationX, 0, mThumbPaint);
            }
        }
    }

    private boolean isADrawPosition(int pos) {
        return (pos - mMinPosition) % mItemSize == 0;
    }

    private int getDrawLocationX(int i) {
        int locationX = mMinPosition + getScrollX();

        return locationX;
    }

    private int positionToThumbIndex(int pos) {
        return pos / mItemSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX();
        ViewGroup parent = (ViewGroup) getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = currentX;
                parent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) moveX, 0);
                break;
            case MotionEvent.ACTION_UP:

                parent.requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:

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
    }

    @Override
    public void computeScroll() {

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.i("asdf", "scroll:" + oldl + " ==> " + l);
    }

    public void setThumbMap(Map<Integer, Bitmap> map) {
        mThumbMap = map;
    }

    public void setCurrentPosition(int pos) {

    }

    private int progressToPosition(int progress) {
        return progress / mMaxProgress * mLength;
    }

    public int getItemSize() {
        return mItemSize;
    }

    public void setItemCount(int count) {
        mItemCount = count;
    }

    public void setCurrentProgress(int progress) {
        mCurrentProgress = progress;
    }
}
