package com.mogujie.tt.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.widget.imageview.MGWebImageView;

public class UserInfoFragment extends TTBaseFragment
		implements
			OnIMServiceListner {

	private View curView = null;
	private User user = null;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		imServiceHelper.connect(getActivity(), null, IMServiceHelper.INTENT_NO_PRIORITY, this);

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);

		initRes();

		return curView;
	}

	@Override
	public void onResume() {
		Intent intent = getActivity().getIntent();
		if (null != intent) {
			String fromPage = intent.getStringExtra(SysConstant.USER_DETAIL_PARAM);
			setTopLeftText(fromPage);
		}
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏
		setTopTitle(getActivity().getString(R.string.page_user_detail));
		setTopLeftButton(R.drawable.tt_top_back);
		topLeftBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});

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
		IMService imService = imServiceHelper.getIMService();
		if (imService == null) {
			return;
		}

		SessionInfo sessionInfo = IMUIHelper.getSessionInfoFromIntent(getActivity().getIntent());
		final ContactEntity contact = imService.getContactManager().findContact(sessionInfo.getSessionId());
		if (contact == null) {
			return;
		}

		DepartmentEntity department = imService.getContactManager().findDepartment(contact.departmentId);
		if (department == null) {
			return;
		}

		MGWebImageView portraitImageView = (MGWebImageView) curView.findViewById(R.id.user_portrait);

		setTextViewContent(R.id.nickName, contact.nickName);
		setTextViewContent(R.id.userName, contact.name);
		IMUIHelper.setWebImageViewAvatar(portraitImageView, contact.avatarUrl, IMSession.SESSION_P2P);

		setTextViewContent(R.id.position, contact.position);
		setTextViewContent(R.id.department, department.title);
		setTextViewContent(R.id.telno, contact.telephone);
		setTextViewContent(R.id.email, contact.email);

		// 设置界面信息
		Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
		if (contact.id.equals(imService.getLoginManager().getLoginId())) {
			chatBtn.setVisibility(View.GONE);
		}

		chatBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				IMUIHelper.openContactChatActivity(getActivity(), contact);
			}
		});
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) curView.findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

}
