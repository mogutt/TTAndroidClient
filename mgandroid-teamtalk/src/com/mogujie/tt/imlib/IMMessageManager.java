package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.db.IMDbManager;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.AckGroupUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.AckUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.GroupUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.imlib.proto.MessageNotifyPacket;
import com.mogujie.tt.imlib.proto.MessagePacket;
import com.mogujie.tt.imlib.proto.UnreadMsgPacket;
import com.mogujie.tt.imlib.proto.UnreadMsgPacket.PacketResponse;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Packet.Response;
import com.mogujie.tt.utils.MessageSplitResult;

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
	private IMUnAckMsgManager unackMsgMgr = new IMUnAckMsgManager();
	private IMUnreadMsgManager unreadMsgMgr = new IMUnreadMsgManager();

	private IMMessageManager() {

	}

	private byte getTextMsgType(int sessionType) {
		byte msgType = ProtocolConstant.MSG_TYPE_P2P_TEXT;

		if (sessionType == IMSession.SESSION_GROUP
				|| sessionType == IMSession.SESSION_TEMP_GROUP) {
			msgType = ProtocolConstant.MSG_TYPE_GROUP_TEXT;
		}

		return msgType;
	}

	private byte getAudioMsgType(int sessionType) {
		byte msgType = ProtocolConstant.MSG_TYPE_P2P_AUDIO;

		if (sessionType == IMSession.SESSION_GROUP
				|| sessionType == IMSession.SESSION_TEMP_GROUP) {
			msgType = ProtocolConstant.MSG_TYPE_GROUP_AUDIO;
		}

		return msgType;
	}

	// todo eric remove msgInfo
	public void sendText(String peerId, String text, int sessionType,
			MessageInfo msgInfo) {
		logger.i("chat#sendText -> peerId:%s, text:%s", peerId, text);

		MessageEntity msg = createMessageEntity(getTextMsgType(sessionType));
		msg.toId = peerId;

		// todo eric utf8?
		msg.msgData = text.getBytes();
		msg.msgLen = msg.msgData.length;
		logger.d("chat#msgLen:%d", msg.msgLen);

		sendMessage(msg, msgInfo);
	}

	public void sendVoice(String peerId, byte[] voiceData, int sesionType,
			MessageInfo msgInfo) {
		logger.i("chat#audio#sendVoice -> peerId:%s, voidDataLen:%d", peerId,
				voiceData.length);

		MessageEntity msg = createMessageEntity(getAudioMsgType(sesionType));
		msg.toId = peerId;

		// todo eric utf8?
		msg.msgData = voiceData;
		msg.msgLen = voiceData.length;

		sendMessage(msg, msgInfo);
	}

	private MessageEntity createMessageEntity(byte msgType) {
		MessageEntity msg = new MessageEntity();
		msg.seqNo = seqNo++;

		// todo eric manage the current user myself
		msg.fromId = IMLoginManager.instance().getLoginId();

		logger.d("chat#msg.fromid" + msg.fromId);

		// todo eric, use the server time
		msg.createTime = (int) (System.currentTimeMillis() / 1000);

		msg.msgType = msgType;

		// todo eric, it looks no one use attach param now
		msg.attach = "";
		return msg;
	}

	private void generateMsgSessionInfo(MessageEntity msg, boolean sending) {
		logger.d("chat#generateMsgSessionInfo msg:%s,  sending:%s", msg,
				sending);

		msg.generateMsgId();
		msg.generateSessionId(sending);
		msg.generateSessionType();

		if (!sending) {
			msg.msgInfo = MessageEntity2MessageInfo(msg);
		}
	}

	private void sendMessage(MessageEntity msg, MessageInfo msgInfo) {
		logger.i("chat#sendMessage, msg:%s", msg);

		generateMsgSessionInfo(msg, true);

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new MessagePacket(msg));

		unackMsgMgr.add(msg, msgInfo);

		logger.i("chat#send packet to server");
		
		IMRecentSessionManager.instance().insertRecentSession(msg);
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
		MessagePacket.PacketResponse resp = (MessagePacket.PacketResponse) packet
				.getResponse();

		logger.d("chat#get msg ack:%s", resp.msgAck);

		MessageEntity msg = unackMsgMgr.remove(resp.msgAck.seqNo);

		IMDbManager.instance(ctx).saveMsg(msg, true);

		if (broadcastMessage(msg, IMActions.ACTION_MSG_ACK, false)) {
			logger.d("chat#broadcast receiving ack msg");
		}
	}

	private MessageInfo MessageEntity2MessageInfo(MessageEntity msg) {
		MessageInfo msgInfo = new MessageInfo();
		msgInfo.setMsgId(msg.seqNo); // todo eric this is right?why i don't use
										// message id
		msgInfo.setMsgFromUserId(msg.fromId);
		msgInfo.setTargetId(msg.toId);
		msgInfo.setMsgCreateTime(msg.createTime);
		msgInfo.setMsgType(msg.msgType);
		// todo eric
		// msgInfo.setMsgRenderType(msgRenderType);
		msgInfo.setMsgAttachContent(null);

		if (msg.msgType == ProtocolConstant.MSG_TYPE_P2P_AUDIO) {
			logger.d("chat#recv audio msg");
			msgInfo.setAudioContent(msg.msgData);
		} else if (msg.msgType == ProtocolConstant.MSG_TYPE_GROUP_TEXT) {
			logger.d("chat#recv text msg");
		}

		return msgInfo;

	}

	public void onRecvMessage(DataBuffer buffer) {
		logger.i("chat#onRecvMessage");

		MessageNotifyPacket packet = new MessageNotifyPacket();
		packet.decode(buffer);

		logger.d("decode recv message ok");

		// todo eric unify the getResponse to get notify, but low priority, 'cuz
		// we're gonna make the tool to automatically
		// generate the packet
		MessageNotifyPacket.packetNotify notify = (MessageNotifyPacket.packetNotify) packet
				.getResponse();

		generateMsgSessionInfo(notify.msg, false);

		logger.d("chat#msg:%s", notify.msg);

		ackMsg(notify.msg);

		List<MessageEntity> splitMessageList = IMContactHelper
				.splitMessage(notify.msg);

		for (MessageEntity msg : splitMessageList) {

			unreadMsgMgr.add(msg);

			if (broadcastMessage(msg, IMActions.ACTION_MSG_RECV, true)) {
				logger.d("chat#broadcast receiving new msg");
			}
		}
	}

	private void ackMsg(MessageEntity msg) {
		logger.d("chat#sendMessageAck -> fromId:%s, seqNo:%d", msg.fromId,
				seqNo);

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new MessageNotifyPacket(msg));

		logger.i("chat#send packet to server");

	}

	// todo eric group id could be == contact id, so session id should be with
	// unique prefix
	public MessageEntity getUnreadMsg(String sessionId, String msgId) {
		logger.d("chat#getUnreadMsg -> sessionId:%s, msgId:%s", sessionId,
				msgId);
		return unreadMsgMgr.getUnreadMsg(sessionId, msgId);
	}

	public MessageEntity popUnreadMsg(String sessionId, String msgId) {
		logger.d("chat#getUnreadMsg -> sessionId:%s, msgId:%s", sessionId,
				msgId);
		return unreadMsgMgr.popUnreadMsg(sessionId, msgId);
	}

	public void onRepUnreadMsg(DataBuffer buffer) {
		logger.i("unread#onRepUnreadMsg");

		UnreadMsgPacket packet = new UnreadMsgPacket();
		packet.decode(buffer);

		UnreadMsgPacket.PacketResponse response = (PacketResponse) packet
				.getResponse();

		handleUnreadMsgs(response.entityList);
	}

	public void onRepGroupUnreadMsg(DataBuffer buffer) {
		logger.i("unread#onRepGroupUnreadMsg");

		GroupUnreadMsgPacket packet = new GroupUnreadMsgPacket();
		packet.decode(buffer);

		GroupUnreadMsgPacket.PacketResponse response = (GroupUnreadMsgPacket.PacketResponse) packet
				.getResponse();

		handleUnreadMsgs(response.entityList);
	}

	public void handleUnreadMsgs(List<MessageEntity> msgList) {
		for (MessageEntity msg : msgList) {
			generateMsgSessionInfo(msg, false);
			
			List<MessageEntity> splitMsgList = IMContactHelper.splitMessage(msg);
			handleUnreadMsgsImpl(splitMsgList);
		}
	}
	
	private void handleUnreadMsgsImpl(List<MessageEntity> msgList) {
		for (MessageEntity msg : msgList) {
			unreadMsgMgr.add(msg);
		}

		IMRecentSessionManager.instance().batchUpdate(msgList);

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

	public void saveUnreadMsgs(String sessionId) {
		if (sessionId == null) {
			return;
		}

		logger.d("chat#saveUnreadMsgs contactId:%s", sessionId);
		ConcurrentHashMap<String, MessageEntity> unreadMsgs = unreadMsgMgr
				.getUnreadMsgs(sessionId);
		if (unreadMsgs == null) {
			logger.d("chat#no unreadmsgs for sessionId:%s", sessionId);
			return;
		}

		ArrayList<MessageEntity> msgs = new ArrayList<MessageEntity>(
				unreadMsgs.values());
		Collections.sort(msgs, new Comparator<MessageEntity>() {

			@Override
			public int compare(MessageEntity lhs, MessageEntity rhs) {
				// TODO Auto-generated method stub
				if (lhs.createTime > rhs.createTime) {
					return 1;
				} else if (lhs.createTime == rhs.createTime) {
					return 0;
				} else
					return -1;
			}

		});

		for (MessageEntity msg : msgs) {
			IMDbManager.instance(ctx).saveMsg(msg, false);
		}

		unreadMsgMgr.removeUnreadMsgs(sessionId);
		IMRecentSessionManager.instance().resetUnreadMsgCnt(sessionId);

		IMRecentSessionManager.instance().broadcast();
	}

}
