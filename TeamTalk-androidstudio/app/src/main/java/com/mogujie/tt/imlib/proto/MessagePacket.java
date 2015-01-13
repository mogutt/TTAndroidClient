package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class MessagePacket extends Packet {

	private Logger logger = Logger.getLogger(MessagePacket.class);

	public MessagePacket() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public MessagePacket(MessageEntity entity) {
		mRequest = new PacketRequest(entity);
		setNeedMonitor(true);
	}

	@Override
	public DataBuffer encode() {

		Header header = mRequest.getHeader();
		DataBuffer headerBuffer = header.encode();
		DataBuffer bodyBuffer = new DataBuffer();

		PacketRequest req = (PacketRequest) mRequest;
		if (null == req)
			return null;

		MessageEntity entity = req.entity;
		bodyBuffer.writeInt(entity.seqNo);
		bodyBuffer.writeString(entity.fromId);
		bodyBuffer.writeString(entity.toId);
		bodyBuffer.writeInt(entity.createTime);
		bodyBuffer.writeByte(entity.type);
		//bodyBuffer.writeByte(1);
		bodyBuffer.writeInt(entity.msgLen);
		bodyBuffer.writeBytes(entity.msgData);
		bodyBuffer.writeString(entity.attach);

		int headLength = headerBuffer.readableBytes();
		int bodyLength = bodyBuffer.readableBytes();
		
		logger.d("chat#message len:%d, header report len:%d", headLength + bodyLength, header.getLength());

		DataBuffer buffer = new DataBuffer(headLength + bodyLength);
		buffer.writeDataBuffer(headerBuffer);
		buffer.writeDataBuffer(bodyBuffer);

		return buffer;
	}

	@Override
	public void decode(DataBuffer buffer) {

		if (null == buffer)
			return;
		try {
			PacketResponse res = new PacketResponse();

			Header header = new Header();
			header.decode(buffer);
			res.setHeader(header);
			
			logger.d("chat#header:%s", header);
			
			// starts filling from here
			res.msgAck.seqNo = buffer.readInt();
			res.msgAck.fromId = buffer.readString();

			mResponse = res;
		} catch (Exception e) {
			logger.e("chat#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		private MessageEntity entity;

		public PacketRequest(MessageEntity message) {
			entity = message;
			Header header = new DefaultHeader(ProtocolConstant.SID_MSG,
					ProtocolConstant.CID_MSG_DATA);

			int contentLength = 4 + (4 + getUtf8Bytes(message.fromId).length)
					+ (4 + getUtf8Bytes(message.toId).length) + 4 + 1 + message.msgLen
					+ 4 + getUtf8Bytes(message.attach).length + 4;
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	
	public static class PacketResponse extends Response {
		public MsgAckEntity msgAck = new MsgAckEntity();
	}
}
