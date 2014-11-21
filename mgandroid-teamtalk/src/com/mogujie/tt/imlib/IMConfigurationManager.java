package com.mogujie.tt.imlib;

import com.mogujie.tt.imlib.db.IMDbManager;
import com.mogujie.tt.log.Logger;

public class IMConfigurationManager extends IMManager {
	private static IMConfigurationManager inst;

	private Logger logger = Logger.getLogger(IMConfigurationManager.class);

	public static IMConfigurationManager instance() {
		synchronized (IMConfigurationManager.class) {
			if (inst == null) {
				inst = new IMConfigurationManager();
			}

			return inst;
		}
	}
	
//todo eric use cache, don't read it from db everytime
//	private Map<String, Map<String, String>> cache = new HashMap<String, Map<String,String>>();
//	
//	String cacheGet(String category, String key) {
//		
//	}
	
	public String get(String category, String key, String defaultValue) {
		logger.d("config#get -> category:%s, key:%s, defaultValue:%s", category, key, defaultValue);
		
//		String value = cacheGet(category, key);
//		if (value != null) {
//			return value;
//		}
		
//		logger.d("config#no cache");
		
		String value = IMDbManager.instance(ctx).getConfiguration(category, key);
		
		logger.d("config#return value:%s", value);
		
		return value == null ? defaultValue : value;
	}
	
	public boolean getBoolean(String category, String key, boolean defaultValue) {
		String value = get(category, key, booleanKey(defaultValue));
		if (value.equals("0")) {
			return false;
		} else {
			return true;
		}
	}
	
	public void set(String category, String key, String value) {
		logger.d("config#set -> category:%s, key:%s, value:%s", category, key, value);
		
		IMDbManager.instance(ctx).updateConfiguration(category, key, value);
	}
	
	
	public void setBoolean(String category, String key, boolean value) {
		logger.d("config#set -> category:%s, key:%s, value:%s", category, key, value);
		
		IMDbManager.instance(ctx).updateConfiguration(category, key, booleanKey(value));
	}
	
	
	public static String booleanKey(boolean value) {
		if (value) {
			return "1";
		} else {
			return "0";
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
