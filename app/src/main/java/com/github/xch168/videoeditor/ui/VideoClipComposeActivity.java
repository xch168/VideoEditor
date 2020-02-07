package com.github.xch168.videoeditor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.VideoEditorHelper;
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
    private long mTotalTime;

    private VideoEditorHelper mEditorHelper;

    private List<VideoPartInfo> mVideoPartInfoList = new ArrayList<>();

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

        mTotalTime = mVideoDuration;

        mPlayBtn = findViewById(R.id.iv);

        mTouchView = findViewById(R.id.touch_view);
        mTouchView.setOnClickListener(this);

        mTimeView = findViewById(R.id.tv_time);

        mPlaySwitchBtn = findViewById(R.id.iv_play_switch);

        mCutBtn = findViewById(R.id.tv_cut);
        mDelBtn = findViewById(R.id.tv_del);
        mRevokeBtn = findViewById(R.id.tv_revoke);
        mHandleVideoBtn = findViewById(R.id.btn_handle_video);

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setDataSource(new DataSource(mVideoPath));
        mVideoView.setOnPlayerEventListener(this);
        mVideoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
        mVideoView.start();

        mEditorTrackView = findViewById(R.id.editor_track_view);
        mEditorTrackView.setVideoPartInfoList(mVideoPartInfoList);
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
                Log.i("asdf", "seekTo:" + currentPos);
                mVideoView.seekTo(scaleToCurrentTime(scale));
                updateProgressText(currentPos);

                updateCuttingBtnState();
            }

            private int scaleToCurrentTime(int scale) {
                for (int i = 0; i < mVideoPartInfoList.size(); i++)  {
                    VideoPartInfo partInfo = mVideoPartInfoList.get(i);
                    if (partInfo.inScaleRange(scale)) {
                        return (int) (((float) (scale - partInfo.getStartScale()) / partInfo.getLength() * partInfo.getDuration()) + partInfo.getStartTime());
                    }
                }
                return (int) mTotalTime;
            }
        });
        mEditorTrackView.setVideoPath(mVideoPath);

        buildVideoPartInfo();
    }

    private void buildVideoPartInfo() {
        VideoPartInfo videoPartInfo = new VideoPartInfo();
        videoPartInfo.setStartTime(0);
        videoPartInfo.setEndTime(mVideoDuration);
        videoPartInfo.setStartScale(0);
        videoPartInfo.setEndScale(mEditorTrackView.getMaxScale());
        mVideoPartInfoList.add(videoPartInfo);

        printAllVideoPartInfo();
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

    public void handleCut(View view) {
        Log.i("asdf", "cut:" + mEditorTrackView.getCurrentScale() + " time:" + mVideoView.getCurrentPosition());
        VideoPartInfo currentPartInfo = getCurrentPartInfo();
        if (currentPartInfo != null) {
            VideoPartInfo videoPartInfo = new VideoPartInfo();
            videoPartInfo.setStartTime(mVideoView.getCurrentPosition());
            videoPartInfo.setEndTime(currentPartInfo.getEndTime());
            videoPartInfo.setStartScale(mEditorTrackView.getCurrentScale());
            videoPartInfo.setEndScale(currentPartInfo.getEndScale());

            currentPartInfo.setEndTime(mVideoView.getCurrentPosition());
            currentPartInfo.setEndScale(mEditorTrackView.getCurrentScale());

            int index = mVideoPartInfoList.indexOf(currentPartInfo) + 1;
            mVideoPartInfoList.add(index, videoPartInfo);
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
        VideoPartInfo curPartInfo = getCurrentPartInfo();
        if (curPartInfo != null) {
            int currentIndex = mVideoPartInfoList.indexOf(curPartInfo);
            int maxScale = mEditorTrackView.getMaxScale() - curPartInfo.getLength();
            int currentScale = curPartInfo.getStartScale();
            mVideoPartInfoList.remove(curPartInfo);
            calcTotalTime();
            for (int i = currentIndex; i < mVideoPartInfoList.size(); i++) {
                VideoPartInfo partInfo = mVideoPartInfoList.get(i);
                if (i == currentIndex) {
                    mVideoView.seekTo((int) partInfo.getStartTime());
                    updateProgressText(calcCurrentTime(partInfo.getStartTime()));
                }
                int length = partInfo.getLength();
                partInfo.setStartScale(i == currentIndex ? curPartInfo.getStartScale() : curPartInfo.getEndScale());
                partInfo.setEndScale(partInfo.getStartScale() + length);
                curPartInfo = partInfo;
            }
            mEditorTrackView.setMaxScale(maxScale);
            mEditorTrackView.setCurrentScale(currentScale);
        }

        updateDelBtnState();
        updateHandleVideoBtnState();

        printAllVideoPartInfo();
    }

    private void calcTotalTime() {
        int totalTime = 0;
        for (int i = 0; i < mVideoPartInfoList.size(); i++) {
            totalTime += mVideoPartInfoList.get(i).getDuration();
        }
        mTotalTime = totalTime;
    }

    private void updateProgressText(long calcCurrentTime) {
        mTimeView.setText(TimeUtil.getTimeSmartFormat(calcCurrentTime) + "/" + TimeUtil.getTimeSmartFormat(mTotalTime));
    }

    private long calcCurrentTime(long currentPos) {
        int currentTime = 0;
        VideoPartInfo currentPartInfo = getCurrentPartInfo();
        if (currentPartInfo != null)
        {
            int currentIndex = mVideoPartInfoList.indexOf(currentPartInfo);
            currentTime += (currentPos - currentPartInfo.getStartTime());
            for (int i = 0; i < currentIndex; i++)
            {
                VideoPartInfo partInfo = mVideoPartInfoList.get(i);
                currentTime += partInfo.getDuration();
            }
            return currentTime;
        }
        return currentPos;
    }

    public void handleVideo(View view) {
        if (mEditorHelper == null) {
            mEditorHelper = new VideoEditorHelper(this);
        }
        mEditorHelper.handleVideo(mVideoPartInfoList, mVideoPath);
    }


    @SuppressLint("SetTextI18n")
    private void updateProgress() {
        int currentPos = mVideoView.getCurrentPosition();
        mTimeView.setText(TimeUtil.getTimeSmartFormat(currentPos) + "/" + TimeUtil.getTimeSmartFormat(mVideoView.getDuration()));
        int currentScale = (int) ((float)currentPos / mVideoView.getDuration() * mEditorTrackView.getMaxScale());
        mEditorTrackView.setCurrentScale(currentScale);

        updateCuttingBtnState();
    }

    private void updateCuttingBtnState() {
        VideoPartInfo curPartInfo = getCurrentPartInfo();
        if (curPartInfo != null) {
            int currentPos = mVideoView.getCurrentPosition();
            long diffStart = currentPos - curPartInfo.getStartTime();
            long diffEnd = curPartInfo.getEndTime() - currentPos;
            mCutBtn.setEnabled(diffStart >= 1000 && diffEnd >= 1000);
        }
    }

    private void updateDelBtnState() {
        mDelBtn.setEnabled(mVideoPartInfoList.size() > 1);
    }

    private void updateHandleVideoBtnState() {
        if (mVideoPartInfoList.size() > 1) {
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

    private VideoPartInfo getCurrentPartInfo() {
        return mEditorTrackView.getCurrentPartInfo();
    }

    private void printAllVideoPartInfo() {
        Log.i("part", "======================start===========================");
        for (int i = 0; i < mVideoPartInfoList.size(); i++) {
            VideoPartInfo partInfo = mVideoPartInfoList.get(i);
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
        if (mVideoPartInfoList != null) {
            mVideoPartInfoList.clear();
            mVideoPartInfoList = null;
        }
    }

    public static void open(Context context, String videoPath, long duration) {
        Intent intent = new Intent(context, VideoClipComposeActivity.class);
        intent.putExtra("video_path", videoPath);
        intent.putExtra("video_duration", duration);
        context.startActivity(intent);
    }
}
