package com.m4399.videoeditor.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ThumbView extends ImageView
{
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
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
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
