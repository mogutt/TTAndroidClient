package com.mogujie.tt.imlib;

import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.app.Application;
import android.content.Context;
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
		
		initImageLoader(getApplicationContext());
	}

	private void startIMService() {
		logger.i("start IMService");

		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

	private static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(200 * 1024 * 1024) 
				.tasksProcessingOrder(QueueProcessingType.LIFO)
//				.writeDebugLogs() // todo eric Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
