package com.mogujie.widget.imageview;

import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.mogujie.im.libs.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

/**
 * Created by 6a209 on 9/11/13.
 * 
 * @modify dolphinWang on 2014/02/07 不再使用Volley, 改用Picasso为图片下载与缓存框架
 * @modify dolphinWang on 2014/02/08 改变picasso的使用方式，提供全局统一的picasso对象
 * @modify dolphinWang on 2014/02/12
 *         添加setImageUrlNeedResize函数，因为某些需求要求图片等高，坑爹的是还需要画一个cover上去
 *         ，ImageView的centerCrop模式又会忽略padding
 * @modify dolphinWang on 2014/02/14 1.根据Picasso load(String
 *         url)函数的url检测做出代码外部的url拦截操作。 2.Fetch单图成功之后，在map中删掉这张bitmap减小内存占用。
 */
public class MGWebImageView extends ImageView {

    @SuppressWarnings("unused")
	private int mPlaceholder = -1;
    protected String mUrl;

    private Drawable mDefaultDrawable;

    private boolean needFit;

    private boolean needResize;

    private int targetWidth;

    private int targetHeight;

    protected boolean isAttachedOnWindow;

    private Transformation mTransformation;

    /**
     * 我们需要hold住一个target的强引用，以免被GC释放掉
     */
    private static HashMap<String, Target> mTargetMap;

    public MGWebImageView(Context context) {
        this(context, null);
    }

    public MGWebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getConfiguration(context, attrs);
    }

    private void getConfiguration(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MGWebImageView);

        mTransformation = BitmapUtils.get(a.getInt(
                R.styleable.MGWebImageView_shape, -1));

        if (mTransformation instanceof RoundedCornerTransfrom) {
            int cornerSize = (int) a.getDimension(
                    R.styleable.MGWebImageView_cornerSize, -1);
			if (cornerSize != -1) {
				((RoundedCornerTransfrom) mTransformation)
						.setCornerSize(cornerSize);
			}
		}

		a.recycle();
	}

	protected void beginProcess(RequestCreator creator) {

		if (needFit && needResize) {
			throw new IllegalArgumentException(
					"fit and resize can not be use in same time!");
		}

		if (needResize && (targetWidth == 0 || targetHeight == 0)) {
			throw new IllegalArgumentException(
					"You need resize the bitmap but set target width(target height) zero. Do you really want do this?");
		}

		// fit和resize不应该同时出现
		if (needFit) {
			creator.fit();
		} else if (needResize) {
			creator.resize(targetWidth, targetHeight).centerCrop();
		}

		if (mDefaultDrawable != null) {
			creator.placeholder(mDefaultDrawable).error(mDefaultDrawable);
		}

		if (mTransformation != null) {
			creator.transform(mTransformation);
		}
		
		// 3.0一下取消fade
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            creator.noFade();
        }

		creator.into(this);
	}

	public String getImageUrl() {
		return mUrl;
	}

	public void setImageUrl(String url) {
		if (!allowedUrl(url)) {
			return;
		}

		mUrl = url;

		// 如果还没attach to window,就暂时不请求,留到onAttachToWindow调用的时候再请求
		if (isAttachedOnWindow) {
			beginProcess(Picasso.with(getContext()).load(url));
		}
	}

	public void setImageUrlNeedFit(String url) {
		needFit = true;
		setImageUrl(url);
	}

	public void setImageUrlNeedResize(String url, int width, int height) {
		needResize = true;
		targetWidth = width;
		targetHeight = height;

		setImageUrl(url);
	}

	public void setDefaultImageResId(int resID) {
		if (resID <= 0) {
			return;
		}

		mDefaultDrawable = getResources().getDrawable(resID);
	}

	public void setDefaultImageDrawable(Drawable drawable) {
		if (drawable == null) {
			return;
		}

		mDefaultDrawable = drawable;
	}

	public void setDefaultImageBitmap(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return;
		}

		mDefaultDrawable = new BitmapDrawable(getResources(), bitmap);
	}

	@Override
	protected void onAttachedToWindow() {
		isAttachedOnWindow = true;
		setImageUrl(mUrl);

		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		Picasso.with(getContext()).cancelRequest(this);
		isAttachedOnWindow = false;

		setImageBitmap(null);

		super.onDetachedFromWindow();
	}

	public static boolean allowedUrl(String url) {
		if (TextUtils.isEmpty(url) || url.trim().length() == 0)
			return false;
		else
			return true;
	}

	public static void fetchBitmap(final Context context, String url,
			TargetCallback cb) {
		if (cb == null) {
			return;
		}

		// 根据picasso的load方法，如果String.trim返回长度为0，则会报错
		if (!allowedUrl(url)) {
			return;
		}

		if (mTargetMap == null) {
			mTargetMap = new HashMap<String, Target>();
		}

		CancelableTarget target = new CancelableTarget(context, cb);

		mTargetMap.put(target.toString(), target);

		Picasso.with(context).load(url).into(target);
	}

	/**
	 * 可以被删除引用的target~
	 * 
	 * @author dolphinWang
	 * 
	 */
	private static class CancelableTarget implements Target {

		private Context mContext;

		private TargetCallback mCallback;

		public CancelableTarget(Context context, TargetCallback cb) {
			mContext = context;
			mCallback = cb;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
			mTargetMap.remove(this.toString());
			Picasso.with(mContext).cancelRequest(this);

			mCallback.onBitmapLoaded(bitmap, from);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			mTargetMap.remove(this.toString());
			Picasso.with(mContext).cancelRequest(this);

			mCallback.onBitmapFailed(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			mCallback.onPrepareLoad(placeHolderDrawable);
		}

	}

	public interface TargetCallback {
		public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from);

		public void onBitmapFailed(Drawable errorDrawable);

		public void onPrepareLoad(Drawable placeHolderDrawable);
	}

	// public static class MGWebImageSaveState extends View.BaseSavedState {
	// String mUrl;
	// boolean needFit;
	// boolean needResize;
	// int targetWidth;
	// int targetHeight;
	//
	// public MGWebImageSaveState(Parcelable state) {
	// super(state);
	// }
	//
	// private MGWebImageSaveState(Parcel in) {
	// super(in);
	//
	// mUrl = in.readString();
	//
	// boolean[] bArray = new boolean[2];
	// in.readBooleanArray(bArray);
	// needFit = bArray[0];
	// needResize = bArray[1];
	//
	// int[] iArrat = new int[2];
	// in.readIntArray(iArrat);
	// targetWidth = iArrat[0];
	// targetHeight = iArrat[1];
	// }
	//
	// public static final Creator<MGWebImageSaveState> CREATOR = new
	// Creator<MGWebImageSaveState>() {
	// public MGWebImageSaveState createFromParcel(Parcel in) {
	// return new MGWebImageSaveState(in);
	// }
	//
	// public MGWebImageSaveState[] newArray(int size) {
	// return new MGWebImageSaveState[size];
	// }
	// };
	//
	// @Override
	// public void writeToParcel(Parcel dest, int flags) {
	// super.writeToParcel(dest, flags);
	// dest.writeString(mUrl);
	// dest.writeBooleanArray(new boolean[] { needFit, needResize });
	// dest.writeIntArray(new int[] { targetWidth, targetHeight });
	// }
	// }

}
