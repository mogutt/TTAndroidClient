package com.mogujie.tt.imlib.network;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;

import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.IMPacketDispatcher;

public class MsgServerHandler extends BaseServerHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelConnected(ctx, e);

		IMLoginManager.instance().onMsgServerConnected();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.channelDisconnected(ctx, e);

		IMLoginManager.instance().onMsgServerDisconnected();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.messageReceived(ctx, e);

		IMPacketDispatcher.dispatch((ChannelBuffer) e.getMessage());
	}

	@Override
	protected void channelUnconnected() {
		// TODO Auto-generated method stub

		IMLoginManager.instance().onMessageServerUnconnected();

	}

}
