package com.mogujie.tt.imlib.utils;

import java.util.List;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.utils.MessageSplitResult;

public class IMContactHelper {
	public static String getRealAvatarUrl(String avatarUrl) {
		if (avatarUrl.toLowerCase().contains("http")) {
			return avatarUrl;
		} else if (avatarUrl.trim().isEmpty()) {
			return "";
		} else {
			return SysConstant.AVATAR_URL_PREFIX + avatarUrl;
		}
	}

	// todo eric
	public static User contactEntity2User(ContactEntity contact) {
		User user = new User();
		/*
		 * 
		 * private String userId = null; private String token=""; private int
		 * onlineStatus; private String name = ""; private String nickName = "";
		 * private String avatarUrl = ""; private String title=""; private
		 * String position=""; private int roleStatus; private int sex; private
		 * String departId; private int jobNum; private String telphone; private
		 * String email;
		 */
		user.setUserId(contact.id);
		user.setName(contact.name);
		user.setNickName(contact.nickName);
		user.setAvatarUrl(contact.avatarUrl);
		user.setTitle(contact.title);
		user.setPosition(contact.position);
		user.setRoleStatus(contact.roleStatus);
		user.setSex(contact.sex);
		user.setDepartId(contact.departmentId);
		user.setJobNum(contact.jobNum);
		user.setTelphone(contact.telephone);
		user.setEmail(contact.email);

		return user;
	}

	public static RecentInfo convertContactEntity2RecentInfo(
			ContactEntity contact, int recentTalkingTime) {

		RecentInfo recentInfo = new RecentInfo();
		recentInfo.setAvatar(contact.avatarUrl);
		recentInfo.setName(contact.name);
		recentInfo.setEntityId(contact.id);
		recentInfo.setSessionType(IMSession.SESSION_P2P);
		recentInfo.setLastTime(recentTalkingTime);
		recentInfo.setNickname(contact.nickName);

		// logger.d("recent#lastTime:%d, lastTimeString:%s", recentTalkingTime,
		// recentInfo.getLastTimeString());

		return recentInfo;
	}

	public static RecentInfo convertGroupEntity2RecentInfo(GroupEntity group) {

		RecentInfo recentInfo = new RecentInfo();
		recentInfo.setAvatar(group.avatarUrl);
		recentInfo.setName(group.name);
		recentInfo.setEntityId(group.id);
		recentInfo.setSessionType(group.type);
		recentInfo.setLasttime(group.updated);
		recentInfo.setNickname(group.name);

		// logger.d("recent#lastTime:%d, lastTimeString:%s", recentTalkingTime,
		// recentInfo.getLastTimeString());

		return recentInfo;
	}

	// if windows client sends a message as "[text][picture][text]", we would
	// split them into 3 different messages, "[text]", "[picture]", "[text]"
	public static List<MessageInfo> splitMessage(MessageInfo msg) {
		MessageSplitResult msr = new MessageSplitResult(msg, msg.msgData);

		msr.decode();

		List<MessageInfo> msgInfoList = msr.getMsgList();
		for (MessageInfo msgInfo : msgInfoList) {
			msgInfo.copy(msg);
		}

		return msgInfoList;
	}
}
