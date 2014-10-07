package com.mogujie.tt.imlib;

import java.nio.charset.Charset;
import java.util.List;

import android.content.Intent;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.db.IMDbManager;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.AckGroupUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.AckUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.proto.GroupUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.imlib.proto.MessageNotifyPacket;
import com.mogujie.tt.imlib.proto.MessagePacket;
import com.mogujie.tt.imlib.proto.UnreadMsgPacket;
import com.mogujie.tt.imlib.proto.UnreadMsgPacket.PacketResponse;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;

public class IMMessageManager extends IMManager {
	private static IMMessageManager inst;
	private Logger logger = Logger.getLogger(IMMessageManager.class);

	public static IMMessageManager instance() {
		synchronized (IMMessageManager.class) {
			if (inst == null) {
				inst = new IMMessageManager();
			}

			return inst;
		}
	}

	private int seqNo = 1;

	private IMMessageManager() {

	}

	public void sendText(String peerId, String text, int sessionType,
			MessageInfo msgInfo) {
		logger.i("chat#text#sendText -> peerId:%s, text:%s", peerId, text);

		fillMessageCommonInfo(msgInfo, peerId, getTextMsgType(sessionType), sessionType);

		msgInfo.msgData = text.getBytes(Charset.forName("utf8"));
		msgInfo.msgLen = msgInfo.msgData.length;

		sendMessage(msgInfo);
	}

	public void sendVoice(String peerId, byte[] voiceData, int sessionType,
			MessageInfo msgInfo) {
		logger.i("chat#audio#sendVoice -> peerId:%s, voidDataLen:%d", peerId, voiceData.length);

		fillMessageCommonInfo(msgInfo, peerId, getAudioMsgType(sessionType), sessionType);

		// todo eric utf8?
		msgInfo.msgData = voiceData;
		msgInfo.msgLen = voiceData.length;

		sendMessage(msgInfo);
	}

	private void fillMessageCommonInfo(MessageInfo msg, String peerId,
			byte msgType, int sessionType) {
		msg.seqNo = seqNo++;
		msg.fromId = IMLoginManager.instance().getLoginId();
		msg.toId = peerId;

		msg.generateMsgIdIfEmpty();
		msg.generateSessionId(true);
		msg.generateSessionType(sessionType);

		logger.d("chat#msg.fromid" + msg.fromId);

		// todo eric, use the server time
		msg.createTime = (int) (System.currentTimeMillis() / 1000);

		msg.type = msgType;

		// it looks no one use attach param now
		msg.attach = "";
	}

	private static byte getTextMsgType(int sessionType) {
		byte msgType = ProtocolConstant.MSG_TYPE_P2P_TEXT;

		if (sessionType == IMSession.SESSION_GROUP
				|| sessionType == IMSession.SESSION_TEMP_GROUP) {
			msgType = ProtocolConstant.MSG_TYPE_GROUP_TEXT;
		}

		return msgType;
	}

	private static byte getAudioMsgType(int sessionType) {
		byte msgType = ProtocolConstant.MSG_TYPE_P2P_AUDIO;

		if (sessionType == IMSession.SESSION_GROUP
				|| sessionType == IMSession.SESSION_TEMP_GROUP) {
			msgType = ProtocolConstant.MSG_TYPE_GROUP_AUDIO;
		}

		return msgType;
	}

	private void sendMessage(MessageInfo msgInfo) {
		logger.i("chat#sendMessage, msg:%s", msgInfo);

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		// todo eric, this should be put in ui layer
		msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);

		IMUnAckMsgManager.instance().add(msgInfo);
		
		IMRecentSessionManager.instance().update(msgInfo);
		IMRecentSessionManager.instance().broadcast();
		
		channel.sendPacket(new MessagePacket(msgInfo));
	}

	private boolean broadcastMessage(MessageEntity msg, String action,
			boolean ordered) {
		// todo eric use local broadcast
		if (ctx != null) {
			Intent intent = new Intent(action);
			intent.putExtra(SysConstant.SESSION_ID_KEY, msg.sessionId);
			intent.putExtra(SysConstant.MSG_ID_KEY, msg.msgId);

			if (ordered) {
				ctx.sendOrderedBroadcast(intent, null);
			} else {
				ctx.sendBroadcast(intent);
			}

			return true;
		}

		return false;
	}

	public void onMessageAck(DataBuffer buffer) {
		logger.i("chat#onMessageAck");

		MessagePacket packet = new MessagePacket();
		packet.decode(buffer);

		// todo eric adjust eclipse formatting detail, let .getResponse on the
		// same line(more characters on 1 line?)
		MessagePacket.PacketResponse resp = (MessagePacket.PacketResponse) packet.getResponse();

		logger.d("chat#get msg ack:%s", resp.msgAck);

		MessageInfo msg = IMUnAckMsgManager.instance().remove(resp.msgAck.seqNo);
		msg.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);

		IMDbManager.instance(ctx).updateMessageStatus(msg);

		if (broadcastMessage(msg, IMActions.ACTION_MSG_ACK, false)) {
			logger.d("chat#broadcast receiving ack msg");
		}
	}

	private MessageInfo decodeMessageInfo(DataBuffer buffer) {
		MessageNotifyPacket packet = new MessageNotifyPacket();
		packet.decode(buffer);

		logger.d("decode recv message ok");

		// todo eric unify the getResponse to get notify, but low priority, 'cuz
		// we're gonna make the tool to automatically
		// generate the packet
		MessageNotifyPacket.packetNotify notify = (MessageNotifyPacket.packetNotify) packet.getResponse();

		MessageEntity msg = notify.msg;
		logger.d("chat#msg:%s", msg);

		return new MessageInfo(msg);

	}

	public void onRecvMessage(DataBuffer buffer) {
		logger.i("chat#onRecvMessage");

		MessageInfo msgInfo = decodeMessageInfo(buffer);
		if (msgInfo == null) {
			logger.e("chat#decodeMessageInfo failed");
			return;
		}

		ackMsg(msgInfo);
		handleUnreadMsg(msgInfo);
		IMRecentSessionManager.instance().broadcast();
	}

	private void setMsgSessionType(MessageInfo msgInfo) {
		ContactEntity contact = IMContactManager.instance().findContact(msgInfo.sessionId);
		if (contact != null) {
			msgInfo.sessionType = IMSession.SESSION_P2P;
			return;
		}
		
		GroupEntity group = IMGroupManager.instance().findGroup(msgInfo.sessionId);
		if (group != null) {
			msgInfo.sessionType = group.type;
			return;
		}
		
		logger.e("chat#unkown msg session type, could be temp group type");
		msgInfo.sessionType = IMSession.SESSION_TEMP_GROUP;
	}
	
	private void handleUnreadMsg(MessageInfo msgInfo) {
		logger.d("chat#handleUnreadMsg");
		List<MessageInfo> splitMessageList = IMContactHelper.splitMessage(msgInfo);

		for (MessageInfo msg : splitMessageList) {
			setMsgSessionType(msg);
			IMUnreadMsgManager.instance().add(msg);
			
			IMRecentSessionManager.instance().update(msg);

			if (broadcastMessage(msg, IMActions.ACTION_MSG_RECV, true)) {
				logger.d("chat#broadcast receiving new msg");
			}
		}
	}

	private void ackMsg(MessageEntity msg) {
		logger.d("chat#sendMessageAck -> fromId:%s, seqNo:%d", msg.fromId, seqNo);

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new MessageNotifyPacket(msg));

		logger.i("chat#send packet to server");

	}

	public void onRepUnreadMsg(DataBuffer buffer) {
		logger.i("unread#onRepUnreadMsg");

		UnreadMsgPacket packet = new UnreadMsgPacket();
		packet.decode(buffer);

		UnreadMsgPacket.PacketResponse response = (PacketResponse) packet.getResponse();

		handleUnreadMsgList(response.entityList);
	}

	public void onRepGroupUnreadMsg(DataBuffer buffer) {
		logger.i("unread#onRepGroupUnreadMsg");

		GroupUnreadMsgPacket packet = new GroupUnreadMsgPacket();
		packet.decode(buffer);

		GroupUnreadMsgPacket.PacketResponse response = (GroupUnreadMsgPacket.PacketResponse) packet.getResponse();

		handleUnreadMsgList(response.entityList);
	}

	private void handleUnreadMsgList(List<MessageEntity> msgEntitiyList) {
		for (MessageEntity msgEntity : msgEntitiyList) {
			MessageInfo msgInfo = new MessageInfo(msgEntity);
			handleUnreadMsg(msgInfo);
		}
		
		IMRecentSessionManager.instance().broadcast();
	}

	public List<MessageInfo> ReadUnreadMsgList(String sessionId, int sessionType) {
		logger.d("unread#ReadUnreadMsgList sessionid:%s, sessionType:%d", sessionId, sessionType);

		List<MessageInfo> msgList = IMUnreadMsgManager.instance().popUnreadMsgList(sessionId);
		if (msgList == null || msgList.isEmpty()) {
			logger.d("unread#no unread msgs");
			return msgList;
		}

		for (MessageInfo msgInfo : msgList) {
			msgInfo.generateSessionType(sessionType);
			IMDbManager.instance(ctx).saveMsg(msgInfo, false);
		}

		if (sessionType == IMSession.SESSION_P2P) {
			ackUnreadMsgs(sessionId);
		} else {
			ackGroupUnreadMsgs(sessionId);
		}

		return msgList;
	}

	public void ackUnreadMsgs(String contactId) {
		if (contactId == null) {
			return;
		}

		logger.d("chat#ackUnreadMsgs contactId:%s", contactId);

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new AckUnreadMsgPacket(contactId));

		logger.i("chat#send packet to server");

	}

	public void ackGroupUnreadMsgs(String groupId) {
		if (groupId == null) {
			return;
		}

		logger.d("chat#ackUnreadMsgs groupId:%s", groupId);

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new AckGroupUnreadMsgPacket(groupId));

		logger.i("chat#send packet to server");

	}
}
