
package com.mogujie.tt.packet;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.action.Action;
import com.mogujie.tt.packet.base.Packet;

/**
 * 消息队列，要通过socket发送的请求都必须提交到这个队列里面来
 * 
 * @author dolphinWang
 * @time 2014/4/30
 */
public class SocketMessageQueue {

    @SuppressWarnings("unused")
    private volatile static boolean mIsOffline;
    private Logger logger = Logger.getLogger(SocketMessageQueue.class);

    /*
     * 等待队列，一个Action在被发送出去之后，会进入这个队列，等待对方返回
     */
    private static final ConcurrentHashMap<Integer, Action> mWaitingList = new ConcurrentHashMap<Integer, Action>();

    /*
     * 任务队列，所有需要socket发送的任务都应该被提交到这个队列来
     */
    private static final Queue<Action> mActionQueue = new LinkedList<Action>();

    private static final int[] mMessages = new int[] {
            MessageDispatchCenter.MESSAGE_OFFLINE,
            MessageDispatchCenter.MESSAGE_ONLINE
    };

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageDispatchCenter.MESSAGE_OFFLINE:
                    mIsOffline = true;
                    break;

                case MessageDispatchCenter.MESSAGE_ONLINE:
                    mIsOffline = false;
                    break;
            }

            super.handleMessage(msg);
        }

    };

    private SocketMessageQueue() {
        MessageDispatchCenter.getInstance().register(mHandler, mMessages);
    }

    private static class SingletonHolder {
        static SocketMessageQueue queue = new SocketMessageQueue();
    }

    public static SocketMessageQueue getInstance() {
        return SingletonHolder.queue;
    }

    public void clear() {
        synchronized (mActionQueue) {
            mActionQueue.clear();
        }

        synchronized (mWaitingList) {
            mWaitingList.clear();
        }
    }

    public void clearActionQueue() {

        synchronized (mActionQueue) {
            try {
                while (mActionQueue.size() > 0) {
                    Action action = mActionQueue.poll();
                    if (null == action) {
                        continue;
                    }

                    Packet packet = action.getPacket();
                    if (null == packet) {
                        continue;
                    }

                    if (packet.getNeedMonitor()) {
                        add2WaitingList(action);
                    }
                }
            } catch (Exception e) {
                logger.e(e.getMessage());
            } finally {
                mActionQueue.clear();
            }
        }
    }

    /**
     * 把任务提交给任务队列
     * 
     * @param action
     */
    public void submitAndEnqueue(Action action) {
        if (null == action)
            return;

        synchronized (mActionQueue) {
            // 如果提交任务的时候正好的网络断开的时候，直接将任务抛弃
            // FIXME: 可不可以不直接抛弃，也许入队之后socket连上了，就可以被发出去了
            // FIXED： 任务队列会有缓冲
            /*
             * if (mIsOffline) { ActionCallback callback = action.getCallback();
             * if (callback != null) { callback.onFaild(); } else {
             * Log.w(getClass().getName(), "Wow this action have no callback!");
             * } return; }
             */
            // 检查一下是否存在
            if (mActionQueue.contains(action)) {
                // 把原来的任务移除掉
                mActionQueue.remove(action);
            }

            // 分配一个sequence number
            short seqNo = action.getPacket().getRequest().getHeader()
                    .getReserved();
            action.setSquenceNo(seqNo);
            mActionQueue.offer(action);
        }
    }

    /**
     * 从队列中获得一个任务，并不移除
     */
    public Action getFront() {
        synchronized (mActionQueue) {
            if (mActionQueue.size() > 0) {
                return mActionQueue.peek();
            } else {
                return null;
            }
        }
    }

    /**
     * 从任务队列取出一个任务
     * 
     * @return
     */
    public Action pull() {
        synchronized (mActionQueue) {
            if (mActionQueue.size() > 0) {
                return mActionQueue.poll();
            } else {
                return null;
            }
        }
    }

    /**
     * 添加一个等待回复的任务到等待队列。
     * 
     * @param action
     */
    public void add2WaitingList(Action action) {
        if (null == action)
            return;

        if (checkIsInMainThread()) {
            throw new RuntimeException(
                    "Call from wrong thread! The main thread should not operate waiting list!");
        }

        synchronized (mWaitingList) {
            // 国际惯例，检测是否任务存在
            final int squenceNo = action.getSequenceNo();
            if (mWaitingList.containsKey(squenceNo)) {
                // 存在的remove掉，再重新入列
                mWaitingList.remove(squenceNo);
            }
            mWaitingList.put(action.getSequenceNo(), action);
        }
    }

    /**
     * Get and remove an action from waiting list by sequence number.
     * 
     * @param sequenceNo
     * @return
     */
    public Action getFromWaitingList(int sequenceNo) {
        if (sequenceNo < 0) {
            return null;
        }

        if (checkIsInMainThread()) {
            throw new RuntimeException(
                    "Call from wrong thread! The main thread should not operate waiting list!");
        }

        Action actionRemoved = null;
        synchronized (mWaitingList) {
            boolean bContains = mWaitingList.containsKey(sequenceNo);
            if (bContains) {
                actionRemoved = mWaitingList.get(sequenceNo);
                mWaitingList.remove(sequenceNo);
            }
        }

        return actionRemoved;
    }

    public Map<Integer, Action> getWaitingList() {
        if (checkIsInMainThread()) {
            throw new RuntimeException(
                    "Call from wrong thread! The main thread should not operate waiting list!");
        }

        synchronized (mWaitingList) {
            return mWaitingList;
        }
    }

    /**
     * 检测是否在主线程上面
     * 
     * @return
     */
    private boolean checkIsInMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public synchronized void stop() {
        mHandler.removeCallbacksAndMessages(null);
        MessageDispatchCenter.getInstance().unRegister(mHandler, mMessages);
    }
}
