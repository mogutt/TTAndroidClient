package com.mogujie.tt.imlib.proto;

import java.util.ArrayList;
import java.util.List;

import com.mogujie.tt.imlib.utils.SearchElement;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinElement;

public class GroupEntity {
	public String id;
	public String name;
	public String avatarUrl;
	public String creatorId;
	public int type; // 1--normal group, 2--temporary group
	public int updated;
	public int shieldStatus;
	public List<String> memberIdList = new ArrayList<String>();

	public PinYinElement pinyinElement = new PinYinElement();
	public SearchElement searchElement = new SearchElement();

	@Override
	public String toString() {
		String ret = "GroupEntity [id=" + id + ", name=" + name + ", avatar="
				+ avatarUrl + ", creatorId=" + creatorId + ", type=" + type
				+ ", updated=" + updated + ", shieldStatus=" + shieldStatus + "memberCnt=" + memberIdList.size()
				+ "]";

		StringBuilder memberString = new StringBuilder("member ids:");
		for (String id : memberIdList) {
			memberString.append(id + ",");
		}

		return ret + memberString;
	}

}
