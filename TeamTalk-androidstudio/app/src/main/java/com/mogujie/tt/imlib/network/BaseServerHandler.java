package com.mogujie.tt.imlib.network;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.mogujie.tt.log.Logger;

public abstract class BaseServerHandler extends SimpleChannelHandler {

	protected boolean connected = false;
	private Logger logger = Logger.getLogger(BaseServerHandler.class);

	protected abstract void channelUnconnected();
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelConnected(ctx, e);

		logger.d("channel#channelConnected");
		connected = true;
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.channelDisconnected(ctx, e);

		logger.d("channel#channelDisconnected");
		connected = false;
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, e);

		logger.e("channel#exceptionCaught:%s", e.getCause().toString());

		if (!connected) {
			channelUnconnected();
		}
	}

}
