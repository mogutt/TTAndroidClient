package com.mogujie.tt.ui.utils;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;

public abstract class EntityList {
	public List<Object> list;
	protected List<Object> backupList;
	protected boolean searchMode = false;
	public boolean isSearchMode() {
		return searchMode;
	}

	public void setSearchMode(boolean searchMode) {
		this.searchMode = searchMode;
	}

	Logger logger = Logger.getLogger(EntityList.class);

	public EntityList(List<Object> list) {
		this.list = list;
		this.backupList = list;
	}

	public void recoverFromBackup() {
		this.list = this.backupList;
	}

	public abstract String getSectionName(int position);

	public void onItemClick(Context ctx, View view, int position) {
		Object object = list.get(position);
		if (object instanceof ContactEntity) {
			handleContactItemClick(ctx, (ContactEntity)object);
		} else if (object instanceof GroupEntity) {
			handleGroupItemClick(ctx, (GroupEntity)object);
		}
	}
	
	protected void handleContactItemClick(Context ctx, ContactEntity contact) {
		IMUIHelper.openContactChatActivity(ctx, contact);
	}

	protected void handleGroupItemClick(Context ctx, GroupEntity group) {
		IMUIHelper.openGroupChatActivity(ctx, group);
	}
	

	public void onSearch(String key) {
		if (key.isEmpty()) {
			searchMode = false;

			recoverFromBackup();

			return;
		}

		searchMode = true;

		onSearchImpl(key);
	}

	protected abstract void onSearchImpl(String key);

	// could be find indexed by Chinase pinyin first character
	public boolean isPinYinIndexable() {
		return false;
	}

	public int getPinYinFirstCharacter(int position) {
		return 0;
	}

	public boolean shouldCheckBoxChecked(int position) {
		return false;
	}

	public void onItemLongClick(View view, Context ctx, int position) {
		if (position >= list.size()) {
			logger.e("position:%d is overflow", position);
			return;
		}

		Object obj = list.get(position);
		if (obj instanceof ContactEntity) {
			IMUIHelper.handleContactItemLongClick(ctx, (ContactEntity) obj);
		}
	}

	
}
