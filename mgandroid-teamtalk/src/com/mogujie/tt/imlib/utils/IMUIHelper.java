package com.mogujie.tt.imlib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.GroupManagerAdapter;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.widget.imageview.MGWebImageView;

public class IMUIHelper {
	public static boolean openSessionChatActivity(Logger logger, Context ctx,
			String sessionId, int sessionType, IMService imService) {
		if (logger == null || ctx == null || sessionId == null
				|| imService == null) {
			return false;
		}

		if (sessionType == IMSession.SESSION_P2P) {
			ContactEntity contact = imService.getContactManager().findContact(
					sessionId);
			if (contact == null) {
				logger.e("chat#no such contact -> id:%s", sessionId);
				return false;
			}

			openContactChatActivity(ctx, contact);
			return true;
		} else {
			GroupEntity group = imService.getGroupManager()
					.findGroup(sessionId);
			if (group == null) {
				logger.e("chat#no such group -> id:%s", sessionId);
				return false;
			}

			openGroupChatActivity(ctx, group);

			return true;
		}
	}

	public static void openContactChatActivity(Context ctx,
			ContactEntity contact) {
		if (contact == null) {
			return;
		}

		openChatActivityImpl(ctx, IMSession.SESSION_P2P, contact.id);

	}

	// todo eric 讨论组
	public static void openGroupChatActivity(Context ctx, GroupEntity group) {
		if (group == null) {
			return;
		}

		openChatActivityImpl(ctx, IMSession.SESSION_GROUP, group.id);
	}

	private static void openChatActivityImpl(Context ctx, int sessionType,
			String sessionId) {
		Intent i = new Intent(ctx, MessageActivity.class);
		setSessionInIntent(i, sessionId, sessionType);
		CacheHub.getInstance().setSessionInfo(new SessionInfo(sessionId, sessionType));

		ctx.startActivity(i);
	}

	public static boolean setMessageOwnerName(Logger logger, IMSession session,
			MessageInfo msgInfo, TextView nameTextView) {
		if (logger == null || session == null || msgInfo == null
				|| nameTextView == null) {
			return false;
		}

		logger.d("name#setMessageOwnerName, fromid:%s, from usrname:%s",
				msgInfo.getMsgFromUserId(), msgInfo.getMsgFromName());
		if (!msgInfo.isMyMsg()) {
			logger.d("name#not my msg");
			ContactEntity contact = session.getSessionContact(msgInfo
					.getMsgFromUserId());
			if (contact != null) {
				nameTextView.setText(contact.name);

				return true;
			} else {
				logger.d("name#contact is null");
			}
		}

		return false;
	}

	public static boolean setMessageOwnerAvatar(Logger logger,
			IMSession session, MessageInfo msgInfo, MGWebImageView webImageView) {
		if (logger == null || session == null || msgInfo == null
				|| webImageView == null) {
			return false;
		}

		logger.d("avatar#setMessageOwnerAvatar, fromid:%s, from usrname:%s",
				msgInfo.getMsgFromUserId(), msgInfo.getMsgFromName());

		ContactEntity contact;
		if (msgInfo.isMyMsg()) {
			logger.d("avatar#isMyMsg");
			contact = session.getLoginContact();
			logger.d("avatar#login contact:%s", contact);
		} else {
			logger.d("avatar#is not my msg");
			contact = session.getSessionContact(msgInfo.getMsgFromUserId());
			logger.d("avatar#avatar:%s", contact);
		}

		if (contact != null) {
			logger.d("avatar#setWebImageViewAvatar avatarUrl:%s",
					contact.avatarUrl);
			setWebImageViewAvatar(webImageView, contact.avatarUrl,
					IMSession.SESSION_P2P);

			return true;
		} else {
			logger.d("avatar#contact is null");
		}

		return false;
	}

	public static int getDefaultAvatarResId(int sessionType) {
		if (sessionType == IMSession.SESSION_P2P) {
			return R.drawable.tt_default_user_portrait_corner;
		} else if (sessionType == IMSession.SESSION_GROUP) {
			return R.drawable.group_default;
		} else if (sessionType == IMSession.SESSION_TEMP_GROUP) {
			return R.drawable.discussion_group_default;
		}

		return R.drawable.tt_default_user_portrait_corner;
	}

	public static void setWebImageViewAvatar(MGWebImageView webImageView,
			String avatarUrl, int sessionType) {
		if (avatarUrl == null) {
			return;
		}

		// logger.d("contactUI#getView avatarUrl:%s", avatarUrl);

		String realAvatarUrl = IMContactHelper.getRealAvatarUrl(avatarUrl);

		Logger logger = Logger.getLogger(IMUIHelper.class);
		logger.d("contactUI#realAvatarUrl:%s", realAvatarUrl);

		if (realAvatarUrl.isEmpty()) {
			webImageView.setImageResource(getDefaultAvatarResId(sessionType));

		} else {
			webImageView
					.setDefaultImageResId(getDefaultAvatarResId(sessionType));
			webImageView.setImageUrlNeedFit(realAvatarUrl);
		}

	}

	public static void setGroupMemberGridViewData(Logger logger, Intent intent,
			IMService imService, GroupManagerAdapter adapter) {
		if (adapter == null) {
			logger.e("groupmgr#adapter is null");
			return;
		}

		logger.d("groupmgr#setGridViewData");

		if (imService == null) {
			logger.e("groupmgr#imservice is null");
			return;
		}

		String sessionId = intent.getStringExtra(SysConstant.SESSION_ID_KEY);
		int sessiondType = intent.getIntExtra(SysConstant.SESSION_TYPE_KEY, 0);
		logger.d("groupmgr#sessionType:%d, sessionId:%s", sessiondType,
				sessionId);

		List<ContactEntity> contactList = new ArrayList<ContactEntity>();
		if (sessiondType == IMSession.SESSION_P2P) {
			ContactEntity contact = imService.getContactManager().findContact(
					sessionId);
			if (contact == null) {
				logger.e("groupmgr#no such contact by id:%s", sessionId);
				return;
			}

			contactList.add(contact);
		} else {
			contactList = imService.getGroupManager()
					.getGroupMembers(sessionId);
			if (contactList == null) {
				logger.e("groupmgr#get members from group id:%s failed",
						sessionId);
				return;
			}
		}

		adapter.setData(contactList);
	}

	public static void setSessionInIntent(Intent intent, String sessionId,
			int sessionType) {
		if (intent == null) {
			return;
		}

		Logger.getLogger(IMUIHelper.class).d("notification#setSessionInIntent, sessionId:%s, sessionType:%d", sessionId, sessionType);
		intent.putExtra(SysConstant.SESSION_ID_KEY, sessionId);
		intent.putExtra(SysConstant.SESSION_TYPE_KEY, sessionType);
	}

	public static class SessionInfo {
		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public void setSessionType(int sessionType) {
			this.sessionType = sessionType;
		}

		String sessionId;
		int sessionType;

		public SessionInfo(String sessionId, int sessionType) {
			super();
			this.sessionId = sessionId;
			this.sessionType = sessionType;
		}

		public int getSessionType() {
			// TODO Auto-generated method stub
			return sessionType;
		}
	}

	public static SessionInfo getSessionInfoFromIntent(Intent intent) {
		if (intent == null) {
			return null;
		}

		return new SessionInfo(
				intent.getStringExtra(SysConstant.SESSION_ID_KEY),
				intent.getIntExtra(SysConstant.SESSION_TYPE_KEY, 0));

	}

	public static class ContactPinyinComparator implements Comparator<Object> {
		@Override
		public int compare(Object objEntity1, Object objEntity2) {
			ContactEntity entity1 = (ContactEntity) objEntity1;
			ContactEntity entity2 = (ContactEntity) objEntity2;

			// TODO Auto-generated method stub
			if (entity2.pinyin.startsWith("#")) {
				return -1;
			} else if (entity1.pinyin.startsWith("#")) {
				// todo eric guess: latter is > 0
				return 1;
			} else {

				return entity1.pinyin.compareToIgnoreCase(entity2.pinyin);
			}
		}
	}

	public static List<Object> getContactSortedList(
			Map<String, ContactEntity> contacts) {
		// todo eric efficiency
		List<Object> contactList = new ArrayList<Object>(contacts.values());
		Collections.sort(contactList, new ContactPinyinComparator());

		return contactList;

	}
}
