
package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.packet.base.DataBuffer;
import com.mogujie.tt.packet.base.Header;
import com.mogujie.tt.packet.base.Packet;
import com.mogujie.tt.utils.SequenceNumberMaker;

/**
 * MsgServerPacket:请求(返回)登陆消息服务器 yugui 2014-05-04
 */

public class LoginPacket extends Packet {

    private Logger logger = Logger.getLogger(LoginPacket.class);

    public LoginPacket() {
    	
    }
    
    public LoginPacket(String _user_id_url, String _user_token, int _online_status,
            int _client_type, String _client_version) {
        mRequest = new LoginRequest(_user_id_url, _user_token, _online_status, _client_type,
                _client_version);
        setNeedMonitor(true);
    }

    @Override
    public DataBuffer encode() {

        Header RequestLoginHeader = mRequest.getHeader();
        DataBuffer headerBuffer = RequestLoginHeader.encode();
        DataBuffer bodyBuffer = new DataBuffer();

        LoginRequest req = (LoginRequest) mRequest;
        if (null == req)
            return null;

        bodyBuffer.writeString(req.getUser_id_url());
        bodyBuffer.writeString(req.getUser_token());
        bodyBuffer.writeInt(req.getOnline_status());
        bodyBuffer.writeInt(req.getClient_type());
        bodyBuffer.writeString(req.getClient_version());

        int headLength = headerBuffer.readableBytes();
        int bodyLength = bodyBuffer.readableBytes();

        DataBuffer buffer = new DataBuffer(headLength + bodyLength);
        buffer.writeDataBuffer(headerBuffer);
        buffer.writeDataBuffer(bodyBuffer);

        return buffer;
    }

    @Override
    public void decode(DataBuffer buffer) {

        if (null == buffer)
            return;
        try {
            LoginResponse res = new LoginResponse();

            Header ResponseLoginHeader = new Header();
            ResponseLoginHeader.decode(buffer);
            res.setHeader(ResponseLoginHeader);

            if (ResponseLoginHeader.getServiceId() != ProtocolConstant.SID_LOGIN ||
                    ResponseLoginHeader.getCommandId() != ProtocolConstant.CID_LOGIN_RES_USERLOGIN)
                return;

            res.setServer_time(buffer.readInt());
            int nResult = buffer.readInt();
            res.setResult(nResult);

            if (nResult == 0) {
                res.setOnline_status(buffer.readInt());
                res.setUserId(buffer.readString(buffer.readInt()));
                res.setNickname(buffer.readString(buffer.readInt()));
                res.setAvatar_url(buffer.readString(buffer.readInt()));
                res.setTitle(buffer.readString(buffer.readInt()));
                res.setPosition(buffer.readString(buffer.readInt()));
                res.setRoleStatus(buffer.readInt());
                res.setSex(buffer.readInt());
                res.setDepartId(buffer.readString(buffer.readInt()));
                res.setJobNumber(buffer.readInt());
                res.setTelphone(buffer.readString(buffer.readInt()));
                res.setEmail(buffer.readString(buffer.readInt()));
                res.setToken(buffer.readString(buffer.readInt()));
            }
            mResponse = res;
        } catch (Exception e) {
            logger.e(e.getMessage());
        }

    }

    public static class LoginRequest extends Request {
        private String user_id_url;
        private String user_token;
        private int online_status;
        private int client_type;
        private String client_version;

        public LoginRequest(String _user_id_url, String _user_token, int _online_status,
                int _client_type, String _client_version) {
            user_id_url = _user_id_url;
            user_token = _user_token;
            online_status = _online_status;
            client_type = _client_type;
            client_version = _client_version;

            Header loginHeader = new Header();

            //loginHeader.setFlag((short) SysConstant.PROTOCOL_FLAG);
            loginHeader.setServiceId(ProtocolConstant.SID_LOGIN);
            loginHeader.setCommandId(ProtocolConstant.CID_LOGIN_REQ_USERLOGIN);
            loginHeader.setVersion((short) SysConstant.PROTOCOL_VERSION);
            //loginHeader.setError((short) SysConstant.PROTOCOL_ERROR);
            short seqNo = SequenceNumberMaker.getInstance().make();
            loginHeader.setReserved(seqNo);
            int contentLength = 4 + getUtf8Bytes(user_id_url).length+ 4 + getUtf8Bytes(user_token).length + 4 + 4 + 4
                    + getUtf8Bytes(client_version).length;
            loginHeader.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + contentLength);

            setHeader(loginHeader);
        }

        public String getClient_version() {
            return client_version;
        }

        public void setClient_version(String client_version) {
            this.client_version = client_version;
        }

        public int getOnline_status() {
            return online_status;
        }

        public void setOnline_status(int online_status) {
            this.online_status = online_status;
        }

        public int getClient_type() {
            return client_type;
        }

        public void setClient_type(int client_type) {
            this.client_type = client_type;
        }

        public String getUser_id_url() {
            return user_id_url;
        }

        public void setUser_id_url(String user_id_url) {
            this.user_id_url = user_id_url;
        }

        public String getUser_token() {
            return user_token;
        }

        public void setUser_token(String user_token) {
            this.user_token = user_token;
        }
    }

    public static class LoginResponse extends Response {
        private int server_time;
        private int result;
        private int online_status;
        private String userId;
        private String nickname;
        private String avatar_url;
        private String title;
        private String position;
        private int roleStatus;
        private int sex;
        private String departId;
        private int jobNumber;
        private String telphone;
        private String email;
        private String token;

        public LoginResponse() {

        }

        public User getUser() {
            User user = new User();
            user.setOnlineStatus(online_status);
            user.setUserId(userId);
            user.setNickName(nickname);
            user.setAvatarUrl(avatar_url);
            user.setTitle(title);
            user.setPosition(position);
            user.setRoleStatus(roleStatus);
            user.setSex(sex);
            user.setDepartId(departId);
            user.setJobNum(jobNumber);
            user.setTelphone(telphone);
            user.setEmail(email);
            user.setToken(token);

            return user;
        }

        public int getServer_time() {
            return server_time;
        }

        public void setServer_time(int server_time) {
            this.server_time = server_time;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public int getOnline_status() {
            return online_status;
        }

        public void setOnline_status(int online_status) {
            this.online_status = online_status;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public int getRoleStatus() {
            return roleStatus;
        }

        public void setRoleStatus(int roleStatus) {
            this.roleStatus = roleStatus;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public String getDepartId() {
            return departId;
        }

        public void setDepartId(String departId) {
            this.departId = departId;
        }

        public int getJobNumber() {
            return jobNumber;
        }

        public void setJobNumber(int jobNumber) {
            this.jobNumber = jobNumber;
        }

        public String getTelphone() {
            return telphone;
        }

        public void setTelphone(String telphone) {
            this.telphone = telphone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
