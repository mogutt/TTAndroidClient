package com.mogujie.tt.imlib.proto;

import java.util.ArrayList;
import java.util.List;

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

public class UnreadMsgGroupListPacket extends Packet {

	private Logger logger = Logger.getLogger(UnreadMsgGroupListPacket.class);


	public UnreadMsgGroupListPacket() {
		mRequest = new PacketRequest();
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
			int cnt = buffer.readInt();
			logger.d("packet#cnt:%d", cnt);
			
			for (int i = 0; i < cnt; ++i) {
				PacketResponse.Entity entity = new PacketResponse.Entity();

				entity.groupId = buffer.readString();
				entity.unreadCnt = buffer.readInt();
				
				logger.d("packet#groupid:%s, unreadCnt:%d", entity.groupId, entity.unreadCnt);
				
				res.entityList.add(entity);
			}

			mResponse = res;
		} catch (Exception e) {
			logger.e("chat#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {

		public PacketRequest() {
			Header header = new DefaultHeader(ProtocolConstant.SID_GROUP,
					ProtocolConstant.CID_GROUP_UNREAD_CNT_REQUEST);

			int contentLength = 0;
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {
		public static class Entity {
			public String groupId;
			public int unreadCnt;
		}

		public List<Entity> entityList = new ArrayList<Entity>();
	}
}
