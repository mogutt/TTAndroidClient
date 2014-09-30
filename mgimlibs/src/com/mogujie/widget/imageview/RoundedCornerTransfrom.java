package com.mogujie.widget.imageview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * 
 * @author dolphinWang
 * @time 2014/03/10
 */
class RoundedCornerTransfrom implements Transformation {

	private int mCornerSize ;
	
	public RoundedCornerTransfrom(int cornerSize){
		this.mCornerSize = cornerSize;
	}

	@Override
	public Bitmap transform(Bitmap source) {
		if (source == null) {
			return null;
		}

		int w = source.getWidth(), h = source.getHeight();
		if (w <= 0 || h <= 0) {
			return null;
		}

		// 生成圆角
		Bitmap round = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(round);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
		canvas.drawRoundRect(new RectF(0, 0, w, h), mCornerSize, mCornerSize,
				paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		// 生成结果图片
		Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas outCanvas = new Canvas(out);
		outCanvas.drawBitmap(source, 0, 0, null);
		outCanvas.drawBitmap(round, 0, 0, paint);
		source.recycle();
		round.recycle();

		return out;
	}

	@Override
	public String key() {
		return null;
	}
	
	public void setCornerSize(int cornerSize) {
        mCornerSize = cornerSize;
    }
}
