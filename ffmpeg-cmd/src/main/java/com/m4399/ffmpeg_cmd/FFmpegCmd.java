package com.m4399.ffmpeg_cmd;


public class FFmpegCmd
{
    static
    {
        System.loadLibrary("ffmpeg-cmd");
    }

    private static OnCmdExecListener sOnCmdExecListener;
    private static long sDuration;

    public static native int exec(int argc, String[] argv);

    public static native void exit();

    public static void exec(String[] cmds, long duration, OnCmdExecListener listener)
    {
        sOnCmdExecListener = listener;
        sDuration = duration;

        exec(cmds.length, cmds);
    }

    public static void onExecuted(int ret)
    {
        if (sOnCmdExecListener != null)
        {
            if (ret == 0)
            {
                sOnCmdExecListener.onProgress(sDuration);
                sOnCmdExecListener.onSuccess();
            }
            else
            {
                sOnCmdExecListener.onFailure();
            }
        }
    }

    public static void onProgress(float progress)
    {
        if (sOnCmdExecListener != null)
        {
            if (sDuration != 0)
            {
                sOnCmdExecListener.onProgress(progress / (sDuration / 1000) * 0.95f);
            }
        }
    }


    public interface OnCmdExecListener
    {
        void onSuccess();

        void onFailure();

        void onProgress(float progress);
    }


}
