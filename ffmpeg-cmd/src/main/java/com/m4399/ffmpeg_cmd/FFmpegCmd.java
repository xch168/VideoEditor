package com.m4399.ffmpeg_cmd;

/**
 * Project Name: VideoEditor
 * File Name:    FFmpegCmd.java
 * ClassName:    FFmpegCmd
 *
 * Description: TODO.
 *
 * @author XuCanHui
 * @date 2018年09月26日 14:27
 *
 * Copyright (c) 2018年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class FFmpegCmd
{
    static
    {
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();
}
