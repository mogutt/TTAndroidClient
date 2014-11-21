package com.mogujie.tt.ui.tools;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * @Description 用于预览相关图片缓存
 * @author Nana
 * @date 2014-8-4
 */
//todo eric remove this shit
public class DisplayBitmapCache {
	private static DisplayBitmapCache instance = null;
	private Context context = null;

	public static synchronized DisplayBitmapCache getInstance(Context c) {
		if (null == instance) {
			instance = new DisplayBitmapCache(c);
		}
		return instance;
	}

	private DisplayBitmapCache(Context c) {
		context = c;
	}

	public Bitmap get(String path) {
		Bitmap bmp = ImageTool.getBigBitmapForDisplay(path, context);
		return bmp;
	}
}
