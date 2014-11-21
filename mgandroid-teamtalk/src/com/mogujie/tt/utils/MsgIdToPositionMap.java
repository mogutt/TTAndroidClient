package com.mogujie.tt.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mogujie.tt.log.Logger;

public class MsgIdToPositionMap {

	private Map<String, Integer> msgIdToPositionMap = new HashMap<String, Integer>();

	public MsgIdToPositionMap() {

	}

	private Logger logger = Logger.getLogger(MsgIdToPositionMap.class);

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (Entry<String, Integer> entry : msgIdToPositionMap.entrySet()) {
			ret.append(String.format("(%s, %d) ", entry.getKey(), entry.getValue()));
		}
		
		return ret.toString();
	}

	public void put(String msgId, int pos) {
		synchronized (msgIdToPositionMap) {
			if (msgIdToPositionMap.containsKey(msgId) || pos < 0)
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

			for (Entry<String, Integer> entry : msgIdToPositionMap.entrySet()) {
				int value = entry.getValue();

				// todo eric
				// if (value >= pos) {
				if (value == pos) {
					entry.setValue(value + offset);
				}
			}
		}
	}

	/****
	 * @param msgId
	 * @return
	 */
	public int getPosition(String msgId) {
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

	public void remove(String msgId) {
		synchronized (msgIdToPositionMap) {
			if (msgIdToPositionMap.containsKey(msgId)) {
				msgIdToPositionMap.remove(msgId);
			}
		}
	}

	public Boolean contains(String msgId) {
		synchronized (msgIdToPositionMap) {
			return msgIdToPositionMap.containsKey(msgId);
		}
	}

	public void printMap() {
		synchronized (msgIdToPositionMap) {

			for (Entry<String, Integer> entry : msgIdToPositionMap.entrySet()) {
				String key = entry.getKey();
				int value = entry.getValue();
				logger.d("key = " + key + " , value = " + value);
			}
		}
	}
}
