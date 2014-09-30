
package com.mogujie.tt.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author seishuchen
 */
public class IMCacheImpl implements Cache {

    private String version = "0.1"; // IM版本信息
    private static Boolean isFristLogin = true; // 判断是否初次登入或退出后登入，不可被清空

    private static Map<String, Object> hmCache = new ConcurrentHashMap<String, Object>(); // 用户信息
    private static IMCacheImpl instance = null;

    public static IMCacheImpl getInstance() {
        if (null == instance) {
            instance = new IMCacheImpl();
        }
        return instance;
    }

    /**
     * 
     */
    private IMCacheImpl() {
        init();
    }

    private void init() {
        return;
    }

    /*
     * 清空缓存信息
     */
    public synchronized void clear() {
        isFristLogin = true;
        instance = null;
        hmCache.clear();
    }

    @Override
    public boolean set(String key, Object value) {
        if (null == key) {
            return false;
        }
        synchronized (hmCache) {
            if (null == value) {
                hmCache.remove(key);
            } else {
                hmCache.put(key, value);
            }
        }
        return true;
    }

    @Override
    public Object get(String key) {
        if (null == key) {
            return null;
        }
        synchronized (hmCache) {
            if (hmCache.containsKey(key)) {
                return hmCache.get(key);
            }
        }
        return null;
    }

    /**
     * @return the isFristLogin
     */
    public static Boolean getIsFristLogin() {
        return isFristLogin;
    }

    /**
     * @param isFristLogin the isFristLogin to set
     */
    public static void setIsFristLogin(Boolean isFristLogin) {
        IMCacheImpl.isFristLogin = isFristLogin;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
