package com.m4399.videoeditor.common;

public interface Allocator<T>
{
    T allocate(Recycler<T> var1, T var2);

    void recycle(T var1);

    void release(T var1);
}
