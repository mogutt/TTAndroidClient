package com.mogujie.widget.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * 圆形ImageView
 *
 * @author dolphinWang 2014/02/16
 *
 */
public class MGWebCircleImageView extends MGWebImageView implements
		Transformation {

	public MGWebCircleImageView(Context context) {
		this(context, null);
	}

	public MGWebCircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setImageUrl(String url) {
		if (!allowedUrl(url)) {
			return;
		}

		mUrl = url;

		if (isAttachedOnWindow) {
			beginProcess(Picasso.with(getContext()).load(url).transform(this));
		}
	}

	@Override
	public void setImageUrlNeedFit(String url) {
		throw new RuntimeException(
				"MGWebCircleImageView can not use method setImageUrlNeedFit");
	}

	@Override
	public void setImageUrlNeedResize(String url, int width, int height) {
		throw new RuntimeException(
				"MGWebCircleImageView can not use method setImageUrlNeedResize");
	}

	@Override
	public Bitmap transform(Bitmap bitmap) {

		if (null == bitmap)
			return null;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (w <= 0 || h <= 0) {
			return null;
		}
		int r = (w < h ? w : h) / 2;
		Bitmap circle = Bitmap.createBitmap(2 * r, 2 * r,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(circle);
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(Color.RED);
		canvas.drawCircle(r, r, r, p);
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		// 生成结果图片
		Bitmap out = Bitmap.createBitmap(2 * r, 2 * r, Bitmap.Config.ARGB_8888);
		Canvas outCan = new Canvas(out);
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
