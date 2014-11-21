
package com.mogujie.tt.packet.action;

import com.mogujie.tt.packet.base.Packet;

/**
 * Action的基类，子类可以各自需要添加成员函数与变量
 * 
 * @author dolphinWang
 * @time 2014/04/30
 */
public class Action {

    /**
     * 默认的超时时间,15秒
     */
	//todo eric
    public static final int DEFAULT_TIME_OUT = 35000;

    private Action(ActionCallback callback, Packet packet, int timeout,
            long timeStamp, int repeatCount) {
        mCallback = callback;
        mPacket = packet;
        mTimeout = timeout;
        mTimeStamp = timeStamp;
        mRepeatCountIfFaild = repeatCount;
    }

    protected ActionCallback mCallback;

    protected Packet mPacket;

    /**
     * 这个Action的校验号
     */
    private int mSequenceNo;

    private int mTimeout;

    private long mTimeStamp;

    private int mRepeatCountIfFaild; // 发送失败或超时重试次数

    /**
     * 外部不要主动调用这个函数，在被提交到消息队列的时候，队列会分配sequence给每一个action
     * 
     * @param squenceNo
     */
    public void setSquenceNo(int squenceNo) {
        mSequenceNo = squenceNo;
    }

    public int getSequenceNo() {
        return mSequenceNo;
    }

    public ActionCallback getCallback() {
        return mCallback;
    }

    public Packet getPacket() {
        return mPacket;
    }

    public int getTimeout() {
        return mTimeout;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public int getmRepeatCountIfFaild() {
        return mRepeatCountIfFaild;
    }

    public int minusRepeatCountIfFaild() {
        mRepeatCountIfFaild--;
        return mRepeatCountIfFaild;
    }

    public void setmRepeatCountIfFaild(int mRepeatCountIfFaild) {
        this.mRepeatCountIfFaild = mRepeatCountIfFaild;
    }

    /**
     * 可以用这个类构造出一个Action。如果有子类继承了Action，可以在子类中继承Builder写一个Builder的子类，
     * 也可以使用工厂模式在这个Builder类中构造出Action的子类
     * 
     * @author dolphinWang
     */
    public static class Builder {

        private ActionCallback callback;

        private Packet packet;

        private int timeout = -1;

        private int repeatcountIfFaild = 0;

        public Builder setCallback(ActionCallback callback) {
            this.callback = callback;

            return this;
        }

        public Builder setPacket(Packet packet) {
            if (packet == null) {
                throw new IllegalArgumentException(
                        "An action must have a Packet!");
            }

            this.packet = packet;

            return this;
        }

        public Builder setTimeout(int timeout) {
            if (timeout <= 0) {
                throw new IllegalArgumentException(
                        "Timeout must not less than 0!");
            }

            this.timeout = timeout;

            return this;
        }

        public Builder setRepeatCountIfFaild(int count) {
            if (count < 0)
                count = 0;
            this.repeatcountIfFaild = count;
            return this;
        }

        public Action build() {
            if (timeout == -1) {
                timeout = DEFAULT_TIME_OUT;
            }

            if (repeatcountIfFaild < 0)
                repeatcountIfFaild = 0;

            return new Action(callback, packet, timeout,
                    System.currentTimeMillis(), repeatcountIfFaild);
        }
    }
}
