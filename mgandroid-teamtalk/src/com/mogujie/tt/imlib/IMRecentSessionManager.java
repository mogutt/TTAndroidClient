package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;

import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.log.Logger;

public class IMRecentSessionManager extends IMManager {
	private static IMRecentSessionManager inst;

	private Logger logger = Logger.getLogger(IMRecentSessionManager.class);
	// key = contact or group id
	private ConcurrentHashMap<String, RecentInfo> recentSessionMap = new ConcurrentHashMap<String, RecentInfo>();
	private int unreadMsgTotalCnt = 0;

	public static IMRecentSessionManager instance() {
		synchronized (IMRecentSessionManager.class) {
			if (inst == null) {
				inst = new IMRecentSessionManager();
			}

			return inst;
		}
	}

	public void addRecentSession(RecentInfo session) {
		logger.d("recent#addRecentSession -> session:%s", session);

		recentSessionMap.put(session.getEntityId(), session);
	}

	private RecentInfo createRecentInfo(MessageInfo msg) {
		if (msg.sessionType == IMSession.SESSION_P2P) {
			ContactEntity contact = IMContactManager.instance().findContact(msg.sessionId);
			return IMContactHelper.convertContactEntity2RecentInfo(contact, msg.createTime);
		} else {
			GroupEntity group = IMGroupManager.instance().findGroup(msg.sessionId);

			return IMContactHelper.convertGroupEntity2RecentInfo(group);
		}
	}

	public void update(MessageInfo msg) {
		logger.d("recent#update msg:%s", msg);
		if (msg == null) {
			return;
		}

		RecentInfo recentInfo = recentSessionMap.get(msg.sessionId);
		if (recentInfo == null) {
			recentInfo = createRecentInfo(msg);
		}

		recentInfo.setLasttime(msg.createTime);
		recentInfo.setLastContent(getMessageEntityDescription(msg));

		if (!msg.isMy()) {
			incUnreadMsgCnt(recentInfo);
		}
	}

	private String getMessageEntityDescription(MessageInfo msg) {
		if (msg == null) {
			return "";
		}

		if (msg.isTextType()) {
			return new String(msg.msgData);
		} else if (msg.isAudioType()) {
			// todo eric i18n
			return "[语音]";
		} else if (msg.isImage()) {
			return "[图片]";
		}

		return "";
	}

	private synchronized void incUnreadMsgCnt(RecentInfo recentInfo) {
		recentInfo.incUnreadCount();
		unreadMsgTotalCnt++;
	}

	// public synchronized void decUnreadMsgCnt(String sessionId) {
	// RecentInfo recentInfo = recentSessionMap.get(sessionId);
	// if (recentInfo == null) {
	// logger.e(
	// "recent:decUnreadMsgCnt didn't find recentinfo by sessionId:%d",
	// sessionId);
	// return;
	// }
	//
	// recentInfo.decUnreadCount();
	// unreadMsgTotalCnt--;
	// }

	public synchronized void resetUnreadMsgCnt(String sessionId) {
		logger.d("recent#resetUnreadMsgCnt -> sessionId:%s", sessionId);

		RecentInfo recentInfo = recentSessionMap.get(sessionId);
		if (recentInfo == null) {
			logger.e("resetUnreadMsgCnt didn't find recentinfo by sessionId:%s", sessionId);
			return;
		}

		int oldMsgCnt = recentInfo.getUnReadCount();
		logger.d("unread#oldMsgCnt:%d", oldMsgCnt);

		unreadMsgTotalCnt -= oldMsgCnt;
		recentInfo.setUnReadCount(0);

		broadcast();
	}

	public int getTotalUnreadMsgCnt() {
		return unreadMsgTotalCnt;
	}

	public List<RecentInfo> getRecentSessionList() {
		// todo eric every time it has to sort, kind of inefficient, change it
		ArrayList<RecentInfo> recnetSessionList = new ArrayList<RecentInfo>(recentSessionMap.values());
		Collections.sort(recnetSessionList);

		return recnetSessionList;
	}

	public void broadcast() {
		logger.d("recent#triggerDataSetChanged");
		if (ctx != null) {
			ctx.sendBroadcast(new Intent(IMActions.ACTION_ADD_RECENT_CONTACT_OR_GROUP));
			logger.d("recent#send new recent contact");
		}

	}
}
