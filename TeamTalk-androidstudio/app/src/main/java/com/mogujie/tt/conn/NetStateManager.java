
package com.mogujie.tt.conn;

public class NetStateManager {
    private boolean mbOnline = true;

    private NetStateManager() {

    }

    private static class SingletonHolder {
        private static NetStateManager instance = new NetStateManager();
    }

    public static NetStateManager getInstance() {
        return SingletonHolder.instance;
    }

    public void setState(boolean bOnline) {
        mbOnline = bOnline;
        //StateManager.getInstance().notifyNetState(bOnline);
    }

    public boolean isOnline() {
        return mbOnline;
    }
}
