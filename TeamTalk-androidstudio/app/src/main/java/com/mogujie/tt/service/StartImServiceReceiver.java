
package com.mogujie.tt.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;

/**
 * 接收登录到消息服务器的广播，启动IMService
 * 
 * @author dolphinWang
 */
public class StartImServiceReceiver extends BroadcastReceiver {

    private static Logger logger = Logger.getLogger(StartImServiceReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (!TextUtils.isEmpty(action)
                && SysConstant.START_SERVICE_ACTION.equals(action)) {
            logger.d("IM Service start!");
            Intent i = new Intent(context, IMService.class);
            context.startService(i);
        }
    }

}
