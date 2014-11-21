package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.ChangeTempGroupMemberPacket;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.CreateTempGroupPacket;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.proto.GroupPacket;
import com.mogujie.tt.imlib.proto.GroupUnreadMsgPacket;
import com.mogujie.tt.imlib.proto.UnreadMsgGroupListPacket;
import com.mogujie.tt.imlib.utils.DumpUtils;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.utils.pinyin.PinYin;

public class IMGroupManager extends IMManager implements OnIMServiceListner {
	public static final int ADD_CHANGE_MEMBER_TYPE = 0;
	public static final int REMOVE_CHANGE_MEMBER_TYPE = 1;

	public static String getChangeMemberTypeString(int changeType) {
		if (changeType == ADD_CHANGE_MEMBER_TYPE) {
			return "tempgroup#adding members";
		} else if (changeType == REMOVE_CHANGE_MEMBER_TYPE) {
			return "tempgroup#removing members";
		} else {
			return "tempgroup#no such type";
		}
	}

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
	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	public void register() {
		logger.d("reconnect#regisgter");

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_LOGIN_RESULT);

		imServiceHelper.registerActions(ctx, actions,
				IMServiceHelper.INTENT_NO_PRIORITY, this);
	}

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

	private void addGroup(GroupEntity group) {
		logger.i("group#addGroup -> entity:%s", group);

		PinYin.getPinYin(logger, group.name, group.pinyinElement);
		groups.put(group.id, group);
	}

	public void onRepGroupList(DataBuffer buffer) {
		logger.i("group#onRepGroupList");

		GroupPacket packet = new GroupPacket();
		packet.decode(buffer);

		GroupPacket.PacketResponse resp = (GroupPacket.PacketResponse) packet
				.getResponse();
		logger.i("group#group cnt:%d", resp.entityList.size());

		for (GroupEntity group : resp.entityList) {
			addGroup(group);
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

			IMUIHelper.triggerSearchDataReady(logger, ctx,
					IMContactManager.instance(), this);
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

				logger.d("group#recent#group:%s", group);

				RecentInfo recentSession = IMContactHelper
						.convertGroupEntity2RecentInfo(group);
				IMRecentSessionManager.instance().addRecentSession(
						recentSession);
			}

			IMRecentSessionManager.instance().broadcast();
		}
	}

	public void changeTempGroupMembers(String groupId,
			List<String> addingMemberList, List<String> removingMemberList) {

		logger.d("changeTempGroupMembers gropuId:%s", groupId);

		DumpUtils.dumpStringList(logger, "tempgroup#adding list",
				addingMemberList);
		DumpUtils.dumpStringList(logger, "tempgroup#removing list",
				removingMemberList);

		changeTempGroupMembersImpl(groupId, ADD_CHANGE_MEMBER_TYPE,
				addingMemberList);
		changeTempGroupMembersImpl(groupId, REMOVE_CHANGE_MEMBER_TYPE,
				removingMemberList);
	}

	public void changeTempGroupMembersImpl(String groupId, int changeType,
			List<String> memberList) {
		logger.d("tempgroup#changeGroupMembers gropuId:%s, changeType:%d",
				groupId, changeType);

		if (memberList.isEmpty()) {
			logger.d("tempgroup#empty, no need to change");
			return;
		}

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("tempgroup#channel is null");
			return;
		}

		ChangeTempGroupMemberPacket.PacketRequest.Entity param = new ChangeTempGroupMemberPacket.PacketRequest.Entity();

		param.groupId = groupId;
		param.changeType = changeType;
		param.memberList = memberList;

		channel.sendPacket(new ChangeTempGroupMemberPacket(param));

	}

	public void onRepChangeTempGroupMembers(DataBuffer buffer) {
		logger.d("tempgroup#onRepchangeTempGroupMembers");

		ChangeTempGroupMemberPacket packet = new ChangeTempGroupMemberPacket();
		packet.decode(buffer);

		ChangeTempGroupMemberPacket.PacketResponse resp = (ChangeTempGroupMemberPacket.PacketResponse) packet
				.getResponse();

		ChangeTempGroupMemberPacket.PacketResponse.Entity entity = resp.entity;

		logger.d("tempgroup#groupId:%s", entity.groupId);
		boolean ok = handleChangeTempGroupMember(entity, entity.groupId);
		logger.d("tempgroup#result ok:%s", ok);

		Intent intent = new Intent(
				IMActions.ACTION_GROUP_CHANGE_TEMP_GROUP_MEMBER_RESULT);
		intent.putExtra(SysConstant.OPERATION_RESULT_KEY, ok);
		intent.putExtra(SysConstant.KEY_SESSION_ID, entity.groupId);

		triggerAddRecentInfo();

		ctx.sendBroadcast(intent);

	}

	private boolean handleChangeTempGroupMember(
			ChangeTempGroupMemberPacket.PacketResponse.Entity entity,
			String groupId) {

		if (entity.result != 0) {
			logger.e("tempgroup#onRepChangeTempGroupMembers failed, result:%d",
					entity.result);
			return false;
		}

		GroupEntity group = findGroup(groupId);
		if (group == null) {
			logger.e("tempgroup#no such group:%s", groupId);
			return false;
		}

		if (entity.memberList == null || entity.memberList.isEmpty()) {
			logger.e("tempgroup#memberList are empty");
			return false;
		}

		DumpUtils
				.dumpStringList(logger,
						getChangeMemberTypeString(entity.changeType),
						entity.memberList);

		if (entity.changeType == ADD_CHANGE_MEMBER_TYPE) {
			group.memberIdList.addAll(entity.memberList);
		} else if (entity.changeType == REMOVE_CHANGE_MEMBER_TYPE) {
			group.memberIdList.removeAll(entity.memberList);
		}

		return true;
	}

	public void reqCreateTempGroup(String tempGroupName, List<String> memberList) {
		logger.d("tempgroup#reqCreateTempGroup, name:%s, member cnt:%d",
				tempGroupName, memberList.size());

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("tempgroup#channel is null");
			return;
		}

		String dummyTempGroupAvatarUrl = "";
		channel.sendPacket(new CreateTempGroupPacket(tempGroupName,
				dummyTempGroupAvatarUrl, memberList));

		logger.i("tempgroup#send packet to server");

	}

	public void onRepCreateTempGroup(DataBuffer buffer) {
		logger.d("tempgroup#onRepCreateTempGroup");

		CreateTempGroupPacket packet = new CreateTempGroupPacket();
		packet.decode(buffer);

		CreateTempGroupPacket.PacketResponse resp = (CreateTempGroupPacket.PacketResponse) packet
				.getResponse();

		Intent intent = new Intent(
				IMActions.ACTION_GROUP_CREATE_TEMP_GROUP_RESULT);
		intent.putExtra(SysConstant.OPERATION_RESULT_KEY, resp.result);
		if (resp.result != 0) {
			logger.e("tempgroup#createTempGroup failed");
		} else {
			GroupEntity group = resp.entity;
			addGroup(group);

			intent.putExtra(SysConstant.KEY_SESSION_ID, group.id);

			triggerAddRecentInfo();

			// todo eric, the return value has bug, updated time is not right,
			// and the member cnt is also not right
			// reqGetTempGroupList();
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

		UnreadMsgGroupListPacket.PacketResponse resp = (UnreadMsgGroupListPacket.PacketResponse) packet
				.getResponse();
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
			logger.d("unread#sending unreadmsg request -> groupId:%s",
					entity.groupId);

			GroupUnreadMsgPacket.PacketRequest.Entity requestParam = new GroupUnreadMsgPacket.PacketRequest.Entity();
			requestParam.groupId = entity.groupId;
			channel.sendPacket(new GroupUnreadMsgPacket(requestParam));

			logger.i("unread#send packet to server");
		}

	}

	@Override
	public void reset() {
		groups.clear();
		groupReady = false;
		tempGroupReady = false;
		unreadMsgGroupListReady = false;
		unreadMsgGroupList = null;
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
			handleLoginResultAction(intent);

		}

	}

	private void handleLoginResultAction(Intent intent) {
		logger.d("contact#handleLoginResultAction");
		int errorCode = intent
				.getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

		if (errorCode == ErrorCode.S_OK) {
			onLoginSuccess();
		}
	}

	private void onLoginSuccess() {
		logger.d("contact#onLogin Successful");
		fetchGroupList();
	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub

	}
}
