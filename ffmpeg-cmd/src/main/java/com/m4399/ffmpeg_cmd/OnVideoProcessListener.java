package com.m4399.ffmpeg_cmd;


public interface OnVideoProcessListener
{
    void onProcessStart();

    void onProcessProgress(float progress);

    void onProcessSuccess();

    void onProcessFailure();
}
