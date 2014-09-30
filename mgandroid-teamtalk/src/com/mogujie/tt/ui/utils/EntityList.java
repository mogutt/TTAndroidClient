package com.mogujie.tt.ui.utils;

import java.util.List;

import android.view.View;

public abstract class EntityList {
	public List<Object> list;
	private List<Object> backupList;
	protected boolean searchMode = false;

	public EntityList(List<Object> list) {
		this.list = list;
		this.backupList = list;
	}

	public void recoverFromBackup() {
		this.list = this.backupList;
	}

	public abstract String getSectionName(int position);

	public abstract void onItemClick(View view, int position);

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

}
