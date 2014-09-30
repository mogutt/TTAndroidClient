package com.mogujie.tt.imlib;

import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;

import android.app.Application;
import android.content.Intent;

public class IMApplication extends Application {

	private Logger logger = Logger.getLogger(IMApplication.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		logger.i("Application starts");
		
		startIMService();

	}

	private void startIMService() {
		logger.i("start IMService");

		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

}
