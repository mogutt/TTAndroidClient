package com.mogujie.tt.imlib.utils;

import java.util.List;

import com.mogujie.tt.log.Logger;

public class DumpUtils {
	public static void dumpStringList(Logger logger, String desc, List<String> memberList) {
		String log = String.format("%s, members:", desc);
		for (String memberId : memberList) {
			log += memberId + ",";
		}

		logger.d("%s", log);
	}
}
