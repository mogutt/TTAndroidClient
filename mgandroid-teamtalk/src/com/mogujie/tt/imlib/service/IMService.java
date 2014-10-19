package com.mogujie.tt.imlib.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.mogujie.tt.imlib.IMConfigurationManager;
import com.mogujie.tt.imlib.IMContactManager;
import com.mogujie.tt.imlib.IMGroupManager;
import com.mogujie.tt.imlib.IMHeartBeatManager;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.IMMessageManager;
import com.mogujie.tt.imlib.IMNotificationManager;
import com.mogujie.tt.imlib.IMRecentSessionManager;
import com.mogujie.tt.imlib.IMReconnectManager;
import com.mogujie.tt.imlib.IMUnAckMsgManager;
import com.mogujie.tt.imlib.IMUnreadMsgManager;
import com.mogujie.tt.imlib.db.IMDbManager;
import com.mogujie.tt.log.Logger;

public class IMService extends Service {

	private Logger logger = Logger.getLogger(IMService.class);
	private IMServiceBinder binder = new IMServiceBinder();

	//hold the references
	@SuppressWarnings("unused")
	private IMLoginManager loginMgr = getLoginManager();
	@SuppressWarnings("unused")
	private IMContactManager contactMgr = getContactManager();
	@SuppressWarnings("unused")
	private IMGroupManager groupMgr = getGroupManager();
	@SuppressWarnings("unused")
	private IMMessageManager messageMgr = getMessageManager();
	@SuppressWarnings("unused")
	private IMRecentSessionManager recentSessionMgr = getRecentSessionManager();
	@SuppressWarnings("unused")
	private IMReconnectManager reconnectMgr = getReconnectManager();
	
	//this manager constructor needs context, put it off when context is valid
	@SuppressWarnings("unused")
	private IMDbManager dbMgr;
	@SuppressWarnings("unused")
	private IMHeartBeatManager heartBeatMgr = getHeartBeatManager();
	@SuppressWarnings("unused")
	private IMUnAckMsgManager unAckMsgMgr = getUnAckMsgManager();
	@SuppressWarnings("unused")
	private IMUnreadMsgManager unReadMsgMgr = getUnReadMsgManager();
	@SuppressWarnings("unused")
	private IMNotificationManager notificationMgr = getNotificationManager();
	@SuppressWarnings("unused")
	private IMConfigurationManager configMgr = getConfigManager();

	public class IMServiceBinder extends Binder {
		public IMService getService() {
			return IMService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		logger.i("IMService onBind");

		return binder;
	}

	@Override
	public void onCreate() {
		logger.i("IMService onCreate");

		super.onCreate();
		
		//make the service foreground, so stop "360 yi jian qingli"(a clean tool) to stop our app
		//todo eric study wechat's mechanism, use a better solution
		startForeground((int)System.currentTimeMillis(),new Notification());
	}

	@Override
	public void onDestroy() {
		logger.i("IMService onDestroy");

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.i("IMService onStartCommand");

		Context ctx = getApplicationContext();
		IMLoginManager.instance().setContext(ctx);
		IMContactManager.instance().setContext(ctx);
		IMMessageManager.instance().setContext(ctx);
		IMGroupManager.instance().setContext(ctx);
		IMRecentSessionManager.instance().setContext(ctx);
		IMReconnectManager.instance().setContext(ctx);
		IMHeartBeatManager.instance().setContext(ctx);
		IMUnAckMsgManager.instance().setContext(ctx);
		IMUnreadMsgManager.instance().setContext(ctx);
		IMNotificationManager.instance().setContext(ctx);
		IMConfigurationManager.instance().setContext(ctx);

		dbMgr = getDbManager();

		IMReconnectManager.instance().register();
		IMHeartBeatManager.instance().register();
		IMNotificationManager.instance().register();
		IMUnAckMsgManager.instance().startUnAckTimeoutTimer();

		return START_STICKY;
	}

	public IMLoginManager getLoginManager() {
		logger.d("getLoginManager");

		return IMLoginManager.instance();
	}

	public IMContactManager getContactManager() {
		logger.d("getContactManager");

		return IMContactManager.instance();
	}

	public IMMessageManager getMessageManager() {
		logger.d("getMessageManager");

		return IMMessageManager.instance();
	}

	public IMHeartBeatManager getHeartBeatManager() {
		logger.d("getHeartBeatManager");

		return IMHeartBeatManager.instance();
	}

	public IMDbManager getDbManager() {
		logger.d("getDbManager");

		return IMDbManager.instance(getApplicationContext());
	}

	public IMGroupManager getGroupManager() {
		logger.d("getGroupManager");

		return IMGroupManager.instance();
	}

	public IMRecentSessionManager getRecentSessionManager() {
		logger.d("getRecentSessionManager");

		return IMRecentSessionManager.instance();
	}

	public IMReconnectManager getReconnectManager() {
		logger.d("getReconnectManager");

		return IMReconnectManager.instance();
	}

	public IMUnAckMsgManager getUnAckMsgManager() {
		logger.d("getUnAckMsgManager");

		return IMUnAckMsgManager.instance();
	}

	public IMUnreadMsgManager getUnReadMsgManager() {
		logger.d("getUnReadMsgManager");

		return IMUnreadMsgManager.instance();
	}

	public IMNotificationManager getNotificationManager() {
		logger.d("getNotificationManager");

		return IMNotificationManager.instance();
	}
	
	public IMConfigurationManager getConfigManager() {
		logger.d("getConfigManager");

		return IMConfigurationManager.instance();
	}
}
