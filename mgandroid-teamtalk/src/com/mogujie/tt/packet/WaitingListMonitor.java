
package com.mogujie.tt.packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.action.Action;
import com.mogujie.tt.packet.action.ActionCallback;

/**
 * Waiting list扫描线程, 一般情况下，这个线程只应该开启一条
 * 
 * @author dolphinWang
 * @time 2014/05/03
 */
public class WaitingListMonitor {

    private Logger logger = Logger.getLogger(WaitingListMonitor.class);
    public static final int DEFAULT_WAITING_LIST_MONTOR_INTERVAL = 10000;
    private static final int DO_WITH_TIMEOUT = 0x01;

    private Monitor mMonitor;

    private static MonitorHandler mHandler;

    private volatile boolean mStoped = false;
    private volatile boolean mStarted = false;

    public WaitingListMonitor(int interval) {
        if (interval <= 0) {
            logger.w("Set interval time of monitor less than 0!");

            mMonitor = new Monitor("IM-waiting-list-monitor", Process.THREAD_PRIORITY_BACKGROUND,
                    DEFAULT_WAITING_LIST_MONTOR_INTERVAL);
        } else {
            mMonitor = new Monitor("IM-waiting-list-monitor", Process.THREAD_PRIORITY_BACKGROUND,
                    interval);
        }

        mHandler = new MonitorHandler(Looper.getMainLooper());
    }

    public synchronized void start() {
        if (mStarted)
            return;
        mStoped = false;
        mMonitor.start();
        logger.d("start WaitingListMonitor!");
        mStarted = true;
    }

    public synchronized void stop() {
        if (mStoped) {
            return;
        }

        mStoped = true;
        mStarted = false;
        mHandler.removeMessages(DO_WITH_TIMEOUT);
    }

    private class Monitor extends Thread {

        private int mInterval;

        public Monitor(String name, int priority, int interval) {
            setName(name);
            setPriority(priority);

            mInterval = interval;
        }

        @Override
        public void run() {
            super.run();

            try {
                while (!mStoped) {

                    Map<Integer, Action> list = SocketMessageQueue
                            .getInstance().getWaitingList();

                    synchronized (list) {

                        final long currentTime = System.currentTimeMillis();
                        ArrayList<Action> timeoutList = null;

                        Iterator<Entry<Integer, Action>> iterator = list.entrySet().iterator();

                        while (iterator.hasNext()) {
                            Entry<Integer, Action> entry = iterator.next();
                            Action action = entry.getValue();
                            final int key = entry.getKey();

                            final long timeStamp = action.getTimeStamp();
                            final int timeout = action.getTimeout();
                            // int sid = action.getPacket().getRequest()
                            // .getHeader().getServiceId();
                            // int cid = action.getPacket().getRequest()
                            // .getHeader().getCommandId();
                            if (timeStamp + timeout < currentTime) {
                                list.remove(key);
                                // 重放入队列
                                if (action.minusRepeatCountIfFaild() >= 0) {
                                    SocketMessageQueue.getInstance()
                                            .submitAndEnqueue(action);
                                } else {
                                    // 被判定为超时
                                    if (null == timeoutList) {
                                        timeoutList = new ArrayList<Action>();
                                        timeoutList.add(action);
                                    }
                                }
                            }
                        }

                        if (timeoutList != null) {
                            Message msg = mHandler.obtainMessage(
                                    DO_WITH_TIMEOUT, timeoutList);
                            mHandler.sendMessage(msg);
                        }
                    }

                    sleep(mInterval);

                }
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }
    }

    private class MonitorHandler extends Handler {

        public MonitorHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DO_WITH_TIMEOUT) {
                ArrayList<Action> timeoutList = (ArrayList<Action>) msg.obj;
                for (Action action : timeoutList) {
                    if (null == action) {
                        continue;
                    }
                    final ActionCallback callback = action.getCallback();
                    if (null != callback) {
                        callback.onTimeout(action.getPacket());
                    }
                }

            }
            super.handleMessage(msg);
        }
    }
}
