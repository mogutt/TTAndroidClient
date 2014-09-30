package com.mogujie.tt.imlib;

import android.content.Context;

public class IMManager {

	protected Context ctx;

	public void setContext(Context context) {
		if (context == null) {
			throw new RuntimeException("context is null");
		}
		
		ctx = context;
	}
}
