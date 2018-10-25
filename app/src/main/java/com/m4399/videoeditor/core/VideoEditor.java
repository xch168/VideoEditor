package com.m4399.videoeditor.core;


import com.m4399.ffmpeg_cmd.FFmpegUtil;
import com.m4399.ffmpeg_cmd.OnVideoProcessListener;

public class VideoEditor
{
    private static final String TAG = "VideoEditor";

    public static void cropVideo(String videoPath, long startTime, long endTime, String destPath, OnVideoProcessListener listener)
    {
        FFmpegUtil.cropVideo(videoPath, startTime, endTime, destPath, listener);
    }

}
