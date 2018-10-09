package com.m4399.videoeditor.widget;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private int lineCorners;
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

    private float cellsPercent;

    private float maxValue, minValue;
    private float reservePercent;

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

        int seekBarRadius = h / 2;

        lineLeft = 0;
        lineRight = w;
        lineTop = 10;
        lineBottom = h - 10;
        lineWidth = lineRight - lineLeft;
        line.set(lineLeft, lineTop, lineRight, lineBottom);

        mLeftThumb.onSizeChanged(seekBarRadius, seekBarRadius, h, lineWidth, mThumbResId, getContext());
        mRightThumb.onSizeChanged(seekBarRadius, seekBarRadius, h, lineWidth, mThumbResId, getContext());

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

        // 绘制左边边界选择控件
        mLeftThumb.draw(canvas);
        // 绘制右边边界选择控件
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

                    if (percent > mRightThumb.currPercent - reservePercent)
                    {
                        percent = mRightThumb.currPercent - reservePercent;
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
                    if (percent < mLeftThumb.currPercent + reservePercent)
                    {
                        percent = mLeftThumb.currPercent + reservePercent;
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
        RadialGradient shadowGradient;
        Paint defaultPaint;
        int lineWidth;
        int widthSize, heightSize;
        float currPercent;
        int left, right, top, bottom;
        Bitmap bmp;

        float material = 0;
        ValueAnimator anim;
        final TypeEvaluator<Integer> te = new TypeEvaluator<Integer>()
        {
            @Override
            public Integer evaluate(float fraction, Integer startValue, Integer endValue)
            {
                int alpha = (int) (Color.alpha(startValue) + fraction * (Color.alpha(endValue) - Color.alpha(startValue)));
                int red = (int) (Color.red(startValue) + fraction * (Color.red(endValue) - Color.red(startValue)));
                int green = (int) (Color.green(startValue) + fraction * (Color.green(endValue) - Color.green(startValue)));
                int blue = (int) (Color.blue(startValue) + fraction * (Color.blue(endValue) - Color.blue(startValue)));
                return Color.argb(alpha, red, green, blue);
            }
        };

        void onSizeChanged(int centerX, int centerY, int hSize, int parentLineWidth, int bmpResId, Context context)
        {
            heightSize = hSize;
            widthSize = (int) (heightSize * 0.8f);
            left = centerX - widthSize / 2;
            right = centerX + widthSize / 2;
            top = centerY - heightSize / 2;
            bottom = centerY + heightSize / 2;

            lineWidth = parentLineWidth - widthSize;

            if (bmpResId > 0)
            {
                Bitmap original = BitmapFactory.decodeResource(context.getResources(), bmpResId);
                Matrix matrix = new Matrix();
                float scaleWidth = ((float) widthSize) / original.getWidth();
                float scaleHeight = ((float) heightSize) / original.getHeight();
                matrix.postScale(scaleWidth, scaleHeight);
                bmp = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
            }
            else
            {
                defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                int radius = (int) (widthSize * 0.5f);
                int barShadowRadius = (int) (radius * 0.95f);
                int mShadowCenterX = widthSize / 2;
                int mShadowCenterY = heightSize / 2;
                shadowGradient = new RadialGradient(mShadowCenterX, mShadowCenterY, barShadowRadius, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
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
            else
            {
                canvas.translate(left, 0);
                drawDefault(canvas);
            }
            canvas.restore();
        }

        private void drawDefault(Canvas canvas)
        {
            int centerX = widthSize / 2;
            int centerY = heightSize / 2;
            int radius = (int) (widthSize * 0.5f);
            // draw shadow
            defaultPaint.setStyle(Paint.Style.FILL);
            canvas.save();
            canvas.translate(0, radius * 0.25f);
            canvas.scale(1 + (0.1f * material), 1 + (0.1f * material), centerX, centerY);
            defaultPaint.setShader(shadowGradient);
            canvas.drawCircle(centerX, centerY, radius, defaultPaint);
            defaultPaint.setShader(null);
            canvas.restore();
            // draw body
            defaultPaint.setStyle(Paint.Style.FILL);
            defaultPaint.setColor(te.evaluate(material, 0xFFFFFFFF, 0xFFE7E7E7));
            canvas.drawCircle(centerX, centerY, radius, defaultPaint);
            // draw border
            defaultPaint.setStyle(Paint.Style.STROKE);
            defaultPaint.setColor(0xFFD7D7D7);
            canvas.drawCircle(centerX, centerY, radius, defaultPaint);
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
