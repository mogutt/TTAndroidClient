package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.AllContactsPacket;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.proto.DepartmentPacket;
import com.mogujie.tt.imlib.proto.RecentContactsPacket;
import com.mogujie.tt.imlib.proto.UnreadMsgContactListPacket;
import com.mogujie.tt.imlib.proto.UnreadMsgPacket;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.utils.pinyin.PinYin;

public class IMContactManager extends IMManager implements OnIMServiceListner {

	private static IMContactManager inst;
	private Logger logger = Logger.getLogger(IMContactManager.class);
	private boolean departmentDataReady, allContactsDataReady,
			recentContactsDataReady, unreadMsgContactListReady;
	private ContactEntity loginContact;
	private List<RecentContactsPacket.UserEntity> recentContactList;
	private List<String> unreadMsgContactList;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();

	// key = id
	private Map<String, DepartmentEntity> departments = new ConcurrentHashMap<String, DepartmentEntity>();
	private Map<String, ContactEntity> contacts = new ConcurrentHashMap<String, ContactEntity>();

	public static IMContactManager instance() {
		synchronized (IMContactManager.class) {
			if (inst == null) {
				inst = new IMContactManager();
			}

			return inst;
		}
	}

	private IMContactManager() {

	}

	public void register() {
		logger.d("reconnect#regisgter");

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_LOGIN_RESULT);

		imServiceHelper.registerActions(ctx, actions,
				IMServiceHelper.INTENT_NO_PRIORITY, this);
	}

	public ContactEntity getLoginContact() {
		return loginContact;
	}

	public void setLoginContact(ContactEntity loginContact) {
		this.loginContact = loginContact;
	}

	public void fetchContacts() {
		logger.d("contact#fetchContacts");

		if (triggerContactsDataReady()) {
			logger.d("contact#contacts are already ready, notify it");
		} else {
			reqGetDepartments();
			reqGetAllContacts();
			reqGetRecentContacts();
			reqUnreadMsgContactList();
		}
	}

	public Map<String, DepartmentEntity> getDepartments() {
		return departments;
	}

	public Map<String, ContactEntity> getContacts() {
		return contacts;
	}

	public ContactEntity findContact(String contactId) {
		if (contactId == null) {
			logger.e("contact#findContact contactId is null");
			return null;
		}
		
		return contacts.get(contactId);
	}

	public DepartmentEntity findDepartment(String departmentId) {
		return departments.get(departmentId);
	}

	public boolean ContactsDataReady() {
		return departmentDataReady && allContactsDataReady;
	}

	public boolean isContactProfileReady(String contactId) {
		ContactEntity contact = findContact(contactId);
		if (contact == null) {
			return false;
		}

		return contact.isProfileReady();
	}

	private void reqGetDepartments() {
		logger.i("contact#reqGetDepartments");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new DepartmentPacket());
	}

	private void reqGetAllContacts() {
		logger.i("contact#reqGetAllContacts");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new AllContactsPacket());
	}

	private boolean triggerContactsDataReady() {
		logger.d("contact#triggerContactsDataReady");
		if (ContactsDataReady()) {
			logger.i("contact#contacts are ready, broadcast");

			if (ctx != null) {
				logger.i("contact#start boradcas t contact_ready action");
				ctx.sendBroadcast(new Intent(IMActions.ACTION_CONTACT_READY));
				return true;
			} else {
				logger.e("contact#ctx is null");
			}

		}

		logger.i("contact#didn't broadcast anything because contacts data are not ready");

		return false;
	}

	public void onRepDepartment(DataBuffer buffer) {
		logger.i("contact#onRepDepartment");

		DepartmentPacket packet = new DepartmentPacket();
		packet.decode(buffer);

		DepartmentPacket.PacketResponse resp = (DepartmentPacket.PacketResponse) packet
				.getResponse();
		logger.i("contact#department cnt:%d", resp.entityList.size());

		for (DepartmentEntity department : resp.entityList) {
			logger.i("department -> entity:%s", department);
			PinYin.getPinYin(logger, department.title, department.pinyinElement);

			departments.put(department.id, department);
		}

		departmentDataReady = true;

		triggerContactsDataReady();
		triggerSearchDataReady();
	}

	private void triggerSearchDataReady() {
		IMUIHelper.triggerSearchDataReady(logger, ctx, this,
				IMGroupManager.instance());
	}

	private void fillDepartment(ContactEntity contact) {
		/*
		 * in inner version, there's no department protocol, so we're using
		 * department name inside ContactEntity to fill in everything
		 * contact.departmentId actually is department name
		 */

		if (contact == null) {
			return;
		}

		if (departments.containsKey(contact.departmentId)) {
			return;
		}

		DepartmentEntity department = new DepartmentEntity();
		department.id = contact.departmentId;
		department.title = contact.departmentId;
		department.description = "";
		department.parentId = "";
		department.leaderId = "";
		department.status = 0;

		PinYin.getPinYin(logger, department.title, department.pinyinElement);

		departments.put(department.id, department);

	}

	public void onRepAllUsers(DataBuffer buffer) {
		logger.i("contact#onRepAllUsers");

		AllContactsPacket packet = new AllContactsPacket();
		packet.decode(buffer);

		AllContactsPacket.PacketResponse resp = (AllContactsPacket.PacketResponse) packet
				.getResponse();
		logger.i("contact#user cnt:%d", resp.entityList.size());

		String loginId = IMLoginManager.instance().getLoginId();
		for (ContactEntity contact : resp.entityList) {
			PinYin.getPinYin(logger, contact.name, contact.pinyinElement);

			// logger.i("user -> entity:%s", contact);

			contacts.put(contact.id, contact);

			if (contact.id.equals(loginId)) {
				logger.i("contact#find login contact");
				loginContact = contact;
			}

			fillDepartment(contact);
		}

		departmentDataReady = true;

		allContactsDataReady = true;

		triggerContactsDataReady();
		triggerSearchDataReady();
		triggerAddRecentContacts();

		triggerReqUnreadMsgs();

	}

	private void reqGetRecentContacts() {
		logger.i("contact#contact#reqGetRecentContacts");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#contact#channel is null");
			return;
		}

		channel.sendPacket(new RecentContactsPacket());
	}

	public boolean recentContactsDataReady() {
		return allContactsDataReady && recentContactsDataReady;
	}

	public void triggerAddRecentContacts() {
		logger.d("contact#triggerAddRecentContacts");

		if (recentContactsDataReady()) {
			logger.d("contact#condition is ready");
			for (RecentContactsPacket.UserEntity recentContact : recentContactList) {
				ContactEntity contact = findContact(recentContact.id);
				if (contact == null) {
					logger.e("contact#no such contact by id:%s",
							recentContact.id);
					continue;
				}

				logger.d("contact#contact:%s", contact.name);
				RecentInfo recentSession = IMContactHelper
						.convertContactEntity2RecentInfo(contact,
								recentContact.userUpdated);
				IMRecentSessionManager.instance().addRecentSession(
						recentSession);
			}

			IMRecentSessionManager.instance().broadcast();
		} else {
			logger.d("contact#condition is not ready");
		}
	}

	public void onRepRecentContacts(DataBuffer buffer) {
		logger.i("contact#contact#onRepRecentContacts");

		RecentContactsPacket packet = new RecentContactsPacket();
		packet.decode(buffer);

		RecentContactsPacket.PacketResponse resp = (RecentContactsPacket.PacketResponse) packet
				.getResponse();
		logger.i("contact#contact#user cnt:%d", resp.entityList.size());

		// for (RecentContactsPacket.UserEntity entity : resp.entityList) {
		// logger.i("contact#contact#recent contact:%s", entity);
		// }

		recentContactList = resp.entityList;

		recentContactsDataReady = true;

		triggerAddRecentContacts();
	}

	private void reqUnreadMsgContactList() {
		logger.i("unread#1reqUnreadMsgContactList");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("unread#channel is null");
			return;
		}

		channel.sendPacket(new UnreadMsgContactListPacket());
	}

	public void onRepUnreadMsgContactList(DataBuffer buffer) {
		logger.i("unread#2onRepUnreadMsgContactList");

		UnreadMsgContactListPacket packet = new UnreadMsgContactListPacket();
		packet.decode(buffer);

		UnreadMsgContactListPacket.PacketResponse resp = (UnreadMsgContactListPacket.PacketResponse) packet
				.getResponse();
		logger.i("unread#unreadMsgContactList cnt:%d", resp.entityList.size());
		unreadMsgContactList = resp.entityList;

		unreadMsgContactListReady = true;

		triggerReqUnreadMsgs();
	}

	private boolean conditionReqUnreadMsgsOK() {
		return unreadMsgContactListReady && allContactsDataReady;
	}

	private void triggerReqUnreadMsgs() {
		logger.d("unread#triggerReqUnreadMsgs");

		if (conditionReqUnreadMsgsOK()) {
			reqUnreadMgs();
		} else {
			logger.d("unread#condition is not ok");
		}
	}

	private void reqUnreadMgs() {
		logger.i("unread#3reqUnreadMsgs");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("unread#channel is null");
			return;
		}

		for (String contactId : unreadMsgContactList) {
			logger.d("unread#sending unreadmsg request -> contactId:%s",
					contactId);
			channel.sendPacket(new UnreadMsgPacket(contactId));
		}

	}

	@Override
	public void reset() {
		departmentDataReady = false;
		allContactsDataReady = false;
		recentContactsDataReady = false;
		unreadMsgContactListReady = false;
		loginContact = null;
		recentContactList = null;
		unreadMsgContactList = null;

		departments.clear();
		contacts.clear();

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
		fetchContacts();
	}

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub

	}
}
