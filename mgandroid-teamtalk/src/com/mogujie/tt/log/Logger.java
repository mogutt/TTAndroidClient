package com.mogujie.tt.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mogujie.tt.imlib.db.IMDbManager;

import android.content.Context;
import android.util.Log;

public class Logger {
	/**
	 * log tag
	 */
	private String tagName = "MoGuLogger";// tag name

//	private static int logLevel = Log.VERBOSE;

	private static int logLevel = Log.WARN;

	private static Logger inst;

	private Lock lock;

	private Logger() {
		lock = new ReentrantLock();
	}

	public static synchronized Logger getLogger(Class<?> key) {
		if (inst == null) {
			inst = new Logger();
		}

		return inst;
	}

	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}

			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}

			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}

			return "[" + st.getFileName() + ":" + st.getLineNumber() + "]";
		}

		return null;
	}

	private String createMessage(String msg) {
		String functionName = getFunctionName();
		long threadId = Thread.currentThread().getId();
		String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date());
		String message = (functionName == null ? msg : (functionName + " - "
				+ String.valueOf(threadId) + " - " + msg));
		return currentTime + " - " + message;
	}

	/**
	 * log.i
	 */
	public void i(String format, Object... args) {
		if (logLevel <= Log.INFO) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.i(tagName, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.v
	 */
	public void v(String format, Object... args) {
		if (logLevel <= Log.VERBOSE) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.v(tagName, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.d
	 */
	public void d(String format, Object... args) {
		if (logLevel <= Log.DEBUG) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.d(tagName, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.e
	 */
	public void e(String format, Object... args) {
		if (logLevel <= Log.ERROR) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.e(tagName, message);
			} finally {
				lock.unlock();
			}
		}
	}
	private String getInputString(String format, Object... args) {
		if (format == null) {
			return "null log format";
		}

		return String.format(format, args);
	}

	/**
	 * log.error
	 */
	public void error(Exception e) {
		if (logLevel <= Log.ERROR) {
			StringBuffer sb = new StringBuffer();
			lock.lock();
			try {
				String name = getFunctionName();
				StackTraceElement[] sts = e.getStackTrace();

				if (name != null) {
					sb.append(name + " - " + e + "\r\n");
				} else {
					sb.append(e + "\r\n");
				}
				if (sts != null && sts.length > 0) {
					for (StackTraceElement st : sts) {
						if (st != null) {
							sb.append("[ " + st.getFileName() + ":"
									+ st.getLineNumber() + " ]\r\n");
						}
					}
				}
				Log.e(tagName, sb.toString());
			} finally {
				lock.unlock();
			}
		}
	}
	/**
	 * log.d
	 */
	public void w(String format, Object... args) {
		if (logLevel <= Log.WARN) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.w(tagName, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * set log level
	 */

	public void setLevel(int l) {
		lock.lock();
		try {
			logLevel = l;
		} finally {
			lock.unlock();
		}
	}
}
