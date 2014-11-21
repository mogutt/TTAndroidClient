
package com.mogujie.tt.biz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.Packet;

/**
 * @Description 联系人界面的公用业务逻辑处理
 * @date 2014-7-12
 */
public class ContactHelper {

    private static Logger logger = Logger.getLogger(ContactHelper.class);
    private static List<RecentInfo> recentInfoList = new ArrayList<RecentInfo>();

    public ContactHelper() {
        super();
    }

    /**
     * @Description 获取排序后的联系人信息
     * @return
     */
    public static List<RecentInfo> getSortedRecentInfoList() {
        sortContactInfo();
        return recentInfoList;
    }

    /**
     * @Description 获取当前联系人信息
     * @return
     */
    public static List<RecentInfo> getRecentInfoList() {
        return recentInfoList;
    }

    /**
     * @Description 清除最近联系人
     * @return void
     */
    public static void clearRecentInfo() {
        recentInfoList.clear();
    }

//    /**
//     * @Description 请求最近联系人
//     */
//    public static void reqRecentContact(final Handler msgHandler, final Handler uiHandler) {
//        Packet packet = PacketDistinguisher.make(
//                ProtocolConstant.SID_BUDDY_LIST,
//                ProtocolConstant.CID_REQUEST_RECNET_CONTACT, null, true);
//
//        ActionCallback callback = new ActionCallback() {
//
//            @Override
//            public void onSuccess(Packet packet) {
//                sendMessageToMsgHandler(packet, msgHandler);
//            }
//
//            @Override
//            public void onTimeout(Packet packet) {
//                uiHandler.sendEmptyMessage(HandlerConstant.HANDLER_CONTACTS_REQUEST_TIMEOUT);
//            }
//
//            @Override
//            public void onFaild(Packet packet) {
//                onTimeout(packet);
//            }
//        };
//        PushActionToQueueTask task = new PushActionToQueueTask(packet, callback);
//        TaskManager.getInstance().trigger(task);
//    }

    /**
     * @Description 向msg handler发送消息
     * @param packet
     * @param msgHandler
     */
    public static void sendMessageToMsgHandler(Packet packet, Handler msgHandler) {
        if (packet == null || msgHandler == null)
            return;
        int serviceId = packet.getResponse().getHeader().getServiceId();
        int commandId = packet.getResponse().getHeader().getCommandId();
        Message msg = new Message();
        msg.what = serviceId * 1000 + commandId;
        msg.obj = packet;
        msgHandler.sendMessage(msg);
    }

    /**
     * @Description 检查联系人列表长度，如果大于定义的最大值就进行remove操作
     */
    private static void checkSizeForRecentList() {
        try {
            if (null == recentInfoList) {
                return;
            }
            int sz = recentInfoList.size();
            while (sz > SysConstant.MAX_CONTACTS_COUNT) {
                recentInfoList.remove(sz - 1);
                sz = recentInfoList.size();
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    /**
     * @Description 加载最近联系人
     * @param msgHandler
     */
    public static void loadRecentInfoList(Handler msgHandler, Context context) {
        try {
            // 1.使当前联系人列表信息和缓存中的信息保持一致
            int friendNo = CacheHub.getInstance()
                    .getFriendIdList(CacheHub.getInstance().getLoginUserId(), context).size();
            if (friendNo > recentInfoList.size()) {
                List<String> friendIds = CacheHub.getInstance().getFriendIdList(
                        CacheHub.getInstance().getLoginUserId(), context);
                addRecentContactsFromIdList(friendIds);
            }
            // 2.完善最近联系人信息，缓存中有的直接使用，没的有从服务端取
            Iterator<RecentInfo> itr = recentInfoList.iterator();
            Queue<String> userIdList = new LinkedList<String>();
            Queue<String> requestUserList = new LinkedList<String>();// 需要请求详细信息的id列表
            while (itr.hasNext()) {
                RecentInfo recentInfo = itr.next();
                String friendId = recentInfo.getUserId();
                User userInfo = CacheHub.getInstance().getUser(friendId, context);
                if (null != userInfo) {
                    recentInfo.setUserId(friendId);
                    if (!TextUtils.isEmpty(userInfo.getNickName())) {
                    	logger.d("recent#1");
                        recentInfo.setUserName(userInfo.getNickName());
                    } else if (!TextUtils.isEmpty(userInfo.getName())) {
                    	logger.d("recent#2");
                        recentInfo.setUserName(userInfo.getName());
                    } else {
                        userIdList.add(friendId);
                        logger.d("recent#3");
                        recentInfo.setUserName(friendId);
                    }
                    recentInfo.setUserAvatar(userInfo.getAvatarUrl());
                } else {
                    requestUserList.add(friendId);
                    //todo eric set user name is here
                    logger.d("recent#4");
                    recentInfo.setUserName(friendId);
                }
                MessageInfo msgInfo = CacheHub.getInstance().getLastMessage(
                        friendId);
                if (null != msgInfo) {
                    recentInfo.setLastContent(msgInfo.getMsgOverview());
                    recentInfo
                            .setLastTime(Long.valueOf(msgInfo.getUpdated() > 0 ? msgInfo
                                    .getUpdated() : msgInfo.getCreated()) * 1000);
                    recentInfo.setMsgType(msgInfo.getMsgType());
                    recentInfo.setDisplayType(msgInfo.getDisplayType());
                } else {
                    recentInfo.setLastTime(0L);
                }

                int unreadCount = CacheHub.getInstance().getUnreadCount(
                        friendId);

                recentInfo.setUnReadCount(unreadCount);
            }
            // 3.排序联系人
            sortContactInfo();
            // 4.向服务端请求信息
            if (requestUserList.size() > 0) {
            	//todo eric 
                //requestUserInfo(requestUserList, msgHandler);
            }
        } catch (Exception e) {
            logger.e(e.toString());
        }
    }

    /**
     * @Description 由id列表向联系人列表添加新的联系人对象
     * @param friendIds
     */
    private static void addRecentContactsFromIdList(List<String> friendIds) {
        if (null == friendIds) {
            return;
        }
        try {
            Iterator<String> itr = friendIds.iterator();
            while (itr.hasNext()) {
                String friendId = itr.next();
                if (CacheHub.getInstance().isLoadedFriendId(friendId)) {
                    continue; // 已经加载进最近联系人列表则忽略
                }
                RecentInfo recentInfo = new RecentInfo();
                recentInfo.setUserId(friendId);
                addNewRecentInfo(recentInfo, false); // 加在最后
                CacheHub.getInstance().setLoadedFriendId(friendId);
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    /**
     * @Description (从服务端请求用户信息成功后)更新联系人列表中的用户信息
     * @param msgHandler
     */
    public static void refreshUserInfo(Handler msgHandler, Context context) {
        if (0 == getRecentInfoList().size()) {
            return; // 如果好友列表为空，直接返回
        }
        try {
            Iterator<RecentInfo> itr = recentInfoList.iterator();
            RecentInfo recentInfo = null;
            String friendId = "";
            Queue<String> requestUserList = new LinkedList<String>();// 如果用户信息为空会一直向服务端请求，直到正常获取为止
            while (itr.hasNext()) {
                recentInfo = itr.next();
                friendId = recentInfo.getUserId();
                User userInfo = CacheHub.getInstance().getUser(friendId, context);
                if (null != userInfo) {
                    recentInfo.setUserId(friendId);
                    if (!TextUtils.isEmpty(userInfo.getNickName())) {
                        logger.d("recent#5");
                        recentInfo.setUserName(userInfo.getNickName());
                    } else if (!TextUtils.isEmpty(userInfo.getName())) {
                    	logger.d("recent#6");
                        recentInfo.setUserName(userInfo.getName());
                    } else {
                    	logger.d("recent#7");
                        recentInfo.setUserName(friendId);
                    }
                    recentInfo.setUserAvatar(userInfo.getAvatarUrl());
                } else {
                    requestUserList.add(friendId);

                }
            }
            if (requestUserList.size() > 0) {
                requestUserInfo(requestUserList, msgHandler);
            }
        } catch (Exception e) {
            logger.e(e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public static void sortContactInfo() {
        Collections.sort(recentInfoList);
    }

    /*
     * 添加一条最近联系人
     * @param recentInfo 最近联系人信息
     * @param before 是否加在最前面
     * @return void
     */
    public static void addNewRecentInfo(RecentInfo recentInfo, Boolean before) {
        if (null == recentInfo || null == recentInfo.getUserId()) {
            return;
        }
        try {
            if (before) {
                recentInfoList.add(0, recentInfo); // 加在最前
            } else {
                recentInfoList.add(recentInfoList.size(), recentInfo); // 加在最后
            }
            // hmContacts.put(recentInfo.getUserId(), true);
            checkSizeForRecentList();
        } catch (Exception e) {
            logger.e(e.toString());
        }
    }

    /**
     * @Description 请求用户信息
     * @param entityId
     * @param msgHandler
     */
    public static void requestUserInfo(Queue<String> userList, final Handler msgHandler) {
//        MessageHelper.sendTaskForUserInfo(userList, new ActionCallback() {
//
//            @Override
//            public void onSuccess(Packet packet) {
//                MessageHelper.sendMessageToMsgHandler(packet, msgHandler);
//            }
//
//            @Override
//            public void onTimeout(Packet packet) {
//            }
//
//            @Override
//            public void onFaild(Packet packet) {
//            }
//        });
    }

    /**
     * @Description 由位置信息获取到最近联系人信息
     * @param position
     * @return
     */
    public static RecentInfo getRecentInfoByPosition(int position) {
        if (position >= recentInfoList.size() || position < 0) {
            return null;
        }
        return recentInfoList.get(position);
    }

    /**
     * @Description 由最近联系人Id获取到联系人信息
     * @param userId
     * @return
     */
    public static RecentInfo getRecentInfoByUserId(String userId) {
        if (null == userId) {
            return null;
        }
        Iterator<RecentInfo> itr = recentInfoList.iterator();
        while (itr.hasNext()) {
            RecentInfo recentInfo = itr.next();
            if (userId.equals(recentInfo.getUserId())) {
                return recentInfo;
            }
        }
        return null;
    }

    /**
     * @Description 重置联系人消息状态，清空消息计数，设置DB中消息状态为已读,返回已读的消息计数
     * @param recentinfo
     * @return
     */
    public static int resetContactMessagetState(RecentInfo recentinfo) {
        if (null == recentinfo)
            return 0;
        // 设置消息计数为0
        recentinfo.setUnReadCount(0);
        int readCount = CacheHub.getInstance().clearUnreadCount(
                recentinfo.getUserId());
        // 设置当前选中用户本地DB中消息为已读
        CacheHub.getInstance().updateMsgReadStatus(
                CacheHub.getInstance().getLoginUserId(),
                recentinfo.getUserId(),
                SysConstant.MESSAGE_ALREADY_READ);
        return readCount; // 返回已读的消息计数
    }

    /*
     * 修改缓存用户信息
     */
    public static Queue<String> updateUserListInCache(List<RecentInfo> userList, Context context) {
        Queue<String> userIdList = new LinkedList<String>();
        for (int i = 0; i < userList.size(); ++i) {
            RecentInfo info = userList.get(i);
            User user = new User();
            user.setUserId(info.getUserId());
            user.setName(info.getUserName());
            user.setNickName(info.getNickName());
            user.setAvatarUrl(info.getUserAvatar());

            userIdList.add(info.getUserId());
            CacheHub.getInstance().addFriendId(info.getUserId());
            CacheHub.getInstance().setUser(user, context);
        }
        return userIdList;
    }
}
