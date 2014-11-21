package com.mogujie.tt.imlib.utils;

import android.view.View;
import android.widget.CheckBox;

import com.mogujie.tt.imlib.IMConfigurationManager;
import com.mogujie.tt.log.Logger;

public class CheckboxConfigUtils {
	private IMConfigurationManager configMgr;
	private Logger logger = Logger.getLogger(CheckboxConfigUtils.class);

	public CheckboxConfigUtils() {

	}

	public void setConfigMgr(IMConfigurationManager configMgr) {
		this.configMgr = configMgr;
	}

	public void initCheckBox(CheckBox checkBox, String configCategory,
			String configKey, boolean defaultValue) {
		handleCheckBoxChanged(checkBox, configCategory, configKey);
		configCheckBox(checkBox, configCategory, configKey, defaultValue);

	}

	private void configCheckBox(CheckBox checkBox, String configCategory,
			String configKey, boolean defaultValue) {

		if (configMgr == null) {
			logger.e("config#configMgr is null");
			return;
		}

		boolean shouldCheck = configMgr.getBoolean(configCategory, configKey, defaultValue);
		logger.d("config#%s is set %s", configKey, shouldCheck);

		checkBox.setChecked(shouldCheck);
	}

	private void handleCheckBoxChanged(final CheckBox checkBox,
			final String configCategory, final String configKey) {
		if (checkBox == null || configMgr == null) {
			return;
		}

		checkBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				configMgr.setBoolean(configCategory, configKey, checkBox.isChecked());
			}
		});
	}
}
