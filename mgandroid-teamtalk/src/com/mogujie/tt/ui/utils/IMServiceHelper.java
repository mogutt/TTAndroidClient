package com.mogujie.tt.ui.utils;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMContactManager;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.service.IMService.IMServiceBinder;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.base.TTBaseFragment;

public class IMServiceHelper {
	public static final int INTENT_NO_PRIORITY = -1;
	public static final int INTENT_MAX_PRIORITY = Integer.MAX_VALUE;

	public interface OnIMServiceListner {
		void onAction(String action, Intent intent,
				BroadcastReceiver broadcastReceiver);

		void onIMServiceConnected();
	}

	private class IMBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			logger.d("im#receive action:%s", action);

			IMServiceHelper.this.listener.onAction(action, intent, this);
		}
	}

	protected static Logger logger = Logger.getLogger(IMServiceHelper.class);
	private IMBroadcastReceiver imReceiver = new IMBroadcastReceiver();
	private IMService imService;
	public OnIMServiceListner listener;
	private boolean registered = false;

	public IMService getIMService() {
		return imService;
	}

	// todo eric when to release?
	private ServiceConnection imServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// todo eric when to unbind the service?
			// TODO Auto-generated method stub
			logger.i("onService(imService)Disconnected");

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			logger.i("im#onService(imService)Connected");

			if (imService == null) {
				IMServiceBinder binder = (IMServiceBinder) service;
				imService = binder.getService();

				if (imService == null) {
					logger.e("im#get imService failed");
					return;
				}

				logger.d("im#get imService ok");
			}

			listener.onIMServiceConnected();
		}
	};

	public boolean connect(Context ctx, List<String> actions,
			int intentPriority, OnIMServiceListner actionlistener) {
		registerActions(ctx, actions, intentPriority, actionlistener);
		return bindService(ctx);
	}

	public void disconnect(Context ctx) {
		logger.d("im#disconnect:%s", imReceiver);
		unregisterActions(ctx);
		unbindService(ctx);
	}

	// todo eric could cause cyclic reference
	public void registerActions(Context ctx, List<String> actions,
			int intentPriority, OnIMServiceListner actionlistener) {
		logger.d("im#registerActions");
		listener = actionlistener;

		if (actions != null) {
			IntentFilter intentFilter = new IntentFilter();

			if (intentPriority != INTENT_NO_PRIORITY) {
				intentFilter.setPriority(intentPriority);

				logger.d("im#setPriority:%d", intentPriority);
			}

			for (String action : actions) {
				intentFilter.addAction(action);
			}

			// todo eric check return value
			logger.d("im#registerReceiver imReceiver:%s", imReceiver);

			ctx.registerReceiver(imReceiver, intentFilter);

			registered = true;

		}

	}

	public void unregisterActions(Context ctx) {
		if (registered) {
			registered = false;
			logger.d("im#unregisterReceiver imReceiver:%s", imReceiver);

			try {
				ctx.unregisterReceiver(imReceiver);
			} catch (IllegalArgumentException exception) {
				logger.w("im#got exception becuase of unmatched reg/unreg, we sould place to onStop next version.e:%s", exception.getMessage());
			}
		}
	}

	public boolean bindService(Context ctx) {
		logger.d("im#bindService");

		Intent intent = new Intent();
		intent.setClass(ctx, IMService.class);

		if (!ctx.bindService(intent, imServiceConnection, android.content.Context.BIND_AUTO_CREATE)) {
			logger.e("im#bindService(imService) failed");
			return false;
		} else {
			logger.i("im#bindService(imService) ok");
			return true;
		}
	}

	public void unbindService(Context ctx) {

		try {
			// todo eric .check the return value .check the right place to call it
			ctx.unbindService(imServiceConnection);
		} catch (IllegalArgumentException exception) {
			logger.w("im#got exception becuase of unmatched bind/unbind, we sould place to onStop next version.e:%s", exception.getMessage());
		}

		logger.i("unbindservice ok");
	}

}
