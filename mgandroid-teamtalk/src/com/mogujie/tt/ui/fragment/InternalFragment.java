package com.mogujie.tt.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.ui.activity.WebViewFragmentActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;

public class InternalFragment extends TTBaseFragment {
	private View curView = null;
	private View ttOpenSourceView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_internal,
				topContentView);

		initRes();

		return curView;
	}

	private void initRes() {
		// 设置顶部标题栏
		setTopTitle(getActivity().getString(R.string.main_innernet));
		ttOpenSourceView = curView.findViewById(R.id.TTOpenSourcePage);
		ttOpenSourceView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent=new Intent(InternalFragment.this.getActivity(),WebViewFragmentActivity.class);
			    intent.putExtra(SysConstant.WEBVIEW_URL, "http://tt.mogu.io/home/introduce?type=mobile");	
				startActivity(intent);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void initHandler() {
	}
}
