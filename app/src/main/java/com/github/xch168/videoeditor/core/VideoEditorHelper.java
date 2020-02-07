package com.github.xch168.videoeditor.core;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.github.xch168.videoeditor.entity.Video;
import com.github.xch168.videoeditor.entity.VideoPartInfo;
import com.github.xch168.videoeditor.ui.BaseActivity;
import com.github.xch168.videoeditor.ui.VideoPreviewActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XuCanHui on 2020/1/29.
 */
public class VideoEditorHelper {
    private Context mContext;

    private long mTotalTime;
    private int mCurrentIndex;
    private List<VideoPartInfo> mVideoPartInfoList;
    private List<Video> mVideoList;

    private String mVideoPath;
    private String mDstPath;

    private Handler mUiHandler;

    private boolean mIsMerging = false;

    private VideoEditor.OnEditListener mOnEditListener = new VideoEditor.OnEditListener() {
        @Override
        public void onSuccess() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsMerging) {
                        mIsMerging = false;
                        Toast.makeText(mContext, "处理完成", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        VideoPreviewActivity.open(mContext, VideoEditor.getSavePath());
                        return;
                    }
                    if (mCurrentIndex + 1 < mVideoPartInfoList.size()) {
                        cutVideo(++mCurrentIndex);
                    } else {
                        mIsMerging = true;
                        mergeVideo();
                    }
                }
            });
        }

        @Override
        public void onFailure() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "处理失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onProgress(float progress) {
            updateProgress(calcCurrentProgress(progress));
        }
    };

    public VideoEditorHelper(Context context) {
        mContext = context;
        mUiHandler = new Handler(Looper.getMainLooper());
        mVideoList = new ArrayList<>();
    }

    private float calcCurrentProgress(float progress) {
        if (progress > 1) {
            progress = 1;
        }
        int currentTime = 0;
        if (mCurrentIndex != -1) {
            VideoPartInfo partInfo = mVideoPartInfoList.get(mCurrentIndex);
            for (int i = 0; i < mCurrentIndex; i++) {
                VideoPartInfo part = mVideoPartInfoList.get(i);
                currentTime += part.getDuration();
            }
            currentTime += (progress * partInfo.getDuration());
        } else {
            currentTime = (int) ((1 + progress) * mTotalTime / 2);
        }
        progress = (float)currentTime / mTotalTime;
        return progress;
    }

    public void handleVideo(List<VideoPartInfo> videoPartInfoList, String videoPath) {
        showProgressDialog();

        mVideoPath = videoPath;
        mDstPath = Environment.getExternalStorageDirectory().getPath() + "/VideoEditor/";
        mVideoPartInfoList = mergeConsecutiveParts(videoPartInfoList);
        mVideoList.clear();
        mTotalTime = calcTotalTime();
        mCurrentIndex = 0;
        cutVideo(mCurrentIndex);
    }

    private List<VideoPartInfo> mergeConsecutiveParts(List<VideoPartInfo> videoPartInfoList) {
        if (videoPartInfoList.size() > 1) {
            List<VideoPartInfo> result = cloneList(videoPartInfoList);
            for (int i = 0; i < result.size(); i++) {
                VideoPartInfo partInfo = result.get(i);
                int nextPartIndex = i + 1;
                if (nextPartIndex < result.size()) {
                    VideoPartInfo nextPartInfo = result.get(nextPartIndex);
                    if (partInfo.getEndTime() == nextPartInfo.getStartTime()) {
                        partInfo.setEndTime(nextPartInfo.getEndTime());
                        result.remove(nextPartInfo);
                        i--;
                    }
                }
            }
            return result;
        }
        return videoPartInfoList;
    }

    private List<VideoPartInfo> cloneList(List<VideoPartInfo> sourceList) {
        List<VideoPartInfo> resultList = new ArrayList<>();
        for (int i = 0; i < sourceList.size(); i++) {
            VideoPartInfo partInfo = sourceList.get(i);
            resultList.add(partInfo.clone());
        }
        return resultList;
    }

    private int calcTotalTime() {
        int totalTime = 0;
        for (int i = 0; i < mVideoPartInfoList.size(); i++) {
            VideoPartInfo partInfo = mVideoPartInfoList.get(i);
            totalTime += partInfo.getDuration();
        }
        return totalTime * 2;
    }

    private void cutVideo(int index) {
        VideoPartInfo partInfo = mVideoPartInfoList.get(mCurrentIndex);
        String path = mDstPath + "part" + index + ".mp4";
        Video video = new Video();
        video.setVideoPath(path);
        video.setDuration(partInfo.getDuration());
        mVideoList.add(video);
        VideoEditor.cropVideo(mVideoPath, path, partInfo.getStartTime(), partInfo.getEndTime(), mOnEditListener);
    }

    private void mergeVideo() {
        if (!mVideoList.isEmpty()) {
            mCurrentIndex = -1;
            String partListFile = createPartListFile(mVideoList);
            VideoEditor.mergeVideo2(partListFile, mTotalTime, mOnEditListener);
        }
    }

    private static String createPartListFile(List<Video> videoList) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/VideoEditor/partList.txt";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < videoList.size(); i++) {
            Video video = videoList.get(i);
            sb.append("file ").append("'").append(video.getVideoPath()).append("'").append("\n");
        }
        File partListFile = new File(path);
        partListFile.deleteOnExit();
        try {
            boolean success = partListFile.createNewFile();
            if (success) {
                FileWriter fileWriter = new FileWriter(partListFile);
                fileWriter.write(sb.toString());
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private void showProgressDialog() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                ((BaseActivity)mContext).showProgressDialog();
            }
        });
    }

    private void hideProgressDialog() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                ((BaseActivity)mContext).hideProgressDialog();
            }
        });
    }

    private void updateProgress(float percent) {
        ((BaseActivity)mContext).updateProgress(percent);
    }
}
