
package com.mogujie.tt.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.entity.MessageInfo;

/**
 * @author seishuchen
 */
public class MessageCacheImpl implements Cache {

    // private static Object locked = new Object(); // 同步锁对象
    private static int msgId = 0; // 前一条消息ID 不可被清空
    private static volatile int unreadCount = 0; // 所有未读消息计数总和
    private static Map<String, Integer> hmCounts = new ConcurrentHashMap<String, Integer>(); // 每个用户未读消息计数
    private static Map<String, MessageInfo> hmLastMessages = new ConcurrentHashMap<String, MessageInfo>(); // 当前用户与好友的最后一条信息
    private static MessageCacheImpl instance;

    public static MessageCacheImpl getInstance() {
        if (null == instance) {
            instance = new MessageCacheImpl();
        }
        return instance;
    }

    public MessageCacheImpl() {

    }

    /*
     * 清空缓存信息
     */
    public synchronized void clear() {
        unreadCount = 0;
        hmCounts.clear();
        hmLastMessages.clear();
        instance = null;
    }

    /*
     * 获取消息ID
     */
    public synchronized int obtainMsgId() {
        return ++msgId;
    }

    // /*
    // * 获得唯一的自增的消息ID,确保在使用前已经初始化preMsgId
    // */
    // public synchronized int obtainMsgId() {
    // return msgId++;
    // }

    /*
     * 初始化消息ID，确保再调用该对象前先初始化该对象
     * @param preMsgId 前一条消息ID
     */
    public synchronized static void initMsgId(int preMsgId) {
        msgId = ++preMsgId;
        return;
    }

    @Override
    public boolean set(String key, Object value) {
        if (null == key) {
            return false;
        }
        synchronized (hmLastMessages) {
            if (null == value) {
                hmLastMessages.remove(key);
            } else {
                hmLastMessages.put(key, (MessageInfo) value);
            }
        }
        return true;
    }

    @Override
    public Object get(String key) {
        if (null == key) {
            return null;
        }
        synchronized (hmLastMessages) {
            if (hmLastMessages.containsKey(key)) {
                return hmLastMessages.get(key);
            }
        }
        return null;
    }

    /**
     * 获得所有未读消息计数
     * 
     * @return the unreadCount
     */
    public static int getUnreadCount() {
        return unreadCount;
    }

    /**
     * 增加未读消息计数总和
     * 
     * @param unreadCount the unreadCount to set
     */
    private synchronized void incUnreadCount(int unreadCount) {
        MessageCacheImpl.unreadCount += unreadCount;
    }

    /**
     * 减少未读消息计数总和
     * 
     * @param unreadCount the unreadCount to set
     */
    private synchronized void decUnreadCount(int unreadCount) {
        MessageCacheImpl.unreadCount -= unreadCount;
    }

    public synchronized void incUnreadCount(String key, int value) {
        if (null == key) {
            return;
        }
        int currCount = value;
        if (hmCounts.containsKey(key)) {
            currCount += hmCounts.get(key);
            setUnreadCount(key, currCount);
        } else {
            currCount += CacheHub.getInstance().getUnreadCount(key);
            setUnreadCount(key, currCount);
        }

        incUnreadCount(value); // 增加未读消息计数总数
    }

    /*
     * 清空某个用户未读消息计数
     * @param key 用户ID
     */
    public int clearUnreadCount(String key) {
        if (null == key) {
            return 0;
        }
        int readCount = 0;
        if (hmCounts.containsKey(key)) {
            readCount = hmCounts.get(key);
            decUnreadCount(readCount); // 减少未读消息计数总数
        }
        setUnreadCount(key, 0);

        return readCount;
    }

    /*
     * 设置某个联系人的未读消息计数
     */
    private boolean setUnreadCount(String key, int value) {
        if (null == key) {
            return false;
        }
        synchronized (hmCounts) {
            if (!TextUtils.isEmpty(key)) {
                hmCounts.put(key, value);
            }
        }
        return true;
    }

    /*
     * 获得某个联系人的未读消息计数
     */
    public int getUnreadCount(String key) {
        synchronized (hmCounts) {
            if (!TextUtils.isEmpty(key)) {
                if (hmCounts.containsKey(key)) {
                    return hmCounts.get(key);
                }
            }
        }
        return 0;
    }

}
