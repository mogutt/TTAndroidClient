
package com.mogujie.tt.db.biz;

import java.util.List;

import android.database.SQLException;
import android.text.TextUtils;

import com.mogujie.tt.db.DataModel;
import com.mogujie.tt.entity.IMRecentContact;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.User;

public class DataBaseHelper {

    private DataModel dataModel = null;

    private static DataBaseHelper instance;

    public static DataBaseHelper getInstance() {
        if (null == instance) {
            instance = new DataBaseHelper();
        }
        return instance;
    }

    /**
     * @author shuchen
     */
    private DataBaseHelper() {
        dataModel = DataModel.getInstance();
    }

    /**
     * 断开数据库
     * 
     * @return void
     */
    public void stopDB() {
        // 断开本地数据库
        try {
            dataModel.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int queryLastMsgId() {
        return dataModel.queryLastMsgId();
    }

    /**
     * 更新数据库中用户，有则忽略，无则添加
     * 
     * @param user
     * @return Boolean
     */
    public Boolean updateUser(User user) {
        return dataModel.update(user, false);
    }

    /**
     * 更新数据库中用户，有则忽略，无则添加
     * 
     * @param user
     * @return Boolean
     */
    public Boolean updateUserForced(User user) {
        return dataModel.update(user, true);
    }

    /**
     * 更新数据库中用户联系人列表，有则忽略，无则添加
     * 
     * @param userId 用户或好友用户ID
     * @param friendUserId 用户或好友用户ID
     * @return relateId
     */
    public int updateRecentContact(String ownerId, String userId, String friendUserId) {
        return dataModel.getRelateId(ownerId, userId, friendUserId);
    }

    /**
     * 更新数据库中用户联系人列表，有则忽略，无则添加
     * 
     * @param onwerId 用户或好友用户ID
     * @param friendUserId 用户或好友用户ID
     * @return relateId
     */
    public int updateRecentContact(String onwerId, String friendUserId) {
        return dataModel.getRelateId(onwerId, onwerId, friendUserId);
    }

    /**
     * 更新数据库中用户，有则忽略，无则添加
     * 
     * @param user
     * @return User
     */
    public User queryUser(User user) {
        String userId = user.getUserId();
        return dataModel.queryUserByUserId(userId);
    }

    /**
     * 更新数据库中用户，有则忽略，无则添加
     * 
     * @param user
     * @return User
     */
    public User queryUser(String userId) {
        return dataModel.queryUserByUserId(userId);
    }

    /**
     * 从数据库中删除指定用户
     * 
     * @param user
     * @return void
     */
    public void deleteUser(User user) {
        dataModel.delete(user);
    }

    /**
     * 添加信息到数据库中
     * 
     * @param msgInfo
     * @return void
     */
    public int pushMsg(String onwerId, MessageInfo msgInfo) {
        int relateId = dataModel.getRelateId(onwerId,
                msgInfo.getMsgFromUserId(), msgInfo.getTargetId());
        msgInfo.setRelateId(relateId);
        return dataModel.add(msgInfo);
    }

    /**
     * 从数据库中删除某条消息
     * 
     * @param msgInfo
     * @return void
     */
    public void deleteMsg(MessageInfo msgInfo) {
        dataModel.delete(msgInfo);
    }

    /**
     * 从数据库拉取某条信息
     * 
     * @param msgId 消息唯一ID
     * @return MessageInfo
     */
    public MessageInfo pullMsgById(int msgId) {
        return dataModel.queryMsgWithExtraByMsgId(msgId);
    }

    /**
     * 从数据库拉取信息两个用户之间最后一条信息
     * 
     * @param userId
     * @param friendUserId
     * @return MessageInfo
     */
    public MessageInfo pullMsg(String onwerId, String userId, String friendUserId) {
        int relateId = dataModel.getRelateId(onwerId, userId, friendUserId);
        return dataModel.queryLastMsgWithoutExtraByRelateId(onwerId, relateId);
    }

    /**
     * 从数据库获得两个用户之间所有未读信息计数
     * 
     * @param userId
     * @param friendUserId
     * @return unreadCount
     */
    public int getMsgUnreadCount(String onwerId, String userId, String friendUserId) {
        int relateId = dataModel.getRelateId(onwerId, userId, friendUserId);
        return dataModel.queryUnreadCountByRelateId(onwerId, relateId);
    }

    /**
     * 从数据库获得当前登入用户除某个用户外的所有未读信息计数
     * 
     * @param onwerId
     * @param exclUserId 不包含的用户ID
     * @return unreadCount
     */
    public int getMsgUnreadTotalCount(String onwerId, String exclUserId) {
        return dataModel.queryUnreadTotalCountExclUserId(onwerId, exclUserId);
    }

    /**
     * 从数据库获得当前登入用户所有未读信息计数
     * 
     * @param entityId
     * @param friendUserId
     * @return unreadCount
     */
    public int getMsgUnreadTotalCount(String onwerId) {
        return dataModel.queryUnreadTotalCountByUserId(onwerId);
    }

    /**
     * 在数据库设置某条信息状态(加载状态)
     * 
     * @param MsgId
     * @param status
     * @return Boolean
     */
    public Boolean updateMsgStatus(int MsgId, int status) {
        return dataModel.updateMsgStatusByMsgId(MsgId, status, 0);
    }

    /**
     * 在数据库设置某条信息状态(是否已读或展现)
     * 
     * @param MsgId
     * @param status
     * @return Boolean
     */
    public Boolean updateMsgReadStatus(int MsgId, int status) {
        return dataModel.updateMsgStatusByMsgId(MsgId, status, 1);
    }

    /**
     * 在数据库设置某条信息消息ID(修正图文混排时的消息ID)
     * 
     * @param MsgId
     * @param MsgParentId
     * @return Boolean
     */
    public Boolean updateMsgParentId(int MsgId, int MsgParentId) {
        // return messageDB.updateParentId(MsgId, msgParentId);
        return false; // 暂时弃用
    }

    /**
     * 更改图片存储路径
     * 
     * @param entityId
     * @param friendUserId
     * @return Boolean
     */
    public Boolean updateMsgImageSavePath(int MsgId, String newPath) {
        return dataModel.updateMsgImageSavePath(MsgId, newPath);
    }

    public Boolean updateImagePathUrlInfo(int msgId, String savePath,
            String url) {
        return dataModel.updateImagePathUrlInfo(msgId, savePath,
                url, 0);
    }

    /**
     * 在数据库设置当前登入用户的所有信息状态(加载状态)为oldStatus的设置为status
     * 
     * @param ownerId 当前登入用户
     * @param status 新的消息状态
     * @param oldStatus 旧的消息状态
     * @return Boolean
     */
    public Boolean updateMsgStatusFromStatus(String ownerId, int status, int oldStatus) {
        return dataModel.updateAllMsgStatusFromStatus(ownerId, status, oldStatus, 0);
    }

    /**
     * 在数据库设置当前登入用户的所有信息状态(加载状态)为oldStatus的设置为status
     * 
     * @param ownerId 当前登入用户
     * @param status 新的消息状态
     * @param oldStatus 旧的消息状态
     * @return Boolean
     */
    public Boolean updateMsgReadStatusFromStatus(String ownerId, int status, int oldStatus) {
        return dataModel.updateAllMsgStatusFromStatus(ownerId, status, oldStatus, 1);
    }

    /**
     * 在数据库设置当前登入用户收到当前聊天对象的所有信息状态(加载状态)
     * 
     * @param userId 当前登入用户
     * @param friendUserId 当前聊天对象
     * @return Boolean
     */
    public Boolean updateMsgStatus(String userId, String friendUserId, int status) {
        int relateId = dataModel.getRelateId(userId, userId, friendUserId);
        return dataModel.updateAllMsgStatus(userId, relateId, status, 0);
    }

    /**
     * 在数据库设置当前登入用户收到当前聊天对象的所有信息状态(是否已读或展现)
     * 
     * @param userId 当前登入用户
     * @param friendUserId 当前聊天对象
     * @return Boolean
     */
    public Boolean updateMsgReadStatus(String userId, String friendUserId,
            int status) {
        int relateId = dataModel.getRelateId(userId, userId, friendUserId);
        return dataModel.updateAllMsgStatus(userId, relateId, status, 1);
    }

    /**
     * 从数据库拉取信息两个用户之间部分信息（由偏移量和信息条数决定）
     * 
     * @param userId 用户ID
     * @param friendUserId 好友ID
     * @param msgId 起始ID
     * @param offset 距离起始ID的偏移量
     * @param size 拉取的消息条数
     * @return List<MessageInfo>
     */
    public List<MessageInfo> pullMsg(String userId, String friendUserId, int msgId,
            int offset,
            int size) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        int relateId = dataModel.getRelateId(userId, userId, friendUserId);
        return dataModel.queryHistoryMsg(userId, relateId, msgId, offset, size);
    }

    /**
     * 从数据库拉取用户的所有最近联系人列表
     * 
     * @return List<IMRecentContact>
     */
    public List<IMRecentContact> pullRecentContact() {
        return dataModel.queryAllContacts();
    }

    /**
     * 从数据库拉取用户的所有最近联系人的用户信息
     * 
     * @param userId
     * @param friendUserId
     * @return List<String>
     */
    public List<String> pullFriendUserIds(String userId) {
        return dataModel.queryAllFriendUserId(userId);
    }

    /*
     * @return 返回删除消息的时间分隔线
     */
    public int checkAndDeleteIfNeed() {
        return dataModel.checkAndDeleteIfNeed();
    }

}
