package com.github.xch168.videoeditor.core;


import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FrameExtractor {
    private static final String TAG = "FrameExtractor";

    private final ExecutorService mExecutor;

    private final MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

    private String mVideoPath;

    private int mDstWidth = 100;
    private int mDstHeight = 100;

    private SparseArray mMetaDataCache = new SparseArray();

    public FrameExtractor() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    private class Task extends AsyncTask<Void, Void, Bitmap> {
        private final long timestampNano;

        private FrameExtractor.Callback callback;

        Task(FrameExtractor.Callback callback, long timestampNano) {
            this.callback = callback;
            this.timestampNano = timestampNano;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (isCancelled()) {
                return null;
            }
            long micro = TimeUnit.MILLISECONDS.toMicros(timestampNano);
            Bitmap bmp = mRetriever.getFrameAtTime(micro);

            if (bmp == null) {
                return null;
            }
            if (isCancelled()) {
                return null;
            }
            bmp = Bitmap.createScaledBitmap(bmp, mDstWidth, mDstHeight, true);
            return bmp;
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            callback.onFrameExtracted(bitmap, timestampNano);
        }
    }

    public AsyncTask<Void, Void, Bitmap> newTask(FrameExtractor.Callback callback, long timestampNano) {
        return new FrameExtractor.Task(callback, timestampNano).executeOnExecutor(mExecutor);
    }

    public boolean setDataSource(String source) {
        try {
            mVideoPath = source;
            mRetriever.setDataSource(source);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public long getVideoDuration() {
        Object result;
        if ((result = mMetaDataCache.get(MediaMetadataRetriever.METADATA_KEY_DURATION)) != null) {
            return (Long) result;
        } else if (mVideoPath != null) {
            String durationStr = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null && !"".equals(durationStr)) {
                Long duration = Long.parseLong(durationStr);
                mMetaDataCache.put(MediaMetadataRetriever.METADATA_KEY_DURATION, duration);
                return duration;
            } else {
                Log.e(TAG, "Retrieve video duration failed");
                return 0;
            }
        } else {
            Log.e(TAG, "Has no video source,so duration is 0");
            return 0;
        }
    }

    public void getFrameByInterval(int interval, Callback callback) {
        long duration = getVideoDuration();
        int frameCount = (int) (duration / interval);
        for (int i = 0; i < frameCount; i++) {
            long timestamp = i * interval;
            newTask(callback, timestamp);
        }
    }

    public void setDstSize(int width, int height) {
        mDstWidth = width;
        mDstHeight = height;
    }

    public void release() {
        mExecutor.shutdownNow();
        while (true) {
            try {
                if (mExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ignored) {}
        }

        mRetriever.release();
    }

    public interface Callback {
        void onFrameExtracted(Bitmap bitmap, long timestamp);
    }
}
