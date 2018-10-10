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
    private static int MERGIN_PADDING = 20;
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

    //params
    private Bitmap thumbSlice;
    private Bitmap thumbSliceRight;
    private Bitmap thumbFrame;
    private int progressMinDiff = 25; //percentage
    private int progressHalfHeight = 0;
    private int thumbPadding = 0;
    private float maxValue = 100f;

    private int progressMinDiffPixels;
    private int thumbSliceLeftX, thumbSliceRightX, thumbMaxSliceRightx;
    private float thumbSliceLeftValue, thumbSliceRightValue;
    private Paint paintThumb = new Paint();
    private SelectThumb selectedThumb;
    private SelectThumb lastSelectedThumb = SelectThumb.SELECT_THUMB_NONE;
    private int thumbSliceHalfWidth;
    private OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener;
    private int resSweepLeft = R.drawable.ic_progress_left;
    private int resSweepRight = R.drawable.ic_progress_right;
    private int resFrame = R.drawable.progress_thumb;
    private int resBackground = R.color.colorMask;
    private int resPaddingColor = android.R.color.holo_red_dark;

    private boolean blocked;
    private boolean isInited;

    private boolean isTouch = false;
    private boolean isDefaultSeekTotal;
    private int prevX;
    private int downX;

    private int screenWidth;

    private int lastDrawLeft;
    private int lastDrawRight;

    private boolean needFrameProgress;
    private float frameProgress;

    private static final int PADDING_BOTTOM_TOP = 10;
    private static final int PADDING_LEFT_RIGHT = 5;

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
        resSweepLeft = a.getResourceId(R.styleable.RangeSeekBar_leftThumbDrawable, R.drawable.ic_progress_left);
        resSweepRight = a.getResourceId(R.styleable.RangeSeekBar_rightThumbDrawable, R.drawable.ic_progress_right);
        resFrame = a.getResourceId(R.styleable.RangeSeekBar_progressThumb, R.drawable.progress_thumb);
        resBackground = a.getResourceId(R.styleable.RangeSeekBar_maskColor, R.color.colorMask);
        resPaddingColor = a.getResourceId(R.styleable.RangeSeekBar_paddingColor, android.R.color.holo_red_dark);
        a.recycle();
    }

    private void initView(Context context)
    {
        thumbSlice = BitmapFactory.decodeResource(getResources(), resSweepLeft);
        thumbSliceRight = BitmapFactory.decodeResource(getResources(), resSweepRight);
        thumbFrame = BitmapFactory.decodeResource(getResources(), resFrame);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int itemWidth = screenWidth / 8;
        float ratio = (float) itemWidth / (float) thumbSlice.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        float frameRatio = (float) itemWidth / (float) thumbFrame.getHeight();
        Matrix frameMatrix = new Matrix();
        frameMatrix.postScale(frameRatio, frameRatio);
        thumbSlice = Bitmap.createBitmap(thumbSlice, 0, 0, thumbSlice.getWidth(), thumbSlice.getHeight(), matrix, false);
        thumbSliceRight = Bitmap.createBitmap(thumbSliceRight, 0, 0, thumbSliceRight.getWidth(), thumbSliceRight.getHeight(), matrix, false);
        thumbFrame = Bitmap.createBitmap(thumbFrame, 0, 0, thumbFrame.getWidth(), thumbFrame.getHeight(), frameMatrix, false);
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
        if (thumbSlice.getHeight() > getHeight())
        {
            getLayoutParams().height = thumbSlice.getHeight();
        }

        thumbSliceHalfWidth = thumbSlice.getWidth() / 2;
        progressMinDiffPixels = calculateCorrds(progressMinDiff) - 2 * thumbPadding;

        selectedThumb = SelectThumb.SELECT_THUMB_NONE;
        setLeftProgress(0);
        setRightProgress(100);
        setThumbMaxSliceRightx(screenWidth);
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        int drawLeft = thumbSliceLeftX;
        int drawRight = thumbSliceRightX;

        paintThumb.setColor(getResources().getColor(resPaddingColor));
        canvas.drawRect(drawLeft + thumbSlice.getWidth() - PADDING_LEFT_RIGHT, 0f, drawRight + PADDING_LEFT_RIGHT, PADDING_BOTTOM_TOP, paintThumb);
        canvas.drawRect(drawLeft + thumbSlice.getWidth() - PADDING_LEFT_RIGHT, thumbSlice.getHeight() - PADDING_BOTTOM_TOP,
                        drawRight + PADDING_LEFT_RIGHT, thumbSlice.getHeight(), paintThumb);
        paintThumb.setColor(getResources().getColor(resBackground));
        paintThumb.setAlpha((int) (255 * 0.9));
        canvas.drawRect(0, 0, drawLeft + PADDING_LEFT_RIGHT, getHeight(), paintThumb);
        canvas.drawRect(drawRight + thumbSliceRight.getWidth() - PADDING_LEFT_RIGHT, 0, getWidth(), getHeight(), paintThumb);
        canvas.drawBitmap(thumbSlice, drawLeft, 0, paintThumb);
        canvas.drawBitmap(thumbSliceRight, drawRight, 0, paintThumb);
        if (needFrameProgress)
        {
            float progress = frameProgress * (getWidth() - thumbSliceHalfWidth * 2) - thumbFrame.getWidth() / 2;
            if (progress > drawRight + thumbSliceHalfWidth * 2)
            {
                progress = drawRight + thumbSliceHalfWidth * 2;
            }
            canvas.drawBitmap(thumbFrame, progress, 0, paintThumb);
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
                    if (mx <= thumbSliceLeftX + thumbSliceHalfWidth * 2 + DRAG_OFFSET)
                    {
                        if (mx >= thumbSliceLeftX)
                        {
                            selectedThumb = SelectThumb.SELECT_THUMB_LEFT;
                        }
                        else
                        {
                            selectedThumb = SelectThumb.SELECT_THUMB_MORE_LEFT;
                        }
                    }
                    else if (mx >= thumbSliceRightX - thumbSliceHalfWidth * 2 - DRAG_OFFSET)
                    {
                        if (mx <= thumbSliceRightX)
                        {
                            selectedThumb = SelectThumb.SELECT_THUMB_RIGHT;
                        }
                        else
                        {
                            selectedThumb = SelectThumb.SELECT_THUMB_MORE_RIGHT;
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

                    if (selectedThumb == SelectThumb.SELECT_THUMB_LEFT)
                    {
                        thumbSliceLeftX = mx;
                    }
                    else if (selectedThumb == SelectThumb.SELECT_THUMB_RIGHT)
                    {
                        thumbSliceRightX = mx;
                    }
                    else if (selectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT)
                    {
                        int distance = mx - prevX;
                        thumbSliceRightX += distance;
                    }
                    else if (selectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT)
                    {
                        int distance = mx - prevX;
                        thumbSliceLeftX += distance;
                    }

                    if (adjustSliceXY(mx))
                    {
                        break;
                    }
                    prevX = mx;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    downX = mx;
                    adjustSliceXY(mx);
                    selectedThumb = SelectThumb.SELECT_THUMB_NONE;
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
        frameProgress = percent;
        invalidate();
    }

    public void showFrameProgress(boolean isShow)
    {
        needFrameProgress = isShow;
    }

    private boolean adjustSliceXY(int mx)
    {
        boolean isNoneArea = false;
        int thumbSliceDistance = thumbSliceRightX - thumbSliceLeftX;
        if (thumbSliceDistance <= progressMinDiffPixels && selectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT && mx <= downX || thumbSliceDistance <= progressMinDiffPixels && selectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT && mx >= downX)
        {
            isNoneArea = true;
        }

        if (thumbSliceDistance <= progressMinDiffPixels && selectedThumb == SelectThumb.SELECT_THUMB_RIGHT && mx <= downX || thumbSliceDistance <= progressMinDiffPixels && selectedThumb == SelectThumb.SELECT_THUMB_LEFT && mx >= downX)
        {
            isNoneArea = true;
        }

        if (isNoneArea)
        {
            if (selectedThumb == SelectThumb.SELECT_THUMB_RIGHT || selectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT)
            {
                thumbSliceRightX = thumbSliceLeftX + progressMinDiffPixels;
            }
            else if (selectedThumb == SelectThumb.SELECT_THUMB_LEFT || selectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT)
            {
                thumbSliceLeftX = thumbSliceRightX - progressMinDiffPixels;
            }
            return true;
        }

        if (mx > thumbMaxSliceRightx && (selectedThumb == SelectThumb.SELECT_THUMB_RIGHT || selectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT))
        {
            thumbSliceRightX = thumbMaxSliceRightx;
            return true;
        }

        if (thumbSliceRightX >= (getWidth() - thumbSliceHalfWidth * 2) - MERGIN_PADDING)
        {
            thumbSliceRightX = getWidth() - thumbSliceHalfWidth * 2;
        }

        if (thumbSliceLeftX < MERGIN_PADDING)
        {
            thumbSliceLeftX = 0;
        }

        return false;
    }

    private void notifySeekBarValueChanged()
    {
        if (thumbSliceLeftX < thumbPadding)
        {
            thumbSliceLeftX = thumbPadding;
        }

        if (thumbSliceRightX < thumbPadding)
        {
            thumbSliceRightX = thumbPadding;
        }

        if (thumbSliceLeftX > getWidth() - thumbPadding)
        {
            thumbSliceLeftX = getWidth() - thumbPadding;
        }

        if (thumbSliceRightX > getWidth() - thumbPadding)
        {
            thumbSliceRightX = getWidth() - thumbPadding;
        }

        invalidate();
        if (mOnRangeSeekBarChangeListener != null)
        {
            calculateThumbValue();

            if (isTouch)
            {
                if (selectedThumb == SelectThumb.SELECT_THUMB_LEFT || selectedThumb == SelectThumb.SELECT_THUMB_MORE_LEFT)
                {
                    mOnRangeSeekBarChangeListener.onRangeChange(0, thumbSliceLeftValue, thumbSliceRightValue);
                }
                else if (selectedThumb == SelectThumb.SELECT_THUMB_RIGHT || selectedThumb == SelectThumb.SELECT_THUMB_MORE_RIGHT)
                {
                    mOnRangeSeekBarChangeListener.onRangeChange(1, thumbSliceLeftValue, thumbSliceRightValue);
                }
                else
                {
                    mOnRangeSeekBarChangeListener.onRangeChange(2, thumbSliceLeftValue, thumbSliceRightValue);
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
        thumbSliceLeftValue = maxValue * thumbSliceLeftX / (getWidth() - thumbSliceHalfWidth * 2);
        thumbSliceRightValue = maxValue * thumbSliceRightX / (getWidth() - thumbSliceHalfWidth * 2);
    }


    private int calculateCorrds(int progress)
    {
        return (int) ((getWidth() - thumbSliceHalfWidth * 2) / maxValue * progress);
    }

    public void setLeftProgress(int progress)
    {
        if (progress <= thumbSliceRightValue - progressMinDiff)
        {
            thumbSliceLeftX = calculateCorrds(progress);
        }
        notifySeekBarValueChanged();
    }

    public void setRightProgress(int progress)
    {
        if (progress >= thumbSliceLeftValue + progressMinDiff)
        {
            thumbSliceRightX = calculateCorrds(progress);
            if (!isDefaultSeekTotal)
            {
                isDefaultSeekTotal = true;
            }
        }
        notifySeekBarValueChanged();
    }

    public float getLeftProgress()
    {
        return thumbSliceLeftValue;
    }

    public float getRightProgress()
    {
        return thumbSliceRightValue;
    }

    public void setProgress(int leftProgress, int rightProgress)
    {
        if (rightProgress - leftProgress >= progressMinDiff)
        {
            thumbSliceLeftX = calculateCorrds(leftProgress);
            thumbSliceRightX = calculateCorrds(rightProgress);
        }
        notifySeekBarValueChanged();
    }

    public void setSliceBlocked(boolean isBLock)
    {
        blocked = isBLock;
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
        this.thumbSlice = thumbSlice;
        init();
    }

    public void setThumbPadding(int thumbPadding)
    {
        this.thumbPadding = thumbPadding;
        invalidate();
    }

    public void setThumbMaxSliceRightx(int maxRightThumb)
    {
        this.thumbMaxSliceRightx = maxRightThumb;
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
