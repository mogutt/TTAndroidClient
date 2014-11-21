
package com.mogujie.tt.entity;

import java.util.Date;

import android.text.TextUtils;
import android.util.Log;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.DateUtil;

/**
 * @author seishuchen
 */
@SuppressWarnings("rawtypes")
public class RecentInfo implements Comparable<RecentInfo> {

	private Logger logger = Logger.getLogger(RecentInfo.class);

    // protected String selfUserId; // 当前用户id

    protected String avatar; // 最近联系人头像

    public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public int getSessionType() {
		return sessionType;
	}

	public void setSessionType(int sessionType) {
		this.sessionType = sessionType;
	}

	public long getLasttime() {
		return lasttime;
	}

	public void setLasttime(long lasttime) {
		this.lasttime = lasttime;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getUnReadCount() {
		return unReadCount;
	}

	private int defaultAvatar = R.id.contact_portrait; // 最近联系人默认头像

    protected String name; // 最近联系人姓名

    protected String entityId; // 最近联系人的用户id

    private byte msgType; // 最近一条消息类型
    
    private int sessionType;

    private int displayType = SysConstant.DISPLAY_TYPE_TEXT; // 消息展示类型

    private String lastContent; // 与最近联系人聊天的最近一条消息内容

    private long lasttime; // 与最近联系人聊天的最近一次时间

    private int unReadCount = 0; // 未读消息计数

    private Date date;

    private String nickname;

    @Override
	public String toString() {
		return "RecentInfo [avatar=" + avatar + ", defaultAvatar="
				+ defaultAvatar + ", name=" + name + ", entityId=" + entityId
				+ ", msgType=" + msgType + ", sessionType=" + sessionType
				+ ", displayType=" + displayType + ", lastContent="
				+ lastContent + ", lasttime=" + lasttime + ", unReadCount="
				+ unReadCount + ", date=" + date + ", nickname=" + nickname
				+ "]";
	}

	public String getUserId() {
        return entityId;
    }

    public void setUserId(String userId) {
    	
//    	logger.d("stack:%s",Log.getStackTraceString(new Throwable()));
        this.entityId = userId;
        logger.d("recent#setUserId -> userId:%s", userId);
    }

    // public String getSelfUserId() {
    // return selfUserId;
    // }
    //
    // public void setSelfUserId(String selfUserId) {
    // this.selfUserId = selfUserId;
    // }

    public String getUserName() {
        return name;
    }

    public void setUserName(String uname) {
        this.name = uname;
        logger.d("recent#setuserName -> uname:%s", uname);
    }

    public String getUserAvatar() {
        if (TextUtils.isEmpty(avatar) || avatar.trim().length() == 0) {
            return null;
            // return SysConstant.DETAULT_PORTRAIT_URL;
        }
        return avatar;
    }

    public void setUserAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(int defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgRenderType) {
        this.msgType = msgRenderType;
    }

    public int getDisplayType() {
        return displayType;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }

    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    public long getLastTime() {
        return lasttime;
    }

    public String getLastTimeString() {
        if (0L == lasttime) {
            return " ";
        }
                
        date = new Date(lasttime*1000);
        return DateUtil.getTimeDisplay(date);
    }

    public void setLastTime(long lasttime) {
        this.lasttime = lasttime;
    }

    public void setLastTime(Date date) {
        this.lasttime = date.getTime();
    }

    /*
     * 未读消息计数加1
     */
    public int incUnreadCount() {
        unReadCount += 1;
        return unReadCount;
    }

    /*
     * 未读消息计数减1
     */
    public int decUnreadCount() {
        unReadCount -= 1;
        return unReadCount;
    }

    public int getUnreadCount() {
        return unReadCount;
    }

    public String getUnreadCountString() {
        if (99 < unReadCount) {
            return "99+";
        }
        return unReadCount + "";
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }
    

    @Override
	public int compareTo(RecentInfo another) {
    	 // 先按最近联系时间排序
        if (getLastTime() < another.getLastTime()) {
            return 1;
        }
        if (getLastTime() > another.getLastTime()) {
            return -1;
        }
		return 0;
	}

	
    public void setNickName(String NickName) {
        nickname = NickName;
    }

    public String getNickName() {
        return nickname;
    }

}
