package com.mogujie.tt.imlib.proto;

import com.mogujie.tt.imlib.utils.SearchElement;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinElement;

public class DepartmentEntity {
	public String id;
	public String title;
	public String description;
	public String parentId;
	public String leaderId;
	public int status;
	
	public PinYinElement pinyinElement = new PinYinElement();
	public SearchElement searchElement = new SearchElement();

	@Override
	public String toString() {
		return "id:" + id + ", title:" + title + ", description:" + description
				+ ", parentId:" + parentId + ", leaderId:" + leaderId
				+ ", status:" + String.valueOf(status);
	}

}
