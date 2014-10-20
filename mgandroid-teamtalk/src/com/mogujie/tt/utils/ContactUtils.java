package com.mogujie.tt.utils;

import com.mogujie.tt.imlib.proto.ContactEntity;

public class ContactUtils {

	public static String getSectionName(ContactEntity contact) {
		return contact.pinyinElement.pinyin.substring(0, 1);
	}

}
