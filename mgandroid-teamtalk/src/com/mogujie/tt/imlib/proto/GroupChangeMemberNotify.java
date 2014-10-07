package com.mogujie.tt.imlib.proto;

import java.util.List;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.proto.GroupChangeMemberNotify.PacketRequest.Entity;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class GroupChangeMemberNotify extends Packet {

	private Logger logger = Logger
			.getLogger(GroupChangeMemberNotify.class);

	public GroupChangeMemberNotify() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public GroupChangeMemberNotify(Entity entity) {
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
		bodyBuffer.writeInt(req.entity.changeType);
		writeStringList(req.entity.memberList, bodyBuffer);

		int headLength = headerBuffer.readableBytes();
		int bodyLength = bodyBuffer.readableBytes();

		logger.d("chat#message len:%d, header report len:%d", headLength
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

			logger.d("chat#header:%s", header);

			// starts filling from here
			res.entity.groupId = buffer.readString();
			res.entity.memberList = readStringList(buffer);

			mResponse = res;
		} catch (Exception e) {
			logger.e("chat#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		public static class Entity {
			public String groupId;
			public int changeType;
			public List<String> memberList;
		}

		public Entity entity;

		public PacketRequest(Entity entity) {
			this.entity = entity;
			Header header = new DefaultHeader(ProtocolConstant.SID_GROUP,
					ProtocolConstant.CID_GROUP_CHANGE_MEMBER_REQUEST);

			int contentLength = getStringLen(entity.groupId)
					+ getIntLen(entity.changeType)
					+ getStringListLen(entity.memberList);
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {
		public static class Entity {
			String groupId;
			List<String> memberList;
		}

		public Entity entity = new Entity();
	}
}
