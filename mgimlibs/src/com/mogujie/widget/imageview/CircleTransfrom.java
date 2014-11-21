package com.mogujie.widget.imageview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.squareup.picasso.Transformation;

/**
 * 
 * @author dolphinWang
 * @time 2014/03/10
 */
class CircleTransfrom implements Transformation {

    @Override
    public Bitmap transform(Bitmap bitmap) {

        if (null == bitmap)
            return null;

        PaintFlagsDrawFilter fliter = new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }
        int r = (w < h ? w : h) / 2;
        Bitmap circle = Bitmap.createBitmap(2 * r, 2 * r,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circle);
        canvas.setDrawFilter(fliter);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        p.setColor(Color.RED);
        canvas.drawCircle(r, r, r, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // 生成结果图片
        Bitmap out = Bitmap.createBitmap(2 * r, 2 * r, Bitmap.Config.ARGB_8888);
        Canvas outCan = new Canvas(out);
        outCan.setDrawFilter(fliter);
        
        outCan.drawBitmap(bitmap, 0, 0, null);
        outCan.drawBitmap(circle, 0, 0, p);
        circle.recycle();
        bitmap.recycle();
        return out;
    }

    @Override
    public String key() {
        return null;
    }
}
