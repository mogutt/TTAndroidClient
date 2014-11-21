package com.mogujie.tt.imlib.network;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import android.os.Handler;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.imlib.proto.PacketEncoder;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

//import com.mogujie.tt.conn.ReconnectManager;

public class SocketThread extends Thread {

	private ClientBootstrap clientBootstrap = null;

	private ChannelFactory channelFactory = null;

	private ChannelFuture channelFuture = null;

	private Channel channel = null;

	public Handler subHandler = null;

	private String strHost = null;

	private int nPort = 0;

	private static Logger logger = Logger.getLogger(SocketThread.class);

	public SocketThread(String strHost, int nPort, SimpleChannelHandler handler) {

		this.strHost = strHost;

		this.nPort = nPort;

		init(handler);

	}

	@Override
	public void run() {

		// Looper.prepare();

		// subHandler = new Handler() {

		// @Override

		// public void handleMessage(Message msg) {

		// switch (msg.what) {

		// // case MessageConstant.REQUEST_NETTY_SERVER:

		// // break;

		// default:

		// }

		// }

		// };

		doConnect();

		// Looper.loop();

	}

	private void init(final SimpleChannelHandler handler) {

		// only one IO thread
		channelFactory = new NioClientSocketChannelFactory(

		Executors.newSingleThreadExecutor(),

		Executors.newSingleThreadExecutor());

		clientBootstrap = new ClientBootstrap(channelFactory);

		clientBootstrap.setOption("connectTimeoutMillis", 5000);

		clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {

				ChannelPipeline pipeline = Channels.pipeline();

				// 接收的数据包解码

				// pipeline.addLast("decoder", new PacketDecoder());

				pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(

				400 * 1024, 0, 4, -4, 0));

				// 发送的数据包编码

				pipeline.addLast("encoder", new PacketEncoder());

				// 具体的业务处理，这个handler只负责接收数据，并传递给dispatcher

				pipeline.addLast("handler", handler);

				return pipeline;

			}

		});

		clientBootstrap.setOption("tcpNoDelay", true);

		clientBootstrap.setOption("keepAlive", true);

		// clientBootstrap.setOption("keepIdle", 20);

		// clientBootstrap.setOption("keepInterval", 5);

		// clientBootstrap.setOption("keepCount", 3);

	}

	public boolean doConnect() {

		try {

			if ((null == channel || (null != channel && !channel.isConnected()))

					&& null != this.strHost && this.nPort > 0) {

				// Start the connection attempt.

				channelFuture = clientBootstrap.connect(new InetSocketAddress(

				strHost, nPort));

				// Wait until the connection attempt succeeds or fails.

				channel = channelFuture.awaitUninterruptibly().getChannel();

				if (!channelFuture.isSuccess()) {

					channelFuture.getCause().printStackTrace();

					clientBootstrap.releaseExternalResources();

					// ReconnectManager.getInstance().setOnRecconnecting(false);

					// ReconnectManager.getInstance().setLogining(false);

					return false;

				}

			}

			// Wait until the connection is closed or the connection attemp

			// fails.

			channelFuture.getChannel().getCloseFuture().awaitUninterruptibly();

			// Shut down thread pools to exit.

			clientBootstrap.releaseExternalResources();

			return true;

		} catch (Exception e) {
			logger.e("do connect failed. e: %s", e.getStackTrace().toString());

			return false;

		}

	}

	public Channel getChannel() {

		return channel;

	}

	public void close() {

		if (null == channelFuture)

			return;

		if (null != channelFuture.getChannel()) {

			channelFuture.getChannel().close();

		}

		channelFuture.cancel();

	}

	public boolean sendPacket(Packet p) {
		Header header = p.getRequest().getHeader();
		ProtocolConstant.ProtocolDumper.dump(true, header);

		if (null != p && null != channelFuture.getChannel()) {

			channelFuture.getChannel().write(p);

			logger.d("packet#send ok");
			return true;

		} else {

			logger.e("packet#send failed");
			return false;

		}

	}

}
