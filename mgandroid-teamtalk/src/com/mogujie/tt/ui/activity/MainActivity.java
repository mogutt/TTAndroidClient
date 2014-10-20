package com.mogujie.tt.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.fragment.ContactFragment;
import com.mogujie.tt.widget.NaviTabButton;

public class MainActivity extends FragmentActivity {
	private static Handler uiHandler = null;// 处理界面消息

	private Fragment[] mFragments;
	private NaviTabButton[] mTabButtons;
	private Logger logger = Logger.getLogger(MainActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.d("MainActivity#savedInstanceState:%s", savedInstanceState);
		if (savedInstanceState != null) {
			logger.w("MainActivity#crashed and restarted, just exit");

			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);

			finish();
		}

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

		moveTaskToBack(true);
	}

	public static Handler getUiHandler() {
		return uiHandler;
	}

	private void initFragment() {
		mFragments = new Fragment[3];
		mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
		mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
		mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
	}

	private void initTab() {
		mTabButtons = new NaviTabButton[3];

		mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
		mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
		mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_my);

		mTabButtons[0].setTitle(getString(R.string.main_chat));
		mTabButtons[0].setIndex(0);
		mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
		mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));

		mTabButtons[1].setTitle(getString(R.string.main_contact));
		mTabButtons[1].setIndex(1);
		mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
		mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

		mTabButtons[2].setTitle(getString(R.string.main_me_tab));
		mTabButtons[2].setIndex(2);
		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));
	}

	public void setFragmentIndicator(int which) {
		getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).show(mFragments[which]).commit();

		mTabButtons[0].setSelectedButton(false);
		mTabButtons[1].setSelectedButton(false);
		mTabButtons[2].setSelectedButton(false);

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

	public void btnChatClick(View v) {
		setFragmentIndicator(0);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
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

		//set in onNewIntent
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		
		handleLocateDepratment(intent);
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
		unRegistEvents();
		super.onDestroy();
	}

}
