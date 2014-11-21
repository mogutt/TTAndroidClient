package com.mogujie.tt.config;

import android.media.AudioManager;

public interface SysConstant {
	String KEY_AVATAR_URL = "key_avatar_url";
	String KEY_IS_IMAGE_CONTACT_AVATAR = "is_image_contact_avatar";
	String KEY_DONT_MODIFY_IMAGE_URI = "dont_modify_image_uri";
	String KEY_LOGIN_NOT_AUTO = "login_not_auto";
	String KEY_USER_PROFILE_ID = "user_profile_id";
	String KEY_LOCATE_DEPARTMENT = "key_locate_department";
	String KEY_MSG_SERVER_ERROR_CODE = "key_msg_server_error_code";
	String lOGIN_ERROR_CODE_KEY = "login_error_code";
	String CONTACT_ID_KEY = "contact_id";
	String FROM_ID_KEY = "from_id";
	String SEQ_NO_KEY = "seq_no";
	String MSG_KEY = "msg";
	String MSG_ID_KEY = "msg_id";
	String KEY_SESSION_TYPE = "session_type";
	String KEY_SESSION_ID = "session_id";
	String OPERATION_RESULT_KEY = "tt_opeartion_result";
	String STATUS_KEY = "status";
	
	int SEX_FEMALE = 0;
	int SEX_MAILE = 1;				
	String AVATAR_URL_PREFIX = "http://122.225.68.125:8600/";
	String DOWNLOAD_IMAGE_URL_REPFIX = "http://122.225.68.125:8600/";
	String UPLOAD_IMAGE_URL_PREFIX = "http://122.225.68.125:8600/";
	
	public static final int PROTOCOL_HEADER_LENGTH = 12;// 默认消息头的长度
	public static final int PROTOCOL_VERSION = 1;
	public static final char PROTOCOL_RESERVED = '0';
	public static final int DEFAULT_SERVICEID = 1000;// 心跳包
	public static final int AUDIO_RECORD_MAX_LENGTH = 120;

	public static final int REQUEST_LOGIN_SUCCESS = 0;
	public static final int DEFAULT_MESSAGE_ID = -1;

	public static final String PREVIEW_TEXT_CONTENT = "content";

	public static final int CHAT_SEARCH_RESULT_TYPE_RESULT = 0;
	public static final int CHAT_SEARCH_RESULT_TYPE_CATEGORY = 1;

	public static final String CONTACT_ACTIVITY = "com.mogujie.tt.ui.activity.ContactFragmentActivity"; // 用它给getClassName作对比，包名改变时注意同步

	public static final String MESSAGE_ACTIVITY = "com.mogujie.tt.ui.activity.MessageActivity";




	// 图片消息占位符
	public static final String MESSAGE_IMAGE_LINK_START = "&$#@~^@[{:";
	public static final String MESSAGE_IMAGE_LINK_END = ":}]&$~@#@";

	public static final String EXTRA_IMAGE_LIST = "imagelist";
	public static final String EXTRA_ALBUM_NAME = "name";
	public static final String EXTRA_ADAPTER_NAME = "adapter";
	public static final String EXTRA_CHAT_USER_ID = "chat_user_id";

	/**
	 * 启动IMService的广播
	 */
	public static final String START_SERVICE_ACTION = "com.mougjie.tt.startService";

	// 语音消息占位符
	public static final String MESSAGE_AUDIO_LINK_START = "";
	public static final String MESSAGE_AUDIO_LINK_END = "";

	public static final int MESSAGE_STATE_UNLOAD = 0X0000;
	public static final int MESSAGE_STATE_LOADDING = 0X0001;
	public static final int MESSAGE_STATE_FINISH_SUCCESSED = 0X0002;
	public static final int MESSAGE_STATE_FINISH_FAILED = 0X0003;
	public static final int STOP_PLAY_VOICE = 0X0004;

	/**
	 * 消息状态
	 */
	public static final int UPLOAD_FAILED = 0X0005;
	public static final int UPLOAD_SUCCESSED = 0X0006;

	/**
	 * 消息类型
	 */
	public static final int DISPLAY_TYPE_TEXT = 0X0007;
	public static final int DISPLAY_TYPE_AUDIO = 0X0008;
	public static final int DISPLAY_TYPE_IMAGE = 0X0009;
	public static final String MSG_OVERVIEW_DISPLAY_TYPE_AUDIO = "[ 语音 ]";
	public static final String MSG_OVERVIEW_DISPLAY_TYPE_IMAGE = "[ 图片 ]";
	public static final String MSG_OVERVIEW_DISPLAY_TYPE_OTHERS = "[ 其它消息 ]";

	public static final int RANDOM_TYPE_FILENAME = 0X00010;
	public static final int RANDOM_TYPE_MSEESAGE_REQUESTNO = 0X00011;
	public static final int DEFAULT_EMO = 0X00012;
	public static final int FILE_SAVE_TYPE_IMAGE = 0X00013;
	public static final int FILE_SAVE_TYPE_AUDIO = 0X00014;

	public static final int EVENT_UNREAD_MSG = 0X0001; // 未读消息计数通知事件
	public static final int EVENT_RECENT_INFO_CHANGED = 0X0002;

	// 联系人信息变化通知事件
	public static final int MESSAGE_QUEUE_LIMIT = 40; // 消息队列大小

	// 消息是否已读
	public static final int MESSAGE_UNREAD = 0X0000; // 消息未读
	public static final int MESSAGE_ALREADY_READ = 0X0001; // 消息已读
	public static final int MESSAGE_DISPLAYED = 0X0002;// 消息已展现

	// public static final int DOWNLOAD_IMAGE_FAILED1 = 0X0019; // 图片下载失败
	public static final int DOWNLOAD_IMAGE_SUCCESSED = 0X0020; // 图片下载成功
	public static final int DOWNLOAD_AUDIO_FAILED = 0X0021; // 语音下载失败
	public static final int DOWNLOAD_AUDIO_SUCCESSED = 0X0022; // 语音下载失败

	public static final int RECORD_AUDIO_TOO_SHORT = 0X0025;

	public static final int HIDE_OTHER_PANNEL = 0X0026;

	public static final byte MESSAGE_TYPE_TELETEXT = 1; // 消息类型 1:
														// 图文消息（文本或图片）；
														// 100: 语音信息
	public static final byte MESSAGE_TYPE_AUDIO = 100; // 消息类型 1: 图文消息（文本或图片）；
														// 100: 语音信息

	public static final float MAX_SOUND_RECORD_TIME = 60.0f;// 单位秒

	public static final int MAX_SELECT_IMAGE_COUNT = 6;

	public static final int EVER_LOAD_IMAGE_COUNT = 30;

	public static final int MIN_SOUND_RECORD_TIME = 1;

	public static final int pageSize = 21;

	public static final int DEFAULT_FRIEND_TYPE = 1;

	public static final int HTTP_SUCCESS_STATUS_CODE = 1001;
	public static final int CONVERT_TOEKN_SUCCESS = 1001;
	public static final int WIRELESS_TOKEN_INVALID = 4003;
	public static final int CONVERT_TOKEN_FAILED = 1000;

	public static final int DEFAULT_VIEW_PAGER_HEIGHT = 140;

	public static final long RANDOM_FILE_MARK_MIN = 1;
	public static final long RANDOM_FILE_MARK_MAX = 1000;

	public static final long RANDOM_MSG_REQUESTNO_MIN = 50000;
	public static final long RANDOM_MSG_REQUESTNO_MAX = 80000;

	public static final int WEB_IMAGE_MIN_WIDTH = 100;
	public static final int WEB_IMAGE_MIN_HEIGHT = 100;


	public static final String CHOOSE_CONTACT = "CHOOSE_CONTACT";
	public static final String READCOUNT = "READ_COUNT";
	public static final String IS_FROM_MESSAGE_ACTIVITY = "IS_FROM_MESSAGE_ACTIVITY";
	public static final String MSG_SERVER_INFO_IP1 = "MSG_SERV_INFO_IP1";
	public static final String MSG_SERVER_INFO_IP2 = "MSG_SERV_INFO_IP2";
	public static final String MSG_SERVER_INFO_PORT = "MSG_SERV_INFO_PORT";

	public static final String DEFAULT_AUDIO_SUFFIX = ".spx";
	public static final int MAX_RECONNECT_COUNT = 10;
	public static final int MAX_HEART_BEAT_TIME = 60;

	public static final int IMAGE_MESSAGE_DEFAULT_WIDTH = 100;

	public static final int IMAGE_MESSAGE_DEFAULT_HEIGHT = 100;

	public static final int CAMERA_WITH_DATA = 3023;
	public static final int MEDIA_TYPE_IMAGE = 1;

	public static final String CUR_MESSAGE = "CUR_MESSAGE";

	public static String MD5_KEY = "%032xxnMGJ";
	public static final String DEFAULT_IMAGE_FORMAT = ".jpg";
	@SuppressWarnings("unused")
	public static final String DEFAULT_AUDIO_FORMAT = ".spx";

	// 语音播放模式
	public static final int AUDIO_PLAY_MODE_NORMAL = AudioManager.MODE_NORMAL;
	public static final int AUDIO_PLAY_MODE_IN_CALL = AudioManager.MODE_IN_CALL;

	public static final int MAX_CONTACTS_COUNT = 100;
	// 状态
	public static final int SOCKET_STATUS_OFFLINE = 0;
	public static final int SOCKET_STATUS_ONLINE = 1;

	// 网络状态
	public static final int NETWORK_STATUS_OFFLINE = 0;
	public static final int NETWORK_STATUS_ONLINE = 1;

	// 链接
	public static final String CONNECT_MSG_SERVER = "MSG_SERVER_CONNECTION";
	public static final String CONNECT_LOGIN_SERVER = "LOGIN_SERVER_CONNECTION";
	public static final String CONNECT_HANDLE = "CONNNECT_HANDLE";

	public static final int SOCKET_LOGIN_SERVER = 1;
	public static final int SOCKET_MSG_SERVER1 = 2;
	public static final int SOCKET_MSG_SERVER2 = 3;

	// Message从哪里跳转过来
	public static final String IS_REFRESH_LIST = "IS_REFRESH_LIST"; // 跳转时是否刷新聊天界面

	public static final int HISTORY_PULL_PER_NUM = 5;// 拉取消息一次获取个数

	public static final String OBJECT_PARAM = "OBJECT_PARAM";

	public static final String APPLICATION_PACKAGE_NAME = "com.mogujie.tt";


	public static final String USER_DETAIL_PARAM = "FROM_PAGE";

	public static final int ALBUM_PREVIEW_BACK = 3;
	public static final int ALBUM_BACK_DATA = 5;

	public static final int GROUP_MANAGER_ADD_RESULT = 6;

	public static final int GROUP_MANAGER_GRID_ROW_SIZE = 4;

	public static final long BLOCK_USER_CHECK_INTERVAL = 15 * 60 * 1000;// 每15分钟检测下黑名单

	public static final int POPUP_MENU_TYPE_TEXT = 1;

	public static final int POPUP_MENU_TYPE_IMAGE = 2;

	public static final int POPUP_MENU_TYPE_AUDIO = 3;

	public static final int HTTP_TIME_OUT = 10 * 1000;

	public static final int WAITING_LIST_MONITOR_INTERVAL = 5000;
	public static final int DEFAULT_PACKET_SEND_MONTOR_INTERVAL = 10;

	// 最近联系人刷新间隔： 单位/秒
	public static final int PULL_TO_REFRESH_INTERVAL = 3;

	// MessageActivity显示进度条时间间隔：单位/秒
	public static final int SHOW_PROGRESS_BAR_INTERVAL = 5;

	public static final int DEFAULT_CUSTOM_SERVICE_TYPE = 23;
	
	public static final String WEBVIEW_URL = "WEBVIEW_URL";
}
