
package com.mogujie.tt.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mogujie.tt.log.Logger;

public class MsgIdToPositionMap {

    private Map<Integer, Integer> msgIdToPositionMap = new HashMap<Integer, Integer>();

    public MsgIdToPositionMap() {

    }

    private Logger logger = Logger.getLogger(MsgIdToPositionMap.class);

    public void put(int msgId, int pos) {
        synchronized (msgIdToPositionMap) {
            if (msgIdToPositionMap.containsKey(msgId) || msgId < 0 || pos < 0)
                return;
            msgIdToPositionMap.put(msgId, pos);

            logger.d("put key = " + msgId + " , value = " + pos);
        }
    }

    /****
     * @Desc 修正位置，因为有些新的插入可以影响原有的位置信息
     * @param pos
     * @param offset
     */
    public void fix(int pos, int offset) {
        synchronized (msgIdToPositionMap) {

            for (Entry<Integer, Integer> entry : msgIdToPositionMap.entrySet()) {
                int value = entry.getValue();
                if (value >= pos) {
                    entry.setValue(value + offset);
                }
            }
        }
    }

    /****
     * @param msgId
     * @return
     */
    public int getPosition(int msgId) {
        synchronized (msgIdToPositionMap) {
            if (msgIdToPositionMap.containsKey(msgId)) {
                return msgIdToPositionMap.get(msgId);
            } else {
                return -1;
            }
        }
    }

    public int size() {
        synchronized (msgIdToPositionMap) {
            return msgIdToPositionMap.size();
        }
    }

    public void clear() {
        synchronized (msgIdToPositionMap) {
            msgIdToPositionMap.clear();
            logger.d("clear, now count = " + msgIdToPositionMap.size());
        }
    }

    public void remove(int msgId) {
        synchronized (msgIdToPositionMap) {
            if (msgIdToPositionMap.containsKey(msgId)) {
                msgIdToPositionMap.remove(msgId);
            }
        }
    }

    public Boolean contains(int msgId) {
        synchronized (msgIdToPositionMap) {
            return msgIdToPositionMap.containsKey(msgId);
        }
    }

    public void printMap() {
        synchronized (msgIdToPositionMap) {

            for (Entry<Integer, Integer> entry : msgIdToPositionMap.entrySet()) {
                int key = entry.getKey();
                int value = entry.getValue();
                logger.d("key = " + key + " , value = " + value);
            }
        }
    }
}
