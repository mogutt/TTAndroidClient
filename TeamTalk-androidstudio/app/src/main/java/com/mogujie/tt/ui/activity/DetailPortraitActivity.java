package com.mogujie.tt.ui.activity;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class DetailPortraitActivity extends Activity {

	private Logger logger = Logger.getLogger(DetailPortraitActivity.class);

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_detail_portrait);
		
		Intent intent = getIntent();
		if (intent == null) {
			logger.e("detailPortrait#displayimage#null intent");
			return;
		}
		
		String resUri = intent.getStringExtra(SysConstant.KEY_AVATAR_URL);
		logger.d("detailPortrait#displayimage#resUri:%s", resUri);
		
		boolean isContactAvatar = intent.getBooleanExtra(SysConstant.KEY_IS_IMAGE_CONTACT_AVATAR, false);
		logger.d("displayimage#isContactAvatar:%s", isContactAvatar);
		
		ImageView portraitView = (ImageView) findViewById(R.id.detail_portrait);
		if (portraitView == null) {
			logger.e("detailPortrait#displayimage#portraitView is null");
			return;
		}
		
		logger.d("detailPortrait#displayimage#going to load the detail portrait");
		
		if (isContactAvatar) {
			IMUIHelper.setEntityImageViewAvatarNoDefaultPortrait(portraitView, resUri, IMSession.SESSION_P2P);
		} else {
			IMUIHelper.displayImage(portraitView, resUri, -1);
		}	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
