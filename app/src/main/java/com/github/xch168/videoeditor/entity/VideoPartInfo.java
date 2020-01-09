package com.github.xch168.videoeditor.entity;


import android.graphics.Rect;

public class VideoPartInfo {
    private int startTime;
    private int endTime;
    private int duration;

    private Rect bounds;
    private int startScale;
    private int endScale;

    public int getStartTime()
    {
        return startTime;
    }

    public void setStartTime(int startTime)
    {
        this.startTime = startTime;
    }

    public int getEndTime()
    {
        return endTime;
    }

    public void setEndTime(int endTime)
    {
        this.endTime = endTime;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
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

    public int getStartScale()
    {
        return startScale;
    }

    public void setStartScale(int startScale)
    {
        this.startScale = startScale;
    }

    public int getEndScale()
    {
        return endScale;
    }

    public void setEndScale(int endScale)
    {
        this.endScale = endScale;
    }

    public boolean inTimeRange(int time) {
        return time > startTime && time < endTime;
    }

    public boolean inScaleRange(int scale) {
        return scale >= startScale && scale < endScale;
    }
}
