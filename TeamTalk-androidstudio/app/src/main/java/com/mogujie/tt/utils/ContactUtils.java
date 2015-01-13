package com.mogujie.tt.utils;

import android.text.TextUtils;

import com.mogujie.tt.imlib.proto.ContactEntity;

public class ContactUtils {

    public static String getSectionName(ContactEntity contact) {
        if (!TextUtils.isEmpty(contact.pinyinElement.pinyin)) {
            return contact.pinyinElement.pinyin.substring(0, 1);
        } else {
            return "";
        }
    }
}
