package com.mogujie.tt.imlib.network;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.IMPacketDispatcher;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;

public class LoginServerHandler extends SimpleChannelHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelConnected(ctx, e);

		IMLoginManager.instance().onLoginServerConnected();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.channelDisconnected(ctx, e);
		
		IMLoginManager.instance().onLoginServerDisconnected();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.messageReceived(ctx, e);

		IMPacketDispatcher.dispatch((ChannelBuffer) e.getMessage());
	}

}
