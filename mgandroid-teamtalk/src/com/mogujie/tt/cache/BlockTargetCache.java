
package com.mogujie.tt.cache;

import java.util.HashMap;
import java.util.Map;

import com.mogujie.tt.config.SysConstant;

/**
 * @Description 黑名单缓存
 * @author Nana
 * @date 2014-7-16
 */
public class BlockTargetCache {
    private static Map<String, Long> targetMap = new HashMap<String, Long>();

    private static BlockTargetCache instance = null;

    public static BlockTargetCache getInstance() {
        if (null == instance) {
            instance = new BlockTargetCache();
        }
        return instance;
    }

    private BlockTargetCache() {
    }

    public void set(String key, Long value) {
        targetMap.put(key, value);
    }

    public Long get(String key) {
        if (targetMap.containsKey(key)) {
            return targetMap.get(key);
        } else {
            return 0L;
        }
    }

    public boolean needCheckBlock(String key) {
        long lastCheckTime = get(key);
        long curTime = System.currentTimeMillis();
        if ((curTime - lastCheckTime) < SysConstant.BLOCK_USER_CHECK_INTERVAL) {
            return false;
        } else {
            set(key, curTime);
            return true;
        }
    }
}
