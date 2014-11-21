package com.mogujie.tt.imlib;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mogujie.tt.config.HandlerConstant;
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
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.task.TaskManager;
import com.mogujie.tt.task.biz.UploadImageTask;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class IMMessageManager extends IMManager implements OnIMServiceListner {
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
	private List<MessageInfo> noSessionEntityMsgList = new ArrayList<MessageInfo>();
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private static Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HandlerConstant.HANDLER_IMAGE_UPLOAD_FAILD :
					IMMessageManager.instance().onUploadImageFaild(msg.obj);
					break;

				case HandlerConstant.HANDLER_IMAGE_UPLOAD_SUCESS :
					IMMessageManager.instance().onImageUploadFinish(msg.obj);
					break;

				default :
					break;
			}

			super.handleMessage(msg);
		}

	};

	public void onUploadImageFaild(Object obj) {
		logger.d("pic#onUploadImageFaild");
		if (obj == null)
			return;

		MessageInfo messageInfo = (MessageInfo) obj;

		logger.d("pic#msg:%s", messageInfo);
		IMUnAckMsgManager.instance().handleTimeoutUnAckMsg(messageInfo.msgId);

		broadcastMsgStatus(messageInfo, SysConstant.MESSAGE_STATE_FINISH_FAILED);

	}

	public void onImageUploadFinish(Object obj) {
		logger.d("pic#onImageUploadFinish");

		if (obj == null)
			return;

		MessageInfo messageInfo = (MessageInfo) obj;
		logger.d("pic#msg:%s", messageInfo);

		String imageUrl = messageInfo.getUrl();
		logger.d("pic#imageUrl:%s", imageUrl);
		String realImageURL = "";
		try {
			realImageURL = URLDecoder.decode(imageUrl, "utf-8");
			logger.d("pic#realImageUrl:%s", realImageURL);
		} catch (UnsupportedEncodingException e) {
			logger.e(e.toString());
		}

		MessageInfo msgInfo = IMUnAckMsgManager.instance().get(messageInfo.msgId);
		if (msgInfo == null) {
			logger.e("pic#no such msgInfo");
		}

		msgInfo.setUrl(realImageURL);

		msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);

		msgInfo.setMsgContent(SysConstant.MESSAGE_IMAGE_LINK_START
				+ realImageURL + SysConstant.MESSAGE_IMAGE_LINK_END);

		broadcastMsgStatus(msgInfo, SysConstant.MESSAGE_STATE_LOADDING);

		logger.d("pic#send pic message, msg:%s", msgInfo);
		sendText(msgInfo.getTargetId(), msgInfo.getMsgContent(), msgInfo.sessionType, msgInfo);
	}

	private void broadcastMsgStatus(MessageInfo msg, int status) {
		logger.d("chat#pic#broadcastMsgStatus msg:%s, status:%d", msg, status);

		Intent intent = new Intent(IMActions.ACTION_MSG_STATUS);
		IMUIHelper.setSessionInIntent(intent, msg.sessionId, msg.sessionType);
		intent.putExtra(SysConstant.STATUS_KEY, status);

		if (ctx != null) {
			ctx.sendBroadcast(intent);
			logger.d("chat#pic#broadcast ok");
		}
	}

	private IMMessageManager() {

	}

	public void register() {
		logger.d("chat#regisgter");

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_GROUP_READY);
		imServiceHelper.registerActions(ctx, actions, IMServiceHelper.INTENT_NO_PRIORITY, this);
	}

	public void sendText(String sessionId, String text, int sessionType,
			MessageInfo msgInfo) {
		logger.i("chat#text#sendText -> peerId:%s, text:%s", sessionId, text);

		fillMessageCommonInfo(msgInfo, sessionId, getTextMsgType(sessionType), sessionType);

		msgInfo.msgData = text.getBytes(Charset.forName("utf8"));
		msgInfo.msgLen = msgInfo.msgData.length;

		sendMessage(msgInfo);
	}

	public void sendVoice(String sessionId, byte[] voiceData, int sessionType,
			MessageInfo msgInfo) {
		logger.i("chat#audio#sendVoice -> sessionId:%s, voidDataLen:%d", sessionId, voiceData.length);

		fillMessageCommonInfo(msgInfo, sessionId, getAudioMsgType(sessionType), sessionType);

		// todo eric utf8?
		msgInfo.msgData = voiceData;
		msgInfo.msgLen = voiceData.length;

		sendMessage(msgInfo);
	}

	public void sendImages(String sessionId, int sessionType,
			List<MessageInfo> msgList) {

		for (MessageInfo msg : msgList) {
			logger.d("chat#pic#sendImage sessionId:%s, msg:%s", sessionId, msg);

			// image message would wrapped as a text message after uploading
			// image to server ok
			fillMessageCommonInfo(msg, sessionId, getTextMsgType(sessionType), sessionType);
		}

		// todo eric get rid of TaskManager
		UploadImageTask upTask = new UploadImageTask(mainHandler, sessionType, SysConstant.UPLOAD_IMAGE_URL_PREFIX, "", msgList);
		TaskManager.getInstance().trigger(upTask);
	}

	private void fillMessageCommonInfo(MessageInfo msg, String sessionId,
			byte msgType, int sessionType) {
		msg.seqNo = seqNo++;
		msg.talkerId = IMLoginManager.instance().getLoginId();
		msg.fromId = msg.talkerId;
		msg.toId = sessionId;

		msg.type = msgType;

		msg.generateMsgIdIfEmpty();
		msg.generateSessionId(true);
		msg.generateSessionType(sessionType);

		logger.d("chat#msg.fromid" + msg.fromId);

		// todo eric, use the server time
		msg.createTime = (int) (System.currentTimeMillis() / 1000);

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

	public void resendMessage(MessageInfo msgInfo) {
		logger.d("chat#resend#resendMessage msgInfo:%s", msgInfo);
		if (msgInfo == null) {
			return;
		}

		msgInfo.setResend(true);

		if (msgInfo.isTextType()) {
			logger.d("chat#resend#this is a text type message");
			sendText(msgInfo.toId, msgInfo.getMsgContent(), msgInfo.sessionType, msgInfo);
		} else if (msgInfo.isAudioType()) {
			logger.d("chat#resend#this is an audio type message");
			sendVoice(msgInfo.toId, msgInfo.getAudioContent(), msgInfo.sessionType, msgInfo);
		} else if (msgInfo.isImage()) {
			logger.d("chat#pic#resend#this is a picture type message");
			List<MessageInfo> msgList = new ArrayList<MessageInfo>();
			msgList.add(msgInfo);

			sendImages(msgInfo.sessionId, msgInfo.sessionType, msgList);
		}

		if (ctx != null) {
			Intent intent = new Intent(IMActions.ACTION_MSG_RESENT);
			IMUIHelper.setSessionInIntent(intent, msgInfo.sessionId, msgInfo.sessionType);
			ctx.sendBroadcast(intent);
		}
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

	private boolean broadcastMessage(String sessionId, int sessionType,
			String action, boolean ordered) {
		// todo eric use local broadcast
		if (ctx != null) {
			Intent intent = new Intent(action);
			IMUIHelper.setSessionInIntent(intent, sessionId, sessionType);

			if (ordered) {
				ctx.sendOrderedBroadcast(intent, null);
			} else {
				ctx.sendBroadcast(intent);
			}

			return true;
		}

		return false;
	}

	private boolean broadcastMessageAck(MessageEntity msg, String action) {
		// todo eric use local broadcast

		if (ctx != null) {
			Intent intent = new Intent(action);

			IMUIHelper.setSessionInIntent(intent, msg.sessionId, msg.sessionType);
			intent.putExtra(SysConstant.MSG_ID_KEY, msg.msgId);

			ctx.sendBroadcast(intent);

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

		if (broadcastMessageAck(msg, IMActions.ACTION_MSG_ACK)) {
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

	private Object findMsgSessionEntity(MessageInfo msg) {
		logger.d("chat#findMsgSessionEntity msg:%s", msg);
		ContactEntity contactEntity = IMContactManager.instance().findContact(msg.sessionId);
		if (contactEntity != null) {
			logger.d("chat#this is a contact msg");
			return contactEntity;
		}

		GroupEntity groupEntity = IMGroupManager.instance().findGroup(msg.sessionId);
		if (groupEntity != null) {
			logger.d("chat#this is a group msg");
			return groupEntity;
		}

		IMGroupManager.instance().reqGetTempGroupList();
		return null;
	}

	public void onRecvMessage(DataBuffer buffer) {
		logger.i("chat#onRecvMessage");

		MessageInfo msgInfo = decodeMessageInfo(buffer);
		if (msgInfo == null) {
			logger.e("chat#decodeMessageInfo failed");
			return;
		}

		logger.d("chat#recvMsg:%s", msgInfo);
		
		ackMsg(msgInfo);

		Object msgSessionEntity = findMsgSessionEntity(msgInfo);

		if (msgSessionEntity != null) {
			logger.d("chat#found msgSessionEntity");
			handleRecvMsg(msgInfo);
		} else {
			logger.d("chat#msgSessionEntity not found for msg:%s", msgInfo);
			noSessionEntityMsgList.add(msgInfo);
		}
	}

	private void handleRecvMsg(MessageInfo msgInfo) {
		logger.d("chat#handleRecvMsg");

		List<MessageInfo> msgInfoList = new ArrayList<MessageInfo>();
		msgInfoList.add(msgInfo);

		handleUnreadMsgListImpl(msgInfoList);
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

	// msgInfoList should belong to the same session
	private void handleUnreadMsgListImpl(List<MessageInfo> allMsgList) {
		logger.d("chat#repeat#handleUnreadMsgListImpl");
		if (allMsgList == null || allMsgList.isEmpty()) {
			logger.w("chat#repeat#empty msg list");
			return;
		}

		List<MessageInfo> unreadMsgList = filterOutReadMsgs(allMsgList);
		if (unreadMsgList.isEmpty()) {
			logger.d("repeat#all messages have been already read");
			
			//ack them
			//However, if we didn't ack them, next time if there's new message come along with them
			//we can ack them all together
			//todo eric
			return;
		}
		
		String sessionId = "";
		int sessionType = -1;

		for (MessageInfo msgInfo : unreadMsgList) {
			if (sessionId.isEmpty()) {
				sessionId = msgInfo.sessionId;

				//todo eric sessionType is still unclear here
				sessionType = msgInfo.sessionType;
			}

			List<MessageInfo> splitMessageList = IMContactHelper.splitMessage(msgInfo);
			logger.d("chat#splitMessage content %s", splitMessageList);

			for (MessageInfo msg : splitMessageList) {
				setMsgSessionType(msg);
				IMUnreadMsgManager.instance().add(msg);

				IMRecentSessionManager.instance().update(msg);
			}
		}

		if (sessionId.isEmpty()) {
			logger.e("chat#handleUnreadMsg sessionid is empty");
			return;
		}

		if (broadcastMessage(sessionId, sessionType, IMActions.ACTION_MSG_RECV, true)) {
//			logger.d("chat#broadcast receiving new msg, callstack:%s", Log.getStackTraceString(new Throwable()));
			logger.d("chat#broadcast receiving new msg");
		}
	}

	List<MessageInfo> filterOutReadMsgs(List<MessageInfo> msgList) {		
		MessageInfo lastMsg = msgList.get(msgList.size() - 1);
		logger.d("repeat#filterAlreadyReadMsg -> lastMsg:%s", lastMsg);

		int time = IMDbManager.instance(ctx).getLastSessionMsgTime(lastMsg);
		logger.d("repeat#last session time:%d", time);
		if (time <= 0) {
			logger.d("repeat#no msg filtered out, all message are unread msgs");
			return msgList;
		}

		//peek 
		MessageInfo firstMsg = msgList.get(0);
		if (time < firstMsg.getCreated()) {
			logger.d("repeat#all msgs are unread");
			return msgList;
		}

		List<MessageInfo> unreadMsgList = new ArrayList<MessageInfo>();

		for (int i = msgList.size() - 1; i >= 0; i--) {
			MessageInfo msg = msgList.get(i);
			if (msg.getCreated() > time) {
				logger.d("repeat#add truly unread msg:%s", msg);
				unreadMsgList.add(0, msg);
			} else {
				break;
			}
		}

		return unreadMsgList;
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
		List<MessageInfo> msgInfoList = new ArrayList<MessageInfo>();

		for (MessageEntity msgEntity : msgEntitiyList) {
			MessageInfo msgInfo = new MessageInfo(msgEntity);
			msgInfoList.add(msgInfo);
		}

		// todo eric, why the order is reversed
		Collections.reverse(msgInfoList);

		handleUnreadMsgListImpl(msgInfoList);

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
//			IMDbManager.instance(ctx).saveMsg(msgInfo, false);
		}
		
		IMDbManager.instance(ctx).saveMsgs(msgList, false);

		//if crash happens before this line, we only have a few repeat messages
		// repeat is better than lost, this is the central idea for the future design and bug fix
		IMDbManager.instance(ctx).updateSessionLastMsg(msgList.get(msgList.size() - 1));

		ackUnreadMsgs(sessionId, sessionType);

		return msgList;
	}

	private void ackUnreadMsgs(String sessionId, int sessionType) {
		if (sessionType == IMSession.SESSION_P2P) {
			ackContactUnreadMsgs(sessionId);
		} else {
			ackGroupUnreadMsgs(sessionId);
		}
	}

	public void ackContactUnreadMsgs(String contactId) {
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

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub
		if (action.equals(IMActions.ACTION_GROUP_READY)) {
			logger.d("chat#on action group ready");

			for (MessageInfo msg : noSessionEntityMsgList) {
				handleRecvMsg(msg);
			}
		}
	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		noSessionEntityMsgList.clear();
	}
}
