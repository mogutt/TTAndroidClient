
package com.mogujie.tt.entity;

public class ShopMember {
    public ShopMember() {

    }

    public ShopMember(int _shop_id) {
        this.shop_id = _shop_id;
    }

    public int getShop_id() {
        return shop_id;
    }

    public void setShop_id(int shop_id) {
        this.shop_id = shop_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_nick_name() {
        return user_nick_name;
    }

    public void setUser_nick_name(String user_nick_name) {
        this.user_nick_name = user_nick_name;
    }

    public String getUser_avatarString() {
        return user_avatarString;
    }

    public void setUser_avatarString(String user_avatarString) {
        this.user_avatarString = user_avatarString;
    }

    public int getUser_role() {
        return user_role;
    }

    public void setUser_role(int user_role) {
        this.user_role = user_role;
    }

    private int shop_id;
    private String user_id;
    private String user_name;
    private String user_nick_name;
    private String user_avatarString;
    private int user_role;

}
