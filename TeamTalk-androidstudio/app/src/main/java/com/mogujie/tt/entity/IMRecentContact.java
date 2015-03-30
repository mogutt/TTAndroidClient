package com.mogujie.tt.entity;

import com.mogujie.tt.cache.biz.CacheHub;

/**
 * @author shuchen
 */
public class IMRecentContact {
    protected int relateId; // 联系id

    protected String ownerId = CacheHub.getInstance().getLoginUserId(); // 用户id

    protected String userId; // 用户id

    protected String friendUserId; // 好友用户id

    private int status; // 最近联系人状态 0 正常， 1 被userId删除

    private int created; // 创建时间

    private int updated; // 更新时间

    public int getRelateId() {
        return relateId;
    }

    public void setRelateId(int relateId) {
        this.relateId = relateId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }
}
