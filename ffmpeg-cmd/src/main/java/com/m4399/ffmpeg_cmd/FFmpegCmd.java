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


    public interface OnCmdExecListener
    {
        void onSuccess();

        void onFailure();

        void onProgress(float progress);
    }


}
