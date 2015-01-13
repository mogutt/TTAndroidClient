
package com.mogujie.tt.biz;

import java.util.ArrayList;
import java.util.List;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.SearchResultItem;

/**
 * 查找功能辅助类
 * 
 * @author fengchen
 */
public class SearchHelper {
    private static List<SearchResultItem> resultList = new ArrayList<SearchResultItem>();

    public SearchHelper() {
        super();
    }

    public static List<SearchResultItem> getResultList() {
        return resultList;
    }

    public static void clear() {
        resultList.clear();
    }

    public static List<SearchResultItem> search(String text) {
        resultList.clear();

        resultList.addAll(searchContact(text));
        resultList.addAll(searchChat(text));

        return resultList;
    }

    public static List<SearchResultItem> searchContact(String text) {
        List<SearchResultItem> list = new ArrayList<SearchResultItem>();

        SearchResultItem item = new SearchResultItem();
        item.setTitle("联系人");
        item.setType(SysConstant.CHAT_SEARCH_RESULT_TYPE_CATEGORY);
        list.add(item);

        item = new SearchResultItem();
        item.setTitle(text);
        item.setUserId("12345");
        list.add(item);
        return list;
    }

    public static List<SearchResultItem> searchChat(String text) {
        List<SearchResultItem> list = new ArrayList<SearchResultItem>();

        SearchResultItem item = new SearchResultItem();
        item.setTitle("聊天记录");
        item.setType(SysConstant.CHAT_SEARCH_RESULT_TYPE_CATEGORY);
        list.add(item);

        item = new SearchResultItem();
        item.setTitle("风尘");
        item.setUserId("1234567");
        item.setContent("Hello, " + text);
        list.add(item);

        item = new SearchResultItem();
        item.setTitle("纳纳");
        item.setUserId("1234567");
        item.setContent("Hello, " + text);
        list.add(item);
        return list;
    }
}
