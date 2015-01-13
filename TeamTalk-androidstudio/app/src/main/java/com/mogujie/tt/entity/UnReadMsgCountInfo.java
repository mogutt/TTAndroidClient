
package com.mogujie.tt.entity;

public class UnReadMsgCountInfo {
    private String fromUserId = null;
    private int unReadCount = 0;

    public UnReadMsgCountInfo() {

    }

    public void setFromUserId(String userid) {
        fromUserId = userid;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setUnReadCount(int count) {
        unReadCount = count;
    }

    public int getUnReadCount() {
        return unReadCount;
    }
}
