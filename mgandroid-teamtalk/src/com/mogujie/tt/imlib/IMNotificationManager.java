package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.View;

import com.mogujie.tt.R;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.common.ConfigDefs;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class IMNotificationManager extends IMManager
		implements
			OnIMServiceListner {

	private static IMNotificationManager inst;
	private Logger logger = Logger.getLogger(IMNotificationManager.class);
	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	public static IMNotificationManager instance() {
		synchronized (IMNotificationManager.class) {
			if (inst == null) {
				inst = new IMNotificationManager();
			}

			return inst;
		}
	}

	private IMNotificationManager() {

	}

	public void register() {
		logger.d("notification#register");

		cancelAllNotifications();

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_MSG_RECV);
		actions.add(IMActions.ACTION_LOGOUT);

		imServiceHelper.registerActions(ctx, actions, IMServiceHelper.INTENT_NO_PRIORITY, this);
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		logger.d("notification#onAction action:%s", action);

		if (action.equals(IMActions.ACTION_MSG_RECV)) {
			handleMsgRecv(intent);
		} else if (action.equals(IMActions.ACTION_LOGOUT)) {
			handleLogout();
		}
	}

	private void handleLogout() {
		cancelAllNotifications();
	}

	public void cancelAllNotifications() {
		logger.d("notification#cancelAllNotifications");

		NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}
		notifyMgr.cancelAll();
	}

	public String getSessionAvatarUrl(int sessionType, String sessionId) {
		String url = "";
		if (sessionType == IMSession.SESSION_P2P) {
			ContactEntity contact = IMContactManager.instance().findContact(sessionId);
			if (contact == null) {
				logger.d("notification#no such contact by id:%s", sessionId);
				return "";
			}

			url = contact.avatarUrl;

		} else {
			GroupEntity group = IMGroupManager.instance().findGroup(sessionId);
			if (group == null) {
				logger.d("notification#no such group by id:%s", sessionId);
				return "";
			}

			url = group.avatarUrl;
		}

		return IMContactHelper.getRealAvatarUrl(url);
	}

	private void handleMsgRecv(Intent intent) {
		logger.d("notification#recv unhandled message");

		SessionInfo sessionInfo = IMUIHelper.getSessionInfoFromIntent(intent);
		String sessionId = sessionInfo.getSessionId();
		logger.d("notification#msg no one handled, sessionId:%s, sessionType:%d", sessionId, sessionInfo.getSessionType());

		if (!shouldGloballyShowNotification()) {
			logger.d("notification#shouldGloballyShowNotification is false, return");
			return;
		}
		
		if (!shouldShowNotificationBySession(sessionInfo)) {
			logger.d("notification#shouldShowNotificationBySession is false, return.sessionInfo:%s", sessionInfo);
			return;
		}

		final MessageEntity msg = IMUnreadMsgManager.instance().getLatestMessage(sessionId);
		if (msg == null) {
			logger.e("notification#getLatestMessage failed for sessionId:%s", sessionId);
			return;
		}
		MessageInfo msgInfo = new MessageInfo(msg);
		//if the message is a multi login message which send from another terminal,not need notificate to status bar
		if(!msgInfo.isMyMsg()) {
			int sessionTotalMsgCnt = IMUnreadMsgManager.instance().getUnreadMsgListCnt(sessionId);
			logger.d("notification#getUnreadMsgListCnt:%d", sessionTotalMsgCnt);

			showNotification(msg, sessionId, sessionTotalMsgCnt);			
		}
	}

	private boolean shouldGloballyShowNotification() {
		if (IMConfigurationManager.instance().getBoolean(ConfigDefs.CATEGORY_GLOBAL, ConfigDefs.KEY_NOTIFICATION_NO_DISTURB, ConfigDefs.DEF_VALUE_NOTIFICATION_NO_DISTURB)) {
			logger.d("notification#global setting: no disturb");
			return false;
		}

		return true;
	}
	
	private boolean shouldShowNotificationBySession(SessionInfo sessionInfo) {
		String sessionKey = IMUIHelper.getSessionKey(sessionInfo.getSessionId(), sessionInfo.getSessionType());
		if (IMConfigurationManager.instance().getBoolean(sessionKey, ConfigDefs.KEY_NOTIFICATION_NO_DISTURB, ConfigDefs.DEF_VALUE_NOTIFICATION_NO_DISTURB)) {
			logger.d("notification#shouldShowNotificationBySession:%s: no disturb", sessionKey);
			return false;
		}

		return true;
	}
	
	

	private boolean shouldUseNotificationSound() {
		return IMConfigurationManager.instance().getBoolean(ConfigDefs.CATEGORY_GLOBAL, ConfigDefs.KEY_NOTIFICATION_GOT_SOUND, ConfigDefs.DEF_VALUE_NOTIFICATION_GOT_SOUND);
	}

	private boolean shouldUseNotificationVibration() {
		return IMConfigurationManager.instance().getBoolean(ConfigDefs.CATEGORY_GLOBAL, ConfigDefs.KEY_NOTIFICATION_GOT_VIBRATION, ConfigDefs.DEF_VALUE_NOTIFICATION_GOT_VIBRATION);
	}

	private void showNotification(final MessageEntity latestMsg,
			final String sessionId, final int sessionTotalMsgCnt) {
		// todo eric need to set the exact size of the big icon
		ImageSize targetSize = new ImageSize(80, 80);
		String avatarUrl = getSessionAvatarUrl(latestMsg.sessionType, sessionId);
		logger.d("notification#notification avatarUrl:%s", avatarUrl);

		ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null, new SimpleImageLoadingListener() {

			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				logger.d("notification#icon onLoadingComplete");
				// holder.image.setImageBitmap(loadedImage);

				showInNotificationBar(latestMsg, sessionId, latestMsg.sessionType, loadedImage, sessionTotalMsgCnt);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				logger.d("notification#icon onLoadingFailed");

				showInNotificationBar(latestMsg, sessionId, latestMsg.sessionType, null, sessionTotalMsgCnt);
			}

		});
	}

	private void showInNotificationBar(MessageEntity msg, String sessionId,
			int sessionType, Bitmap iconBitmap, int sessionTotalMsgCnt) {
		logger.d("notification#showInNotificationBar msg:%s, sessionId:%s, sessionType:%d, sessionTotalMsgCnt:%d", msg, sessionId, sessionType, sessionTotalMsgCnt);

		NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}

		Builder builder = new NotificationCompat.Builder(ctx);
		builder.setContentTitle(getNotificationTitle(msg));
		builder.setContentText(getNotificationContentText(sessionTotalMsgCnt, msg));
		builder.setSmallIcon(R.drawable.tt_logo);
		builder.setTicker(getRollingText(sessionTotalMsgCnt, msg, false));
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);

		// this is the content near the right bottom side
		// builder.setContentInfo("content info");

		if (shouldUseNotificationVibration()) {
			// delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
			long[] vibrate = {0, 200, 250, 200};
			builder.setVibrate(vibrate);
		} else {
			logger.d("notification#setting is not using vibration");
		}

		// sound
		if (shouldUseNotificationSound()) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		} else {
			logger.d("notification#setting is not using sound");
		}
		if (iconBitmap != null) {
			logger.d("notification#fetch icon from network ok");
			builder.setLargeIcon(iconBitmap);
		} else {
			// todo eric default avatar is too small, need big size(128 * 128)
			Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), IMUIHelper.getDefaultAvatarResId(msg.sessionType));
			if (defaultBitmap != null) {
				builder.setLargeIcon(defaultBitmap);
			}
		}

		int notificationId = getSessionNotificationId(sessionId, sessionType);

		Intent intent = new Intent(ctx, MessageActivity.class);
		IMUIHelper.setSessionInIntent(intent, sessionId, sessionType);

		// if MessageActivity is in the background, the system would bring it to
		// the front
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(pendingIntent);

		Notification notification = builder.build();

		notifyMgr.notify(notificationId, notification);
	}

	private String getNotificationContent(MessageEntity msg) {
		// todo eric i18n
		if (msg.isTextType()) {
			return msg.getText();
		} else if (msg.isAudioType()) {
			return "[语音]";
		} else if (msg.isImage()) {
			return "[图片]";
		} else {
			return "错误消息图片";
		}
	}

	private String getRollingText(int sessionTotalMsgCnt, MessageEntity msg,
			boolean noName) {
		String msgContent = getNotificationContent(msg);
		String contactName = msg.fromId;
		ContactEntity contact = IMContactManager.instance().findContact(msg.fromId);
		if (contact == null) {
			logger.e("notification#no contact id:%s", msg.fromId);
		} else {
			contactName = contact.name;
		}

		String unit = ctx.getString(R.string.msg_cnt_unit);
		if (noName) {
			return String.format("[%d%s] %s", sessionTotalMsgCnt, unit, msgContent);
		} else {
			return String.format("[%d%s]%s: %s", sessionTotalMsgCnt, unit, contactName, msgContent);
		}
	}

	private String getNotificationTitle(MessageEntity msg) {
		if (msg.isGroupMsg()) {
			GroupEntity group = IMGroupManager.instance().findGroup(msg.toId);
			if (group == null) {
				logger.e("notification#no such group id:%s", msg.toId);
				return "no such group:" + msg.toId;
			}

			return group.name;
		} else if (msg.isP2PMsg()) {
			ContactEntity contact = IMContactManager.instance().findContact(msg.fromId);
			if (contact == null) {
				logger.e("notification#no such contact id:%s", msg.fromId);
				return "no such contact:" + msg.fromId;
			}

			return contact.name;
		}

		return "wrong message type:" + msg.fromId + " " + msg.toId;
	}

	private String getNotificationContentText(int sessionTotalMsgCnt,
			MessageEntity msg) {
		if (msg.isGroupMsg()) {

			return getRollingText(sessionTotalMsgCnt, msg, false);
		} else {
			return getRollingText(sessionTotalMsgCnt, msg, true);
		}
	}

	@Override
	public void onIMServiceConnected() {
		logger.d("notification#onIMServiceConnected");

	}

	// come from
	// http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
	private long hashBKDR(String str) {
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}

		return hash;
	}

	/* End Of BKDR Hash Function */

	public int getSessionNotificationId(String sessionId, int sessionType) {
		logger.d("notification#getSessionNotificationId sessionId:%s, sessionType:%d", sessionId, sessionType);

		String sessionKey = getSessionKey(sessionId, sessionType);
		int hashedNotificationId = (int) hashBKDR(sessionKey);
		logger.d("notification#hashedNotificationId:%d", hashedNotificationId);

		return hashedNotificationId;
	}

	private String getSessionKey(String sessionId, int sessionType) {
		return String.format("%s_%d", sessionId, sessionType);
	}

	@Override
	public void reset() {
		cancelAllNotifications();
	}

	// private void oldNotification() {
	// Notification notification = new Notification();
	//
	// //notification.icon = IMUIHelper.getDefaultAvatarResId(msg.sessionType);
	// if (icon == null) {
	// logger.e("notification#icon is null");
	// notification.icon = IMUIHelper.getDefaultAvatarResId(msg.sessionType);
	// } else {
	// notification.largeIcon = icon;
	// }
	//
	// //notification.icon = IMUIHelper.getDefaultAvatarResId(msg.sessionType);
	//
	// //delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
	// long[] vibrate = {0, 200, 250, 200};
	// notification.vibrate = vibrate;
	//
	// notification.when = System.currentTimeMillis();
	//
	// notification.flags |= Notification.FLAG_AUTO_CANCEL;
	//
	// // rolling text
	// notification.tickerText = getRollingText(msg, false);
	// notification.defaults = Notification.DEFAULT_SOUND;
	//
	// Intent intent = new Intent(ctx, MessageActivity.class);
	// IMUIHelper.setSessionInIntent(intent, sessionId, sessionType);
	//
	// PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
	// PendingIntent.FLAG_ONE_SHOT);
	// notification.setLatestEventInfo(ctx, getNotificationTitle(msg),
	// getNotificationContentText(msg), pendingIntent);
	// notifyMgr.notify(Integer.parseInt(sessionId), notification);
	// }

}
