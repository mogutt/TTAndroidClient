package com.mogujie.tt.imlib.utils;

import java.util.HashMap;
import java.util.Map;

import com.mogujie.tt.log.Logger;

public class SessionNotificationIdMap {
	private Logger logger = Logger.getLogger(SessionNotificationIdMap.class);

	private Map<String, Integer> sessionNotifitionIdMap = new HashMap<String, Integer>();
	private int notificationId = 0;

	public int addSession(String sessionId, int sessionType) {
		logger.d("notification#addSession sessionId:%s, sessionType:%d", sessionId, sessionType);
		
		String sessionKey = getSessionKey(sessionId, sessionType);
		int id = ++notificationId;
		
		logger.d("notification#sessionKey:%s, notificationId:%d", sessionKey, id);
		
		sessionNotifitionIdMap.put(sessionKey, id);
		
		return id;
	}
	
	private String getSessionKey(String sessionId, int sessionType) {
		return String.format("%d_%s", sessionType, sessionId);
	}
	
	public int getSessionNotificationId(String sessionId, int sessionType) {
		logger.d("notification#getSessionNotificationId sessionId:%s, sessionType:%s", sessionId, sessionType);
		Integer value = sessionNotifitionIdMap.get(getSessionKey(sessionId, sessionType));
		if (value == null) {
			logger.d("notification#no such session");
			return -1;
		}
		
		return value;
	}
}
