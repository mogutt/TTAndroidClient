package com.mogujie.tt.imlib;

import org.jboss.netty.buffer.ChannelBuffer;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;

public class IMPacketDispatcher {
	private static Logger logger = Logger.getLogger(IMPacketDispatcher.class);

	public static void dispatch(ChannelBuffer channelBuffer) {
		if (channelBuffer == null) {
			logger.e("packet#channelBuffer is null");
			return;
		}

		DataBuffer buffer = new DataBuffer(channelBuffer);

		Header header = new Header();
		header.decode(buffer);

		buffer.resetReaderIndex();
		int commandId = header.getCommandId();
		int serviceId = header.getServiceId();

		ProtocolConstant.ProtocolDumper.dump(false, header);

		logger.d("dispatch packet, serviceId:%d, commandId:%d", serviceId,
				commandId);

		// todo eric make it a table
		if (serviceId == ProtocolConstant.SID_LOGIN) {
			switch (commandId) {
			case ProtocolConstant.CID_LOGIN_RES_MSGSERVER:
				IMLoginManager.instance().onRepMsgServerAddrs(buffer);
				return;
			case ProtocolConstant.CID_LOGIN_RES_USERLOGIN:
				IMLoginManager.instance().onRepMsgServerLogin(buffer);
				return;
			}
		} else if (serviceId == ProtocolConstant.SID_BUDDY_LIST) {
			switch (commandId) {
			case ProtocolConstant.CID_BUDDY_LIST_DEPARTMENT_RESPONSE:
				IMContactManager.instance().onRepDepartment(buffer);
				return;
			case ProtocolConstant.CID_BUDDY_LIST_ALL_USER_RESPONSE:
				IMContactManager.instance().onRepAllUsers(buffer);
				return;
			case ProtocolConstant.CID_BUDDY_LIST_FRIEND_LIST:
				IMContactManager.instance().onRepRecentContacts(buffer);
				return;
			}

		} else if (serviceId == ProtocolConstant.SID_MSG) {
			switch (commandId) {
			case ProtocolConstant.CID_MSG_DATA_ACK:
				IMMessageManager.instance().onMessageAck(buffer);
				return;
			case ProtocolConstant.CID_MSG_DATA:
				IMMessageManager.instance().onRecvMessage(buffer);
				return;
			case ProtocolConstant.CID_MSG_UNREAD_CNT_RESPONSE:
				IMContactManager.instance().onRepUnreadMsgContactList(buffer);
				return;
			case ProtocolConstant.CID_MSG_UNREAD_MSG_RESPONSE:
				IMMessageManager.instance().onRepUnreadMsg(buffer);
				return;
			}

		} else if (serviceId == ProtocolConstant.SID_GROUP) {
			switch (commandId) {
			case ProtocolConstant.CID_GROUP_LIST_RESPONSE:
				IMGroupManager.instance().onRepGroupList(buffer);
				return;
			case ProtocolConstant.CID_GROUP_DIALOG_LIST_RESPONSE:
				IMGroupManager.instance().onRepGroupList(buffer);
				return;
			case ProtocolConstant.CID_GROUP_CREATE_TMP_GROUP_RESPONSE:
				IMGroupManager.instance().onRepCreateTempGroup(buffer);
				return;
			case ProtocolConstant.CID_GROUP_UNREAD_CNT_RESPONSE:
				IMGroupManager.instance().onRepUnreadMsgGroupList(buffer);
				return;
			case ProtocolConstant.CID_GROUP_UNREAD_MSG_RESPONSE:
				IMMessageManager.instance().onRepGroupUnreadMsg(buffer);
				return;
			case ProtocolConstant.CID_GROUP_CHANGE_MEMBER_RESPONSE:
				IMGroupManager.instance().onRepChangeTempGroupMembers(buffer);
				return;

			}

		}

		logger.e("packet#unhandled serviceId:%d, commandId:%d", serviceId,
				commandId);

	}

}
