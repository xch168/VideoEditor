package com.m4399.videoeditor.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.m4399.videoeditor.R;

public class RangeSeekBar extends View
{
    private Drawable mLeftCursor;
    private Drawable mRightCursor;

    private Drawable mProgressCursor;

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

        mLeftCursor = typedArray.getDrawable(R.styleable.RangeSeekBar_rangeThumb);
        mRightCursor = typedArray.getDrawable(R.styleable.RangeSeekBar_rangeThumb);
        mProgressCursor = typedArray.getDrawable(R.styleable.RangeSeekBar_progressThumb);

        typedArray.recycle();
    }

    private void initView()
    {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        mLeftCursor.draw(canvas);
    }
}
