package com.mogujie.widget.imageview;

import android.content.Context;
import android.util.AttributeSet;

public class MGWebImageviewWithFixRatio extends MGWebImageView {

	private int mWidth = 0;
	private int mHeight = 0;
	private boolean isWidthFixMode;

	public MGWebImageviewWithFixRatio(Context context) {
		super(context);
	}

	public MGWebImageviewWithFixRatio(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (isWidthFixMode) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = 0;
			if (mWidth != 0) {
				height = width * mHeight / mWidth;
			}
			setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void setAspectRatio(int width, int height) {
		this.isWidthFixMode = true;
		this.mWidth = width;
		this.mHeight = height;
	}
}
