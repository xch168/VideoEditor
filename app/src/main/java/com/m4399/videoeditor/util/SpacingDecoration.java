package com.m4399.videoeditor.util;


import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

public class SpacingDecoration extends RecyclerView.ItemDecoration
{
    private int mHorizontalSpacing = 0;
    private int mVerticalSpacing = 0;
    private boolean mIncludeHEdge = false;//是否保留水平外边间距
    private boolean mIncludeVEdge = false;//是否保留垂直外边间距
    private boolean mHasHeader = false;//是否含有header

    /**
     * 间隔单位为dp
     * @param hSpacing 横向间隔（单位为dp）
     * @param vSpacing 纵向间隔（单位为dp）
     * @param includeEdge 是否包括边缘
     */
    public SpacingDecoration(Context context, float hSpacing, float vSpacing, boolean includeEdge)
    {
        this(context, hSpacing,vSpacing,includeEdge,includeEdge,false);
    }

    /**
     * 间隔单位为dp
     * @param hSpacing 横向间隔（单位为dp）
     * @param vSpacing 纵向间隔（单位为dp）
     * @param includeHEdge 是否保留水平外边间距
     * @param includeVEdge 是否保留垂直外边间距
     */
    public SpacingDecoration(Context context, float hSpacing, float vSpacing, boolean includeHEdge, boolean includeVEdge, boolean hasHeader)
    {
        mHorizontalSpacing = SizeUtil.dp2px(context, hSpacing);
        mVerticalSpacing = SizeUtil.dp2px(context, vSpacing);
        mIncludeHEdge = includeHEdge;
        mIncludeVEdge = includeVEdge;
        mHasHeader = hasHeader;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        // Only handle the vertical situation
        int position = parent.getChildAdapterPosition(view);
        //处理有header的情况
        if (mHasHeader)
        {
            if (position == 0)
            {
                return;//header不处理间距
            }
            else
            {
                position = position - 1;//减去header占用
            }
        }
        if (parent.getLayoutManager() instanceof GridLayoutManager)
        {
            GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
            int spanCount = layoutManager.getSpanCount();
            int column = position % spanCount;
            getGridItemOffsets(outRect, position, column, spanCount);
        }
        else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager)
        {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
            int spanCount = layoutManager.getSpanCount();
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            int column = lp.getSpanIndex();
            getGridItemOffsets(outRect, position, column, spanCount);
        }
        else if (parent.getLayoutManager() instanceof LinearLayoutManager)
        {
            //水平方向只有一项
            outRect.left = mHorizontalSpacing;
            outRect.right = mHorizontalSpacing;
            //处理垂直方向间距
            if (mIncludeVEdge)
            {
                if (position == 0)
                {
                    outRect.top = mVerticalSpacing;
                }
                outRect.bottom = mVerticalSpacing;
            }
            else
            {
                if (position > 0)
                {
                    outRect.top = mVerticalSpacing;
                }
            }
        }
    }

    private void getGridItemOffsets(Rect outRect, int position, int column, int spanCount)
    {
        //处理水平方向间距
        if (mIncludeHEdge)
        {
            outRect.left = mHorizontalSpacing * (spanCount - column) / spanCount;
            outRect.right = mHorizontalSpacing * (column + 1) / spanCount;
        }
        else
        {
            outRect.left = mHorizontalSpacing * column / spanCount;
            outRect.right = mHorizontalSpacing * (spanCount - 1 - column) / spanCount;
        }
        //处理垂直方向间距
        if (mIncludeVEdge)
        {
            if (position < spanCount)
            {
                outRect.top = mVerticalSpacing;
            }
            outRect.bottom = mVerticalSpacing;
        }
        else
        {
            if (position >= spanCount)
            {
                outRect.top = mVerticalSpacing;
            }
        }
    }
}
