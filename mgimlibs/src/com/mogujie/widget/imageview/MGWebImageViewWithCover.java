/**
 * Filename:    MGWebImageViewWithCover.java  
 * Description:   
 * @author:     shangqu  
 * @version:    1.0  
 * Create at:   2014-1-23 下午3:24:30   
 */

package com.mogujie.widget.imageview;


import com.mogujie.im.libs.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


/**
 * @author dolphinWang
 * @time 2014/01/23
 */
public class MGWebImageViewWithCover extends MGWebImageView {

	private Drawable mCover;

	private static final Drawable mBlankCover = new ColorDrawable(Color.argb(0,
			0, 0, 0));

	/**
	 * @param context
	 */
	public MGWebImageViewWithCover(Context context) {
		this(context, null);
	}

	public MGWebImageViewWithCover(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.MGWebImageViewWithCover);

			mCover = a.getDrawable(R.styleable.MGWebImageViewWithCover_cover);

			a.recycle();
		} else {
			mCover = mBlankCover;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 在这里给imageview上面盖一层cover
		// paddingRight留出来给shadow
		if (mCover != null) {
			mCover.setBounds(getPaddingLeft(), getPaddingTop(), getWidth()
					- getPaddingLeft(), getHeight() - getPaddingTop()
					- getPaddingBottom());
			mCover.draw(canvas);
		}
	}

	public final void setCover(Drawable cover) {
		if (cover == null) {
			mCover = mBlankCover;
		} else {
			mCover = cover;
		}

		postInvalidate();
	}

	public final void setCover(int resID) {
		if (resID < 0) {
			mCover = mBlankCover;
		} else {
			mCover = getResources().getDrawable(resID);
		}
		
		postInvalidate();
	}
}
