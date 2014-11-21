
package com.mogujie.tt.entity;

import android.text.TextUtils;
import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;

public class SearchResultItem {
    protected String avatar; // 联系人或者群头像
    protected int defaultAvatar = R.drawable.tt_default_user_portrait_corner; // 联系人默认头像
    protected String title; // 联系人姓名或者群名
    protected String userId; // 联系人的用户id，或者群ID
    protected String nickname; // 昵称（是否需要？）
    protected String content; // 消息内容
    protected int type = SysConstant.CHAT_SEARCH_RESULT_TYPE_RESULT;// 0,结果项
                                                                    // 1,分类项

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAvatar() {
        if (TextUtils.isEmpty(avatar) || avatar.trim().length() == 0) {
            return null;
        }
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(int defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String txt) {
        this.title = txt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}
