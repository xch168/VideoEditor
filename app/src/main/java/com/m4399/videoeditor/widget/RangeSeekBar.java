package com.m4399.videoeditor.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.m4399.videoeditor.R;

public class RangeSeekBar extends View
{
    private static String TAG = "RangeSeekBar";

    private static final int PADDING_LEFT_RIGHT = 5;
    private static final int PADDING_BOTTOM_TOP = 5;
    private static final int MARGIN_PADDING = 20;
    private static final int DRAG_OFFSET = 50;

    enum SelectThumb
    {
        /**
         * 没有选中滑块
         */
        SELECT_THUMB_NONE,

         /**
          * 选中左边滑块
          */
        SELECT_THUMB_LEFT,
        /**
         * 选中左边滑块的左侧
         */
        SELECT_THUMB_MORE_LEFT,
        /**
         * 选中右边滑块
         */
        SELECT_THUMB_RIGHT,

        /**
         * 选中右边滑块的右侧
         */
        SELECT_THUMB_MORE_RIGHT
    }

    /**
     * 左滑块
     */
    private Bitmap mLeftThumb;
    /**
     * 右滑块
     */
    private Bitmap mRightThumb;
    /**
     * 进度滑块
     */
    private Bitmap mProgressThumb;

    private Paint mPaint = new Paint();

    private SelectThumb mSelectedThumb;

    private int mLeftThumbRes = R.drawable.ic_progress_left;
    private int mRightThumbRes = R.drawable.ic_progress_right;
    private int mProgressThumbRes = R.drawable.progress_thumb;
    private int mMaskRes = R.color.colorMask;
    private int mStrokeRes = R.color.colorStroke;

    private int progressMinDiff = 25; //percentage
    private int progressHalfHeight = 0;
    private int thumbPadding = 0;
    private float maxValue = 100f;

    private int mLeftThumbX;
    private int mRightThumbX;
    private int mRightThumbMaxX;

    private int progressMinDiffPixels;
    private float mLeftThumbValue;
    private float mRightThumbValue;

    private int mThumbHalfWidth;

    private boolean blocked;
    private boolean isInited;

    private boolean isTouch = false;
    private boolean isDefaultSeekTotal;
    private int prevX;
    private int downX;

    private int mScreenWidth;

    private boolean needFrameProgress;
    private float mFrameProgress;

    private OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener;

    public RangeSeekBar(Context context)
    {
        super(context);
        initView(context);
    }

    public RangeSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initAttrs(attrs);
        initView(context);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        initView(context);
    }

    private void initAttrs(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
        mLeftThumbRes = a.getResourceId(R.styleable.RangeSeekBar_leftThumbDrawable, R.drawable.ic_progress_left);
        mRightThumbRes = a.getResourceId(R.styleable.RangeSeekBar_rightThumbDrawable, R.drawable.ic_progress_right);
        mProgressThumbRes = a.getResourceId(R.styleable.RangeSeekBar_progressThumb, R.drawable.progress_thumb);
        mMaskRes = a.getResourceId(R.styleable.RangeSeekBar_maskColor, R.color.colorMask);
        mStrokeRes = a.getResourceId(R.styleable.RangeSeekBar_paddingColor, R.color.colorStroke);
        a.recycle();
    }

    private void initView(Context context)
    {
        mLeftThumb = BitmapFactory.decodeResource(getResources(), mLeftThumbRes);
        mRightThumb = BitmapFactory.decodeResource(getResources(), mRightThumbRes);
        mProgressThumb = BitmapFactory.decodeResource(getResources(), mProgressThumbRes);

        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int itemWidth = mScreenWidth / 8;
        float ratio = (float) itemWidth / (float) mLeftThumb.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        float frameRatio = (float) itemWidth / (float) mProgressThumb.getHeight();
        Matrix frameMatrix = new Matrix();
        frameMatrix.postScale(frameRatio, frameRatio);
        mLeftThumb = Bitmap.createBitmap(mLeftThumb, 0, 0, mLeftThumb.getWidth(), mLeftThumb.getHeight(), matrix, false);
        mRightThumb = Bitmap.createBitmap(mRightThumb, 0, 0, mRightThumb.getWidth(), mRightThumb.getHeight(), matrix, false);
        mProgressThumb = Bitmap.createBitmap(mProgressThumb, 0, 0, mProgressThumb.getWidth(), mProgressThumb.getHeight(), frameMatrix, false);
        invalidate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isInited)
        {
            isInited = true;
            init();
        }
    }

    private void init()
    {
        if (mLeftThumb.getHeight() > getHeight())
        {
            getLayoutParams().height = mLeftThumb.getHeight();
        }

        mThumbHalfWidth = mLeftThumb.getWidth() / 2;
        progressMinDiffPixels = calculateCorrds(progressMinDiff) - 2 * thumbPadding;

        mSelectedThumb = SelectThumb.SELECT_THUMB_NONE;
        setLeftProgress(0);
        setRightProgress(100);
        setRightThumbMaxX(mScreenWidth - mRightThumb.getWidth());
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // 绘制边框
        mPaint.setColor(getResources().getColor(mStrokeRes));
        canvas.drawRect(mLeftThumbX + mLeftThumb.getWidth() - PADDING_LEFT_RIGHT, 0f, mRightThumbX + PADDING_LEFT_RIGHT, PADDING_BOTTOM_TOP, mPaint);
        canvas.drawRect(mLeftThumbX + mLeftThumb.getWidth() - PADDING_LEFT_RIGHT, mLeftThumb.getHeight() - PADDING_BOTTOM_TOP,
                        mRightThumbX + PADDING_LEFT_RIGHT, mLeftThumb.getHeight(), mPaint);

        // 绘制超出范围的蒙层
        mPaint.setColor(getResources().getColor(mMaskRes));
        canvas.drawRect(0, 0, mLeftThumbX + PADDING_LEFT_RIGHT, getHeight(), mPaint);
        canvas.drawRect(mRightThumbX + mRightThumb.getWidth() - PADDING_LEFT_RIGHT, 0, getWidth(), getHeight(), mPaint);

        // 绘制左右thumb
        mPaint.setAlpha(255);
        canvas.drawBitmap(mLeftThumb, mLeftThumbX, 0, mPaint);
        canvas.drawBitmap(mRightThumb, mRightThumbX, 0, mPaint);

        // 绘制进度
        if (needFrameProgress)
        {
            float progress = (mLeftThumbX + mLeftThumb.getWidth()) + mFrameProgress * (mRightThumbX - mLeftThumbX - mLeftThumb.getWidth());
            if (progress > mRightThumbX - mProgressThumb.getWidth())
            {
                progress = mRightThumbX - mProgressThumb.getWidth();
            }
            canvas.drawBitmap(mProgressThumb, progress, 0, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!blocked)
        {
            int mx = (int) event.getX();

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    if (mx <= mLeftThumbX + mThumbHalfWidth * 2 + DRAG_OFFSET)
                    {
                        if (mx >= mLeftThumbX)
                        {
                            mSelectedThumb = SelectThumb.SELECT_THUMB_LEFT;
                        }
                        else
                        {
                            mSelectedThumb = SelectThumb.SELECT_THUMB_MORE_LEFT;
                        }
                    }
                    else if (mx >= mRightThumbX - mThumbHalfWidth * 2 - DRAG_OFFSET)
                    {
                        if (mx <= mRightThumbX)
                        {
                            mSelectedThumb = SelectThumb.SELECT_THUMB_RIGHT;
                        }
                        else
                        {
                            mSelectedThumb = SelectThumb.SELECT_THUMB_MORE_RIGHT;
                        }

                    }
                    downX = mx;
                    prevX = mx;
                    if (mOnRangeSeekBarChangeListener != null)
                    {
                        mOnRangeSeekBarChangeListener.onStartTrackingTouch();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (mSelectedThumb == SelectThumb.SELECT_THUMB_LEFT)
                    {
                        mLeftThumbX = mx;
                    }
                    else if (mSelectedThumb == SelectThumb.SELECT_THUMB_RIGHT)
                    {
                        mRightThumbX = mx;
                    }
                    else if (mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT)
                    {
                        int distance = mx - prevX;
                        mRightThumbX += distance;
                    }
                    else if (mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT)
                    {
                        int distance = mx - prevX;
                        mLeftThumbX += distance;
                    }

                    if (adjustThumbXY(mx))
                    {
                        break;
                    }
                    prevX = mx;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    downX = mx;
                    adjustThumbXY(mx);
                    mSelectedThumb = SelectThumb.SELECT_THUMB_NONE;
                    if (mOnRangeSeekBarChangeListener != null)
                    {
                        mOnRangeSeekBarChangeListener.onStopTrackingTouch();
                    }
                    break;
                default:
                    break;
            }

            if (mx != downX)
            {
                isTouch = true;
                notifySeekBarValueChanged();
            }
        }
        return true;
    }

    public void setFrameProgress(float percent)
    {
        mFrameProgress = percent;
        invalidate();
    }

    public void showFrameProgress(boolean isShow)
    {
        needFrameProgress = isShow;
    }

    private boolean adjustThumbXY(int mx)
    {
        boolean isNoneArea = false;
        int thumbSliceDistance = mRightThumbX - mLeftThumbX;
        if (thumbSliceDistance <= progressMinDiffPixels && mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT && mx <= downX || thumbSliceDistance <= progressMinDiffPixels && mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT && mx >= downX)
        {
            isNoneArea = true;
        }

        if (thumbSliceDistance <= progressMinDiffPixels && mSelectedThumb == SelectThumb.SELECT_THUMB_RIGHT && mx <= downX || thumbSliceDistance <= progressMinDiffPixels && mSelectedThumb == SelectThumb.SELECT_THUMB_LEFT && mx >= downX)
        {
            isNoneArea = true;
        }

        if (isNoneArea)
        {
            if (mSelectedThumb == SelectThumb.SELECT_THUMB_RIGHT || mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT)
            {
                mRightThumbX = mLeftThumbX + progressMinDiffPixels;
            }
            else if (mSelectedThumb == SelectThumb.SELECT_THUMB_LEFT || mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT)
            {
                mLeftThumbX = mRightThumbX - progressMinDiffPixels;
            }
            return true;
        }

        if (mx > mRightThumbMaxX && (mSelectedThumb == SelectThumb.SELECT_THUMB_RIGHT || mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT))
        {
            mRightThumbX = mRightThumbMaxX;
            return true;
        }

        if (mRightThumbX >= (getWidth() - mThumbHalfWidth * 2) - MARGIN_PADDING)
        {
            mRightThumbX = getWidth() - mThumbHalfWidth * 2;
        }

        if (mLeftThumbX < MARGIN_PADDING)
        {
            mLeftThumbX = 0;
        }

        return false;
    }

    private void notifySeekBarValueChanged()
    {
        if (mLeftThumbX < thumbPadding)
        {
            mLeftThumbX = thumbPadding;
        }

        if (mRightThumbX < thumbPadding)
        {
            mRightThumbX = thumbPadding;
        }

        if (mLeftThumbX > getWidth() - thumbPadding)
        {
            mLeftThumbX = getWidth() - thumbPadding;
        }

        if (mRightThumbX > getWidth() - thumbPadding)
        {
            mRightThumbX = getWidth() - thumbPadding;
        }

        invalidate();
        if (mOnRangeSeekBarChangeListener != null)
        {
            calculateThumbValue();

            if (isTouch)
            {
                if (mSelectedThumb == SelectThumb.SELECT_THUMB_LEFT || mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT)
                {
                    mOnRangeSeekBarChangeListener.onRangeChange(0, mLeftThumbValue, mRightThumbValue);
                }
                else if (mSelectedThumb == SelectThumb.SELECT_THUMB_RIGHT || mSelectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT)
                {
                    mOnRangeSeekBarChangeListener.onRangeChange(1, mLeftThumbValue, mRightThumbValue);
                }
                else
                {
                    mOnRangeSeekBarChangeListener.onRangeChange(2, mLeftThumbValue, mRightThumbValue);
                }
            }
        }

        isTouch = false;
    }

    private void calculateThumbValue()
    {
        if (0 == getWidth())
        {
            return;
        }
        mLeftThumbValue = maxValue * mLeftThumbX / (getWidth() - mThumbHalfWidth * 2);
        mRightThumbValue = maxValue * mRightThumbX / (getWidth() - mThumbHalfWidth * 2);
    }


    private int calculateCorrds(int progress)
    {
        return (int) ((getWidth() - mThumbHalfWidth * 2) / maxValue * progress);
    }

    public void setLeftProgress(int progress)
    {
        if (progress <= mRightThumbValue - progressMinDiff)
        {
            mLeftThumbX = calculateCorrds(progress);
        }
        notifySeekBarValueChanged();
    }

    public void setRightProgress(int progress)
    {
        if (progress >= mLeftThumbValue + progressMinDiff)
        {
            mRightThumbX = calculateCorrds(progress);
            if (!isDefaultSeekTotal)
            {
                isDefaultSeekTotal = true;
            }
        }
        notifySeekBarValueChanged();
    }

    public float getLeftProgress()
    {
        return mLeftThumbValue;
    }

    public float getRightProgress()
    {
        return mRightThumbValue;
    }

    public void setProgress(int leftProgress, int rightProgress)
    {
        if (rightProgress - leftProgress >= progressMinDiff)
        {
            mLeftThumbX = calculateCorrds(leftProgress);
            mRightThumbX = calculateCorrds(rightProgress);
        }
        notifySeekBarValueChanged();
    }

    public void setThumbBlocked(boolean isBlock)
    {
        blocked = isBlock;
        invalidate();
    }

    public void setMaxValue(int maxValue)
    {
        this.maxValue = maxValue;
    }

    public void setProgressMinDiff(int progressMinDiff)
    {
        this.progressMinDiff = progressMinDiff;
        progressMinDiffPixels = calculateCorrds(progressMinDiff);
    }

    public void setProgressHeight(int progressHeight)
    {
        this.progressHalfHeight = progressHalfHeight / 2;
        invalidate();
    }

    public void setThumbSlice(Bitmap thumbSlice)
    {
        this.mLeftThumb = thumbSlice;
        init();
    }

    public void setThumbPadding(int thumbPadding)
    {
        this.thumbPadding = thumbPadding;
        invalidate();
    }

    public void setRightThumbMaxX(int maxRightThumb)
    {
        this.mRightThumbMaxX = maxRightThumb;
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener)
    {
        this.mOnRangeSeekBarChangeListener = listener;
    }

    public interface OnRangeSeekBarChangeListener
    {
        void onRangeChange(int witchSide, float leftThumb, float rightThumb);

        void onStartTrackingTouch();

        void onStopTrackingTouch();
    }
}
