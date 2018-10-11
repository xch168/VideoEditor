package com.m4399.videoeditor.media;


import com.m4399.videoeditor.common.AtomicRefCounted;
import com.m4399.videoeditor.common.Recycler;

public abstract class AtomicShareable<T> extends AtomicRefCounted
{
    protected final Recycler<T> _Recycler;

    public AtomicShareable(Recycler<T> recycler)
    {
        this._Recycler = recycler;
    }

    protected abstract void onLastRef();
}