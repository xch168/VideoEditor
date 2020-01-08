package com.github.xch168.videoeditor.entity;


import android.graphics.Rect;

public class VideoPartInfo {
    private long startTime;
    private long endTime;
    private long duration;

    private Rect bounds;
    private int startPosition;
    private int endPosition;

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public Rect getBounds()
    {
        return bounds;
    }

    public void setBounds(Rect bounds)
    {
        this.bounds = bounds;
    }

    public int getStartPosition()
    {
        return startPosition;
    }

    public void setStartPosition(int startPosition)
    {
        this.startPosition = startPosition;
    }

    public int getEndPosition()
    {
        return endPosition;
    }

    public void setEndPosition(int endPosition)
    {
        this.endPosition = endPosition;
    }
}
