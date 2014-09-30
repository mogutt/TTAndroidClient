
package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;
import com.mogujie.tt.utils.SequenceNumberMaker;

/**
 * MsgServerPacket:请求(返回)分配一个消息服务器的IP和端口 yugui 2014-05-04
 */

public class MsgServerPacket extends Packet {

    private Logger logger = Logger.getLogger(MsgServerPacket.class);

    public MsgServerPacket() {
        mRequest = new MsgServerRequest();
        setNeedMonitor(true);
    }

    @Override
    public DataBuffer encode() {
        Header RequestMsgServerHeader = mRequest.getHeader();
        DataBuffer headerBuffer = RequestMsgServerHeader.encode();
        int readable = headerBuffer.readableBytes();

        DataBuffer buffer = new DataBuffer(readable);
        buffer.writeDataBuffer(headerBuffer);

        return buffer;
    }

    @Override
    public void decode(DataBuffer buffer) {
        if (null == buffer)
            return;

        try {
            MsgServerResponse res = new MsgServerResponse();

            Header ResponseMsgServerHeader = new Header();
            ResponseMsgServerHeader.decode(buffer);
            res.setHeader(ResponseMsgServerHeader);

            if (ResponseMsgServerHeader.getServiceId() != ProtocolConstant.SID_LOGIN
                    ||
                    ResponseMsgServerHeader.getCommandId() != ProtocolConstant.CID_LOGIN_RES_MSGSERVER)
                return;

            int nResult = buffer.readInt();
            res.setResult(nResult);
            if (nResult == 0) {
                int len = buffer.readInt();
                res.setStrIp1(buffer.readString(len));
                len = buffer.readInt();
                res.setStrIp2(buffer.readString(len));
                res.setPort(buffer.readShort());
            }

            mResponse = res;
        } catch (Exception e) {
            logger.e(e.getMessage());
        }

    }

    public static class MsgServerRequest extends Request {

        public MsgServerRequest() {

            Header msrHeader = new Header();
            msrHeader.setVersion((short) SysConstant.PROTOCOL_VERSION);
            //msrHeader.setFlag((short) SysConstant.PROTOCOL_FLAG);
            msrHeader.setServiceId(ProtocolConstant.SID_LOGIN);
            msrHeader.setCommandId(ProtocolConstant.CID_LOGIN_REQ_MSGSERVER);
            //msrHeader.setError((short) SysConstant.PROTOCOL_ERROR);
            short seqNo = SequenceNumberMaker.getInstance().make();
            msrHeader.setReserved(seqNo);
            msrHeader.setLength(SysConstant.PROTOCOL_HEADER_LENGTH);
            
            setHeader(msrHeader);
        }
    }

    public static class MsgServerResponse extends Response {
        private int result;
        private String strIp1;
        private String strIp2;
        private short port;

        public MsgServerResponse() {

        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public String getStrIp1() {
            return strIp1;
        }

        public void setStrIp1(String strIp1) {
            this.strIp1 = strIp1;
        }

        public String getStrIp2() {
            return strIp2;
        }

        public void setStrIp2(String strIp2) {
            this.strIp2 = strIp2;
        }

        public short getPort() {
            return port;
        }

        public void setPort(short port) {
            this.port = port;
        }

    }
}
