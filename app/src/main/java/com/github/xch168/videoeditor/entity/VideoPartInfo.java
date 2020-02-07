package com.github.xch168.videoeditor.entity;


import android.graphics.Rect;

public class VideoPartInfo {
    private long startTime;
    private long endTime;
    private long duration;

    private Rect bounds;
    private int startScale;
    private int endScale;

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
        return endTime - startTime;
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
        return time >= startTime && time < endTime;
    }

    public boolean inScaleRange(int scale) {
        return scale >= startScale && scale < endScale;
    }

    public int getLength() {
        return endScale - startScale;
    }

    public VideoPartInfo clone() {
        VideoPartInfo partInfo = new VideoPartInfo();
        partInfo.startTime = startTime;
        partInfo.endTime = endTime;
        partInfo.duration = duration;
        partInfo.bounds = new Rect();
        partInfo.startScale = startScale;
        partInfo.endScale = endScale;
        return partInfo;
    }

}
