package com.m4399.videoeditor.core;


import android.util.Log;

import com.m4399.ffmpeg_cmd.CmdList;
import com.m4399.ffmpeg_cmd.FFmpegCmd;

public class VideoEditor
{
    private static final String TAG = "VideoEditor";

    public static void cropVideo(String videoPath, long startTime, long endTime, long duration, FFmpegCmd.OnCmdExecListener listener)
    {
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-y");
        cmd.append("-ss").append(startTime).append("-t").append(endTime).append("-accurate_seek");
        cmd.append("-i").append(videoPath);
        cmd.append("-codec").append("copy").append(videoPath);

        execCmd(cmd, duration, listener);
    }

    private static void execCmd(CmdList cmd, long duration, final FFmpegCmd.OnCmdExecListener listener)
    {
        String[] cmds = cmd.toArray(new String[cmd.size()]);
        String cmdLog = "";
        for (String ss : cmds)
        {
            cmdLog += ss;
        }
        Log.i(TAG, "cmd:" + cmdLog);
        FFmpegCmd.exec(cmds, duration, new FFmpegCmd.OnCmdExecListener()
        {
            @Override
            public void onSuccess()
            {
                listener.onSuccess();
            }

            @Override
            public void onFailure()
            {
                listener.onFailure();
            }

            @Override
            public void onProgress(float progress)
            {
                listener.onProgress(progress);
            }
        });
    }
}
