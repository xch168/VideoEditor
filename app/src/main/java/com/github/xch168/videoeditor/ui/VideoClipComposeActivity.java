package com.github.xch168.videoeditor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.VideoEditor;
import com.github.xch168.videoeditor.entity.Video;
import com.github.xch168.videoeditor.entity.VideoPartInfo;
import com.github.xch168.videoeditor.widget.EditorMediaTrackView;
import com.github.xch168.videoeditor.widget.EditorTrackView;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.utils.TimeUtil;
import com.kk.taurus.playerbase.widget.BaseVideoView;

import java.util.ArrayList;
import java.util.List;

public class VideoClipComposeActivity extends BaseActivity implements OnPlayerEventListener, View.OnClickListener {

    private static final int MSG_UPDATE_PROGRESS = 1;

    private BaseVideoView mVideoView;
    private ImageView mPlayBtn;
    private View mTouchView;
    private TextView mTimeView;
    private EditorTrackView mEditorTrackView;
    private ImageView mPlaySwitchBtn;
    private View mCutBtn;
    private View mDelBtn;
    private View mRevokeBtn;
    private View mHandleVideoBtn;

    private String mVideoPath;
    private long mVideoDuration;

    @SuppressLint("HandlerLeak")
    private Handler mUiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    updateProgress();

                    mUiHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clip_compose);
        setTitle("视频剪辑和合成");
        setBackBtnVisible(true);

        mVideoPath = getIntent().getStringExtra("video_path");
        mVideoDuration = getIntent().getLongExtra("video_duration", 0);

        mPlayBtn = findViewById(R.id.iv);

        mTouchView = findViewById(R.id.touch_view);
        mTouchView.setOnClickListener(this);

        mTimeView = findViewById(R.id.tv_time);

        mPlaySwitchBtn = findViewById(R.id.iv_play_switch);

        mCutBtn = findViewById(R.id.tv_cutting);
        mDelBtn = findViewById(R.id.tv_del);
        mRevokeBtn = findViewById(R.id.tv_revoke);
        mHandleVideoBtn = findViewById(R.id.btn_handle_video);

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setDataSource(new DataSource(mVideoPath));
        mVideoView.setOnPlayerEventListener(this);
        mVideoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
        mVideoView.start();

        mEditorTrackView = findViewById(R.id.editor_track_view);
        mEditorTrackView.setOnTrackViewChangeListener(new EditorMediaTrackView.OnTrackViewChangeListener() {
            @Override
            public void onStartTrackingTouch() {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
            }

            @Override
            public void onScaleChanged(int scale) {
                int currentPos = (int) ((float)scale / mEditorTrackView.getMaxScale() * mVideoView.getDuration());
                mTimeView.setText(TimeUtil.getTimeSmartFormat(currentPos) + "/" + TimeUtil.getTimeSmartFormat(mVideoView.getDuration()));
                Log.i("asdf", "seekTo:" + currentPos);
                mVideoView.seekTo(currentPos);

                updateCuttingBtnState();
            }
        });
        mEditorTrackView.setVideoPath(mVideoPath);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case PLAYER_EVENT_ON_START:
            case PLAYER_EVENT_ON_RESUME:
                mPlayBtn.setVisibility(View.GONE);
                mPlaySwitchBtn.setSelected(false);
                mUiHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                break;
            case PLAYER_EVENT_ON_PAUSE:
            case PLAYER_EVENT_ON_PLAY_COMPLETE:
                mPlayBtn.setVisibility(View.VISIBLE);
                mPlaySwitchBtn.setSelected(true);
                mUiHandler.removeMessages(MSG_UPDATE_PROGRESS);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        handlePlayStateSwitch();
    }

    public void handlePlaySwitch(View view) {
        handlePlayStateSwitch();
    }

    private void handlePlayStateSwitch() {
        if (!mVideoView.isInPlaybackState()) {
            mVideoView.start();
            return;
        }
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        } else {
            if (mVideoView.getState() == IPlayer.STATE_PLAYBACK_COMPLETE) {
                mVideoView.start(0);
            } else {
                mVideoView.resume();
            }
        }
    }

    public void handleCutting(View view) {
        Log.i("asdf", "cut:" + mEditorTrackView.getCurrentScale() + " time:" + mVideoView.getCurrentPosition());
        int currentPartIndex = getCurrentPartIndex();
        VideoPartInfo currentPartInfo = mEditorTrackView.getVideoPartInfo(currentPartIndex);
        if (currentPartInfo != null) {
            VideoPartInfo videoPartInfo = new VideoPartInfo();
            videoPartInfo.setStartTime(mVideoView.getCurrentPosition());
            videoPartInfo.setEndTime(currentPartInfo.getEndTime());
            videoPartInfo.setStartScale(mEditorTrackView.getCurrentScale());
            videoPartInfo.setEndScale(currentPartInfo.getEndScale());

            currentPartInfo.setEndTime(mVideoView.getCurrentPosition());
            currentPartInfo.setEndScale(mEditorTrackView.getCurrentScale());

            mEditorTrackView.getVideoPartInfoList().add(currentPartIndex + 1, videoPartInfo);
            mEditorTrackView.update();

            updateDelBtnState();
            updateHandleVideoBtnState();
            mCutBtn.setEnabled(false);
        }
        printAllVideoPartInfo();
    }

    public void handleRevoke(View view) {

    }

    public void handleDel(View view) {
        VideoPartInfo curPartInfo = mEditorTrackView.getVideoPartInfo(getCurrentPartIndex());
        if (curPartInfo != null) {
            mEditorTrackView.getVideoPartInfoList().remove(curPartInfo);
        }

        updateDelBtnState();
        updateHandleVideoBtnState();

        printAllVideoPartInfo();
    }

    private String mDstPath;
    private int mCurrentIndex;
    private boolean mIsMerging = false;
    private long mTotalTime;
    private List<VideoPartInfo> mPendingHandleVideoPartList;
    private List<Video> mVideoPartList = new ArrayList<>();
    private VideoEditor.OnEditListener mOnEditListener = new VideoEditor.OnEditListener() {

        @Override
        public void onSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mIsMerging) {
                        mIsMerging = false;
                        Toast.makeText(VideoClipComposeActivity.this, "处理完成", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        VideoPreviewActivity.open(VideoClipComposeActivity.this, VideoEditor.getSavePath());
                        return;
                    }
                    if (mCurrentIndex + 1 < mPendingHandleVideoPartList.size()) {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    Toast.makeText(VideoClipComposeActivity.this, "处理失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onProgress(float progress) {
            updateProgress(calcCurrentProgress(progress));
        }
    };

    private float calcCurrentProgress(float progress) {
        VideoPartInfo partInfo = mPendingHandleVideoPartList.get(mCurrentIndex);
        int currentTime = 0;
        for (int i = 0; i < mCurrentIndex; i++) {
            VideoPartInfo part = mPendingHandleVideoPartList.get(i);
            currentTime += part.getDuration();
            Log.i("asdf", "gg:" + part.getDuration());
        }
        currentTime += (progress * partInfo.getDuration());
        progress = (float)currentTime / mTotalTime;
        Log.i("asdf", "gg:current:" + currentTime + " total:" + mTotalTime + " progress:" + progress);
        return progress;
    }

    private int calcTotalTime() {
        int totalTime = 0;
        for (int i = 0; i < mPendingHandleVideoPartList.size(); i++) {
            VideoPartInfo partInfo = mPendingHandleVideoPartList.get(i);
            totalTime += partInfo.getDuration();
        }
        return totalTime * 2;
    }

    private void cutVideo(int index) {
        VideoPartInfo partInfo = mPendingHandleVideoPartList.get(mCurrentIndex);
        String path = mDstPath + "part" + index + ".mp4";
        Video video = new Video();
        video.setVideoPath(path);
        video.setDuration(partInfo.getDuration());
        mVideoPartList.add(video);
        VideoEditor.cropVideo(mVideoPath, path, partInfo.getStartTime(), partInfo.getEndTime(), mOnEditListener);
    }

    private void mergeVideo() {
        if (!mVideoPartList.isEmpty()) {
            VideoEditor.mergeVideo2(mVideoPartList, mOnEditListener);
        }
    }

    public void handleVideo(View view) {
        showProgressDialog();
        mDstPath = Environment.getExternalStorageDirectory().getPath() + "/VideoEditor/";
        mPendingHandleVideoPartList = mergeConsecutiveParts();
        mVideoPartList.clear();
        mTotalTime = calcTotalTime();
        cutVideo(mCurrentIndex);
    }

    private List<VideoPartInfo> mergeConsecutiveParts() {
        List<VideoPartInfo> videoPartInfoList = mEditorTrackView.getVideoPartInfoList();
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
            resultList.add(partInfo.copy());
        }
        return resultList;
    }

    @SuppressLint("SetTextI18n")
    private void updateProgress() {
        int currentPos = mVideoView.getCurrentPosition();
        mTimeView.setText(TimeUtil.getTimeSmartFormat(currentPos) + "/" + TimeUtil.getTimeSmartFormat(mVideoView.getDuration()));
        float currentScale = (float)currentPos / mVideoView.getDuration() * mEditorTrackView.getMaxScale();
        mEditorTrackView.setCurrentScale(currentScale);

        updateCuttingBtnState();
    }

    private void updateCuttingBtnState() {
        int currentPos = mVideoView.getCurrentPosition();
        VideoPartInfo curPartInfo = mEditorTrackView.getVideoPartInfo(getCurrentPartIndex());
        if (curPartInfo != null) {
            int diffStart = currentPos - curPartInfo.getStartTime();
            int diffEnd = curPartInfo.getEndTime() - currentPos;
            mCutBtn.setEnabled(diffStart >= 1000 && diffEnd >= 1000);
        }
    }

    private void updateDelBtnState() {
        mDelBtn.setEnabled(mEditorTrackView.getVideoPartInfoList().size() > 1);
    }

    private void updateHandleVideoBtnState() {
        if (mEditorTrackView.getVideoPartInfoList().size() > 1) {
            mHandleVideoBtn.setEnabled(true);
        } else {
            VideoPartInfo curPartInfo = mEditorTrackView.getVideoPartInfo(0);
            if (curPartInfo.getStartTime() == 0 && curPartInfo.getEndTime() == mVideoView.getDuration()) {
                mHandleVideoBtn.setEnabled(false);
            } else {
                mHandleVideoBtn.setEnabled(true);
            }
        }
    }

    private int getCurrentPartIndex() {
        for (int i = 0; i < mEditorTrackView.getVideoPartInfoList().size(); i++) {
            VideoPartInfo partInfo = mEditorTrackView.getVideoPartInfoList().get(i);
            if (partInfo.inTimeRange(mVideoView.getCurrentPosition())) {
                return i;
            }
        }
        return -1;
    }

    private void printAllVideoPartInfo() {
        Log.i("part", "======================start===========================");
        for (int i = 0; i < mEditorTrackView.getVideoPartInfoList().size(); i++) {
            VideoPartInfo partInfo = mEditorTrackView.getVideoPartInfoList().get(i);
            Log.i("asdf", "part:" + i + " ==> startT:" + partInfo.getStartTime() + " endT:" + partInfo.getEndTime() + " --> startS:" + partInfo.getStartScale() + " endS:" + partInfo.getEndScale());
        }
        Log.i("part", "----------------------end---------------------------");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUiHandler != null) {
            mUiHandler.removeMessages(MSG_UPDATE_PROGRESS);
            mUiHandler = null;
        }
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }
    }

    public static void open(Context context, String videoPath, long duration) {
        Intent intent = new Intent(context, VideoClipComposeActivity.class);
        intent.putExtra("video_path", videoPath);
        intent.putExtra("video_duration", duration);
        context.startActivity(intent);
    }
}
