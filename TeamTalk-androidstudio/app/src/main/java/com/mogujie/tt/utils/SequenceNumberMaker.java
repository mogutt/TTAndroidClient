
package com.mogujie.tt.utils;

/**
 * 专门用来分配序列号
 * 
 * @author dolphinWang
 * @time 2014//05/02
 */
public class SequenceNumberMaker {

    private volatile short mSquence = 0;

    private static class SingletonHolder {
        static SequenceNumberMaker maker = new SequenceNumberMaker();
    }

    private SequenceNumberMaker() {
    }

    public static SequenceNumberMaker getInstance() {
        return SingletonHolder.maker;
    }

    public short make() {
        synchronized (this) {
            mSquence++;
            if (mSquence >= Short.MAX_VALUE)
                mSquence = 1;
        }

        return mSquence;
    }
}
