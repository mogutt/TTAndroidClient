
package com.mogujie.tt.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mogujie.tt.entity.User;

/**
 * @author seishuchen
 */
public class UserCacheImpl implements Cache {

    private static User loginUser = null;
    private static User chatUser = null;
    // private static User lastChatUser = null;
    private static Map<String, User> hmCache = new ConcurrentHashMap<String, User>(); // 用户信息
    private static UserCacheImpl instance = null;

    public static UserCacheImpl getInstance() {
        if (null == instance) {
            instance = new UserCacheImpl();
        }
        return instance;
    }

    private UserCacheImpl() {

    }

    /*
     * 清空用户缓存信息
     */
    public void clear() {
        hmCache.clear();
        loginUser = null;
        chatUser = null;
        instance = null;
        // lastChatUser = null;
    }

    @Override
    public boolean set(String key, Object value) {
        if (null == key) {
            return false;
        }
        if (null != value) {
            hmCache.put(key, (User) value);
        } else {
            hmCache.remove(key);
        }
        return true;
    }

    @Override
    public Object get(String key) {
        if (null == key) {
            return null;
        }
        if (hmCache.containsKey(key)) {
            return hmCache.get(key);
        }
        return null;
    }

    // /**
    // * @return the lastChatUser
    // */
    // public User getLastChatUser() {
    // return lastChatUser;
    // }

    /**
     * @param lastChatUser the lastChatUser to set
     */
    // public void setLastChatUser(User lastChatUser) {
    // UserCacheImpl.lastChatUser = lastChatUser;
    // }

    // public void clearLastChatUser() {
    // UserCacheImpl.lastChatUser = null;
    // }

    /**
     * @return the chatUser
     */
    public User getChatUser() {
        return chatUser;
    }

    /**
     * 获得当前聊天对象用户ID
     */
    public String getChatUserId() {
        if (null == getChatUser()) {
            return null;
        }
        return getChatUser().getUserId();
    }

    /**
     * @param chatUser the chatUser to set
     */
    public boolean setChatUser(User chatUser) {
        if (null == chatUser) {
            throw new IllegalArgumentException("chatUser is null");
        }
        UserCacheImpl.chatUser = chatUser;
        return true;
    }

    /**
     * 清空当前聊天对象
     * 
     * @param user 用户信息
     */
    public void clearChatUser() {
        if (null != chatUser) {
            chatUser = null;
        }
        return;
    }

    // /**
    // * 清空当前聊天对象
    // *
    // * @param user 用户信息
    // */
    // public void clearChatUser() {
    // // 每次清空聊天对象时，保存前一次聊天对象
    // if (null != chatUser) {
    // //setLastChatUser(chatUser);
    // UserCacheImpl.chatUser = null;
    // }
    // return;
    // }

    /**
     * @return the loginUser
     */
    public User getLoginUser() {
        return loginUser;
    }

    /**
     * 获得当前登入用户Id
     */
    public String getLoginUserId() {
        if (null == getLoginUser()) {
            return null;
        }
        return getLoginUser().getUserId();
    }

    /**
     * @param loginUser the loginUser to set
     */
    public void setLoginUser(User loginUser) {
        if (null == loginUser) {
            throw new IllegalArgumentException("loginUser is null");
        }
        UserCacheImpl.loginUser = loginUser;
    }

    /**
     * 设置当前登入用户
     * 
     * @param user 用户信息
     */
    public void setLoginUserId(String userId) {
        if (null == userId) {
            throw new IllegalArgumentException("login userId is null");
        }
        User loginUser = new User();
        loginUser.setUserId(userId);
        setLoginUser(loginUser);

        return;
    }

}
