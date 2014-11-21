
package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;
import com.mogujie.tt.utils.SequenceNumberMaker;

public class HeartBeatPacket extends Packet {

    private Logger logger = Logger.getLogger(HeartBeatPacket.class);

    public HeartBeatPacket() {
        mRequest = new HeartBeatRequest();
        setNeedMonitor(false);
    }

    @Override
    public DataBuffer encode() {
        Header heartbeatHeader = mRequest.getHeader();
        DataBuffer headerBuffer = heartbeatHeader.encode();
        // 这个协议没有body
        return headerBuffer;
    }

    @Override
    public void decode(DataBuffer buffer) {
        if (null == buffer)
            return;
        try {
            HeartBeatResponse res = new HeartBeatResponse();
            Header ResponseHeartBeatHeader = new Header();
            ResponseHeartBeatHeader.decode(buffer);
            res.setHeader(ResponseHeartBeatHeader);
            mResponse = res;
        } catch (Exception e) {
            logger.e(e.getMessage());
        }

    }

    public static class HeartBeatRequest extends Request {

        public HeartBeatRequest() {
            Header recentcontactHeader = new Header();
            recentcontactHeader
                    .setVersion((short) SysConstant.PROTOCOL_VERSION);
            //recentcontactHeader.setFlag((short) SysConstant.PROTOCOL_FLAG);
            recentcontactHeader.setServiceId(ProtocolConstant.SID_DEFAULT);
            recentcontactHeader
                    .setCommandId(ProtocolConstant.CID_HEART_BEAT);
           // recentcontactHeader.setError((short) SysConstant.PROTOCOL_ERROR);
            short seqNo = SequenceNumberMaker.getInstance().make();
            recentcontactHeader.setReserved(seqNo);
            int contentLength = 0;
            recentcontactHeader.setLength(SysConstant.PROTOCOL_HEADER_LENGTH
                    + contentLength);

            setHeader(recentcontactHeader);
        }
    }

    // 服务器目前不会给心跳进行回复，但是会主动发心跳过来
    public static class HeartBeatResponse extends Response {

    }
}
