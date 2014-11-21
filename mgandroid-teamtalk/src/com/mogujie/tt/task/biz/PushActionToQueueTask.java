
package com.mogujie.tt.task.biz;

import com.mogujie.tt.packet.SocketMessageQueue;
import com.mogujie.tt.packet.action.Action;
import com.mogujie.tt.packet.action.ActionCallback;
import com.mogujie.tt.packet.action.Action.Builder;
import com.mogujie.tt.packet.base.Packet;
import com.mogujie.tt.task.BaseTask;

public class PushActionToQueueTask extends BaseTask {

    private Packet packet;
    private ActionCallback callback;

    public PushActionToQueueTask(Packet _packet, ActionCallback _callback)
    {
        packet = _packet;
        callback = _callback;
    }

    @Override
    public Object doTask() {
        if (null == packet)
            return null;
        Builder builer = new Builder();
        Action action = builer.setPacket(packet).setCallback(callback).build();
        SocketMessageQueue.getInstance().submitAndEnqueue(action);
        return null;
    }
}
