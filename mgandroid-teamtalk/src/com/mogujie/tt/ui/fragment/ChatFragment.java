package com.mogujie.tt.ui.fragment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mogujie.tt.R;
import com.mogujie.tt.adapter.ChatAdapter;
import com.mogujie.tt.adapter.SearchAdapter;
import com.mogujie.tt.app.IMEntrance;
import com.mogujie.tt.biz.ContactHelper;
import com.mogujie.tt.biz.MessageNotifyCenter;
import com.mogujie.tt.biz.SearchHelper;
import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.conn.NetStateManager;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMRecentSessionManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.packet.MessageDispatchCenter;
import com.mogujie.tt.ui.activity.MainActivity;
import com.mogujie.tt.ui.activity.SearchActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.utils.CommonUtil;

/**
 * @Description 最近联系人Fragment页
 * @author Nana
 * @date 2014-7-24
 */
@SuppressLint("HandlerLeak")
public class ChatFragment extends MainFragment
		implements
			/*
			 * OnRefreshListener2<
			 * ListView>,
			 */
			OnItemSelectedListener,
			OnItemClickListener,
			OnIMServiceListner,
			OnItemLongClickListener {

	private static Handler uiHandler = null;// 处理界面消息
	private static Handler msgHandler = null;// 处理协议消息
	private ChatAdapter contactAdapter;
	private SearchAdapter searchAdapter;
	private View searchTransparentView;
	private TextView searchTipTextView;
	private ListView contactListView;
	private PullToRefreshListView searchListView;
	//	private LinearLayout tipView;
	//	private TextView tipTextView;
	private View curView = null;
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private View noNetworkView;

	public static Handler getUiHandler() {
		return uiHandler;
	}

	public static Handler getMsgHandler() {
		return msgHandler;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.d("chatfragment#onCreate");

		List<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_ADD_RECENT_CONTACT_OR_GROUP);
		actions.add(IMActions.ACTION_SERVER_DISCONNECTED);
		actions.add(IMActions.ACTION_LOGIN_RESULT);
		actions.add(IMActions.ACTION_SEARCH_DATA_READY);

		imServiceHelper.connect(getActivity(), actions, IMServiceHelper.INTENT_NO_PRIORITY, this);

		// initHandler();
		// registEvents();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		logger.d("onCreateView");

		if (null != curView) {
			logger.d("curView is not null, remove it");
			((ViewGroup) curView.getParent()).removeView(curView);
		}
		curView = inflater.inflate(R.layout.tt_fragment_chat, topContentView);

		super.init(curView);
		initTitleView();// 初始化顶部view
		initSearchBar();// 初始化顶部搜索条
		initContactListView(); // 初始化联系人列表视图
		initSearchView(); // 初始化搜索结果列表视图
		// updateAdapter(getActivity());// 加载数据
		showProgressBar();// 创建时没有数据，显示加载动画
		return curView;
	}

	@Override
	public void onStart() {
		logger.d("chatfragment#onStart");

		super.onStart();
	}

	@Override
	public void onStop() {
		logger.d("chatfragment#onStop");

		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onPause() {
		logger.d("chatfragment#onPause");

		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		logger.d("chatfragment#onResume");

		// handleTips(getActivity().getString(R.string.loading_contacts));//
		// 数据源为空时需要空列表提示
		setTileByNetState();

		// todo eric disable it first
		// requestContacts(LoginManager.getInstance().isFirstLogin());
		super.onResume();
	}

	@Override
	public void onDestroy() {
		imServiceHelper.disconnect(getActivity());
		unRegistEvents();
		super.onDestroy();
	}

	//	/**
	//	 * @Description 注册事件
	//	 */
	//	private void registEvents() {
	//		// 消息数据
	//		int msgData = ProtocolConstant.SID_MSG * 1000
	//				+ ProtocolConstant.CID_MSG_DATA;
	//		MessageDispatchCenter.getInstance().register(msgHandler, msgData);
	//		// 网络状态通知
	//		NetStateDispach.getInstance().register(getActivity().getClass(), uiHandler);
	//		// 未读消息通知
	//		MessageNotifyCenter.getInstance().register(SysConstant.EVENT_UNREAD_MSG, uiHandler, HandlerConstant.HANDLER_CONTACTS_NEW_MESSAGE_COME);
	//		MessageNotifyCenter.getInstance().register(SysConstant.EVENT_RECENT_INFO_CHANGED, uiHandler, HandlerConstant.HANDLER_CONTACTS_TO_REFRESH);
	//	}

	/**
	 * @Description 取消事件注册
	 */
	private void unRegistEvents() {
		MessageDispatchCenter.getInstance().unRegister(msgHandler);
		MessageNotifyCenter.getInstance().unregister(SysConstant.EVENT_UNREAD_MSG, getUiHandler(), HandlerConstant.HANDLER_CONTACTS_NEW_MESSAGE_COME);
		MessageNotifyCenter.getInstance().unregister(SysConstant.EVENT_RECENT_INFO_CHANGED, uiHandler, HandlerConstant.HANDLER_CONTACTS_TO_REFRESH);
		return;
	}

	/**
	 * @Description 网络断开时设置消息界面标题
	 */
	private void setTileByNetState() {
		if (!NetStateManager.getInstance().isOnline()) {
			setTopTitle(getActivity().getString(R.string.disconnected));
		}
	}

	/**
	 * @Description 设置顶部按钮
	 */
	private void initTitleView() {
		// 设置标题
		setTopTitle(getActivity().getString(R.string.chat_title));

	}

	private void hideSearchView() {
		setTopBar(R.drawable.tt_top_default_bk);
		hideTopLeftButton();
		hideTopSearchBar();
		SearchHelper.clear();
		topSearchEdt.setText("");

		// todo eric
		// setTopRightButton(R.drawable.tt_top_search);
		searchListView.setVisibility(View.GONE);
		searchTransparentView.setVisibility(View.GONE);
		CommonUtil.hideInput(getActivity());
	}

	private void clearSearchResult() {
		searchListView.setBackgroundColor(getResources().getColor(R.color.default_light_black_color));
		SearchHelper.clear();
		searchAdapter.notifyDataSetChanged();
	}

	private void doSearch(String text) {
		SearchHelper.search(text);
		if (searchAdapter.getCount() > 0) {
			searchTransparentView.setVisibility(View.GONE);
			searchListView.setVisibility(View.VISIBLE);
			searchTipTextView.setVisibility(View.GONE);
			searchAdapter.notifyDataSetChanged();
		} else {
			searchListView.setVisibility(View.GONE);
			searchTransparentView.setVisibility(View.VISIBLE);
			searchTipTextView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * @Description: 初始化搜索相关控件
	 */
	private void initSearchBar() {
		topSearchEdt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				String text = topSearchEdt.getText().toString().trim();
				if (TextUtils.isEmpty(text)) {
					clearSearchResult();
					searchTransparentView.setVisibility(View.VISIBLE);
					searchListView.setVisibility(View.GONE);
				} else {
					searchListView.setVisibility(View.VISIBLE);
					doSearch(text);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	@Override
	protected void initHandler() {
		msgHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case HandlerConstant.HANDLER_RECV_CONTACTLIST :
					case ProtocolConstant.SID_BUDDY_LIST * 1000
							+ ProtocolConstant.CID_CONTACT_RECENT_RESPONSE :
						if (isAdded()) {
							onRecieveRecentContactList(msg.obj, getActivity());
						} else {
							onRecieveRecentContactList(msg.obj, IMEntrance.getInstance().getContext());
						}
						break;
					case HandlerConstant.HANDLER_RECV_UNREAD_MSG_COUNT :
					case ProtocolConstant.SID_MSG * 1000
							+ ProtocolConstant.CID_MSG_UNREAD_CNT_RESPONSE :
						// onRecvUnreadMsgCount(msg.obj);
						break;
					case ProtocolConstant.SID_BUDDY_LIST * 1000
							+ ProtocolConstant.CID_GET_USER_INFO_RESPONSE :
						if (isAdded()) {
							onGetUserInfo(msg.obj, getActivity());
						} else {
							onGetUserInfo(msg.obj, IMEntrance.getInstance().getContext());
						}
						break;
					default :
						break;
				}
			}
		};

		uiHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case HandlerConstant.HANDLER_NET_STATE_DISCONNECTED :
						if (isAdded()) {
							setTopTitle(getActivity().getString(R.string.network_was_disconnected));
							// handleTips(getActivity().getString(R.string.network_was_disconnected));
							hideProgressBar();
						}
						break;
					case HandlerConstant.HANDLER_NET_STATE_CONNECTED :
						if (isAdded()) {
							setTopTitle(getActivity().getString(R.string.contact_name));
						}
						break;
					case HandlerConstant.HANDLER_CONTACTS_NEW_MESSAGE_COME :
						if (isAdded()) {
							updateAdapter(getActivity());
						} else {
							updateAdapter(IMEntrance.getInstance().getContext());
						}
						break;
					case HandlerConstant.HANDLER_CONTACTS_TO_REFRESH :
						contactAdapter.notifyDataSetChanged();
						break;
					case HandlerConstant.HANDLER_CONTACTS_REQUEST_TIMEOUT :
						if (isAdded()) {
							hideProgressBar();
							// handleTips(getActivity().getString(R.string.load_contacts_failed));
						}
						break;
					default :
						break;
				}
			}
		};
	}

	//	/**
	//	 * @Description 初始化提示视图
	//	 */
	//	private void initTipView() {
	//		// tipView = (LinearLayout) curView.findViewById(R.id.ContactTipsView);
	//		// tipView.setVisibility(View.INVISIBLE);
	//		// tipTextView = (TextView)
	//		// curView.findViewById(R.id.ContactTipsTextView);
	//
	//	}

	/**
	 * @Description 初始化列表视图
	 */
	// private void initContactListView() {
	// contactListView = (PullToRefreshListView) curView
	// .findViewById(R.id.ContactListView);
	//
	// Drawable loadingDrawable = getActivity().getResources().getDrawable(
	// R.drawable.pull_to_refresh_indicator);
	// final int indicatorWidth = (int) TypedValue.applyDimension(
	// TypedValue.COMPLEX_UNIT_DIP, 29, getActivity().getResources()
	// .getDisplayMetrics());
	// loadingDrawable
	// .setBounds(new Rect(0, indicatorWidth, 0, indicatorWidth));
	// contactListView.getLoadingLayoutProxy().setLoadingDrawable(
	// loadingDrawable);
	//
	// contactListView.getRefreshableView().setCacheColorHint(Color.WHITE);
	// contactListView.getRefreshableView().setSelector(
	// new ColorDrawable(Color.WHITE));
	//
	// contactListView.setOnRefreshListener(this);
	// contactListView.setOnItemClickListener(this);
	// // 绑定数据源
	// try {
	// contactAdapter = new ChatAdapter(getActivity());
	// contactAdapter.setIMService(imServiceHelper);
	// contactListView.setAdapter(contactAdapter);
	// } catch (ParseException e) {
	// logger.e(e.toString());
	// }
	// }

	private void initContactListView() {
		contactListView = (ListView) curView.findViewById(R.id.ContactListView);

		// Drawable loadingDrawable = getActivity().getResources().getDrawable(
		// R.drawable.pull_to_refresh_indicator);
		// final int indicatorWidth = (int) TypedValue.applyDimension(
		// TypedValue.COMPLEX_UNIT_DIP, 29, getActivity().getResources()
		// .getDisplayMetrics());
		// loadingDrawable
		// .setBounds(new Rect(0, indicatorWidth, 0, indicatorWidth));

		// contactListView.getLoadingLayoutProxy().setLoadingDrawable(
		// loadingDrawable);
		//
		// contactListView.getRefreshableView().setCacheColorHint(Color.WHITE);
		// contactListView.getRefreshableView().setSelector(
		// new ColorDrawable(Color.WHITE));
		//
		// contactListView.setOnRefreshListener(this);
		contactListView.setOnItemClickListener(this);
		contactListView.setOnItemLongClickListener(this);
		// 绑定数据源
		try {
			contactAdapter = new ChatAdapter(getActivity());
			contactListView.setAdapter(contactAdapter);
		} catch (ParseException e) {
			logger.e(e.toString());
		}
	}

	private void initSearchView() {
		searchListView = (PullToRefreshListView) curView.findViewById(R.id.SearchListView);
		searchListView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				topSearchEdt.setVisibility(View.GONE);
				searchListView.setVisibility(View.GONE);
			}
		});

		searchListView.setOnItemClickListener(this);

		searchTransparentView = (View) curView.findViewById(R.id.search_transparent_view);
		searchTipTextView = (TextView) curView.findViewById(R.id.search_tip_txt);
		noNetworkView = curView.findViewById(R.id.layout_no_network);

		try {
			searchAdapter = new SearchAdapter(getActivity());
			searchListView.setAdapter(searchAdapter);
			searchListView.setVisibility(View.GONE);
		} catch (ParseException e) {
			logger.e(e.toString());
		}
	}

	// @Override
	// public void onPullDownToRefresh(
	// final PullToRefreshBase<ListView> refreshView) {
	// refreshView.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// ContactHelper.reqRecentContact(msgHandler, uiHandler);
	// refreshView.onRefreshComplete();
	// }
	// }, 200);
	// }
	//
	// @Override
	// public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
	//
	// }
	//
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		RecentInfo recentInfo = contactAdapter.getItem(position);
		if (recentInfo == null) {
			logger.e("recent#null recentInfo -> position:%d", position);
			return;
		}

		logger.d("xrecent#onItemClick recentInfo:%s", recentInfo);

		// handleUnreadMsgs(recentInfo.getEntityId(),
		// recentInfo.getSessionType());

		IMUIHelper.openSessionChatActivity(logger, getActivity(), recentInfo.getEntityId(), recentInfo.getSessionType(), imServiceHelper.getIMService());

		// if (position < 1)
		// return;
		// position -= 1;
		// User targetUser = contactAdapter.pullUser(position);
		// if (null != targetUser) {
		// // 重置联系人消息状态
		// int readCount = ContactHelper
		// .resetContactMessagetState(ContactHelper.getRecentInfoByPosition(position));
		// // 设置新的聊天对象
		// CacheHub.getInstance().setChatUser(targetUser);
		// // 切换到消息界面
		// Intent i = new Intent(getActivity(), MessageActivity.class);
		// Bundle bundle = new Bundle();
		// bundle.putInt(SysConstant.READCOUNT, readCount);
		//
		// bundle.putInt(SysConstant.SESSION_TYPE_KEY, IMSession.SESSION_P2P);
		// bundle.putString(SysConstant.SESSION_ID_KEY, targetUser.getUserId());
		//
		// i.putExtras(bundle);
		// getActivity().startActivity(i);
		// }
	}

	//	/**
	//	 * @Description 本地没有数据时进行请求
	//	 * @param needRequest
	//	 */
	//	private void requestContacts(boolean needRequest) {
	//		if (needRequest || contactAdapter.getCount() < 1) {
	//			// ContactHelper.reqRecentContact(msgHandler, uiHandler);
	//		}
	//	}

	// private void hideTips() {
	// tipTextView.setText("");
	// tipView.setVisibility(View.INVISIBLE);
	// }

	//	private void showTips(String tips) {
	//		tipTextView.setText(tips);
	//		tipView.setVisibility(View.VISIBLE);
	//	}

	// /**
	// * @Description 显示提示信息 1.没有最近联系人使用：getString(R.string.no_recent_contact)
	// * 2.网络断开连接使用：getString(R.string.network_was_disconnected)
	// */
	// private void handleTips(String msg) {
	// if (0 < contactAdapter.getCount()) {
	// hideTips();
	// return;
	// }
	// showTips(msg);
	// }

	/**
	 * @Description 收到从服务端返回的联系人数据时处理
	 * @param msg
	 */
	private void onRecieveRecentContactList(Object msg, Context context) {
		hideProgressBar();
		// handleResponseData(msg, context);
		updateAdapter(context);
		if (isAdded() && null != msg) {
			// handleTips(getActivity().getString(R.string.no_recent_contact));
		}
	}

	// /**
	// * @Description 处理服务端返回的最近联系人列表信息 1.修改缓存用户信息
	// * 2.拉取用户的详细信息(目前只有online信息,暂时不用拉取) 3.拉取未读消息
	// * @param msg
	// */
	// private void handleResponseData(Object msg, Context context) {
	// if (null == msg) {
	// return;
	// }
	// RecentContactPacket packet = (RecentContactPacket) msg;
	// List<RecentInfo> userList = ((RecentContactResponse) (packet
	// .getResponse())).getRecentInfos();
	// if (null == userList || userList.size() <= 0)
	// return;
	// Queue<String> idList = ((RecentContactResponse) (packet.getResponse()))
	// .getRecentIdList();
	// CacheHub.getInstance().addFriendList(idList);
	// // 1.拉取用户的详细信息
	// ContactHelper.requestUserInfo(idList, msgHandler);
	// // 2.拉取未读消息
	// Packet _packet = PacketDistinguisher.make(ProtocolConstant.SID_MSG,
	// ProtocolConstant.CID_MSG_UNREAD_CNT_REQUEST, null, true);
	// ActionCallback _callback = new ActionCallback() {
	//
	// @Override
	// public void onSuccess(Packet packet) {
	// ContactHelper.sendMessageToMsgHandler(packet, msgHandler);
	// }
	//
	// @Override
	// public void onTimeout(Packet packet) {
	// }
	//
	// @Override
	// public void onFaild(Packet packet) {
	//
	// }
	// };
	// PushActionToQueueTask task = new PushActionToQueueTask(_packet,
	// _callback);
	// TaskManager.getInstance().trigger(task);
	// }

	/**
	 * @Description 更新适配器 1. 准备数据列表 2.通知界面更新
	 */
	private void updateAdapter(Context context) {
		ContactHelper.loadRecentInfoList(msgHandler, context);
		contactAdapter.notifyDataSetChanged();
	}

	// /**
	// * @Description 收到未读消息时处理
	// * @param msg
	// */
	// private void onRecvUnreadMsgCount(Object msg) {
	// if (msg == null)
	// return;
	//
	// UnReadMsgCountPacket _packet = (UnReadMsgCountPacket) msg;
	// List<UnReadMsgCountInfo> unreadMsgCountList = ((UnReadMsgCountResponse)
	// (_packet
	// .getResponse())).getUnReadMsgCountList();
	//
	// for (int i = 0; i < unreadMsgCountList.size(); i++) {
	// UnReadMsgCountInfo info = unreadMsgCountList.get(i);
	//
	// Object[] obj = new Object[1];
	// obj[0] = info.getFromUserId();
	// if (obj[0] == null)
	// continue;
	// Packet packet = PacketDistinguisher.make(ProtocolConstant.SID_MSG,
	// ProtocolConstant.CID_MSG_UNREAD_MSG_REUQEST, obj, true);
	//
	// ActionCallback _callback = new ActionCallback() {
	//
	// @Override
	// public void onSuccess(Packet packet) {
	// ContactHelper.sendMessageToMsgHandler(packet, msgHandler);
	// }
	//
	// @Override
	// public void onTimeout(Packet packet) {
	// if (packet == null)
	// return;
	// }
	//
	// @Override
	// public void onFaild(Packet packet) {
	// onTimeout(packet);
	// }
	// };
	// PushActionToQueueTask task = new PushActionToQueueTask(packet,
	// _callback);
	// TaskManager.getInstance().trigger(task);
	// }
	// }

	/**
	 * @Description 获取到用户信息后处理:更新缓存，修改界面联系人信息
	 * @param obj
	 */
	private void onGetUserInfo(Object obj, Context context) {
		if (null == obj) {
			return;
		}

		// Queue<User> userList = ((QueryUsersInfoResponse) (((Packet) obj)
		// .getResponse())).getUserList();
		// while (userList.size() > 0) {
		// User user = userList.poll();
		// if (user == null) {
		// continue;
		// }
		// CacheHub.getInstance().setUser(user, context);
		// }
		// ContactHelper.refreshUserInfo(msgHandler, getActivity());
		// // 通知联系人数据适配器数据已经更新
		// // contactAdapter.notifyDataSetChanged();
		// updateAdapter(context);
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub
		logger.d("chatfragment#recent#onActions -> action:%s", action);

		if (action.equals(IMActions.ACTION_ADD_RECENT_CONTACT_OR_GROUP)) {
			// hideTips();
			onRecentContactDataReady();
		} else if (action.equals(IMActions.ACTION_SERVER_DISCONNECTED)) {
			handleServerDisconnected();
		} else if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
			handleOnLoginResult(intent);
		}
		
		tryHandleSearchAction(action);
	}

	private void handleOnLoginResult(Intent intent) {
		logger.d("chatfragment#handleOnLoginResult");

		int errorCode = intent.getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

		if (errorCode == ErrorCode.S_OK) {
			logger.d("chatfragment#loginOk");

			noNetworkView.setVisibility(View.GONE);
		}
	}
	private void handleServerDisconnected() {
		logger.d("chatfragment#handleServerDisconnected");
		noNetworkView.setVisibility(View.VISIBLE);
		Toast.makeText(getActivity(), getString(R.string.no_network_notification), Toast.LENGTH_SHORT).show();
	}

	private void onRecentContactDataReady() {
		IMRecentSessionManager recentSessionManager = imServiceHelper.getIMService().getRecentSessionManager();

		int totalUnreadMsgCnt = recentSessionManager.getTotalUnreadMsgCnt();
		logger.d("unread#total cnt %d", totalUnreadMsgCnt);
		((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);

		List<RecentInfo> recentSessionList = recentSessionManager.getRecentSessionList();
		contactAdapter.setData(recentSessionList);

		hideProgressBar();
	}
	

	@Override
	public void onIMServiceConnected() {
		// TODO Auto-generated method stub
		logger.d("chatfragment#recent#onIMServiceConnected");

		IMService imService = imServiceHelper.getIMService();
		if (imService == null) {
			return;
		}
		if (imService.getContactManager().recentContactsDataReady()) {
			onRecentContactDataReady();
		}
		
		tryInitSearch(imService);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		RecentInfo recentInfo = contactAdapter.getItem(position);
		if (recentInfo == null) {
			logger.e("recent#onItemLongClick null recentInfo -> position:%d", position);
			return false;
		}

		if (recentInfo.getSessionType() != IMSession.SESSION_P2P) {
			return false;
		}

		IMService imService = imServiceHelper.getIMService();
		if (imService == null) {
			return false;
		}

		ContactEntity contact = imService.getContactManager().findContact(recentInfo.getEntityId());
		if (contact == null) {
			return false;
		}

		IMUIHelper.handleContactItemLongClick(getActivity(), contact);

		return true;
	}

}
