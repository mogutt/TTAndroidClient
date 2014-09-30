package com.mogujie.tt.imlib;

import java.util.LinkedList;
import java.util.List;

import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.log.Logger;

public class IMUnAckMsgManager {
	private Logger logger = Logger.getLogger(IMUnAckMsgManager.class);

	private List<MessageEntity> unackMsgList = new LinkedList<MessageEntity>();

	public void add(MessageEntity msg, MessageInfo msgInfo) {
		logger.d("unack#add unack msg -> seqNo:%d", msg.seqNo);
		
		//todo eric
		msg.msgInfo = msgInfo;
		
		synchronized (unackMsgList) {
			// todo eric, add timeout
			unackMsgList.add(msg);
		}
	}

	public MessageEntity remove(int msgSeqNo) {
		logger.d("unack#try to remove unack msg -> seqNo:%d", msgSeqNo);
		logger.d("current unack msg cnt:%d", unackMsgList.size());
		
		synchronized (unackMsgList) {
			for (MessageEntity entity : unackMsgList) {
				if (entity.seqNo == msgSeqNo) {
					logger.d("unack#remove ok");
					unackMsgList.remove(entity);
					return entity;
				}
			}

			return null;
		}
	}

}
