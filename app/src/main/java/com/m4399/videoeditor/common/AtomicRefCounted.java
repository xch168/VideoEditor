package com.m4399.videoeditor.common;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AtomicRefCounted implements Releasable
{
    private final AtomicInteger _RefCount = new AtomicInteger();

    public AtomicRefCounted()
    {
        this._RefCount.set(1);
    }

    public void reset()
    {
        Assert.assertEquals(0, this._RefCount.get());
        this._RefCount.set(1);
    }

    public final void ref()
    {
        int var1 = this._RefCount.incrementAndGet();
        Assert.assertGreaterThan(var1, 1);
    }

    protected abstract void onLastRef();

    public final void release()
    {
        int var1 = this._RefCount.decrementAndGet();
        if (var1 == 0)
        {
            this.onLastRef();
        }
        else
        {
            Assert.assertGreaterThan(var1, 0);
        }
    }
}
