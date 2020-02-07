package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import com.github.xch168.videoeditor.entity.VideoPartInfo;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.List;

public class EditorMediaTrackView extends View {
    private static final int THUMB_TIME_INTERVAL = 5000;

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
    private int mInitMaxScale;
    private int mLength;

    private int mThumbSize;
    private int mDrawOffset;

    private Paint mTextPaint;
    private Paint mThumbPaint;
    private Bitmap mDefaultThumb;

    private SparseArray<Bitmap> mThumbList;
    private FrameExtractor mFrameExtractor;

    private float mLastX;

    private boolean mIsTrackingByUser = false;
    private OnTrackViewChangeListener mOnTrackViewChangeListener;
    private OnScrollChangeListener mOnScrollChangeListener;

    private List<VideoPartInfo> mVideoPartInfoList;

    public EditorMediaTrackView(Context context) {
        super(context);
        mContext = context;
        mThumbList = new SparseArray<>();

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
        refreshSize();
    }

    private void refreshSize() {
        int halfWidth = getWidth() / 2;
        mLength = mMaxScale;
        mMinPosition = -halfWidth;
        mMaxPosition = mLength - halfWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawThumbnail(canvas);
    }

    private void drawThumbnail(Canvas canvas) {
        if (mVideoPartInfoList == null) return;
        int start = (getScrollX() - mDrawOffset) + mMinScale;
        int end = (getScrollX() + mDrawOffset + getWidth()) + mMinScale;
        start = start >= mMinScale ? start : 0;
        for (int i = 0; i < mVideoPartInfoList.size(); i++) {
            VideoPartInfo partInfo = mVideoPartInfoList.get(i);
            if (start > partInfo.getEndScale() || end < partInfo.getStartScale()) {
                continue;
            }
            int clipLeft = Math.max(partInfo.getStartScale(), start);
            int clipRight = Math.min(partInfo.getEndScale(), end);
            canvas.save();
            canvas.clipRect(clipLeft, 0, clipRight, mThumbSize);
            int startScale = clipLeft - partInfo.getDrawOffset();
            for (int scale = startScale; scale <= clipRight; scale++) {
                if (isADrawScale(scale)) {
                    int locationX = scale - mMinScale;
                    int index = getThumbIndex(partInfo, scale);
                    Bitmap drawThumb = mDefaultThumb;
                    if (inThumbListRange(index) && mThumbList.get(index) != null) {
                        drawThumb = mThumbList.get(index);
                    }
                    canvas.drawBitmap(drawThumb, locationX, 0, mThumbPaint);
                    canvas.drawText("" + (index + 1), locationX + 28, 38, mTextPaint);
                }
            }
            canvas.restore();
        }
    }

    private boolean isADrawScale(int scale) {
        return scale >= mMinScale && scale <= mMaxScale && scale % mThumbSize == 0 && scale != mMaxScale;
    }

    private int getThumbIndex(VideoPartInfo partInfo, int scale) {
        int offset = partInfo.getInitOffset() - partInfo.getDrawOffset();
        int dScale = scale - (partInfo.getStartScale() - partInfo.getDrawOffset());
        scale = offset + dScale;
        return scale / mThumbSize;
    }

    private boolean inThumbListRange(int index) {
        return index >= 0 && index < mThumbList.size();
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

    public void setMaxScale(int maxScale) {
        mMaxScale = maxScale;
        refreshSize();
        invalidate();
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
        mFrameExtractor.getFrameByInterval(THUMB_TIME_INTERVAL, new FrameExtractor.Callback() {
            @Override
            public void onFrameExtracted(Bitmap bitmap, long timestamp) {
                int index = (int) (timestamp / THUMB_TIME_INTERVAL);
                mThumbList.put(index, bitmap);
                invalidate();
            }
        });
        mInitMaxScale = calcInitMaxScale(mFrameExtractor.getVideoDuration());
        setMaxScale(mInitMaxScale);
    }

    private int calcInitMaxScale(long duration) {
        return (int) (duration / THUMB_TIME_INTERVAL * mThumbSize);
    }

    public int getInitMaxScale() {
        return mInitMaxScale;
    }

    public int getThumbSize() {
        return mThumbSize;
    }

    public void setOnTrackViewChangeListener(OnTrackViewChangeListener listener) {
        mOnTrackViewChangeListener = listener;
    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        mOnScrollChangeListener = listener;
    }

    public void setVideoPartInfoList(List<VideoPartInfo> videoPartInfoList) {
        mVideoPartInfoList = videoPartInfoList;
    }

    public interface OnTrackViewChangeListener {
        void onStartTrackingTouch();
        void onScaleChanged(int scale);
    }

    public interface OnScrollChangeListener {
        void onScrollChange(int scrollX);
    }

}
