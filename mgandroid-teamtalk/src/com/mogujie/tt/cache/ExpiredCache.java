
package com.mogujie.tt.cache;

/**
 * @author seishuchen
 */
public interface ExpiredCache {
    boolean set(String key, Object value, int expired);

    Object get(String key);
}
