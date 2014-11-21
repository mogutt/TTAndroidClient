
package com.mogujie.tt.service;

//import com.mogujie.tt.conn.ReconnectManager;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

public class IMService extends Service {
    private ConnectionChangeReceiver connectChangeReciver = null;
    //ReconnectManager //rmInstance = ReconnectManager.getInstance();
    //StateManager smInstance = StateManager.getInstance();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        connectChangeReciver = new ConnectionChangeReceiver();
        registerReceiver(connectChangeReciver, filter);

        start();
    }

    public void start() {
        //rmInstance.startReconnctManager(IMService.this);
        //smInstance.startTimer();
    }

    public void stop() {
        //rmInstance.stopReconnctManager();
        //smInstance.stopTimer();
    }

    @Override
    public void onDestroy()
    {
        stop();
        unregisterReceiver(connectChangeReciver);
    }

}
