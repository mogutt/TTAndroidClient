package com.mogujie.tt.imlib.proto;

import java.util.ArrayList;
import java.util.List;

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

public class UnreadMsgPacket extends Packet {

	private Logger logger = Logger.getLogger(UnreadMsgPacket.class);

	public UnreadMsgPacket(String contactId) {
		mRequest = new PacketRequest(contactId);
		setNeedMonitor(true);
	}

	public UnreadMsgPacket() {
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

		bodyBuffer.writeString(req.contactId);

		int headLength = headerBuffer.readableBytes();
		int bodyLength = bodyBuffer.readableBytes();

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

			// starts filling from here
			String sessionId = buffer.readString();
			int cnt = buffer.readInt();
			for (int i = 0; i < cnt; ++i) {
				MessageEntity entity = new MessageEntity();

				String id = buffer.readString();
				String name = buffer.readString();
				String nickName = buffer.readString();
				String avatarUrl = buffer.readString();
				int createTime = buffer.readInt();
				byte msgType = buffer.readByte();
				int msgLen = buffer.readInt();
				byte[] msgContent = null;
				if (msgLen > 0) {
					msgContent = buffer.readBytes(msgLen);
				}

				entity.fromId = id;
				entity.toId = IMLoginManager.instance().getLoginId();
				entity.talkerId = id;
				entity.createTime = createTime;
				entity.type = msgType;
				entity.msgLen = msgLen;
				entity.msgData = msgContent;
				entity.sessionId = id;

				logger.d("unread#1");
				logger.d("unread#got unreadmsg:%s", entity);
				res.entityList.add(entity);
			}

			mResponse = res;
		} catch (Exception e) {
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		public String contactId;

		public PacketRequest(String contactId) {
			this.contactId = contactId;
			Header header = new DefaultHeader(ProtocolConstant.SID_MSG,
					ProtocolConstant.CID_MSG_UNREAD_MSG_REUQEST);

			int contentLength = getUtf8Bytes(contactId).length;
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + (4 + contentLength));

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {

		public List<MessageEntity> entityList = new ArrayList<MessageEntity>();
	}
}
