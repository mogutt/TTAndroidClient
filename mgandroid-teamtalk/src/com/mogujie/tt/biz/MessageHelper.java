package com.mogujie.tt.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.mogujie.tt.cache.BlockTargetCache;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.IMMessageManager;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.action.ActionCallback;
import com.mogujie.tt.packet.base.Packet;
import com.mogujie.tt.task.TaskCallback;
import com.mogujie.tt.task.TaskManager;
import com.mogujie.tt.task.biz.CheckUserBlockTask;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.utils.FileUtil;

/**
 * @Description 消息界面的公用业务逻辑处理
 * @author Nana
 * @date 2014-7-11
 */
public class MessageHelper {
	private static Logger logger = Logger.getLogger(MessageHelper.class);
	private static int customServiceType = SysConstant.DEFAULT_CUSTOM_SERVICE_TYPE;

	/**
	 * @Description 连上消息服务器之后，如果用户信息不全就重新获取
	 * @param msgHandler
	 * @param uiHandler
	 */
	public static void onConnectedMsgServer(Handler msgHandler,
			Handler uiHandler) {
		User chatUser = CacheHub.getInstance().getChatUser();
		if (null != chatUser) {
			Message msg = uiHandler.obtainMessage();
			msg.what = HandlerConstant.SET_TITLE;
			msg.obj = chatUser;
			uiHandler.sendMessage(msg);

			boolean invalidName = TextUtils.isEmpty(chatUser.getName())
					|| "null".equals(chatUser.getName());
			boolean invalidNickName = TextUtils.isEmpty(chatUser.getNickName())
					|| "null".equals(chatUser.getNickName());

			if (invalidName && invalidNickName) {
				MessageHelper.requestUserInfo(chatUser.getUserId(), msgHandler);
			}
		}
	}

	/**
	 * @Description 请求客服使用的回调
	 */
	public static ActionCallback getCallbackForRqsCustomService(
			final Handler uiHandler) {
		ActionCallback callback = new ActionCallback() {

			@Override
			public void onSuccess(Packet packet) {
			}

			@Override
			public void onTimeout(Packet packet) {
				uiHandler.sendEmptyMessage(HandlerConstant.REQUEST_CUSTOM_SERVICE_FAILED);
			}

			@Override
			public void onFaild(Packet packet) {
				onTimeout(packet);
			}
		};
		return callback;
	}

	/**
	 * @Description 服务端返回用户信息
	 * @param obj
	 */
	public static void onGetUserInfo(Object obj, Handler uiHandler,
			Context context) {
		MessageActivity.requestingUserInfo = false;
		if (obj == null) {
			return;
		}
		// Queue<User> userList = ((QueryUsersInfoResponse) (((Packet) obj)
		// .getResponse())).getUserList();
		// if (userList.size() != 0) {
		//
		// User user = userList.poll();
		// if (user == null) {
		// return;
		// }
		// CacheHub.getInstance().setChatUser(user);
		// CacheHub.getInstance().setUser(user, context);
		// Message msg = uiHandler.obtainMessage();
		// msg.what = HandlerConstant.SET_TITLE;
		// msg.obj = user;
		// uiHandler.sendMessage(msg);
		// }
	}

	public static void onReceiveMessage2(MessageInfo messageInfo,
			IMServiceHelper imServiceHelper) {
		// // MessageSplitResult msr = new MessageSplitResult(msg.msgInfo,
		// // msg.msgData);
		// //
		// // msr.decode();
		// //
		// // Queue<MessageInfo> msgListInfos = msr.getMsgList();
		//
		//
		// MessageInfo messageInfo = msg.msgInfo;

		if (messageInfo != null) {

			if (messageInfo.getDisplayType() != SysConstant.DISPLAY_TYPE_IMAGE) {
				messageInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
				// CacheHub.getInstance().updateMsgStatus(messageInfo.getMsgId(),
				// SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
			} else {
				if (!FileUtil.isSdCardAvailuable()) {
					messageInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);
					// CacheHub.getInstance().updateMsgStatus(
					// messageInfo.getMsgId(),
					// SysConstant.MESSAGE_STATE_FINISH_FAILED);
				}
			}

			logger.d("chat#start addItem");
			MessageActivity.addItem(messageInfo);
		}
	}

	// /**
	// * @Description 收到聊天对象发来的新消息
	// * @param obj
	// */
	// public static void onReceiveMessage(Object obj) {
	// if (obj == null)
	// return;
	//
	// RecvMessagePacket _packet = (RecvMessagePacket) obj;
	// RecvMesageResponse res = (RecvMesageResponse) _packet.getResponse();
	// if (res == null) {
	// return;
	// }
	// MessageSplitResult msr = res.getMsgSpliteResult();
	// if (msr == null)
	// return;
	//
	// Queue<MessageInfo> msgListInfos = msr.getMsgList();
	//
	// while (!msgListInfos.isEmpty()) {
	// MessageInfo messageInfo = msgListInfos.poll();
	//
	// if (messageInfo != null) {
	// User chatUser = CacheHub.getInstance().getChatUser();
	// if (chatUser != null
	// && messageInfo.getMsgFromUserId().equals(
	// chatUser.getUserId())) {
	//
	// messageInfo
	// .setMsgReadStatus(SysConstant.MESSAGE_ALREADY_READ);
	// CacheHub.getInstance().updateMsgReadStatus(
	// messageInfo.getMsgId(),
	// SysConstant.MESSAGE_ALREADY_READ);
	//
	// if (messageInfo.getDisplayType() != SysConstant.DISPLAY_TYPE_IMAGE) {
	// messageInfo
	// .setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
	// CacheHub.getInstance().updateMsgStatus(
	// messageInfo.getMsgId(),
	// SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
	// } else {
	// if (!FileUtil.isSdCardAvailuable()) {
	// messageInfo
	// .setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);
	// CacheHub.getInstance().updateMsgStatus(
	// messageInfo.getMsgId(),
	// SysConstant.MESSAGE_STATE_FINISH_FAILED);
	// }
	// }
	//
	// MessageActivity.addItem(messageInfo);
	// }
	// }
	// }
	//
	// }

	// /**
	// * @Description 服务端在接收到消息后会回复个消息确认
	// * @param obj
	// */
	// public static void onReceiveMsgACK(Object obj) {
	// if (obj == null)
	// return;
	//
	// MessagePacket packet = (MessagePacket) obj;
	// SendMessageAckResponse response = (SendMessageAckResponse) packet
	// .getResponse();
	// if (response == null)
	// return;
	// int nMsgId = response.getSeqNo();
	// MessageActivity.updateMessageState(nMsgId,
	// SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
	//
	// SendMessageRequest request = (SendMessageRequest) (packet.getRequest());
	// if (null == request)
	// return;
	// MessageInfo messageInfo = request.getMsgInfo();
	// if (null == messageInfo)
	// return;
	//
	// if (messageInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
	// MessageActivity.updateMessageState(messageInfo,
	// SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
	// }
	// }

	public static void onReceiveMsgACK2(Intent intent) {
		if (intent == null) {
			return;
		}
		
		String msgId = intent.getStringExtra(SysConstant.MSG_ID_KEY);
		logger.d("chat#onReceiveMsgACK2, msgId:%s", msgId);

		MessageActivity.updateMessageState(msgId, SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);

//		MessageInfo messageInfo = msg.msgInfo;
//		if (null == messageInfo)
//			return;
//
//		if (messageInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
//			MessageActivity.updateMessageState(messageInfo, SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
//		}
	}

	/**
	 * @Description 检查是否是黑名单用户(15分钟检测一次)
	 * @param shopId
	 * @param uiHandler
	 */
	public static void blockUserCheck(String shopId, final Handler uiHandler) {
		if (!BlockTargetCache.getInstance().needCheckBlock(shopId)) {
			return;
		}
		if (true) {
			uiHandler.sendEmptyMessage(HandlerConstant.START_BLOCK_CHECK);
			CheckUserBlockTask checkTask = new CheckUserBlockTask(shopId);
			checkTask.setCallBack(new TaskCallback() {

				@Override
				public void callback(Object result) {
					boolean isBlcok = (Boolean) result;
					if (isBlcok) {
						uiHandler.sendEmptyMessage(HandlerConstant.SHOULD_BLOCK_USER);
					} else {
						uiHandler.sendEmptyMessage(HandlerConstant.SHOULD_NOT_BLOCK_USER);
					}
				}
			});
			try {
				TaskManager.getInstance().trigger(checkTask);
			} catch (Exception e) {
				uiHandler.sendEmptyMessage(HandlerConstant.SHOULD_NOT_BLOCK_USER);
				logger.e(e.getMessage());
			}
		}
	}

	/**
	 * @Description 请求用户信息
	 * @param userId
	 * @param msgHandler
	 */
	public static void requestUserInfo(String userId, final Handler msgHandler) {
		if (MessageActivity.requestingUserInfo)
			return;
		MessageActivity.requestingUserInfo = true;
		Queue<String> userList = new LinkedList<String>();
		userList.add(userId);
		// //sendTaskForUserInfo(userList, new ActionCallback() {
		//
		// @Override
		// public void onSuccess(Packet packet) {
		// MessageHelper.sendMessageToMsgHandler(packet, msgHandler);
		// MessageActivity.requestingUserInfo = false;
		// }
		//
		// @Override
		// public void onTimeout(Packet packet) {
		// MessageActivity.requestingUserInfo = false;
		// }
		//
		// @Override
		// public void onFaild(Packet packet) {
		// MessageActivity.requestingUserInfo = false;
		// }
		// });
	}

	/**
	 * @Description 图片上传到文件服务器后执行
	 * @param obj
	 * @param uiHandler
	 * @param msgHandler
	 */
	public static void onImageUploadFinish(Object obj, Handler uiHandler,
			Handler msgHandler, IMMessageManager msgManager, int sessionType) {
		logger.d("chat#pic#onImageUploadFinish");
		if (null == obj)
			return;

		if (msgManager == null) {
			logger.e("chat#null msgManager");
		}

		MessageInfo messageInfo = (MessageInfo) obj;
		String imageUrl = messageInfo.getUrl();
		logger.d("pic#imageUrl:%s", imageUrl);
		String realImageURL = "";
		try {
			realImageURL = URLDecoder.decode(imageUrl, "utf-8");
			logger.d("pic#realImageUrl:%s", realImageURL);
		} catch (UnsupportedEncodingException e) {
			logger.e(e.toString());
		}
		messageInfo.setUrl(realImageURL);

		if (realImageURL.equals("")) {

			Message msg = uiHandler.obtainMessage();
			msg.what = HandlerConstant.HANDLER_SEND_MESSAGE_FAILED;
			msg.obj = messageInfo;
			uiHandler.sendMessage(msg);
			return;
		}

		messageInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);

		messageInfo.setMsgContent(SysConstant.MESSAGE_IMAGE_LINK_START
				+ realImageURL + SysConstant.MESSAGE_IMAGE_LINK_END);

		// MessageHelper.doSendTask(messageInfo, uiHandler, msgHandler);
		logger.d("pic#send pic message, content:%s", messageInfo.getMsgContent());
		msgManager.sendText(messageInfo.getTargetId(), messageInfo.getMsgContent(), sessionType, messageInfo);
	}

	/**
	 * @Description 向服务器发送消息
	 * @param msgInfo
	 * @param uiHandler
	 * @param msgHandler
	 */
	// public static void doSendTask(MessageInfo msgInfo, final Handler
	// uiHandler,
	// final Handler msgHandler) {
	//
	// Object[] objs = new Object[1];
	// objs[0] = msgInfo;
	// Packet packet = PacketDistinguisher.make(ProtocolConstant.SID_MSG,
	// ProtocolConstant.CID_MSG_DATA, objs, true);
	// ActionCallback callback = new ActionCallback() {
	//
	// @Override
	// public void onSuccess(Packet packet) {
	// sendMessageToMsgHandler(packet, msgHandler);
	// }
	//
	// @Override
	// public void onTimeout(Packet packet) {
	// if (null == packet)
	// return;
	//
	// SendMessageRequest request = (SendMessageRequest) packet
	// .getRequest();
	// if (null == request)
	// return;
	//
	// MessageInfo info = request.getMsgInfo();
	// if (null == info)
	// return;
	//
	// Message msg = uiHandler.obtainMessage();
	// msg.what = HandlerConstant.HANDLER_SEND_MESSAGE_TIMEOUT;
	// msg.obj = info;
	// uiHandler.sendMessage(msg);
	// }
	//
	// @Override
	// public void onFaild(Packet packet) {
	// onTimeout(packet);
	// }
	// };
	// PushActionToQueueTask task = new PushActionToQueueTask(packet, callback);
	// TaskManager.getInstance().trigger(task);
	// }

	/**
	 * @Description 向msg handler发送消息
	 * @param packet
	 * @param msgHandler
	 */
	public static void sendMessageToMsgHandler(Packet packet, Handler msgHandler) {
		if (packet == null || msgHandler == null)
			return;
		int serviceId = packet.getResponse().getHeader().getServiceId();
		int commandId = packet.getResponse().getHeader().getCommandId();
		Message msg = new Message();
		msg.what = serviceId * 1000 + commandId;
		msg.obj = packet;
		msgHandler.sendMessage(msg);
	}

	// /**
	// * @Description 获取用户信息
	// * @param userIdList
	// * @param callback
	// */
	// public static void sendTaskForUserInfo(Queue<String> userIdList,
	// ActionCallback callback) {
	//
	// Object[] obj = new Object[1];
	// obj[0] = userIdList;
	//
	// Packet packet = PacketDistinguisher.make(
	// ProtocolConstant.SID_BUDDY_LIST,
	// ProtocolConstant.CID_GET_USER_INFO_REQUEST, obj, true);
	// PushActionToQueueTask task = new PushActionToQueueTask(packet, callback);
	// TaskManager.getInstance().trigger(task);
	//
	// }
	//
	/**
	 * @Description 设置消息的基本信息
	 * @param userId
	 * @param msgDisplayType
	 * @param msg
	 * @return
	 */
	private static MessageInfo setMsgBaseInfo(String userId,
			int msgDisplayType, MessageInfo msg) {
		if (msg == null || TextUtils.isEmpty(userId)) {
			return null;
		}

		msg.setMsgFromUserId(CacheHub.getInstance().getLoginUserId());
		msg.setIsSend(true);
		int createTime = (int) (System.currentTimeMillis() / 1000);
		msg.setMsgCreateTime(createTime);
		msg.setTargetId(userId);
		msg.setDisplayType(msgDisplayType);
		msg.setMsgType(SysConstant.MESSAGE_TYPE_TELETEXT); // 1语音或文本消息
															// 100 语音消息
		msg.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);
		msg.setMsgReadStatus(SysConstant.MESSAGE_ALREADY_READ);
		byte msgType = 1;
		msg.setMsgType(msgType);
		msg.setMsgAttachContent("");
//		int messageSendRequestNo = CacheHub.getInstance().obtainMsgId();
//		msg.setMsgId(messageSendRequestNo);
		return msg;
	}

	/**
	 * @Description 生成消息对象
	 * @param targetUserId
	 * @param content
	 * @return
	 */
	public static MessageInfo obtainTextMessage(String targetUserId,
			String content) {
		logger.d("chat#text#generating text message, toId:%s, content:%s", targetUserId, content);
		
		if (TextUtils.isEmpty(content) || TextUtils.isEmpty(targetUserId)) {
			return null;
		}

		MessageInfo msg = new MessageInfo();
		msg.setMsgContent(content);

		msg = setMsgBaseInfo(targetUserId, SysConstant.DISPLAY_TYPE_TEXT, msg);
		CacheHub.getInstance().pushMsg(msg);
		return msg;
	}

	/**
	 * @Description 设置语音消息内容
	 * @param msg
	 */
	public static void setMsgAudioContent(MessageInfo msg) {
		if (null == msg) {
			return;
		}
		int tLen = msg.getPlayTime();

		if (tLen < 0) {
			return;
		}
		byte[] result = new byte[4];
		result = CommonUtil.intToBytes(tLen);

		String msgAudioSavePath = msg.getSavePath();
		if (msgAudioSavePath == null) {
			return;
		}

		byte[] bytes = FileUtil.getFileContent(msgAudioSavePath);
		if (bytes == null) {
			return;
		}

		int contentLength = bytes.length;
		byte[] byteAduioContent = new byte[4 + contentLength];
		System.arraycopy(result, 0, byteAduioContent, 0, 4);
		System.arraycopy(bytes, 0, byteAduioContent, 4, contentLength);

		msg.setAudioContent(byteAduioContent);
	}

	public static int getCustomServiceType() {
		return customServiceType;
	}

}
