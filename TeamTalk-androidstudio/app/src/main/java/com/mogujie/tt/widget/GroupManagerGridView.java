
package com.mogujie.tt.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class GroupManagerGridView extends GridView {

    private OnTouchBlankPositionListener touchBlankPosListener = null;

    public GroupManagerGridView(Context context) {
        super(context);
    }

    public GroupManagerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
//                MeasureSpec.AT_MOST);
    	
    	int expandSpec = MeasureSpec.makeMeasureSpec(heightMeasureSpec,
    	                                             MeasureSpec.AT_MOST);
    	
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

//    // 禁止Grid滑动
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
////        if (ev.getAction() == MotionEvent.ACTION_MOVE)
////            return true;
//        return super.dispatchTouchEvent(ev);
//    }

    public interface OnTouchBlankPositionListener {
        boolean onTouchBlankPosition();
    }

    public void setOnTouchBlankPositionListener(OnTouchBlankPositionListener listener) {
        touchBlankPosListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

//        if (touchBlankPosListener != null) {
//            if (!isEnabled()) {
//                return isClickable() || isLongClickable();
//            }
//
//            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
//                final int motionPosition = pointToPosition((int) event.getX(), (int) event.getY());
//                if (motionPosition == -1) {
//                    return touchBlankPosListener.onTouchBlankPosition();
//                }
//            }
//        }

        return super.onTouchEvent(event);
    }
}
