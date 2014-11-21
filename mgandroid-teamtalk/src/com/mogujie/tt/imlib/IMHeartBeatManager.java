package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.HeartBeatPacket;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class IMHeartBeatManager extends IMManager implements OnIMServiceListner {
	// todo eric this is not a good way to keep heartbeat

	private static IMHeartBeatManager inst;

	public static IMHeartBeatManager instance() {
		synchronized (IMContactManager.class) {
			if (inst == null) {
				inst = new IMHeartBeatManager();
			}

			return inst;
		}
	}

	private Logger logger = Logger.getLogger(IMHeartBeatManager.class);
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private PendingIntent pi;

//todo eric, ask luoning to support 4 minutes heartbeat
//right now, it will cost the battery really bad
	private final int HEARTBEAT_INTERVAL = 4 * 60 * 1000;
	//private final int HEARTBEAT_INTERVAL = 10 * 1000;

	public void register() {
		logger.d("heartbeat#regisgter");

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_LOGIN_RESULT);
		actions.add(IMActions.ACTION_SERVER_DISCONNECTED);
		actions.add(IMActions.ACTION_SENDING_HEARTBEAT);

		imServiceHelper.registerActions(ctx, actions, IMServiceHelper.INTENT_NO_PRIORITY, this);
	}

	private void reqSendHeartbeat() {
		logger.i("heartbeat#reqSendHeartbeat");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new HeartBeatPacket());

		logger.i("heartbeat#send packet to server");
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		logger.d("heartbeat#onAction action:%s", action);

		if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
			handleLoginResultAction(intent);

		} else if (action.equals(IMActions.ACTION_SERVER_DISCONNECTED)) {
			handleDisconnectServerAction();
		} else if (action.equals(IMActions.ACTION_SENDING_HEARTBEAT)) {
			handleSendingHeartbeart();

		}
	}

	private void handleSendingHeartbeart() {
		logger.d("heartbeat#handleSendingHeartbeart");
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "teamtalk_heartbeat_wakelock");
		wl.acquire();
		try {
			reqSendHeartbeat();
		} catch (Exception e) {
			logger.e("heartbeat#got exception");
		} finally {
			wl.release();
		}
	}
	private void handleDisconnectServerAction() {
		logger.d("heartbeat#handleDisconnectServerAction");
		cancelHeartbeatTimer();
	}

	private void handleLoginResultAction(Intent intent) {
		logger.d("heartbeat#handleLoginResultAction");
		int errorCode = intent.getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

		if (errorCode == ErrorCode.S_OK) {
			onLoginSuccess();
		}
	}

	private void onLoginSuccess() {
		logger.d("heartbeat#onLogin Successful");

		scheduleHeartbeat(HEARTBEAT_INTERVAL);
	}

	private void scheduleHeartbeat(int seconds) {
		logger.d("heartbeat#scheduleHeartbeat every %d seconds", seconds);

		if (pi == null) {
			logger.d("heartbeat#fill in pendingintent");

			Intent intent = new Intent(IMActions.ACTION_SENDING_HEARTBEAT);
			pi = PendingIntent.getBroadcast(ctx, 0, intent, 0);
			if (pi == null) {
				logger.e("heartbeat#pi is null");
				return;
			}
		}

		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pi);
	}

	private void cancelHeartbeatTimer() {
		logger.d("heartbeat#cancelHeartbeatTimer");
		if (pi == null) {
			logger.e("heartbeat#pi is null");
			return;
		}

		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
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
