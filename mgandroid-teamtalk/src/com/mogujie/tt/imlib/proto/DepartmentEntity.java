package com.mogujie.tt.imlib.proto;

public class DepartmentEntity {
	public String id;
	public String title;
	public String description;
	public String parentId;
	public String leaderId;
	public int status;
	
	public String pinyin;

	@Override
	public String toString() {
		return "id:" + id + ", title:" + title + ", description:" + description
				+ ", parentId:" + parentId + ", leaderId:" + leaderId
				+ ", status:" + String.valueOf(status) + ", pinyin:" + pinyin;
	}

}
