
package com.mogujie.tt.packet;

import android.os.Process;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.conn.ConnectionStore;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.action.Action;
import com.mogujie.tt.packet.action.ActionCallback;
import com.mogujie.tt.socket.MoGuSocket;

public class PacketSendMonitor {

    public static final int DEFAULT_PACKET_SEND_MONTOR_INTERVAL = 10;

    private Monitor mMonitor;

    private volatile boolean mNeedStop;
    private volatile boolean mStarted = false;
    private Logger logger = Logger.getLogger(PacketSendMonitor.class);

    public PacketSendMonitor(int interval) {
        if (interval <= 0) {
            mMonitor = new Monitor("DUODUO-packet-send-monitor",
                    Process.THREAD_PRIORITY_BACKGROUND,
                    DEFAULT_PACKET_SEND_MONTOR_INTERVAL);
        } else {
            mMonitor = new Monitor("DUODUO-packet-send-monitor",
                    Process.THREAD_PRIORITY_BACKGROUND, interval);
        }
        // Logger.getLogger(PacketSendMonitor.class).d("start PacketSendMonitor !!!");
        // mMonitor.start();
    }

    public synchronized void start() {
        if (mStarted)
            return;
        mNeedStop = false;
        mMonitor.start();
        Logger.getLogger(PacketSendMonitor.class).d(
                "start PacketSendMonitor !!!");
        mStarted = true;
    }

    public synchronized void stop() {
        if (mNeedStop) {
            return;
        }

        mNeedStop = true;
        mStarted = false;
    }

    private class Monitor extends Thread {

        private int mInterval;

        public Monitor(String name, int priority, int interval) {
            setName(name);
            setPriority(priority);

            mInterval = interval;
        }

        @Override
        public void run() {
            super.run();
            try {
                while (!mNeedStop) {
                    Action action = SocketMessageQueue.getInstance().getFront();
                    boolean bSuccess = false;
                    int sid = 0;
                    int cid = 0;
                    do {

                        if (action == null)
                            break;

                        final long currentTime = System.currentTimeMillis();
                        final long timeStamp = action.getTimeStamp();
                        final int timeout = action.getTimeout();

                        // 在队列中时间过长
                        if (timeStamp + timeout < currentTime) {
                            SocketMessageQueue.getInstance().pull();
                            // 重放入队列
                            if (action.minusRepeatCountIfFaild() >= 0) {
                                SocketMessageQueue.getInstance().submitAndEnqueue(action);
                            } else {

                                ActionCallback callback = action.getCallback();
                                if (callback != null) {
                                    callback.onFaild(action.getPacket());
                                }
                            }
                            break;
                        }

                        sid = action.getPacket().getRequest().getHeader()
                                .getServiceId();
                        cid = action.getPacket().getRequest().getHeader()
                                .getCommandId();
                        // 这里需要区分一下用哪一个SOCKET发送
                        MoGuSocket messageClient = ConnectionStore
                                .getInstance().get(
                                        SysConstant.CONNECT_MSG_SERVER);

                        if (sid == ProtocolConstant.SID_LOGIN
                                && cid == ProtocolConstant.CID_LOGIN_REQ_MSGSERVER) {
                            messageClient = ConnectionStore.getInstance()
                                    .get(SysConstant.CONNECT_LOGIN_SERVER);
                        } else {
                        }

                        if (messageClient == null)
                            break;

                        // 移除队列
                        SocketMessageQueue.getInstance().pull();

                        // 加入超时监视
                        if (action.getPacket().getNeedMonitor()) {
                            Logger.getLogger(PacketSendMonitor.class).d(
                                    "push an action into waiting list :seqNo = "
                                            + action.getSequenceNo());
                            SocketMessageQueue.getInstance().add2WaitingList(
                                    action);
                            Logger.getLogger(PacketSendMonitor.class).d(
                                    "push success");
                        }

                        // if((sid != ProtocolConstant.SID_MSG) ||((sid ==
                        // ProtocolConstant.SID_MSG) && (cid !=
                        // ProtocolConstant.CID_MSG_DATA))) {
                        bSuccess = messageClient.sendPacket(action.getPacket());
                        // }

                    } while (false);

                    if (bSuccess)
                        Logger.getLogger(PacketSendMonitor.class).d(
                                "send message success : sid = " + sid
                                        + " cid = " + cid + " seqNo = "
                                        + action.getSequenceNo());

                    if (action == null
                            ) {
                        sleep(mInterval);
                    }
                }
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }
    }
}
