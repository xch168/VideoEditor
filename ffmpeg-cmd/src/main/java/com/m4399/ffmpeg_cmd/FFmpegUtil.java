package com.m4399.ffmpeg_cmd;

import android.util.Log;

public class FFmpegUtil
{
    private static final String TAG = "FFmpegUtil";

    /**
     * 使用FFmpeg命令行进行视频剪切
     *
     * @param srcFile    源文件
     * @param startTime  剪切的开始时间
     * @param endTime    剪切的结束时间
     * @param destFile   剪切后的文件
     */
    public static void cropVideo(String srcFile, long startTime, long endTime, String destFile, OnVideoProcessListener listener)
    {
        long duration = endTime - startTime;
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-y");
        cmd.append("-ss").append(startTime/ 1000).append("-t").append(duration / 1000).append("-accurate_seek");
        cmd.append("-i").append(srcFile);
        cmd.append("-codec").append("copy").append(destFile);

        execCmd(cmd, duration, listener);
    }

    private static void execCmd(CmdList cmd, long duration, final OnVideoProcessListener listener)
    {
        String[] cmds = cmd.toArray(new String[cmd.size()]);
        Log.i(TAG, "cmd:" + cmd);
        listener.onProcessStart();
        FFmpegCmd.exec(cmds, duration, new FFmpegCmd.OnCmdExecListener()
        {
            @Override
            public void onSuccess()
            {
                listener.onProcessSuccess();
            }

            @Override
            public void onFailure()
            {
                listener.onProcessFailure();
            }

            @Override
            public void onProgress(float progress)
            {
                listener.onProcessProgress(progress);
            }
        });
    }
}
