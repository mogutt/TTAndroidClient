package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.log.Logger;

public class IMUnreadMsgManager extends IMManager {
	private static IMUnreadMsgManager inst;

	public static IMUnreadMsgManager instance() {
		synchronized (IMUnreadMsgManager.class) {
			if (inst == null) {
				inst = new IMUnreadMsgManager();
			}

			return inst;
		}
	}

	private Logger logger = Logger.getLogger(IMUnreadMsgManager.class);

	// todo eric, add session type
	// key = session_id
	private HashMap<String, List<MessageInfo>> unreadMsgs = new HashMap<String, List<MessageInfo>>();
	private Map<String, MessageInfo> sessionLastUnreadMsgMap = new HashMap<String, MessageInfo>();

	public synchronized void updateSessionLastUnreadMsg(String sessionId, MessageInfo msg) {
		logger.d("unread#repeat#updateSessionLastUnreadMsg -> sessionId:%s, msg:%s", sessionId, msg);
		
		if (TextUtils.isEmpty(sessionId) || msg == null) {
			logger.e("unread#repeat#invalid args");
			return;
		}
		
		sessionLastUnreadMsgMap.put(sessionId, msg);
	}
	
	public synchronized MessageInfo popSessionLastUnreadMsg(String sessionId) {
		logger.d("unread#repeat#popSessionLastUnreadMsg -> sessionId:%s", sessionId);
		
		if (!sessionLastUnreadMsgMap.containsKey(sessionId)) {
			return null;
		}
		
		return sessionLastUnreadMsgMap.get(sessionId);
	}

	public synchronized void add(MessageInfo msg) {
		logger.d("unread#unreadMgr#add unread msg:%s", msg);

		List<MessageInfo> msgList = unreadMsgs.get(msg.sessionId);
		if (msgList == null) {
			msgList = new ArrayList<MessageInfo>();
		}

		msgList.add(msg);
		unreadMsgs.put(msg.sessionId, msgList);
	}

	public synchronized List<MessageInfo> popUnreadMsgList(String sessionId) {
		logger.d("unread#getUnreadMsgList sessionId:%s", sessionId);

		List<MessageInfo> msgList = unreadMsgs.remove(sessionId);
		if (msgList == null) {
			logger.w("unread# sessionId:%s has no unreadMsgs", sessionId);
			return null;
		}

		return msgList;
	}
	
//	public synchronized boolean hasUnreadMsgs(String sessionId) {
//		logger.d("unread#hasUnreadMsgs sessionId:%s", sessionId);
//
//		List<MessageInfo> msgList = unreadMsgs.get(sessionId);
//		if (msgList == null || msgList.isEmpty()) {
//			logger.d("unread# sessionId:%s has no unreadMsgs", sessionId);
//			return false;
//		}
//
//		return true;
//	}

	public synchronized int getUnreadMsgListCnt(String sessionId) {
		logger.d("unread#getUnreadMsgListCnt sessionId:%s", sessionId);
		List<MessageInfo> msgList = unreadMsgs.get(sessionId);
		if (msgList == null) {
			return 0;
		}

		return msgList.size();
	}

	public synchronized MessageInfo getLatestMessage(String sessionId) {
		logger.d("unread#getLatestMessage sessionId:%s", sessionId);
		List<MessageInfo> msgList = unreadMsgs.get(sessionId);

		if (msgList == null || msgList.isEmpty()) {
			return null;
		}

		return msgList.get(msgList.size() - 1);
	}

	@Override
	public void reset() {
		unreadMsgs.clear();
	}

	// public synchronized MessageInfo getUnreadMsg(String sessionId, String
	// msgId) {
	// logger.d("unread#getUnreadMsg sessionId:%s, msgId:%s", sessionId, msgId);
	//
	// List<MessageInfo> msgList = unreadMsgs.get(sessionId);
	// if (msgList == null) {
	// logger.w("unread# sessionId:%s has no unreadMsgs", sessionId);
	// return null;
	// }
	//
	// for (MessageInfo msg : msgList) {
	// if (msg.msgId.equals(msgId)) {
	// return msg;
	// }
	// }
	//
	// logger.d("unread#no such unread msg");
	// return null;
	// }
}
