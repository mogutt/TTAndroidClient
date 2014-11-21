package com.mogujie.tt.imlib.proto;

import java.util.List;

import android.database.CursorJoiner.Result;

import com.mogujie.tt.R.string;
import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.proto.LoginPacket.PacketRequest.Entity;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.DefaultHeader;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class LoginPacket extends Packet {

	private Logger logger = Logger.getLogger(LoginPacket.class);

	public LoginPacket() {
		// todo eric remove this
		setNeedMonitor(true);
	}

	public LoginPacket(Entity entity) {
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

		bodyBuffer.writeString(req.entity.name);
		bodyBuffer.writeString(req.entity.pass);
		bodyBuffer.writeInt(req.entity.onlineStatus);
		bodyBuffer.writeInt(req.entity.clientType);
		bodyBuffer.writeString(req.entity.clientVersion);

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
			
			/*public int serverTime;
			public int result;
			public int onlineStatus;
			public String name;
			public String userId;
			public String nickName;
			public String avatarUrl;
			public String tile;
			public String position;
			public int roleStatus;
			public int sex;
			public String departId;
			public int jobNUm;
			public String telphone;
			public String email;
			public String token;
			public int userType;
			*/
			res.entity.serverTime = buffer.readInt();
			res.entity.result = buffer.readInt();
			if(res.entity.result == 0) {
				res.entity.onlineStatus = buffer.readInt();
				res.entity.userId = buffer.readString();
				//res.entity.name = buffer.readString();
				res.entity.nickName = buffer.readString();
				res.entity.avatarUrl = buffer.readString();
				res.entity.tile = buffer.readString();
				res.entity.position = buffer.readString();
				res.entity.roleStatus = buffer.readInt();
				res.entity.sex = buffer.readInt();
				res.entity.departId = buffer.readString();
				res.entity.jobNUm = buffer.readInt();
				res.entity.telphone = buffer.readString();
				res.entity.email = buffer.readString();
				res.entity.token = buffer.readString();
				//res.entity.userType = buffer.readInt();
			}

			mResponse = res;
		} catch (Exception e) {
			logger.e("packet#decode exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}

	}

	public static class PacketRequest extends Request {
		public static class Entity {
			public String name;
			public String pass;
			public int onlineStatus;
			public int clientType;
			public String clientVersion;
		}

		public Entity entity;

		public PacketRequest(Entity entity) {
			this.entity = entity;
			Header header = new DefaultHeader(ProtocolConstant.SID_LOGIN, ProtocolConstant.CID_LOGIN_REQ_USERLOGIN);

			int contentLength = getStringLen(entity.name)
					+ getStringLen(entity.pass)
					+ getIntLen(entity.onlineStatus)
					+ getIntLen(entity.clientType)
					+ getStringLen(entity.clientVersion);
			header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

			setHeader(header);
		}
	}

	public static class PacketResponse extends Response {
		public static class Entity {
			public int serverTime;
			public int result;
			public int onlineStatus;
			public String name;
			public String userId;
			public String nickName;
			public String avatarUrl;
			public String tile;
			public String position;
			public int roleStatus;
			public int sex;
			public String departId;
			public int jobNUm;
			public String telphone;
			public String email;
			public String token;
			public int userType;
		}
		
		   public User getUser() {
	            User user = new User();
	            user.setOnlineStatus(entity.onlineStatus);
	            user.setUserId(entity.userId);
	            user.setNickName(entity.nickName);
	            user.setAvatarUrl(entity.avatarUrl);
	            user.setTitle("");
	            user.setPosition("");
	            user.setRoleStatus(0);
	            user.setSex(0);
	            user.setDepartId("");
	            user.setJobNum(0);
	            user.setTelphone("");
	            user.setEmail("");
	            user.setToken("");

	            return user;
	        }

		public Entity entity = new Entity();
	}
}
