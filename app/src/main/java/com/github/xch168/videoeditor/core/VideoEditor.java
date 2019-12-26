package com.github.xch168.videoeditor.core;


import android.os.Environment;
import android.util.Log;

import com.github.xch168.ffmpeg_cmd.CmdList;
import com.github.xch168.ffmpeg_cmd.FFmpegCmd;
import com.github.xch168.videoeditor.entity.Video;

import java.io.File;
import java.util.List;

public class VideoEditor {
    private static final String TAG = "VideoEditor";

    public static void cropVideo(String videoPath, long startTime, long endTime, OnEditListener listener) {
        long duration = endTime - startTime;
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-y");
        cmd.append("-ss").append(startTime/ 1000).append("-t").append(duration / 1000).append("-accurate_seek");
        cmd.append("-i").append(videoPath);
        cmd.append("-codec").append("copy").append(getSavePath());

        execCmd(cmd, duration, listener);
    }

    public static void mergeVideo(List<Video> videoList, OnEditListener listener) {
        long duration = 0;
        StringBuilder filterParams = new StringBuilder();
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-y");

        for (int i = 0; i < videoList.size(); i++) {
            filterParams.append("[").append(i).append(":v]").append("scale=").append(720).append(":").append(1080)
                          .append(",setdar=").append("720/1080").append("[outv").append(i).append("];");
        }
        for (int i = 0; i < videoList.size(); i++) {
            Video video = videoList.get(i);
            filterParams.append("[outv").append(i).append("]");
            cmd.append("-i").append(video.getVideoPath());

            duration += video.getDuration();
        }
        filterParams.append("concat=n=").append(videoList.size()).append(":v=1:a=0[outv]");
        filterParams.append(";");
        for (int i = 0; i < videoList.size(); i++) {
            filterParams.append("[").append(i).append(":a]");
        }
        filterParams.append("concat=n=").append(videoList.size()).append(":v=0:a=1[outa]");
        cmd.append("-filter_complex");
        cmd.append(filterParams.toString());
        cmd.append("-map").append("[outv]");
        cmd.append("-map").append("[outa]");
        cmd.append(getSavePath());

        execCmd(cmd, duration, listener);
    }

    public static void addPictureWatermark(String videoPath, long duration, String watermarkPath, OnEditListener listener) {
        StringBuilder param = new StringBuilder();
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-i").append(videoPath);
        cmd.append("-vf");
        param.append("movie=").append(watermarkPath).append(",scale=").append(256).append(":").append(144).append("[watermark];");
        param.append("[in][watermark] ");
        param.append("overlay=").append("main_w-overlay_w-").append(25).append(":").append(25).append("[out]");
        cmd.append(param.toString());
        cmd.append(getSavePath());

        execCmd(cmd, duration, listener);
    }

    public static void addTextWatermark(String videoPath, long duration, String textWatermark, OnEditListener listener) {
        StringBuilder param = new StringBuilder();
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-i").append(videoPath);
        cmd.append("-vf");
        param.append("drawtext=");
        param.append("text=").append(textWatermark).append(":").append("fontsize=").append(24).append(":").append("fontcolor=").append("white").append(":");
        param.append("x=").append(10).append(":").append("y=").append(10).append(":");
        param.append("shadowy=").append(2);
        cmd.append(param.toString());
        cmd.append(getSavePath());

        execCmd(cmd, duration, listener);
    }

    private static void execCmd(CmdList cmd, long duration, final OnEditListener listener) {
        String[] cmds = cmd.toArray(new String[cmd.size()]);
        String cmdLog = "";
        for (String ss : cmds) {
            cmdLog = cmdLog + " " + ss;
        }
        Log.i(TAG, "cmd:" + cmdLog);
        FFmpegCmd.exec(cmds, duration, new FFmpegCmd.OnCmdExecListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure() {
                listener.onFailure();
            }

            @Override
            public void onProgress(float progress) {
                listener.onProgress(progress);
            }
        });
    }

    public static String getSavePath() {
        String savePath = Environment.getExternalStorageDirectory().getPath() + "/VideoEditor/";
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return savePath + "out.mp4";
    }

    public interface OnEditListener {
        void onSuccess();

        void onFailure();

        void onProgress(float progress);
    }
}
