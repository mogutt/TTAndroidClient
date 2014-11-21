package com.mogujie.tt.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.ui.activity.DetailPortraitActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class UserInfoFragment extends MainFragment
		implements
			OnIMServiceListner {

	private View curView = null;
	private User user = null;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	SessionInfo sessionInfo;

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();

		imServiceHelper.disconnect(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceHelper.connect(getActivity(), null, IMServiceHelper.INTENT_NO_PRIORITY, this);

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);

		super.init(curView);
		showProgressBar();

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
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}

	@Override
	protected void initHandler() {
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		logger.d("detail#onAction action:%s", action);
	}

	@Override
	public void onIMServiceConnected() {
		logger.d("detail#onIMServiceConnected");

		IMService imService = imServiceHelper.getIMService();
		if (imService == null) {
			logger.e("detail#imService is null");
			return;
		}

		sessionInfo = IMUIHelper.getSessionInfoFromIntent(getActivity().getIntent());
		logger.d("detail#sessionInfo:%s", sessionInfo);

		initBaseProfile(imService, sessionInfo);
		initDetailProfile(imService, sessionInfo);
	}

	private void initBaseProfile(IMService imService, SessionInfo sessionInfo) {
		logger.d("detail#initBaseProfile");

		if (imService == null) {
			logger.e("detail#imService is null");
			return;
		}

		final ContactEntity contact = imService.getContactManager().findContact(sessionInfo.getSessionId());
		if (contact == null) {
			logger.d("detail#no such contact id:%s", sessionInfo.getSessionId());
			return;
		}

		ImageView portraitImageView = (ImageView) curView.findViewById(R.id.user_portrait);

		setTextViewContent(R.id.nickName, contact.nickName);
		setTextViewContent(R.id.userName, contact.name);
		IMUIHelper.setEntityImageViewAvatar(portraitImageView, contact.avatarUrl, IMSession.SESSION_P2P);
		
		portraitImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
				intent.putExtra(SysConstant.KEY_AVATAR_URL, contact.avatarUrl);
				intent.putExtra(SysConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
				
				startActivity(intent);
			}
		});

		// 设置界面信息
		Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
		if (contact.id.equals(imService.getLoginManager().getLoginId())) {
			chatBtn.setVisibility(View.GONE);
		}

		chatBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				IMUIHelper.openContactChatActivity(getActivity(), contact);
				getActivity().finish();
			}
		});

	}

	private void initDetailProfile(IMService imService, SessionInfo sessionInfo) {
		logger.d("detail#initDetailProfile");

		if (imService == null) {
			logger.e("detail#imService is null");
			return;
		}

		hideProgressBar();

		final ContactEntity contact = imService.getContactManager().findContact(sessionInfo.getSessionId());
		if (contact == null) {
			logger.d("detail#no such contact id:%s", sessionInfo.getSessionId());
			return;
		}

		DepartmentEntity department = imService.getContactManager().findDepartment(contact.departmentId);
		if (department == null) {
			return;
		}

		setTextViewContent(R.id.department, department.title);
		setTextViewContent(R.id.telno, contact.telephone);
		setTextViewContent(R.id.email, contact.email);

		View phoneView = curView.findViewById(R.id.phoneArea);
		IMUIHelper.setViewTouchHightlighted(phoneView);
		phoneView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (contact.id.equals(IMLoginManager.instance().getLoginId()))
					return;
				IMUIHelper.callPhone(getActivity(), contact.telephone);
			}
		});

		setSex(contact.sex);
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) curView.findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void setSex(int sex) {
		if (curView == null) {
			return;
		}

		TextView sexTextView = (TextView) curView.findViewById(R.id.sex);
		if (sexTextView == null) {
			return;
		}

		int textColor = Color.rgb(255, 138, 168); //xiaoxian
		String text = getString(R.string.sex_female_name);

		if (sex == SysConstant.SEX_MAILE) {
			textColor = Color.rgb(144, 203, 1);
			text = getString(R.string.sex_male_name);
		}

		sexTextView.setVisibility(View.VISIBLE);
		sexTextView.setText(text);
		sexTextView.setTextColor(textColor);
	}

}
