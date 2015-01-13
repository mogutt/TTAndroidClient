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
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.GroupManagerAdapter;
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
import com.mogujie.tt.utils.pinyin.PinYin.PinYinElement;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class IMUIHelper {
	public static String getSessionKey(String sessionId, int sessionType) {
		return String.format("%s_%d", sessionId, sessionType);
	}
	
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

	public static boolean handleDepartmentSearch(String key, DepartmentEntity department) {
		if (TextUtils.isEmpty(key) || department == null) {
			return false;
		}
		
		department.searchElement.reset();
		
		return handleTokenFirstCharsSearch(key, department.pinyinElement, department.searchElement)
		|| handleTokenPinyinFullSearch(key, department.pinyinElement, department.searchElement)
		|| handleNameSearch(department.title, key, department.searchElement);
	}
	
	
	public static boolean handleGroupSearch(String key, GroupEntity group) {
		if (TextUtils.isEmpty(key) || group == null) {
			return false;
		}
		
		group.searchElement.reset();
		
		return handleTokenFirstCharsSearch(key, group.pinyinElement, group.searchElement)
		|| handleTokenPinyinFullSearch(key, group.pinyinElement, group.searchElement)
		|| handleNameSearch(group.name, key, group.searchElement);
	}
	
	public static boolean handleContactSearch(String key, ContactEntity contact) {
		if (TextUtils.isEmpty(key) || contact == null) {
			return false;
		}
		
		contact.searchElement.reset();
		
		return handleTokenFirstCharsSearch(key, contact.pinyinElement, contact.searchElement)
		|| handleTokenPinyinFullSearch(key, contact.pinyinElement, contact.searchElement)
		|| handleNameSearch(contact.name, key, contact.searchElement);
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
	
	public static boolean handleTokenFirstCharsSearch(String key, PinYinElement pinYinElement, SearchElement searchElement) {
		return handleNameSearch(pinYinElement.tokenFirstChars, key.toUpperCase(), searchElement);
	}
	
	public static boolean handleTokenPinyinFullSearch(String key, PinYinElement pinYinElement, SearchElement searchElement) {
		if (TextUtils.isEmpty(key)) {
			return false;
		}
		
		String searchKey = key.toUpperCase();
		
		//clear the old search result
		searchElement.reset();
		
		int tokenCnt = pinYinElement.tokenPinyinList.size();
		int startIndex = -1;
		int endIndex = -1;
		
		for (int i = 0; i < tokenCnt; ++i) {
			String tokenPinyin = pinYinElement.tokenPinyinList.get(i);
			
			int tokenPinyinSize = tokenPinyin.length();
			int searchKeySize = searchKey.length();
			
			int keyCnt = Math.min(searchKeySize, tokenPinyinSize);
			String keyPart = searchKey.substring(0, keyCnt);
			
			if (tokenPinyin.startsWith(keyPart)) {
				
				if (startIndex == -1) {
					startIndex = i;
				} 
				
				endIndex = i + 1;
			} else {
				continue;
			}
			
			if (searchKeySize <= tokenPinyinSize) {
				searchKey = "";
				break;
			}
			
			searchKey = searchKey.substring(keyCnt, searchKeySize);
		}
		
		if (!searchKey.isEmpty()) {
			return false;
		}
		
		if (startIndex >= 0 && endIndex > 0) {
			searchElement.startIndex = startIndex;
			searchElement.endIndex = endIndex;
			
			return true;
		}
		
		return false;
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
		if (contact.telephone == null || contact.telephone.isEmpty()) {
			return ctx.getString(R.string.empty_phone_no);
		} else {
			return contact.telephone;
		}
	}

	public static void callPhone(Context ctx, String phoneNumber) {
		if (ctx == null) {
			return;
		}

		if (phoneNumber == null || phoneNumber.isEmpty()) {
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
			logger.e("chat#openSessionChatActivity invalid args");
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
//		//explicitly send a broadcast to notify that the session could be changed
//		//so that messageactivity would reload
//		Intent intent = new Intent(IMActions.ACTION_NEW_MESSAGE_SESSION);
//		setSessionInIntent(intent, sessionId, sessionType);
//		ctx.sendBroadcast(intent);
		
		Intent i = new Intent(ctx, MessageActivity.class);
		setSessionInIntent(i, sessionId, sessionType);

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
			IMSession session, MessageInfo msgInfo, ImageView imageView) {
		if (logger == null || session == null || msgInfo == null
				|| imageView == null) {
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
			logger.d("avatar#setEntityImageViewAvatar avatarUrl:%s", contact.avatarUrl);
			setEntityImageViewAvatar(imageView, contact.avatarUrl, IMSession.SESSION_P2P);

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

//	public static void setWebImageViewAvatar(MGWebImageView webImageView,
//			String avatarUrl, int sessionType) {
//		if (avatarUrl == null) {
//			return;
//		}
//
//		// logger.d("contactUI#getView avatarUrl:%s", avatarUrl);
//
//		String realAvatarUrl = IMContactHelper.getRealAvatarUrl(avatarUrl);
//
//		Logger logger = Logger.getLogger(IMUIHelper.class);
//		logger.d("contactUI#realAvatarUrl:%s", realAvatarUrl);
//
//		if (realAvatarUrl.isEmpty()) {
//			webImageView.setImageResource(getDefaultAvatarResId(sessionType));
//
//		} else {
//			webImageView.setDefaultImageResId(getDefaultAvatarResId(sessionType));
//			webImageView.setImageUrlNeedFit(realAvatarUrl);
//		}
//
//	}
	
	
	public static void setEntityImageViewAvatar(ImageView imageView,
			String avatarUrl, int sessionType) {
		
		setEntityImageViewAvatarImpl(imageView, avatarUrl, sessionType, true);
	}
	
	public static void setEntityImageViewAvatarNoDefaultPortrait(ImageView imageView,
			String avatarUrl, int sessionType) {
		setEntityImageViewAvatarImpl(imageView, avatarUrl, sessionType, false);
	}
	
	public static void setEntityImageViewAvatarImpl(ImageView imageView,
			String avatarUrl, int sessionType, boolean showDefaultPortrait) {
		if (avatarUrl == null) {
			avatarUrl = "";
		}
		
		String fullAvatar = IMContactHelper.getRealAvatarUrl(avatarUrl);
		int defaultResId = -1;
		
		if (showDefaultPortrait) {
			defaultResId = getDefaultAvatarResId(sessionType);
		}
		
		displayImage(imageView, fullAvatar, defaultResId);
	}
	
	public static void displayImage(ImageView imageView,
			String resourceUri, int defaultResId) {

		Logger logger = Logger.getLogger(IMUIHelper.class);
		
		logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

		if (resourceUri == null) {
			resourceUri = "";
		}
		
		boolean showDefaultImage = !(defaultResId <= 0);
		
		if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
			logger.e("displayimage#, unable to display image");
			return;
		}

	
		DisplayImageOptions options;
		if (showDefaultImage) {
			options = new DisplayImageOptions.Builder().
			showImageOnLoading(defaultResId).
			showImageForEmptyUri(defaultResId).
			showImageOnFail(defaultResId).
			cacheInMemory(true).
			cacheOnDisk(true).
			considerExifParams(true).
	//		displayer(new RoundedBitmapDisplayer(5)).
	//		imageScaleType(ImageScaleType.EXACTLY).
			build();
		} else {
			options = new DisplayImageOptions.Builder().
			cacheInMemory(true).
			cacheOnDisk(true).
			considerExifParams(true).
			build();
		}

		ImageLoader.getInstance().displayImage(resourceUri, imageView, options, null);
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

		String sessionId = intent.getStringExtra(SysConstant.KEY_SESSION_ID);
		int sessiondType = intent.getIntExtra(SysConstant.KEY_SESSION_TYPE, 0);
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
		intent.putExtra(SysConstant.KEY_SESSION_ID, sessionId);
		intent.putExtra(SysConstant.KEY_SESSION_TYPE, sessionType);
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

		return new SessionInfo(intent.getStringExtra(SysConstant.KEY_SESSION_ID), intent.getIntExtra(SysConstant.KEY_SESSION_TYPE, 0));

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

		return (sessionInfo.getSessionId().equals(session.getSessionId()) && sessionInfo.getSessionType() == session.getSessionType());
	}
}
