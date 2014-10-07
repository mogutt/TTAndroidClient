package com.mogujie.tt.task.biz;

import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.config.TaskConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.https.MoGuHttpClient;
import com.mogujie.tt.imlib.IMUnAckMsgManager;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.task.MAsyncTask;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.tools.PhotoHandler;

public class UploadImageTask extends MAsyncTask {
	private String strUrl;
	private String strDao;
	List<MessageInfo> list;
	private Logger logger = Logger.getLogger(UploadImageTask.class);
	private IMService imService;

	public UploadImageTask(IMService imService, int sessionType, String url, String Dao, List<MessageInfo> msgInfoList) {
		this.imService = imService;
		
		strUrl = url;
		strDao = Dao;
		list = msgInfoList;
		// setCallBack(this);
		
		logger.d("chat#pic#put uploading images msg list into unack manager");
		
		for (MessageInfo msgInfo : msgInfoList) {
			msgInfo.generateMsgId();
			msgInfo.generateSessionId(true);
			msgInfo.generateSessionType(sessionType);
			IMUnAckMsgManager.instance().add(msgInfo);
		}
	}

	@Override
	public int getTaskType() {
		return TaskConstant.TASK_UPLOAD_IMAGE;
	}

	@Override
	public Object doTask() {

		for (MessageInfo messageInfo : list) {
			String result = null;
			Bitmap bitmap = null;
			try {
				bitmap = PhotoHandler.revitionImage(messageInfo.getSavePath());
				if (null != bitmap) {
					byte[] bytes = PhotoHandler.getBytes(bitmap);
					MoGuHttpClient httpClient = new MoGuHttpClient();
					// result = httpClient.uploadImage(strUrl, bytes,
					// messageInfo.getSavePath(), strDao);

					// todo eric result
					// result =
					// httpClient.uploadImage3("http://122.225.68.125:8001/upload/",
					// messageInfo.getSavePath());

					result = httpClient.uploadImage3("http://122.225.68.125:8600/upload/", messageInfo.getSavePath());

					logger.d("pic#uploadImage ret url:%s", result);

				}

				Handler handler = MessageActivity.getUiHandler();
				Message message = handler.obtainMessage();

				if (TextUtils.isEmpty(result)) {
					message.what = HandlerConstant.HANDLER_IMAGE_UPLOAD_FAILD;
					message.obj = messageInfo;
					messageInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);
					if (imService != null) {
						imService.getUnAckMsgManager().handleTimeoutUnAckMsg(messageInfo);
					}

					logger.e("pic#send HANDLER_IMAGE_UPLOAD_FAILD to messageactivity");
					handler.sendMessage(message);
				} else {
					String imageUrl = (String) result;
					Logger.getLogger(UploadImageTask.class).d(imageUrl);
					messageInfo.setUrl(imageUrl);
					message.what = HandlerConstant.HANDLER_IMAGE_UPLOAD_SUCESS;
					message.obj = messageInfo;

					logger.d("pic#send HANDLER_IMAGE_UPLOAD_SUCESS to messageactivity");

					handler.sendMessage(message);
				}
			} catch (IOException e) {
				logger.e(e.getMessage());
			}
		}

		return null;
	}

	// @Override
	// public void callback(Object result) {
	// Handler handler = MessageActivity.getMsgHandler();
	// Message message = handler.obtainMessage();
	//
	// if (result == null) {
	// message.what = HandlerConstant.HANDLER_IMAGE_UPLOAD_FAILD;
	// message.obj = messageInfo;
	// handler.sendMessage(message);
	// } else {
	// String imageUrl = (String) result;
	// Logger.getLogger(UploadImageTask.class).d(imageUrl);
	// messageInfo.setUrl(imageUrl);
	// message.what = HandlerConstant.HANDLER_IMAGE_UPLOAD_SUCESS;
	// message.obj = messageInfo;
	// handler.sendMessage(message);
	// }
	// }
}
