package com.m4399.videoeditor.common;


import java.util.ArrayList;
import java.util.Iterator;

public class SynchronizedPool<T> implements Recycler<T>, Releasable
{
    private final Allocator<T> mAllocator;
    private final SynchronizedPool.OnBufferAvailableListener onBufferAvailableListener;
    private int mLimit;
    private final ArrayList<T> mCache;

    public SynchronizedPool(Allocator<T> alloc, SynchronizedPool.OnBufferAvailableListener listener, int limit)
    {
        this.mCache = new ArrayList();
        this.mAllocator = alloc;
        this.onBufferAvailableListener = listener;
        this.mLimit = limit;
    }

    public SynchronizedPool(Allocator<T> alloc)
    {
        this(alloc, null, -1);
    }

    public Allocator<T> getAllocator()
    {
        return this.mAllocator;
    }

    public synchronized T allocate()
    {
        if (!mCache.isEmpty())
        {
            T item = mCache.remove(mCache.size() - 1);
            return this.mAllocator.allocate(this, item);
        }
        else if (this.mLimit == 0)
        {
            return null;
        }
        else
        {
            if (this.mLimit > 0)
            {
                --this.mLimit;
            }

            return this.mAllocator.allocate(this, (T) null);
        }
    }

    public void recycle(T object)
    {
        boolean var2;
        synchronized (this)
        {
            var2 = mLimit == 0 && mCache.isEmpty();
            mCache.add(object);
        }

        if (var2 && this.onBufferAvailableListener != null)
        {
            this.onBufferAvailableListener.onBufferAvailable(this);
        }

    }

    public synchronized void release()
    {
        Iterator var1 = mCache.iterator();

        while (var1.hasNext())
        {
            Object item = var1.next();
            this.mAllocator.release((T) item);
        }

    }

    public interface OnBufferAvailableListener
    {
        void onBufferAvailable(SynchronizedPool<?> var1);
    }
}

