package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	//todo eric, add session type
	// key = session_id
	private HashMap<String, List<MessageInfo>> unreadMsgs = new HashMap<String, List<MessageInfo>>();

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

//	public synchronized MessageInfo getUnreadMsg(String sessionId, String msgId) {
//		logger.d("unread#getUnreadMsg sessionId:%s, msgId:%s", sessionId, msgId);
//
//		List<MessageInfo> msgList = unreadMsgs.get(sessionId);
//		if (msgList == null) {
//			logger.w("unread# sessionId:%s has no unreadMsgs", sessionId);
//			return null;
//		}
//
//		for (MessageInfo msg : msgList) {
//			if (msg.msgId.equals(msgId)) {
//				return msg;
//			}
//		}
//		
//		logger.d("unread#no such unread msg");
//		return null;
//	}
}
