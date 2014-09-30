
package com.mogujie.tt.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

/**
 * @author seishuchen
 */
public class ContactCacheImpl implements Cache {
    private static Object lockObj = new Object();
    private static volatile List<String> friendIdList = Collections
            .synchronizedList(new ArrayList<String>());
    private static Map<String, Object> hmCache = new ConcurrentHashMap<String, Object>();
    private static Map<String, Boolean> hmLoadedIdsCache =
            new ConcurrentHashMap<String, Boolean>();// 判断某个好友ID是否已经在最近联系人列表中
    private static ContactCacheImpl instance = null;

    public static ContactCacheImpl getInstance() {
        if (null == instance) {
            instance = new ContactCacheImpl();
        }
        return instance;
    }

    public ContactCacheImpl() {
    }

    /*
     * 清空缓存信息
     */
    public synchronized void clear() {
        friendIdList.clear();
        hmCache.clear();
        hmLoadedIdsCache.clear();
        instance = null;
    }

    public boolean addFriendId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return false;
        }
        return set(userId, userId);
    }
    
    public void addFriendList(Queue<String>list){
        while (null != list.peek()) {
            String id = list.poll();
            addFriendId(id);
        }
    }

    @Override
    public boolean set(String key, Object value) {
        if (null == key)
        {
            return false;
        }
        synchronized (lockObj) {
            if (hmCache.containsKey(key)) {
                return true;
            }
            if (null == value) {
                hmCache.remove(key);
            } else {
                friendIdList.add(key);
                hmCache.put(key, value);
            }
        }
        return true;
    }

    @Override
    public Object get(String key) {
        if (null == key)
        {
            return null;
        }
        Object obj = null;
        synchronized (hmCache) {
            if (hmCache.containsKey(key)) {
                return hmCache.get(key);
            }
        }
        return obj;
    }

    public boolean setLoaded(String key) {
        if (null == key)
        {
            return false;
        }
        synchronized (hmLoadedIdsCache) {
            hmLoadedIdsCache.put(key, true);
        }
        return true;
    }

    public boolean isLoaded(String key) {
        if (null == key)
        {
            return false;
        }
        synchronized (hmLoadedIdsCache) {
            if (hmLoadedIdsCache.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the friendIdList
     */
    public static List<String> getFriendIdList() {
        return friendIdList;
    }

    public static void setFriendIdList(List<String> list) {
        friendIdList = list;
    }
}
