package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.ui.base.TTBaseFragmentActivity;
import com.mogujie.tt.ui.fragment.WebviewFragment;

public class WebViewFragmentActivity extends TTBaseFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		if (intent.hasExtra(SysConstant.WEBVIEW_URL)) {
			WebviewFragment.setUrl(intent.getStringExtra(SysConstant.WEBVIEW_URL));
		}
		setContentView(R.layout.tt_fragment_activity_webview);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
