/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The default {@link java.util.concurrent.ExecutorService} used for new
 * {@link Picasso} instances.
 * <p/>
 * Exists as a custom type so that we can differentiate the use of defaults
 * versus a user-supplied instance.
 * 
 * 感觉单线程比较快所以我们就用了单线程
 */
class PicassoExecutorService extends ThreadPoolExecutor {
	private static final int DEFAULT_THREAD_COUNT = 3;

	@SuppressWarnings("unused")
	private static final int WIFI_NETWORK_THREAD_SIZE = 4;
	@SuppressWarnings("unused")
	private static final int FAST_MOBILE_NETWORK_THREAD_SIZE = 3;
	private static final int MEDIUM_MOBILE_NETWORK_THREAD_SIZE = 2;
	private static final int LOW_MOBILE_NETWORK_THREAD_SIZE = 1;

	PicassoExecutorService() {
		super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				new Utils.PicassoThreadFactory());
	}

	@TargetApi(Build.VERSION_CODES.CUPCAKE)
	void adjustThreadCount(NetworkInfo info) {
		if (info == null || !info.isConnectedOrConnecting()) {
			setThreadCount(DEFAULT_THREAD_COUNT);
			return;
		}
		switch (info.getType()) {
		case ConnectivityManager.TYPE_WIFI:
		case ConnectivityManager.TYPE_WIMAX:
		case ConnectivityManager.TYPE_ETHERNET:
			setThreadCount(MEDIUM_MOBILE_NETWORK_THREAD_SIZE);
			break;
		case ConnectivityManager.TYPE_MOBILE:
			switch (info.getSubtype()) {
			case TelephonyManager.NETWORK_TYPE_LTE: // 4G
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				setThreadCount(MEDIUM_MOBILE_NETWORK_THREAD_SIZE);
				break;
			case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				setThreadCount(LOW_MOBILE_NETWORK_THREAD_SIZE);
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
			case TelephonyManager.NETWORK_TYPE_EDGE:
				setThreadCount(LOW_MOBILE_NETWORK_THREAD_SIZE);
				break;
			default:
				setThreadCount(DEFAULT_THREAD_COUNT);
			}
			break;
		default:
			setThreadCount(DEFAULT_THREAD_COUNT);
		}
	}

	private void setThreadCount(int threadCount) {
		setCorePoolSize(threadCount);
		setMaximumPoolSize(threadCount);
	}
}
