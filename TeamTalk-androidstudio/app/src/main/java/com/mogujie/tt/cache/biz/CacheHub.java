package com.mogujie.tt.cache.biz;

import java.util.List;
import java.util.Queue;

import android.content.Context;

import com.mogujie.tt.cache.CacheModel;
import com.mogujie.tt.cache.ContactCacheImpl;
import com.mogujie.tt.cache.MessageCacheImpl;
import com.mogujie.tt.db.ContactModel;
import com.mogujie.tt.db.UserModel;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.log.Logger;

public class CacheHub {

	private static CacheHub instance;

	public static CacheHub getInstance() {
		if (null == instance) {
			instance = new CacheHub();
		}
		return instance;
	}

	private SessionInfo sessionInfo;
	private Logger logger = Logger.getLogger(CacheHub.class);

	/**
	 * @author shuchen
	 */
	private CacheHub() {

	}

	/**
	 * 清除所有缓存
	 */
	public void clear() {
		CacheModel.getInstance().clear();
	}

	public void setSessionInfo(IMUIHelper.SessionInfo sessionIfo) {
		this.sessionInfo = sessionIfo;

		logger.d("chat#setSessionInfo sessionInfo:%s", sessionIfo);
	}

	public SessionInfo getSessionInfo() {
		return sessionInfo;
	}

	/*
	 * 设置用户信息
	 * 
	 * @param user 用户信息
	 */
	public void setUser(User user, Context context) {
		CacheModel.getInstance().setUser(user);
		if (null != context) {
			new UserModel(context).add(user);
		}
	}

	/*
	 * 获得用户信息
	 * 
	 * @param userId 用户ID
	 */
	public User getUser(String userId, Context context) {
		User user = CacheModel.getInstance().getUser(userId);
		if (null == user && null != context) {
			user = new UserModel(context).query(userId);
			CacheModel.getInstance().setUser(user);
		}
		return user;
	}

	/*
	 * 设置当前登入用户
	 * 
	 * @param user 用户信息
	 */
	public void setLoginUser(User user) {
		CacheModel.setLoginUser(user);
		return;
	}

	/*
	 * 获得当前登入用户
	 */
	public User getLoginUser() {
		return CacheModel.getLoginUser();
	}

	/*
	 * 获得当前登入用户Id
	 */
	public String getLoginUserId() {
		return CacheModel.getLoginUserId();
	}

	/*
	 * 设置当前聊天对象ID
	 * 
	 * @param userId 聊天对象用户ID
	 */
	public void setChatUserId(String userId) {
		CacheModel.getInstance().setChatUserId(userId);
		return;
	}

	/*
	 * 获得当前聊天对象用户ID
	 */
	public String getChatUserId() {
		return CacheModel.getInstance().getChatUserId();
	}

	/*
	 * 设置当前聊天对象
	 * 
	 * @param user 用户信息
	 */
	public void setChatUser(User user) {
		CacheModel.getInstance().setChatUser(user);
		return;
	}

	/*
	 * 清空当前聊天对象并设置前一回聊天对象
	 * 
	 * @param user 用户信息
	 */
	public void clearChatUser() {
		CacheModel.getInstance().clearChatUser();
		return;
	}

	/*
	 * 获得当前聊天对象
	 */
	public User getChatUser() {
		return CacheModel.getInstance().getChatUser(); // 参数判空放在下一层
	}

	/*
	 * 获得前一回聊天对象
	 */
	// public User getLastChatUser() {
	// return CacheModel.getInstance().getLastChatUser(); // 参数判空放在下一层
	// }

	/*
	 * 清空前一回聊天对象
	 */
	// public void clearLastChatUser() {
	// CacheModel.getInstance().clearLastChatUser();
	// return; // 参数判空放在下一层
	// }

	/*
	 * 获得与好友聊天的最后一条信息
	 * 
	 * @param userId 用户ID
	 */
	public MessageInfo getLastMessage(String friendUserId) {
		return CacheModel.getInstance().getLastMessage(friendUserId);
	}

	/*
	 * 推送一条新消息到DB并更新最后一条消息
	 */
	public int obtainMsgId() {
		return CacheModel.getInstance().obtainMsgId();
	}

	/*
	 * 推送一条新消息到DB并更新最后一条消息
	 */
	public boolean pushMsg(MessageInfo msgInfo) {
		return CacheModel.getInstance().push(msgInfo);
	}

	/**
	 * 从数据库拉取信息两个用户之间部分信息（由偏移量和信息条数决定）
	 * 
	 * @param userId
	 *            用户ID
	 * @param friendUserId
	 *            好友ID
	 * @param msgId
	 *            起始ID
	 * @param offset
	 *            距离起始ID的偏移量
	 * @param size
	 *            拉取的消息条数
	 * @return MessageInfo
	 */
	public List<MessageInfo> pullMsg(String userId, String friendUserId,
			int msgId, int offset, int size) {
		return CacheModel.getInstance().pullMsg(userId, friendUserId, msgId, offset, size);
	}

	public List<String> getFriendIdList(String ownerId, Context context) {
		List<String> idList = CacheModel.getInstance().getFriendIdList();
		if (idList.size() < 1 && null != context) {
			idList = new ContactModel(context).queryFriendsIdList(ownerId);
			CacheModel.getInstance().setFriendIdList(idList);
		}
		return idList;
	}

	public boolean setLoadedFriendId(String friendId) {
		return CacheModel.getInstance().setLoadedFriendId(friendId);
	}

	public boolean isLoadedFriendId(String friendId) {
		return CacheModel.getInstance().isLoadedFriendId(friendId);
	}

	/*
	 * 清空某个用户未读消息计数
	 * 
	 * @param userId 用户ID
	 */
	public int clearUnreadCount(String userId) {
		return MessageCacheImpl.getInstance().clearUnreadCount(userId);
	}

	/*
	 * 获得用户未读消息计数
	 * 
	 * @param userId 用户ID
	 */
	public int getUnreadCount(String userId) {
		return MessageCacheImpl.getInstance().getUnreadCount(userId);
	}

	/*
	 * 获得用户未读消息计数
	 * 
	 * @param userId 用户ID
	 */
	public int getUnreadCount() {
		return MessageCacheImpl.getUnreadCount();
	}

	/*
	 * 添加一条最近联系人
	 * 
	 * @param userId 最近联系人
	 * 
	 * @return void
	 */
	public void addFriendId(String userId) {
		ContactCacheImpl.getInstance().addFriendId(userId);
	}

	public void addFriendList(Queue<String> list) {
		ContactCacheImpl.getInstance().addFriendList(list);
	}
	/*
	 * 根据消息ID更新DB中消息的加载状态
	 */
	public Boolean updateMsgStatus(String msgId, int state) {
		//return CacheModel.getInstance().updateMsgStatus(msgId, state);
		return false;
	}

	/*
	 * 更新两个用户之间的所有消息的加载状态
	 */
	public Boolean updateMsgStatus(String userId, String friendUserId, int state) {
		return CacheModel.getInstance().updateMsgStatus(userId, friendUserId, state);
	}

	/*
	 * 根据消息ID更新DB中消息的是否已读或展现状态
	 */
	public Boolean updateMsgReadStatus(int msgId, int state) {
		return CacheModel.getInstance().updateMsgReadStatus(msgId, state);
	}

	/*
	 * 更新消息ID，主要针对图文混排的消息
	 */
	public Boolean updateMsgParentId(int msgId, int msgParentId) {
		return CacheModel.getInstance().updateMsgParentId(msgId, msgParentId);
	}

	/*
	 * 更新两个用户之间的所有消息是否已读或展现状态
	 */
	public Boolean updateMsgReadStatus(String userId, String friendUserId,
			int state) {
		return CacheModel.getInstance().updateMsgReadStatus(userId, friendUserId, state);
	}

	/**
	 * 更改图片存储路径
	 * 
	 * @param entityId
	 * @param friendUserId
	 * @return Boolean
	 */
	public Boolean updateMsgImageSavePath(String MsgId, String newPath) {
		return CacheModel.getInstance().updateMsgImageSavePath(MsgId, newPath);
	}

	/*
	 * 初次打开时检查DB中消息是否达到上限,若达到则删除最老的部分信息
	 * 
	 * @return 返回删除消息的时间分隔线
	 */
	public int checkAndDeleteIfNeed() {
		// 如果需要的话，删除最老的部分历史信息
		return CacheModel.getInstance().checkAndDeleteIfNeed();
	}

}
