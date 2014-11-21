
package com.mogujie.tt.ui.activity;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.album.AlbumHelper;
import com.mogujie.tt.adapter.album.ImageBucket;
import com.mogujie.tt.adapter.album.ImageBucketAdapter;
import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;

/**
 * @Description 相册列表
 * @author Nana
 * @date 2014-5-6
 */
public class PickPhotoActivity extends Activity implements OnTouchListener {
    List<ImageBucket> dataList = null;
    ListView listView = null;
    ImageBucketAdapter adapter = null;
    AlbumHelper helper = null;
    TextView cancel = null;
    Handler uiHandler = null;
    public static Bitmap bimap = null;
    private String CHAT_USER_ID = null;
    boolean touchable = true;
	private Logger logger = Logger.getLogger(PickPhotoActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	logger.d("pic#PickPhotoActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_pick_photo);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        CHAT_USER_ID = (String) getIntent().getSerializableExtra(
                SysConstant.EXTRA_CHAT_USER_ID);
        helper = AlbumHelper.getHelper(getApplicationContext());
        dataList = helper.getImagesBucketList(true);
        bimap = BitmapFactory.decodeResource(getResources(),
                R.drawable.tt_default_album_grid_image);
    }

    /**
     * 初始化view
     */
    private void initView() {
        listView = (ListView) findViewById(R.id.list);
        adapter = new ImageBucketAdapter(this, dataList);

        listView.setAdapter(adapter);
        listView.setOnTouchListener(this);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent intent = new Intent(PickPhotoActivity.this,
                        ImageGridActivity.class);
                intent.putExtra(SysConstant.EXTRA_IMAGE_LIST,
                        (Serializable) dataList.get(position).imageList);
                intent.putExtra(SysConstant.EXTRA_ALBUM_NAME,
                        dataList.get(position).bucketName);
                intent.putExtra(SysConstant.EXTRA_CHAT_USER_ID,
                        CHAT_USER_ID);
                startActivity(intent);
                setResult(Activity.RESULT_OK, null);
                PickPhotoActivity.this.finish();
            }
        });
        cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, null);
                PickPhotoActivity.this.finish();
                overridePendingTransition(R.anim.tt_stay, R.anim.tt_album_exit);
            }
        });

        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_CANCEL_SELECTED:
                        adapter.setSelectedPosition(-1);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        View mDownView = null;
        @SuppressWarnings("unused")
        float mDownX = 0;
        int mDownPosition = -1;
        VelocityTracker mVelocityTracker = null;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                Rect rect = new Rect();
                int childCount = listView.getChildCount();
                int[] listViewCoords = new int[2];
                listView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = listView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child;
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mDownPosition = listView.getPositionForView(mDownView);

                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);

                    adapter.setSelectedPosition(mDownPosition);
                    adapter.notifyDataSetChanged();
                }
            }
                break;
            case MotionEvent.ACTION_UP:
                uiHandler.sendEmptyMessage(HandlerConstant.HANDLER_CANCEL_SELECTED);
                break;
        }
        return false;
    }
}
