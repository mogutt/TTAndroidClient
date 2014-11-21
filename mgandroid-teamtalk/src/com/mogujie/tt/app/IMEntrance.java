
package com.mogujie.tt.app;

import android.content.Context;

import com.mogujie.tt.cache.biz.CacheHub;
//import com.mogujie.tt.conn.ReconnectManager;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.socket.SocketStateManager;

public class IMEntrance {
    private static IMEntrance instance = null;
    private int serviceType;
    private Logger logger = Logger.getLogger(IMEntrance.class);
    private Context context = null;

    private IMEntrance() {
    	logger.d("created");
    }

    public static synchronized IMEntrance getInstance() {
        if (null == instance) {
            instance = new IMEntrance();
        }
        return instance;
    }

    public void killTask() {
        // rongzhi commit -----
        try {
//            StateManager.getInstance().stopTimer();
//            StateManager.getInstance().resetSockets();
            SocketStateManager.getInstance().setState(false);
            //ReconnectManager.getInstance().setPause(true);
            //LoginManager.getInstance().ClearMessageQueue();
            CacheHub.getInstance().clear();
            //TokenManager.getInstance().resetAll();
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    public void initTask(final Context cxt, final String userName, final String userPass) {
        try {
            logger.d("initTask ==>" + userName);

            this.context = cxt;
            // 以下就是为了初始化Login的handler
            //LoginManager.getInstance();
            //ReconnectManager.getInstance().setPause(false);

            //ReconnectManager.getInstance().setLogining(true);

            //LoginManager.getInstance().doLogin(context, userName, userPass);
           
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    public void setServiceType(int type) {
        serviceType = type;
    }

    public int getServiceType() {
        return serviceType;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
