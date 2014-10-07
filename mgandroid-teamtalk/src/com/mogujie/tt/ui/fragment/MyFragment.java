package com.mogujie.tt.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.ui.activity.UserInfoActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.widget.imageview.MGWebImageView;

public class MyFragment extends TTBaseFragment implements OnIMServiceListner {
	private View curView = null;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		imServiceHelper.connect(getActivity(), null, IMServiceHelper.INTENT_NO_PRIORITY, this);
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);

		initRes();

		return curView;
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置顶部标题栏
		setTopTitle(getActivity().getString(R.string.page_me));
		// 设置页面其它控件
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void initHandler() {

	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub
		if (curView == null) {
			return;
		}

		IMService imService = imServiceHelper.getIMService();
		if (imService == null) {
			return;
		}

		final ContactEntity loginContact = imService.getContactManager().getLoginContact();
		if (loginContact == null) {
			return;
		}

		TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
		TextView userNameView = (TextView) curView.findViewById(R.id.userName);
		MGWebImageView portraitImageView = (MGWebImageView) curView.findViewById(R.id.user_portrait);

		nickNameView.setText(loginContact.nickName);
		userNameView.setText(loginContact.name);
		IMUIHelper.setWebImageViewAvatar(portraitImageView, loginContact.avatarUrl, IMSession.SESSION_P2P);
		
		RelativeLayout userContainer = (RelativeLayout) curView.findViewById(R.id.user_container);
		userContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(), UserInfoActivity.class);
				IMUIHelper.setSessionInIntent(intent, loginContact.id, IMSession.SESSION_P2P);
				getActivity().startActivity(intent);
			}
		});
	}
}
