
package com.mogujie.tt.cache;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

import com.mogujie.tt.biz.MessageDistCenter;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.db.biz.DataBaseHelper;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.utils.FileUtil;

/**
 * @author seishuchen
 */
public class CacheModel {

    private static Map<String, Integer> hmPullLastMsgFromDB = new ConcurrentHashMap<String, Integer>(); // 记录用户是否从DB中拉取过最后一天消息
    private static DataBaseHelper dbHelper = null;
    private static Logger logger = Logger.getLogger(CacheModel.class);
    private static CacheModel instance = null;

    public static CacheModel getInstance() {
        if (null == instance) {
            instance = new CacheModel();
        }
        return instance;
    }

    private CacheModel() {
        dbHelper = DataBaseHelper.getInstance();
        initCahce();
    }

    /*
     * 初始化缓存
     */
    private void initCahce() {
        initMessageID();
        // 用户登录才可以初始化，否则直接退出
        User loginUser = getLoginUser();
        if (null == loginUser) {
            return;
        }

        initContactCache(loginUser.getUserId());
        List<String> friendIds = ContactCacheImpl.getFriendIdList();
        initMessageCache(friendIds, loginUser.getUserId());
        reviseMsgLoadingStatusInDB(loginUser.getUserId());

        IMCacheImpl.setIsFristLogin(false); // 设置不再需要初始化缓存
    }

    /*
     * 清空缓存
     */
    public void clear() {
        IMCacheImpl.getInstance().clear();
        ContactCacheImpl.getInstance().clear();
        MessageCacheImpl.getInstance().clear();
        UserCacheImpl.getInstance().clear();
        instance = null;
    }

    /*
     * 初始化消息缓存
     * @param onwerId 当前登录用户ID
     */
    private Boolean initMessageCache(List<String> friendIds, String onwerId) {
        Boolean success = false;
        if (TextUtils.isEmpty(onwerId)) {
            return success;
        }
        success = initLastMessageAndCount(friendIds, onwerId);

        return success;
    }

    /*
     * 从DB获得前一条消息ID（最大的消息ID）并初始化消息缓存中的消息ID
     */
    private Boolean initMessageID() {
        int preMsgId = dbHelper.queryLastMsgId();
        MessageCacheImpl.initMsgId(preMsgId); // 初始化消息ID
        return true;
    }

    /*
     * 初始化用户最后一天消息及未读消息计数（从DB获得未读消息计数）
     * @param userId 用户ID
     */
    private Boolean initLastMessageAndCount(List<String> friendIds,
            String ownerId) {
//        try {
//            if (null == friendIds || 0 == friendIds.size()) {
//                return true;
//            }
//            Iterator<String> itr = friendIds.iterator();
//            int unreadCount = 0;
//            MessageInfo msgInfo = null;
//            while (itr.hasNext()) {
//                String friendUserId = itr.next();
//                unreadCount = dbHelper.getMsgUnreadCount(ownerId, ownerId,
//                        friendUserId);
//                MessageCacheImpl.getInstance().incUnreadCount(friendUserId,
//                        unreadCount);
//
//                msgInfo = dbHelper.pullMsg(ownerId, ownerId, friendUserId);
//                if (null != msgInfo
//                        && SysConstant.DISPLAY_TYPE_TEXT == msgInfo
//                                .getDisplayType()) {
//                    msgInfo = dbHelper.pullMsgById(msgInfo.getMsgId());
//                    MessageCacheImpl.getInstance().set(friendUserId, msgInfo);
//                } else {
//                    MessageCacheImpl.getInstance().set(friendUserId, msgInfo);
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            logger.e(e.getMessage());
//            return false;
//        }
    	return false;
    }

    /*
     * 获得用户未读消息计数从DB获得未读消息计数
     * @param userId 用户ID
     */
    private Boolean initContactCache(String onwerId) {
        Boolean success = false;
        success = initContactFriendIds(onwerId);
        return success;
    }

    /*
     * 从DB获得本地好友id列表
     * @param onwerId 当前登录用户ID
     */
    private Boolean initContactFriendIds(String onwerId) {
        if (!TextUtils.isEmpty(onwerId)) {
            List<String> friendUserIds = dbHelper.pullFriendUserIds(onwerId);
            Iterator<String> itr = friendUserIds.iterator();
            while (itr.hasNext()) {
                String friendUserId = itr.next();
                ContactCacheImpl.getInstance().addFriendId(friendUserId);
            }
            return true;
        }
        return false;
    }

    /*
     * 将DB中所有当前用户信息中加载状态为loading的修正为failed状态
     * @param userId 用户ID
     */
    private Boolean reviseMsgLoadingStatusInDB(String onwerId) {
        return dbHelper.updateMsgStatusFromStatus(onwerId,
                SysConstant.MESSAGE_STATE_FINISH_FAILED,
                SysConstant.MESSAGE_STATE_LOADDING);
    }

    /*
     * 设置用户信息
     * @param user 用户信息
     */
    public void setUser(User user) {
        if (null != user && null != user.getUserId()) {
            UserCacheImpl.getInstance().set(user.getUserId(), user);
        }
    }

    /*
     * 获得用户信息
     * @param userId 用户ID
     */
    public User getUser(String userId) {
        return (User) UserCacheImpl.getInstance().get(userId);
    }

    /*
     * 设置当前登入用户
     * @param user 用户信息
     */
    public static void setLoginUser(User user) {
        UserCacheImpl.getInstance().setLoginUser(user);
    }

    /*
     * 获得当前登入用户
     */
    public static User getLoginUser() {
        return UserCacheImpl.getInstance().getLoginUser();
    }

    /*
     * 设置当前登入用户
     * @param user 用户信息
     */
    public static void setLoginUserId(String userId) {
        UserCacheImpl.getInstance().setLoginUserId(userId);
    }

    /*
     * 获得当前登入用户
     */
    public static String getLoginUserId() {
        return UserCacheImpl.getInstance().getLoginUserId();
    }

    /*
     * 设置当前聊天对象ID
     * @param userId 聊天对象用户ID
     */
    public void setChatUserId(String userId) {
        setChatUser(new User(userId));
    }

    /*
     * 获得当前聊天对象用户ID
     */
    public String getChatUserId() {
        return UserCacheImpl.getInstance().getChatUserId();
    }

    /*
     * 设置当前聊天对象
     * @param user 用户信息
     */
    public void setChatUser(User user) {
        UserCacheImpl.getInstance().setChatUser(user);
        if (null != user) {
            ContactCacheImpl.getInstance().addFriendId(user.getUserId());
        }
    }

    /*
     * 清空当前聊天对象
     * @param user 用户信息
     */
    public void clearChatUser() {
        UserCacheImpl.getInstance().clearChatUser();
    }

    /*
     * 获得当前聊天对象
     */
    public User getChatUser() {
        return UserCacheImpl.getInstance().getChatUser();
    }

    // /*
    // * 设置前一回聊天对象
    // */
    // public void setLastChatUser(User user) {
    // UserCacheImpl.getInstance().setLastChatUser(user);
    // }
    //
    // /*
    // * 清空前一回聊天对象
    // */
    // public void clearLastChatUser() {
    // UserCacheImpl.getInstance().clearLastChatUser();
    // }
    //
    // /*
    // * 获得前一回聊天对象
    // */
    // public User getLastChatUser() {
    // return UserCacheImpl.getInstance().getLastChatUser();
    // }

    /*
     * 获得与好友聊天的最后一条信息
     * @param userId 用户ID
     */
    public MessageInfo getLastMessage(String friendUserId) {
//        try {
//            MessageInfo msgInfo = (MessageInfo) MessageCacheImpl.getInstance().get(
//                    friendUserId);
//            if (null == msgInfo) {
//                if (toPullLasgMsgFromDB(friendUserId)) {
//                    msgInfo = dbHelper.pullMsg(getLoginUserId(), getLoginUserId(),
//                            friendUserId);
//                    if (null != msgInfo
//                            && SysConstant.DISPLAY_TYPE_TEXT == msgInfo
//                                    .getDisplayType()) {
//                        msgInfo = dbHelper.pullMsgById(msgInfo.getMsgId());
//                        MessageCacheImpl.getInstance().set(friendUserId, msgInfo);
//                    } else {
//                        MessageCacheImpl.getInstance().set(friendUserId, msgInfo);
//                    }
//                }
//                return msgInfo;
//            }
//            return msgInfo;
//        } catch (Exception e) {
//            logger.e(e.getMessage());
//            return null;
//        }
    	return null;
    }

    /*
     * 判断用户是否需要从DB拉取最后一条消息
     */
    private Boolean toPullLasgMsgFromDB(String userId) {
        if (null == userId) {
            return false;
        }
        int currCount = 1;
        if (hmPullLastMsgFromDB.containsKey(userId)) {
            synchronized (hmPullLastMsgFromDB) {
                currCount += hmPullLastMsgFromDB.get(userId);
                hmPullLastMsgFromDB.put(userId, currCount);
            }
        } else {
            synchronized (hmPullLastMsgFromDB) {
                hmPullLastMsgFromDB.put(userId, currCount);
            }
        }

        return hmPullLastMsgFromDB.get(userId) > 5 ? false : true;
    }

    /*
     * 获得唯一的自增的消息ID,确保在使用前已经初始化preMsgId
     */
    public int obtainMsgId() {
        return MessageCacheImpl.getInstance().obtainMsgId();
    }

    /*
     * 推送一条消息到消息队列
     */
    public boolean push(MessageInfo msgInfo) {
        return MessageDistCenter.getInstance().push(msgInfo);
    }

    /*
     * 推送一条消息到DB
     */
    public int pushMsg(MessageInfo msgInfo) {
        int msgId = SysConstant.DEFAULT_MESSAGE_ID;
        if (null == msgInfo) {
            return msgId;
        }

        return dbHelper.pushMsg(getLoginUserId(), msgInfo);
    }

    /**
     * 从数据库拉取信息两个用户之间部分信息（由偏移量和信息条数决定）
     * 
     * @param userId 用户ID
     * @param friendUserId 好友ID
     * @param msgId 起始ID
     * @param offset 距离起始ID的偏移量
     * @param size 拉取的消息条数
     * @return MessageInfo
     */
    public List<MessageInfo> pullMsg(String userId, String friendUserId,
            int msgId, int offset, int size) {
        return dbHelper.pullMsg(userId, friendUserId, msgId, offset, size);
    }

    public List<String> getFriendIdList() {
        return ContactCacheImpl.getFriendIdList();
    }

    public void setFriendIdList(List<String> list) {
        ContactCacheImpl.setFriendIdList(list);
    }

    public boolean setLoadedFriendId(String friendId) {
        return ContactCacheImpl.getInstance().setLoaded(friendId);
    }

    public boolean isLoadedFriendId(String friendId) {
        return ContactCacheImpl.getInstance().isLoaded(friendId);
    }

    /**
     * 更改图片存储路径
     * 
     * @param entityId
     * @param friendUserId
     * @return Boolean
     */
    public Boolean updateMsgImageSavePath(String MsgId, String newPath) {
        //return dbHelper.updateMsgImageSavePath(MsgId, newPath);
    	return false;
    }

    public Boolean updateMsgStatus(int msgId, int state) {
        return dbHelper.updateMsgStatus(msgId, state);
    }

    public Boolean updateMsgReadStatus(int msgId, int state) {
        // 更新DB中消息是否已读状态,单例模式下ojDB不为空
        return dbHelper.updateMsgReadStatus(msgId, state);
    }

    public Boolean updateMsgParentId(int msgId, int msgParentId) {
        // 更新DB中消息消息ID，主要针对图文混排消息,单例模式下ojDB不为空
        return dbHelper.updateMsgParentId(msgId, msgParentId);
    }

    public Boolean updateMsgStatus(String userId, String friendUserId, int state) {
        // 更新DB中两用户之间所有消息加载状态, 单例模式下ojDB不为空
        return dbHelper.updateMsgStatus(userId, friendUserId, state);
    }

    public Boolean updateMsgReadStatus(String userId, String friendUserId,
            int state) {
        // 更新DB中两用户之间所有消息状态（是否已读或展现）,一般为已读 单例模式下ojDB不为空
        return dbHelper.updateMsgReadStatus(userId, friendUserId, state);
    }

    /*
     * @return 返回删除消息的时间分隔线
     */
    public int checkAndDeleteIfNeed() {
        // 如果需要的话，删除最老的部分历史信息
        int timeLine = dbHelper.checkAndDeleteIfNeed();
        if (0 < timeLine) {
            File audioDirFile = new File(
                    CommonUtil.getSavePath(SysConstant.FILE_SAVE_TYPE_AUDIO));
            File imageDirFile = new File(
                    CommonUtil.getSavePath(SysConstant.FILE_SAVE_TYPE_IMAGE));
            FileUtil.deleteHistoryFiles(audioDirFile, timeLine);
            FileUtil.deleteHistoryFiles(imageDirFile, timeLine);
        }
        return timeLine;
    }
}
