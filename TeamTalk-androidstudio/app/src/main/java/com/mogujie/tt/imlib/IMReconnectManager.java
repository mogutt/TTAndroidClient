package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class IMReconnectManager extends IMManager implements OnIMServiceListner {

	private static IMReconnectManager inst;
	private Logger logger = Logger.getLogger(IMReconnectManager.class);
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private boolean reconnecting = false;

	private final int INIT_RECONNECT_INTERVAL_SECONDS = 3;
	private int reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
	private final int MAX_RECONNECT_INTERVAL_SECONDS = 60;

	public static IMReconnectManager instance() {
		synchronized (IMReconnectManager.class) {
			if (inst == null) {
				inst = new IMReconnectManager();
			}

			return inst;
		}
	}

	private IMReconnectManager() {
	}

	public void register() {
		logger.d("reconnect#regisgter");

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_LOGIN_RESULT);
		actions.add(IMActions.ACTION_SERVER_DISCONNECTED);
		actions.add(IMActions.ACTION_RECONNECT);
		actions.add(ConnectivityManager.CONNECTIVITY_ACTION);

		imServiceHelper.registerActions(ctx, actions, IMServiceHelper.INTENT_NO_PRIORITY, this);
	}

	private void scheduleReconnect(int seconds) {
		logger.d("reconnect#scheduleReconnect after %d seconds", seconds);

		Intent intent = new Intent(IMActions.ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent, 0);
		if (pi == null) {
			logger.e("reconnect#pi is null");
			return;
		}

		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds
				* 1000, pi);
	}

	private void resetReconnectTime() {
		logger.d("reconnect#resetReconnectTime");
		reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
	}

	public void reconnect() {
		logger.i("reconnect#reconnect");
		if (reconnecting) {
			logger.d("reconnect#it's already doing reconnect");
			return;
		}

		logger.d("reconnect#reconnect the server");

		if (!IMLoginManager.instance().isEverLogined()) {
			logger.d("reconnect#not everlogined before, no need to do reconnect");
			return;
		}

		// so reconnect will use the initial reconnecting time
		resetReconnectTime();

		if (IMLoginManager.instance().relogin()) {
			reconnecting = true;
			logger.d("reconnect#start reconnecting");
		}
	}

	private void onLoginSuccess() {
		logger.d("reconnect#onLogin Successful");
		reconnecting = false;
	}

	private void onLoginFailed() {
		logger.d("reconnect#onLoginFailed");

		if (reconnecting) {
			logger.d("reconnect#in reconnecting procedure");

			scheduleReconnect(reconnectInterval);
			incrementReconnectInterval();
		}
	}

	private void incrementReconnectInterval() {
		if (reconnectInterval >= MAX_RECONNECT_INTERVAL_SECONDS) {
			reconnectInterval = MAX_RECONNECT_INTERVAL_SECONDS;
		} else {
			reconnectInterval = reconnectInterval * 2;
		}
	}

	private void handleLoginResultAction(Intent intent) {
		logger.d("reconnect#handleLoginResultAction");
		int errorCode = intent.getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

		if (errorCode == ErrorCode.S_OK) {
			onLoginSuccess();
		} else {
			onLoginFailed();
		}
	}

	private void handleDisconnectServerAction(Intent intent) {
		logger.d("reconnect#handleDisconnectServerAction");
		if (isNetworkAvailable(intent)) {
			logger.d("reconnect#disconnect with the server, network is available, reconnect");
			reconnect();
		} else {
			logger.d("reconnect#network is unavailable, no need to reconnect");
		}
	}

	private boolean isNetworkAvailable(Intent intent) {
		boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		return !noConnectivity;
	}

	private void handleNetworkActivityChangedAction(Intent intent) {
		logger.d("reconnect#handleNetworkActivityChangedAction");

		if (isNetworkAvailable(intent)) {
			logger.d("reconnect#network is available");
			reconnect();
		} else {
			logger.d("reconnect#network is unavailable");
		}

	}

	private void handleReconnectServer() {
		logger.d("reconnect#handleReconnectServer");

		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "teamtalk_reconnecting_wakelock");
		wl.acquire();

		try {
			IMLoginManager.instance().relogin();
		} finally {
			wl.release();
		}
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		logger.d("reconnect#onAction action:%s", action);

		if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
			handleLoginResultAction(intent);

		} else if (action.equals(IMActions.ACTION_SERVER_DISCONNECTED)) {
			handleDisconnectServerAction(intent);

		} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			handleNetworkActivityChangedAction(intent);
		} else if (action.equals(IMActions.ACTION_RECONNECT)) {
			handleReconnectServer();
		}
	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}