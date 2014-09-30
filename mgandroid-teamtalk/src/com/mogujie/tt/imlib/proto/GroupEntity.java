package com.mogujie.tt.imlib.proto;

import java.util.ArrayList;
import java.util.List;

public class GroupEntity {
	public String id;
	public String name;
	public String avatarUrl;
	public String creatorId;
	public int type; // 1--normal group, 2--temporary group
	public int updated;
	public List<String> memberIdList = new ArrayList<String>();
	
	public String pinyin;

	@Override
	public String toString() {
		 String ret = "GroupEntity [id=" + id + ", name=" + name + ", avatar="
				+ avatarUrl + ", creatorId=" + creatorId + ", type=" + type
				+ ", updated=" + updated + ", pinyin=" + pinyin +  "]";
		 
		 StringBuilder memberString = new StringBuilder("member ids:");
		 for (String id: memberIdList) {
			 memberString.append(id + ",");
		 }
		 
		 return ret + memberString;
	}

}
