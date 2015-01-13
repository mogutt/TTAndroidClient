package com.mogujie.tt.imlib.proto;

import java.util.List;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class CreateTempGroupPacket extends Packet {

	private Logger logger = Logger.getLogger(CreateTempGroupPacket.class);

	public CreateTempGroupPacket() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public CreateTempGroupPacket(String groupName, String groupAvatarUrl,
			List<String> memberList) {
		mRequest = new PacketRequest(groupName, groupAvatarUrl, memberList);
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

		bodyBuffer.writeString(req.groupName);
		bodyBuffer.writeString(req.groupAvatarUrl);
		writeStringList(req.memberList, bodyBuffer);

		int headLength = headerBuffer.readableBytes();
		int bodyLength = bodyBuffer.readableBytes();

		logger.d("tempgroup#message len:%d, header report len:%d", headLength
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

			logger.d("tempgroup#header:%s", header);

			// starts filling from here
			res.result = buffer.readInt();
			String groupId = buffer.readString();
			String groupName = buffer.readString();
			List<String> memberList = readStringList(buffer);

			if (res.result == 0) {
				res.entity.id = groupId;
				res.entity.name = groupName;
				res.entity.avatarUrl = "";
				res.entity.creatorId = IMLoginManager.instance().getLoginId();
				res.entity.type = IMSession.SESSION_TEMP_GROUP;
				res.entity.updated = (int) (System.currentTimeMillis() / 1000L);
				logger.d("updated#updated:%d", res.entity.updated);
				res.entity.memberIdList = memberList;
				logger.d("tempgroup#group:%s", res.entity);
			}

			mResponse = res;
		} catch (Exception e) {
			logger.e("tempgroup#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		String groupName;
		String groupAvatarUrl;
		List<String> memberList;

		public PacketRequest(String groupName, String groupAvatarUrl,
				List<String> memberList) {
			this.groupName = groupName;
			this.groupAvatarUrl = groupAvatarUrl;
			this.memberList = memberList;

			Header header = new DefaultHeader(ProtocolConstant.SID_GROUP,
					ProtocolConstant.CID_GROUP_CREATE_TMP_GROUP_REQUEST);

			int contentLength = getStringLen(groupName)
					+ getStringLen(groupAvatarUrl)
					+ getStringListLen(memberList);
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {
		public int result;
		public GroupEntity entity = new GroupEntity();
	}
}
