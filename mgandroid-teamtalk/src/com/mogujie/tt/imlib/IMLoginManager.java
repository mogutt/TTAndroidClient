package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.db.IMDbManager;
import com.mogujie.tt.imlib.network.LoginServerHandler;
import com.mogujie.tt.imlib.network.MsgServerHandler;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.LoginPacket;
import com.mogujie.tt.imlib.proto.MsgServerPacket;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.ui.utils.Md5Helper;

public class IMLoginManager extends IMManager {
	private static IMLoginManager inst;

	public static IMLoginManager instance() {
		synchronized (IMLoginManager.class) {
			if (inst == null) {
				inst = new IMLoginManager();
			}

			return inst;
		}
	}

	private Logger logger = Logger.getLogger(IMLoginManager.class);
	private String loginUserName;
	private String loginPwd;
	private String loginId;
	private SocketThread loginServerThread;
	private List<String> msgServerAddrs;
	private int msgServerPort;
	private SocketThread msgServerThread;
	// todo eric make it to ContactEntity too
	private User loginUser;
	private static final int MSG_SERVER_DISCONNECTED_EVENT = -1;
	private boolean loggined = false;
	private boolean everLogined = false;
	private boolean identityChanged = false;
	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			if (msg.what == MSG_SERVER_DISCONNECTED_EVENT) {
				IMLoginManager.instance().disconnectMsgServer();
			}

			// // todo eric don't put ui stuff here
			// Toast.makeText(ctx, "网络发生错误，已和服务器断开连接",
			// Toast.LENGTH_LONG).show();

			super.handleMessage(msg);
		}

	};

	private static final int STATUS_CONNECT_LOGIN_SERVER = 0;
	private static final int STATUS_REQ_MSG_SERVER_ADDRS = 1;
	private static final int STATUS_CONNECT_MSG_SERVER = 2;
	private static final int STATUS_LOGINING_MSG_SERVER = 3;
	private static final int STATUS_LOGIN_OK = 4;
	private static final int STATUS_LOGIN_FAILED = 5;
	private static final int STATUS_MSG_SERVER_DISCONNECTED = 6;

	private int currentStatus = STATUS_CONNECT_LOGIN_SERVER;

	public boolean isEverLoginned() {
		return everLogined;
	}

	public boolean isLoggined() {
		return loggined;
	}

	public User getLoginUser() {
		return loginUser;
	}

	public boolean isDoingLogin() {
		return currentStatus <= STATUS_LOGINING_MSG_SERVER;
	}
	
	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		logger.d("login#setLoginId -> loginId:%s", loginId);
		this.loginId = loginId;
	}

	public IMLoginManager() {
		logger.d("login#creating IMLoginManager");
	}

	public boolean relogin() {
		logger.d("login#relogin");
		
		if (isDoingLogin()) {
			logger.d("login#isDoingLogin, no need");
			return false;
		}
		
		if (loggined) {
			logger.d("login#already logined, no need");
			return false;
		}
		
		connectLoginServer();
		
		return true;
	}

	public void login(String userName, String password,
			boolean userNameChanged, boolean pwdChanged) {
		logger.i(
				"login#login -> userName:%s, userNameChanged:%s, pwdChanged:%s",
				userName, userNameChanged, pwdChanged);

		loginUserName = userName;

		if (pwdChanged) {
			loginPwd = Md5Helper.encode(password);
		} else {
			loginPwd = password;
		}

		identityChanged = userNameChanged || pwdChanged;

		connectLoginServer();
	}

	private void connectLoginServer() {
		String ip = ProtocolConstant.LOGIN_IP1;
		int port = ProtocolConstant.LOGIN_PORT;

		logger.i("login#connect login server -> (%s:%d)", ip, port);

		loginServerThread = new SocketThread(ip, port, new LoginServerHandler());
		loginServerThread.start();
	}

	public void cancel() {
		// todo eric
		logger.i("login#cancel");
	}

	public void onLoginServerUnconnected() {
		logger.i("login#onLoginServerUnConnected");

		IMLoginManager.instance().onLoginFailed(
				ErrorCode.E_CONNECT_LOGIN_SERVER_FAILED);
	}

	public void onLoginFailed(int errorCode) {
		logger.i("login#onLoginFailed -> errorCode:%d", errorCode);

		currentStatus = STATUS_LOGIN_FAILED;
		loggined = false;

		Intent intent = new Intent(IMActions.ACTION_LOGIN_RESULT);
		intent.putExtra(SysConstant.lOGIN_ERROR_CODE_KEY, errorCode);
		if (ctx != null) {
			logger.i("login#broadcast login failed");
			ctx.sendBroadcast(intent);
		}
	}

	public void onLoginOk() {
		logger.i("login#onLoginOk loginUser:%s", loginUser);

		currentStatus = STATUS_LOGIN_OK;

		loggined = true;
		everLogined = true;

		if (identityChanged) {
			IMDbManager.instance(ctx)
					.saveLoginIdentity(loginUserName, loginPwd);
		}

		Intent intent = new Intent(IMActions.ACTION_LOGIN_RESULT);
		intent.putExtra(SysConstant.lOGIN_ERROR_CODE_KEY, ErrorCode.S_OK);
		if (ctx != null) {
			logger.i("login#broadcast login ok");
			ctx.sendBroadcast(intent);
		}

		fetchData();
	}

	private void fetchData() {
		logger.i("login#fetch data");

		IMContactManager.instance().fetchContacts();
		IMGroupManager.instance().fetchGroupList();
	}

	public void onLoginServerConnected() {
		logger.i("login#onLoginServerConnected");

		fetchMsgServerAddrs();
	}

	public void onLoginServerDisconnected() {
		logger.e("login#onLoginServerDisconnected");

		// todo eric is enum capable of comparing just like int?
		if (currentStatus < STATUS_CONNECT_MSG_SERVER) {
			logger.e("login server disconnected unexpectedly");
			onLoginFailed(ErrorCode.E_REQ_MSG_SERVER_ADDRS_FAILED);
		}
	}

	public void fetchMsgServerAddrs() {
		logger.i("login#fetchMsgServerAddr");

		currentStatus = STATUS_REQ_MSG_SERVER_ADDRS;

		if (loginServerThread != null) {
			loginServerThread.sendPacket(new MsgServerPacket());
			logger.d("login#send packet ok");
		}
	}

	public void onRepMsgServerAddrs(DataBuffer buffer) {
		logger.i("login#onRepMsgServerAddrs");

		MsgServerPacket packet = new MsgServerPacket();
		packet.decode(buffer);

		MsgServerPacket.MsgServerResponse resp = (MsgServerPacket.MsgServerResponse) packet
				.getResponse();

		if (resp == null) {
			logger.e("login#decode MsgServerResponse failed");
			onLoginFailed(ErrorCode.E_REQ_MSG_SERVER_ADDRS_FAILED);
			return;
		}

		if (msgServerAddrs == null) {
			msgServerAddrs = new ArrayList<String>();
		}

		msgServerAddrs.add(resp.getStrIp1());
		msgServerAddrs.add(resp.getStrIp2());

		msgServerPort = resp.getPort();

		logger.i("login#msgserver ip1:%s,  login ip2:%s, port:%d",
				resp.getStrIp1(), resp.getStrIp2(), resp.getPort());

		connectMsgServer();
	}

	private String pickLoginServerIp() {
		// todo eric
		// pick the second one right now
		return msgServerAddrs.get(1);
	}

	private void disconnectLoginServer() {
		logger.i("login#disconnectLoginServer");

		if (loginServerThread != null) {
			loginServerThread.close();

			logger.i("login#do real disconnectLoginServer ok");

			// todo eric
			// loginServerThread = null;
		}
	}

	private void disconnectMsgServer() {
		logger.i("login#disconnectMsgServer");

		if (msgServerThread != null) {
			msgServerThread.close();
			logger.i("login#do real disconnectMsgServer ok");

			// msgServerThread = null;
		}
	}

	private void connectMsgServer() {
		currentStatus = STATUS_CONNECT_MSG_SERVER;

		disconnectLoginServer();

		String ip = pickLoginServerIp();

		logger.i("login#connectMsgServer -> (%s:%d)", ip, msgServerPort);

		msgServerThread = new SocketThread(ip, msgServerPort,
				new MsgServerHandler());
		msgServerThread.start();
	}

	public void onMessageServerUnconnected() {
		logger.i("login#onMessageServerUnconnected");
		onLoginFailed(ErrorCode.E_CONNECT_MSG_SERVER_FAILED);
	}

	public void onMsgServerConnected() {
		logger.i("login#onMsgServerConnected");

		reqLoginMsgServer();
	}

	private void broadcastDisconnectWithServer() {
		logger.i("login#broadcastDisconnectWithServer");

		if (ctx != null) {
			ctx.sendBroadcast(new Intent(IMActions.ACTION_SERVER_DISCONNECTED));
		}
	}

	public void onMsgServerDisconnected() {
		logger.w("login#onMsgServerDisconnected");

		if (currentStatus < STATUS_LOGIN_OK) {
			onLoginFailed(ErrorCode.E_LOGIN_MSG_SERVER_FAILED);
		} else {
			broadcastDisconnectWithServer();
		}

		currentStatus = STATUS_MSG_SERVER_DISCONNECTED;

		loggined = false;

		// only 2 threads(ui thread, network thread) would request sending
		// packet
		// let the ui thread to close the connection
		// so if the ui thread has a sending task, no synchronization issue
		handler.sendEmptyMessage(MSG_SERVER_DISCONNECTED_EVENT);

	}

	private void reqLoginMsgServer() {
		logger.i("login#reqLoginMsgServer");

		currentStatus = STATUS_LOGINING_MSG_SERVER;

		if (msgServerThread != null) {
			msgServerThread.sendPacket(new LoginPacket(loginUserName, loginPwd,
					ProtocolConstant.ON_LINE, ProtocolConstant.CLIENT_TYPE,
					ProtocolConstant.CLIENT_VERSION));
		}
	}

	public void onRepMsgServerLogin(DataBuffer buffer) {
		logger.i("login#onRepMsgServerLogin");

		LoginPacket packet = new LoginPacket();
		packet.decode(buffer);

		LoginPacket.LoginResponse resp = (LoginPacket.LoginResponse) packet
				.getResponse();

		if (resp == null) {
			logger.e("login#decode LoginResponse failed");
			onLoginFailed(ErrorCode.E_LOGIN_MSG_SERVER_FAILED);
			return;
		}

		int loginResult = resp.getResult();

		logger.d("login#loginResult:%d", loginResult);

		if (loginResult == 0) {
			loginUser = resp.getUser();
			setLoginId(loginUser.getUserId());

			onLoginOk();
		} else {
			// todo eric right now, no detail failed reason
			onLoginFailed(ErrorCode.E_LOGIN_GENERAL_FAILED);
		}
	}

	public SocketThread getMsgServerChannel() {
		return msgServerThread;
	}

	public void disconnect() {
		logger.d("login#disconnect");

		disconnectLoginServer();
		disconnectMsgServer();
	}
}
