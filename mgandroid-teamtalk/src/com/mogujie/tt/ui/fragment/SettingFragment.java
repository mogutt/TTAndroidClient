package com.mogujie.tt.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.mogujie.tt.R;
import com.mogujie.tt.imlib.IMConfigurationManager;
import com.mogujie.tt.imlib.common.ConfigDefs;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.base.TTBaseFragment;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class SettingFragment extends TTBaseFragment
		implements
			OnIMServiceListner {

	private Logger logger = Logger.getLogger(SettingFragment.class);
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private IMConfigurationManager configMgr;
	private View curView = null;
	private View exitTeamTalkView;
	private CheckBox notificationNoDisturbCheckBox;
	private CheckBox notificationGotSoundCheckBox;
	private CheckBox notificationGotVibrationCheckBox;
	private CheckBox saveTrafficModeCheckBox;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//		ArrayList<String> actions = new ArrayList<String>();

		imServiceHelper.connect(this.getActivity(), null, IMServiceHelper.INTENT_MAX_PRIORITY, this);

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_setting, topContentView);

		initExitView();

		initRes();

		return curView;
	}

	private void initOptions() {
		notificationNoDisturbCheckBox = (CheckBox) curView.findViewById(R.id.NotificationNoDisturbCheckbox);
		notificationGotSoundCheckBox = (CheckBox) curView.findViewById(R.id.notifyGotSoundCheckBox);
		notificationGotVibrationCheckBox = (CheckBox) curView.findViewById(R.id.notifyGotVibrationCheckBox);
		saveTrafficModeCheckBox = (CheckBox) curView.findViewById(R.id.saveTrafficCheckBox);
		
		initCheckBox(notificationNoDisturbCheckBox, ConfigDefs.KEY_NOTIFICATION_NO_DISTURB, ConfigDefs.DEF_VALUE_NOTIFICATION_NO_DISTURB);
		initCheckBox(notificationGotSoundCheckBox, ConfigDefs.KEY_NOTIFICATION_GOT_SOUND, ConfigDefs.DEF_VALUE_NOTIFICATION_GOT_SOUND);
		initCheckBox(notificationGotVibrationCheckBox, ConfigDefs.KEY_NOTIFICATION_GOT_VIBRATION, ConfigDefs.DEF_VALUE_NOTIFICATION_GOT_VIBRATION);
		initCheckBox(saveTrafficModeCheckBox, ConfigDefs.KEY_SAVE_TRAFFIC_MODE, ConfigDefs.DEF_VALUE_SAVE_TRAFFIC_MODE);
	}
	private void initCheckBox(CheckBox checkBox, String configKey,
			boolean defaultValue) {
		handleCheckBoxChanged(checkBox, configKey);
		configCheckBox(checkBox, configKey, defaultValue);

	}

	private void configCheckBox(CheckBox checkBox, String configKey,
			boolean defaultValue) {

		if (configMgr == null) {
			logger.e("config#configMgr is null");
			return;
		}

		boolean shouldCheck = configMgr.getBoolean(ConfigDefs.CATEGORY_GLOBAL, configKey, defaultValue);
		logger.d("config#%s is set %s", configKey, shouldCheck);
		
		checkBox.setChecked(shouldCheck);
	}

	private void handleCheckBoxChanged(final CheckBox checkBox,
			final String configKey) {
		if (checkBox == null) {
			return;
		}

		checkBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				configMgr.setBoolean(ConfigDefs.CATEGORY_GLOBAL, configKey, checkBox.isChecked());
			}
		});
	}

	private void initExitView() {
		exitTeamTalkView = curView.findViewById(R.id.exitTeamTalkView);

		if (exitTeamTalkView != null) {
			exitTeamTalkView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "you clicked me", Toast.LENGTH_SHORT).show();
				}
			});

			IMUIHelper.setViewTouchHightlighted(exitTeamTalkView);
		}
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏
		setTopTitle(getActivity().getString(R.string.setting_page_name));
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
		logger.d("config#onIMServiceConnected");

		IMService imService = imServiceHelper.getIMService();
		if (imService != null) {
			configMgr = imService.getConfigManager();

			initOptions();
		}
	}
}
