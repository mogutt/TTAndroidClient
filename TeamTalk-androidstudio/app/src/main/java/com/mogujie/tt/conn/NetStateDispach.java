
package com.mogujie.tt.conn;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.Message;

public class NetStateDispach {
    private static NetStateDispach pInstance = null;
    private Map<String, Handler> hmHandler = new ConcurrentHashMap<String, Handler>();

    public static NetStateDispach getInstance() {
        if (null == pInstance)
        {
            pInstance = new NetStateDispach();
        }
        return pInstance;
    }

    public void register(Class<?> key, Handler handler)
    {
        hmHandler.put(key.getName(), handler);
    }

    public void unregister(Class<?> key) {
        hmHandler.remove(key.getName());
    }

    @SuppressWarnings("rawtypes")
    public void dispachMsg(int eventId) {
        Message message = null;
        Iterator iter = hmHandler.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Handler handler = (Handler) entry.getValue();
            message = handler.obtainMessage();
            message.what = eventId;
            message.obj = null;
            handler.sendMessage(message);
        }
    }
}
