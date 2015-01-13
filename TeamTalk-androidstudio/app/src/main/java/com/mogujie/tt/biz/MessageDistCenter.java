
package com.mogujie.tt.biz;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.mogujie.tt.cache.AutoCloseable;
import com.mogujie.tt.cache.CacheModel;
import com.mogujie.tt.cache.Dispatcher;
import com.mogujie.tt.cache.MessageCacheImpl;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.log.Logger;

/**
 * @Description 业务消息分发中心
 * @date 2014-7-12
 */
public class MessageDistCenter implements Dispatcher, AutoCloseable, Runnable {
    private static Logger logger = Logger.getLogger(MessageDistCenter.class);
    private BlockingQueue<MessageInfo> msgQueue = null;

    private Thread mqThread = null;

    private volatile boolean run = true;
    private static MessageDistCenter instance;

    public static MessageDistCenter getInstance() {
        if (null == instance) {
            instance = new MessageDistCenter();
        }
        return instance;
    }

    public MessageDistCenter() {
        init();
    }

    public void init() {
        this.msgQueue = new ArrayBlockingQueue<MessageInfo>(SysConstant.MESSAGE_QUEUE_LIMIT);
        this.mqThread = new Thread(this, "Message Queue Thread");
        this.mqThread.setDaemon(true);
        this.mqThread.start();

    }

    @Override
    public void close() throws Exception {
        this.setRun(false);
    }

    @Override
    public void run() {
        while (this.run) {
            if (0 < msgQueue.size()) {
                MessageInfo messageInfo = msgQueue.poll();
                if (null != messageInfo) {
                    this.dispatch(messageInfo);
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.e(e.getMessage());
                }
            }
        }

    }

    /*
     * 把一条消息推入消息队列,把不合法的消息Kill在门外
     */
    public boolean push(MessageInfo msginfo) {
        if (null == msginfo) {
            logger.e("空消息");
            return false;
        }
//        if (SysConstant.DEFAULT_MESSAGE_ID == msginfo.msgId) {
//            logger.e("不合法的消息");
//            return false;
//        }
        if (msgQueue.size() < SysConstant.MESSAGE_QUEUE_LIMIT) {
            this.msgQueue.add(msginfo);
            return true;
        } else {
            logger.e("Queue full");
            return false;
        }
    }

    /**
     * @return the run
     */
    public boolean isRun() {
        return run;
    }

    /**
     * @param run the run to set
     */
    public void setRun(boolean run) {
        this.run = run;
    }

    public void setQueue(BlockingQueue<MessageInfo> queue) {
        this.msgQueue = queue;
    }

    @Override
    public void dispatch(MessageInfo messageInfo) {
        if (messageInfo.getIsSend()) {
            CacheModel.getInstance().pushMsg(messageInfo);
            MessageCacheImpl.getInstance().set(messageInfo.getTargetId(), messageInfo);
        } else if (!messageInfo.getIsSend()) {
            CacheModel.getInstance().pushMsg(messageInfo);
            MessageCacheImpl.getInstance().set(messageInfo.getMsgFromUserId(), messageInfo);
            MessageCacheImpl.getInstance().incUnreadCount(messageInfo.getMsgFromUserId(), 1);
            MessageNotifyCenter.getInstance().doNotify(SysConstant.EVENT_UNREAD_MSG);
        }
    }
}
