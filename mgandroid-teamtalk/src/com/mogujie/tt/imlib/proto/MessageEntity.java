package com.mogujie.tt.imlib.proto;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.log.Logger;

public class MessageEntity {
	public int seqNo;
	public String fromId;
	public String toId;
	public int createTime;
	public byte type;
	public int msgLen;
	public byte[] msgData;
	public String attach;

	// non-meta members
	protected Logger logger = Logger.getLogger(MessageEntity.class);
	public String msgId;
	public String sessionId;
	public int sessionType = -1;

	public boolean isMy() {
		return fromId.equals(IMLoginManager.instance().getLoginId());
	}

	public void copy(MessageEntity anotherEntity) {
		seqNo = anotherEntity.seqNo;
		fromId = anotherEntity.fromId;
		toId = anotherEntity.toId;
		createTime = anotherEntity.createTime;
		type = anotherEntity.type;
		msgLen = anotherEntity.msgLen;
		msgData = anotherEntity.msgData;
		attach = anotherEntity.attach;
		generateMsgId();
		sessionId = anotherEntity.sessionId;
		sessionType = anotherEntity.sessionType;
	}

	private String getMsgDataDescription() {
		if (type == ProtocolConstant.MSG_TYPE_P2P_TEXT) {
			return new String(msgData);
		} else {
			return "";
		}
	}

	public boolean isTextType() {
		// return (msgType == ProtocolConstant.MSG_TYPE_GROUP_TEXT || msgType ==
		// ProtocolConstant.MSG_TYPE_P2P_TEXT);
		return msgInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_TEXT;
	}

	public boolean isAudioType() {
		// return (msgType == ProtocolConstant.MSG_TYPE_GROUP_AUDIO || msgType
		// == ProtocolConstant.MSG_TYPE_P2P_AUDIO);
		return msgInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO;
	}

	public boolean isPictureType() {
		// if (isTextType()) {
		// String msgContent = new String(msgData);
		// if (msgContent != null &&
		// msgContent.contains(SysConstant.MESSAGE_IMAGE_LINK_START)) {
		// return true;
		// }
		// }
		//
		// return false;
		return msgInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE;
	}

	public String getText() {
		if (isTextType()) {
			return new String(msgData);
		} else {
			return "";
		}
	}

	public void generateMsgId(/* boolean sending */) {
		// logger.d("chat#generateMessageId -> sending:%s", sending);
		//
		// // unique session, unique time, seqNo+fromId
		// msgId = String.format("%s_%d_%d_%s", getSessionId(sending),
		// createTime,
		// seqNo, fromId);
		msgId = UUID.randomUUID().toString();
	}

	public void generateMsgIdIfEmpty(/* boolean sending */) {
		if (msgId == null || msgId.isEmpty()) {
			msgId = UUID.randomUUID().toString();
		}
	}

	public void generateSessionId(boolean sending) {
		logger.d("chat#generateSessionId sending:%s", sending);

		sessionId = getSessionId(sending);

		logger.d("chat#session id:%s", sessionId);
	}

	public void generateSessionType(int sessionType) {
		this.sessionType = sessionType;
	}

	public boolean isGroupMsg() {
		// todo eric consider flag &
		if (type == ProtocolConstant.MSG_TYPE_GROUP_AUDIO
				|| type == ProtocolConstant.MSG_TYPE_GROUP_TEXT) {
			return true;
		}

		return false;
	}

	public boolean isP2PMsg() {
		if (type == ProtocolConstant.MSG_TYPE_P2P_AUDIO
				|| type == ProtocolConstant.MSG_TYPE_P2P_TEXT) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		// todo eric make createtime readble
		// todo eric if the content is text, should i logging here
		// todo eric fix all warnings, like locale param in String.format
		return String.format("seqNo:%d,  fromId:%s, toId:%s, createTime:%d, msgType:%d, msgLen:%d, msgData:%s, attach:%s, msgId:%s", seqNo, fromId, toId, createTime, type, msgLen, getMsgDataDescription(), (attach == null)
				? ""
				: attach, (msgId == null) ? "" : msgId);
	}

	public String getSessionId(boolean sending) {
		if (type == ProtocolConstant.MSG_TYPE_P2P_TEXT
				|| type == ProtocolConstant.MSG_TYPE_P2P_AUDIO) {
			return sending ? toId : fromId;
		}

		if (type == ProtocolConstant.MSG_TYPE_GROUP_TEXT
				|| type == ProtocolConstant.MSG_TYPE_GROUP_AUDIO) {
			return toId;
		}

		logger.e("chat#getSessionId failed");

		return null;
	}

	public static String createAudioInfo(MessageInfo msgInfo) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("path", msgInfo.getSavePath());
			jo.put("length", msgInfo.getPlayTime());
			return jo.toString();
		} catch (JSONException e) {
			Logger logger = Logger.getLogger(MessageEntity.class);
			logger.e("audio#createAudioInfo failed");
		}

		return "";
	}

	public static String createPicInfo(MessageInfo msgInfo) {
		Logger logger = Logger.getLogger(MessageEntity.class);
		logger.d("pic#createPicInfo getSavePath:%s", msgInfo.getSavePath());
		JSONObject jo = new JSONObject();
		try {
			jo.put("path", msgInfo.getSavePath());
			return jo.toString();
		} catch (JSONException e) {

			logger.e("pic#createPicInfo failed");
		}

		return "";
	}

	public static class AudioInfo {
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		private String path;
		private int length;

		public AudioInfo(String path, int length) {
			this.path = path;
			this.length = length;

			Logger.getLogger(MessageEntity.class).d("audio#path:%s, length:%d", path, length);
		}

		public static AudioInfo create(String info) {
			JSONObject jo;
			try {
				jo = new JSONObject(info);
				return new AudioInfo((String) jo.get("path"), (Integer) jo.get("length"));

			} catch (JSONException e1) {
				Logger logger = Logger.getLogger(MessageEntity.class);
				// TODO Auto-generated catch block
				logger.d("audio#createAudioInfo failed");
			}

			return null;

		}
	}

	public static class PicInfo {
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		private String path;

		public PicInfo(String path) {
			this.path = path;

			Logger.getLogger(MessageEntity.class).d("pic#set path:%s", path);
		}

		public static PicInfo create(String info) {
			JSONObject jo;
			try {
				jo = new JSONObject(info);
				return new PicInfo((String) jo.get("path"));
			} catch (JSONException e1) {
				Logger logger = Logger.getLogger(MessageEntity.class);
				// TODO Auto-generated catch block
				logger.d("pic#createPicInfo failed");
			}

			return null;

		}
	}

	public String getContent() {
		if (isTextType()) {
			return new String(msgData);
		} else if (isAudioType()) {
			return createAudioInfo(msgInfo);
		} else if (isPictureType()) {
			return createPicInfo(msgInfo);
		}

		return "";
	}

	// todo eric
	public MessageInfo msgInfo;

}
