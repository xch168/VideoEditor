package com.m4399.videoeditor.widget;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.m4399.videoeditor.R;

public class RangeSeekBar extends View
{
    private Paint paint = new Paint();

    private int lineTop, lineBottom, lineLeft, lineRight;
    private int lineWidth;
    private RectF line = new RectF();

    private int mThumbResId;
    private int mInsideRangeLineColor;
    private int mOutsideRangeLineColor;

    private Thumb mLeftThumb = new Thumb();
    private Thumb mRightThumb = new Thumb();

    private Thumb mCurrentTouchThumb;

    private Drawable mProgressCursor;

    private OnRangeChangedListener mOnRangeChangedListener;

    private float maxValue, minValue;

    public RangeSeekBar(Context context)
    {
        super(context);
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        initAttrs(attrs);

        initView();
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initAttrs(attrs);

        initView();
    }

    private void initAttrs(AttributeSet attrs)
    {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);

        mThumbResId = typedArray.getResourceId(R.styleable.RangeSeekBar_rangeThumb, 0);
        mProgressCursor = typedArray.getDrawable(R.styleable.RangeSeekBar_progressThumb);
        mInsideRangeLineColor = typedArray.getColor(R.styleable.RangeSeekBar_insideRangeLineColor, 0xFF4BD962);
        mOutsideRangeLineColor = typedArray.getColor(R.styleable.RangeSeekBar_outsideRangeLineColor, 0xFFD7D7D7);

        typedArray.recycle();
    }

    private void initView()
    {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSize * 1.8f > widthSize)
        {
            setMeasuredDimension(widthSize, (int) (widthSize / 1.8f));
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        lineLeft = 0;
        lineRight = w;
        lineTop = 15;
        lineBottom = h - 15;
        lineWidth = lineRight - lineLeft;
        line.set(lineLeft, lineTop, lineRight, lineBottom);

        mLeftThumb.onSizeChanged(h, lineWidth, mThumbResId, getContext());
        mRightThumb.onSizeChanged(h, lineWidth, mThumbResId, getContext());

        mRightThumb.left += mLeftThumb.widthSize;
        mRightThumb.right += mLeftThumb.widthSize;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mOutsideRangeLineColor);

        // 绘制左边超出范围蒙层
        canvas.drawRect(0, lineTop,
                        mLeftThumb.left + mLeftThumb.widthSize / 2 + mLeftThumb.lineWidth * mLeftThumb.currPercent,
                        lineBottom, paint);

        // 绘制右边超出范围蒙层
        canvas.drawRect(mRightThumb.left + mRightThumb.widthSize / 2 + mRightThumb.lineWidth * mRightThumb.currPercent, lineTop,
                        lineRight,
                        lineBottom, paint);

        // 绘制中间选中范围蒙层
        paint.setColor(mInsideRangeLineColor);
        canvas.drawRect(mLeftThumb.left + mLeftThumb.widthSize / 2 + mLeftThumb.lineWidth * mLeftThumb.currPercent, lineTop,
                        mRightThumb.left + mRightThumb.widthSize / 2 + mRightThumb.lineWidth * mRightThumb.currPercent, lineBottom, paint);

        // 绘制左、右边界选择thumb
        mLeftThumb.draw(canvas);
        mRightThumb.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                boolean touchResult = false;
                if (mRightThumb.currPercent >= 1 && mLeftThumb.collide(event))
                {
                    mCurrentTouchThumb = mLeftThumb;
                    touchResult = true;
                }
                else if (mRightThumb.collide(event))
                {
                    mCurrentTouchThumb = mRightThumb;
                    touchResult = true;
                }
                else if (mLeftThumb.collide(event))
                {
                    mCurrentTouchThumb = mLeftThumb;
                    touchResult = true;
                }
                return touchResult;
            case MotionEvent.ACTION_MOVE:
                float percent;
                float x = event.getX();

                mCurrentTouchThumb.material = mCurrentTouchThumb.material >= 1 ? 1 : mCurrentTouchThumb.material + 0.1f;

                if (mCurrentTouchThumb == mLeftThumb)
                {
                    if (x < lineLeft)
                    {
                        percent = 0;
                    }
                    else
                    {
                        percent = (x - lineLeft) * 1f / (lineWidth - mRightThumb.widthSize);
                    }

                    if (percent > mRightThumb.currPercent)
                    {
                        percent = mRightThumb.currPercent;
                    }
                    mLeftThumb.slide(percent);
                }
                else if (mCurrentTouchThumb == mRightThumb)
                {
                    if (x > lineRight)
                    {
                        percent = 1;
                    }
                    else
                    {
                        percent = (x - lineLeft - mLeftThumb.widthSize) * 1f / (lineWidth - mLeftThumb.widthSize);
                    }
                    if (percent < mLeftThumb.currPercent)
                    {
                        percent = mLeftThumb.currPercent;
                    }
                    mRightThumb.slide(percent);
                }

                if (mOnRangeChangedListener != null)
                {
                    float[] result = getCurrentRange();
                    mOnRangeChangedListener.onRangeChanged(this, result[0], result[1]);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mCurrentTouchThumb.materialRestore();

                if (mOnRangeChangedListener != null)
                {
                    float[] result = getCurrentRange();
                    mOnRangeChangedListener.onRangeChanged(this, result[0], result[1]);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public float[] getCurrentRange()
    {
        float range = maxValue - minValue;
        return new float[]{minValue + range * mLeftThumb.currPercent,
                           minValue + range * mRightThumb.currPercent};
    }

    public void setMinValue(float minValue)
    {
        this.minValue = minValue;
    }

    public void setMaxValue(float maxValue)
    {
        this.maxValue = maxValue;
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener)
    {
        mOnRangeChangedListener = listener;
    }

    public interface OnRangeChangedListener
    {
        void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue);
    }

    private class Thumb
    {
        int lineWidth;
        int widthSize, heightSize;
        float currPercent;
        int left, right, top, bottom;
        Bitmap bmp;

        float material = 0;
        ValueAnimator anim;

        void onSizeChanged(int hSize, int parentLineWidth, int bmpResId, Context context)
        {
            heightSize = hSize;
            widthSize = (int) (hSize * 0.2);
            left = hSize / 2 - widthSize / 2;
            right = hSize / 2 + widthSize / 2;
            top = hSize / 2 - heightSize / 2;
            bottom = hSize / 2 + heightSize / 2;

            lineWidth = parentLineWidth - widthSize;

            if (bmpResId > 0)
            {
                bmp = BitmapFactory.decodeResource(context.getResources(), bmpResId);
            }
        }

        boolean collide(MotionEvent event)
        {
            float x = event.getX();
            float y = event.getY();
            int offset = (int) (lineWidth * currPercent);
            return x > left + offset && x < right + offset && y > top && y < bottom;
        }

        void slide(float percent)
        {
            if (percent < 0) percent = 0;
            else if (percent > 1) percent = 1;
            currPercent = percent;
        }

        void draw(Canvas canvas)
        {
            int offset = (int) (lineWidth * currPercent);
            canvas.save();
            canvas.translate(offset, 0);
            if (bmp != null)
            {
                canvas.drawBitmap(bmp, left, top, null);
            }
            canvas.restore();
        }

        private void materialRestore()
        {
            if (anim != null) anim.cancel();
            anim = ValueAnimator.ofFloat(material, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    material = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            anim.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    material = 0;
                    invalidate();
                }
            });
            anim.start();
        }
    }
}
