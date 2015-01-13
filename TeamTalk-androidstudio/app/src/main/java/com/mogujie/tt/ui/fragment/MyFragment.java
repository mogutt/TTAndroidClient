package com.mogujie.tt.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.ui.activity.SettingActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class MyFragment extends MainFragment implements OnIMServiceListner {
	private View curView = null;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private View contentView;
	private View settingView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_CONTACT_READY);
		imServiceHelper.connect(getActivity(), actions, IMServiceHelper.INTENT_NO_PRIORITY, this);
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
		super.init(curView);
		
		contentView = curView.findViewById(R.id.content);
		settingView = curView.findViewById(R.id.openSetttingPage);
		
		settingView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MyFragment.this.getActivity(), SettingActivity.class));
			}
		});
		hideContent();

		// 设置顶部标题栏
		setTopTitle(getActivity().getString(R.string.page_me));
		// 设置页面其它控件

	}

	private void hideContent() {
		if (contentView != null) {
			contentView.setVisibility(View.GONE);
		}
	}

	private void showContent() {
		if (contentView != null) {
			contentView.setVisibility(View.VISIBLE);
		}
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
		logger.d("detail#onAction action:%s", action);

		if (action.equals(IMActions.ACTION_CONTACT_READY)) {
			init(imServiceHelper.getIMService());
		}
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
		if (!imService.getContactManager().ContactsDataReady()) {
			logger.i("detail#contact data are not ready");
		} else {
			init(imService);
		}
	}

	private void init(IMService imService) {
		showContent();
		hideProgressBar();
		
		if (imService == null) {
			return;
		}

		final ContactEntity loginContact = imService.getContactManager().getLoginContact();
		if (loginContact == null) {
			return;
		}

		TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
		TextView userNameView = (TextView) curView.findViewById(R.id.userName);
		ImageView portraitImageView = (ImageView) curView.findViewById(R.id.user_portrait);

		nickNameView.setText(loginContact.nickName);
		userNameView.setText(loginContact.name);
		IMUIHelper.setEntityImageViewAvatar(portraitImageView, loginContact.avatarUrl, IMSession.SESSION_P2P);

		RelativeLayout userContainer = (RelativeLayout) curView.findViewById(R.id.user_container);
		userContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openUserProfileActivity(getActivity(), loginContact.id);
			}
		});
	}
}
