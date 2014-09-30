
package com.mogujie.tt.timer;

import java.util.Timer;
import java.util.TimerTask;

public class TimerHelper {
    private ITimerProcessor mProcessor;

    private long mDelayMs;

    private Timer mTimer;

    private TimerTask mTimerTask;

    private boolean isStarted = false;

    /**
     * 构造函数
     * 
     * @param delayMs 延时
     * @param processor 定时处理器，由调用者定制实现
     */
    public TimerHelper(int delayMs, ITimerProcessor processor) {
        mProcessor = processor;
        mDelayMs = delayMs;
    }

    /**
     * 启动定时器
     */
    public void startTimer(boolean bFlag) {
        stopTimer();
        mTimer = new Timer(true);
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                if (null != mProcessor && isRunning()) {
                    mProcessor.process();
                }
            }

        };
        if (bFlag)
        {
            mTimer.schedule(mTimerTask, 0, mDelayMs);
        } else {
            mTimer.schedule(mTimerTask, mDelayMs);
        }
        isStarted = true;
    }

    /**
     * 停止定时器
     */
    public void stopTimer() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        if (null != mTimerTask) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        isStarted = false;
    }

    public boolean isRunning() {
        return isStarted;
    }
}
