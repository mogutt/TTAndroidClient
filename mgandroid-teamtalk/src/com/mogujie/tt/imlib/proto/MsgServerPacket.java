package com.mogujie.tt.imlib.proto;

import java.util.List;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.proto.MsgServerPacket.PacketRequest.Entity;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class MsgServerPacket extends Packet {

	private Logger logger = Logger.getLogger(MsgServerPacket.class);

	public MsgServerPacket() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public MsgServerPacket(Entity entity) {
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
			res.entity.result = buffer.readInt();
			/*
			 * public String ip1;
			public String ip2;
			public short port;

			 */
			res.entity.ip1 = buffer.readString();
			res.entity.ip2 = buffer.readString();
			res.entity.port = buffer.readShort();

			mResponse = res;
		} catch (Exception e) {
			logger.e("packet#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		public static class Entity {
			public int userType;
		}

		public Entity entity;

		public PacketRequest(Entity entity) {
			this.entity = entity;
			Header header = new DefaultHeader(ProtocolConstant.SID_LOGIN, ProtocolConstant.CID_LOGIN_REQ_MSGSERVER);

			int contentLength = 0;
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {
		public static class Entity {
			public int result;
			public String ip1;
			public String ip2;
			public short port;
		}

		public Entity entity = new Entity();
	}
}
