
package com.mogujie.tt.cache;

import com.mogujie.tt.entity.MessageInfo;

/**
 * @author seishuchen
 */
public interface Dispatcher {
    void dispatch(MessageInfo messageInfo);

    void init();
}
