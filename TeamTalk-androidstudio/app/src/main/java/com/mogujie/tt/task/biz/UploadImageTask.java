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
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.task.MAsyncTask;
import com.mogujie.tt.ui.tools.PhotoHandler;

public class UploadImageTask extends MAsyncTask {
	//	private String strUrl;
	//	private String strDao;
	private Logger logger = Logger.getLogger(UploadImageTask.class);
	private List<MessageInfo> list;
	private Handler handler;

	public UploadImageTask(Handler handler, int sessionType, String url,
			String Dao, List<MessageInfo> msgInfoList) {
		this.handler = handler;
		list = msgInfoList;

		logger.d("chat#pic#put uploading images msg list into unack manager");

		for (MessageInfo msgInfo : msgInfoList) {
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
					//					byte[] bytes = PhotoHandler.getBytes(bitmap);
					MoGuHttpClient httpClient = new MoGuHttpClient();
					// result = httpClient.uploadImage(strUrl, bytes,
					// messageInfo.getSavePath(), strDao);

					// todo eric result
					// result =
					// httpClient.uploadImage3("http://122.225.68.125:8001/upload/",
					// messageInfo.getSavePath());

					byte[] bytes = PhotoHandler.getBytes(bitmap);
					result = httpClient.uploadImage3(SysConstant.UPLOAD_IMAGE_URL_PREFIX
							+ "upload/", bytes, messageInfo.getSavePath());

					logger.d("pic#uploadImage ret url:%s", result);

				}

				if (handler == null) {
					logger.e("pic#handler is null");
					return null;
				}

				Message message = handler.obtainMessage();

				if (TextUtils.isEmpty(result)) {
					logger.e("pic#upload failed");

					message.what = HandlerConstant.HANDLER_IMAGE_UPLOAD_FAILD;
					message.obj = messageInfo;
					messageInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);

				} else {
					String imageUrl = (String) result;
					logger.e("pic#upload ok, url:%s", imageUrl);

					Logger.getLogger(UploadImageTask.class).d(imageUrl);
					messageInfo.setUrl(imageUrl);
					message.what = HandlerConstant.HANDLER_IMAGE_UPLOAD_SUCESS;
					message.obj = messageInfo;
				}
				
				handler.sendMessage(message);

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
