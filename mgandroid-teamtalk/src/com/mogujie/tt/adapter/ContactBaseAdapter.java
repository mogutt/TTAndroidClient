package com.mogujie.tt.adapter;

import java.util.List;
import java.util.Map;

import com.mogujie.tt.entity.ContactSortEntity;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;

public interface ContactBaseAdapter {
	int getPositionForSection(int section);

	int getSectionForPosition(int position);

	void updateListView(List<ContactSortEntity> list);

	Object getItem(int position);
}
