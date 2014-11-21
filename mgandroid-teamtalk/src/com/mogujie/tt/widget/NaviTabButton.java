package com.mogujie.tt.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.activity.MainActivity;

public class NaviTabButton extends FrameLayout {
	private int mIndex;

	private ImageView mImage;
	private TextView mTitle;
	private TextView mNotify;

	private Drawable mSelectedImg;
	private Drawable mUnselectedImg;

	private Context mContext;

	private Logger logger = Logger.getLogger(NaviTabButton.class);

	public NaviTabButton(Context context) {
		this(context, null);
	}

	public NaviTabButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NaviTabButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.mContext = context;

		View.OnClickListener clickListner = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    if (mIndex==0) {
			        ((MainActivity) mContext).btnChatClick();
                }else{
                    ((MainActivity) mContext).setFragmentIndicator(mIndex);
                }
			}
		};

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.tt_navi_tab_button, this, true);
		RelativeLayout container = (RelativeLayout) findViewById(R.id.tab_btn_container);

		mImage = (ImageView) findViewById(R.id.tab_btn_default);
		mTitle = (TextView) findViewById(R.id.tab_btn_title);
		mNotify = (TextView) findViewById(R.id.tab_unread_notify);

		container.setOnClickListener(clickListner);
	}

	public void setIndex(int index) {
		this.mIndex = index;
	}

	public void setUnselectedImage(Drawable img) {
		this.mUnselectedImg = img;
	}

	public void setSelectedImage(Drawable img) {
		this.mSelectedImg = img;
	}

	private void setSelectedColor(Boolean selected) {
		if (selected) {
			mTitle.setTextColor(getResources().getColor(
					R.color.default_blue_color));
		} else {
			mTitle.setTextColor(getResources().getColor(
					R.color.default_light_grey_color));
		}
	}

	public void setSelectedButton(Boolean selected) {
		setSelectedColor(selected);
		if (selected) {
			mImage.setImageDrawable(mSelectedImg);
		} else {
			mImage.setImageDrawable(mUnselectedImg);
		}
	}

	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setUnreadNotify(int unreadNum) {
		logger.d("unread#setUreadNotify -> unreadNum:%d", unreadNum);
		if (0 == unreadNum) {
			mNotify.setVisibility(View.INVISIBLE);
			return;
		}

		String notify;
		if (unreadNum > 99) {
			notify = "99+";
		} else {
			notify = Integer.toString(unreadNum);
		}

		mNotify.setText(notify);
		mNotify.setVisibility(View.VISIBLE);
	}

}
