package com.github.xch168.videoeditor.helper;


import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import com.github.xch168.videoeditor.App;
import com.github.xch168.videoeditor.entity.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoManger {
    private static VideoManger sInstance;

    private ContentResolver mContentResolver;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    public static VideoManger getInstance() {
        if (sInstance == null) {
            sInstance = new VideoManger();
        }
        return sInstance;
    }

    private VideoManger() {
        mContentResolver = App.getContext().getContentResolver();

        mHandlerThread = new HandlerThread("video-manger");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public List<Video> getAllVideo() {
        List<Video> videoList = new ArrayList<>();
        String[] mediaColumns = new String[] {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION
        };
        Cursor cursor = mContentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);
        if (cursor == null) return videoList;

        if (cursor.moveToFirst()) {
            do {
                Video video = new Video();
                video.setVideoPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                File file = new File(video.getVideoPath());
                boolean canRead = file.canRead();
                long length = file.length();
                if (!canRead || length == 0) {
                    continue;
                }
                video.setVideoName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                if (duration < 0) {
                    duration = 0;
                }
                video.setDuration(duration);

                if (video.getVideoName() != null && video.getVideoName().toLowerCase().endsWith("mp4")) {
                    videoList.add(video);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return videoList;
    }

    public void getAllVideo(final OnLoadCompletionListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    List<Video> videoList = getAllVideo();
                    listener.onLoadCompletion(videoList);
                }
            }
        });
    }

    public interface OnLoadCompletionListener {
        void onLoadCompletion(List<Video> videoList);
    }
}
