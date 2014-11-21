
package com.mogujie.tt.config;

public class HandlerConstant {
    public static final int REQUEST_LOGIN_SUCCESS = 1; // 登陆Login Server成功
    public static final int REQUEST_LOGIN_FAILED = 2;// 登陆Login server失败

    public static final int REQUEST_MESSAGE_SERVER_SUCCESS = 3; // 请求Msg server
                                                                // 地址成功
    public static final int REQUEST_MESSAGE_SERVER_FAILED = 4; // 请求Msg Server
                                                               // 地址失败

    public static final int RECEIVE_UNREAD_MESSAGE = 5; // 收到未读消息
    public static final int RECEIVE_OLD_MESSAGE = 6; // 收到历史消息
    // public static final int RECEIVE_MSG_DATA_ACK= 7;// 服务端收到消息确认
    // public static final int RECEIVE_MSG_DATA = 8;// 收到消息
    public static final int HANDLER_SEND_MESSAGE_TIMEOUT = 9; // 发送消息超时

    public static final int HANDLER_STOP_PLAY = 10;// Speex 通知主界面停止播放

    public static final int HANDLER_CONNECT_SUCESS = 11;// socket连接成功
    public static final int HANDLER_DISCONNECT = 12; // socket 断开链接

    // public static final int HANDLER_GET_USER_INFO = 13;// 获取用户信息

    public static final int HANDLER_LOGIN_KICK = 14; // 用户被踢

    public static final int HANDLER_LOGIN_MSG_SERVER = 15;// 登陆到MsgServer

    public static final int HANDLER_LOGIN_MSG_SERVER_FAILED = 155;

    public static final int HANDLER_LOGIN_MSG_SERVER_TIMEOUT = 156;

    public static final int HANDLER_RECV_CONTACTLIST = 16; // 获取最近联系人列表
    public static final int HANDLER_RECV_UNREAD_MSG_COUNT = 17; // 获取未读消息计数

    // public static final int HANDLER_GET_CUSTOM_SERVICE = 18; // 由店铺获取客服信息

    // 网络状态机对外提供状态
    public static final int HANDLER_NET_STATE_CONNECTED = 19;// 连接上
    public static final int HANDLER_NET_STATE_DISCONNECTED = 20; // 断开

    // 图片上传成功
    public static final int HANDLER_IMAGE_UPLOAD_SUCESS = 21;
    // 图片上传失败
    public static final int HANDLER_IMAGE_UPLOAD_FAILD = 22;

    public static final int HANDLER_IMAGE_MESSAGE_DOWNLOAD_SUCCESS = 23; // 图片信息下载完成

    public static final int HANDLER_RECORD_FINISHED = 24; // 录音结束

    public static final int HANDLER_CANCEL_SELECTED = 25;

    public static final int SHOULD_BLOCK_USER = 26;

    public static final int SHOULD_NOT_BLOCK_USER = 27;

    public static final int START_BLOCK_CHECK = 28;

    public static final int SET_TITLE = 29;

    public static final int RECEIVE_MAX_VOLUME = 30;

    public static final int RECORD_AUDIO_TOO_LONG = 31;

    public static final int REQUEST_CUSTOM_SERVICE_FAILED = 32;

    // 联系人信息加载成功
    public static final int HANDLER_CONTACTS_TO_LOAD = 101; // 加载联系人
    public static final int HANDLER_CONTACTS_NEW_MESSAGE_COME = 103; // 通知联系人界面有新消息来了
    public static final int HANDLER_MESSAGES_NEW_MESSAGE_COME = 104; // 通知聊天界面有新消息来了
    public static final int HANDLER_CONTACTS_TO_REFRESH = 107; // 通知联系人数据变化
    public static final int HANDLER_CONTACTS_REQUEST_TIMEOUT = 108;

    public static final int HANDLER_SEND_MESSAGE_FAILED = 110;

    public static final int HANDLER_CHANGE_CONTACT_TAB = 111;

}
