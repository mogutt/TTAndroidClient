package com.mogujie.tt.config;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.Header;

public class ProtocolConstant {

	public static class ProtocolDumper {
		
		private static Logger logger = Logger.getLogger(ProtocolDumper.class);

//		private static String[] sid = new String[SID_MAX];
//		private static String[] loginCid = new String[CID_LOGIN_MAX];
//		private static String[] buddyListCid = new String[CID_BUDDY_LIST_MAX];
//		private static String[] msgCid = new String[CID_MSG_MAX];
//		private static String[] groupCid = new String[CID_GROUP_MAX];
//
//		public static String[] getCidList(int serviceId) {
//			switch (serviceId) {
//			case SID_LOGIN:
//				return loginCid;
//			case SID_MSG:
//				return msgCid;
//			case SID_BUDDY_LIST:
//				return buddyListCid;
//			case SID_GROUP:
//				return groupCid;
//			default:
//				return null;
//			}
//		}
//
//		// use shell command to generate below result
//		// grep "CID_MSG"
//		// /Users/ericxu1983/Documents/workspace/TT/mgandroid-teamtalk/src/com/mogujie/tt/config/ProtocolConstant.java
//		// | awk '{printf "msgCid[%s]=\"%s\";\n",$5,$5}'
//		static {
//			sid[SID_LOGIN] = "SID_LOGIN";
//			sid[SID_BUDDY_LIST] = "SID_BUDDY_LIST";
//			sid[SID_MSG] = "SID_MSG";
//
//			// todo eric can we switch server?
//			sid[SID_SWITCH_SERVER] = "SID_SWITCH_SERVER";
//			sid[SID_GROUP] = "SID_GROUP";
//			sid[SID_FILE] = "SID_FILE";
//			sid[SID_OTHER] = "SID_OTHER";
//
//			loginCid[CID_LOGIN_REQ_MSGSERVER] = "CID_LOGIN_REQ_MSGSERVER";
//			loginCid[CID_LOGIN_RES_MSGSERVER] = "CID_LOGIN_RES_MSGSERVER";
//			loginCid[CID_LOGIN_REQ_USERLOGIN] = "CID_LOGIN_REQ_USERLOGIN";
//			loginCid[CID_LOGIN_RES_USERLOGIN] = "CID_LOGIN_RES_USERLOGIN";
//			loginCid[CID_LOGIN_REQ_LOGINOUT] = "CID_LOGIN_REQ_LOGINOUT";
//			loginCid[CID_LOGIN_RES_LOGINOUT] = "CID_LOGIN_RES_LOGINOUT";
//			loginCid[CID_LOGIN_KICK_USER] = "CID_LOGIN_KICK_USER";
//
//			buddyListCid[CID_BUDDY_LIST_REQUEST] = "CID_BUDDY_LIST_REQUEST";
//			buddyListCid[CID_BUDDY_LIST_FRIEND_LIST] = "CID_BUDDY_LIST_FRIEND_LIST";
//			buddyListCid[CID_BUDDY_LIST_ONLINE_FRIEND_LIST] = "CID_BUDDY_LIST_ONLINE_FRIEND_LIST";
//			buddyListCid[CID_BUDDY_LIST_STATUS_NOTIFY] = "CID_BUDDY_LIST_STATUS_NOTIFY";
//			buddyListCid[CID_BUDDY_LIST_USER_STATUS_REQUEST] = "CID_BUDDY_LIST_USER_STATUS_REQUEST";
//			buddyListCid[CID_BUDDY_LIST_USER_STATUS_RESPONSE] = "CID_BUDDY_LIST_USER_STATUS_RESPONSE";
//			buddyListCid[CID_BUDDY_LIST_USER_INFO_RESPONSE] = "CID_BUDDY_LIST_USER_INFO_RESPONSE";
//			buddyListCid[CID_BUDDY_LIST_USER_INFO_REQUEST] = "CID_BUDDY_LIST_USER_INFO_REQUEST";
//			buddyListCid[CID_BUDDY_LIST_REMOVE_SESSION_REQ] = "CID_BUDDY_LIST_REMOVE_SESSION_REQ";
//			buddyListCid[CID_BUDDY_LIST_REMOVE_SESSION_RES] = "CID_BUDDY_LIST_REMOVE_SESSION_RES";
//			buddyListCid[CID_BUDDY_LIST_ALL_USER_REQUEST] = "CID_BUDDY_LIST_ALL_USER_REQUEST";
//			buddyListCid[CID_BUDDY_LIST_ALL_USER_RESPONSE] = "CID_BUDDY_LIST_ALL_USER_RESPONSE";
//			buddyListCid[CID_BUDDY_LIST_USERS_STATUS_REQUEST] = "CID_BUDDY_LIST_USERS_STATUS_REQUEST";
//			buddyListCid[CID_BUDDY_LIST_USERS_STATUS_RESPONSE] = "CID_BUDDY_LIST_USERS_STATUS_RESPONSE";
//			buddyListCid[CID_BUDDY_LIST_DEPARTMENT_REQUEST] = "CID_BUDDY_LIST_DEPARTMENT_REQUEST";
//			buddyListCid[CID_BUDDY_LIST_DEPARTMENT_RESPONSE] = "CID_BUDDY_LIST_DEPARTMENT_RESPONSE";
//
//			msgCid[CID_MSG_DATA] = "CID_MSG_DATA";
//			msgCid[CID_MSG_DATA_ACK] = "CID_MSG_DATA_ACK";
//			msgCid[CID_MSG_READ_ACK] = "CID_MSG_READ_ACK";
//			msgCid[CID_MSG_TIME_REQUEST] = "CID_MSG_TIME_REQUEST";
//			msgCid[CID_MSG_TIME_RESPONSE] = "CID_MSG_TIME_RESPONSE";
//			msgCid[CID_MSG_UNREAD_CNT_REQUEST] = "CID_MSG_UNREAD_CNT_REQUEST";
//			msgCid[CID_MSG_UNREAD_CNT_RESPONSE] = "CID_MSG_UNREAD_CNT_RESPONSE";
//			msgCid[CID_MSG_UNREAD_MSG_REUQEST] = "CID_MSG_UNREAD_MSG_REUQEST";
//			msgCid[CID_MSG_HISTORY_MSG_REQUEST] = "CID_MSG_HISTORY_MSG_REQUEST";
//			msgCid[CID_MSG_HISTORY_SERVICE_MSG_REQUEST] = "CID_MSG_HISTORY_SERVICE_MSG_REQUEST";
//			msgCid[CID_MSG_HISTORY_SERVICE_MSG_RESPONSE] = "CID_MSG_HISTORY_SERVICE_MSG_RESPONSE";
//			msgCid[CID_MSG_UNREAD_MSG_RESPONSE] = "CID_MSG_UNREAD_MSG_RESPONSE";
//			msgCid[CID_MSG_HISTORY_MSG_RESPONSE] = "CID_MSG_HISTORY_MSG_RESPONSE";
//
//			groupCid[CID_GROUP_LIST_REQUEST] = "CID_GROUP_LIST_REQUEST";
//			groupCid[CID_GROUP_LIST_RESPONSE] = "CID_GROUP_LIST_RESPONSE";
//			groupCid[CID_GROUP_USER_LIST_REQUEST] = "CID_GROUP_USER_LIST_REQUEST";
//			groupCid[CID_GROUP_USER_LIST_RESPONSE] = "CID_GROUP_USER_LIST_RESPONSE";
//			groupCid[CID_GROUP_UNREAD_CNT_REQUEST] = "CID_GROUP_UNREAD_CNT_REQUEST";
//			groupCid[CID_GROUP_UNREAD_CNT_RESPONSE] = "CID_GROUP_UNREAD_CNT_RESPONSE";
//			groupCid[CID_GROUP_UNREAD_MSG_REQUEST] = "CID_GROUP_UNREAD_MSG_REQUEST";
//			groupCid[CID_GROUP_UNREAD_MSG_RESPONSE] = "CID_GROUP_UNREAD_MSG_RESPONSE";
//			groupCid[CID_GROUP_HISTORY_MSG_REQUEST] = "CID_GROUP_HISTORY_MSG_REQUEST";
//			groupCid[CID_GROUP_HISTORY_MSG_RESPONSE] = "CID_GROUP_HISTORY_MSG_RESPONSE";
//			groupCid[CID_GROUP_MSG_READ_ACK] = "CID_GROUP_MSG_READ_ACK";
//			groupCid[CID_GROUP_CREATE_TMP_GROUP_REQUEST] = "CID_GROUP_CREATE_TMP_GROUP_REQUEST";
//			groupCid[CID_GROUP_CREATE_TMP_GROUP_RESPONSE] = "CID_GROUP_CREATE_TMP_GROUP_RESPONSE";
//			groupCid[CID_GROUP_CHANGE_MEMBER_REQUEST] = "CID_GROUP_CHANGE_MEMBER_REQUEST";
//			groupCid[CID_GROUP_CHANGE_MEMBER_RESPONSE] = "CID_GROUP_CHANGE_MEMBER_RESPONSE";
//			groupCid[CID_GROUP_DIALOG_LIST_REQUEST] = "CID_GROUP_DIALOG_LIST_REQUEST";
//			groupCid[CID_GROUP_DIALOG_LIST_RESPONSE] = "CID_GROUP_DIALOG_LIST_RESPONSE";
//			groupCid[CID_GROUP_CREATE_NORMAL_GROUP_NOTIFY] = "CID_GROUP_CREATE_NORMAL_GROUP_NOTIFY";
//			groupCid[CID_GROUP_CHANGE_MEMEBER_NOTIFY] = "CID_GROUP_CHANGE_MEMEBER_NOTIFY";
//		}

		public static void dump(boolean toSend, Header header) {
//			int serviceId = header.getServiceId();
//			int commandId = header.getCommandId();
//			int seqNo = header.getReserved();
//
//			String serviceString = "";
//			String commandString = "";
//			do {
//				// todo eric, exceed array index potential error
//				String ret = sid[serviceId];
//				if (ret == null) {
//					logger.e("unkown serviceid:%d", serviceId);
//					break;
//				}
//
//				serviceString = ret;
//
//				String[] cidList = getCidList(serviceId);
//				if (cidList == null) {
//					logger.e("no such cidList for service:%s", serviceString);
//					break;
//				}
//
//				ret = cidList[commandId];
//				if (ret == null) {
//					logger.e("no such commandId:%d", commandId);
//					break;
//				}
//
//				commandString = ret;
//
//			} while (false);
//
//			logger.d(
//					"packet#%s packet, service:%s(%d), command:%s(%d), seqNo:%d",
//					toSend ? "sending" : "received", serviceString, serviceId,
//					commandString, commandId, seqNo);
		}

	}

	// msg type
	public static final byte MSG_TYPE_P2P_FLAG = 0x00;
	public static final byte MSG_TYPE_GROUP_FLAG = 0x10;

	public static final byte MSG_TYPE_P2P_TEXT = MSG_TYPE_P2P_FLAG + 0x01;
	public static final byte MSG_TYPE_P2P_AUDIO = MSG_TYPE_P2P_FLAG + 0x02;
	public static final byte MSG_TYPE_GROUP_TEXT = MSG_TYPE_GROUP_FLAG + 0x01;
	public static final byte MSG_TYPE_GROUP_AUDIO = MSG_TYPE_GROUP_FLAG + 0x02;
	
	public static final int MESSAGE_TYPE_IM = 0x01; // 普通用户+系统消息
	public static final int MESSAGE_TYPE_IM_GROUP = 0x11;
	public static final int MESSAGE_TYPE_IM_AUDIO = 0x02;   			
	public static final int MESSAGE_TYPE_IM_GROUP_AUDIO = 0x12;

	public static final byte MSG_TYPE_GROUP_TEXT_FOR_HISTORY_REASON_COMPATIBILITY = 3;
	
	// command id for group message
	public static final int CID_GROUP_LIST_REQUEST = 1;
	public static final int CID_GROUP_LIST_RESPONSE = 2;
	public static final int CID_GROUP_USER_LIST_REQUEST = 3;
	public static final int CID_GROUP_USER_LIST_RESPONSE = 4;
	public static final int CID_GROUP_UNREAD_CNT_REQUEST = 5;
	public static final int CID_GROUP_UNREAD_CNT_RESPONSE = 6;
	public static final int CID_GROUP_UNREAD_MSG_REQUEST = 7;
	public static final int CID_GROUP_UNREAD_MSG_RESPONSE = 8;
	public static final int CID_GROUP_HISTORY_MSG_REQUEST = 9;
	public static final int CID_GROUP_HISTORY_MSG_RESPONSE = 10;
	public static final int CID_GROUP_MSG_READ_ACK = 11;
	public static final int CID_GROUP_CREATE_TMP_GROUP_REQUEST = 12;
	public static final int CID_GROUP_CREATE_TMP_GROUP_RESPONSE = 13;
	public static final int CID_GROUP_CHANGE_MEMBER_REQUEST = 14;
	public static final int CID_GROUP_CHANGE_MEMBER_RESPONSE = 15;
	public static final int CID_GROUP_DIALOG_LIST_REQUEST = 16;
	public static final int CID_GROUP_DIALOG_LIST_RESPONSE = 17;
	public static final int CID_GROUP_CREATE_NORMAL_GROUP_NOTIFY = 18;
	public static final int CID_GROUP_CHANGE_MEMEBER_NOTIFY = 19;
	public static final int CID_GROUP_MAX = 21;

	// command id for buddy list
	public static final int CID_BUDDY_LIST_REQUEST = 1; //
	public static final int CID_BUDDY_LIST_FRIEND_LIST = 3; //
	public static final int CID_BUDDY_LIST_ONLINE_FRIEND_LIST = 4; //
	public static final int CID_BUDDY_LIST_STATUS_NOTIFY = 5; //
	public static final int CID_BUDDY_LIST_USER_STATUS_REQUEST = 8; //
	public static final int CID_BUDDY_LIST_USER_STATUS_RESPONSE = 9; //
	public static final int CID_BUDDY_LIST_USER_INFO_RESPONSE = 10; //
	public static final int CID_BUDDY_LIST_USER_INFO_REQUEST = 11;
	public static final int CID_BUDDY_LIST_REMOVE_SESSION_REQ = 12;
	public static final int CID_BUDDY_LIST_REMOVE_SESSION_RES = 13;
	public static final int CID_BUDDY_LIST_ALL_USER_REQUEST = 14;
	public static final int CID_BUDDY_LIST_ALL_USER_RESPONSE = 15;
	public static final int CID_BUDDY_LIST_USERS_STATUS_REQUEST = 16;
	public static final int CID_BUDDY_LIST_USERS_STATUS_RESPONSE = 17;
	public static final int CID_BUDDY_LIST_DEPARTMENT_REQUEST = 18;
	public static final int CID_BUDDY_LIST_DEPARTMENT_RESPONSE = 19;
	public static final int CID_BUDDY_LIST_MAX = 21;

	public static final int USER_MSG_TYPE = 1;

	public static final int IM_PDU_VERSION = 3;

	// SERVICE_ID
	public static final int SID_LOGIN = 1;
	public static final int SID_BUDDY_LIST = 2;
	public static final int SID_MSG = 3;
	public static final int SID_SWITCH_SERVER = 4;
	public static final int SID_GROUP = 5;
	public static final int SID_FILE = 6;
	public static final int SID_OTHER = 7;
	public static final int SID_DEFAULT = 7;

	// todo eric, make the value enum, so if adding new sid, no need to change
	// the value for max
	public static final int SID_MAX = 0x0010;

	// LOGIN IP,PORT
	public static final String LOGIN_IP1 = "122.225.68.125";
	public static final String LOGIN_IP2 = "101.68.218.125";	
	//open source
	public static final int LOGIN_PORT = 18008;

	// COMMAND_ID FOR LOGIN
	public static final int CID_LOGIN_REQ_MSGSERVER = 1;
	public static final int CID_LOGIN_RES_MSGSERVER = 2;
	public static final int CID_LOGIN_REQ_USERLOGIN = 3;
	public static final int CID_LOGIN_RES_USERLOGIN = 4;
	public static final int CID_LOGIN_REQ_LOGINOUT = 5;
	public static final int CID_LOGIN_RES_LOGINOUT = 6;
	public static final int CID_LOGIN_KICK_USER = 7;
	public static final int CID_LOGIN_MAX = 10;

	// CONTACT
	public static final int CID_REQUEST_RECNET_CONTACT = 1;

	public static final int CID_GET_USER_INFO_REQUEST = 11;// 请求用户信息

	public static final int CID_GET_USER_INFO_RESPONSE = 10;// 获取用户信息
	
	public static  final int CID_BUDDY_LIST_EMPLOYEE_REQUEST	= 18;
	
	public static final int CID_BUDDY_LIST_EMPLOYEE_RESPONSE	= 19;
	
	

	public static final int ON_LINE = 1;
	// COMMAND_ID FOR MSG
	public static final int CID_MSG_DATA = 1;
	public static final int CID_MSG_DATA_ACK = 2;
	public static final int CID_MSG_READ_ACK = 3;
	public static final int CID_MSG_TIME_REQUEST = 5;
	public static final int CID_MSG_TIME_RESPONSE = 6;
	public static final int CID_MSG_UNREAD_CNT_REQUEST = 7;
	public static final int CID_MSG_UNREAD_CNT_RESPONSE = 8;
	public static final int CID_MSG_UNREAD_MSG_REUQEST = 9;
	public static final int CID_MSG_HISTORY_MSG_REQUEST = 10;
	// public static final int CID_MSG_LIST_RESPONSE = 11;
	public static final int CID_MSG_HISTORY_SERVICE_MSG_REQUEST = 12;
	public static final int CID_MSG_HISTORY_SERVICE_MSG_RESPONSE = 13;

	public static final int CID_MSG_UNREAD_MSG_RESPONSE = 14;
	public static final int CID_MSG_HISTORY_MSG_RESPONSE = 15;
	public static final int CID_MSG_MAX = 17;

	public static final int CID_SHOP_MEMBER_RESPONSE = 2;
	public static final int CID_CONTACT_RECENT_RESPONSE = 3;
	public static final int CID_CONTACT_FRIEND_STATUS_NOTIYF = 5;
	public static final int CID_QUERY_USER_ONLINE_STATUS_REQUEST = 8;
	public static final int CID_QUERY_USER_ONLINE_STATUS_RESPONSE = 9;

	public static final int CID_HEART_BEAT = 1;

	public static final int RES_RESULT_SUCCESS = 0;

	public static final int CLIENT_TYPE = 0x12;// 表示android，登陆消息服务器时使用

	public static final String CLIENT_VERSION = "ANDROID_TEAMTALK_V1.0.1";

}
