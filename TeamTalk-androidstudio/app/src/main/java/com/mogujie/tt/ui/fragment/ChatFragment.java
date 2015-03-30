
package com.mogujie.tt.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.ChatAdapter;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMRecentSessionManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.ui.activity.MainActivity;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * @Description 最近联系人Fragment页
 * @author Nana
 * @date 2014-7-24
 */
public class ChatFragment extends MainFragment
        implements
        OnItemSelectedListener,
        OnItemClickListener,
        OnIMServiceListner,
        OnItemLongClickListener {

    private IMServiceHelper imServiceHelper = new IMServiceHelper();
    private ChatAdapter contactAdapter;
    private ListView contactListView;
    private View curView = null;
    private View noNetworkView;
    private ProgressBar reconnectingProgressBar;
    private int firstUnreadPosition = 0;

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
        actions.add(IMActions.ACTION_DOING_LOGIN);

        imServiceHelper.connect(getActivity(), actions, IMServiceHelper.INTENT_NO_PRIORITY, this);
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
        noNetworkView = curView.findViewById(R.id.layout_no_network);
        reconnectingProgressBar = (ProgressBar) curView.findViewById(R.id.progressbar_reconnect);

        super.init(curView);
        initTitleView();// 初始化顶部view
        initContactListView(); // 初始化联系人列表视图
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

        super.onStop();
    }

    @Override
    public void onPause() {
        logger.d("chatfragment#onPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        logger.d("chatfragment#onResume");
        try {
            IMRecentSessionManager recentSessionManager = imServiceHelper.getIMService()
                    .getRecentSessionManager();
            int totalUnreadMsgCnt = recentSessionManager.getTotalUnreadMsgCnt();
            logger.d("unread#total cnt %d", totalUnreadMsgCnt);
            ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);
        } catch (Exception e) {
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        imServiceHelper.disconnect(getActivity());

        super.onDestroy();
    }

    /**
     * @Description 设置顶部按钮
     */
    private void initTitleView() {
        // 设置标题
        setTopTitle(getActivity().getString(R.string.chat_title));

    }

    private void initContactListView() {
        contactListView = (ListView) curView.findViewById(R.id.ContactListView);

        contactListView.setOnItemClickListener(this);
        contactListView.setOnItemLongClickListener(this);

        contactAdapter = new ChatAdapter(getActivity());
        contactListView.setAdapter(contactAdapter);

        // this is critical, disable loading when finger sliding, otherwise
        // you'll find sliding is not very smooth
        contactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(),
                true, true));
    }

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

        IMUIHelper.openSessionChatActivity(logger, getActivity(), recentInfo.getEntityId(),
                recentInfo.getSessionType(), imServiceHelper.getIMService());
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
        } else if (action.equals(IMActions.ACTION_DOING_LOGIN)) {
            handleDoingLogin(intent);
        }

        tryHandleSearchAction(action);
    }

    private void handleDoingLogin(Intent intent) {
        logger.d("chatFragment#login#recv handleDoingLogin event");
        if (reconnectingProgressBar != null) {
            reconnectingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void handleOnLoginResult(Intent intent) {
        logger.d("chatfragment#handleOnLoginResult");

        int errorCode = intent.getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

        if (errorCode == ErrorCode.S_OK) {
            logger.d("chatfragment#loginOk");

            noNetworkView.setVisibility(View.GONE);
        } else {
            if (reconnectingProgressBar != null) {
                reconnectingProgressBar.setVisibility(View.GONE);
            }
        }
    }

    private void handleServerDisconnected() {
        logger.d("chatfragment#handleServerDisconnected");

        // IMService imService = imServiceHelper.getIMService();
        // if (imService != null) {
        // if (imService.getLoginManager().isLogout()) {
        // logger.d("chatfragment#is logout, do nothing");
        // return;
        // }
        // }

        if (reconnectingProgressBar != null) {
            reconnectingProgressBar.setVisibility(View.GONE);
        }

        if (noNetworkView != null) {
            noNetworkView.setVisibility(View.VISIBLE);
        }
        // Toast.makeText(getActivity(),
        // getString(R.string.no_network_notification),
        // Toast.LENGTH_SHORT).show();
    }

    private void onRecentContactDataReady() {
        IMRecentSessionManager recentSessionManager = imServiceHelper.getIMService()
                .getRecentSessionManager();

        int totalUnreadMsgCnt = recentSessionManager.getTotalUnreadMsgCnt();
        logger.d("unread#total cnt %d", totalUnreadMsgCnt);
        ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);

        List<RecentInfo> recentSessionList = recentSessionManager.getRecentSessionList();
        contactAdapter.setData(recentSessionList);

        firstUnreadPosition = recentSessionManager.getFirstUnreadPosition();

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

        initNoNewtworkClickEvent(imService);
    }

    private void initNoNewtworkClickEvent(final IMService imService) {
        logger.d("chatFragment#initNoNewtworkClickEvent");
        if (noNetworkView == null) {
            return;
        }
        noNetworkView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logger.d("chatFragment#noNetworkView clicked");
                imService.getReconnectManager().reconnect();

                reconnectingProgressBar.setVisibility(View.VISIBLE);
            }
        });

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

    @Override
    protected void initHandler() {
        // TODO Auto-generated method stub

    }

    /**
     * 滑动到指定联系人条目
     * 
     * @Description
     * @param position
     */
    private void scrollToPosition(final int position) {
        try {
            if (position < 0) {
                return;
            }
            if (contactListView != null) {
                contactListView.smoothScrollToPositionFromTop(position, 0);
            }
        } catch (Exception e) {
            logger.e("chatfragment#scrollToPosition, do nothing:", position);
        }
    }

    /**
     * 滚动到有未读消息的第一个联系人
     * 
     * @Description
     * @param position
     */
    public void setUnreadPosition() {
        scrollToPosition(firstUnreadPosition);
    }
}
