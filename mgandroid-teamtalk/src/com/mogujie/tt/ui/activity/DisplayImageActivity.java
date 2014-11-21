
package com.mogujie.tt.ui.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R.anim;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mogujie.tt.R;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.tools.DisplayBitmapCache;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.widget.MGProgressbar;
import com.mogujie.widget.imageview.MGWebImageView;
import com.polites.android.GestureImageView;
import com.squareup.picasso.Picasso.LoadedFrom;

/**
 * @Description
 * @author Nana
 * @date 2014-4-10
 */
public class DisplayImageActivity extends TTBaseActivity {

    protected static Handler uiHandler = null;
    protected GestureImageView view;
    protected GestureImageView newView;
    private MessageInfo messageInfo = null;
    private boolean isMine = false;
    private MGProgressbar mProgressbar = null;
    private FrameLayout parentLayout = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_display_image_layout);
        // 获取参数
        messageInfo = (MessageInfo) getIntent().getSerializableExtra(
                SysConstant.CUR_MESSAGE);
        isMine = getIntent().getBooleanExtra("ISMINE", false);

        initRes();

        if (isMine)
            return;

        initProgress();
        mProgressbar.showProgress();
        Bitmap bigBitmap = null;
        if (bigBitmap == null) {

//            String bigImagePath = CommonUtil.getMd5Path(messageInfo.getUrl(), SysConstant.FILE_SAVE_TYPE_IMAGE);
        	String bigImagePath = messageInfo.getSavePath();

            if (messageInfo.getUrl() != null) {
//                if (new File(bigImagePath).exists()) {
            	
            	if (new File(bigImagePath).exists()) {

                    Bitmap bitmap = DisplayBitmapCache.getInstance(DisplayImageActivity.this).get(
                            bigImagePath);
                    closeProgressDialog(bitmap, true);
                } else {
                    MGWebImageView.fetchBitmap(this, messageInfo.getUrl(),
                            new MGWebImageView.TargetCallback() {
                                @Override
                                public void onPrepareLoad(
                                        Drawable placeHolderDrawable) {
                                }

                                @Override
                                public void onBitmapLoaded(Bitmap bitmap,
                                        LoadedFrom from) {
                                    String bigImagePath = CommonUtil.getMd5Path(
                                            messageInfo.getUrl(),
                                            SysConstant.FILE_SAVE_TYPE_IMAGE);

                                    File myFile = new File(bigImagePath);
                                    if (myFile.exists())
                                        return;
                                    BufferedOutputStream bos = null;
                                    try {
                                        if (bitmap != null) {
                                            FileOutputStream fout = new FileOutputStream(
                                                    myFile);
                                            bos = new BufferedOutputStream(fout);
                                            bitmap.compress(
                                                    Bitmap.CompressFormat.JPEG,
                                                    100, bos);
                                            bos.flush();
                                            bos.close();
                                            bos = null;
                                            messageInfo
                                                    .setSavePath(bigImagePath);
                                            messageInfo
                                                    .setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
                                            MessageActivity.updateMessageSavePath(
                                                    messageInfo.msgId,
                                                    bigImagePath);

                                            CacheHub.getInstance()
                                                    .updateMsgImageSavePath(
                                                            messageInfo
                                                                    .msgId,
                                                            bigImagePath);
                                            CacheHub.getInstance()
                                                    .updateMsgStatus(
                                                            messageInfo
                                                                    .msgId,
                                                            SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
                                            Bitmap bmp = DisplayBitmapCache
                                                    .getInstance(DisplayImageActivity.this).get(
                                                            bigImagePath);
                                            closeProgressDialog(bmp, true);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (bos != null) {
                                                bos.flush();
                                                bos.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();

                                        }
                                    }
                                }

                                @Override
                                public void onBitmapFailed(
                                        Drawable errorDrawable) {
                                    closeProgressDialog(null, false);
                                }
                            });
                }
            } else {
                if (messageInfo.getSavePath() != null
                        && messageInfo.getSavePath().equals(bigImagePath)) {
                    Bitmap bitmap = DisplayBitmapCache.getInstance(DisplayImageActivity.this).get(
                            messageInfo.getSavePath());
                    closeProgressDialog(bitmap, true);
                } else {
                    closeProgressDialog(null, false);
                }
            }
        }
    }

    private void initRes() {
        view = (GestureImageView) this.findViewById(R.id.image);
        newView = (GestureImageView) this.findViewById(R.id.new_image);
        parentLayout = (FrameLayout) this.findViewById(R.id.layout);
        // 1.默认显示一张图片
        if (messageInfo.getSavePath() != null) {
            Bitmap bitmap = DisplayBitmapCache.getInstance(DisplayImageActivity.this).get(
                    messageInfo.getSavePath());
            view.setImageBitmap(bitmap);
        } else {
            view.setBackgroundResource(R.drawable.tt_default_image);
        }
        view.setClickable(true);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                parentLayout.performClick();
            }
        });
        parentLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DisplayImageActivity.this.finish();
                DisplayImageActivity.this.overridePendingTransition(
                        R.anim.tt_stay, R.anim.tt_image_exit);
            }

        });
    }

    private void initProgress() {
        View view = LayoutInflater.from(DisplayImageActivity.this).inflate(
                R.layout.tt_progress_ly, null);
        mProgressbar = (MGProgressbar) view.findViewById(R.id.tt_progress);
        mProgressbar.setShowText(false);
        addContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void closeProgressDialog(Bitmap bitmap, boolean downloadSuccess) {
        mProgressbar.hideProgress();
        if (downloadSuccess == false || bitmap == null) {

            if (!downloadSuccess) {
                Logger.getLogger(DisplayImageActivity.class)
                        .d("download faild");
            } else {
                Logger.getLogger(DisplayImageActivity.class)
                        .d("bitmap == null");
            }

            Toast.makeText(
                    this,
                    this.getResources().getString(
                            R.string.image_download_failed), Toast.LENGTH_LONG)
                    .show();
            return;
        }
        view.setVisibility(View.GONE);
        newView.setImageBitmap(bitmap);
        newView.setClickable(true);
        newView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                parentLayout.performClick();
            }
        });
    }

    @Override
    protected void initHandler() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            DisplayImageActivity.this.overridePendingTransition(R.anim.tt_stay,
                    R.anim.tt_image_exit);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
