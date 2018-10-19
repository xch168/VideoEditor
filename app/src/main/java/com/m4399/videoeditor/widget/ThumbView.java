package com.m4399.videoeditor.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.m4399.videoeditor.R;
import com.m4399.videoeditor.util.SizeUtil;

public class ThumbView extends ImageView
{
    private Paint mOutsidePaint;
    private Paint mInsidePaint;
    private Paint mRoundPaint = new Paint();
    private Path mPath = new Path();
    private Xfermode mXfermode;
    private int mWidth;
    private int mHeight;
    private int mRadius;
    private int mOutsidePadding;
    private int mInsidePadding;
    private RectF mRectF;
    private RectF mInsideRectF;
    private RectF mOutsideRectF;
    private float[] mRadii;

    public ThumbView(Context context)
    {
        this(context, null);
    }

    public ThumbView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView()
    {
        mRadii = new float[8];
        mRectF = new RectF();
        mInsideRectF = new RectF();
        mOutsideRectF = new RectF();
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        mRadius = SizeUtil.dp2px(getContext(), 4.0F);
        mOutsidePadding = SizeUtil.dp2px(getContext(), 1.0F);
        mInsidePadding = SizeUtil.dp2px(getContext(), 4.0F);

        mOutsidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutsidePaint.setColor(getResources().getColor(R.color.colorStroke));
        mOutsidePaint.setStyle(Paint.Style.STROKE);
        mOutsidePaint.setStrokeWidth(SizeUtil.dp2px(getContext(), 1.0F));

        mInsidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInsidePaint.setColor(getResources().getColor(android.R.color.white));
        mInsidePaint.setStyle(Paint.Style.STROKE);
        mInsidePaint.setStrokeWidth(SizeUtil.dp2px(getContext(), 4.0F));

        setScaleType(ImageView.ScaleType.CENTER_CROP);
        int index = 0;
        while (index < mRadii.length)
        {
            mRadii[index] = (mRadius / 2 - mOutsidePadding / 2.0F);
            index += 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mHeight > 0 && mWidth > 0)
        {
            canvas.saveLayer(mRectF, null, Canvas.ALL_SAVE_FLAG);
            super.onDraw(canvas);
            mPath.reset();
            mPath.addRoundRect(mRectF, mRadii, Path.Direction.CCW);
            mRoundPaint.setAntiAlias(true);
            mRoundPaint.setStyle(Paint.Style.FILL);
            mRoundPaint.setXfermode(mXfermode);
            canvas.drawPath(mPath, mRoundPaint);
            mRoundPaint.setXfermode(null);
            canvas.restore();
            mInsideRectF.set(0.0F, 0.0F, mWidth, mHeight);
            mOutsideRectF.set(mOutsidePadding / 2.0F, mOutsidePadding / 2.0F, mWidth - mOutsidePadding / 2.0F, mHeight - mOutsidePadding / 2.0F);
            canvas.drawRoundRect(mInsideRectF, mRadius, mRadius, mInsidePaint);
            canvas.drawRoundRect(mOutsideRectF, mRadius, mRadius, mOutsidePaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        mRectF.set(mInsidePadding / 2.0F, mInsidePadding / 2.0F, mWidth - mInsidePadding / 2.0F, mHeight - mInsidePadding / 2.0F);
    }

    public void setThumb(Bitmap bitmap)
    {
        setImageBitmap(bitmap);
        invalidate();
    }

    public void setBorderColor(@ColorInt int color)
    {

    }

    public void setBorderWidth(float width)
    {

    }

    public void setCornerRadius(float radius)
    {

    }

    public boolean isInTarget(int x, int y)
    {
        Rect rect = new Rect();
        getHitRect(rect);
        return rect.contains(x, y);
    }
}
