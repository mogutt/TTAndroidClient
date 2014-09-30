
package com.mogujie.tt.entity;

import java.io.Serializable;

public class GroupManagerEntity implements Serializable{

    private static final long serialVersionUID = 1L;

    private int headId;

    private String name;

    public GroupManagerEntity(int id, String name) {
        headId = id;
        this.name = name;
    }

    public int getHeadId() {
        return headId;
    }

    public String getName() {
        return name;
    }

}
