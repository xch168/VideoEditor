package com.m4399.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class ImageSeekBar extends ViewGroup
{
    private ThumbView mThumbView;

    private boolean mIsDragging;
    private int mTouchSlop;
    private int mOriginalX, mLastX;

    private int mMax = 1000;
    private int mProgress;
    private float mPercent;

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public ImageSeekBar(Context context)
    {
        this(context, null);
    }

    public ImageSeekBar(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ImageSeekBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mThumbView = new ThumbView(context);

        addView(mThumbView);

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mThumbView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final int thumbWidth = mThumbView.getMeasuredWidth();
        final int thumbHeight = mThumbView.getMeasuredHeight();
        mThumbView.layout(0, 0, thumbWidth, thumbHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isEnabled()) {
            return false;
        }

        boolean handle = false;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();

                mLastX = mOriginalX = x;
                mIsDragging = false;

                if (!mThumbView.isPressed() && mThumbView.isInTarget(x, y))
                {
                    mThumbView.setPressed(true);
                    handle = true;
                    if (mOnSeekBarChangeListener != null)
                    {
                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                mOriginalX = mLastX = 0;
                getParent().requestDisallowInterceptTouchEvent(false);
                if (mThumbView.isPressed())
                {
                    mThumbView.setPressed(false);
                    invalidate();
                    handle = true;
                    if (mOnSeekBarChangeListener != null)
                    {
                        mOnSeekBarChangeListener.onStopTrackingTouch(this);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX();

                if (!mIsDragging && Math.abs(x - mOriginalX) > mTouchSlop)
                {
                    mIsDragging = true;
                }
                if (mIsDragging)
                {
                    int moveX = x - mLastX;
                    if (mThumbView.isPressed())
                    {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        moveThumbByPixel(moveX);
                        handle = true;
                        invalidate();
                    }
                }

                mLastX = x;
                break;
        }

        return handle;
    }

    private void moveThumbByPixel(int pixel)
    {
        float x = mThumbView.getX() + pixel;

        Log.i("asdf", "getX:" + getX() + " width:" + getWidth() + "  x:" + x);

        if (x >= getX() && x <= getX() + getWidth() - mThumbView.getWidth())
        {
            mThumbView.setX(x);
            if (mOnSeekBarChangeListener != null)
            {
                mPercent = (mThumbView.getX() - getX()) / (getWidth() - mThumbView.getWidth());
                mProgress = (int) (mPercent * mMax);
                mOnSeekBarChangeListener.onProgressChanged(this, mProgress, mPercent);
            }
        }
    }

    public void setMax(int max)
    {
        mMax = max;
    }

    public void setThumbBitmap(Bitmap bitmap)
    {
        mThumbView.setThumb(bitmap);
    }

    public void setOnSeekBarChangeListener(ImageSeekBar.OnSeekBarChangeListener listener)
    {
        mOnSeekBarChangeListener = listener;
    }

    public interface OnSeekBarChangeListener
    {
        void onProgressChanged(ImageSeekBar seekBar, int progress, float percent);

        void onStartTrackingTouch(ImageSeekBar seekBar);

        void onStopTrackingTouch(ImageSeekBar seekBar);
    }
}
