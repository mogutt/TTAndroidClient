
package com.mogujie.tt.imlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.mogujie.tt.adapter.ChatAdapter;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.utils.IMContactHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.fragment.ChatFragment;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;

public class IMRecentSessionManager extends IMManager implements
        OnIMServiceListner {
    private static IMRecentSessionManager inst;

    private Logger logger = Logger.getLogger(IMRecentSessionManager.class);
    // key = contact or group id
    private ConcurrentHashMap<String, RecentInfo> recentSessionMap = new ConcurrentHashMap<String, RecentInfo>();
    private int unreadMsgTotalCnt = 0;
    private IMServiceHelper imServiceHelper = new IMServiceHelper();
    private int firstUnreadPosition = -1;

    public static IMRecentSessionManager instance() {
        synchronized (IMRecentSessionManager.class) {
            if (inst == null) {
                inst = new IMRecentSessionManager();
            }

            return inst;
        }
    }

    public void register() {
        logger.d("recent#regisgter");

        List<String> actions = new ArrayList<String>();
        actions.add(IMActions.ACTION_LOGIN_RESULT);

        imServiceHelper.registerActions(ctx, actions,
                IMServiceHelper.INTENT_NO_PRIORITY, this);
    }

    public void addRecentSession(RecentInfo session) {
        logger.d("recent#addRecentSession -> session:%s", session);

        recentSessionMap.put(session.getEntityId(), session);
    }

    private RecentInfo createRecentInfo(MessageInfo msg) {
        if (msg.sessionType == IMSession.SESSION_P2P) {
            ContactEntity contact = IMContactManager.instance().findContact(
                    msg.sessionId);
            if (contact == null) {
            	logger.e("recent#no such contact -> id:%s", msg.sessionId);
            	return null;
            }
            return IMContactHelper.convertContactEntity2RecentInfo(contact,
                    msg.createTime);
        } else {
            GroupEntity group = IMGroupManager.instance().findGroup(
                    msg.sessionId);
            if (group == null) {
            	logger.e("recent#no such group -> id:%s", msg.sessionId);
            	return null;
            }

            return IMContactHelper.convertGroupEntity2RecentInfo(group);
        }
    }

    public void update(MessageInfo msg) {
        logger.d("recent#update msg:%s", msg);
        if (msg == null) {
            return;
        }

        RecentInfo recentInfo = recentSessionMap.get(msg.sessionId);
        if (recentInfo == null) {
            recentInfo = createRecentInfo(msg);
            if (recentInfo == null) {
            	logger.e("recent#update failed for msg:%s", msg);
            	return;
            }
        }

        recentInfo.setLasttime(msg.createTime);
        recentInfo.setLastContent(getMessageEntityDescription(msg));

        if (!msg.isMyMsg()) {
            incUnreadMsgCnt(recentInfo);
        }
        recentSessionMap.put(msg.sessionId, recentInfo);
        IMRecentSessionManager.instance().broadcast();
    }

    private String getMessageEntityDescription(MessageInfo msg) {
        if (msg == null) {
            return "";
        }
        
        String result = getMessageBasicDescrption(msg);
        
        if (msg.sessionType == IMSession.SESSION_P2P) {
        	return result;
        } else {
        	ContactEntity talkerContact = IMContactManager.instance().findContact(msg.fromId);
        	if (talkerContact == null) {
        		logger.e("recent#no such contact -> id:%s", msg.fromId);
        		return result;
        	}
        	
//        	if (talkerContact.id.equals(IMLoginManager.instance().getLoginId())) {
//        		logger.d("recent#this is the current login uesr");
//        		return result;
//        	}
        	
        	return String.format("%s: %s", talkerContact.name, result);
        }
    }
    
    private String getMessageBasicDescrption(MessageInfo msg) {
    	if (msg.isTextType()) {
            return msg.getMsgContent();
        } else if (msg.isAudioType()) {
            // todo eric i18n
            return "[语音]";
        } else if (msg.isImage()) {
            return "[图片]";
        }
		return "";
    }

    private synchronized void incUnreadMsgCnt(RecentInfo recentInfo) {
        recentInfo.incUnreadCount();
        unreadMsgTotalCnt++;
    }

    // public synchronized void decUnreadMsgCnt(String sessionId) {
    // RecentInfo recentInfo = recentSessionMap.get(sessionId);
    // if (recentInfo == null) {
    // logger.e(
    // "recent:decUnreadMsgCnt didn't find recentinfo by sessionId:%d",
    // sessionId);
    // return;
    // }
    //
    // recentInfo.decUnreadCount();
    // unreadMsgTotalCnt--;
    // }

    public synchronized void resetUnreadMsgCnt(String sessionId) {
        logger.d("recent#resetUnreadMsgCnt -> sessionId:%s", sessionId);

        RecentInfo recentInfo = recentSessionMap.get(sessionId);
        if (recentInfo == null) {
            logger.e(
                    "resetUnreadMsgCnt didn't find recentinfo by sessionId:%s",
                    sessionId);
            return;
        }

        int oldMsgCnt = recentInfo.getUnReadCount();
        logger.d("unread#oldMsgCnt:%d", oldMsgCnt);

        unreadMsgTotalCnt -= oldMsgCnt;
        recentInfo.setUnReadCount(0);

        broadcast();
    }

    public int getTotalUnreadMsgCnt() {
        return unreadMsgTotalCnt;
    }

    public List<RecentInfo> getRecentSessionList() {
        // todo eric every time it has to sort, kind of inefficient, change it
        ArrayList<RecentInfo> recentInfoList = new ArrayList<RecentInfo>(
                recentSessionMap.values());
        Collections.sort(recentInfoList);
        
        setFirstUnreadPosition(recentInfoList);
        
        return recentInfoList;
    }

    private void setFirstUnreadPosition(ArrayList<RecentInfo> recentInfoList) {
        for(int i=0;i<recentInfoList.size();++i){
            if (recentInfoList.get(i).getUnreadCount()>0) {
                    firstUnreadPosition=i;
                    break;
            }
        }
    }

    public int getFirstUnreadPosition() {
        return firstUnreadPosition;
    }

    public void broadcast() {
        logger.d("recent#triggerDataSetChanged");
        if (ctx != null) {
            ctx.sendBroadcast(new Intent(
                    IMActions.ACTION_ADD_RECENT_CONTACT_OR_GROUP));
            logger.d("recent#send new recent contact");
        }

    }

    @Override
    public void onAction(String action, Intent intent,
            BroadcastReceiver broadcastReceiver) {
        if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
            handleLoginResultAction(intent);
        }
    }

    private void handleLoginResultAction(Intent intent) {
        logger.d("recent#handleLoginResultAction");
        int errorCode = intent
                .getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

        if (errorCode == ErrorCode.S_OK) {
            onLoginSuccess();
        }
    }

    private void onLoginSuccess() {
        logger.d("recent#onLogin Successful");

        // when reconnecting ok, we reset the unread Msg cnt
        unreadMsgTotalCnt = 0;
        recentSessionMap.clear();
    }

    @Override
    public void onIMServiceConnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        recentSessionMap.clear();
        unreadMsgTotalCnt = 0;

    }
}
