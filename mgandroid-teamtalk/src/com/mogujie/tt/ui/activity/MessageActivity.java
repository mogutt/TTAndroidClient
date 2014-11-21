
package com.mogujie.tt.ui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mogujie.tt.R;
import com.mogujie.tt.adapter.MessageAdapter;
import com.mogujie.tt.adapter.album.AlbumHelper;
import com.mogujie.tt.adapter.album.ImageBucket;
import com.mogujie.tt.app.IMEntrance;
import com.mogujie.tt.audio.biz.AudioPlayerHandler;
import com.mogujie.tt.audio.biz.AudioRecordHandler;
import com.mogujie.tt.biz.MessageHelper;
import com.mogujie.tt.biz.MessageNotifyCenter;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMContactManager;
import com.mogujie.tt.imlib.IMGroupManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.IMUIHelper.SessionInfo;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.MessageDispatchCenter;
import com.mogujie.tt.task.TaskCallback;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.tools.Emoparser;
import com.mogujie.tt.ui.tools.ImageTool;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.widget.CustomEditView;
import com.mogujie.tt.widget.EmoGridView;
import com.mogujie.tt.widget.EmoGridView.OnEmoGridViewItemClick;
import com.mogujie.tt.widget.MGProgressbar;
import com.mogujie.tt.widget.SpeekerToast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

//import com.mogujie.tt.conn.ReconnectManager;

/**
 * @Description 主消息界面
 * @author Nana
 * @date 2014-7-15
 */
public class MessageActivity extends TTBaseActivity
        implements
        OnRefreshListener2<ListView>,
        View.OnClickListener,
        OnTouchListener,
        TextWatcher,
        SensorEventListener,
        OnIMServiceListner {
    @Override
    public void onIMServiceConnected() {
        logger.d("messageactivity#onIMServiceConnected");

        imService = imServiceHelper.getIMService();

        int sessionType = session.getSessionType();
        String sessionId = session.getSessionId();

        logger.d("messageactivity#sessionType:%d, sessionId:%s", sessionType, sessionId);

        setTitleByUser2(sessionId, sessionType);
        handleOnResume(true);
        cancelSessionNotifications();
    }

    private void handleOnResume(boolean firstStart) {
        logger.d("messageactivity#handleOnResume firstStart:%s", firstStart);

        int sessionType = session.getSessionType();
        String sessionId = session.getSessionId();

        if (imService == null) {
            return;
        }

        // todo eric additem has been clean in loadHistoryMessage below
        // no need to additem when firstStart is true
        handleUnreadMsgs(sessionId, sessionType);

        if (firstStart) {
            loadHistoryMessage(sessionId, false);
        }
    }

    private void handleUnreadMsgs(String sessionId, int sessionType) {
        logger.d("messageacitivity#handleUnreadMsgs sessionId:%s", sessionId);

        if (imService == null) {
            return;
        }

        imService.getRecentSessionManager().resetUnreadMsgCnt(sessionId);

        List<MessageInfo> unreadMsgList = imService.getMessageManager().ReadUnreadMsgList(
                sessionId, sessionType);
        if (unreadMsgList == null || unreadMsgList.isEmpty()) {
            logger.d("messageactivity#no unread msg");
            return;
        }

        logger.d("messageactivity#got cnt:%d unread msgs", unreadMsgList.size());

        for (MessageInfo msg : unreadMsgList) {
            adapter.addItem(msg);
        }

        adapter.notifyDataSetChanged();

        scrollToBottomListItem();
    }

    // todo eric timeout and failed 2 statuses
    private void onMsgAck(Intent intent) {
        logger.d("messageactivity#onMsgAck");

        // Bundle extras = intent.getExtras();
        // MessageEntity msg = (MessageEntity)
        // extras.getSerializable(SysConstant.MSG_KEY);
        // if (msg == null) {
        // logger.e("messageactivity#msg is null");
        // return;
        // }
        //
        // String curChatId = CacheHub.getInstance().getChatUser().getUserId();
        // if (!msg.toId.equals(curChatId)) {
        // logger.d("messageactivity#toid:%s is not current userid:%s, ignore",
        // msg.toId, curChatId);
        // return;
        //
        // }
        //
        MessageHelper.onReceiveMsgACK2(intent);
        adapter.notifyDataSetChanged();

    }

    private void onMsgRecv(Intent intent, BroadcastReceiver broadcastReceiver) {
        logger.d("messageactivity#onMsgRecv");
        String sessionId = intent.getStringExtra(SysConstant.KEY_SESSION_ID);
        if (!sessionId.equals(session.getSessionId())) {
            logger.d("messageactivity#not this session msg -> id:%s", sessionId);
            return;
        }

        // //todo eric this is meaningless param right
        // String msgId = intent.getStringExtra(SysConstant.MSG_ID_KEY);
        // logger.d("messageactivity#msg belongs to this session, sessionId:%s, msgId:%s",
        // sessionId, msgId);

        // eat the message, so there woould be no notification in notification
        // bar
        broadcastReceiver.abortBroadcast();

        if (imService == null) {
            return;
        }

        imService.getRecentSessionManager().resetUnreadMsgCnt(sessionId);
        List<MessageInfo> msgList = imService.getMessageManager().ReadUnreadMsgList(sessionId,
                session.getSessionType());
        if (msgList == null) {
            logger.e("messageactivity#no any unread MessageInfo list");
            return;
        }

        for (MessageInfo msgInfo : msgList) {
            MessageHelper.onReceiveMessage2(msgInfo, imServiceHelper);
        }

        scrollToBottomListItem();
    }

    @Override
    public void onAction(String action, Intent intent,
            BroadcastReceiver broadcastReceiver) {
        logger.d("messageactivity#onAction -> action:%s", action);

        if (action.equals(IMActions.ACTION_MSG_ACK)) {
            onMsgAck(intent);
        } else if (action.equals(IMActions.ACTION_MSG_UNACK_TIMEOUT)) {
            onMsgUnAckTimeout(intent);
        } else if (action.equals(IMActions.ACTION_MSG_RECV)) {
            if (!imServiceConnectionEnabled) {
                logger.d("messageactivity#imServiceConnection Disabled, do nothing");
                return;
            }

            onMsgRecv(intent, broadcastReceiver);
        } else if (action.equals(IMActions.ACTION_MSG_RESENT)) {
            onMessageResent(intent);
        } else if (action.equals(IMActions.ACTION_MSG_STATUS)) {
            onMessageNewStatus(intent);
        }
    }

    private void onMessageNewStatus(Intent intent) {
        logger.d("chat#onMessageNewStatus");
        if (intent == null) {
            return;
        }

        // int newStatus = intent.getIntExtra(SysConstant.STATUS_KEY,
        // SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
        adapter.notifyDataSetChanged();
    }

    private void onMessageResent(Intent intent) {
        logger.d("chat#resend#onMessageResent");
        if (intent == null) {
            return;
        }

        SessionInfo sessionInfoFromIntent = IMUIHelper.getSessionInfoFromIntent(intent);
        if (sessionInfoFromIntent == null) {
            return;
        }

        if (IMUIHelper.isSameSession(sessionInfoFromIntent, session)) {
            logger.d("chat#resend#same session, reloading messages");
            loadHistoryMessage(session.getSessionId(), true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        logger.d("chat#onNewIntent");
        if (intent == null) {
            return;
        }

        SessionInfo newSessionInfo = IMUIHelper.getSessionInfoFromIntent(intent);
        if (newSessionInfo == null) {
            return;
        }

        logger.d("chat#newSessionInfo:%s", newSessionInfo);

        logger.d("chat#current sessionInfo:sessionid:%s, sessionType:%d", session.getSessionId(),
                session.getSessionType());

        // different session
        if (!(newSessionInfo.getSessionId().equals(session.getSessionId()) && newSessionInfo
                .getSessionType() == session.getSessionType())) {
            logger.w("chat#goint to open a new chat window");

            this.finish();

            // todo eric original author uses a static adapter to handle all
            // activity logics
            // we can't open the new chat window directly here, because the old
            // would clean the adapter data
            // after we just open the new window
            // so we have to open the window to in onDestroy
            // todo eric rewrite the whole shit all over
            this.newSessionInfo = newSessionInfo;
        }
    }

    private void onMsgUnAckTimeout(Intent intent) {
        String msgId = intent.getStringExtra(SysConstant.MSG_ID_KEY);
        logger.d("chat#onMsgUnAckTimeout, msgId:%s", msgId);

        updateMessageState(msgId, SysConstant.MESSAGE_STATE_FINISH_FAILED);
    }

    private static Handler uiHandler = null;// 处理界面消息
    private static Handler msgHandler = null;// 处理协议消息
    private PullToRefreshListView lvPTR = null;
    private CustomEditView messageEdt = null;
    private TextView sendBtn = null;
    private Button recordAudioBtn = null;
    private ImageView keyboardInputImg = null;
    private ImageView soundVolumeImg = null;
    private LinearLayout soundVolumeLayout = null;
    private static MessageAdapter adapter = null;
    private ImageView audioInputImg = null;
    private ImageView addPhotoBtn = null;
    private ImageView addEmoBtn = null;
    private EmoGridView emoGridView = null;
    private String audioSavePath = null;
    private InputMethodManager inputManager = null;
    private AudioRecordHandler audioRecorderInstance = null;
    private Thread audioRecorderThread = null;
    private Dialog soundVolumeDialog = null;
    private View unreadMessageNotifyView = null;
    private View addOthersPanelView = null;
    private AlbumHelper albumHelper = null;
    private static List<ImageBucket> albumList = null;
    MGProgressbar progressbar = null;
    private boolean audioReday = false;
    static private AudioManager audioManager = null;
    static private SensorManager sensorManager = null;
    static private Sensor sensor = null;
    static private int audioPlayMode = SysConstant.AUDIO_PLAY_MODE_NORMAL;
    private int preAudioPlayMode = SysConstant.AUDIO_PLAY_MODE_NORMAL;
    private String takePhotoSavePath = "";
    // 避免用户信息与商品详情的重复请求
    public static boolean requestingGoodsDetail = false;
    public static boolean requestingUserInfo = false;
    private Logger logger = Logger.getLogger(MessageActivity.class);
    private IMServiceHelper imServiceHelper = new IMServiceHelper();
    private IMService imService;
    private IMSession session = new IMSession(imServiceHelper);
    private int MSG_CNT_PER_PAGE = 18;
    private int firstHistoryMsgTime = -1;
    private boolean imServiceConnectionEnabled = false;

    SessionInfo newSessionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.d("messageactivity#onCreate:%s", this);

        super.onCreate(savedInstanceState);
        initView();
        initData();
        initHandler();
        registEvents();
        initAudioSensor();
        IMEntrance.getInstance().setContext(MessageActivity.this);

        ArrayList<String> actions = new ArrayList<String>();
        actions.add(IMActions.ACTION_MSG_ACK);
        actions.add(IMActions.ACTION_MSG_RECV);
        actions.add(IMActions.ACTION_MSG_UNACK_TIMEOUT);
        actions.add(IMActions.ACTION_MSG_RESENT);
        actions.add(IMActions.ACTION_MSG_STATUS);

        imServiceHelper.connect(this, actions, IMServiceHelper.INTENT_MAX_PRIORITY, this);
        logger.d("messageactivity#register im service");

        int sessionType = getIntent().getIntExtra(SysConstant.KEY_SESSION_TYPE, 0);
        String sessionId = getIntent().getStringExtra(SysConstant.KEY_SESSION_ID);

        session.setType(sessionType);
        session.setSessionId(sessionId);
    }

    private void cancelSessionNotifications() {
        logger.d("chat#notification#cancelSessionNotifications");
        if (session.getSessionId().isEmpty()) {
            logger.e("chat#notification#sessionId is still empty");
            return;
        }

        if (imService == null) {
            return;
        }

        NotificationManager notifyMgr = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyMgr == null) {
            return;
        }

        int notificationId = imService.getNotificationManager().getSessionNotificationId(
                session.getSessionId(), session.getSessionType());
        notifyMgr.cancel(notificationId);
    }

    @Override
    protected void onResume() {
        logger.d("messageactivity#onresume:%s", this);
        super.onResume();

        imServiceConnectionEnabled = true;

        firstHistoryMsgTime = 0;

        CacheHub.getInstance().setSessionInfo(
                new SessionInfo(session.getSessionId(), session.getSessionType()));

        // not the first time
        if (imServiceHelper.getIMService() != null) {
            cancelSessionNotifications();
            handleOnResume(false);
        }

        // 修改消息状态
        setMessageState();
        // 修改消息状态修改后，通知联系人列表更新列表信息
        MessageNotifyCenter.getInstance().doNotify(SysConstant.EVENT_RECENT_INFO_CHANGED);

        // 下面的标志用于防止用户信息与商品详情的重复请求
        MessageActivity.requestingGoodsDetail = false;
        MessageActivity.requestingUserInfo = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;

        // // 拍照时如果断网直接提示，不进行图片消息的展示
        // if (!StateManager.getInstance().isOnline()) {
        // Toast.makeText(MessageActivity.this,
        // getResources().getString(R.string.disconnected_by_server),
        // Toast.LENGTH_LONG).show();
        //
        // return;
        // }

        switch (requestCode) {
            case SysConstant.CAMERA_WITH_DATA:
                handleTakePhotoData(data);
                break;
            case SysConstant.ALBUM_BACK_DATA:
                logger.d("pic#ALBUM_BACK_DATA");
                setIntent(data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void initHandler() {
        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // switch (msg.what) {
                // case ProtocolConstant.SID_MSG * 1000
                // + ProtocolConstant.CID_MSG_DATA_ACK:
                // MessageHelper.onReceiveMsgACK(msg.obj);
                // break;
                // case ProtocolConstant.SID_MSG * 1000
                // + ProtocolConstant.CID_MSG_DATA:
                // MessageHelper.onReceiveMessage(msg.obj);
                // break;
                // case ProtocolConstant.SID_OTHER * 1000
                // + ProtocolConstant.CID_GET_USER_INFO_RESPONSE:
                // MessageHelper.onGetUserInfo(msg.obj, uiHandler,
                // MessageActivity.this);
                // break;
                // }
            }
        };

        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_RECORD_FINISHED:
                        onRecordVoiceEnd((Float) msg.obj);
                        break;
                    case HandlerConstant.HANDLER_NET_STATE_DISCONNECTED:
                        setTitle(getString(R.string.disconnected));
                        break;
                    case HandlerConstant.HANDLER_LOGIN_MSG_SERVER:
                        MessageHelper.onConnectedMsgServer(msgHandler, uiHandler);
                        break;
                    case HandlerConstant.HANDLER_IMAGE_UPLOAD_FAILD:
                        onUploadImageFaild(msg.obj);
                        break;
                    case HandlerConstant.HANDLER_IMAGE_UPLOAD_SUCESS:
                        logger.d("pic#upload image ok");
                        MessageHelper.onImageUploadFinish(msg.obj, uiHandler, msgHandler,
                                imServiceHelper.getIMService().getMessageManager(),
                                session.getSessionType());
                        adapter.notifyDataSetChanged();
                        break;
                    case HandlerConstant.HANDLER_STOP_PLAY:
                        adapter.stopVoicePlayAnim((String) msg.obj);
                        break;
                    case HandlerConstant.RECEIVE_MAX_VOLUME:
                        onReceiveMaxVolume((Integer) msg.obj);
                        break;
                    case HandlerConstant.RECORD_AUDIO_TOO_LONG:
                        doFinishRecordAudio();
                        break;
                    case HandlerConstant.HANDLER_MESSAGES_NEW_MESSAGE_COME:
                        if (CommonUtil.isTopActivy(MessageActivity.this,
                                SysConstant.MESSAGE_ACTIVITY)) {
                            // 设置消息状态并通知联系人列表更新列表信息
                            setMessageState();
                            MessageNotifyCenter.getInstance().doNotify(
                                    SysConstant.EVENT_RECENT_INFO_CHANGED);
                        }
                        break;
                    case HandlerConstant.HANDLER_SEND_MESSAGE_TIMEOUT:
                        adapter.updateMessageState((MessageInfo) msg.obj,
                                SysConstant.MESSAGE_STATE_FINISH_FAILED);
                        break;
                    case HandlerConstant.HANDLER_SEND_MESSAGE_FAILED:
                        adapter.updateMessageState((MessageInfo) msg.obj,
                                SysConstant.MESSAGE_STATE_FINISH_FAILED);
                        break;
                    case HandlerConstant.SHOULD_BLOCK_USER:
                        blockUser(true);
                        break;
                    case HandlerConstant.SHOULD_NOT_BLOCK_USER:
                        blockUser(false);
                        break;
                    case HandlerConstant.START_BLOCK_CHECK:
                        onStartBlockCheck();
                        break;
                    case HandlerConstant.SET_TITLE:
                        enableBottomView(true);
                        setTitleByUser((User) msg.obj);
                        break;
                    case HandlerConstant.REQUEST_CUSTOM_SERVICE_FAILED:
                        onRequestCustomServiceFailed();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * @Description 请求客服失败后处理
     */
    private void onRequestCustomServiceFailed() {
        enableBottomView(false);// 禁用输入框
        setTitle(R.string.request_custom_service_failed);// 标题栏给出失败提示
    }

    /**
     * @Description 是否禁用底部输入控件
     * @param enabled
     */
    private void enableBottomView(boolean enabled) {
        sendBtn.setEnabled(enabled);
        recordAudioBtn.setEnabled(enabled);
        audioInputImg.setEnabled(enabled);
        messageEdt.setEnabled(enabled);
        keyboardInputImg.setEnabled(enabled);
        addPhotoBtn.setEnabled(enabled);
        addEmoBtn.setEnabled(enabled);
    }

    /**
     * @Description 显示联系人界面
     */
    private void showGroupManageActivity(boolean fromMessagePage) {
        Intent i = new Intent(this, GroupManagermentActivity.class);
        i.putExtra(SysConstant.KEY_SESSION_ID, session.getSessionId());
        i.putExtra(SysConstant.KEY_SESSION_TYPE, session.getSessionType());

        startActivity(i);
    }

    /**
     * @Description 注册事件
     */
    private void registEvents() {
        // 接收未读消息提示
        MessageNotifyCenter.getInstance().register(SysConstant.EVENT_UNREAD_MSG, getUiHandler(),
                HandlerConstant.HANDLER_MESSAGES_NEW_MESSAGE_COME);

        // // 接收网络状态通知
        // NetStateDispach.getInstance()
        // .register(MessageActivity.class, uiHandler);
    }

    /**
     * @Description 取消事件注册
     */
    private void unregistEvents() {
        MessageDispatchCenter.getInstance().unRegister(uiHandler);
        MessageNotifyCenter.getInstance().unregister(SysConstant.EVENT_UNREAD_MSG, getUiHandler(),
                HandlerConstant.HANDLER_MESSAGES_NEW_MESSAGE_COME);
    }

    /**
     * @Description 初始化AudioManager，用于访问控制音量和钤声模式
     */
    private void initAudioSensor() {
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * @Description 初始化数据（相册,表情,数据库相关）
     */
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                albumHelper = AlbumHelper.getHelper(MessageActivity.this);
                albumList = albumHelper.getImagesBucketList(false);
                Emoparser.getInstance(MessageActivity.this);
            }
        }).start();
    }

    /**
     * @Description 初始化界面控件
     */
    private void initView() {
        // 设置顶部标题栏
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        topLeftBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MessageActivity.this.finish();
            }
        });
        letTitleTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MessageActivity.this.finish();
            }
        });

        // 绑定布局资源(注意放所有资源初始化之前)
        LayoutInflater.from(this).inflate(R.layout.tt_activity_message, topContentView);
        // 右上角联系人图标
        setRightButton(R.drawable.tt_top_right_group_manager);

        // 未读消息提示
        unreadMessageNotifyView = new View(this);
        unreadMessageNotifyView.setBackgroundResource(R.drawable.tt_unread_message_notify_bg);
        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, width);
        lp.gravity = Gravity.TOP | Gravity.RIGHT;
        lp.topMargin = width - 4;
        lp.rightMargin = width - 5;
        unreadMessageNotifyView.setLayoutParams(lp);
        topBar.addView(unreadMessageNotifyView, lp);
        unreadMessageNotifyView.setVisibility(View.GONE);

        // 输入对象
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 表情
        emoGridView = (EmoGridView) findViewById(R.id.emo_gridview);
        emoGridView.setOnEmoGridViewItemClick(new OnEmoGridViewItemClick() {
            @Override
            public void onItemClick(int facesPos, int viewIndex) {
                int deleteId = (++viewIndex) * (SysConstant.pageSize - 1);
                if (deleteId > Emoparser.getInstance(MessageActivity.this).getResIdList().length) {
                    deleteId = Emoparser.getInstance(MessageActivity.this).getResIdList().length;
                }
                if (deleteId == facesPos) {
                    String msgContent = messageEdt.getText().toString();
                    if (msgContent.isEmpty())
                        return;
                    if (msgContent.contains("["))
                        msgContent = msgContent.substring(0, msgContent.lastIndexOf("["));
                    messageEdt.setText(msgContent);
                } else {
                    int resId = Emoparser.getInstance(MessageActivity.this).getResIdList()[facesPos];
                    String pharse = Emoparser.getInstance(MessageActivity.this).getIdPhraseMap()
                            .get(resId);
                    int startIndex = messageEdt.getSelectionStart();
                    Editable edit = messageEdt.getEditableText();
                    if (startIndex < 0 || startIndex >= edit.length()) {
                        if (null != pharse) {
                            edit.append(pharse);
                        }
                    } else {
                        if (null != pharse) {
                            edit.insert(startIndex, pharse);
                        }
                    }
                }
                Editable edtable = messageEdt.getText();
                int position = edtable.length();
                Selection.setSelection(edtable, position);
            }
        });
        emoGridView.setAdapter();

        // 列表控件(开源PTR)
        lvPTR = (PullToRefreshListView) this.findViewById(R.id.message_list);

        Drawable loadingDrawable = getResources().getDrawable(R.drawable.pull_to_refresh_indicator);
        final int indicatorWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29,
                getResources().getDisplayMetrics());
        loadingDrawable.setBounds(new Rect(0, indicatorWidth, 0, indicatorWidth));
        lvPTR.getLoadingLayoutProxy().setLoadingDrawable(loadingDrawable);

        lvPTR.getRefreshableView().setCacheColorHint(Color.WHITE);
        lvPTR.getRefreshableView().setSelector(new ColorDrawable(Color.WHITE));
        lvPTR.getRefreshableView().setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (emoGridView.getVisibility() == View.VISIBLE) {
                        emoGridView.setVisibility(View.GONE);
                    }

                    if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                        addOthersPanelView.setVisibility(View.GONE);
                    }
                    inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
                }
                return false;
            }
        });

        lvPTR.getRefreshableView().addHeaderView(
                LayoutInflater.from(this).inflate(R.layout.tt_messagelist_header,
                        lvPTR.getRefreshableView(), false));
        adapter = new MessageAdapter(this);
        adapter.setSession(session);
        adapter.setIMServiceHelper(imServiceHelper);
        lvPTR.setAdapter(adapter);
        lvPTR.setOnRefreshListener(this);
//        lvPTR.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        // 界面底部输入框布局
        sendBtn = (TextView) this.findViewById(R.id.send_message_btn);
        recordAudioBtn = (Button) this.findViewById(R.id.record_voice_btn);
        audioInputImg = (ImageView) this.findViewById(R.id.voice_btn);
        messageEdt = (CustomEditView) this.findViewById(R.id.message_text);
        RelativeLayout.LayoutParams param = (LayoutParams) messageEdt.getLayoutParams();
        param.addRule(RelativeLayout.LEFT_OF, R.id.show_add_photo_btn);
        param.addRule(RelativeLayout.RIGHT_OF, R.id.show_emo_btn);
        messageEdt.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollToBottomListItem();
                    if (emoGridView.getVisibility() == View.VISIBLE) {
                        emoGridView.setVisibility(View.GONE);
                    }

                    if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                        addOthersPanelView.setVisibility(View.GONE);
                    }
                }
            }
        });
        messageEdt.setOnClickListener(this);

        keyboardInputImg = (ImageView) this.findViewById(R.id.show_keyboard_btn);
        addPhotoBtn = (ImageView) this.findViewById(R.id.show_add_photo_btn);
        addPhotoBtn.setOnClickListener(this);
        addEmoBtn = (ImageView) this.findViewById(R.id.show_emo_btn);
        addEmoBtn.setOnClickListener(this);
        initSoundVolumeDlg();

        addOthersPanelView = findViewById(R.id.add_others_panel);
        View takePhotoBtn = findViewById(R.id.take_photo_btn);
        takePhotoBtn.setOnClickListener(this);
        View takeCameraBtn = findViewById(R.id.take_camera_btn);
        takeCameraBtn.setOnClickListener(this);

        // 初始化滚动条(注意放到最后)
        View view = LayoutInflater.from(MessageActivity.this)
                .inflate(R.layout.tt_progress_ly, null);
        progressbar = (MGProgressbar) view.findViewById(R.id.tt_progress);
        LayoutParams pgParms = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pgParms.bottomMargin = 50;
        addContentView(view, pgParms);

        // 绑定各控件事件监听对象
        messageEdt.addTextChangedListener(this);
        keyboardInputImg.setOnClickListener(this);
        audioInputImg.setOnClickListener(this);
        recordAudioBtn.setOnTouchListener(this);
        sendBtn.setOnClickListener(this);
        topLeftBtn.setOnClickListener(this);
        topRightBtn.setOnClickListener(this);
    }

    /**
     * @Description 初始化音量对话框
     */
    private void initSoundVolumeDlg() {
        soundVolumeDialog = new Dialog(this, R.style.SoundVolumeStyle);
        soundVolumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        soundVolumeDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        soundVolumeDialog.setContentView(R.layout.tt_sound_volume_dialog);
        soundVolumeDialog.setCanceledOnTouchOutside(true);
        soundVolumeImg = (ImageView) soundVolumeDialog.findViewById(R.id.sound_volume_img);
        soundVolumeLayout = (LinearLayout) soundVolumeDialog.findViewById(R.id.sound_volume_bk);
    }

    private List<MessageInfo> loadHistory() {
        logger.d("messageactivity#loadHistory");

        List<MessageInfo> historyMsgInfo = imServiceHelper
                .getIMService()
                .getDbManager()
                .getHistoryMsg(session.getSessionId(), session.getSessionType(), 0,
                        MSG_CNT_PER_PAGE, firstHistoryMsgTime);

        if (historyMsgInfo != null && !historyMsgInfo.isEmpty()) {
            firstHistoryMsgTime = historyMsgInfo.get(0).getCreated();
            logger.d("messageactivity#db#got the fristHistoryMsgTime:%d", firstHistoryMsgTime);
        }

        return historyMsgInfo;

    }

    private void loadHistoryMessage(String sessionId, boolean fromStart) {
        logger.d("chat#messageactivity#loadHistoryMessage sessionId:%s, reset:%s", sessionId,
                fromStart);

        if (imServiceHelper.getIMService() == null) {
            logger.d("messageactivity#still not connected im");
            return;
        }

        adapter.clearItem();
        adapter.clearMsgIndexMap();

        if (fromStart) {
            firstHistoryMsgTime = 0;
        }

        List<MessageInfo> historyMsgInfo = loadHistory();

        if (!historyMsgInfo.isEmpty()) {
            logger.d("messageactivity#got historyMessage");
        }
        adapter.addItem(true, historyMsgInfo);
        adapter.notifyDataSetChanged();

        scrollToBottomListItem();
    }

    // /**
    // * @Description 判断如果是从联系人界面切换到消息界面，则根据需要渲染消息界面
    // * @param intent
    // */
    // private void fillMessageViewByContact(Intent intent) {
    // logger.d("fillMessageViewByContact");
    //
    // if (null == intent)
    // return;
    //
    // loadHistoryMessage(session.getSessionId());
    // }

    /**
     * @Description 设置会话对象设置消息标题
     * @param user
     */

    private void setTitleByUser(User user) {
        if (null == user) {
            return;
        }
        if (!TextUtils.isEmpty(user.getNickName())) {
            setTitle(user.getNickName());
        } else if (!TextUtils.isEmpty(user.getName())) {
            setTitle(user.getName());
        } else {
            setTitle(user.getUserId());
        }
    }

    private void setTitleByGroup(String sessionId) {
        IMGroupManager groupMgr = imServiceHelper.getIMService().getGroupManager();
        if (groupMgr == null) {
            return;
        }

        GroupEntity group = groupMgr.findGroup(sessionId);
        if (group == null) {
            logger.e("messageactivity#findGroup failed.sessionid:%s", sessionId);
            return;
        }
        setTitle(group.name);

    }

    // todo eric
    private void setTitleByUser2(String sessionId, int sessionType) {
        logger.d("setTitleByUser sessionId:%s,  sessionType:%d", sessionId, sessionType);

        if (sessionType == IMSession.SESSION_P2P) {
            IMContactManager contactManager = imServiceHelper.getIMService().getContactManager();
            final ContactEntity contact = contactManager.findContact(sessionId);
            if (contact == null) {
                logger.e("messageactivity#findContact failed.sessionid:%s", sessionId);
                return;
            }

            if (!TextUtils.isEmpty(contact.nickName)) {
                setTitle(contact.nickName);
            }else if(!TextUtils.isEmpty(contact.name)){
                setTitle(contact.name);
            }else{
                setTitle(contact.id);
            }
            topTitleTxt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    IMUIHelper.openUserProfileActivity(MessageActivity.this, contact.id);
                }
            });
        } else {
            setTitleByGroup(sessionId);
        }
    }

    /**
     * @Description 设置消息相关状态：未读消息计数，消息已读状态，界面未读消息提示
     */
    private void setMessageState() {
        User loginUser = CacheHub.getInstance().getLoginUser();
        User chatUser = CacheHub.getInstance().getChatUser();
        if (null != loginUser && null != chatUser) {
            CacheHub.getInstance().clearUnreadCount(chatUser.getUserId());
            CacheHub.getInstance().updateMsgReadStatus(loginUser.getUserId(), chatUser.getUserId(),
                    SysConstant.MESSAGE_ALREADY_READ);
        }
        if (0 < CacheHub.getInstance().getUnreadCount()) {
            unreadMessageNotifyView.setVisibility(View.VISIBLE);
        } else {
            unreadMessageNotifyView.setVisibility(View.GONE);
        }
    }

    public void showProgress() {
        progressbar.showProgress();
    }

    public void hideProgress() {
        progressbar.hideProgress();
    }

    /**
     * @Description 开始检测是否是黑名单用户时，界面显示状态
     */
    private void onStartBlockCheck() {
        showProgress();
        enableBottomView(false);
    }

    /**
     * @Description 根据是否是黑名单用户显示相关信息
     * @param block
     */
    private void blockUser(boolean block) {
        hideProgress();
        if (block) {
            Toast.makeText(MessageActivity.this, getString(R.string.block_chat), Toast.LENGTH_LONG)
                    .show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    MessageActivity.this.finish();
                }
            }, 1000);
        } else {
            enableBottomView(true);
        }
    }

    /**
     * @Description 修改消息的文件保存路径
     * @param msgId
     * @param path
     */
    public static void updateMessageSavePath(String msgId, String path) {
        adapter.updateItemSavePath(msgId, path);
    }

    /**
     * @Description 修改消息状态
     */
    public static void updateMessageState(String msgId, int state) {
        adapter.updateItemState(msgId, state);
    }

    /**
     * @Description 修改消息状态
     */
    public static void updateMessageState(MessageInfo msgInfo, int state) {
        adapter.updateMessageState(msgInfo, state);
    }

    /**
     * @Description 清空适配器
     */
    public static void clearItem() {
        adapter.clearItem();
        adapter.notifyDataSetChanged();
    }

    /**
     * @Description 向消息列表适配器中添加一条消息
     * @param msgInfo
     */
    public static void addItem(MessageInfo msgInfo) {
        Logger logger = Logger.getLogger(MessageActivity.class);
        logger.d("chat#addItem msgInfo:%s", msgInfo);

        // todo eric workaround
        if (TextUtils.isEmpty(msgInfo.talkerId)) {
//            logger.e("talkerid#empty talkerid, callstack:%s",
//                    Log.getStackTraceString(new Throwable()));
            
            logger.e("talkerid#empty talkerid");
            msgInfo.talkerId = msgInfo.fromId;
        }

        adapter.addItem(msgInfo);
        adapter.notifyDataSetChanged();
    }

    /**
     * @Description 向消息列表适配器中添加历史消息
     * @param fromStart
     * @param historyMsgInfo
     */
    public static void addItem(boolean fromStart,
            ArrayList<MessageInfo> historyMsgInfo) {
        adapter.addItem(true, historyMsgInfo);
        adapter.notifyDataSetChanged();
    }

    /**
     * @Description 录音超时(60s)，发消息调用该方法
     */
    public void doFinishRecordAudio() {
        try {
            if (audioRecorderInstance.isRecording()) {
                audioRecorderInstance.setRecording(false);
            }
            if (soundVolumeDialog.isShowing()) {
                soundVolumeDialog.dismiss();
            }

            recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal);

            audioRecorderInstance.setRecordTime(SysConstant.MAX_SOUND_RECORD_TIME);
            onRecordVoiceEnd(SysConstant.MAX_SOUND_RECORD_TIME);
        } catch (Exception e) {
        }
    }

    /**
     * @Description 根据分贝值设置录音时的音量动画
     * @param voiceValue
     */
    private void onReceiveMaxVolume(int voiceValue) {
        if (voiceValue < 200.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_02);
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_03);
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_04);
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_05);
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_06);
        } else if (voiceValue > 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_07);
        }
    }

    /**
     * @Description 图片上传失败时界面的相关显示
     * @param obj
     */
    private void onUploadImageFaild(Object obj) {
        logger.d("chat#pic#onUploadImageFaild");
        if (obj == null)
            return;
        MessageInfo messageInfo = (MessageInfo) obj;
        adapter.updateMessageState(messageInfo, SysConstant.MESSAGE_STATE_FINISH_FAILED);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    /**
     * @Description 处理拍照后的数据
     * @param data
     */
    private void handleTakePhotoData(Intent data) {
        Bitmap bitmap = null;
        if (data == null) {
            bitmap = ImageTool.createImageThumbnail(takePhotoSavePath);
        } else {
            Bundle extras = data.getExtras();
            bitmap = extras == null ? null : (Bitmap) extras.get("data");
        }
        if (bitmap == null) {
            return;
        }
        // 将图片发送至服务器
        MessageInfo msg = new MessageInfo();
        msg.setMsgFromUserId(CacheHub.getInstance().getLoginUserId());
        msg.setIsSend(true);
        msg.setMsgCreateTime((int) (System.currentTimeMillis() / 1000));
        msg.setSavePath(takePhotoSavePath);
        msg.setDisplayType(SysConstant.DISPLAY_TYPE_IMAGE);
        msg.setMsgType(SysConstant.MESSAGE_TYPE_TELETEXT);
        msg.setMsgContent("");
        msg.setTargetId(session.getSessionId());
        msg.setMsgReadStatus(SysConstant.MESSAGE_ALREADY_READ);
        msg.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);
        // int messageSendRequestNo = CacheHub.getInstance().obtainMsgId();
        // msg.msgId(messageSendRequestNo);
        CacheHub.getInstance().pushMsg(msg);

        addItem(msg);

        List<MessageInfo> messageList = new ArrayList<MessageInfo>();
        messageList.add(msg);

        if (imService != null) {
            imService.getMessageManager().sendImages(session.getSessionId(),
                    session.getSessionType(), messageList);
        }
    }

    /**
     * @Description 录音结束后处理录音数据
     * @param audioLen
     */
    private void onRecordVoiceEnd(float audioLen) {
        logger.d("messageactivity#chat#audio#onRecordVoiceEnd audioLen:%f", audioLen);

        // User chatUser = CacheHub.getInstance().getChatUser();
        // if (chatUser == null) {
        // Toast.makeText(MessageActivity.this,
        // getResources().getString(R.string.link_is_connecting),
        // Toast.LENGTH_LONG).show();
        //
        // return;
        // }
        // 语音时长

        int tLen = (int) (audioLen + 0.5);
        tLen = tLen < 1 ? 1 : tLen;
        if (tLen < audioLen) {
            ++tLen;
        }

        // 绘制到界面（不关心语音内容）
        MessageInfo msg = new MessageInfo();
        msg.setDisplayType(SysConstant.DISPLAY_TYPE_AUDIO);
        msg.setMsgFromUserId(CacheHub.getInstance().getLoginUserId());
        msg.setIsSend(true);
        msg.setTargetId(session.getSessionId());
        msg.setMsgCreateTime((int) (System.currentTimeMillis() / 1000));
        msg.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);
        msg.setSavePath(audioSavePath);
        msg.setMsgContent("");
        msg.setMsgAttachContent("");
        msg.setMsgReadStatus(SysConstant.MESSAGE_ALREADY_READ);
        msg.setPlayTime(tLen);
        MessageHelper.setMsgAudioContent(msg);

        byte msgType = SysConstant.MESSAGE_TYPE_AUDIO;
        msg.setMsgType(msgType);

        // int messageSendRequestNo = CacheHub.getInstance().obtainMsgId();
        // msg.setMsgId(messageSendRequestNo);
        CacheHub.getInstance().pushMsg(msg);

        addItem(msg);

        imServiceHelper.getIMService().getMessageManager()
                .sendVoice(msg.getTargetId(), msg.getAudioContent(), session.getSessionType(), msg);

        // 设置语音内容,发送
        // if (StateManager.getInstance().isOnline()) {
        // SendAudioMessageTask sendTask = new SendAudioMessageTask(
        // MessageActivity.this, msg, audioSavePath, tLen);
        // sendTask.setCallBack(new TaskCallback() {
        // @Override
        // public void callback(Object result) {
        // if (result == null) {
        // Toast.makeText(
        // MessageActivity.this,
        // getResources().getString(
        // R.string.write_audio_file_failed),
        // Toast.LENGTH_LONG).show();
        // }
        // }
        // });
        // TaskManager.getInstance().trigger(sendTask);
        // } else {
        // Toast.makeText(MessageActivity.this,
        // getResources().getString(R.string.disconnected_by_server),
        // Toast.LENGTH_LONG).show();
        // }
    }

    @Override
    protected void onDestroy() {
        logger.d("messageactivity#onDestroy:%s", this);
        imServiceHelper.disconnect(this);

        unregistEvents();
        adapter.clearItem();

        if (newSessionInfo != null) {
            IMUIHelper.openSessionChatActivity(logger, this, newSessionInfo.getSessionId(),
                    newSessionInfo.getSessionType(), imService);
        }

        super.onDestroy();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
    }

    @Override
    public void onPullDownToRefresh(
            final PullToRefreshBase<ListView> refreshView) {
        // 获取消息
        refreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // User chatUser = CacheHub.getInstance().getChatUser();
                // if (chatUser == null
                // || CacheHub.getInstance().getLoginUser() == null) {
                // return;
                // }
                // MessageInfo msgInfo = null;
                // for (int i = 0; i < adapter.getCount(); i++) {
                // if (adapter.getItem(i) instanceof MessageInfo) {
                // msgInfo = (MessageInfo) adapter.getItem(i);
                // break;
                // }
                // }

                // todo eric what is startMsgId is here?
                // int startMsgId = (null == msgInfo) ? 0 : msgInfo.getMsgId();
                // // 如果为空，则从最近一条消息拉取
            	
            	//case would be:
            	//open the window, no history message, so firstHistoryMsgTime is still <= 0
            	//then we send a message to the peer, this message has been written into db
            	//then we pull and refresh, so we load history messages from time 0, so 1 message
            	// has been read, but there's already 1 message inside adapter's message list.
            	//so 2 same messages in the chat dialog
            	if (firstHistoryMsgTime <= 0 && adapter.getCount() > 0) {
            		adapter.clearItem();
            	}
                List<MessageInfo> historyMsgInfo = loadHistory();

                adapter.addItem(true, historyMsgInfo);
                adapter.notifyDataSetChanged();
                ListView mlist = lvPTR.getRefreshableView();
                if (!(mlist).isStackFromBottom()) {
                    mlist.setStackFromBottom(true);
                }
                mlist.setStackFromBottom(false);
                refreshView.onRefreshComplete();
            }
        }, 200);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.left_btn) {
            MessageActivity.this.finish();
        } else if (id == R.id.right_btn) {
            showGroupManageActivity(true);
        } else if (id == R.id.show_add_photo_btn) {

            if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                addOthersPanelView.setVisibility(View.GONE);
            } else if (addOthersPanelView.getVisibility() == View.GONE) {
                addOthersPanelView.setVisibility(View.VISIBLE);
                inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
            }
            if (null != emoGridView
                    && emoGridView.getVisibility() == View.VISIBLE) {
                emoGridView.setVisibility(View.GONE);
            }

            scrollToBottomListItem();

        } else if (id == R.id.take_photo_btn) {
            if (albumList.size() < 1) {
                Toast.makeText(MessageActivity.this,
                        getResources().getString(R.string.not_found_album), Toast.LENGTH_LONG)
                        .show();
                return;
            }
            Intent intent = new Intent(MessageActivity.this, PickPhotoActivity.class);
            intent.putExtra(SysConstant.EXTRA_CHAT_USER_ID, session.getSessionId());
            startActivityForResult(intent, SysConstant.ALBUM_BACK_DATA);
            MessageActivity.this.overridePendingTransition(R.anim.tt_album_enter, R.anim.tt_stay);

            addOthersPanelView.setVisibility(View.GONE);

            scrollToBottomListItem();
        } else if (id == R.id.take_camera_btn) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoSavePath = CommonUtil.getImageSavePath(String.valueOf(System
                    .currentTimeMillis())
                    + ".jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(takePhotoSavePath)));
            startActivityForResult(intent, SysConstant.CAMERA_WITH_DATA);
            addOthersPanelView.setVisibility(View.GONE);

            scrollToBottomListItem();

        } else if (id == R.id.show_emo_btn) {
            inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
            if (emoGridView.getVisibility() == View.GONE) {
                emoGridView.setVisibility(View.VISIBLE);
            } else if (emoGridView.getVisibility() == View.VISIBLE) {
                emoGridView.setVisibility(View.GONE);
            }

            if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                addOthersPanelView.setVisibility(View.GONE);
            }

            scrollToBottomListItem();

        } else if (id == R.id.send_message_btn) {
            // todo eric
            // if (!StateManager.getInstance().isOnline()) {
            // Toast.makeText(
            // MessageActivity.this,
            // getResources().getString(
            // R.string.disconnected_by_server),
            // Toast.LENGTH_LONG).show();
            //
            // return;
            // }

            logger.d("messageactivity#send btn clicked");

            String content = messageEdt.getText().toString();
            logger.d("messageactivity#chat content:%s", content);
            if (content.trim().equals("")) {
                Toast.makeText(MessageActivity.this,
                        getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
                return;
            }
            MessageInfo msg = MessageHelper.obtainTextMessage(session.getSessionId(), content);
            if (msg != null) {
                addItem(msg);

                if (session != null) {
                    session.sendText(session.getSessionType(), msg);
                }

                messageEdt.setText("");
                // emoGridView.setVisibility(View.GONE);
            }

            scrollToBottomListItem();

        } else if (id == R.id.voice_btn) {
            inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
            messageEdt.setVisibility(View.GONE);
            audioInputImg.setVisibility(View.GONE);
            recordAudioBtn.setVisibility(View.VISIBLE);
            keyboardInputImg.setVisibility(View.VISIBLE);
            emoGridView.setVisibility(View.GONE);
            addOthersPanelView.setVisibility(View.GONE);
            addEmoBtn.setVisibility(View.GONE);
            messageEdt.setText("");
        } else if (id == R.id.show_keyboard_btn) {
            recordAudioBtn.setVisibility(View.GONE);
            keyboardInputImg.setVisibility(View.GONE);
            messageEdt.setVisibility(View.VISIBLE);
            audioInputImg.setVisibility(View.VISIBLE);
            addEmoBtn.setVisibility(View.VISIBLE);
        } else if (id == R.id.message_text) {
            if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                addOthersPanelView.setVisibility(View.GONE);
            }
            emoGridView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        scrollToBottomListItem();
        if (id == R.id.record_voice_btn) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                if (AudioPlayerHandler.getInstance().isPlaying())
                    AudioPlayerHandler.getInstance().stopPlayer();
                y1 = event.getY();
                recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_pressed);
                recordAudioBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.release_to_send_voice));

                soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
                soundVolumeImg.setVisibility(View.VISIBLE);
                soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_default_bk);
                soundVolumeDialog.show();
                audioSavePath = CommonUtil
                        .getAudioSavePath(CacheHub.getInstance().getLoginUserId());
                audioRecorderInstance = new AudioRecordHandler(audioSavePath, new TaskCallback() {

                    @Override
                    public void callback(Object result) {
                        logger.d("messageactivity#audio#in callback");
                        if (audioReday) {
                            if (msgHandler != null) {
                                logger.d("messageactivity#audio#send record finish message");

                                Message msg = uiHandler.obtainMessage();
                                msg.what = HandlerConstant.HANDLER_RECORD_FINISHED;
                                msg.obj = audioRecorderInstance.getRecordTime();
                                uiHandler.sendMessage(msg);
                            }
                        }
                    }
                });
                audioRecorderThread = new Thread(audioRecorderInstance);
                audioReday = false;
                audioRecorderInstance.setRecording(true);
                logger.d("messageactivity#audio#audio record thread starts");
                audioRecorderThread.start();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                y2 = event.getY();
                if (y1 - y2 > 50) {
                    soundVolumeImg.setVisibility(View.GONE);
                    soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_cancel_bk);
                } else {
                    soundVolumeImg.setVisibility(View.VISIBLE);
                    soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_default_bk);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                // if (!StateManager.getInstance().isOnline()) {
                // Toast.makeText(
                // MessageActivity.this,
                // getResources().getString(
                // R.string.disconnected_by_server),
                // Toast.LENGTH_LONG).show();
                // if (soundVolumeDialog.isShowing()) {
                // soundVolumeDialog.dismiss();
                // }
                // return false;
                // }
                if (audioRecorderInstance.isRecording()) {
                    audioRecorderInstance.setRecording(false);
                }
                if (soundVolumeDialog.isShowing()) {
                    soundVolumeDialog.dismiss();
                }
                recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal);
                recordAudioBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.tip_for_voice_forward));
                if (y1 - y2 <= 50) {
                    if (audioRecorderInstance.getRecordTime() >= 0.5) {
                        if (audioRecorderInstance.getRecordTime() < SysConstant.MAX_SOUND_RECORD_TIME) {
                            audioReday = true;
                        }
                    } else {
                        soundVolumeImg.setVisibility(View.GONE);
                        soundVolumeLayout
                                .setBackgroundResource(R.drawable.tt_sound_volume_short_tip_bk);
                        soundVolumeDialog.show();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                if (soundVolumeDialog.isShowing())
                                    soundVolumeDialog.dismiss();
                                this.cancel();
                            }
                        }, 700);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        logger.d("messageactivity#onStop:%s", this);

        if (null != adapter) {
            adapter.hidePopup();
        }
        if (AudioPlayerHandler.getInstance().isPlaying())
            AudioPlayerHandler.getInstance().stopPlayer();

        super.onStop();
    }

    @Override
    protected void onStart() {
        logger.d("messageactivity#onStart:%s", this);
        super.onStart();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            sendBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEdt
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.send_message_btn);
            addPhotoBtn.setVisibility(View.GONE);
        } else {
            addPhotoBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEdt
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.show_add_photo_btn);
            sendBtn.setVisibility(View.GONE);
        }
    }

    /**
     * @Description 滑动到列表底部
     */
    private void scrollToBottomListItem() {
        logger.d("messageactivity#scrollToBottomListItem");

        // todo eric, why use the last one index + 2 can real scroll to the
        // bottom?
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {
            lv.setSelection(adapter.getCount() + 1);
        }
    }

    @Override
    protected void onPause() {
        logger.d("messageactivity#onPause:%s", this);
        super.onPause();

        imServiceConnectionEnabled = false;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent arg0) {
        try {
            if (!adapter.isAudioPlaying()) {
                return;
            }
            float range = arg0.values[0];
            if (null != sensor && range == sensor.getMaximumRange()) {
                if (audioPlayMode == AudioManager.MODE_NORMAL) {
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    if (preAudioPlayMode == AudioManager.MODE_IN_CALL) {
//                        SpeekerToast.show(MessageActivity.this,
//                                MessageActivity.this.getText(R.string.audio_in_speeker), 1000);
                    }

                    preAudioPlayMode = AudioManager.MODE_NORMAL;
                }
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                if (audioPlayMode!=AudioManager.MODE_IN_CALL) {
//                    SpeekerToast.show(MessageActivity.this,MessageActivity.this.getText(R.string.audio_in_call), 1000);
                }
                preAudioPlayMode = AudioManager.MODE_IN_CALL;
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * @Description 设置听筒模式
     * @param mode
     */
    public static void setAudioMode(int mode) {
        if (mode != SysConstant.AUDIO_PLAY_MODE_NORMAL
                && mode != SysConstant.AUDIO_PLAY_MODE_IN_CALL) {
            return;
        }
        audioPlayMode = mode;
        audioManager.setMode(audioPlayMode);
    }

    public static int getAudioMode() {
        return audioPlayMode;
    }

    public static Handler getUiHandler() {
        return uiHandler;
    }

    public static Handler getMsgHandler() {
        return msgHandler;
    }

}
