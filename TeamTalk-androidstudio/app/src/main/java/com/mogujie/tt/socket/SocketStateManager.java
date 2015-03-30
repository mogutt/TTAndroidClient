
package com.mogujie.tt.socket;


public class SocketStateManager {

    private boolean mbOnline = false;

    private SocketStateManager() {

    }

    private static class SingletonHolder {

        private static SocketStateManager instance = new SocketStateManager();

    }

    public static SocketStateManager getInstance() {

        return SingletonHolder.instance;

    }

    public void setState(boolean bOnline) {

        mbOnline = bOnline;

//        StateManager.getInstance().notifySocketState(bOnline);

    }

    public boolean isOnline() {

        return mbOnline;

    }

}
