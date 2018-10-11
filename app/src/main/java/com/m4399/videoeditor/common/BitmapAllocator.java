package com.m4399.videoeditor.common;

import com.m4399.videoeditor.media.ShareableBitmap;

public class BitmapAllocator implements Allocator<ShareableBitmap>
{

    private final int width;
    private final int height;

    public BitmapAllocator(int w, int h)
    {
        width = w;
        height = h;
    }

    @Override
    public ShareableBitmap allocate(Recycler<ShareableBitmap> recycler, ShareableBitmap reused)
    {
        if (reused != null)
        {
            reused.reset();
            return reused;
        }

        return new ShareableBitmap(recycler, width, height);
    }

    @Override
    public void recycle(ShareableBitmap object)
    {

    }

    @Override
    public void release(ShareableBitmap object)
    {
        object.getData().recycle();
    }

}
