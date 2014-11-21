package com.mogujie.tt.imlib.utils;

import java.util.List;

import android.util.Log;

import com.mogujie.tt.log.Logger;

public class DumpUtils {
	public static void dumpStringList(Logger logger, String desc,
			List<String> memberList) {
		String log = String.format("%s, members:", desc);
		for (String memberId : memberList) {
			log += memberId + ",";
		}

		logger.d("%s", log);
	}

	//oneLine for purpose of "tail -f", so you can track them at one line
	public static void dumpStacktrace(Logger logger, String desc,
			boolean oneLine) {
		String stackTraceString = Log.getStackTraceString(new Throwable());

		if (oneLine) {
			stackTraceString = stackTraceString.replace("\n", "####");
		}
		
		logger.d("%s:%s", desc, stackTraceString);
	}
}
