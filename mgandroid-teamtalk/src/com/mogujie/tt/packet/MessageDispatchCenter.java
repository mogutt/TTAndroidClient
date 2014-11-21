
package com.mogujie.tt.packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.imlib.IMPacketDispatcher;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.action.Action;
import com.mogujie.tt.packet.action.ActionCallback;
import com.mogujie.tt.packet.action.Action.Builder;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;

/**
 * 消息处理中心
 * 
 * @author dolphinWang
 * @time 2014/05/03
 */
public class MessageDispatchCenter {
    /**
     * 消息列表，可以自行添加
     */
    public static final int MESSAGE_OFFLINE = 0x0001;
    public static final int MESSAGE_ONLINE = 0x0002;

    private static final ConcurrentHashMap<Integer, ArrayList<Handler>> mHandlerMap = new ConcurrentHashMap<Integer, ArrayList<Handler>>();
    private static final List<Handler> mInterestAllList = new LinkedList<Handler>();
    private Logger logger = Logger.getLogger(MessageDispatchCenter.class);

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            final Object obj = msg.obj;

            final ArrayList<Handler> list = mHandlerMap.get(what);

            if (null != list) {
                for (Handler handler : list) {
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(what, obj));
                    }
                }
            }

            synchronized (mInterestAllList) {
                for (Handler handler : mInterestAllList) {
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(what, obj));
                    } else {
                        mInterestAllList.remove(handler);
                    }
                }
            }
            super.handleMessage(msg);
        }

    };

    private MessageDispatchCenter() {
    }

    private static class SingletonHolder {
        static MessageDispatchCenter center = new MessageDispatchCenter();
    }

//    public void OnRecvMessage(DataBuffer buffer) {
//    	logger.d("OnRecvMessage");
//        if (null == buffer) {
//        	logger.d("buffer is empty");
//            return;
//        }
//
//        try {
//            Header header = new Header();
//            header.decode(buffer);
//
//            
//            buffer.resetReaderIndex();
//            int seqNo = header.getReserved();
//            int commandId = header.getCommandId();
//            int serviceId = header.getServiceId();
//            
//            ProtocolConstant.ProtocolDumper.dump(false, header);
//            
//            /*
//            if (serviceId == ProtocolConstant.SID_MSG || serviceId == ProtocolConstant.SID_GROUP) {
//            	//todo eric
//            	IMPacketDispatcher.dispatch(serviceId, commandId, buffer);
//            	return;
//            }
//            */
//            
//
//            if (serviceId == ProtocolConstant.SID_MSG
//                    && commandId == ProtocolConstant.CID_MSG_UNREAD_MSG_RESPONSE) {
//            }
//            Packet packet = null;
//            Action action = SocketMessageQueue.getInstance()
//                    .getFromWaitingList(seqNo);
//            if (null == action) {// waitingList中没有找到，说明不是我们主动请求的
//                // 脏包直接抛弃
//                // TODO:
//                if (seqNo != 0) { 
//                    // 注释掉下面一行：@lanhu 不知道当时为什么要这样做
//                    // packet = PacketDistinguisher.make(serviceId, commandId,
//                    // null, false);
//                	
//                	logger.w("packet#seqNo is not 0, should be new packets");
//                	//IMPacketDispatcher.dispatch(serviceId, commandId, buffer);
//       
//                	
//                    return;
//                }
//
//                // SERVER推过来的数据
//                packet = PacketDistinguisher.make(serviceId, commandId, null,
//                        false);
//                if (null == packet)
//                    return;
//                Builder builder = new Builder();
//                action = builder.setPacket(packet).setCallback(null).build();
//                action.getPacket().decode(buffer);
//                packet = action.getPacket();
//            } else {
//                action.getPacket().decode(buffer);
//                packet = action.getPacket();
//                ActionCallback callback = action.getCallback();
//                if (null != callback && null != packet.getResponse()) {
//                    callback.onSuccess(packet);
//                }
//            }
//
//            if (null != packet.getResponse()) {
//                Message msg = new Message();
//                msg.what = serviceId * 1000 + commandId;
//                msg.obj = packet;
//                mHandler.sendMessage(msg);
//            } else {
//                logger.e(
//                        " packet.getResponse() == null sid = " + serviceId
//                                + " cid = " + commandId);
//            }
//        } catch (Exception e) {
//        	logger.e("find exception");
//            logger.e(e.getMessage());
//        }
//
//    }

    public static MessageDispatchCenter getInstance() {
        return SingletonHolder.center;
    }

    public void register(Handler hander, int... messages) {

        synchronized (mHandlerMap) {
            if (null == messages || messages.length == 0 || null == hander) {
                return;
            }

            for (int message : messages) {
                ArrayList<Handler> handlerList = mHandlerMap.get(message);

                if (null == handlerList) {
                    handlerList = new ArrayList<Handler>();

                    handlerList.add(hander);
                    mHandlerMap.put(message, handlerList);
                } else {
                    handlerList.add(hander);
                }
            }
        }
    }

    public void registerAllInterest(Handler hander) {
        if (null == hander)
            return;
        synchronized (mInterestAllList) {
            if (!mInterestAllList.contains(hander))
                mInterestAllList.add(hander);
        }
    }

    public void unregisterAllInterest(Handler hander) {
        if (null == hander)
            return;
        synchronized (mInterestAllList) {
            if (mInterestAllList.contains(hander))
                mInterestAllList.remove(hander);
        }
    }

    public void unRegister(Handler hander) {
        synchronized (mHandlerMap) {
            if (null == hander) {
                return;
            }

            Iterator<Entry<Integer, ArrayList<Handler>>> itor = mHandlerMap
                    .entrySet().iterator();

            while (itor.hasNext()) {
                Entry<Integer, ArrayList<Handler>> entry = itor.next();

                final ArrayList<Handler> list = entry.getValue();

                if (null != list && list.contains(hander)) {
                    list.remove(hander);
                }
            }
        }
    }

    /**
     * 这个函数提供针对某个handler的部分“取消注册”。如果messages参数为空或者没有值但handler不为空，
     * 表示取消某个handler全部注册。 如果messages参数部位空但handler为空，表示取消某些message得注册。
     * 
     * @param hander
     * @param messages
     */
    public void unRegister(Handler hander, int... messages) {
        synchronized (mHandlerMap) {
            if (null == messages && null == hander) {
                return;
            }

            if (null == messages && null != hander) {
                unRegister(hander);
            } else if (null != messages && messages.length == 0
                    && null == hander) {
                unRegister(messages);
            }

            for (int message : messages) {
                final ArrayList<Handler> list = mHandlerMap.get(message);

                if (null != list) {
                    list.remove(hander);
                }
            }
        }
    }

    /**
     * 这个函数提供针对某个message的全部“取消注册”
     * 
     * @param messages
     */
    public void unRegister(int... messages) {
        synchronized (mHandlerMap) {
            for (int message : messages) {
                final ArrayList<Handler> list = mHandlerMap.get(message);
                list.clear();
            }
        }
    }

    public Handler getDispatcher() {
        return mHandler;
    }
}
