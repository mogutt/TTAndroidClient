package com.mogujie.tt.imlib;

import java.util.Timer;
import java.util.TimerTask;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.conn.ConnectionStore;
import com.mogujie.tt.imlib.network.SocketThread;
import com.mogujie.tt.imlib.proto.DepartmentPacket;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.socket.MoGuSocket;

public class IMHeartBeatManager {
	// todo eric this is not a good way to keep heartbeat

	private static IMHeartBeatManager inst;

	public static IMHeartBeatManager instance() {
		synchronized (IMContactManager.class) {
			if (inst == null) {
				inst = new IMHeartBeatManager();
			}

			return inst;
		}
	}

	private Timer timer = new Timer(true);
	private int cnt = 0;
	private Logger logger = Logger.getLogger(IMHeartBeatManager.class);

	public void sendHeartbeatPriodically(int seconds) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				logger.d("heartbeat#timer cnt:%d", ++cnt);
				reqSendHeartbeat();

			}
		}, seconds * 1000, seconds * 1000);
	}

	// todo eirc receive the heartbeat packet from server, if no packet from
	// server over 30 seconds, cut the connection, reconnect
	private void reqSendHeartbeat() {
		logger.i("heartbeat#reqSendHeartbeat");

		SocketThread channel = IMLoginManager.instance().getMsgServerChannel();
		if (channel == null) {
			logger.e("contact#channel is null");
			return;
		}

		channel.sendPacket(new com.mogujie.tt.imlib.proto.HeartBeatPacket());

		logger.i("heartbeat#send packet to server");
	}

}
