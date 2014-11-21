
package com.mogujie.tt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 服务端返回的消息列表
 * @author Nana
 * @date 2014-3-15
 */
public class MessageList implements Serializable {
    private static final long serialVersionUID = 1L;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public List<MessageInfo> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<MessageInfo> messageList) {
        this.messageList = messageList;
    }

    private String targetId;
    private int messageCount;
    private List<MessageInfo> messageList = new ArrayList<MessageInfo>();
}
