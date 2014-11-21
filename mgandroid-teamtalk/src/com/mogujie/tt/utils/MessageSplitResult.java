
package com.mogujie.tt.utils;

import java.util.LinkedList;
import java.util.List;

import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.log.Logger;

public class MessageSplitResult {

    public MessageSplitResult(MessageInfo info, byte[] content) {

        originMsgInfo = info;
        originContent = content;
        if (CacheHub.getInstance().getChatUser() != null) {
            logger.d("target user id = "
                    + CacheHub.getInstance().getChatUser().getUserId()
                    + " name = "
                    + CacheHub.getInstance().getChatUser().getName());
        }
    }

    private Logger logger = Logger.getLogger(MessageSplitResult.class);

    private boolean isTextMsgType(byte msgType) {
    	return (msgType == ProtocolConstant.MSG_TYPE_GROUP_TEXT || msgType == ProtocolConstant.MSG_TYPE_P2P_TEXT || msgType == ProtocolConstant.MSG_TYPE_GROUP_TEXT_FOR_HISTORY_REASON_COMPATIBILITY);
    }
    
    private boolean isAudioMsgType(byte msgType) {
    	return (msgType == ProtocolConstant.MSG_TYPE_GROUP_AUDIO || msgType == ProtocolConstant.MSG_TYPE_P2P_AUDIO);
    }
    public void decode() {
    	byte msgType = originMsgInfo.getMsgType();
        if (isAudioMsgType(msgType)) {
            setAudioDetail();
        } else if (isTextMsgType(msgType)) {
            split();
        }
    }

    private void split() {
        if (originMsgInfo == null || originContent == null)
            return;
        String strOriginContent = new String(originContent);
        if (strOriginContent == null || strOriginContent.equals(""))
            return;

        logger.d("chat#originContent:%s",strOriginContent);
        // C++程序员的写法.... 这里是不是可以抽出一个函数，放在StringUtil里面? by语鬼
        String start = SysConstant.MESSAGE_IMAGE_LINK_START;
        String end = SysConstant.MESSAGE_IMAGE_LINK_END;

        while (!strOriginContent.isEmpty()) {

            int nStart = strOriginContent.indexOf(start);

            if (nStart < 0) {// 没有头
                String strSplitString = strOriginContent;
                addMessage(strSplitString);
                strOriginContent = "";
            } else {
                String subContentString = strOriginContent.substring(nStart);
                int nEnd = subContentString.indexOf(end);
                if (nEnd < 0) {// 没有尾
                    String strSplitString = strOriginContent;
                    addMessage(strSplitString);
                    strOriginContent = "";
                } else {// 匹配到
                    String pre = strOriginContent.substring(0, nStart);
                    addMessage(pre);

                    String matchString = subContentString.substring(0, nEnd
                            + end.length());
                    addMessage(matchString);

                    strOriginContent = subContentString.substring(nEnd
                            + end.length());
                }
            }

        }

    }

    private MessageInfo originMsgInfo = null;
    private List<MessageInfo> MsgList = new LinkedList<MessageInfo>();
    private byte[] originContent = null;
    private int nMsgParentId = -1;

    public void addMessage(String strContent) {

        if (strContent == null)
            return;

        strContent.trim();
        if (strContent.equals(""))
            return;

        MessageInfo msgInfo = new MessageInfo();
        msgInfo.copyFromOtherMsgInfo(originMsgInfo);

        if (strContent.startsWith(SysConstant.MESSAGE_IMAGE_LINK_START)
                && strContent.endsWith(SysConstant.MESSAGE_IMAGE_LINK_END)) {
            // image message

            msgInfo.setDisplayType(SysConstant.DISPLAY_TYPE_IMAGE);
            String imageUrl = strContent
                    .substring(SysConstant.MESSAGE_IMAGE_LINK_START.length());
            imageUrl = imageUrl.substring(0,
                    imageUrl.indexOf(SysConstant.MESSAGE_IMAGE_LINK_END));

            logger.d("recv an image message: image url = " + imageUrl);

            msgInfo.setUrl(imageUrl.isEmpty() ? null : imageUrl);
            msgInfo.setDisplayType(SysConstant.DISPLAY_TYPE_IMAGE);
            msgInfo.setMsgContent("");
            msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_UNLOAD);
            msgInfo.setMsgReadStatus(SysConstant.MESSAGE_UNREAD);
            msgInfo.setMsgParentId(nMsgParentId);
            // int newId =
            // IMDBManager.getInstance(Login.context).pushMsg(msgInfo);
            if (!FileUtil.isSdCardAvailuable()) {
                msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);
            }

//            int newId = CacheHub.getInstance().obtainMsgId();
//            if (newId > 0) {
//                msgInfo.setMsgId(newId);
//                CacheHub.getInstance().pushMsg(msgInfo);
//                logger.d("push to db, success");
//            }
//            if (nMsgParentId == -1) {
//                nMsgParentId = newId;
//            }

            MsgList.add(msgInfo);

        } else {
            // text message
            msgInfo.setDisplayType(SysConstant.DISPLAY_TYPE_TEXT);
            msgInfo.setMsgContent(strContent);
            msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
            msgInfo.setMsgReadStatus(SysConstant.MESSAGE_UNREAD);
            msgInfo.setMsgParentId(nMsgParentId);
            logger.d("recv a text message, content = %s", strContent);
            // int newId =
            // IMDBManager.getInstance(Login.context).pushMsg(msgInfo);
            
//            int newId = CacheHub.getInstance().obtainMsgId();
//            if (newId > 0) {
//                msgInfo.setMsgId(newId);
//                CacheHub.getInstance().pushMsg(msgInfo);
//                logger.d("push to db, success");
//            }
//            if (nMsgParentId == -1) {
//                nMsgParentId = newId;
//            }

            MsgList.add(msgInfo);
        }

    }

    public byte[] getOriginContent() {
        return originContent;
    }

    public void setOriginContent(byte[] originContent) {
        this.originContent = originContent;
    }

    private void setAudioDetail() {

        if (originContent == null)
            return;

        MessageInfo audioMessageInfo = new MessageInfo();
        audioMessageInfo.copyFromOtherMsgInfo(originMsgInfo);

        int msgLen = originContent.length;

        if (msgLen < 4) {// 错误判断
            audioMessageInfo.setSavePath("");
            audioMessageInfo.setPlayTime(0);
            audioMessageInfo.setDisplayType(SysConstant.DISPLAY_TYPE_AUDIO);
        } else {

            logger.d("recv an audio message");
            byte[] playTimeByte = new byte[4];
            byte[] audioContent = new byte[msgLen - 4];

            System.arraycopy(originContent, 0, playTimeByte, 0, 4);
            System.arraycopy(originContent, 4, audioContent, 0, msgLen - 4);

            int playTime = CommonUtil.byteArray2int(playTimeByte);

            String audioSavePath = FileUtil
                    .saveAudioResourceToFile(audioContent);
            audioMessageInfo.setPlayTime(playTime);
            audioMessageInfo.setSavePath(audioSavePath);
            audioMessageInfo.setDisplayType(SysConstant.DISPLAY_TYPE_AUDIO);

        }
        audioMessageInfo.setMsgParentId(nMsgParentId);
//        int newId = CacheHub.getInstance().obtainMsgId();
//        if (newId > 0) {
//            audioMessageInfo.setMsgId(newId);
//            CacheHub.getInstance().pushMsg(audioMessageInfo);
//        }
//
//        if (nMsgParentId == -1) {
//            nMsgParentId = newId;
//        }

        MsgList.add(audioMessageInfo);

    }

    public List<MessageInfo> getMsgList() {
        return MsgList;
    }

    public void setMsgList(List<MessageInfo> msgList) {
        MsgList = msgList;
    }
}
