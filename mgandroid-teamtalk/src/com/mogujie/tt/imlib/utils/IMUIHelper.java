package com.mogujie.tt.imlib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.GroupManagerAdapter;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMContactManager;
import com.mogujie.tt.imlib.IMGroupManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.activity.UserInfoActivity;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinArea;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinElement;
import com.mogujie.widget.imageview.MGWebImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class IMUIHelper {
	public static class DepartmentPinyinComparator
			implements
				Comparator<Object> {

		@Override
		public int compare(Object objEntity1, Object objEntity2) {
			DepartmentEntity entity1 = (DepartmentEntity) objEntity1;
			DepartmentEntity entity2 = (DepartmentEntity) objEntity2;

			return entity1.pinyinElement.pinyin.compareToIgnoreCase(entity2.pinyinElement.pinyin);
		}
	};

	public static class GroupPinyinComparator implements Comparator<Object> {

		@Override
		public int compare(Object objEntity1, Object objEntity2) {
			GroupEntity entity1 = (GroupEntity) objEntity1;
			GroupEntity entity2 = (GroupEntity) objEntity2;

			return entity1.pinyinElement.pinyin.compareToIgnoreCase(entity2.pinyinElement.pinyin);
		}
	};

	public static boolean triggerSearchDataReady(Logger logger, Context ctx,
			IMContactManager contactMgr, IMGroupManager groupMgr) {
		//contact,department, group data are all ready
		logger.d("search#triggerSearchDataReady");

		if (isSearchDataReady(contactMgr, groupMgr)) {
			logger.i("search#conditions are all ready, broadcast");

			if (ctx != null) {
				logger.d("search#start boradcast search_data_ready action");
				ctx.sendBroadcast(new Intent(IMActions.ACTION_SEARCH_DATA_READY));
				return true;
			}

		}

		logger.d("search#didn't broadcast anything because conditions are still not ready");

		return false;

	}

	public static boolean isSearchDataReady(IMContactManager contactMgr,
			IMGroupManager groupMgr) {
		return contactMgr.ContactsDataReady()
				&& groupMgr.groupReadyConditionOk();
	}

	public static boolean handleContactPinyinSearch(Logger logger,
			PinYinElement contactPinyinElement, String key,
			SearchElement contactSearchElement) {
		contactSearchElement.reset();

		String pinyin = contactPinyinElement.pinyin;

		//the first char # was added manually when creating pinyin
		if (pinyin.startsWith("#")) {
			pinyin = pinyin.substring(1);
		}

		SearchElement pinyinSearchElement = new SearchElement();
		if (!IMUIHelper.handleNameSearch(pinyin, key, pinyinSearchElement)) {
			return false;
		}

		logger.d("pinyin#pinyinSearchElement:%s", pinyinSearchElement);

		return IMUIHelper.locateNameAreaByPinyinIndex(contactPinyinElement, contactSearchElement, pinyinSearchElement.startIndex, pinyinSearchElement.endIndex);
	}

	public static boolean locateNameAreaByPinyinIndex(
			PinYinElement pinYinElement, SearchElement searchElement,
			int pinyinStartIndex, int pinyinEndIndex) {
		for (int i = 0; i < pinYinElement.pinyinArea.size(); ++i) {
			PinYinArea area = pinYinElement.pinyinArea.get(i);

			if (pinyinStartIndex >= area.startIndex
					&& pinyinStartIndex <= area.endIndex) {
				searchElement.startIndex = i;
			}

			if (pinyinEndIndex >= area.startIndex
					&& pinyinEndIndex <= area.endIndex) {
				searchElement.endIndex = i + 1;
				return true;
			}
		}

		return false;
	}
	public static boolean handleNameSearch(String name, String key,
			SearchElement searchElement) {
		int index = name.indexOf(key);
		if (index == -1) {
			return false;
		}

		searchElement.startIndex = index;
		searchElement.endIndex = index + key.length();

		return true;
	}

	public static void setTextViewCharHilighted(TextView textView, String text,
			int startIndex, int endIndex, int color) {
		if (textView == null || text == null) {
			return;
		}

		if (startIndex < 0) {
			return;
		}

		if (endIndex > text.length()) {
			return;
		}

		textView.setText(text, BufferType.SPANNABLE);

		Spannable span = (Spannable) textView.getText();
		if (span == null) {
			return;
		}

		span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	public static void setViewTouchHightlighted(final View view) {
		if (view == null) {
			return;
		}

		view.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					view.setBackgroundColor(Color.rgb(1, 175, 244));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					view.setBackgroundColor(Color.rgb(255, 255, 255));
				}
				return false;
			}
		});
	}

	public static void handleContactItemLongClick(final Context ctx,
			final ContactEntity contact) {
		if (contact == null) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
		builder.setTitle(contact.name);
		String[] items = new String[]{ctx.getString(R.string.check_profile),
				getCallPhoneDescription(ctx, contact)};

		builder.setItems(items, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0 :
						IMUIHelper.openUserProfileActivity(ctx, contact.id);
						break;
					case 1 :
						IMUIHelper.callPhone(ctx, contact.telephone);
						break;
				}
			}
		});
		builder.show();
	}

	private static String getCallPhoneDescription(Context ctx,
			ContactEntity contact) {
		return String.format("%s(%s)", ctx.getString(R.string.call_phone), getPhoneNumberDescription(ctx, contact));
	}

	private static String getPhoneNumberDescription(Context ctx,
			ContactEntity contact) {
		if (contact.telephone.isEmpty()) {
			return ctx.getString(R.string.empty_phone_no);
		} else {
			return contact.telephone;
		}
	}

	public static void callPhone(Context ctx, String phoneNumber) {
		if (ctx == null) {
			return;
		}

		if (phoneNumber.isEmpty()) {
			return;
		}

		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ phoneNumber));

		ctx.startActivity(intent);

	}

	public static void openUserProfileActivity(Context ctx, String contactId) {
		Intent intent = new Intent(ctx, UserInfoActivity.class);
		IMUIHelper.setSessionInIntent(intent, contactId, IMSession.SESSION_P2P);
		ctx.startActivity(intent);
	}

	public static boolean openSessionChatActivity(Logger logger, Context ctx,
			String sessionId, int sessionType, IMService imService) {
		if (logger == null || ctx == null || sessionId == null
				|| imService == null) {
			return false;
		}

		if (sessionType == IMSession.SESSION_P2P) {
			ContactEntity contact = imService.getContactManager().findContact(sessionId);
			if (contact == null) {
				logger.e("chat#no such contact -> id:%s", sessionId);
				return false;
			}

			openContactChatActivity(ctx, contact);
			return true;
		} else {
			GroupEntity group = imService.getGroupManager().findGroup(sessionId);
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

		openChatActivity(ctx, IMSession.SESSION_P2P, contact.id);

	}

	// todo eric 讨论组
	public static void openGroupChatActivity(Context ctx, GroupEntity group) {
		if (group == null) {
			return;
		}

		openChatActivity(ctx, group.type, group.id);
	}

	public static void openChatActivity(Context ctx, int sessionType,
			String sessionId) {
		Intent i = new Intent(ctx, MessageActivity.class);
		i.setAction(IMActions.ACTION_NEW_MESSAGE_SESSION);

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

		logger.d("name#setMessageOwnerName, fromid:%s, from usrname:%s", msgInfo.getMsgFromUserId(), msgInfo.getMsgFromName());
		if (!msgInfo.isMyMsg()) {
			logger.d("name#not my msg");
			ContactEntity contact = session.getSessionContact(msgInfo.getMsgFromUserId());
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

		logger.d("avatar#setMessageOwnerAvatar, fromid:%s, from usrname:%s", msgInfo.getMsgFromUserId(), msgInfo.getMsgFromName());

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
			logger.d("avatar#setWebImageViewAvatar avatarUrl:%s", contact.avatarUrl);
			setWebImageViewAvatar(webImageView, contact.avatarUrl, IMSession.SESSION_P2P);

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
			webImageView.setDefaultImageResId(getDefaultAvatarResId(sessionType));
			webImageView.setImageUrlNeedFit(realAvatarUrl);
		}

	}

	public static void setEntityImageViewAvatar(ImageView imageView,
			String avatarUrl, int sessionType) {

		Logger logger = Logger.getLogger(IMUIHelper.class);

		logger.d("debug#setEntityImageViewAvatar imageView:%s, avatarUrl:%s", imageView, avatarUrl);

		if (avatarUrl == null) {
			return;
		}

		int defaultResId = getDefaultAvatarResId(sessionType);

		//todo eric created too many options, but I can't find a way to change showImageOnLoading resource id
		// dynamically based on sessionType
		DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(defaultResId).showImageForEmptyUri(defaultResId).showImageOnFail(defaultResId).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(5)).build();

		String realAvatarUrl = IMContactHelper.getRealAvatarUrl(avatarUrl);

		logger.d("contactUI#realAvatarUrl:%s", realAvatarUrl);

		ImageLoader.getInstance().displayImage(realAvatarUrl, imageView, options, null);
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
		logger.d("groupmgr#sessionType:%d, sessionId:%s", sessiondType, sessionId);

		List<ContactEntity> contactList = new ArrayList<ContactEntity>();
		if (sessiondType == IMSession.SESSION_P2P) {
			ContactEntity contact = imService.getContactManager().findContact(sessionId);
			if (contact == null) {
				logger.e("groupmgr#no such contact by id:%s", sessionId);
				return;
			}

			contactList.add(contact);
		} else {
			contactList = imService.getGroupManager().getGroupMembers(sessionId);
			if (contactList == null) {
				logger.e("groupmgr#get members from group id:%s failed", sessionId);
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
		@Override
		public String toString() {
			return "SessionInfo [sessionId=" + sessionId + ", sessionType="
					+ sessionType + "]";
		}

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

		return new SessionInfo(intent.getStringExtra(SysConstant.SESSION_ID_KEY), intent.getIntExtra(SysConstant.SESSION_TYPE_KEY, 0));

	}

	public static class ContactPinyinComparator implements Comparator<Object> {
		@Override
		public int compare(Object objEntity1, Object objEntity2) {
			ContactEntity entity1 = (ContactEntity) objEntity1;
			ContactEntity entity2 = (ContactEntity) objEntity2;

			// TODO Auto-generated method stub
			if (entity2.pinyinElement.pinyin.startsWith("#")) {
				return -1;
			} else if (entity1.pinyinElement.pinyin.startsWith("#")) {
				// todo eric guess: latter is > 0
				return 1;
			} else {

				return entity1.pinyinElement.pinyin.compareToIgnoreCase(entity2.pinyinElement.pinyin);
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

	public static List<Object> getGroupSortedList(
			Map<String, GroupEntity> groups) {
		// todo eric efficiency
		List<Object> groupList = new ArrayList<Object>(groups.values());
		Collections.sort(groupList, new GroupPinyinComparator());

		return groupList;
	}

	public static List<Object> getDepartmentSortedList(
			Map<String, DepartmentEntity> departments) {
		// todo eric efficiency
		List<Object> departmentList = new ArrayList<Object>(departments.values());
		Collections.sort(departmentList, new DepartmentPinyinComparator());

		return departmentList;
	}

	public static boolean isSameSession(SessionInfo sessionInfo,
			IMSession session) {
		if (sessionInfo == null || session == null) {
			return false;
		}

		return (sessionInfo.getSessionId().equals(session.getSessionId()) && sessionInfo.getSessionType() == session.getType());
	}
}
