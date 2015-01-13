
package com.mogujie.tt.conn;

import java.util.HashMap;

import com.mogujie.tt.socket.MoGuSocket;

public class ConnectionManager {
    private HashMap<String, MoGuSocket> hmSockets;

    private ConnectionManager() {
        hmSockets = new HashMap<String, MoGuSocket>();
    }

    private static class SingletonHolder {
        static ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.instance;
    }

    public void put(String key, MoGuSocket socket) {
        // 重新放入socket对象
        // 判断之前有没有socket存在，存在的话先关掉
        // MoGuSocket recentSocket = hmSockets.get(key);
        // if (recentSocket != null) {
        // recentSocket.close();
        // }
        // Logger.getLogger(ConnectionManager.class).e("Put socket:" +
        // socket.getChannel().getRemoteAddress().toString());
        hmSockets.put(key, socket);
    }

    public MoGuSocket get(String key) {
        return hmSockets.get(key);
    }

    public void remove(String key) {
        hmSockets.remove(key);
    }
}
