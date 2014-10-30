package com.mogujie.tt.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.mogujie.tt.R;
import com.mogujie.tt.app.IMEntrance;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.conn.NetStateDispach;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.db.IMDbManager.LoginIdentity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class LoginActivity extends TTBaseActivity implements OnIMServiceListner {

	// todo eric remove uiHandler dead Codes
	// protected static Handler uiHandler = null;

	private EditText mNameView;

	private EditText mPasswordView;

	private String loginName;

	@SuppressWarnings("unused")
	private View mLoginFormView;

	private View mLoginStatusView;

	private Logger logger = Logger.getLogger(LoginActivity.class);

	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	private IMLoginManager imLoginMgr;

	public static Context instance = null;

	private LoginIdentity loginIdentity;

	private String getLoginErrorTip(int errorCode) {
		switch (errorCode) {
		case ErrorCode.E_CONNECT_LOGIN_SERVER_FAILED:
			return getString(R.string.connect_login_server_failed);
		case ErrorCode.E_REQ_MSG_SERVER_ADDRS_FAILED:
			return getString(R.string.req_msg_server_addrs_failed);
		case ErrorCode.E_CONNECT_MSG_SERVER_FAILED:
			return getString(R.string.connect_msg_server_failed);
		case ErrorCode.E_LOGIN_MSG_SERVER_FAILED:
			return getString(R.string.login_msg_server_failed);
		case ErrorCode.E_LOGIN_GENERAL_FAILED:
			return getString(R.string.login_error_general_failed);

		default:
			return getString(R.string.login_error_unexpected);

		}
	}

	private void onLoginError(int errorCode) {
		logger.e("login#onLoginError -> errorCode:%d", errorCode);

		String errorTip = getLoginErrorTip(errorCode);
		logger.d("login#errorTip:%s", errorTip);

		mLoginStatusView.setVisibility(View.GONE);

		Toast.makeText(this, errorTip, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub

		logger.d("login#onAction -> action:%s", action);

		if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
			int errorCode = intent.getIntExtra(
					SysConstant.lOGIN_ERROR_CODE_KEY, -1);

			if (errorCode != ErrorCode.S_OK) {
				onLoginError(errorCode);
			} else {
				onLoginSuccess();
			}
		}
	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub

		logger.d("login#onIMServiceConnected");
		IMService imService = imServiceHelper.getIMService();
		if (imService == null) {
			return;
		}

		imLoginMgr = imService.getLoginManager();

		if (imLoginMgr != null) {
			logger.i("chat#connect im service ok");
		}

		try {
			loginIdentity = imService.getDbManager().loadLoginIdentity();

			logger.d("login#loginId:%s", loginIdentity.loginId);

			mNameView.setText(loginIdentity.loginId);
			mPasswordView.setText(loginIdentity.pwd);
		} catch (Exception e) {
			logger.w("loadIdentity failed");
		}
	}

	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		logger.d("login#onCreate");
		
		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_LOGIN_RESULT);
		if (!imServiceHelper.connect(getApplicationContext(), actions,
				IMServiceHelper.INTENT_NO_PRIORITY, this)) {
			logger.e("login#fatal,  connect im service failed");
		}

//		 if (true) {
//		 CommonTest.test();
//		 return;
//		 }

		IMEntrance.getInstance().setContext(LoginActivity.this);

		initHandler();

		setContentView(R.layout.tt_activity_login);

		instance = this;

		mNameView = (EditText) findViewById(R.id.name);

		mPasswordView = (EditText) findViewById(R.id.password);

		mPasswordView

		.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView textView, int id,

			KeyEvent keyEvent) {

				if (id == R.id.login || id == EditorInfo.IME_NULL) {

					attemptLogin();

					return true;

				}

				return false;

			}

		});

		mLoginFormView = findViewById(R.id.login_form);

		mLoginStatusView = findViewById(R.id.login_status);

		findViewById(R.id.sign_in_button).setOnClickListener(

		new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				attemptLogin();

				// if (NetworkUtil.isNetWorkAvalible(LoginActivity.this)) {
				//
				//
				//
				// } else {
				// mPasswordView.setError(getString(R.string.invalid_network));
				// mPasswordView.requestFocus();
				// }

			}

		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceHelper.disconnect(getApplicationContext());
	}

	public void attemptLogin() {

		// mNameView.setError(null);
		//
		// mPasswordView.setError(null);

		loginName = mNameView.getText().toString();

		String mPassword = mPasswordView.getText().toString();

		boolean cancel = false;

		View focusView = null;

		if (TextUtils.isEmpty(mPassword)) {

			// mPasswordView.setError(getString(R.string.error_field_required));
			Toast.makeText(this, getString(R.string.error_password_required),
					Toast.LENGTH_SHORT).show();

			focusView = mPasswordView;

			cancel = true;

		} else if (mPassword.length() < 4) {

			// mPasswordView.setError(getString(R.string.error_invalid_password));
			//
			// focusView = mPasswordView;
			//
			// cancel = true;

		}

		if (TextUtils.isEmpty(loginName)) {

			// mNameView.setError(getString(R.string.error_field_required));
			Toast.makeText(this, getString(R.string.error_field_required),
					Toast.LENGTH_SHORT).show();

			focusView = mNameView;

			cancel = true;

		}

		if (cancel) {

			focusView.requestFocus();

		} else {

			showProgress(true);
			// mPasswordView.setFocusable(false);
			// mNameView.setFocusable(false);

			// login(mName, mPassword);
			// IMEntrance.getInstance().initTask(this, loginNickName,
			// mPassword);
			if (imLoginMgr != null) {
				boolean userNameChanged = true;
				boolean pwdChanged = true;
				if (loginIdentity != null) {
					if (loginName.equals(loginIdentity.loginId)) {
						logger.d("login#username is not changed");
						userNameChanged = false;
					}

					if (mPassword.equals(loginIdentity.pwd)) {
						logger.d("login#pwd is not changed");
						pwdChanged = false;
					}
				}
				imLoginMgr.login(loginName, mPassword, userNameChanged,
						pwdChanged);
			}

		}

	}

	@Override
	public void onBackPressed() {
		logger.d("login#onBackPressed");
		if (imLoginMgr != null) {
			imLoginMgr.cancel();
		}

		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	private void showProgress(final boolean show) {
		if (show) {
			mLoginStatusView.setVisibility(View.VISIBLE);
		} else {
			mLoginStatusView.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			LoginActivity.this.finish();

			return true;

		}

		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void initHandler() {

		// uiHandler = new Handler() {
		//
		// @Override
		// public void handleMessage(Message msg) {
		//
		// super.handleMessage(msg);
		//
		// switch (msg.what) {
		// case HandlerConstant.HANDLER_LOGIN_MSG_SERVER:
		// onLoginSuccess();
		// break;
		// case HandlerConstant.HANDLER_LOGIN_MSG_SERVER_FAILED:
		// onLoginFailed(getString(R.string.login_failed));
		// break;
		// case HandlerConstant.HANDLER_LOGIN_MSG_SERVER_TIMEOUT:
		// onLoginFailed(getString(R.string.login_timeout));
		// break;
		// default:
		// onLoginFailed(getString(R.string.error_incorrect_user));
		// break;
		// }
		// }
		// };
	}

	// private void onLoginFailed(String tip) {
	// mPasswordView.setError(tip);
	// mPasswordView.requestFocus();
	// mLoginStatusView.setVisibility(View.GONE);
	// }

	private void onLoginSuccess() {
		logger.i("login#onLoginSuccess");

		// todo eric remove it
		CacheHub.getInstance()
				.setLoginUser(
						imServiceHelper.getIMService().getLoginManager()
								.getLoginUser());

		// todo eric remove this
		// Intent i = new Intent();
		// i.setAction(SysConstant.START_SERVICE_ACTION);
		// LoginActivity.this.sendBroadcast(i);

		Intent intent = new Intent(LoginActivity.this, MainActivity.class);

		startActivity(intent);

		LoginActivity.this.finish();
	}

	@Override
	protected void onStop() {

		NetStateDispach.getInstance().unregister(LoginActivity.class);

		super.onStop();

	}

}
