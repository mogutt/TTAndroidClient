package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class MessageNotifyPacket extends Packet {

	private Logger logger = Logger.getLogger(MessageNotifyPacket.class);

	public MessageNotifyPacket() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public MessageNotifyPacket(MessageEntity msg) {
		mRequest = new PacketAck(msg);
		setNeedMonitor(true);
	}

	@Override
	public DataBuffer encode() {

		Header header = mRequest.getHeader();
		DataBuffer headerBuffer = header.encode();
		DataBuffer bodyBuffer = new DataBuffer();

		PacketAck req = (PacketAck) mRequest;
		if (null == req)
			return null;

		MsgAckEntity msgAck = req.msgAck;
		bodyBuffer.writeInt(msgAck.seqNo);
		bodyBuffer.writeString(msgAck.fromId);
		
		int headLength = headerBuffer.readableBytes();
		int bodyLength = bodyBuffer.readableBytes();

		DataBuffer buffer = new DataBuffer(headLength + bodyLength);
		buffer.writeDataBuffer(headerBuffer);
		buffer.writeDataBuffer(bodyBuffer);

		return buffer;
	}

	@Override
	public void decode(DataBuffer buffer) {
		if (null == buffer) {
			return;
		}
			
		try {
			packetNotify res = new packetNotify();

			Header header = new Header();
			header.decode(buffer);
			res.setHeader(header);
			logger.d("chat#recv message header:%s, actually len:%d", header, buffer.getOrignalBuffer().capacity());
			
			MessageEntity msg = res.msg;
			msg.seqNo = buffer.readInt();
			msg.fromId = buffer.readString();
			msg.toId = buffer.readString();
			//talker id is equal to the from  id forever
			msg.talkerId = msg.fromId;
			
			if (msg.fromId.equals(IMLoginManager.instance().getLoginId())) {
				logger.d("chat#recv multiLoginSelfMsg");
				msg.multiLoginSelfMsg = true;
			}
			
			msg.createTime = buffer.readInt();
			msg.type = buffer.readByte();
			
			//todo eric check the acutual remaining buffer length first, to refuse bug like notorious ssl heartbleed one
			//todo eric test:modify the incoming packet, and see if the client can handle wrong packet, and should be no crash 
			msg.msgLen = buffer.readInt();
			if (msg.msgLen > 0) {
				msg.msgData = buffer.readBytes(msg.msgLen);
			}
			
			msg.attach = buffer.readString();
			
			mResponse = res;
		} catch (Exception e) {
			logger.e(e.getMessage());
		}

	}

	public static class PacketAck extends Ack {
		private MsgAckEntity msgAck = new MsgAckEntity();

		public PacketAck(MessageEntity msg) {
			msgAck.seqNo = msg.seqNo;
			msgAck.fromId = msg.fromId;
			
			Header header = new DefaultHeader(ProtocolConstant.SID_MSG,
					ProtocolConstant.CID_MSG_DATA_ACK);

			int contentLength = 4 + (4 + msgAck.fromId.length());
			
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class packetNotify extends Notify {
		public MessageEntity msg = new MessageEntity();
	}
}
