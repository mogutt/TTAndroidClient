
package com.mogujie.tt.conn;

import java.util.concurrent.ConcurrentHashMap;

import com.mogujie.tt.socket.MoGuSocket;

public class ConnectionStore {

    private ConcurrentHashMap<String, MoGuSocket> hmSockets = null;

    private ConnectionStore() {
        hmSockets = new ConcurrentHashMap<String, MoGuSocket>();
    }

    private static class SingletonHolder {
        static ConnectionStore instance = new ConnectionStore();
    }

    public static ConnectionStore getInstance() {
        return SingletonHolder.instance;
    }

    public void put(String key, MoGuSocket socket) {
        hmSockets.put(key, socket);
    }

    public MoGuSocket get(String key) {
        return hmSockets.get(key);
    }

    public void remove(String key) {
        hmSockets.remove(key);
    }
}
