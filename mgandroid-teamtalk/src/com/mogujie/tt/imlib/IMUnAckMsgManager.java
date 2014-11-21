package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.db.IMDbManager;
import com.mogujie.tt.log.Logger;

public class IMUnAckMsgManager extends IMManager {
	private static IMUnAckMsgManager inst;

	public static IMUnAckMsgManager instance() {
		synchronized (IMUnAckMsgManager.class) {
			if (inst == null) {
				inst = new IMUnAckMsgManager();
			}

			return inst;
		}
	}

	private class UnAckMsg {
		public UnAckMsg(MessageInfo msg, long timeoutElapsedRealtime) {
			this.msg = msg;
			this.timeoutElapsedRealtime = timeoutElapsedRealtime;
		}

		public MessageInfo msg;
		public long timeoutElapsedRealtime;
	}

	private Logger logger = Logger.getLogger(IMUnAckMsgManager.class);

	// todo eric, after testing ok, make it a longer value
	private final long TIMEOUT_MILLISECONDS = 6 * 1000;
	private final long IMAGE_TIMEOUT_MILLISECONDS = 4 * 60 * 1000;
	// key = msgId
	private HashMap<String, UnAckMsg> unackMsgList = new HashMap<String, UnAckMsg>();
	private Handler timerHandler = new Handler();

	private long getTimeoutTolerance(MessageInfo msgInfo) {
		if (msgInfo.isImage()) {
			return IMAGE_TIMEOUT_MILLISECONDS;
		} else {
			return TIMEOUT_MILLISECONDS;
		}
	}

	public synchronized void handleTimeoutUnAckMsg(String msgId) {
		if (msgId == null) {
			return;
		}

		logger.d("unack#handleTimeoutUnAckMsg ,msgId:%s", msgId);

		UnAckMsg unAckMsg = unackMsgList.get(msgId);
		if (unAckMsg == null) {
			logger.e("unack#so such message -> msgId:%s", msgId);
			return;
		}

		MessageInfo msg = unAckMsg.msg;
		handleTimeoutUnAckMsgImpl(msg);

		removeUnackMsg(msg.msgId);
	}

	private synchronized void removeUnackMsg(String msgId) {
		logger.d("unack#removeUnackMsg msgId:%s", msgId);

		unackMsgList.remove(msgId);
	}

	private void handleTimeoutUnAckMsgImpl(MessageInfo msg) {
		logger.d("unack#msg is unack timeout -> msg:%s", msg);
		msg.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);
		IMDbManager.instance(ctx).updateMessageStatus(msg);

		Intent intent = new Intent(IMActions.ACTION_MSG_UNACK_TIMEOUT);
		intent.putExtra(SysConstant.MSG_ID_KEY, msg.msgId);
		ctx.sendBroadcast(intent);

		logger.d("unack#broadcast is ok");
	}

	// todo eric make locker more smaller
	private synchronized void timerImpl() {
		logger.d("unack#UnAckMsgTimeoutTimer run");

		long currentElapsedRealtime = SystemClock.elapsedRealtime();

		List<MessageInfo> toRemovedMsgList = new ArrayList<MessageInfo>();
		for (java.util.Map.Entry<String, UnAckMsg> entry : unackMsgList
				.entrySet()) {
			UnAckMsg unAckMsg = entry.getValue();

			// todo eric optimization
			if (currentElapsedRealtime >= unAckMsg.timeoutElapsedRealtime) {
				logger.d("unack#find timeout msg");
				handleTimeoutUnAckMsgImpl(unAckMsg.msg);
				toRemovedMsgList.add(unAckMsg.msg);
			}
		}

		for (MessageInfo msg : toRemovedMsgList) {
			removeUnackMsg(msg.msgId);
		}
	}

	private void startTimer() {
		timerHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				timerImpl();
				startTimer();
			}
		}, 10 * 1000);
	}

	public synchronized void startUnAckTimeoutTimer() {
		logger.d("unack#startUnAckMsgTimeoutTimer");
		startTimer();
	}

	public synchronized void add(MessageInfo msgInfo) {
		logger.d("unack#add unack msg -> msgInfo:%s", msgInfo);

		String msgId = msgInfo.msgId;
		// todo eric efficiency
		if (unackMsgList.containsKey(msgId) || msgInfo.isResend()) {
			// for uploading image msg, it has already been added to the list
			// when
			// uploading the image, and at the time of sending the msg to the
			// peer, reset the timer
			IMDbManager.instance(ctx).deleteMsg(msgId);
			removeUnackMsg(msgId);
		}

		IMDbManager.instance(ctx).saveMsg(msgInfo, true);

		unackMsgList.put(msgInfo.msgId,
				new UnAckMsg(msgInfo, SystemClock.elapsedRealtime()
						+ getTimeoutTolerance(msgInfo)));
	}

	public synchronized MessageInfo remove(int msgSeqNo) {
		logger.d("unack#try to remove unack msg -> seqNo:%d", msgSeqNo);
		logger.d("unack#current unack msg cnt:%d", unackMsgList.size());

		for (java.util.Map.Entry<String, UnAckMsg> entry : unackMsgList
				.entrySet()) {
			UnAckMsg unAckMsg = entry.getValue();
			if (unAckMsg.msg.seqNo == msgSeqNo) {

				logger.d("unack#remove ok");

				MessageInfo msgInfo = unAckMsg.msg;
				removeUnackMsg(unAckMsg.msg.msgId);
				return msgInfo;
			}
		}

		return null;
	}

	public synchronized MessageInfo get(String msgId) {
		logger.d("unack#get msgId:%s", msgId);
		UnAckMsg unAckMsg = unackMsgList.get(msgId);
		if (unAckMsg == null) {
			return null;
		} else {
			return unAckMsg.msg;
		}
	}

	@Override
	public void reset() {
		unackMsgList.clear();
	}
}
