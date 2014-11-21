package com.mogujie.tt.imlib.proto;

import java.util.ArrayList;
import java.util.List;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.proto.GroupUnreadMsgPacket.PacketRequest.Entity;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class GroupUnreadMsgPacket extends Packet {

	private Logger logger = Logger.getLogger(GroupUnreadMsgPacket.class);

	public GroupUnreadMsgPacket() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public GroupUnreadMsgPacket(Entity entity) {
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

		bodyBuffer.writeString(req.entity.groupId);

		int headLength = headerBuffer.readableBytes();
		int bodyLength = bodyBuffer.readableBytes();

		logger.d("packet#message len:%d, header report len:%d", headLength
				+ bodyLength, header.getLength());

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

			logger.d("packet#header:%s", header);

			// starts filling from here
			String groupId = buffer.readString();
			int msgCnt = buffer.readInt();
			for (int i = 0; i < msgCnt; ++i) {
				String fromId = buffer.readString();
				int createTime = buffer.readInt();
				byte msgType = buffer.readByte();
				int msgLen = buffer.readInt();
				byte[] msgData = null; 
				if (msgLen > 0) {
					msgData = buffer.readBytes(msgLen);
				}
				
				MessageEntity message = new MessageEntity();
				message.fromId = fromId;
				message.toId = groupId;
				message.createTime = createTime;
				message.talkerId = fromId;
				
				message.type = msgType;
				message.msgLen = msgLen;
				message.msgData = msgData;
				message.sessionId = message.toId;
				
				res.entityList.add(message);

			}

			mResponse = res;
		} catch (Exception e) {
			logger.e("packet#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		public static class Entity {
			public String groupId;
		}

		public Entity entity;

		public PacketRequest(Entity entity) {
			this.entity = entity;
			Header header = new DefaultHeader(ProtocolConstant.SID_GROUP,
					ProtocolConstant.CID_GROUP_UNREAD_MSG_REQUEST);

			int contentLength = getStringLen(entity.groupId);
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {

		public List<MessageEntity> entityList = new ArrayList<MessageEntity>();
	}
}
