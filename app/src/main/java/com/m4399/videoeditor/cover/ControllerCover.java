package com.m4399.videoeditor.cover;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kk.taurus.playerbase.event.BundlePool;
import com.kk.taurus.playerbase.event.EventKey;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.log.PLog;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.player.OnTimerUpdateListener;
import com.kk.taurus.playerbase.receiver.BaseCover;
import com.kk.taurus.playerbase.receiver.ICover;
import com.kk.taurus.playerbase.receiver.IReceiverGroup;
import com.kk.taurus.playerbase.touch.OnTouchGestureListener;
import com.kk.taurus.playerbase.utils.TimeUtil;
import com.m4399.videoeditor.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ControllerCover extends BaseCover implements OnTimerUpdateListener,
                                                          OnTouchGestureListener
{
    private final int MSG_CODE_DELAY_HIDDEN_CONTROLLER = 101;

    @BindView(R.id.cover_player_controller_bottom_container)
    View mBottomContainer;
    @BindView(R.id.cover_player_controller_image_view_play_state)
    ImageView mStateIcon;
    @BindView(R.id.cover_player_controller_text_view_curr_time)
    TextView mCurrTime;
    @BindView(R.id.cover_player_controller_text_view_total_time)
    TextView mTotalTime;
    @BindView(R.id.cover_player_controller_seek_bar)
    SeekBar mSeekBar;

    private int mBufferPercentage;

    private int mSeekProgress = -1;

    private boolean mTimerUpdateProgressEnable = true;

    private Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSG_CODE_DELAY_HIDDEN_CONTROLLER:
                    PLog.d(getTag().toString(), "msg_delay_hidden...");
                    setControllerState(false);
                    break;
            }
        }
    };

    private boolean mGestureEnable = true;

    private String mTimeFormat;

    private Unbinder unbinder;
    private ObjectAnimator mBottomAnimator;

    public ControllerCover(Context context)
    {
        super(context);
    }

    @Override
    public View onCreateCoverView(Context context)
    {
        return View.inflate(context, R.layout.controller_cover, null);
    }

    @Override
    public int getCoverLevel()
    {
        return ICover.COVER_LEVEL_LOW;
    }

    @Override
    public void onReceiverBind()
    {
        super.onReceiverBind();
        unbinder = ButterKnife.bind(this, getView());

        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        getGroupValue().registerOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
    }

    @Override
    protected void onCoverDetachedToWindow()
    {
        super.onCoverDetachedToWindow();
        mBottomContainer.setVisibility(View.GONE);
        removeDelayHiddenMessage();
    }

    @Override
    public void onReceiverUnBind()
    {
        super.onReceiverUnBind();

        cancelBottomAnimation();

        getGroupValue().unregisterOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
        removeDelayHiddenMessage();
        mHandler.removeCallbacks(mSeekEventRunnable);

        unbinder.unbind();
    }

    @OnClick({ R.id.cover_player_controller_image_view_play_state})
    public void onViewClick(View view)
    {
        switch (view.getId())
        {
            case R.id.cover_player_controller_image_view_play_state:
                boolean selected = mStateIcon.isSelected();
                if (selected)
                {
                    requestResume(null);
                }
                else
                {
                    requestPause(null);
                }
                mStateIcon.setSelected(!selected);
                break;
        }
    }

    private IReceiverGroup.OnGroupValueUpdateListener mOnGroupValueUpdateListener = new IReceiverGroup.OnGroupValueUpdateListener()
    {
        @Override
        public String[] filterKeys()
        {
            return new String[]{"timer_update_enable"};
        }

        @Override
        public void onValueUpdate(String key, Object value)
        {
            if (key.equals("timer_update_enable"))
            {
                mTimerUpdateProgressEnable = (boolean) value;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (fromUser)
            {
                updateUI(progress, seekBar.getMax());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            sendSeekEvent(seekBar.getProgress());
        }
    };

    private void sendSeekEvent(int progress)
    {
        mTimerUpdateProgressEnable = false;
        mSeekProgress = progress;
        mHandler.removeCallbacks(mSeekEventRunnable);
        mHandler.postDelayed(mSeekEventRunnable, 300);
    }

    private Runnable mSeekEventRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (mSeekProgress < 0)
            {
                return;
            }
            Bundle bundle = BundlePool.obtain();
            bundle.putInt(EventKey.INT_DATA, mSeekProgress);
            requestSeek(bundle);
        }
    };

    private void cancelBottomAnimation()
    {
        if (mBottomAnimator != null)
        {
            mBottomAnimator.cancel();
            mBottomAnimator.removeAllListeners();
            mBottomAnimator.removeAllUpdateListeners();
        }
    }

    private void setBottomContainerState(final boolean state)
    {
        mBottomContainer.clearAnimation();
        cancelBottomAnimation();
        mBottomAnimator = ObjectAnimator.ofFloat(mBottomContainer, "alpha", state ? 0 : 1, state ? 1 : 0).setDuration(300);
        mBottomAnimator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                super.onAnimationStart(animation);
                if (state)
                {
                    mBottomContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);
                if (!state)
                {
                    mBottomContainer.setVisibility(View.GONE);
                }
            }
        });
        mBottomAnimator.start();
        if (state)
        {
            PLog.d(getTag().toString(), "requestNotifyTimer...");
            requestNotifyTimer();
        }
        else
        {
            PLog.d(getTag().toString(), "requestStopTimer...");
            requestStopTimer();
        }
    }

    private void setControllerState(boolean state)
    {
        if (state)
        {
            sendDelayHiddenMessage();
        }
        else
        {
            removeDelayHiddenMessage();
        }
        setBottomContainerState(state);
    }

    private boolean isControllerShow()
    {
        return mBottomContainer.getVisibility() == View.VISIBLE;
    }

    private void toggleController()
    {
        if (isControllerShow())
        {
            setControllerState(false);
        }
        else
        {
            setControllerState(true);
        }
    }

    private void sendDelayHiddenMessage()
    {
        removeDelayHiddenMessage();
        mHandler.sendEmptyMessageDelayed(MSG_CODE_DELAY_HIDDEN_CONTROLLER, 5000);
    }

    private void removeDelayHiddenMessage()
    {
        mHandler.removeMessages(MSG_CODE_DELAY_HIDDEN_CONTROLLER);
    }

    private void setCurrTime(int curr)
    {
        mCurrTime.setText(TimeUtil.getTime(mTimeFormat, curr));
    }

    private void setTotalTime(int duration)
    {
        mTotalTime.setText(TimeUtil.getTime(mTimeFormat, duration));
    }

    private void setSeekProgress(int curr, int duration)
    {
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(curr);
        float secondProgress = mBufferPercentage * 1.0f / 100 * duration;
        setSecondProgress((int) secondProgress);
    }

    private void setSecondProgress(int secondProgress)
    {
        mSeekBar.setSecondaryProgress(secondProgress);
    }

    @Override
    public void onTimerUpdate(int curr, int duration, int bufferPercentage)
    {
        if (!mTimerUpdateProgressEnable)
        {
            return;
        }
        if (mTimeFormat == null)
        {
            mTimeFormat = TimeUtil.getFormat(duration);
        }
        mBufferPercentage = bufferPercentage;
        updateUI(curr, duration);
    }

    private void updateUI(int curr, int duration)
    {
        setSeekProgress(curr, duration);
        setCurrTime(curr);
        setTotalTime(duration);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle)
    {
        switch (eventCode)
        {
            case OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET:
                mBufferPercentage = 0;
                mTimeFormat = null;
                updateUI(0, 0);
                break;
            case OnPlayerEventListener.PLAYER_EVENT_ON_STATUS_CHANGE:
                int status = bundle.getInt(EventKey.INT_DATA);
                if (status == IPlayer.STATE_PAUSED)
                {
                    mStateIcon.setSelected(true);
                }
                else if (status == IPlayer.STATE_STARTED)
                {
                    mStateIcon.setSelected(false);
                }
                break;
            case OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START:
            case OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE:
                mTimerUpdateProgressEnable = true;
                break;
        }
    }

    @Override
    public void onErrorEvent(int eventCode, Bundle bundle)
    {

    }

    @Override
    public void onReceiverEvent(int eventCode, Bundle bundle)
    {

    }

    @Override
    public Bundle onPrivateEvent(int eventCode, Bundle bundle)
    {
        switch (eventCode)
        {
            case -201:
                if (bundle != null)
                {
                    int curr = bundle.getInt(EventKey.INT_ARG1);
                    int duration = bundle.getInt(EventKey.INT_ARG2);
                    updateUI(curr, duration);
                }
                break;
        }
        return null;
    }

    @Override
    public void onSingleTapUp(MotionEvent event)
    {
        if (!mGestureEnable)
        {
            return;
        }
        //toggleController();
    }

    @Override
    public void onDoubleTap(MotionEvent event)
    {
    }

    @Override
    public void onDown(MotionEvent event)
    {
    }

    @Override
    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        if (!mGestureEnable)
        {
            return;
        }
    }

    @Override
    public void onEndGesture()
    {
    }
}
