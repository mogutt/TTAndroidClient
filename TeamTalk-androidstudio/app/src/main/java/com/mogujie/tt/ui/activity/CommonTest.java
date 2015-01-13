package com.mogujie.tt.ui.activity;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.SearchElement;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.utils.Md5Helper;
import com.mogujie.tt.utils.pinyin.PinYin;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinElement;

public class CommonTest {

	private static Logger logger = Logger.getLogger(CommonTest.class);

	//private static HandlerThread networkThread = new HandlerThread("network");
	//private static Handler networkHandler = new Handler(networkThread.getLooper());

	private static void testLog() {
		Logger logger = Logger.getLogger(CommonTest.class);
		logger.v("no format");
		logger.v("%s_%d_%f", "string", 1, 1.3f);
		logger.d("%s_%d_%f", "string", 1, 1.3f);
		logger.i("%s_%d_%f", "string", 1, 1.3f);
		logger.w("%s_%d_%f", "string", 1, 1.3f);
		logger.e("%s_%d_%f", "string", 1, 1.3f);

		//deliberately test error cases
		//		logger.d("%s_%d_%f", "string");
		//		logger.d("%s",1);

	}

	private static void testNettyClient() {
		final class ClientHandler extends SimpleChannelHandler {

			@Override
			public void channelClosed(ChannelHandlerContext ctx,
					ChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.channelClosed(ctx, e);
				logger.d("channelClose");
			}

			@Override
			public void channelConnected(ChannelHandlerContext ctx,
					ChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.channelConnected(ctx, e);

				logger.d("channelConnected");

				String msg = "GET / HTTP/1.1\r\nHOST:www.sina.com.cn\r\n\r\n";
				ChannelBuffer buffer = ChannelBuffers.buffer(msg.length());
				buffer.writeBytes(buffer);

				e.getChannel().write(buffer);

				logger.d("write message ok");
			}

			@Override
			public void channelDisconnected(ChannelHandlerContext ctx,
					ChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.channelDisconnected(ctx, e);

				logger.d("channelDisconnected");
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					ExceptionEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.exceptionCaught(ctx, e);

				logger.d("exceptionCaught");

				logger.e(e.getCause().toString());
			}

			@Override
			public void messageReceived(ChannelHandlerContext ctx,
					MessageEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.messageReceived(ctx, e);

				logger.d("messageReceived");
			}

			@Override
			public void writeComplete(ChannelHandlerContext ctx,
					WriteCompletionEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.writeComplete(ctx, e);

				logger.d("wrieComplete");
			}

		}

		NioClientSocketChannelFactory factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				// TODO Auto-generated method stub
				return org.jboss.netty.channel.Channels.pipeline(new ClientHandler());
			}
		});

		bootstrap.connect(new InetSocketAddress("61.172.201.19", 80));
		//nioclientsocketchannelfactory is usually 1 socket in 1 thread, but server channel factory can
		//hold more workers in one same thread
		//bootstrap.connect(new InetSocketAddress("61.172.201.19", 80));
		//bootstrap.connect(new InetSocketAddress("61.172.201.19", 80));

	}

	private static void testNettyServer() {
		final class DiscardServerHandler extends SimpleChannelHandler {

			@Override
			public void channelClosed(ChannelHandlerContext ctx,
					ChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.channelClosed(ctx, e);

				logger.d("channelClosed");
			}

			@Override
			public void channelConnected(ChannelHandlerContext ctx,
					ChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.channelConnected(ctx, e);

				logger.d("channelConnected");

			}

			@Override
			public void channelDisconnected(ChannelHandlerContext ctx,
					ChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.channelDisconnected(ctx, e);

				logger.d("channelDisconnected");

			}

			@Override
			public void childChannelClosed(ChannelHandlerContext ctx,
					ChildChannelStateEvent e) throws Exception {
				// TODO Auto-generated method stub
				super.childChannelClosed(ctx, e);

				logger.d("childChannelClosed");
			}

			@Override
			public void messageReceived(ChannelHandlerContext ctx,
					MessageEvent e) throws Exception {
				logger.d("messageReceived");
				// TODO Auto-generated method stub
				//				ChannelBuffer buf = (ChannelBuffer)e.getMessage();
				//				while (buf.readable()) {
				//					System.out.println((char)buf.readByte());
				//					System.out.flush();
				//				}

				e.getChannel().write(e.getMessage());
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					ExceptionEvent e) throws Exception {
				logger.e("exceptionCaught");

				// TODO Auto-generated method stub
				e.getCause().printStackTrace();

				Channel channel = e.getChannel();
				channel.close();
			}

		}
		NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				// TODO Auto-generated method stub
				return org.jboss.netty.channel.Channels.pipeline(new DiscardServerHandler());
			}
		});

		bootstrap.bind(new InetSocketAddress(8080));
		bootstrap.setOption("reuseAddress", true);
	}

	private static void testMd5() {
		String content = "111111";
		String result = Md5Helper.encode(content);
		Logger logger = Logger.getLogger(CommonTest.class);
		logger.d("md5#%s", result);
	}

	private static void testPinyin() {

		PinYinElement pinyinElement = new PinYinElement();
		PinYin.getPinYin(logger, "你xyz好", pinyinElement);
		logger.d("pinyin#pinyinElement:%s", pinyinElement);
	}

	private static void testHandleNameSearch() {
		SearchElement searchElement = new SearchElement();
		IMUIHelper.handleNameSearch("我是满山xyz啊", "满山", searchElement);

		logger.d("pinyin#testHandleNameSearch searchElement:%s", searchElement);
	}

	private static void testHandleTokenFirstCharsSearch() {
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenFirstCharsSearch("ms", pinyinElement, searchElement);
			logger.d("pinyin#testHandleTokenFirstCharsSearch 满山 searchElement:%s", searchElement);
		}

		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "徐明刚", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenFirstCharsSearch("mg", pinyinElement, searchElement);
			logger.d("pinyin#testHandleTokenFirstCharsSearch 徐明刚 searchElement:%s", searchElement);
		}
		

		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "徐明刚", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenFirstCharsSearch("ms", pinyinElement, searchElement);
			logger.d("pinyin#testHandleTokenFirstCharsSearch wrong searchElement:%s", searchElement);
		}
	}
	
	
	public static void testHandlePinyinFullSearch() {
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("ma", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [满山, ma] searchElement:%s", searchElement);
		}
		
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("man", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [满山, man] searchElement:%s", searchElement);
		}
		
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("mans", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [满山, mans] searchElement:%s", searchElement);
		}
		
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("anshan", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [满山, anshan] searchElement:%s", searchElement);
		}
		
		
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("manshanX", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [满山, manshanX] searchElement:%s", searchElement);
		}
		
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "满山", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("m", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [满山, m] searchElement:%s", searchElement);
		}
		
		{
			PinYinElement pinyinElement = new PinYinElement();
			PinYin.getPinYin(logger, "安卓端IM", pinyinElement);
			
			SearchElement searchElement = new SearchElement();
			IMUIHelper.handleTokenPinyinFullSearch("duan", pinyinElement, searchElement);
			logger.d("pinyin#handlePinyinFullSearch [安卓端IM, duan] searchElement:%s", searchElement);
		}
		
	}

	public static void test() {
		//testHandleNameSearch();
		testHandlePinyinFullSearch();
//		testHandleTokenFirstCharsSearch();
		//testPinyin();
		//testMd5();
		//testLog();
		//		testNettyServer();
		//crash
		//		networkHandler.post(new Runnable() {
		//			
		//			@Override
		//			public void run() {
		//				// TODO Auto-generated method stub
		//				testNettyClient();
		//			}
		//		});

		//		new Thread(new Runnable() {
		//			
		//			@Override
		//			public void run() {
		//				logger.d("thread id:%d", Thread.currentThread().getId());
		//				// TODO Auto-generated method stub
		//				testNettyClient();
		//			}
		//		}).start();

	}

}
