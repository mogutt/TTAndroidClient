package com.mogujie.tt.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mogujie.tt.R;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.common.ConfigDefs;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.CheckboxConfigUtils;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.ui.activity.GroupMemberSelectActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;
import com.mogujie.tt.ui.utils.IMGroupMemberGridViewHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class GroupManagerFragment extends TTBaseFragment
		implements
			OnIMServiceListner {
	// OnTouchBlankPositionListener, {
	@Override
	public void onDestroyView() {
		imServiceHelper.disconnect(getActivity());
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	private View curView = null;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private IMService imService;
	private IMGroupMemberGridViewHelper gridViewHelper = new IMGroupMemberGridViewHelper();
	CheckboxConfigUtils checkBoxConfiger = new CheckboxConfigUtils();
	CheckBox dontDisturbCheckbox;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_group_manage, topContentView);
		dontDisturbCheckbox = (CheckBox) curView.findViewById(R.id.NotificationNoDisturbCheckbox);

		imServiceHelper.connect(getActivity(), null, IMServiceHelper.INTENT_NO_PRIORITY, this);
		initRes();

		return curView;
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏

		setTopLeftButton(R.drawable.tt_top_back);
		setTopLeftText(getActivity().getString(R.string.top_left_back));
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));

		// 设置其它页面信息
		// gridView = (GroupManagerGridView) curView
		// .findViewById(R.id.group_manager_grid);

		// // 点击空白地方时处理
		// ((GroupManagerGridView)
		// gridView).setOnTouchBlankPositionListener(this);
		//
		// gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
		// @Override
		// public boolean onItemLongClick(AdapterView<?> adapterView,
		// View view, int position, long id) {
		// if (position < adapter.getCount() - 1 && adapter.getCount() > 3) {
		// adapter.setRemoveState(true);
		// adapter.notifyDataSetChanged();
		// }
		// return true;
		// }
		// });
		// gridView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> adapterView, View view,
		// int position, long id) {
		// if (adapter.getRemoveState()) {
		// if (adapter.getCount() > 3) {
		// adapter.remove(position);
		// }
		// if (adapter.getCount() <= 3) {
		// adapter.setRemoveState(false);
		// }
		// adapter.notifyDataSetChanged();
		// } else {
		// // if (position == adapter.getAddFriendPosition()) {
		// // Intent intent = new Intent(getActivity(),
		// // ContactFragmentActivity.class);
		// // intent.putExtra(SysConstant.CHOOSE_CONTACT, true);
		// // startActivityForResult(intent,
		// // SysConstant.GROUP_MANAGER_ADD_RESULT);
		// // // adapter.add();
		// // }
		// }
		// }
		// });
	}

	private void startGroupMemberSelectActivity() {
		Intent intent = new Intent(getActivity(), GroupMemberSelectActivity.class);

		IMUIHelper.setSessionInIntent(intent, gridViewHelper.getSessionId(), gridViewHelper.getSessionType());

		getActivity().startActivity(intent);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void initHandler() {
	}

	// @Override
	// public boolean onTouchBlankPosition() {
	// adapter.setRemoveState(false);
	// adapter.notifyDataSetChanged();
	// return false;
	// }

	private void init() {
		SessionInfo sessionInfo = IMUIHelper.getSessionInfoFromIntent(getActivity().getIntent());
		if (sessionInfo == null) {
			logger.e("groupmgr#getSessionInfoFromIntent failed");
			return;
		}

		int sessionType = sessionInfo.getSessionType();
		String sessionId = sessionInfo.getSessionId();

		if (imService == null) {
			return;
		}

		if (curView == null) {
			return;
		}

		String title = "聊天信息";
		boolean hasAddBtn = true;
		boolean shouldDisplayNotifySetting = false;
		if (sessionType == IMSession.SESSION_P2P) {

			View groupNameContainerView = curView.findViewById(R.id.group_manager_name);
			if (groupNameContainerView == null) {
				return;
			}

			groupNameContainerView.setVisibility(View.GONE);

		} else {
			shouldDisplayNotifySetting = true;

			if (sessionType == IMSession.SESSION_GROUP) {
				hasAddBtn = false;
			}

			GroupEntity group = imService.getGroupManager().findGroup(sessionId);
			if (group == null) {
				return;
			}

			if (sessionType == IMSession.SESSION_TEMP_GROUP) {
				if (!(imService != null && imService.getLoginManager().getLoginId().equals(group.creatorId))) {
					hasAddBtn = false;
				}
			}

			// todo eric i18n
			title += String.format("(%d)", group.memberIdList.size());

			TextView groupNameView = (TextView) curView.findViewById(R.id.group_manager_title);
			if (groupNameView == null) {
				return;
			}
			groupNameView.setText(group.name);
		}

		initDontDisturbCheckbox(shouldDisplayNotifySetting, sessionInfo);
		gridViewHelper.onInit(curView, R.id.group_manager_grid, getActivity(), hasAddBtn, new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				boolean isAddMemberButton = gridViewHelper.getAdapter().isAddMemberButton(position);
				if (isAddMemberButton) {
					logger.d("groupmgr#click add MemberButton");
					startGroupMemberSelectActivity();
				}
			}
		}, null, null);

		setTopTitle(title);
	}

	private void initDontDisturbCheckbox(boolean shouldDisplayNotifySetting,
			SessionInfo sessionInfo) {

		View notifySettingArea = curView.findViewById(R.id.notification_setting_area);
		if (notifySettingArea != null) {
			notifySettingArea.setVisibility(shouldDisplayNotifySetting
					? View.VISIBLE
					: View.GONE);
		}

		if (!shouldDisplayNotifySetting) {
			return;
		}

		String sessionKey = IMUIHelper.getSessionKey(sessionInfo.getSessionId(), sessionInfo.getSessionType());
		checkBoxConfiger.initCheckBox(dontDisturbCheckbox, sessionKey, ConfigDefs.KEY_NOTIFICATION_NO_DISTURB, ConfigDefs.DEF_VALUE_NOTIFICATION_NO_DISTURB);
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub
		logger.d("groupmgr#onIMServiceConnected");

		imService = imServiceHelper.getIMService();
		checkBoxConfiger.setConfigMgr(imService.getConfigManager());

		init();

		gridViewHelper.onSetGridData(imService, getActivity().getIntent());
	}
}
