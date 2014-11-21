
package com.mogujie.tt.packet.action;

import com.mogujie.tt.packet.base.Packet;

/**
 * 基础的action callback
 * 
 * @author dolphinWang
 */
public interface ActionCallback {
    void onSuccess(Packet packet);

    void onFaild(Packet packet);

    void onTimeout(Packet packet);
}
