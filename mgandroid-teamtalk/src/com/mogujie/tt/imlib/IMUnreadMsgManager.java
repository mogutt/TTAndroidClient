package com.mogujie.tt.imlib;

import java.util.concurrent.ConcurrentHashMap;

import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.log.Logger;

public class IMUnreadMsgManager {
	private Logger logger = Logger.getLogger(IMUnreadMsgManager.class);

	// key = session_id
	// sub map key = msg_id
	private ConcurrentHashMap<String, ConcurrentHashMap<String, MessageEntity>> unreadMsgs = new ConcurrentHashMap<String, ConcurrentHashMap<String, MessageEntity>>();

	public void add(MessageEntity msg) {
		logger.d("chat#add unread msg:%s", msg);

		ConcurrentHashMap<String, MessageEntity> msgMap = unreadMsgs
				.get(msg.sessionId);
		if (msgMap == null) {
			msgMap = new ConcurrentHashMap<String, MessageEntity>();
		}

		msgMap.put(msg.msgId, msg);
		unreadMsgs.put(msg.sessionId, msgMap);
	}

	public MessageEntity getUnreadMsg(String sessionId, String msgId) {
		ConcurrentHashMap<String, MessageEntity> msgMap = unreadMsgs
				.get(sessionId);
		if (msgMap == null) {
			return null;
		}

		return msgMap.get(msgId);
	}
	
	public MessageEntity popUnreadMsg(String sessionId, String msgId) {
		ConcurrentHashMap<String, MessageEntity> msgMap = unreadMsgs
				.get(sessionId);
		if (msgMap == null) {
			return null;
		}

		return msgMap.remove(msgId);
	}

	public ConcurrentHashMap<String, MessageEntity> getUnreadMsgs(
			String sessionId) {

		return unreadMsgs.get(sessionId);
	}

	public void removeUnreadMsgs(String sessionId) {
		unreadMsgs.remove(sessionId);
	}

	public void remove(String sessionId, String msgId) {
		logger.d("chat#remove sessionId:%s, msgId:%s", sessionId, msgId);

		ConcurrentHashMap<String, MessageEntity> msgMap = unreadMsgs
				.get(sessionId);

		if (msgMap == null) {
			return;
		}

		msgMap.remove(msgId);
	}

}
