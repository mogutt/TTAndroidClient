package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.CreateTempGroupPacket;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.proto.GroupPacket;
import com.mogujie.tt.imlib.proto.GroupUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.UnreadMsgGroupListPacket;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.utils.pinyin.PinYin;

public class IMGroupManager extends IMManager {
	private static IMGroupManager inst;

	public static IMGroupManager instance() {
		synchronized (IMGroupManager.class) {
			if (inst == null) {
				inst = new IMGroupManager();
			}

			return inst;
		}

	}

	private Logger logger = Logger.getLogger(IMGroupManager.class);
	private Map<String, GroupEntity> groups = new ConcurrentHashMap<String, GroupEntity>();
	private boolean groupReady = false;
	private boolean tempGroupReady = false;
	private boolean unreadMsgGroupListReady = false;
	private List<UnreadMsgGroupListPacket.PacketResponse.Entity> unreadMsgGroupList;

	public Map<String, GroupEntity> getGroups() {
		return groups;
	}

	public List<GroupEntity> getNormalGroupList() {
		List<GroupEntity> normalGroupList = new ArrayList<GroupEntity>();

		for (Entry<String, GroupEntity> entry : groups.entrySet()) {
			GroupEntity group = entry.getValue();
			if (group == null) {
				continue;
			}

			if (group.type == IMSession.SESSION_GROUP) {
				normalGroupList.add(group);
			}
		}

		return normalGroupList;
	}

	public void setGroups(Map<String, GroupEntity> groups) {
		this.groups = groups;
	}

	public GroupEntity findGroup(String groupId) {
		logger.d("group#findGroup groupId:%s", groupId);

		return groups.get(groupId);
	}

	public List<ContactEntity> getGroupMembers(String groupId) {
		logger.d("group#getGroupMembers groupId:%s", groupId);

		GroupEntity group = findGroup(groupId);
		if (group == null) {
			logger.e("group#no such group id:%s", groupId);
			return null;
		}

		ArrayList<ContactEntity> memberList = new ArrayList<ContactEntity>();
		for (String id : group.memberIdList) {
			ContactEntity contact = IMContactManager.instance().findContact(id);
			if (contact == null) {
				logger.e("group#no such contact id:%s", id);
				continue;
			}

			memberList.add(contact);
		}

		return memberList;
	}

	public void fetchGroupList() {
		logger.d("group#fetchGroupList");

		reqGetGroupList();
		reqGetTempGroupList();
		reqUnreadMsgGroupList();

	}

	private void reqGetGroupList() {
		logger.i("group#reqGetGroupList");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new GroupPacket(IMSession.SESSION_GROUP));

		logger.i("group#send packet to server");

	}
	
	public void reqGetTempGroupList() {
		logger.i("group#reqGetTempGroupList");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new GroupPacket(IMSession.SESSION_TEMP_GROUP));

		logger.i("group#send packet to server");

	}

	public boolean groupReadyConditionOk() {
		return groupReady && tempGroupReady;
	}

	private boolean unreadMsgGroupListReadyConditionOk() {
		return groupReadyConditionOk() && unreadMsgGroupListReady;
	}

	public void onRepGroupList(DataBuffer buffer) {
		logger.i("group#onRepGroupList");

		GroupPacket packet = new GroupPacket();
		packet.decode(buffer);

		GroupPacket.PacketResponse resp = (GroupPacket.PacketResponse) packet.getResponse();
		logger.i("group#group cnt:%d", resp.entityList.size());

		for (GroupEntity group : resp.entityList) {
			logger.i("group# -> entity:%s", group);

			group.pinyin = PinYin.getPinYin(group.name);
			groups.put(group.id, group);
		}

		Header header = packet.getResponse().getHeader();
		int commandId = header.getCommandId();
		if (commandId == ProtocolConstant.CID_GROUP_DIALOG_LIST_RESPONSE) {
			logger.d("group#tempgroup list is ready");
			tempGroupReady = true;
		} else if (commandId == ProtocolConstant.CID_GROUP_LIST_RESPONSE) {
			logger.d("group#group list is ready");
			groupReady = true;
		}

		if (groupReadyConditionOk()) {
			ctx.sendBroadcast(new Intent(IMActions.ACTION_GROUP_READY));
			logger.d("group#broadcast group ready msg");
		}

		triggerAddRecentInfo();
		triggerReqUnreadMsgs();
	}

	private void triggerAddRecentInfo() {
		if (groupReadyConditionOk()) {
			for (Entry<String, GroupEntity> entry : groups.entrySet()) {

				GroupEntity group = entry.getValue();
				if (group == null) {
					continue;
				}

				RecentInfo recentSession = IMContactHelper.convertGroupEntity2RecentInfo(group);
				IMRecentSessionManager.instance().addRecentSession(recentSession);
			}

			IMRecentSessionManager.instance().broadcast();
		}
	}

	public void adjustDialogMembers(List<String> addingMemberList,
			List<String> removingMemberList) {
		logger.d("adjust#adjustDialogMembers, adding size:%d, removing size:%d", addingMemberList.size(), removingMemberList.size());

		if (addingMemberList.isEmpty() && removingMemberList.isEmpty()) {
			logger.d("tempgroup#no need to adjust");
			return;
		}

	}

	public void reqCreateTempGroup(String tempGroupName, List<String> memberList) {
		logger.d("tempgroup#reqCreateTempGroup, name:%s, member cnt:%d", tempGroupName, memberList.size());

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("tempgroup#channel is null");
			return;
		}

		String dummyTempGroupAvatarUrl = "";
		channel.sendPacket(new CreateTempGroupPacket(tempGroupName, dummyTempGroupAvatarUrl, memberList));

		logger.i("tempgroup#send packet to server");

	}

	public void onRepCreateTempGroup(DataBuffer buffer) {
		logger.d("tempgroup#onRepCreateTempGroup");

		CreateTempGroupPacket packet = new CreateTempGroupPacket();
		packet.decode(buffer);

		CreateTempGroupPacket.PacketResponse resp = (CreateTempGroupPacket.PacketResponse) packet.getResponse();

		Intent intent = new Intent(IMActions.ACTION_GROUP_CREATE_TEMP_GROUP_RESULT);
		intent.putExtra(SysConstant.OPERATION_RESULT_KEY, resp.result);
		if (resp.result != 0) {
			logger.e("tempgroup#createTempGroup failed");
		} else {
			GroupEntity group = resp.entity;
//			group.pinyin = PinYin.getPinYin(group.name);

			logger.i("tempgroup# -> new temp group:%s", group);

			intent.putExtra(SysConstant.SESSION_ID_KEY, group.id);
			
			//todo eric, the return value has bug, updated time is not right, and the member cnt is also not right
			reqGetTempGroupList();
		}

		ctx.sendBroadcast(intent);

	}


	private void reqUnreadMsgGroupList() {
		logger.i("unread#reqUnreadMsgGroupList");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("unread#channel is null");
			return;
		}

		channel.sendPacket(new UnreadMsgGroupListPacket());

		logger.i("unread#send packet to server");
	}

	public void onRepUnreadMsgGroupList(DataBuffer buffer) {
		logger.i("unread#onRepUnreadMsgGroupList");

		UnreadMsgGroupListPacket packet = new UnreadMsgGroupListPacket();
		packet.decode(buffer);

		UnreadMsgGroupListPacket.PacketResponse resp = (UnreadMsgGroupListPacket.PacketResponse) packet.getResponse();
		logger.i("unread#unreadMsgGroupList cnt:%d", resp.entityList.size());
		unreadMsgGroupList = resp.entityList;

		unreadMsgGroupListReady = true;

		triggerReqUnreadMsgs();
	}

	private void triggerReqUnreadMsgs() {
		logger.d("unread#group triggerReqUnreadMsgs");

		if (unreadMsgGroupListReadyConditionOk()) {
			reqUnreadMgs();
		} else {
			logger.d("unread#condition is not ok");
		}

	}

	private void reqUnreadMgs() {
		logger.i("unread#group reqUnreadMsgs");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("unread#channel is null");
			return;
		}

		for (UnreadMsgGroupListPacket.PacketResponse.Entity entity : unreadMsgGroupList) {
			logger.d("unread#sending unreadmsg request -> groupId:%s", entity.groupId);

			GroupUnreadMsgPacket.PacketRequest.Entity requestParam = new GroupUnreadMsgPacket.PacketRequest.Entity();
			requestParam.groupId = entity.groupId;
			channel.sendPacket(new GroupUnreadMsgPacket(requestParam));

			logger.i("unread#send packet to server");
		}

	}
}
