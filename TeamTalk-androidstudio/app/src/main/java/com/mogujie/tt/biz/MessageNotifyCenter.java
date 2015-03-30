
package com.mogujie.tt.biz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.Message;

import com.mogujie.tt.config.SysConstant;

/**
 * @Description 消息通知中心
 * @date 2014-7-12
 */
public class MessageNotifyCenter {
    private static List<Integer> eventList = Collections
            .synchronizedList(new ArrayList<Integer>());// 消息事件
    private static Map<Integer, Map<Integer, Handler>> hmEventHandlers = new ConcurrentHashMap<Integer, Map<Integer, Handler>>(); // 消息通知
    private static MessageNotifyCenter instance = null;

    public static MessageNotifyCenter getInstance() {
        if (null == instance) {
            instance = new MessageNotifyCenter();
        }
        return instance;
    }

    private MessageNotifyCenter() {
        eventList.add(SysConstant.EVENT_UNREAD_MSG);
        eventList.add(SysConstant.EVENT_RECENT_INFO_CHANGED);
    }

    /*
     * 清空所有注册的事件通知
     */
    public void clear() {
        hmEventHandlers.clear();
    }

    /*
     * 通知
     */
    public void doNotify(int event) {
        if (0 == eventList.size()) {
            return;
        }
        Map<Integer, Handler> eventHandlerList = hmEventHandlers.get(event);
        if (null == eventHandlerList) {
            return;
        }

        for (Entry<Integer, Handler> entry : eventHandlerList.entrySet())
        {
            doNotify(entry);
        }
    }

    private void doNotify(Entry<Integer, Handler> entry) {
        if (null == entry) {
            return;
        }
        Handler handler = entry.getValue();
        Integer msg = entry.getKey();
        Message message = handler.obtainMessage();
        message.what = msg;
        handler.sendMessage(message);
    }

    /*
     * 注册一个事件消息通知
     */
    public Boolean register(Integer event, Handler handler, Integer message) {
        if (!eventList.contains(event)) {
            return false; // 不支持的消息通知事件
        }
        Map<Integer, Handler> eventHandlerList = null;
        if (!hmEventHandlers.containsKey(event)) {
            // 新注册事件，不存在则新加一个
            eventHandlerList = new IdentityHashMap<Integer, Handler>();
            eventHandlerList.put(message, handler);
            hmEventHandlers.put(event, eventHandlerList);
            return true;
        }
        // 已注册事件
        eventHandlerList = hmEventHandlers.get(event);
        if (!eventHandlerList.containsKey(message)) {
            // 已注册事件，但没有注册该消息通知
            eventHandlerList.put(message, handler);
            hmEventHandlers.put(event, eventHandlerList);
        }

        return true;
    }

    /*
     * 反注册一个事件消息通知
     */
    public Boolean unregister(Integer event, Handler handler, int message) {
        if (!eventList.contains(event) || null == handler) {
            return false; // 不支持的消息通知事件
        }
        Map<Integer, Handler> eventHandlerList;
        if (!hmEventHandlers.containsKey(event)) {
            return true; // 未注册事件
        }
        // 已注册事件
        eventHandlerList = hmEventHandlers.get(event);
        if (!eventHandlerList.containsKey(message)) {

            return true; // 事件已注册，但没有注册该事件消息通知
        }
        eventHandlerList.remove(message);
        hmEventHandlers.put(event, eventHandlerList);
        return true;
    }

}
