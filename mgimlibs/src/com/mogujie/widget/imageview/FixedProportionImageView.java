package com.mogujie.widget.imageview;

import com.mogujie.im.libs.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;


/**
 * 涓�釜鍙互璁剧疆瀹介珮姣旂殑imageview锛岄檮甯︾偣鍑荤殑color mask鍔熻兘
 * 
 * @author dolphinWang
 * 
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class FixedProportionImageView extends MGWebImageView {

    private float proportion;
    private int mColor;
    private boolean needColorMask;
    private boolean heightBased;

    public FixedProportionImageView(Context context) {
        super(context);
    }

    public FixedProportionImageView(Context context, AttributeSet attr) {
        super(context, attr);
        ViewConfiguration.get(context);

        TypedArray a = context.obtainStyledAttributes(attr,
                R.styleable.FixedProportionImageView);
        proportion = a.getFloat(
                R.styleable.FixedProportionImageView_fixedProportion, 0);
        heightBased = a.getBoolean(
                R.styleable.FixedProportionImageView_heightBased, false);
        mColor = a.getColor(R.styleable.FixedProportionImageView_maskedColor,
                Color.TRANSPARENT);
        needColorMask = a.getBoolean(
                R.styleable.FixedProportionImageView_needColorMask, true);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (proportion != 0) {
            if (!heightBased) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = (int) (width * proportion);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                        height == 0 ? MeasureSpec.UNSPECIFIED
                                : MeasureSpec.EXACTLY);
            } else {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                int width = (int) (height * proportion);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                        width == 0 ? MeasureSpec.UNSPECIFIED
                                : MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getDrawable() == null || !needColorMask)
            return super.onTouchEvent(event);

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:

            getDrawable().mutate().setColorFilter(mColor,
                    PorterDuff.Mode.MULTIPLY);
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            getDrawable().mutate().clearColorFilter();
            break;
        }

        return super.onTouchEvent(event);
    }

    public void heightBased() {
        heightBased = true;
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
        requestLayout();
    }

    public void setMaskedColor(int color) {
        needColorMask = true;
        mColor = color;
    }
}
