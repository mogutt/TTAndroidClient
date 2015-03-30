
package com.mogujie.tt.socket;

import org.jboss.netty.buffer.ChannelBuffer;

import org.jboss.netty.channel.ChannelHandlerContext;

import org.jboss.netty.channel.ChannelStateEvent;

import org.jboss.netty.channel.ExceptionEvent;

import org.jboss.netty.channel.MessageEvent;

import org.jboss.netty.channel.SimpleChannelHandler;

import android.os.Handler;

import android.os.Message;








import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.conn.ConnectionStore;
//import com.mogujie.tt.conn.ReconnectManager;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.MessageDispatchCenter;
import com.mogujie.tt.packet.base.DataBuffer;


public class SocketHandler extends SimpleChannelHandler {

    private SocketStateManager ssmInstance = SocketStateManager.getInstance();

    //private ReconnectManager //rcInstance = ReconnectManager.getInstance();

    private ConnectionStore cmInstance = ConnectionStore.getInstance();

    //private StateManager smInstance = StateManager.getInstance();

    protected Logger logger = Logger.getLogger(SocketHandler.class);

    public void channelConnected(ChannelHandlerContext context,

            ChannelStateEvent e) throws Exception {

        String clientAddr = context.getChannel().getRemoteAddress().toString();

        logger.i("[CONNECTED] ADDRESS:" + clientAddr);

        //Handler handler = LoginManager.getInstance().getHandler();

        Handler handler = null;
        if (null != handler)

        {

            Message msg = handler.obtainMessage();

            msg.what = HandlerConstant.HANDLER_CONNECT_SUCESS;

            msg.obj = clientAddr;

            handler.dispatchMessage(msg);

        }

        else

        {

            logger.e("Can not Send Message");

            //rcInstance.setLogining(false);

            //rcInstance.setOnRecconnecting(false);

        }

        // ssmInstance.setState(true);

    }

    @Override
    public void messageReceived(ChannelHandlerContext context,

            MessageEvent event) throws Exception {
    	
    	logger.d("messageReceived");

        ChannelBuffer originbuffer = (ChannelBuffer) event.getMessage();

        DataBuffer dataBuffer = new DataBuffer(originbuffer);

        // DataBuffer buffer = (DataBuffer) event.getMessage();

        //MessageDispatchCenter.getInstance().OnRecvMessage(dataBuffer);

    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)

            throws Exception {

        if (this == ctx.getPipeline().getLast()) {

            logger.e(e.toString());

        }

        ctx.sendUpstream(e);

        e.getCause().printStackTrace();

        //rcInstance.setOnRecconnecting(false);

        ssmInstance.setState(false);

        //rcInstance.setLogining(false);

        //smInstance.resetSockets();

    }

    public void channelDisconnected(ChannelHandlerContext context,

            ChannelStateEvent e) throws Exception {

        String clientAddr = context.getChannel().getRemoteAddress().toString();

        logger.i("packet#[ DISCONNECTED ] ADDRESS:" + clientAddr);

        super.channelDisconnected(context, e);

        MoGuSocket socketLogin = cmInstance.get(SysConstant.CONNECT_LOGIN_SERVER);

        if (null != socketLogin && null != socketLogin.getChannel())

        {

            String localAddr = socketLogin.getChannel().getRemoteAddress().toString();

            if (clientAddr.equals(localAddr))

            {

                socketLogin.close();

                socketLogin = null;

                cmInstance.remove(SysConstant.CONNECT_LOGIN_SERVER);

            }

        }

//        if ((!clientAddr.contains(LoginManager.getInstance().getLoginIp1())
//
//                || (clientAddr.contains(LoginManager.getInstance().getLoginIp1()) && !clientAddr
//
//                        .contains(String.valueOf(LoginManager.getInstance().getLoginPort())))) ||
//
//                (!clientAddr.contains(LoginManager.getInstance().getLoginIp2())
//
//                || (clientAddr.contains(LoginManager.getInstance().getLoginIp2()) && !clientAddr
//
//                        .contains(String.valueOf(LoginManager.getInstance().getLoginPort()))))) {
//
//            MoGuSocket socket = cmInstance.get(SysConstant.CONNECT_MSG_SERVER);
//
//            if (null != socket && null != socket.getChannel())
//
//            {
//
//                String localAddr = socket.getChannel().getRemoteAddress().toString();
//
//                if (localAddr.equals(clientAddr)) {
//
//                    ssmInstance.setState(false);
//
//                }
//
//            }
//
//        }
//
        context.getChannel().close();

    }

}
