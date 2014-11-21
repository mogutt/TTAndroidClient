
package com.mogujie.tt.ui.tools;

import com.mogujie.tt.utils.CommonUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;

public class BubbleImageHelper {
    private Context context = null;
    private static BubbleImageHelper instance = null;

    public static synchronized BubbleImageHelper getInstance(Context c) {
        if (null == instance) {
            instance = new BubbleImageHelper(c);
        }
        return instance;
    }

    private BubbleImageHelper(Context c) {
        context = c;
    }

    private Bitmap getScaleImage(Bitmap bitmap, float width, float height) {
        if (null == bitmap || width < 0.0f || height < 0.0f) {
            return null;
        }
        Matrix matrix = new Matrix();
        float scaleWidth = width / bitmap.getWidth();
        float scaleHeight = height / bitmap.getHeight();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    public Bitmap getBubbleImageBitmap(Bitmap srcBitmap,
            int backgroundResourceID) {
        if (null == srcBitmap) {
            return null;
        }
        Bitmap background = null;
        background = BitmapFactory.decodeResource(context.getResources(),
                backgroundResourceID);
        if (null == background) {
            return null;
        }

        Bitmap mask = null;
        Bitmap newBitmap = null;
        mask = srcBitmap;

        float srcWidth = (float) srcBitmap.getWidth();
        float srcHeight = (float) srcBitmap.getHeight();
        if (srcWidth < (float) CommonUtil.getImageMessageItemMinWidth(context)
                && srcHeight < (float) CommonUtil
                        .getImageMessageItemMinHeight(context)) {
            srcWidth = CommonUtil.getImageMessageItemMinWidth(context);
            srcHeight = (float) CommonUtil
                    .getImageMessageItemMinHeight(context);
            Bitmap tmp = getScaleImage(background, srcWidth, srcHeight);
            if (null != tmp) {
                background = tmp;
            } else {
                tmp = getScaleImage(srcBitmap,
                        (float) CommonUtil
                                .getImageMessageItemDefaultWidth(context),
                        (float) CommonUtil
                                .getImageMessageItemDefaultHeight(context));
                if (null != tmp) {
                    mask = tmp;
                }
            }
        }

        Config config = background.getConfig();
        if (null == config) {
            config = Bitmap.Config.ARGB_8888;
        }

        newBitmap = Bitmap.createBitmap(background.getWidth(),
                background.getHeight(), config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(background, 0, 0, null);

        Paint paint = new Paint();

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        int left = 0;
        int top = 0;
        int right = mask.getWidth();
        int bottom = mask.getHeight();
        if (mask.getWidth() > background.getWidth()) {
            left = (mask.getWidth() - background.getWidth()) / 2;
            right = mask.getWidth() - left;
        }

        if (mask.getHeight() > background.getHeight()) {
            top = (mask.getHeight() - background.getHeight()) / 2;
            bottom = mask.getHeight() - top;
        }

        newCanvas.drawBitmap(mask, new Rect(left, top, right, bottom),
                new Rect(0, 0, background.getWidth(), background.getHeight()),
                paint);

        return newBitmap;
    }
}
