package com.mogujie.tt.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

import com.mogujie.tt.R;
import com.mogujie.tt.biz.MessageNotifyCenter;
import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.conn.NetStateDispach;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.fragment.ChatFragment;
import com.mogujie.tt.ui.fragment.ContactFragment;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.widget.NaviTabButton;

public class MainActivity extends FragmentActivity
		implements
			OnIMServiceListner {
	private static Handler uiHandler = null;// 处理界面消息

	private Fragment[] mFragments;
	private NaviTabButton[] mTabButtons;
	private Logger logger = Logger.getLogger(MainActivity.class);
	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.d("MainActivity#savedInstanceState:%s", savedInstanceState);
		//todo eric when crash, this will be called, why?
		if (savedInstanceState != null) {
			logger.w("MainActivity#crashed and restarted, just exit");

			jumpToLoginPage();

			finish();
		}

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_LOGOUT);

		imServiceHelper.connect(this, actions, IMServiceHelper.INTENT_NO_PRIORITY, this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tt_activity_main);

		initTab();
		initFragment();
		setFragmentIndicator(0);

		initHandler();
		registEvents();
	}

	@Override
	public void onBackPressed() {
		//don't let it exit
		//super.onBackPressed();

		//nonRoot	If false then this only works if the activity is the root of a task; if true it will work for any activity in a task.
		//document http://developer.android.com/reference/android/app/Activity.html

		//moveTaskToBack(true);

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);

	}

	public static Handler getUiHandler() {
		return uiHandler;
	}

	private void initFragment() {
		mFragments = new Fragment[4];
		mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
		mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
		mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_internal);
		mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
	}

	private void initTab() {
		mTabButtons = new NaviTabButton[4];

		mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
		mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
		mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_internal);
		mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_my);

		mTabButtons[0].setTitle(getString(R.string.main_chat));
		mTabButtons[0].setIndex(0);
		mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
		mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));

		mTabButtons[1].setTitle(getString(R.string.main_contact));
		mTabButtons[1].setIndex(1);
		mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
		mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

		mTabButtons[2].setTitle(getString(R.string.main_innernet));
		mTabButtons[2].setIndex(2);
		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));

		mTabButtons[3].setTitle(getString(R.string.main_me_tab));
		mTabButtons[3].setIndex(3);
		mTabButtons[3].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
		mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));
	}

	public void setFragmentIndicator(int which) {
		getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).show(mFragments[which]).commit();

		mTabButtons[0].setSelectedButton(false);
		mTabButtons[1].setSelectedButton(false);
		mTabButtons[2].setSelectedButton(false);
		mTabButtons[3].setSelectedButton(false);

		mTabButtons[which].setSelectedButton(true);
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		uiHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case HandlerConstant.HANDLER_CONTACTS_NEW_MESSAGE_COME :
						showUnreadMessageCount();
						break;
					default :
						break;
				}
			}
		};
	}

	public void setUnreadMessageCnt(int unreadCnt) {
		mTabButtons[0].setUnreadNotify(unreadCnt);
	}

	private void showUnreadMessageCount() {
		//todo eric when to 
		//mTabButtons[0].setUnreadNotify(CacheHub.getInstance().getUnreadCount());
	}

	/**
	 * @Description 注册事件
	 */
	private void registEvents() {
		NetStateDispach.getInstance().register(this.getClass(), uiHandler);
		// 未读消息通知
		MessageNotifyCenter.getInstance().register(SysConstant.EVENT_UNREAD_MSG, uiHandler, HandlerConstant.HANDLER_CONTACTS_NEW_MESSAGE_COME);
	}

	/**
	 * @Description 取消事件注册
	 */
	private void unRegistEvents() {
		MessageNotifyCenter.getInstance().unregister(SysConstant.EVENT_UNREAD_MSG, getUiHandler(), HandlerConstant.HANDLER_CONTACTS_NEW_MESSAGE_COME);
		return;
	}

	public void btnChatClick() {
		try {
			setFragmentIndicator(0);
			((ChatFragment) mFragments[0]).setUnreadPosition();
		} catch (Exception e) {
			logger.e("mainactivity#btnChatClick", e.toString());
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		handleLocateDepratment(intent);
	}

	public void btnContactClick(View v) {
		setFragmentIndicator(1);
	}

	public void btnMyClick(View v) {
		setFragmentIndicator(2);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void handleLocateDepratment(Intent intent) {
		String departmentIdToLocate = intent.getStringExtra(SysConstant.KEY_LOCATE_DEPARTMENT);
		if (departmentIdToLocate == null || departmentIdToLocate.isEmpty()) {
			return;
		}

		logger.d("department#got department to locate id:%s", departmentIdToLocate);
		setFragmentIndicator(1);
		ContactFragment fragment = (ContactFragment) mFragments[1];
		if (fragment == null) {
			logger.e("department#fragment is null");
			return;
		}

		fragment.locateDepartment(departmentIdToLocate);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		logger.d("mainactivity#onDestroy");

		unRegistEvents();

		imServiceHelper.disconnect(getApplicationContext());

		super.onDestroy();
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		if (action.equals(IMActions.ACTION_LOGOUT)) {
			handleOnLogout();
		}
	}

	private void handleOnLogout() {
		logger.d("mainactivity#login#handleOnLogout");

		finish();

		logger.d("mainactivity#login#kill self, and start login activity");
		
		jumpToLoginPage();

	}

	private void jumpToLoginPage() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(SysConstant.KEY_LOGIN_NOT_AUTO, true);
		startActivity(intent);
	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub

	}

}
