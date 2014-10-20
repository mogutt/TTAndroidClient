package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.imlib.utils.SearchElement;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinElement;

public class ContactEntity {
	
	public String id;
	public String name;
	public String nickName;
	public String avatarUrl;
	public String title;
	// todo eric, change it to address
	public String position; // 地址
	public int roleStatus; // 用户在职状态 0:在职 1:离职
	public int sex; // 0:女 1:男
	public String departmentId;
	public int jobNum; // 工号
	public String telephone;
	public String email;
	
	//not protocol
	public PinYinElement pinyinElement = new PinYinElement();
	public SearchElement searchElement = new SearchElement();
	
	@Override
	public String toString() {
		return String.format(
				"id:%s, name:%s, nickName:%s, avatarUrl:%s, title:%s, position:%s, "
						+ "roleStatus:%d, sex:%d, departmentId:%s, jobNum:%d,"
						+ " telephone:%s, email:%s, pinyinElement:%s", id, name, nickName,
				avatarUrl, title, position, roleStatus, sex, departmentId,
				jobNum, telephone, email, pinyinElement);
	}
	
}