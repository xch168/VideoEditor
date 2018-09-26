package com.m4399.videoeditor.entity;


public class Video
{
    private int id;
    private String videoName;
    private String videoPath;
    private long duration;
    private long videoSize;

    public int getId()
    {
        return id;
    }

    public String getVideoName()
    {
        return videoName;
    }

    public long getDuration()
    {
        return duration;
    }

    public long getVideoSize()
    {
        return videoSize;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setVideoName(String videoName)
    {
        this.videoName = videoName;
    }

    public void setVideoPath(String videoPath)
    {
        this.videoPath = videoPath;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public void setVideoSize(long videoSize)
    {
        this.videoSize = videoSize;
    }

    public String getVideoPath()
    {
        return videoPath;
    }
}
