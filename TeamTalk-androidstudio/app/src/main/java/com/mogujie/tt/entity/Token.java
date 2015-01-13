
package com.mogujie.tt.entity;

import java.io.Serializable;

/**
 * 从服务器换取token的时候返回的数据结构
 * 
 * @author dolphinWang
 */
public class Token implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String token = "";
    private String dao = "";
    private String strHosts = "";

    private int exceptionCode = -1;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDao() {
        return dao;
    }

    public void setDao(String dao) {
        this.dao = dao;
    }

    public String getHosts() {
        return strHosts;
    }

    public void setHosts(String strHosts) {
        this.strHosts = strHosts;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }
}
