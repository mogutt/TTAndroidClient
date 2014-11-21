package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.jboss.netty.channel.StaticChannelPipeline;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.mogujie.tt.cache.biz.CacheHub;
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
import com.mogujie.tt.imlib.proto.MsgServerPacket.PacketRequest.Entity;
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

	private static final int LOGIN_ERROR_TOKEN_EXPIRED = 6;

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
	private boolean retryReqLoginServerAddrsFlag = false;
	private boolean logoutFlag = false;
	private int msgServerErrorCode = 0;

	public boolean isLogout() {
		return logoutFlag;
	}

	public void setLogout(boolean logout) {
		logger.d("login#setLogout");

		this.logoutFlag = logout;
		//		logger.d("login#setLogout:%s", Log.getStackTraceString(new Throwable()));
	}

	private AsyncHttpClient client = new AsyncHttpClient();
	private PersistentCookieStore myCookieStore;

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

	private static final int STATUS_REQ_LOGIN_SERVER_ADDRS = -1;
	private static final int STATUS_CONNECT_LOGIN_SERVER = 0;
	private static final int STATUS_REQ_MSG_SERVER_ADDRS = 1;
	private static final int STATUS_CONNECT_MSG_SERVER = 2;
	private static final int STATUS_LOGINING_MSG_SERVER = 3;
	private static final int STATUS_LOGIN_OK = 4;
	private static final int STATUS_LOGIN_FAILED = 5;
	private static final int STATUS_MSG_SERVER_DISCONNECTED = 6;

	private int currentStatus = STATUS_REQ_LOGIN_SERVER_ADDRS;

	@Override
	public void setContext(Context context) {
		super.setContext(context);

		myCookieStore = new PersistentCookieStore(ctx);
		client.setCookieStore(myCookieStore);
	}

	public void logOut() {
		//		if not login, do nothing
		//		send logOuting message, so reconnect won't react abnormally
		//		but when reconnect start to work again?use isEverLogined
		//		close the socket
		//		 send logOuteOk message
		//		mainactivity jumps to login page

		logger.d("login#logOut");

		logger.d("login#stop reconnecting");
		//		everlogined is enough to stop reconnecting
		setEverLogined(false);
		//		ctx.sendBroadcast(new Intent(IMActions.ACTION_LOGOUTING));
		setLogout(true);

		disconnectMsgServer();

		logger.d("login#send logout finish message");
		ctx.sendBroadcast(new Intent(IMActions.ACTION_LOGOUT));
	}

	public boolean isEverLogined() {
		return everLogined;
	}

	public void setEverLogined(boolean everLogined) {
		this.everLogined = everLogined;
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

	public boolean reloginFromStart() {
		logger.d("login#reloginFromStart");

		if (isDoingLogin()) {
			logger.d("login#isDoingLogin, no need");
			return false;
		}

		login(loginUserName, loginPwd, true, true);

		return true;
	}

	public boolean relogin() {
		logger.d("login#relogin");

		connectLoginServer();

		return true;
	}

	public void login(String userName, String password,
			boolean userNameChanged, boolean pwdChanged) {

		if (ctx != null) {
			ctx.sendBroadcast(new Intent(IMActions.ACTION_DOING_LOGIN));
		}

		logger.i("login#login -> userName:%s, userNameChanged:%s, pwdChanged:%s", userName, userNameChanged, pwdChanged);

		loginUserName = userName;
		loginPwd = password;
		if(pwdChanged){
			loginPwd = Md5Helper.encode(password);
		}else{
			loginPwd = password;
		}

		identityChanged = userNameChanged || pwdChanged;

		connectLoginServer();
	}

	private void connectLoginServer() {
		currentStatus = STATUS_CONNECT_LOGIN_SERVER;
		String ip = ProtocolConstant.LOGIN_IP1;
		int port = ProtocolConstant.LOGIN_PORT;

		logger.i("login#connect login server -> (%s:%d)", ip, port);

		if (loginServerThread != null) {
			loginServerThread.close();
			loginServerThread = null;
		}
		
 		loginServerThread = new SocketThread(ip, port, new LoginServerHandler());
		loginServerThread.start();
	}

	public void cancel() {
		// todo eric
		logger.i("login#cancel");
	}

	public void onLoginServerUnconnected() {
		logger.i("login#onLoginServerUnConnected");

		IMLoginManager.instance().onLoginFailed(ErrorCode.E_CONNECT_LOGIN_SERVER_FAILED);
	}

	public void onLoginFailed(int errorCode) {
		logger.i("login#onLoginFailed -> errorCode:%d", errorCode);

		currentStatus = STATUS_LOGIN_FAILED;
		loggined = false;

		Intent intent = new Intent(IMActions.ACTION_LOGIN_RESULT);
		intent.putExtra(SysConstant.lOGIN_ERROR_CODE_KEY, errorCode);
		if (errorCode == ErrorCode.E_MSG_SERVER_ERROR_CODE) {
			intent.putExtra(SysConstant.KEY_MSG_SERVER_ERROR_CODE, msgServerErrorCode);
		}

		if (ctx != null) {
			logger.i("login#broadcast login failed");
			ctx.sendBroadcast(intent);
		}
	}

	public void onLoginOk() {
		logger.i("login#onLoginOk");

		setLogout(false);

		currentStatus = STATUS_LOGIN_OK;

		loggined = true;
		everLogined = true;

		if (identityChanged) {
			IMDbManager.instance(ctx).saveLoginIdentity(loginUserName, loginPwd);
		}

		Intent intent = new Intent(IMActions.ACTION_LOGIN_RESULT);
		intent.putExtra(SysConstant.lOGIN_ERROR_CODE_KEY, ErrorCode.S_OK);
		if (ctx != null) {
			logger.i("login#broadcast login ok");
			ctx.sendBroadcast(intent);
		}
	}

	public void onLoginServerConnected() {
		logger.i("login#onLoginServerConnected");

		reqMsgServerAddrs();
	}

	public void onLoginServerDisconnected() {
		logger.e("login#onLoginServerDisconnected");

		// todo eric is enum capable of comparing just like int?
		if (currentStatus < STATUS_CONNECT_MSG_SERVER) {
			logger.e("login server disconnected unexpectedly");
			onLoginFailed(ErrorCode.E_REQ_MSG_SERVER_ADDRS_FAILED);
		}
	}

	public void reqMsgServerAddrs() {
		logger.i("login#reqMsgServerAddrs");

		currentStatus = STATUS_REQ_MSG_SERVER_ADDRS;

		if (loginServerThread != null) {
			Entity entity = new Entity();
			entity.userType = 0;
			loginServerThread.sendPacket(new MsgServerPacket(entity));
			logger.d("login#send packet ok");
		}
	}

	public void onRepMsgServerAddrs(DataBuffer buffer) {
		logger.i("login#onRepMsgServerAddrs");

		MsgServerPacket packet = new MsgServerPacket();
		packet.decode(buffer);

		MsgServerPacket.PacketResponse resp = (MsgServerPacket.PacketResponse) packet.getResponse();

		if (resp == null) {
			logger.e("login#decode MsgServerResponse failed");
			onLoginFailed(ErrorCode.E_REQ_MSG_SERVER_ADDRS_FAILED);
			return;
		}

		if (msgServerAddrs == null) {
			msgServerAddrs = new ArrayList<String>();
		}

		msgServerAddrs.add(resp.entity.ip1);
		msgServerAddrs.add(resp.entity.ip2);

		msgServerPort = resp.entity.port;

		logger.i("login#msgserver ip1:%s,  login ip2:%s, port:%d", resp.entity.ip1, resp.entity.ip2, resp.entity.port);

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
		//		logger.i("login#disconnectMsgServer, callstack:%s", Log.getStackTraceString(new Throwable()));
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

		if (msgServerThread != null) {
			msgServerThread.close();
			msgServerThread = null;
		}
		
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
			com.mogujie.tt.imlib.proto.LoginPacket.PacketRequest.Entity entity = new com.mogujie.tt.imlib.proto.LoginPacket.PacketRequest.Entity();
			entity.name = loginUserName;
			entity.pass = loginPwd;
			entity.onlineStatus = ProtocolConstant.ON_LINE;
			entity.clientType = ProtocolConstant.CLIENT_TYPE;
			entity.clientVersion = ProtocolConstant.CLIENT_VERSION;
			msgServerThread.sendPacket(new LoginPacket(entity));
		}
	}

	public void onRepMsgServerLogin(DataBuffer buffer) {
		logger.i("login#onRepMsgServerLogin");

		LoginPacket packet = new LoginPacket();
		packet.decode(buffer);

		LoginPacket.PacketResponse resp = (LoginPacket.PacketResponse) packet.getResponse();

		if (resp == null) {
			logger.e("login#decode LoginResponse failed");
			onLoginFailed(ErrorCode.E_LOGIN_MSG_SERVER_FAILED);
			return;
		}

		int loginResult = resp.entity.result;
		this.msgServerErrorCode = loginResult;

		logger.d("login#loginResult:%d", loginResult);

		if (loginResult == 0) {
			loginUser = resp.getUser();
			loginUser.setUserId(resp.entity.userId);
			setLoginId(resp.entity.userId);

			onLoginOk();
		} else {
			logger.e("login#login msg server failed, result:%d", loginResult);

			// todo eric right now, no detail failed reason
			onLoginFailed(ErrorCode.E_MSG_SERVER_ERROR_CODE);

			if (loginResult == LOGIN_ERROR_TOKEN_EXPIRED) {
				logger.e("login#error:token expired");

				reloginFromStart();
			}
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

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
